package com.github.rinde.rinsim.geom;

import data.RoutingTable;
import fileMaker.IOHandler;

/**
 * Created by christof on 14.04.17.
 */
public class RoutingTableSupplier {
    private static RoutingTable routingTable = null;

    public static RoutingTable getRoutingTable() {
        synchronized (RoutingTableSupplier.class) {
            if (routingTable == null) {
                try {
                    routingTable = (RoutingTable) IOHandler.readFile(IOHandler.getRoutingTablePath());
                } catch (Exception e) {
                	e.printStackTrace();
                    System.out.println("failed to load the routingtable " + IOHandler.getRoutingTablePath());
                    routingTable = new RoutingTable();
                }
            }
        }
        return routingTable;
    }
}