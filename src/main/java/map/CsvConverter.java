package map;

import com.github.rinde.rinsim.geom.MultiAttributeData;
import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.geom.TableGraph;
import com.google.common.base.Optional;
import data.Link;
import fileMaker.IOHandler;

import java.io.IOException;
import java.util.Map;

/**
 * Created by Christof on 7/10/2016.
 */
public class CsvConverter {

    private Optional<String> output_dir = Optional.absent();

    public static void convertLinkMap(IOHandler ioHandler) throws IOException {
//        System.out.println("linkmap: " + ioHandler.getLinkMapPath());
        Map<String, Link> linkMap = ioHandler.getLinkMap();
        int count = 0;

        TableGraph<MultiAttributeData> graph = new TableGraph<MultiAttributeData>();
        for(String id : linkMap.keySet()) {
            Link link = linkMap.get(id);
            MultiAttributeData.Builder data = MultiAttributeData.builder().setLength(link.getLength());
            graph.addConnection(new Point(link.getStartX(),link.getStartY()),
                    new Point(link.getEndX(),link.getEndY()),
                    data.build());
            count++;
        }
//        System.out.println("Count: " + count);

//            // Export file
        DotWriter.export(graph, ioHandler);

    }

    /**
     * Sets the output folder of any newly converted csv file by this {@link CsvConverter}.
     *
     * @param folder The given folder.
     */
    public void setOutputDir(String folder) {
        output_dir = Optional.of(folder);
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
//                String[] splittedLine = line.split(",");
//                MultiAttributeData.Builder data = MultiAttributeData.builder().setLength(Double.valueOf(splittedLine[5]));
//                graph.addConnection(new Point(Double.valueOf(splittedLine[9]), -1 * Double.valueOf(splittedLine[10])),
//                        new Point(Double.valueOf(splittedLine[11]), -1 * Double.valueOf(splittedLine[12])),
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
