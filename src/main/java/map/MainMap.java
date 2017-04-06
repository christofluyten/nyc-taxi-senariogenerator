package map;

import fileMaker.IOHandler;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

public class MainMap {
    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
        //OsmConverter map = new OsmConverter();

        CsvConverter converter = new CsvConverter();
        converter.setOutputDir("src/main/resources/maps/");
//        converter.convertLinkFile("src/main/resources/links/links.csv");
        IOHandler ioHandler = new IOHandler();
        converter.convertLinkMap(ioHandler);
//        converter.createTestMap();
    }
}
