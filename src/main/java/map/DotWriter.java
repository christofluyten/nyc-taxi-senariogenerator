package map;

import com.github.rinde.rinsim.geom.Graph;
import com.github.rinde.rinsim.geom.MultiAttributeData;
import com.github.rinde.rinsim.geom.io.DotGraphIO;
import fileMaker.IOHandler;

import java.io.IOException;

/**
 * This class is responsible for writing the final product. It uses the build-in write functionality from {@link DotGraphIO}.
 */
public class DotWriter {

    protected static void export(Graph<MultiAttributeData> graph, String folder) {
        try {
            DotGraphIO.getMultiAttributeGraphIO().write(graph, folder + "map_ " + System.currentTimeMillis() + ".dot");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected static void export(Graph<MultiAttributeData> graph, IOHandler ioHandler) {
        try {
            DotGraphIO.getMultiAttributeGraphIO().write(graph, ioHandler.getMapFilePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
