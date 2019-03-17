package org.opentrafficsim.road.network.factory.opendrive;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.image.ImageObserver;
import java.io.Serializable;
import java.rmi.RemoteException;

import javax.naming.NamingException;
import javax.vecmath.Point2d;

import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.draw.core.PaintPolygons;
import org.opentrafficsim.road.network.lane.Lane;

import nl.tudelft.simulation.dsol.animation.D2.Renderable2D;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.language.d3.DirectedPoint;

/**
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-09-14 01:33:02 +0200 (Mon, 14 Sep 2015) $, @version $Revision: 1401 $, by $Author: averbraeck $,
 * initial version Oct 17, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class LaneAnimationOD extends Renderable2D implements Serializable
{
    /** */
    private static final long serialVersionUID = 20141017L;

    /** Color of the lane. */
    private final Color color;

    /**
     * @param source Lane; s
     * @param simulator SimulatorInterface.TimeDoubleUnit; s
     * @param color Color; color of the lane.
     * @throws NamingException ne
     * @throws RemoteException on communication failure
     */
    public LaneAnimationOD(final Lane source, final SimulatorInterface.TimeDoubleUnit simulator, final Color color)
            throws NamingException, RemoteException
    {
        super(source, simulator);
        this.color = color;
    }

    /**
     * Paint a road stripe.
     * @param graphics Graphics2D; the graphics context
     * @param color Color; the color of the road stripe
     * @param referencePoint DirectedPoint; offset of the reference point of the lane from the origin
     * @param line OTSLine3D; the coordinates of the center line of the stripe
     */
    public static void paintLine(final Graphics2D graphics, final Color color, final DirectedPoint referencePoint,
            final OTSLine3D line)
    {
        graphics.setColor(color);
        graphics.setStroke(new BasicStroke(0.1f)); // width of the stripe is 0.1m
        Path2D.Double path = new Path2D.Double();
        boolean start = true;
        for (OTSPoint3D point : line.getPoints())
        {
            if (start)
            {
                path.moveTo(point.x - referencePoint.x, -point.y + referencePoint.y);
                start = false;
            }
            else
            {
                path.lineTo(point.x - referencePoint.x, -point.y + referencePoint.y);
            }
        }
        graphics.draw(path);
    }

    /**
     * Draw one arrow on the lane at a specified relative position in a specified color.
     * @param graphics Graphics2D; the graphics context
     * @param color Color; the color of the arrow
     * @param ref DirectedPoint; offset of the reference point of the lane from the origin
     * @param line OTSLine3D; the coordinates of the center line of the lane
     * @param fraction double; the relative position on the lane
     * @param dir LongitudinalDirectionality; the driving direction of the lane
     */
    private static void paintArrow(final Graphics2D graphics, final Color color, final DirectedPoint ref, final OTSLine3D line,
            final double fraction, final LongitudinalDirectionality dir)
    {
        try
        {
            double len = 3.0;
            double ar = 0.5;
            DirectedPoint start = line.getLocationFraction(fraction);
            graphics.setColor(color);
            graphics.setStroke(new BasicStroke(0.2f));
            Path2D.Double path = new Path2D.Double();
            path.moveTo(start.x - ref.x, -start.y + ref.y);
            Point2d end = new Point2d(start.x + len * Math.cos(start.getRotZ()), start.y + len * Math.sin(start.getRotZ()));
            path.lineTo(end.x - ref.x, -end.y + ref.y);
            graphics.draw(path);

            if (dir.equals(LongitudinalDirectionality.DIR_PLUS) || dir.equals(LongitudinalDirectionality.DIR_BOTH))
            {
                path = new Path2D.Double();
                path.moveTo(end.x - ref.x, -end.y + ref.y);
                Point2d ar1 = new Point2d(end.x + ar * Math.cos(start.getRotZ() + 0.75 * Math.PI),
                        end.y + ar * Math.sin(start.getRotZ() + 0.75 * Math.PI));
                path.lineTo(ar1.x - ref.x, -ar1.y + ref.y);
                path.moveTo(end.x - ref.x, -end.y + ref.y);
                Point2d ar2 = new Point2d(end.x + ar * Math.cos(start.getRotZ() + 1.25 * Math.PI),
                        end.y + ar * Math.sin(start.getRotZ() + 1.25 * Math.PI));
                path.lineTo(ar2.x - ref.x, -ar2.y + ref.y);
                graphics.draw(path);
            }

            if (dir.equals(LongitudinalDirectionality.DIR_MINUS) || dir.equals(LongitudinalDirectionality.DIR_BOTH))
            {
                path = new Path2D.Double();
                path.moveTo(start.x - ref.x, -start.y + ref.y);
                Point2d ar1 = new Point2d(start.x + ar * Math.cos(start.getRotZ() + 0.25 * Math.PI),
                        start.y + ar * Math.sin(start.getRotZ() + 0.25 * Math.PI));
                path.lineTo(ar1.x - ref.x, -ar1.y + ref.y);
                path.moveTo(start.x - ref.x, -start.y + ref.y);
                Point2d ar2 = new Point2d(start.x + ar * Math.cos(start.getRotZ() - 0.25 * Math.PI),
                        start.y + ar * Math.sin(start.getRotZ() - 0.25 * Math.PI));
                path.lineTo(ar2.x - ref.x, -ar2.y + ref.y);
                graphics.draw(path);
            }

        }
        catch (OTSGeometryException exception)
        {
            exception.printStackTrace();
        }
    }

    /** {@inheritDoc} */
    @Override
    public final void paint(final Graphics2D graphics, final ImageObserver observer)
    {
        Lane lane = (Lane) getSource();
        PaintPolygons.paintMultiPolygon(graphics, this.color, lane.getLocation(), lane.getContour(), true);
        // paintLine(graphics, Color.yellow, lane.getLocation(), lane.getCenterLine());
        paintLine(graphics, Color.white, lane.getLocation(), lane.getContour());
        paintArrow(graphics, Color.yellow, lane.getLocation(), lane.getCenterLine(), 0.25,
                lane.getLaneType().getDirectionality(lane.getNetwork().getGtuType(GTUType.DEFAULTS.VEHICLE)));
        paintArrow(graphics, Color.green, lane.getLocation(), lane.getCenterLine(), 0.50,
                lane.getLaneType().getDirectionality(lane.getNetwork().getGtuType(GTUType.DEFAULTS.VEHICLE)));
        paintArrow(graphics, Color.blue, lane.getLocation(), lane.getCenterLine(), 0.75,
                lane.getLaneType().getDirectionality(lane.getNetwork().getGtuType(GTUType.DEFAULTS.VEHICLE)));
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "LaneAnimationOD [color=" + this.color + "]";
    }
}
