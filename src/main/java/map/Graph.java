package map; /**
 * Created by christof on 25.11.16.
 */


// Java program to check if a given directed graph is strongly
// connected or not
import data.Link;
import data.Node;
import fileMaker.IOHandler;
import javolution.io.Struct;
import sun.applet.resources.MsgAppletViewer;

import java.io.*;
        import java.util.*;
        import java.util.LinkedList;

// This class represents a directed graph using adjacency
// list representation
class Graph
{
    private int V;   // No. of vertices
    private Map<Node,List<Node>> adj;
    private Map<Node,List<Node>> adjTrans;
    private Node startNode;
    private Set<Node> stack = new HashSet<>();
    private Map<String, Link> linkMap;
    private Set<Node> unreachables = new HashSet<>();
    private static IOHandler ioHandler;



    //Constructor
    Graph() throws IOException, ClassNotFoundException {
        this.ioHandler = new IOHandler();
//        ioHandler.setAttribute("");
//        System.out.println(ioHandler.getLinkMapPath());
        this.linkMap = ioHandler.getLinkMap();
//        Map<String, Link> linkMap = new HashMap<>();
//        linkMap.put("1",new Link("1",1,0,0,1,1));
//        linkMap.put("2",new Link("2",1,1,1,0,0));

//        linkMap.put("3",new Link("3",1,0,0,1,1));
//        linkMap.put("4",new Link("4",1,0,0,1,1));
//
//        linkMap.put("5",new Link("5",1,0,0,1,1));
//        linkMap.put("6",new Link("6",1,0,0,1,1));


        Set<Node> nodes = Node.getNodes(linkMap);
        V = nodes.size();
        adj = new HashMap<Node,List<Node>>();
        adjTrans = new HashMap<Node,List<Node>>();
        for (Node key: nodes) {
//            System.out.println(key);
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

    Graph(Map<String, Link> linkMap) {
        this.ioHandler = new IOHandler();
        this.linkMap = linkMap;
        Set<Node> nodes = Node.getNodes(linkMap);
        V = nodes.size();
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
    void addEdge(Link link) {
        adj.get(link.getStartNode()).add(link.getEndNode());
        adjTrans.get(link.getEndNode()).add(link.getStartNode());

    }

    // A recursive function to print DFS starting from v
    void DFSUtil(Map<Node,Boolean> visited,Node node)
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
            List<Node> toVisit = adj.get(nextNode);
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
    Boolean isSC() {
        // Step 1: Mark all the vertices as not visited
        // (For first DFS)
        Map<Node,Boolean> visited = new HashMap<>();
        for(Node node:adj.keySet()){
            visited.put(node,false);
        }

        // Step 2: Do DFS traversal starting from first vertex.
        DFSUtil(visited,startNode);

        // If DFS traversal doesn't visit all vertices, then
        // return false.
        int countTrue = 0;
        int countFalse = 0;
        for(Node node:visited.keySet()){
            if(visited.get(node) == false ){
                countFalse++;
                unreachables.add(node);
            } else {
                countTrue++;
            }
        }

//        System.out.println("true "+ countTrue+" false "+countFalse);
        for(Node node:visited.keySet()){
            if(visited.get(node) == false ){
                return  false;
            }
        }



        // Step 3: Create a reversed graph
//        Graph gr = getTranspose();

        // Step 4: Mark all the vertices as not visited (For
        // second DFS)
//        for (int i = 0; i < V; i++)
//            visited[i] = false;
//
//        // Step 5: Do DFS for reversed graph starting from
//        // first vertex. Staring Vertex must be same starting
//        // point of first DFS
//        gr.DFSUtil(0, visited);
//
//        // If all vertices are not visited in second DFS, then
//        // return false
//        for (int i = 0; i < V; i++)
//            if (visited[i] == false)
//                return false;

        return true;
    }


    public static void main(String args[]) throws IOException, ClassNotFoundException {
        Graph g1 = new Graph();
        if (g1.isSC())
            System.out.println("Yes");
        else
            System.out.println("No");
        g1.deleteUnreachableNodes();

        g1 = new Graph();

        if (g1.isSC())
            System.out.println("Yes");
        else
            System.out.println("No");
    }

    public void deleteUnreachableNodes() throws IOException, ClassNotFoundException {
        System.out.println("unreachables:" +unreachables.size());
        int count = 0;
        Map<String,Link> newLinkMap = new HashMap<>();
        Map<String,Link> restMap = new HashMap<>();
        for(String id:linkMap.keySet()) {
            Link link = linkMap.get(id);
            Boolean flag = true;
            for (Node node : unreachables) {
                if ((link.getStartNode().equals(node) || link.getEndNode().equals(node))){
                    flag = false;
                    count++;
                    restMap.put(link.getId(),link);
                    break;
                }
            }
            if(flag){
                newLinkMap.put(link.getId(),link);
            }
        }

//        System.out.println(newLinkMap.size() +" vs " + linkMap.size()+ " count "+count);
        IOHandler.writeFile(newLinkMap,ioHandler.getLinkMapPath());
    }

    public static Map<String, Link> deleteUnvisitedLinks(Map<String, Link> linkMap) throws IOException, ClassNotFoundException {
        Graph g1 = new Graph(linkMap);
        g1.isSC();
        g1.deleteUnreachableNodes();
        return ioHandler.getLinkMap();
    }
}
