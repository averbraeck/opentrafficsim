package org.opentrafficsim.road.network.factory.opendrive.communicationRTI;

import java.rmi.RemoteException;
import java.util.Map;

import javax.media.j3d.Bounds;
import javax.naming.NamingException;
import javax.vecmath.Point3d;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.language.d3.BoundingBox;
import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.gtu.AbstractGTU;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.RelativePosition.TYPE;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristics;
import org.opentrafficsim.core.gtu.perception.Perception;
import org.opentrafficsim.core.gtu.plan.strategical.StrategicalPlanner;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.road.gtu.lane.perception.LanePerceptionNone;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlannerNone;

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
     * @param id car id
     * @param type GTU type
     * @param simulator simulator
     * @param initialLocation location
     * @param network hhe network in whoch the subjective car will be registered
     * @throws GTUException when GTU cannot be initialized
     * @throws SimRuntimeException when operational plan execution or perception execution cannot be scheduled
     * @throws NamingException when animation cannot be registered
     * @throws RemoteException when animation context or simulator cannot be reached
     */
    public SubjectiveCar(String id, GTUType type, OTSDEVSSimulatorInterface simulator, DirectedPoint initialLocation,
            final OTSNetwork network) throws SimRuntimeException, GTUException, RemoteException, NamingException
    {
        super(id, type, simulator, new LaneBasedStrategicalPlannerNone(), new LanePerceptionNone(), initialLocation,
                Speed.ZERO, network);
        this.position = initialLocation;
        System.out.println("Subjective car created at position " + this.position);

        this.length = new Length(4.0, LengthUnit.METER);
        this.width = new Length(2.0, LengthUnit.METER);

        new SubjectiveCarAnimation(this, simulator);
    }

    /**
     * @param id car id
     * @param gtuType GTU type
     * @param strategicalPlanner the strategical planner of the GTU
     * @param perception the perception unit of the GTU
     * @param simulator simulator
     * @param initialLocation location
     * @throws GTUException when GTU cannot be initialized
     * @throws SimRuntimeException when operational plan execution or perception execution cannot be scheduled
     * @throws NetworkException on inconsistency of the network
     */
    public SubjectiveCar(String id, GTUType gtuType, OTSDEVSSimulatorInterface simulator,
            StrategicalPlanner strategicalPlanner, Perception perception, DirectedPoint initialLocation)
            throws SimRuntimeException, NetworkException, GTUException
    {
        super(id, gtuType, simulator, strategicalPlanner, perception, initialLocation, Speed.ZERO, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Length getLength()
    {
        return this.length;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Length getWidth()
    {
        return this.width;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Speed getMaximumVelocity()
    {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RelativePosition getFront()
    {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RelativePosition getRear()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public RelativePosition getCenter()
    {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<TYPE, RelativePosition> getRelativePositions()
    {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy()
    {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bounds getBounds()
    {
        double dx = 0.5 * getLength().doubleValue();
        double dy = 0.5 * getWidth().doubleValue();
        return new BoundingBox(new Point3d(-dx, -dy, 0.0), new Point3d(dx, dy, 0.0));
    }

    /**
     * @return position
     */
    public DirectedPoint getPosition()
    {
        return this.position;
    }

    /**
     * @param position set position
     */
    public void setPosition(DirectedPoint position)
    {
        this.position = position;
    }

    /** {@inheritDoc} */
    @Override
    public DirectedPoint getLocation()
    {
        // System.out.println("Subjective car at position " + this.position);

        return this.getPosition();
    }

    /** {@inheritDoc} */
    @Override
    public BehavioralCharacteristics getBehavioralCharacteristics()
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
