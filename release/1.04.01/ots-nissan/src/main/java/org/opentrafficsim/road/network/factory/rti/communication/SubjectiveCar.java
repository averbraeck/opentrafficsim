package org.opentrafficsim.road.network.factory.rti.communication;

import java.rmi.RemoteException;

import javax.media.j3d.Bounds;
import javax.naming.NamingException;
import javax.vecmath.Point3d;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.immutablecollections.ImmutableMap;
import org.djutils.immutablecollections.ImmutableSet;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.gtu.AbstractGTU;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.road.network.OTSRoadNetwork;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.language.d3.BoundingBox;
import nl.tudelft.simulation.language.d3.DirectedPoint;

/** */
public class SubjectiveCar extends AbstractGTU
{

    /** */
    private static final long serialVersionUID = 1L;

    /** */
    private DirectedPoint position = null;

    /** */
    private Length length;

    /** */
    private Length width;

    /**
     * @param id String; car id
     * @param type GTUType; GTU type
     * @param simulator OTSSimulatorInterface; simulator
     * @param initialLocation DirectedPoint; location
     * @param network OTSRoadNetwork; the network in which the subjective car will be registered
     * @throws GTUException when GTU cannot be initialized
     * @throws SimRuntimeException when operational plan execution or perception execution cannot be scheduled
     * @throws NamingException when animation cannot be registered
     * @throws RemoteException when animation context or simulator cannot be reached
     */
    public SubjectiveCar(final String id, final GTUType type, final OTSSimulatorInterface simulator,
            final DirectedPoint initialLocation, final OTSRoadNetwork network)
            throws SimRuntimeException, GTUException, RemoteException, NamingException
    {
        super(id, type, simulator, network);
        this.position = initialLocation;
        System.out.println("Subjective car created at position " + this.position);

        this.length = new Length(4.0, LengthUnit.METER);
        this.width = new Length(2.0, LengthUnit.METER);

        new SubjectiveCarAnimation(this, simulator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Length getLength()
    {
        return this.length;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Length getWidth()
    {
        return this.width;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Speed getMaximumSpeed()
    {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final RelativePosition getFront()
    {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final RelativePosition getRear()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public final RelativePosition getCenter()
    {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final ImmutableMap<RelativePosition.TYPE, RelativePosition> getRelativePositions()
    {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void destroy()
    {
        super.destroy();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Bounds getBounds()
    {
        double dx = 0.5 * getLength().doubleValue();
        double dy = 0.5 * getWidth().doubleValue();
        return new BoundingBox(new Point3d(-dx, -dy, 0.0), new Point3d(dx, dy, 0.0));
    }

    /**
     * @return position
     */
    public final DirectedPoint getPosition()
    {
        return this.position;
    }

    /**
     * @param position DirectedPoint; set position
     */
    public final void setPosition(final DirectedPoint position)
    {
        this.position = position;
    }

    /** {@inheritDoc} */
    @Override
    public final DirectedPoint getLocation()
    {
        // System.out.println("Subjective car at position " + this.position);
        return this.getPosition();
    }

    /** {@inheritDoc} */
    @Override
    public final ImmutableSet<RelativePosition> getContourPoints()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "SubjectiveCar [position=" + this.position + ", length=" + this.length + ", width=" + this.width + "]";
    }

}
