package farmbot.Pathing;

import java.awt.*;
import java.io.File;
import java.util.Iterator;
import java.util.List;

import javax.swing.*;

import farmbot.Pathing.Graph.Vertex;
import javafx.geometry.Point3D;

public class GraphDrawer extends JComponent {

    private final GlobalGraph globalGraph;
//    private Graph graph;

    public GraphDrawer() {
//        this.graph = new Graph();
        globalGraph = new GlobalGraph("routesBG" + File.separator + "AB");
        globalGraph.buildGlobalGraph();
//        graph.buildGraph(path);
    }

    public static void main(String[] args) {
        GraphDrawer graphDrawer = new GraphDrawer();
        graphDrawer.draw();
    }

    public void draw() {
        JFrame testFrame = new JFrame();
        testFrame.setDefaultCloseOperation(2);
        this.setPreferredSize(new Dimension(1000, 800));
        testFrame.getContentPane().add(this, "Center");
        JPanel buttonsPanel = new JPanel();
        testFrame.getContentPane().add(buttonsPanel, "South");
        testFrame.pack();
        testFrame.setVisible(true);
//        this.graph.normalize();
        this.globalGraph.normalize();
        this.repaint();
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        this.globalGraph.dijkstra();

//        List<Vertex> shortestPath = this.globalGraph.getShortestPath(globalGraph.getRandomPointFromGraph(), globalGraph.getRandomPointFromGraph());

        Vertex first = globalGraph.getVertices().get(0);
        Vertex last = globalGraph.getVertices().get(750);
        List<Vertex> shortestPath = this.globalGraph.getShortestPath(first.coordinates, last.coordinates);

        System.out.println("shortestPath:" + shortestPath.size());

        Iterator var3;
        Vertex current;
        for (var3 = shortestPath.iterator(); var3.hasNext(); current.visit = true) {
            current = (Vertex) var3.next();
        }

        var3 = this.globalGraph.getVertices().iterator();

        while (var3.hasNext()) {
            current = (Vertex) var3.next();
            g.setColor(Color.black);
            if (current.visit) {
                g.setColor(Color.red);
            }

            int currentX = this.normalizeX(current.coordinates.getX());
            int currentY = this.normalizeY(current.coordinates.getY());
            g.fillOval(currentX, currentY, 10, 10);
            Iterator var7 = current.neighbors.iterator();

            while (var7.hasNext()) {
                Vertex neighbor = (Vertex) var7.next();
                g.drawLine(currentX, currentY, this.normalizeX(neighbor.coordinates.getX()), this.normalizeY(neighbor.coordinates.getY()));
            }
        }

    }

    private int normalizeX(double v) {
        return this.normalize(v, 700);
    }

    private int normalizeY(double v) {
        return this.normalize(v, 700);
    }

    private int normalize(
        double v,
        int add)
    {
        return (int) (v * 1.0D) + add;
    }
}
