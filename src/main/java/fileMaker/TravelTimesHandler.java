package fileMaker;

import data.Date;
import data.Link;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Created by christof on 07.12.16.
 */
public class TravelTimesHandler {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        IOHandler ioHandler = new IOHandler();
        ioHandler.setTravelTimesDirectory("/media/christof/Elements/Traffic_estimates/");
        makeLinkToMinAndAverageTravelTimes(ioHandler);
    }


    public static void makeLinkIdToNodesId() throws IOException, ClassNotFoundException {
        IOHandler ioHandler = new IOHandler();
        Scanner linkScanner = new Scanner(new File(ioHandler.getLinksFilePath()));
        linkScanner.nextLine();
        Map<String, String> nodesToLink = new HashMap<>();
        while (linkScanner.hasNextLine()) {
            String line = linkScanner.nextLine();
            String[] splitLine = line.split(",");
            nodesToLink.put(splitLine[1] + "," + splitLine[2], splitLine[0]);
        }
        linkScanner.close();
        IOHandler.writeFile(nodesToLink, IOHandler.getNodesIdToLinkIdPath());
    }

    public static void makeLinkToMinAndAverageTravelTimes(IOHandler ioHandler) throws IOException, ClassNotFoundException {
        String nodesIdToLinkIdPath = IOHandler.getNodesIdToLinkIdPath();
        if (!ioHandler.fileExists(nodesIdToLinkIdPath)) {
            makeLinkIdToNodesId();
        }
        Map<String, String> nodesToLink = (Map<String, String>) IOHandler.readFile(nodesIdToLinkIdPath);
        List<String> nodesId = new ArrayList<>();
        nodesId.addAll(nodesToLink.keySet());

        Map<String, List<Double>> nodeSpeedMap = new HashMap<>();

        for (String id : nodesId) {
            List<Double> speedList = new ArrayList<>();
            speedList.add(666666.0);
            speedList.add(0.0);
            speedList.add(0.0);
            nodeSpeedMap.put(id, speedList);
        }

        int count = 0;
        for (int year = 2010; year < 2014; year++) {
            System.out.println("start " + year);
            Scanner scanner = new Scanner(new File(ioHandler.getTravelTimesPath(String.valueOf(year))));
            scanner.nextLine();

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] splitLine = line.split(",");
                String id = splitLine[0] + "," + splitLine[1];

                List<Double> speedList = nodeSpeedMap.get(id);
                Double time = Double.valueOf(splitLine[3]);
                List<Double> newSpeedList = new ArrayList<>();
                try {
                    if (time < speedList.get(0)) {
                        newSpeedList.add(time);
                    } else {
                        newSpeedList.add(speedList.get(0));
                    }

                    newSpeedList.add(speedList.get(1) + time);
                    newSpeedList.add(speedList.get(2) + 1);
                    nodeSpeedMap.put(id, newSpeedList);
                } catch (NullPointerException e) {
//                    System.out.println("nullpointer");
                }
                count++;
                if (count % 1000000 == 0) {
                    System.out.println("count " + count);
                }
            }
            scanner.close();
        }

        Map<String, List<Double>> linkSpeedMap = new HashMap<>();
        int counter = 0;
        for (String id : nodeSpeedMap.keySet()) {
            List<Double> tempList = nodeSpeedMap.get(id);
            Double totalTime = tempList.get(1);
            Double amount = tempList.get(2);
            tempList.remove(2);
            tempList.remove(1);
            if (amount != 0.0) {
                tempList.add(totalTime / amount);
                linkSpeedMap.put(nodesToLink.get(id), nodeSpeedMap.get(id));
                counter++;
            }
        }
        System.out.println("amount of links with travel times = " + counter);

        IOHandler.writeFile(linkSpeedMap, IOHandler.getLinkToMinAndAverageTravelTimesPath());

        FileWriter writer = new FileWriter(IOHandler.getLinkToMinAndAverageTravelTimesPath() + ".csv");
        writer.write("linkId,min_travel_time,average_travel_time\n");
        for (String id : linkSpeedMap.keySet()) {
            writer.write(id);
            writer.write(",");
            Double minTime = linkSpeedMap.get(id).get(0);
            if (minTime == 666666.0) {
                writer.write("None");
            } else {
                writer.write(String.valueOf(minTime));
            }
            writer.write(",");
            writer.write(String.valueOf(linkSpeedMap.get(id).get(1)));
            writer.write("\n");
        }
        writer.close();
    }
    static void makeDateToTravelTimes(data.Date startDate, data.Date endDate, IOHandler ioHandler) throws IOException, ClassNotFoundException {
        System.out.println("making dateToTravelTimes");
        List<String[]> splitLines = Extractor.extractLinesFromTravelTimes(ioHandler.getTravelTimesPath(startDate.getYear()), startDate, endDate);
//        System.out.println(splitLines.size()+" "+startDate.getStringDate()+" "+endDate.getStringDate());
        Map<String, Map<data.Date, Double>> dateToTravelTimes = new HashMap<>();
        Map<String, String> nodesIdToLinkId = (Map<String, String>) IOHandler.readFile(IOHandler.getNodesIdToLinkIdPath());
        List<String> linkIds = new ArrayList<>();
        for (String nodeId : nodesIdToLinkId.keySet()) {
            linkIds.add(nodesIdToLinkId.get(nodeId));
        }

        for (String linkId : linkIds) {
            dateToTravelTimes.put(linkId, new HashMap<data.Date, Double>());
        }

        for (String[] splitLine : splitLines) {
            try {
                String linkId = nodesIdToLinkId.get(splitLine[0] + "," + splitLine[1]);
                dateToTravelTimes.get(linkId).put(new data.Date(splitLine[2]), Double.valueOf(splitLine[3]));
            } catch (NullPointerException e) {
//                System.out.println("no linkId");
            }
        }
        System.out.println("size " + dateToTravelTimes.keySet().size());

        for (String linkId : linkIds) {
            if (dateToTravelTimes.get(linkId).size() == 0) {
                dateToTravelTimes.remove(linkId);
            }
        }

        System.out.println("size " + dateToTravelTimes.keySet().size());

        IOHandler.writeFile(dateToTravelTimes, IOHandler.getDateToTravelTimesMapPath(startDate.getShortStringDateForPath(), endDate.getShortStringDateForPath()));
        FileWriter writer = new FileWriter(IOHandler.getDateToTravelTimesMapPath(startDate.getShortStringDateForPath(), endDate.getShortStringDateForPath()) + ".csv");
        writer.write("linkId,date,time\n");
        for (String id : dateToTravelTimes.keySet()) {
            for (Date date : dateToTravelTimes.get(id).keySet()) {
                writer.write(id);
                writer.write(",");
                writer.write(date.getStringDate());
                writer.write(",");
                writer.write(String.valueOf(dateToTravelTimes.get(id).get(date)));
                writer.write("\n");
            }

        }
        writer.close();
    }

    public static void setTraffic(Map<String, Link> linkMap, IOHandler ioHandler) throws IOException, ClassNotFoundException {
        Map<String, List<Double>> minAndAverageTravelTimes = ioHandler.getLinkToMinAndAverageTravelTimes();
        Map<String, Map<data.Date, Double>> dateToTravelTimes = ioHandler.getDateToTravelTimes();
        data.Date startDate = ioHandler.getPassengerStartTime();
        data.Date endDate = ioHandler.getPassengerEndTime();
        for (String id : linkMap.keySet()) {
            Link link = linkMap.get(id);
            data.Date iterator = new data.Date(startDate.getStringDate());
            while (iterator.lessThan(endDate)) {
                try {
                    link.getTravelTimesMap().put(iterator, dateToTravelTimes.get(id).get(iterator));
                } catch (NullPointerException e1) {
                    try {
                        link.getTravelTimesMap().put(iterator, minAndAverageTravelTimes.get(id).get(1));
                    } catch (NullPointerException e2) {
                        Double length = link.getLengthInM();
                        link.getTravelTimesMap().put(iterator, length / 17.8816);
                    }
                }
                iterator = data.Date.getNextHour(iterator, 1);
            }
        }
    }
}
