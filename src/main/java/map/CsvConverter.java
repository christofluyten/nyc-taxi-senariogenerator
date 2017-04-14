package map;

import com.github.rinde.rinsim.geom.MultiAttributeData;
import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.geom.TableGraph;
import com.google.common.base.Optional;
import data.Link;
import fileMaker.IOHandler;
import freemarker.ext.beans.HashAdapter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Christof on 7/10/2016.
 */
public class CsvConverter {

    private Optional<String> output_dir = Optional.absent();

    /**
     * Sets the output folder of any newly converted csv file by this {@link CsvConverter}.
     *
     * @param folder The given folder.
     */
    public void setOutputDir(String folder) {
        output_dir = Optional.of(folder);
    }

    public static void convertLinkMap(IOHandler ioHandler) throws IOException {
//        System.out.println("linkmap: " + ioHandler.getLinkMapPath());
        Map<String, Link> linkMap = ioHandler.getLinkMap();
        int count = 0;

        TableGraph<MultiAttributeData> graph = new TableGraph<MultiAttributeData>();
        for(String id : linkMap.keySet()) {
            Link link = linkMap.get(id);
            MultiAttributeData.Builder data = MultiAttributeData.builder();
            data.setLength(link.getLengthInKm());
            if(ioHandler.getWithTraffic()){
                data.setMaxSpeed(link.getSpeed(ioHandler.getPassengerStartTime()));
            }
            graph.addConnection(new Point(link.getStartX(),link.getStartY()),
                    new Point(link.getEndX(),link.getEndY()),
                    data.build());
            count++;
        }
//        System.out.println("Count: " + count);

//            // Export file
        DotWriter.export(graph, ioHandler);

    }

    public void createTestMap() {
        Link link1 = new Link("1", 10,0,0,1,0);
        Link link2 = new Link("2", 20,1,0,1,1);
        Link link3 = new Link("3", 10,1,1,0,1);
        Link link4 = new Link("4", 20,0,1,0,0);


        Map<String, Link> linkMap = new HashMap<>();
        linkMap.put(link1.getId(),link1);
        linkMap.put(link2.getId(),link2);
        linkMap.put(link3.getId(),link3);
        linkMap.put(link4.getId(),link4);


        TableGraph<MultiAttributeData> graph = new TableGraph<MultiAttributeData>();
        for(String id : linkMap.keySet()) {
            Link link = linkMap.get(id);
            MultiAttributeData.Builder data = MultiAttributeData.builder();
            data.setLength(link.getLengthInKm());
            data.setMaxSpeed(36);
            graph.addConnection(new Point(link.getStartX(),link.getStartY()),
                    new Point(link.getEndX(),link.getEndY()),
                    data.build());
        }
//        System.out.println("Count: " + count);

//            // Export file
        DotWriter.export(graph, output_dir.get()+"testMap.dot");
    }


//    public void convertLinkFile(String linkFile) {
//        try {
//            Scanner scanner = new Scanner(new File(linkFile));
//            scanner.nextLine();
//            int count = 0;
//
//            TableGraph<MultiAttributeData> graph = new TableGraph<MultiAttributeData>();
//            while (scanner.hasNextLine()) {
//                String line = scanner.nextLine();
//                String[] splitLine = line.split(",");
//                MultiAttributeData.Builder data = MultiAttributeData.builder().setLength(Double.valueOf(splitLine[5]));
//                graph.addConnection(new Point(Double.valueOf(splitLine[9]), -1 * Double.valueOf(splitLine[10])),
//                        new Point(Double.valueOf(splitLine[11]), -1 * Double.valueOf(splitLine[12])),
//                        data.build());
//                count++;
//            }
//            System.out.println("Count: " + count);
//            scanner.close();
//
//
////            // Export file
//            if (output_dir.isPresent()) {
//                DotWriter.export(graph, output_dir.get());
//
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//
//    }

}
