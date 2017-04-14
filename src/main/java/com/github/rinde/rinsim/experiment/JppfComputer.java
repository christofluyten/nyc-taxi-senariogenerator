/*
 * Copyright (C) 2011-2017 Rinde van Lon, imec-DistriNet, KU Leuven
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.rinde.rinsim.experiment;

import com.github.rinde.rinsim.experiment.Experiment.Builder;
import com.github.rinde.rinsim.experiment.Experiment.SimArgs;
import com.github.rinde.rinsim.experiment.Experiment.SimulationResult;
import com.github.rinde.rinsim.experiment.PostProcessor.FailureStrategy;
import com.github.rinde.rinsim.scenario.Scenario;
import com.github.rinde.rinsim.scenario.ScenarioIO;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import com.google.common.collect.*;
import com.google.common.math.DoubleMath;
import org.jppf.JPPFException;
import org.jppf.client.JPPFClient;
import org.jppf.client.JPPFJob;
import org.jppf.client.event.TaskResultEvent;
import org.jppf.client.event.TaskResultListener;
import org.jppf.node.protocol.AbstractTask;
import org.jppf.node.protocol.Task;
import org.jppf.task.storage.DataProvider;
import org.jppf.task.storage.MemoryMapDataProvider;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.math.RoundingMode;
import java.util.*;

import static com.google.common.base.Preconditions.*;
import static com.google.common.base.Verify.verifyNotNull;

final class JppfComputer implements Computer {
  private static Optional<JPPFClient> client = Optional.absent();
  private static final String JOB_NAME = "RinSim - Experiment";
  private static final long THREAD_SLEEP_MS = 1000L;

  JppfComputer() {}

  static JPPFClient getJppfClient() {
    if (!client.isPresent()) {
      client = Optional.of(new JPPFClient());
    }

    return client.get();
  }

  @Override
  public ExperimentResults compute(Builder builder, Set<SimArgs> inputs) {
    final IdMap<MASConfiguration> configMap = new IdMap<>("c",
      MASConfiguration.class);
    final IdMap<ScenarioProvider> scenarioMap = new IdMap<>("s",
      ScenarioProvider.class);

    final List<ResultListener> listeners =
      new ArrayList<>(builder.resultListeners);

    @SuppressWarnings({"rawtypes", "unchecked"})
    final IdMap<PostProcessor<?>> ppMap = new IdMap("p", PostProcessor.class);
    final Map<String, Scenario> scenariosMap = new LinkedHashMap<>();

    // create tasks
    final List<SimulationTask> tasks = new ArrayList<>();
    constructTasks(inputs, tasks, configMap, scenarioMap, ppMap, scenariosMap);

    // this sorts tasks using this chain: scenario, configuration, objective
    // function, postprocessor, seed
    Collections.sort(tasks);

    // determine size of batches
    final int numBatches = Math.min(tasks.size(), builder.numBatches);
    final int batchSize = DoubleMath.roundToInt(tasks.size()
      / (double) numBatches,
      RoundingMode.CEILING);

    final Map<Task<?>, JPPFJob> taskJobMap = new LinkedHashMap<>();
    final ResultsCollector res = new ResultsCollector(tasks.size(),
      scenariosMap, taskJobMap, listeners);
    final List<JPPFJob> jobs = new ArrayList<>();

    final int compositeTaskSize = builder.compositeTaskSize;
    for (int i = 0; i < numBatches; i++) {
      final JPPFJob job = new JPPFJob(new MemoryMapDataProvider(), res);
      job.setName(Joiner.on("").join(JOB_NAME, " ", i + 1, "/", numBatches));
      jobs.add(job);

      final List<SimulationTask> batch =
        tasks.subList(i * batchSize, (i + 1) * batchSize);

      for (int j = 0; j < batch.size(); j += compositeTaskSize) {

        final List<SimulationTask> compositeTasks =
          batch.subList(j, Math.min(batch.size(), j + compositeTaskSize));

        for (final SimulationTask t : compositeTasks) {
          final MASConfiguration config = configMap.getValue(t
            .getConfigurationId());
          final ScenarioProvider scenario = scenarioMap.getValue(t
            .getScenarioId());
          job.getDataProvider().setParameter(t.getPostProcessorId(),
            ppMap.getValue(t.getPostProcessorId()));
          job.getDataProvider().setParameter(t.getConfigurationId(), config);
          job.getDataProvider().setParameter(t.getScenarioId(), scenario);

          taskJobMap.put(t, job);
        }

        try {
          if (compositeTasks.size() > 1) {
            final CompositeTask composite = new CompositeTask(compositeTasks);
            job.add(composite);
          } else {
            for (final SimulationTask st : compositeTasks) {
              job.add(st);
            }
          }
        } catch (final JPPFException e) {
          throw new IllegalStateException(e);
        }
      }
    }

    checkState(!getJppfClient().isClosed());
    try {
      int count = 0;
      for (final JPPFJob job : jobs) {
        count++;
        getJppfClient().submitJob(job);
      }
    } catch (final Exception e) {
      throw new IllegalStateException(e);
    }
    res.awaitResults();

    final ExperimentResults results =
      ExperimentResults.create(builder, res.buildResults());
    for (final ResultListener l : listeners) {
      l.doneComputing(results);
    }
    return results;
  }

  static void constructTasks(
      Set<SimArgs> inputs,
      List<SimulationTask> tasks,
      IdMap<MASConfiguration> configMap,
      IdMap<ScenarioProvider> scenarioMap,
      IdMap<PostProcessor<?>> ppMap,
      Map<String, Scenario> scenariosMap) {

    for (final SimArgs args : inputs) {
      final String configId = configMap.storeAndGenerateId(
        args.getMasConfig());
      final String scenId = scenarioMap.storeAndGenerateId(
        new ScenarioProvider(ScenarioIO.write(args.getScenario()),
          args.getScenario().getClass()));
      scenariosMap.put(scenId, args.getScenario());

      final String postProcId =
        ppMap.storeAndGenerateId(args.getPostProcessor());
      tasks.add(new SimulationTask(args.getRandomSeed(), args.getRepetition(),
        scenId, configId, postProcId));
    }
  }

  static SimulationResult processResult(SimulationTask simTask,
      final Map<String, Scenario> scenariosMap,
      final Map<Task<?>, JPPFJob> jobMap) {
    checkNotNull(simTask);
    if (simTask.getThrowable() != null) {
      throw new IllegalArgumentException(simTask.getThrowable());
    }
    final Object result = simTask.getResult();
    final Scenario scen = scenariosMap.get(simTask.getScenarioId());

    final DataProvider dp = jobMap.get(simTask).getDataProvider();
    final MASConfiguration conf = dp.getParameter(simTask.getConfigurationId());
    final PostProcessor<?> pp = dp.getParameter(simTask.getPostProcessorId());

    final SimArgs args =
      SimArgs.create(scen, conf, simTask.getSeed(), simTask.getRepetition(),
        false, pp, null);
    return SimulationResult.create(args, result);
  }

  static class ResultsCollector implements TaskResultListener {
    private final ImmutableSet.Builder<SimulationResult> results;
    private final Map<String, Scenario> scenariosMap;
    private final Map<Task<?>, JPPFJob> taskJobMap;
    private final List<ResultListener> listeners;
    private final int expectedNumResults;
    private int receivedNumResults;
    private Optional<IllegalArgumentException> exception;

    ResultsCollector(int expectedNumberOfResults,
        final Map<String, Scenario> scenMap,
        final Map<Task<?>, JPPFJob> tjMap, List<ResultListener> list) {
      results = ImmutableSet.builder();
      scenariosMap = scenMap;
      taskJobMap = tjMap;
      listeners = list;
      expectedNumResults = expectedNumberOfResults;
      receivedNumResults = 0;
      exception = Optional.absent();
    }

    @Override
    public void resultsReceived(@Nullable TaskResultEvent event) {
      final TaskResultEvent ev = verifyNotNull(event);
      for (final Task<?> t : ev.getTasks()) {
        final List<SimulationTask> simTasks = ((RinSimTask) t).getTasks();
        try {
          for (final SimulationTask simTask : simTasks) {
            final SimulationResult res = processResult(simTask, scenariosMap,
              taskJobMap);

            results.add(res);
            for (final ResultListener l : listeners) {
              l.receive(res);
            }
          }
        } catch (final IllegalArgumentException iae) {
          exception = Optional.of(iae);
        }
        receivedNumResults += simTasks.size();
      }
    }

    void awaitResults() {
      while (!isComplete() && !exception.isPresent()) {
        try {
          Thread.sleep(THREAD_SLEEP_MS);
        } catch (final InterruptedException e) {
          throw new IllegalStateException(e);
        }
      }
      if (exception.isPresent()) {
        throw exception.get();
      }
    }

    boolean isComplete() {
      return receivedNumResults == expectedNumResults;
    }

    ImmutableSet<SimulationResult> buildResults() {
      return results.build();
    }
  }

  static class IdMap<T> {
    private final BiMap<T, String> configMap;
    private int idNum;
    private final String prefix;
    private final Class<T> clazz;

    IdMap(String idPrefix, Class<T> cls) {
      clazz = cls;
      configMap = HashBiMap.create();
      idNum = 0;
      prefix = idPrefix;
    }

    String storeAndGenerateId(T value) {
      checkArgument(
        value instanceof Serializable,
        "When using JPPF, instances of %s must implement Serializable, "
          + "found: '%s' of class: %s.",
        clazz, value, value.getClass());
      final String id;
      if (configMap.containsKey(value)) {
        id = configMap.get(value);
      } else {
        id = prefix + idNum++;
        configMap.put(value, id);
      }
      return id;
    }

    T getValue(String id) {
      return configMap.inverse().get(id);
    }

    String getKey(T value) {
      return configMap.get(value);
    }
  }

  /**
   * This class provides instances of {@link Scenario}. This class equals
   * another if the provided scenarios are equal.
   *
   * @author Rinde van Lon
   */
  static final class ScenarioProvider implements Supplier<Scenario>,
      Serializable {
    private static final long serialVersionUID = 1738175155810322872L;

    private final String serializedScenario;
    private final Class<?> scenarioClass;
    @Nullable
    private transient Scenario localCache;

    ScenarioProvider(String serialScen, Class<?> clz) {
      serializedScenario = serialScen;
      scenarioClass = clz;
    }

    @SuppressWarnings("null")
    @Override
    public Scenario get() {
      if (localCache == null) {
        localCache = (Scenario) ScenarioIO.read(
          serializedScenario, scenarioClass);
      }
      return localCache;
    }

    @Override
    public int hashCode() {
      return serializedScenario.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object other) {
      if (other == null || other.getClass() != getClass()) {
        return false;
      }
      final ScenarioProvider sp = (ScenarioProvider) other;
      return Objects.equal(serializedScenario, sp.serializedScenario);
    }
  }

  interface RinSimTask extends Task<Object> {
    ImmutableList<SimulationTask> getTasks();
  }

  static final class CompositeTask extends AbstractTask<Object>
      implements RinSimTask {
    private static final long serialVersionUID = 6456630263901275865L;
    final ImmutableList<SimulationTask> taskList;

    CompositeTask(Iterable<SimulationTask> tasks) {
      taskList = ImmutableList.copyOf(tasks);
    }

    @Override
    public ImmutableList<SimulationTask> getTasks() {
      return taskList;
    }

    @Override
    public void run() {
      for (final RinSimTask task : taskList) {
        task.run();
      }
      setResult("COMPOSITE: ALL GOOD");
    }

    @Override
    public void setDataProvider(@Nullable DataProvider dp) {
      for (final SimulationTask st : taskList) {
        st.setDataProvider(dp);
      }
    }
  }

  static final class SimulationTask extends AbstractTask<Object>
      implements Comparable<SimulationTask>, RinSimTask {
    private static final long serialVersionUID = 5298683984670600238L;

    private final long seed;
    private final int repetition;
    private final String scenarioId;
    private final String configurationId;
    private final String postProcessorId;
    private final String id;
    private final int hashCode;

    SimulationTask(long randomSeed, int repetitionNumber, String scenId,
        String configId, String postProcId) {
      seed = randomSeed;
      repetition = repetitionNumber;
      scenarioId = scenId;
      configurationId = configId;
      postProcessorId = postProcId;
      id =
        Joiner.on("-").join(seed, scenarioId, configurationId, postProcessorId);
      hashCode =
        Objects.hashCode(seed, scenarioId, configurationId, postProcessorId);
    }

    @Override
    public ImmutableList<SimulationTask> getTasks() {
      return ImmutableList.of(this);
    }

    @Override
    public void run() {
      // gather data from provider
      final DataProvider dataProvider = getDataProvider();
      checkNotNull(
        dataProvider,
        "Probable problem: your MASConfiguration/PostProcessor may not be "
          + "fully serializable.");

      final Supplier<Scenario> scenario = getDataProvider().getParameter(
        scenarioId);
      final MASConfiguration configuration = getDataProvider().getParameter(
        configurationId);
      final PostProcessor<?> postProcessor = getDataProvider().getParameter(
        postProcessorId);

      final Scenario s = scenario.get();
      final SimArgs simArgs =
        SimArgs.create(s, configuration, seed, repetition,
          false, postProcessor, null);

      Object simResult;
      do {
        simResult = Experiment.perform(simArgs);
      } while (simResult == FailureStrategy.RETRY);

      checkArgument(simResult instanceof Serializable,
        "Your PostProcessor must generate Serializable objects, found %s.",
        simResult);

      setResult(simResult);
    }

    long getSeed() {
      return seed;
    }

    int getRepetition() {
      return repetition;
    }

    String getScenarioId() {
      return scenarioId;
    }

    String getConfigurationId() {
      return configurationId;
    }

    String getPostProcessorId() {
      return postProcessorId;
    }

    @Override
    public String getId() {
      return id;
    }

    @Deprecated
    @Override
    public void setId(@Nullable String identifier) {
      throw new UnsupportedOperationException();
    }

    @Override
    public int hashCode() {
      return hashCode;
    }

    @Override
    public boolean equals(@Nullable Object o) {
      if (o == null || o.getClass() != this.getClass()) {
        return false;
      }
      final SimulationTask t = (SimulationTask) o;
      return Objects.equal(t.seed, seed)
        && Objects.equal(t.scenarioId, scenarioId)
        && Objects.equal(t.configurationId, configurationId)
        && Objects.equal(t.postProcessorId, postProcessorId);
    }

    @Override
    public int compareTo(@Nullable SimulationTask o) {
      final SimulationTask other = verifyNotNull(o);
      return ComparisonChain.start()
        .compare(scenarioId, other.scenarioId)
        .compare(configurationId, other.configurationId)
        .compare(postProcessorId, other.postProcessorId,
          Ordering.natural().nullsLast())
        .compare(seed, other.seed)
        .result();
    }
  }
}
