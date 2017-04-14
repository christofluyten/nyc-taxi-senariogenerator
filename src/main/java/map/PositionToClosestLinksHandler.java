package map;

import data.Link;
import fileMaker.IOHandler;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by christof on 19.11.16.
 */
public class PositionToClosestLinksHandler {

    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException, ClassNotFoundException {
        IOHandler ioHandler = new IOHandler();
        ioHandler.setAttribute("New500");
        makeMap(ioHandler);
        checkEmptyList(ioHandler);
    }


    public static void makeMap(IOHandler ioHandler) throws IOException, ClassNotFoundException {


        Map<Integer, Map<Integer, Set<Link>>> map = new HashMap<Integer, Map<Integer, Set<Link>>>();

        for (int x = 0; x < Link.getNbOfLonStep(); x++) {
            Map<Integer, Set<Link>> tempMap = new HashMap<Integer, Set<Link>>();

            for (int y = 0; y < Link.getNbOfLatStep(); y++) {
                tempMap.put(y, new HashSet<Link>());
            }
            map.put(x, tempMap);
        }

        Map<String, Link> linkMap = ioHandler.getLinkMap();

        for(String id : linkMap.keySet()){

            Link link = linkMap.get(id);

            int startX = (int) Math.floor((link.getStartX() - Link.getLowestLongitude()) / Link.getLongitudeStep());
            int startY = (int) Math.floor((link.getStartY() - Link.getLowestLatitude()) / Link.getLatitudeStep());

            for (int x = startX - 1; x < startX + 2; x++) {
                for (int y = startY - 1; y < startY + 2; y++) {

                    try {
                        Set set = map.get(x).get(y);
                        set.add(link);
                    } catch (NullPointerException e) {
                    }
                }
            }

            int endX = (int) Math.floor((link.getEndX() - Link.getLowestLongitude()) / Link.getLongitudeStep());
            int endY = (int) Math.floor((link.getEndY() - Link.getLowestLatitude()) / Link.getLatitudeStep());


            for (int x = endX - 1; x < endX + 2; x++) {
                for (int y = endY - 1; y < endY + 2; y++) {

                    try {
                        Set set = map.get(x).get(y);
                        set.add(link);
                    } catch (NullPointerException e) {
                    }
                }
            }
        }
        ioHandler.writePositionToClosestLinks(map);
        System.out.println("ptclMap is made and "+checkEmptyList(map)+" sets are empty");
    }

    public static void checkEmptyList(IOHandler ioHandler) throws IOException, ClassNotFoundException {
        Map<Integer,Map<Integer,Set<Link>>> linkMap = ioHandler.getPositionToClosestLinks();

        for (int x : linkMap.keySet()) {
            for (int y : linkMap.get(x).keySet()) {
                if (linkMap.get(x).get(y).size() == 0) {
                    System.out.println(x + " " + y + " is empty");
                }
            }
        }
    }

    public static int checkEmptyList(Map<Integer,Map<Integer,Set<Link>>> linkMap) {
        int result = 0;
        for (int x : linkMap.keySet()) {
            for (int y : linkMap.get(x).keySet()) {
                if (linkMap.get(x).get(y).size() == 0) {
                    result++;
                }
            }
        }
        return result;
    }
}
