//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package farmbot.Pathing;

import java.awt.*;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.swing.*;

import farmbot.Pathing.Graph.Vertex;

public class GraphDrawer extends JComponent {
    private Graph graph;

    public GraphDrawer(Graph graph) {
        this.graph = graph;
    }

    public static void main(String[] args) throws IOException {
        Path path = BotPath.getPath("sylvanar_farm.txt");
        Graph graph = new Graph();
        graph.buildGraph(path);
        GraphDrawer graphDrawer = new GraphDrawer(graph);
        graphDrawer.draw();
    }

    public void draw() {
        JFrame testFrame = new JFrame();
        testFrame.setDefaultCloseOperation(2);
        this.setPreferredSize(new Dimension(1500, 1000));
        testFrame.getContentPane().add(this, "Center");
        JPanel buttonsPanel = new JPanel();
        testFrame.getContentPane().add(buttonsPanel, "South");
        testFrame.pack();
        testFrame.setVisible(true);
        this.graph.normalize();
        this.repaint();
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        this.graph.floyd();
        List<Vertex> shortestPath = this.graph.getShortestPath(10, 95);

        Iterator var3;
        Vertex current;
        for (var3 = shortestPath.iterator(); var3.hasNext(); current.visit = true) {
            current = (Vertex) var3.next();
        }

        var3 = this.graph.getVertices().iterator();

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
        return this.normalize(v, 600);
    }

    private int normalizeY(double v) {
        return this.normalize(v, 700);
    }

    private int normalize(
        double v,
        int add)
    {
        return (int) (v * 3.0D) + add;
    }
}
