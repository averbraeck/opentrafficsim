package org.opentrafficsim.draw.road;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageObserver;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.road.network.lane.object.LaneBasedObject;

import nl.tudelft.simulation.dsol.animation.D2.Renderable2D;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

/**
 * Abstract class for objects that draw a line perpendicular on the lane.
 * <p>
 * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 25 jan. 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <T> the LaneBasedObject class of the source that indicates the location of the Renderable on the screen
 */
public abstract class AbstractLineAnimation<T extends LaneBasedObject> extends Renderable2D<T>
{

    /** Rectangle to color. */
    private final Rectangle2D rectangle;

    /** Half length, for placement of coupled text labels. */
    private final double halfLength;

    /**
     * Construct the line animation.
     * @param source T; source
     * @param simulator SimulatorInterface.TimeDoubleUnit; the simulator to schedule on
     * @param length double; length of the line, as fraction of the lane width
     * @param width Length; line width
     * @throws NamingException in case of registration failure of the animation
     * @throws RemoteException in case of remote registration failure of the animation
     */
    public AbstractLineAnimation(final T source, final SimulatorInterface.TimeDoubleUnit simulator, final double length,
            final Length width) throws NamingException, RemoteException
    {
        super(source, simulator);
        this.halfLength = .5 * length * source.getLane().getWidth(0.0).getSI();
        this.rectangle = new Rectangle2D.Double(-.5 * width.si, -this.halfLength, width.si, 2 * this.halfLength);
    }

    /**
     * Returns half the length.
     * @return half the length
     */
    public final double getHalfLength()
    {
        return this.halfLength;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public void paint(final Graphics2D graphics, final ImageObserver observer) throws RemoteException
    {
        graphics.fill(this.rectangle);
    }

}
