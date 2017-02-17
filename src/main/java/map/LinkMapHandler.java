package map;

import data.Link;
import fileMaker.IOHandler;
import fileMaker.TravelTimesHandler;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by christof on 19.11.16.
 */
public class LinkMapHandler {
    private static int id = 0;

//    public static void main(String[] args) throws IOException, ClassNotFoundException {
//        makeLinkMap("src/main/resources/links/links.csv");
//    }


    public static void makeLinkMap(String linkFile, IOHandler ioHandler) throws IOException, ClassNotFoundException {
        Scanner linkScanner = new Scanner(new File(linkFile));
        linkScanner.nextLine();
        Map<String, Link> linkMap = new HashMap<String, Link>();

        while (linkScanner.hasNextLine()) {
            String line = linkScanner.nextLine();
            String[] splitLine = line.split(",");
            Link link = new Link(splitLine[0], Double.valueOf(splitLine[5]),
                    Double.valueOf(splitLine[9]), Double.valueOf(splitLine[10]) * -1, Double.valueOf(splitLine[11]), Double.valueOf(splitLine[12]) * -1);
            linkMap.put(link.getId(), link);
        }

        Map<String, Link> newLinkMap = Graph.deleteUnvisitedLinks(linkMap);
        System.out.println("makeLinkMap4");

        if (ioHandler.getWithTraffic()) {
            TravelTimesHandler.setTraffic(newLinkMap, ioHandler);
        }
        System.out.println("makeLinkMap5");

        IOHandler.writeLinkMap(newLinkMap, "");
        System.out.println("makeLinkMap6");


    }


    public static void cut(String linkMapPath, double maximumStreetLength) throws IOException, ClassNotFoundException {
        IOHandler ioHandler = new IOHandler();
        Map<String, Link> linkMap = (Map<String, Link>) ioHandler.readFile(linkMapPath);
        Map<String, Link> newLinkMap = new HashMap<>();
        int extaLinks = 0;
        for (String id : linkMap.keySet()) {
            Link link = linkMap.get(id);
            double length = link.getLength();
            int amountOfParts = (int) Math.ceil(length / maximumStreetLength);
            extaLinks += amountOfParts - 1;
            if (amountOfParts > 1) {
                int cutsLeft = amountOfParts - 1;
                List<Double> coordinates = new ArrayList<>();
                coordinates.add(link.getStartX());
                coordinates.add(link.getStartY());
                while (cutsLeft > 0) {
                    coordinates.add(((link.getEndX() - link.getStartX()) * ((double) (amountOfParts - cutsLeft) / amountOfParts)) + link.getStartX());
                    coordinates.add(((link.getEndY() - link.getStartY()) * ((double) (amountOfParts - cutsLeft) / amountOfParts)) + link.getStartY());
                    cutsLeft--;
                }
                coordinates.add(link.getEndX());
                coordinates.add(link.getEndY());

                int i = 0;
                while (i + 3 < coordinates.size()) {
                    Link newLink = new Link(String.valueOf(getNextId()), length / (amountOfParts), coordinates.get(i), coordinates.get(i + 1), coordinates.get(i + 2), coordinates.get(i + 3));
                    newLink.setTravelTimesMap(link.getTravelTimesMap());
                    newLink.setAmountOfCuts(amountOfParts - 1);
                    newLinkMap.put(newLink.getId(), newLink);
                    i += 2;
                }
            } else {
                //TODO test this
                Link newLink = new Link(String.valueOf(getNextId()), link.getLength(), link.getStartX(), link.getStartY(), link.getEndX(), link.getEndY());
                newLink.setTravelTimesMap(link.getTravelTimesMap());
                newLink.setAmountOfCuts(amountOfParts - 1);
                newLinkMap.put(newLink.getId(), newLink);
            }
        }

        IOHandler.writeLinkMap(newLinkMap, String.valueOf((int) maximumStreetLength));
        System.out.println("There are " + extaLinks + " links added.");
    }


    private static int getNextId() {
        id++;
        return id;
    }
}
