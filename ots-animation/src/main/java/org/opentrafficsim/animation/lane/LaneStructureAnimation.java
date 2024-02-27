package org.opentrafficsim.animation.lane;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.image.ImageObserver;
import java.lang.reflect.Field;
import java.rmi.RemoteException;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.naming.NamingException;

import org.djutils.draw.point.OrientedPoint2d;
import org.djutils.draw.point.Point2d;
import org.djutils.exceptions.Try;
import org.opentrafficsim.base.geometry.OtsRenderable;
import org.opentrafficsim.core.geometry.OtsLine2d;
import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.road.gtu.lane.perception.LaneStructureRecord;
import org.opentrafficsim.road.gtu.lane.perception.RollingLaneStructure;
import org.opentrafficsim.road.gtu.lane.perception.RollingLaneStructureRecord;
import org.opentrafficsim.road.gtu.lane.perception.RollingLaneStructureRecord.RecordLink;

/**
 * LaneStructureAnimation.java.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class LaneStructureAnimation extends OtsRenderable<LaneStructureLocatable>
{
    /** Destroyed. */
    private boolean isDestroyed = false;

    /**
     * @param source LaneStructureLocatable; dummy locatable
     * @throws NamingException on naming exception
     * @throws RemoteException on remote exception
     */
    LaneStructureAnimation(final LaneStructureLocatable source) throws NamingException, RemoteException
    {
        super(source, source.getGtu().getSimulator());
        this.setFlip(false);
        this.setRotate(false);
    }

    /** {@inheritDoc} */
    @Override
    public void paint(final Graphics2D graphics, final ImageObserver observer)
    {
        if (!this.isDestroyed)
        {
            setRendering(graphics);
            if (getSource().getGtu().isDestroyed())
            {
                this.isDestroyed = true;
                Try.execute(() -> destroy(getSource().getGtu().getSimulator()),
                        "Exception during deletion of LaneStructureAnimation.");
                return;
            }
            else
            {
                LaneStructureRecord rt = getSource().getRollingLaneStructure().getRootRecord();
                if (rt != null)
                {
                    paintRecord(rt, graphics);
                }
            }
            resetRendering(graphics);
        }
    }

    /**
     * @param lsr LaneStructureRecord; record
     * @param graphics Graphics2D; graphics
     */
    @SuppressWarnings({"unchecked"})
    private void paintRecord(final LaneStructureRecord lsr, final Graphics2D graphics)
    {
        // line
        OrientedPoint2d loc = Try.assign(() -> getSource().getLocation(), "Unable to return location.");
        graphics.setStroke(new BasicStroke(
                getSource().getRollingLaneStructure().animationAccess.getCrossSectionRecords().containsValue(lsr) ? 1.0f
                        : 0.5f));
        graphics.setColor(
                getSource().getRollingLaneStructure().animationAccess.getCrossSectionRecords().containsValue(lsr) ? Color.PINK
                        : getSource().getRollingLaneStructure().animationAccess.getUpstreamEdge().contains(lsr) ? Color.MAGENTA
                                : getSource().getRollingLaneStructure().animationAccess.getDownstreamEdge().contains(lsr)
                                        ? Color.GREEN : Color.CYAN);
        OtsLine2d line = Try.assign(() -> lsr.getLane().getCenterLine().extractFractional(0.1, 0.9),
                "Exception while painting LaneStructures");
        Path2D.Double path = new Path2D.Double();
        boolean start = true;
        for (Point2d point : line.getPoints())
        {
            if (start)
            {
                path.moveTo(point.x - loc.x, -(point.y - loc.y));
                start = false;
            }
            else
            {
                path.lineTo(point.x - loc.x, -(point.y - loc.y));
            }
        }
        graphics.draw(path);
        // connection
        Field sourceField = Try.assign(() -> RollingLaneStructureRecord.class.getDeclaredField("source"),
                "Exception while painting LaneStructure");
        sourceField.setAccessible(true);
        LaneStructureRecord src =
                Try.assign(() -> (LaneStructureRecord) sourceField.get(lsr), "Exception while painting LaneStructure");
        if (src != null)
        {
            Field sourceLinkField = Try.assign(() -> RollingLaneStructureRecord.class.getDeclaredField("sourceLink"),
                    "Exception while painting LaneStructure");
            sourceLinkField.setAccessible(true);
            RecordLink link = (RecordLink) Try.assign(() -> sourceLinkField.get(lsr), "Exception while painting LaneStructure");
            float f1 = link.equals(RecordLink.DOWN) ? 0.9f : link.equals(RecordLink.UP) ? 0.1f : 0.5f;
            float f2 = link.equals(RecordLink.DOWN) ? 0.0f : link.equals(RecordLink.UP) ? 1.0f : 0.5f;
            float f3 = f1;
            float f4 = f2;
            OrientedPoint2d p1 = Try.assign(() -> src.getLane().getCenterLine().getLocationFraction(f3),
                    "Exception while painting LaneStructure");
            OrientedPoint2d p2 = Try.assign(() -> line.getLocationFraction(f4), "Exception while painting LaneStructure");
            path = new Path2D.Double();
            path.moveTo(p1.x - loc.x, -(p1.y - loc.y));
            path.lineTo(p2.x - loc.x, -(p2.y - loc.y));
            graphics.setStroke(
                    new BasicStroke(0.15f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10f, new float[] {.3f, 1.2f}, 0f));
            graphics.setColor(Color.DARK_GRAY);
            graphics.draw(path);
        }
        // left/right
        paintLateralConnection(lsr, lsr.getLeft(), Color.RED, graphics, loc);
        paintLateralConnection(lsr, lsr.getRight(), Color.BLUE, graphics, loc);
        // recursion to depending records
        Field dependentField = Try.assign(() -> RollingLaneStructureRecord.class.getDeclaredField("dependentRecords"),
                "Exception while painting LaneStructure");
        dependentField.setAccessible(true);
        Set<LaneStructureRecord> dependables =
                (Set<LaneStructureRecord>) Try.assign(() -> dependentField.get(lsr), "Exception while painting LaneStructure");
        if (dependables != null)
        {
            for (LaneStructureRecord dependable : new LinkedHashSet<>(dependables)) // concurrency
            {
                paintRecord(dependable, graphics);
            }
        }
    }

    /**
     * Paint the connection to a lateral record.
     * @param main LaneStructureRecord; main record
     * @param adj LaneStructureRecord; adjacent record, can be {@code null}
     * @param color Color; color
     * @param graphics Graphics2D; graphics
     * @param loc OrientedPoint2d; location
     */
    private void paintLateralConnection(final LaneStructureRecord main, final LaneStructureRecord adj, final Color color,
            final Graphics2D graphics, final OrientedPoint2d loc)
    {
        if (adj == null)
        {
            return;
        }
        float f1 = 0.45f;
        float f2 = 0.55f;
        OrientedPoint2d p1 = Try.assign(() -> main.getLane().getCenterLine().getLocationFraction(f1),
                "Exception while painting LaneStructure");
        OrientedPoint2d p2 = Try.assign(() -> adj.getLane().getCenterLine().getLocationFraction(f2),
                "Exception while painting LaneStructure");
        Path2D.Double path = new Path2D.Double();
        path.moveTo(p1.x - loc.x, -(p1.y - loc.y));
        path.lineTo(p2.x - loc.x, -(p2.y - loc.y));
        graphics.setStroke(
                new BasicStroke(0.05f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10f, new float[] {.15f, 0.6f}, 0f));
        graphics.setColor(color);
        graphics.draw(path);
    }

    /**
     * Enables visualization of this lane structure. This is purely for debugging purposes.
     * @param rollingLaneStructure RollingLaneStructure; the lane structure to visualize
     * @param gtu Gtu; GTU to animate the LaneStructure of
     */
    public static final void visualize(final RollingLaneStructure rollingLaneStructure, final Gtu gtu)
    {
        Try.execute(() -> new LaneStructureAnimation(new LaneStructureLocatable(rollingLaneStructure, gtu)),
                "Could not create animation.");
    }

}
