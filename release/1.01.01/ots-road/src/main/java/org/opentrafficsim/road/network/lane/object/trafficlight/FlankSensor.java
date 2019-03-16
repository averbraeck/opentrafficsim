package org.opentrafficsim.road.network.lane.object.trafficlight;

import nl.tudelft.simulation.dsol.simulators.DEVSSimulatorInterface;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.core.compatibility.Compatible;
import org.opentrafficsim.core.gtu.RelativePosition.TYPE;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.network.lane.CrossSectionElement;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.object.sensor.AbstractSensor;
import org.opentrafficsim.road.network.lane.object.sensor.TrafficLightSensor;

/**
 * Embedded sensors used by a TrafficLightSensor.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$,
 *          initial version Feb 28, 2019 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class FlankSensor extends AbstractSensor
{
    /** */
    private static final long serialVersionUID = 20161104L;

    /** The parent that must be informed of all flanks. */
    private final TrafficLightSensor parent;

    /**
     * Construct a new FlankSensor.
     * @param id String; the name of the new FlankSensor
     * @param lane Lane; the lane of the new FlankSensor
     * @param longitudinalPosition Length; the longitudinal position of the new FlankSensor
     * @param positionType TYPE; the position on the GTUs that triggers the new FlankSensor
     * @param simulator DEVSSimulatorInterface.TimeDoubleUnit; the simulator engine
     * @param parent TrafficLightSensor; the traffic light sensor that deploys this FlankSensor
     * @param compatible Compatible; object that determines if a GTU is detectable by the new FlankSensor
     * @throws NetworkException when the network is inconsistent
     */
    public FlankSensor(final String id, final Lane lane, final Length longitudinalPosition, final TYPE positionType,
            final DEVSSimulatorInterface.TimeDoubleUnit simulator, final TrafficLightSensor parent, final Compatible compatible)
            throws NetworkException
    {
        super(id, lane, longitudinalPosition, positionType, simulator, compatible);
        this.parent = parent;
    }

    /** {@inheritDoc} */
    @Override
    protected final void triggerResponse(final LaneBasedGTU gtu)
    {
        this.parent.signalDetection(this, gtu);
    }

    /** {@inheritDoc} */
    @Override
    public final FlankSensor clone(final CrossSectionElement newCSE, final SimulatorInterface.TimeDoubleUnit newSimulator)
            throws NetworkException
    {
        Throw.when(!(newCSE instanceof Lane), NetworkException.class, "sensors can only be cloned for Lanes");
        Throw.when(!(newSimulator instanceof DEVSSimulatorInterface.TimeDoubleUnit), NetworkException.class,
                "simulator should be a DEVSSimulator");
        // XXX should the parent of the clone be our parent??? And should the (cloned) parent not construct its own flank
        // sensors?
        return new FlankSensor(getId(), (Lane) newCSE, getLongitudinalPosition(), getPositionType(),
                (DEVSSimulatorInterface.TimeDoubleUnit) newSimulator, this.parent, super.getDetectedGTUTypes());
    }

    /**
     * Return the parent (TrafficLightSensor) of this FlankSensor.
     * @return TrafficLightSensor; the parent of this flank sensor
     */
    public final TrafficLightSensor getParent()
    {
        return this.parent;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "FlankSensor [parent=" + this.parent.getId() + "]";
    }

}
