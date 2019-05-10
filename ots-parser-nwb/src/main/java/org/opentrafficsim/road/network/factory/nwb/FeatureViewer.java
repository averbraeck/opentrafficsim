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

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.MultiLineString;
import org.opengis.feature.Feature;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.Property;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;

/**
 * Visualize road data.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 10 may 2019 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class FeatureViewer extends JFrame
{

    /** ... */
    private static final long serialVersionUID = 20190510L;

    /** The graph. */
    GraphPanel panelGraph;

    /**
     * Construct a new shape viewer.
     */
    public FeatureViewer()
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
     * Construct a new shape viewer.
     * @param left int; the left edge of the new window
     * @param top int; the top edge of the new window
     * @param width int; the width of the new window
     * @param height int; the height of the new window
     */
    public FeatureViewer(final int left, final int top, final int width, final int height)
    {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.panelGraph = new GraphPanel();
        this.setLayout(new BorderLayout(0, 0));
        panelGraph = new GraphPanel();
        this.add(panelGraph);
        setBounds(left, top, width, height);
        setVisible(true);
    }

    /**
     * Construct the design line of a Feature.
     * @param feature Feature; the feature
     * @return OTSLine3D; the design line of the feature
     * @throws OTSGeometryException when the feature is not a proper line
     */
    public static OTSLine3D designLine(final Feature feature) throws OTSGeometryException
    {
        GeometryAttribute geometry = feature.getDefaultGeometryProperty();
        MultiLineString multiLineString = (MultiLineString) geometry.getValue();
        Coordinate[] coordinates = multiLineString.getCoordinates();
        return new OTSLine3D(coordinates);
    }

    /**
     * Display road data.
     * @param features List<Feature>; the road data to show
     * @throws OTSGeometryException
     */
    public void showRoadData(final List<Feature> features) throws OTSGeometryException
    {
        panelGraph.renderRoadData(features);
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
        private List<Feature> data = null;

        /**
         * Graph road data.
         * @param features List&lt;RoadData&gt;; the road data to graph
         * @throws OTSGeometryException
         */
        public void renderRoadData(final List<Feature> features) throws OTSGeometryException
        {
            // Compute the bounding box of the road data.
            for (Feature f : features)
            {
                for (OTSPoint3D p : designLine(f).getPoints())
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
            this.data = features;
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
            for (Feature feature : this.data)
            {
                Property property = feature.getProperty("WEGBEHSRT");
                if (null != property)
                {
                    switch (((String) property.getValue()).charAt(0))
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
                }
                else
                {
                    g.setColor(Color.BLACK);
                }
                try
                {
                    boolean firstPoint = true;
                    int prevX = 0;
                    int prevY = 0;
                    for (OTSPoint3D p : designLine(feature).getPoints())
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
                catch (OTSGeometryException ge)
                {
                    System.err.println("cannot happen");
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
            Feature nearestFeature = null;
            for (Feature feature : this.data)
            {
                double distance;
                try
                {
                    distance = pointerLocation.closestPointOnLine2D(designLine(feature)).distanceSI(pointerLocation);
                    if (distance < nearest)
                    {
                        nearest = distance;
                        nearestFeature = feature;
                    }
                }
                catch (OTSGeometryException e)
                {
                    System.err.println("Cannot happen");
                }
            }
            if (null == nearestFeature)
            {
                // System.out.println("No object near pointer");
                setToolTipText(null);
            }
            else
            {
                // System.out.println(nearestRD);
                String s = "<html>" + nearestFeature.toString().replaceAll(", ", "<br/>") + "</html>";
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
