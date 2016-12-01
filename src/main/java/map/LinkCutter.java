package map;

import data.Link;
import fileMaker.IOHandler;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Created by christof on 19.11.16.
 */

public class LinkCutter {
    private static int id = 0;


    public static void main(String[] args) throws IOException, ClassNotFoundException {

        double maximumStreetLength = 500;
        FileWriter writer = new FileWriter(IOHandler.getLinksDirectory()+IOHandler.getLinkMapFileName()+"New"
                + (int)maximumStreetLength +".csv");
        System.out.println("start cutting");

        IOHandler ioHandler = new IOHandler();
        ioHandler.setAttribute("New");
        cut(ioHandler.getLinkMap(),maximumStreetLength, writer);

        System.out.println("end cutting");
        writer.close();

    }


    public static void cut(String linkMapPath, double maximumStreetLength) throws IOException, ClassNotFoundException {
        IOHandler ioHandler = new IOHandler();
        Map<String,Link> linkMap = (Map<String, Link>) ioHandler.readFile(linkMapPath);
        Map<String,Link> newLinkMap = new HashMap<>();
        int extaLinks = 0;
        for(String id : linkMap.keySet()){
            Link link = linkMap.get(id);
            double length = link.getLength();
            int amountOfParts = (int) Math.ceil(length / maximumStreetLength);
            extaLinks += amountOfParts-1;
            if(amountOfParts > 1) {
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
                    newLinkMap.put(newLink.getId(),newLink);
                    i += 2;
                }
            } else {
                Link newLink = new Link(String.valueOf(getNextId()), link.getLength(), link.getStartX(), link.getStartY(), link.getEndX(), link.getEndY());
                newLinkMap.put(newLink.getId(),newLink);
            }
        }

        IOHandler.writeLinkMap(newLinkMap, String.valueOf((int)maximumStreetLength));
        System.out.println("There are "+extaLinks+" links added." );
    }

    private static void cut(Map<String, Link> linkMap, double maximumStreetLength, FileWriter writer) throws IOException, ClassNotFoundException {
        Link.writeTitles(writer);
        Map<String,Link> newLinkMap = new HashMap<>();
        int extaLinks = 0;
        for(String id : linkMap.keySet()){
            Link link = linkMap.get(id);
            double length = link.getLength();
            int amountOfParts = (int) Math.ceil(length / maximumStreetLength);
            extaLinks += amountOfParts-1;
            if(amountOfParts > 1) {
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
                    newLink.write(writer);
                    newLinkMap.put(newLink.getId(),newLink);
                    i += 2;
                }
            } else {
                Link newLink = new Link(String.valueOf(getNextId()), link.getLength(), link.getStartX(), link.getStartY(), link.getEndX(), link.getEndY());
                newLink.write(writer);
                newLinkMap.put(newLink.getId(),newLink);
            }
        }

        IOHandler.writeLinkMap(newLinkMap, String.valueOf((int)maximumStreetLength));
        System.out.println("There are "+extaLinks+" links added." );
    }




    private static int getNextId(){
        id++;
        return id;
    }

}
