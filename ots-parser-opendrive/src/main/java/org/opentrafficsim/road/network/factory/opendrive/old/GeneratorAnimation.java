package org.opentrafficsim.road.network.factory.opendrive.old;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageObserver;
import java.io.Serializable;
import java.rmi.RemoteException;

import javax.media.j3d.Bounds;
import javax.naming.NamingException;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.road.network.lane.Lane;

import nl.tudelft.simulation.dsol.animation.Locatable;
import nl.tudelft.simulation.dsol.animation.D2.Renderable2D;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.language.d3.BoundingBox;
import nl.tudelft.simulation.language.d3.DirectedPoint;

/**
 * sink sensor animation.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands.<br>
 * All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-08-12 16:37:45 +0200 (Wed, 12 Aug 2015) $, @version $Revision: 1240 $, by $Author: averbraeck $,
 * initial version Jan 30, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class GeneratorAnimation extends Renderable2D implements Serializable
{
    /** */
    private static final long serialVersionUID = 20150130L;

    /** The half width left and right of the center line that is used to draw the block. */
    private final double halfWidth;

    /**
     * Construct the DefaultCarAnimation for a LaneBlock (road block).
     * @param lane Lane; the lane where the generator is
     * @param position Length; the position on the lane
     * @param simulator SimulatorInterface.TimeDoubleUnit; the simulator to schedule on
     * @throws NamingException in case of registration failure of the animation
     * @throws RemoteException in case of remote registration failure of the animation
     * @throws OTSGeometryException if position out of bounds
     */
    public GeneratorAnimation(final Lane lane, final Length position, final SimulatorInterface.TimeDoubleUnit simulator)
            throws NamingException, RemoteException, OTSGeometryException
    {
        super(new GenPos(lane, position), simulator);
        this.halfWidth = 0.4 * (lane.getBeginWidth().si + lane.getEndWidth().si) / 2.0;
        // based on avg width
    }

    /** {@inheritDoc} */
    @Override
    public final void paint(final Graphics2D graphics, final ImageObserver observer)
    {
        graphics.setColor(Color.BLUE);
        Rectangle2D rectangle = new Rectangle2D.Double(-0.25, -this.halfWidth, 0.5, 2 * this.halfWidth);
        graphics.fill(rectangle);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "GeneratorAnimation [getSource()=" + this.getSource() + "]";
    }

    /** Generator position. */
    private static class GenPos implements Locatable, Serializable
    {
        /** */
        private static final long serialVersionUID = 20150000L;

        /** Location. */
        private DirectedPoint location;

        /** Lane width. */
        private double width;

        /**
         * @param lane Lane; the lane where the generator is
         * @param position Length; the position on the lane
         * @throws OTSGeometryException on position out of bounds
         */
        public GenPos(final Lane lane, final Length position) throws OTSGeometryException
        {
            this.location = lane.getCenterLine().getLocation(position);
            this.width = (lane.getBeginWidth().si + lane.getEndWidth().si) / 2.0; // avg width
        }

        /** {@inheritDoc} */
        @Override
        public DirectedPoint getLocation() throws RemoteException
        {
            return this.location;
        }

        /** {@inheritDoc} */
        @Override
        public Bounds getBounds() throws RemoteException
        {
            return new BoundingBox(this.width, this.width, 0.0);
        }

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "GenPos [location=" + this.location + ", width=" + this.width + "]";
        }

    }
}
