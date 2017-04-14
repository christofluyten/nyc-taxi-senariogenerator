package com.github.rinde.rinsim.geom;


import com.github.rinde.rinsim.geom.io.DotGraphIO;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import data.Route;
import data.RoutingTable;
import fileMaker.IOHandler;

import javax.measure.Measure;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by Christof on 1/04/2017.
 */
public class RoutingTableHandler {

    private Table<Point, Point, Route> table;



    private int nbOfShortestPathCalc;

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        RoutingTableHandler routingTableHandler = new RoutingTableHandler();
        routingTableHandler.createTable();
    }


    public void createTable() throws IOException, ClassNotFoundException {
        table = HashBasedTable.create();

//        snapshot = (GraphRoadModelSnapshot) IOHandler.readFile("src/main/resources/maps/snapshot");
//
//        if(ioHandler.fileExists(ioHandler.getRoutingTablePath())){
//            routingTable = (RoutingTable) IOHandler.readFile(ioHandler.getRoutingTablePath());
//        }

//        IOHandler.writeFile(routingTable,ioHandler.getRoutingTablePath());


//        Graph<MultiAttributeData> graph = DotGraphIO.getMultiAttributeGraphIO().read(ioHandler.getMapFilePath());
        Graph<MultiAttributeData> graph = DotGraphIO.getMultiAttributeGraphIO().read("src/main/resources/maps/map500.dot");

        GeomHeuristic heuristic = GeomHeuristics.time(70d);

        Set<Point> points = graph.getNodes();


        Set<Connection<MultiAttributeData>> edges = graph.getConnections();

        int nfOfNodes = points.size();
        System.out.println("# nodes " + points.size());
        System.out.println("# edges " + edges.size());


        int edgeCount = 0;

//        Point p1 = new Point(-73.9925251, -40.7534779);
//        Point p2 = new Point(-73.9896878, -40.7522886);


        for (Connection<MultiAttributeData> edge : edges) {
            List<Point> route = new ArrayList<>();
            route.add(edge.from());
            route.add(edge.to());
//            System.out.println("path " + Graphs.shortestPath(graph, edge.from(), edge.to(), heuristic));
            if (Graphs.shortestPath(graph, edge.from(), edge.to(), heuristic).size() == 2) {
                table.put(edge.from(), edge.to(), new Route(edge.to(), getTravelTime(graph, heuristic, route)));
                edgeCount++;
            }
//            RoadPath roadPath = snapshot.getPathTo(edge.from(), edge.to(), SI.MILLI(SI.SECOND), Measure.valueOf(120d, NonSI.KILOMETERS_PER_HOUR), heuristic);
//            routingTable.addRoute(edge.from(), edge.to(), new Route(roadPath.getPath().get(1), roadPath.getTravelTime()));
//                edgeCount++;
        }

        System.out.println("edgeCount " + edgeCount);
//        IOHandler.writeFile(new RoutingTable(table), IOHandler.getRoutingTablePath());

        System.out.println("table size " + table.size());


        int count = 0;
        for (Point fromPoint : points) {
            count++;
            System.out.println("outerloop " + count);
            nbOfShortestPathCalc = 0;

            table.put(fromPoint, fromPoint, new Route(fromPoint, 0d));
            for (Point toPoint : points) {
                if (!table.contains(fromPoint, toPoint)) {
//                    List<Point> route = snapshot.getPathTo(fromPoint, toPoint, SI.MILLI(SI.SECOND), Measure.valueOf(120d, NonSI.KILOMETERS_PER_HOUR), heuristic).getPath();
                    List<Point> route = Graphs.shortestPath(graph, fromPoint, toPoint, heuristic);
                    addAllSubpathsToTable(graph, heuristic, route);
                } else {
                    nbOfShortestPathCalc++;
                }
            }
            System.out.println("nbOfShortestPathCalc " + (nfOfNodes - nbOfShortestPathCalc) + " table size " + table.size());

        }
        IOHandler.writeFile(new RoutingTable(table), IOHandler.getRoutingTablePath());
    }

    private void addAllSubpathsToTable(Graph<MultiAttributeData> graph, GeomHeuristic heuristic, List<Point> route) {
        if (!table.contains(route.get(0), route.get(route.size() - 1))) {
            double travelTime1 = getTravelTimeRecusive(new ArrayList<>(route.subList(0, route.size() - 1)), heuristic, graph);
            travelTime1 += getTravelTimeRecusive(new ArrayList<>(route.subList(route.size() - 2, route.size())), heuristic, graph);

            double travelTime2 = getTravelTimeRecusive(new ArrayList<>(route.subList(1, route.size())), heuristic, graph);
            travelTime2 += getTravelTimeRecusive(new ArrayList<>(route.subList(0, 2)), heuristic, graph);

//                List<Point> sPath = Graphs.shortestPath(graph,route.get(0),route.get(route.size()-1),heuristic);
//                if(!route.equals(sPath)){
//                    System.out.println("Error not the same route");
//                    System.out.println("sPath "+sPath);
//                    System.out.println("route "+route);
//                    System.out.println();
//                }

            if (Math.abs(travelTime1 - travelTime2) < 1) {
//                    System.out.println("travelTime1 "+travelTime1+" shortestPathTravelTime "+snapshot.getPathTo(route.get(0),route.get(route.size()-1),SI.MILLI(SI.SECOND),Measure.valueOf(120d,NonSI.KILOMETERS_PER_HOUR),heuristic).getTravelTime());
                table.put(route.get(0), route.get(route.size() - 1), new Route(route.get(1), travelTime1));
            } else {
                System.out.println("Error " + route.toString() + " " + travelTime1 + " " + travelTime2);
            }
        }
    }

    private double getTravelTimeRecusive(List<Point> route, GeomHeuristic heuristic, Graph graph) {
        if (!table.contains(route.get(0), route.get(route.size() - 1))) {
            addAllSubpathsToTable(graph, heuristic, route);
        }
        return table.get(route.get(0), route.get(route.size() - 1)).getTravelTime();
    }


    private double getTravelTime(Graph<MultiAttributeData> graph, GeomHeuristic heuristic, List<Point> route) {
        double travelTime = 0;
        for (int i = 0; i < route.size() - 1; i++) {
            travelTime += heuristic.calculateTravelTime(graph, route.get(i), route.get(i + 1), SI.KILOMETER,
                    Measure.valueOf(120d, NonSI.KILOMETERS_PER_HOUR), SI.MILLI(SI.SECOND));
        }
        return Math.round(travelTime * 1000000000d) / 1000000000d;
    }


}
