#!/usr/bin/env bash
export MAVEN_OPTS=-Xmx4g -server
nohup mvn clean compile -U exec:java -Dexec.mainClass="com.github.christof.experiment.NycExperiment" -Dexec.args="false" &
