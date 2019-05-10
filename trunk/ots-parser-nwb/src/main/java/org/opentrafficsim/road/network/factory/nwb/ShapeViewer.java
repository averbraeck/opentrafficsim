package org.opentrafficsim.road.network.factory.nwb;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.ToolTipManager;

import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.road.network.factory.nwb.ShapeFileReader1.RoadData;

/**
 * Visualize road data.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 6 may 2019 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class ShapeViewer extends JFrame
{
    /** ... */
    private static final long serialVersionUID = 20190506L;

    /** The graph. */
    GraphPanel panelGraph;

    /**
     * Construct a new shape viewer.
     */
    public ShapeViewer()
    {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.panelGraph = new GraphPanel();
        this.setLayout(new BorderLayout(0, 0));
        panelGraph = new GraphPanel();
        this.add(panelGraph);
        setBounds(100, 100, 1000, 800);
        setVisible(true);
    }

    /**
     * Display road data.
     * @param roadData List<RoadData>; the road data to show
     */
    public void showRoadData(final List<RoadData> roadData)
    {
        panelGraph.renderRoadData(roadData);
    }

    /**
     * Graph.
     */
    static class GraphPanel extends JPanel implements MouseMotionListener
    {
        /** ... */
        private static final long serialVersionUID = 20190506L;

        /** Smallest X coordinate. */
        private double minX = Double.MAX_VALUE;

        /** Smallest Y coordinate. */
        private double minY = Double.MAX_VALUE;

        /** Biggest X coordinate. */
        private double maxX = Double.MIN_VALUE;

        /** Biggest Y coordinate. */
        private double maxY = Double.MIN_VALUE;

        /** X size. */
        private double width = 100;

        /** Y size. */
        private double height = 100;

        /** Current rendering scale. */
        private double scale;

        /** Current offset in X in meters. */
        private double xOffset;

        /** Current offset in Y in meters. */
        private double yOffset;

        /** Road data. */
        private List<RoadData> data = null;

        /**
         * Graph road data.
         * @param roadData List&lt;RoadData&gt;; the road data to graph
         */
        public void renderRoadData(final List<RoadData> roadData)
        {
            // Compute the bounding box of the road data.
            for (RoadData rd : roadData)
            {
                for (OTSPoint3D p : rd.designLine.getPoints())
                {
                    minX = Math.min(minX, p.x);
                    minY = Math.min(minY, p.y);
                    maxX = Math.max(maxX, p.x);
                    maxY = Math.max(maxY, p.y);
                }
            }
            System.out.println(String.format("range is %.1f,%.1f - %.1f,%.1f", minX, minY, maxX, maxY));
            this.width = maxX - minX;
            this.height = maxY - minY;
            this.data = roadData;
            repaint();
            this.addMouseMotionListener(this);
        }

        /** {@inheritDoc} */
        @Override
        protected void paintComponent(Graphics g)
        {
            Graphics2D g2 = (Graphics2D) g;
            g2.setPaint(Color.WHITE);
            g2.fillRect(0, 0, getWidth(), getHeight());
            if (null == this.data)
            {
                return;
            }
            double xScale = super.getWidth() / this.width;
            double yScale = super.getHeight() / this.height;
            this.scale = Math.min(xScale, yScale);
            this.xOffset = this.minX - ((super.getWidth() / scale - this.width) / 2);
            this.yOffset = this.maxY + ((super.getHeight() / scale - this.height) / 2);
            for (RoadData rd : this.data)
            {
                switch (rd.roadAuthority)
                {
                    case 'G':
                        g.setColor(Color.GREEN);
                        break;

                    case 'P':
                        g.setColor(Color.MAGENTA);
                        break;

                    case 'R':
                        g.setColor(Color.RED);
                        break;

                    case 'W':
                        g.setColor(Color.DARK_GRAY);
                        break;

                    default:
                        g.setColor(Color.BLACK);
                        break;
                }
                boolean firstPoint = true;
                int prevX = 0;
                int prevY = 0;
                for (OTSPoint3D p : rd.designLine.getPoints())
                {
                    int x = (int) worldToPanelX(p.x);
                    int y = (int) worldToPanelY(p.y);
                    if (firstPoint)
                    {
                        firstPoint = false;
                    }
                    else
                    {
                        g2.drawLine(prevX, prevY, x, y);
                    }
                    prevX = x;
                    prevY = y;
                }
            }
        }

        @Override
        public void mouseDragged(MouseEvent e)
        {
            // Ignored
        }

        /** Maximum distance in pixels between pointer and object in order to show information about the object. */
        private static final int CATCHRANGE = 10; // in pixels

        @Override
        public void mouseMoved(MouseEvent mouseEvent)
        {
            double x = panelToWorldX(mouseEvent.getX());
            double y = panelToWorldY(mouseEvent.getY());
            // String location = String.format("Mouse at %d, %d (%.2f, %.2f)", mouseEvent.getX(), mouseEvent.getY(), x, y);
            // System.out.println(location);

            OTSPoint3D pointerLocation = new OTSPoint3D(x, y);
            double nearest = CATCHRANGE / this.scale;
            RoadData nearestRD = null;
            for (RoadData rd : this.data)
            {
                double distance = pointerLocation.closestPointOnLine2D(rd.designLine).distanceSI(pointerLocation);
                if (distance < nearest)
                {
                    nearest = distance;
                    nearestRD = rd;
                }
            }
            if (null == nearestRD)
            {
                // System.out.println("No object near pointer");
                setToolTipText(null);
            }
            else
            {
                // System.out.println(nearestRD);
                String s = "<html>" + nearestRD.toString().replaceAll(", ",  "<br/>") + "</html>";
                setToolTipText(s);
                ToolTipManager.sharedInstance().setDismissDelay(20000);
                // System.out.println(s);
            }
        }

        /**
         * Translate world coordinate in meter to panel coordinate.
         * @param x double; the world coordinate
         * @return double; the corresponding X coordinate on the panel
         */
        double worldToPanelX(final double x)
        {
            return (x - xOffset) * scale;
        }

        /**
         * Translate world coordinate in meter to panel coordinate.
         * @param y double; the world coordinate
         * @return double; the corresponding Y coordinate on the panel
         */
        double worldToPanelY(final double y)
        {
            return (yOffset - y) * scale;
        }

        /**
         * Translate panel coordinate to world.
         * @param x double; the panel coordinate
         * @return double; the corresponding X coordinate in the world
         */
        double panelToWorldX(final double x)
        {
            return x / this.scale + this.xOffset;
        }

        /**
         * Translate panel coordinate to world.
         * @param y double; the panel coordinate
         * @return double; the corresponding Y coordinate in the world
         */
        double panelToWorldY(final double y)
        {
            return this.yOffset - y / this.scale;
        }

    }
}
