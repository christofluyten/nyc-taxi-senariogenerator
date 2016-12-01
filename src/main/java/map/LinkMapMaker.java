package map;

import data.Link;
import fileMaker.IOHandler;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Created by christof on 19.11.16.
 */
public class LinkMapMaker {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        makeLinkMap("src/main/resources/links/links.csv");
    }


    public static void makeLinkMap(String linkFile) throws IOException, ClassNotFoundException {
        Scanner linkScanner = new Scanner(new File(linkFile));
        linkScanner.nextLine();
        Map<String, Link> linkMap = new HashMap<String, Link>();

        while (linkScanner.hasNextLine()) {
            String line = linkScanner.nextLine();
            String[] splittedLine = line.split(",");
            Link link = new Link(splittedLine[0], Double.valueOf(splittedLine[5]),
                    Double.valueOf(splittedLine[9]), Double.valueOf(splittedLine[10])*-1, Double.valueOf(splittedLine[11]), Double.valueOf(splittedLine[12])*-1);
            linkMap.put(link.getId(), link);
        }
        Map<String, Link> newLinkMap = Graph.deleteUnvisitedLinks(linkMap);
        IOHandler.writeLinkMap(newLinkMap,"");

//        }
    }
}
