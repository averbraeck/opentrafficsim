package org.opentrafficsim.road.network.factory.opendrive;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.image.ImageObserver;
import java.rmi.RemoteException;

import javax.naming.NamingException;
import javax.vecmath.Point2d;

import nl.tudelft.simulation.dsol.animation.D2.Renderable2D;
import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.animation.PaintPolygons;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-09-14 01:33:02 +0200 (Mon, 14 Sep 2015) $, @version $Revision: 1401 $, by $Author: averbraeck $,
 * initial version Oct 17, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class LaneAnimationOD extends Renderable2D
{
    /** color of the lane. */
    private final Color color;

    /**
     * @param source s
     * @param simulator s
     * @param color color of the lane.
     * @throws NamingException ne
     * @throws RemoteException on communication failure
     */
    public LaneAnimationOD(final Lane source, final OTSSimulatorInterface simulator, final Color color)
        throws NamingException, RemoteException
    {
        super(source, simulator);
        this.color = color;
    }

    public static void paintLine(final Graphics2D graphics, final Color color, final DirectedPoint referencePoint,
        final OTSLine3D line)
    {
        graphics.setColor(color);
        graphics.setStroke(new BasicStroke(0.1f));
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

    private static void paintArrow(final Graphics2D graphics, final Color color, final DirectedPoint ref,
        final OTSLine3D line, final double fraction, final LongitudinalDirectionality dir)
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
            Point2d end =
                new Point2d(start.x + len * Math.cos(start.getRotZ()), start.y + len * Math.sin(start.getRotZ()));
            path.lineTo(end.x - ref.x, -end.y + ref.y);
            graphics.draw(path);

            if (dir.equals(LongitudinalDirectionality.DIR_PLUS) || dir.equals(LongitudinalDirectionality.DIR_BOTH))
            {
                path = new Path2D.Double();
                path.moveTo(end.x - ref.x, -end.y + ref.y);
                Point2d ar1 =
                    new Point2d(end.x + ar * Math.cos(start.getRotZ() + 0.75 * Math.PI), end.y + ar
                        * Math.sin(start.getRotZ() + 0.75 * Math.PI));
                path.lineTo(ar1.x - ref.x, -ar1.y + ref.y);
                path.moveTo(end.x - ref.x, -end.y + ref.y);
                Point2d ar2 =
                        new Point2d(end.x + ar * Math.cos(start.getRotZ() + 1.25 * Math.PI), end.y + ar
                            * Math.sin(start.getRotZ() + 1.25 * Math.PI));
                    path.lineTo(ar2.x - ref.x, -ar2.y + ref.y);
                graphics.draw(path);
            }
            
            if (dir.equals(LongitudinalDirectionality.DIR_MINUS) || dir.equals(LongitudinalDirectionality.DIR_BOTH))
            {
                path = new Path2D.Double();
                path.moveTo(start.x - ref.x, -start.y + ref.y);
                Point2d ar1 =
                    new Point2d(start.x + ar * Math.cos(start.getRotZ() + 0.25 * Math.PI), start.y + ar
                        * Math.sin(start.getRotZ() + 0.25 * Math.PI));
                path.lineTo(ar1.x - ref.x, -ar1.y + ref.y);
                path.moveTo(start.x - ref.x, -start.y + ref.y);
                Point2d ar2 =
                        new Point2d(start.x + ar * Math.cos(start.getRotZ() - 0.25 * Math.PI), start.y + ar
                            * Math.sin(start.getRotZ() - 0.25 * Math.PI));
                    path.lineTo(ar2.x - ref.x, -ar2.y + ref.y);
                graphics.draw(path);
            }

        }
        catch (NetworkException exception)
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
        paintArrow(graphics, Color.yellow, lane.getLocation(), lane.getCenterLine(), 0.25, lane
            .getDirectionality(GTUType.ALL));
        paintArrow(graphics, Color.green, lane.getLocation(), lane.getCenterLine(), 0.50, lane
            .getDirectionality(GTUType.ALL));
        paintArrow(graphics, Color.blue, lane.getLocation(), lane.getCenterLine(), 0.75, lane
            .getDirectionality(GTUType.ALL));
    }
}
