package map;

/**
 * Created by christof on 25.11.16.
 */


// Java program to check if a given directed graph is strongly
// connected or not

import data.Link;
import data.Node;

import java.io.IOException;
import java.util.*;

// This class represents a directed graph using adjacency
// list representation
class Graph
{
    private Map<Node,List<Node>> adj;
    private Map<Node,List<Node>> adjTrans;
    private Node startNode;
    private Set<Node> stack = new HashSet<>();
    private Map<String, Link> linkMap;
    private Set<Node> unreachables;
//    private Map<String,Link> restMap = new HashMap<>();




    private Graph(Map<String, Link> linkMap) {
        this.linkMap = linkMap;
        Set<Node> nodes = Node.getNodes(linkMap);
        adj = new HashMap<>();
        adjTrans = new HashMap<>();
        for (Node key: nodes) {
            adj.put(key,new ArrayList<Node>());
            adjTrans.put(key,new ArrayList<Node>());
        }
        int count = 0;
        for(Node node: nodes){
            if(count == 0){
                this.startNode = node;
                break;
            }
            count++;
        }

        for(String id:linkMap.keySet()){
            Link link = linkMap.get(id);
            addEdge(link);
        }
    }


    //Function to add an edge into the graph
    private void addEdge(Link link) {
        adj.get(link.getStartNode()).add(link.getEndNode());
        adjTrans.get(link.getEndNode()).add(link.getStartNode());

    }

    // A recursive function to print DFS starting from v
    private void DFSUtil(Map<Node, Boolean> visited, Node node, Map<Node,List<Node>> adjMap)
    {
        visited.put(node,true);
        stack.add(node);
        Node nextNode = null;

        while(stack.size() > 0){
            for(Node n:stack){
                nextNode = n;
                break;
            }
            stack.remove(nextNode);
            List<Node> toVisit = adjMap.get(nextNode);
            for(Node n2:toVisit){
                if(!visited.get(n2)){
                    visited.put(n2,true);
                    stack.add(n2);
                }
            }
        }
    }



    // The main function that returns true if graph is strongly
    // connected
    private Boolean isSC(Map<Node,List<Node>> adjMap) {
        // Step 1: Mark all the vertices as not visited
        // (For first DFS)
        Map<Node,Boolean> visited = new HashMap<>();

        for(Node node:adjMap.keySet()){
            visited.put(node,false);
        }

        // Step 2: Do DFS traversal starting from first vertex.
        DFSUtil(visited,startNode, adjMap);

        // If DFS traversal doesn't visit all vertices, then
        // return false.
        unreachables = new HashSet<>();
        for(Node node:visited.keySet()){
            if(!visited.get(node)){
                unreachables.add(node);
            }
        }
        for(Node node:visited.keySet()){
            if(!visited.get(node)){
                return  false;
            }
        }

        return true;
    }


    private Map<String, Link> deleteUnreachableNodes() throws IOException, ClassNotFoundException {
        Map<String,Link> newLinkMap = new HashMap<>();
        int count = 0;
        for(String id:linkMap.keySet()) {
            Link link = linkMap.get(id);
            Boolean flag = true;
            for (Node node : unreachables) {
                if ((link.getStartNode().equals(node) || link.getEndNode().equals(node))){
                    flag = false;
                    count++;
//                    restMap.put(link.getId(),link);
                    break;
                }
            }
            if(flag){
                newLinkMap.put(link.getId(),link);
            }
        }
//        System.out.println(count +" links deleted");
        return newLinkMap;
    }

    static Map<String, Link> deleteUnvisitedLinks(Map<String, Link> linkMap) throws IOException, ClassNotFoundException {
        Graph g1 = new Graph(linkMap);
        g1.isSC(g1.adj);
        Graph g2 = new Graph(g1.deleteUnreachableNodes());
        g2.isSC(g2.adjTrans);

        return g2.deleteUnreachableNodes();
//        for(String id : g1.restMap.keySet()){
//            g2.restMap.put(id,g1.restMap.get(id));
//        }
//        System.out.println(g2.restMap.size()); //1554
//        return g2.restMap;
    }
}
