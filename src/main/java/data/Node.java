package data;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by christof on 25.11.16.
 */
public final class Node implements Serializable{
    private final double X;
    private final double Y;


    public Node(double x, double y) {
        X = x;
        Y = y;
    }

    public double getX() {
        return X;
    }

    public double getY() {
        return Y;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!Node.class.isAssignableFrom(obj.getClass())) {
            return false;
        }
        final Node other = (Node) obj;
        if (this.X != other.getX()){
            return false;
        }
        if (this.Y != other.getY()){
            return false;
        }
        return true;
    }

    public int hashCode(){

        return String.valueOf(X).hashCode() ^ String.valueOf(Y).hashCode();
    }

    public static Set<Node> getNodes(Map<String, Link> linkMap) {
        Set<Node> result = new HashSet<Node>();
        for(String key:linkMap.keySet()){
            Link link = linkMap.get(key);
            result.add(new Node(link.getStartX(),link.getStartY()));
            result.add(new Node(link.getEndX(),link.getEndY()));
        }
        return result;
    }

    public String toString() {
        return "Node " + getX() + " " + getY();
    }

    public static void writeTitles(FileWriter writer) throws IOException {
        writer.write("X,Y\n");
    }

    public void write(FileWriter writer) throws IOException {
        writer.write(String.valueOf(getX()));
        writer.write(",");
        writer.write(String.valueOf(getY()));
        writer.write("\n");
    }
}
