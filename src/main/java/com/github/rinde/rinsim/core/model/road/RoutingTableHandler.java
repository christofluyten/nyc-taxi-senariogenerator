package com.github.rinde.rinsim.core.model.road;


import com.github.rinde.rinsim.geom.*;
import com.github.rinde.rinsim.geom.io.DotGraphIO;
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

    private int nbOfShortestPathCalc;
    private RoutingTable routingTable;


    public void createTable(IOHandler ioHandler) throws IOException, ClassNotFoundException {

        routingTable = new RoutingTable();

        if (ioHandler.fileExists(ioHandler.getRoutingTablePath())) {
            routingTable = (RoutingTable) IOHandler.readFile(ioHandler.getRoutingTablePath());
        }

//        IOHandler.writeFile(routingTable,ioHandler.getRoutingTablePath());


        Graph<MultiAttributeData> graph = DotGraphIO.getMultiAttributeGraphIO().read(ioHandler.getMapFilePath());
//        Graph<MultiAttributeData> graph = DotGraphIO.getMultiAttributeGraphIO().read("src/main/resources/maps/testMap.dot");

        GeomHeuristic heuristic = GeomHeuristics.time(70d);

        Set<Point> points = graph.getNodes();


        Set<Connection<MultiAttributeData>> edges = graph.getConnections();

        int nfOfNodes = points.size();
        System.out.println("# nodes " + points.size());
        System.out.println("# edges " + edges.size());


        int edgeCount = 0;

        for (Connection<MultiAttributeData> edge : edges) {
            List<Point> route = new ArrayList<>();
            route.add(edge.from());
            route.add(edge.to());

            if (Graphs.shortestPath(graph, edge.from(), edge.to(), heuristic).size() == 2) {
                routingTable.addRoute(edge.from(), edge.to(), new Route(route.get(1), getTravelTime(graph, heuristic, route)));
                edgeCount++;
            }
        }

        System.out.println("edgeCount " + edgeCount);
        IOHandler.writeFile(routingTable, ioHandler.getRoutingTablePath());

        System.out.println("table size " + routingTable.size());


        int count = 0;
        for (Point fromPoint : points) {
            count++;
            System.out.println("outerloop " + count);
            nbOfShortestPathCalc = 0;

            routingTable.addRoute(fromPoint, fromPoint, new Route(fromPoint, 0d));
            for (Point toPoint : points) {
                if (!routingTable.containsRoute(fromPoint, toPoint)) {
                    List<Point> route = Graphs.shortestPath(graph, fromPoint, toPoint, heuristic);
                    addAllSubpathsToTable(graph, heuristic, route);
                } else {
                    nbOfShortestPathCalc++;
                }
            }
            System.out.println("nbOfShortestPathCalc " + (nfOfNodes - nbOfShortestPathCalc) + " table size " + routingTable.size());

        }
        IOHandler.writeFile(routingTable, ioHandler.getRoutingTablePath());
    }

    private void addAllSubpathsToTable(Graph<MultiAttributeData> graph, GeomHeuristic heuristic, List<Point> route) {
        if (!routingTable.containsRoute(route.get(0), route.get(route.size() - 1))) {
            double travelTime1 = getTravelTimeRecusive(new ArrayList<>(route.subList(0, route.size() - 1)), heuristic, graph);
            travelTime1 += getTravelTimeRecusive(new ArrayList<>(route.subList(route.size() - 2, route.size())), heuristic, graph);

            double travelTime2 = getTravelTimeRecusive(new ArrayList<>(route.subList(1, route.size())), heuristic, graph);
            travelTime2 += getTravelTimeRecusive(new ArrayList<>(route.subList(0, 2)), heuristic, graph);


            if (travelTime1 == travelTime2) {
                routingTable.addRoute(route.get(0), route.get(route.size() - 1), new Route(route.get(1), travelTime1));
            } else {
                System.out.println("Error " + route.toString() + " " + travelTime1 + " " + travelTime2);
            }
        }
    }

    private double getTravelTimeRecusive(List<Point> route, GeomHeuristic heuristic, Graph graph) {
        if (!routingTable.containsRoute(route.get(0), route.get(route.size() - 1))) {
            addAllSubpathsToTable(graph, heuristic, route);
        }
        return routingTable.getRoute(route.get(0), route.get(route.size() - 1)).getTravelTime();
    }


    private double getTravelTime(Graph<MultiAttributeData> graph, GeomHeuristic heuristic, List<Point> route) {
        double travelTime = 0;
        for (int i = 0; i < route.size() - 1; i++) {
//            System.out.println("used calc");
            travelTime += heuristic.calculateTravelTime(graph, route.get(i), route.get(i + 1), SI.KILOMETER,
                    Measure.valueOf(70.0, NonSI.KILOMETERS_PER_HOUR), SI.MILLI(SI.SECOND));
//            System.out.println("from "+ route.get(0)+" to " + route.get(i+1)+ " time "+travelTime );
        }
        return Math.round(travelTime * 100) / 100;
    }


}
