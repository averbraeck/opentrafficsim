package org.opentrafficsim.road.network.lane.object.sensor;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.core.compatibility.Compatible;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.network.lane.CrossSectionElement;
import org.opentrafficsim.road.network.lane.Lane;

import nl.tudelft.simulation.dsol.logger.SimLogger;
import nl.tudelft.simulation.dsol.simulators.DEVSSimulatorInterface;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

/**
 * A DestinationSensor is a sensor that deletes a GTU that has the node it will pass after this sensor as its destination.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands.<br>
 * All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-08-12 16:37:45 +0200 (Wed, 12 Aug 2015) $, @version $Revision: 1240 $, by $Author: averbraeck $,
 * initial version an 30, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class DestinationSensor extends AbstractSensor
{
    /** */
    private static final long serialVersionUID = 20150130L;

    /** the destination node for which this is the DestinationSensor. */
    private final Node destinationNode;

    /**
     * @param lane Lane; the lane that triggers the deletion of the GTU.
     * @param position Length; the position of the sensor
     * @param gtuDirectionality GTUDirectionality; GTU directionality
     * @param simulator DEVSSimulatorInterface.TimeDoubleUnit; the simulator to enable animation.
     * @throws NetworkException when the position on the lane is out of bounds w.r.t. the center line of the lane
     */
    public DestinationSensor(final Lane lane, final Length position, final GTUDirectionality gtuDirectionality,
            final DEVSSimulatorInterface.TimeDoubleUnit simulator) throws NetworkException
    {
        this(lane, position, gtuDirectionality.isPlus() ? Compatible.PLUS : Compatible.MINUS, simulator);
    }

    /**
     * @param lane Lane; the lane that triggers the deletion of the GTU.
     * @param position Length; the position of the sensor
     * @param compatible Compatible; compatible GTU type and direction
     * @param simulator DEVSSimulatorInterface.TimeDoubleUnit; the simulator to enable animation.
     * @throws NetworkException when the position on the lane is out of bounds w.r.t. the center line of the lane
     */
    public DestinationSensor(final Lane lane, final Length position, final Compatible compatible,
            final DEVSSimulatorInterface.TimeDoubleUnit simulator) throws NetworkException
    {
        super("DESTINATION@" + lane.getFullId(), lane, position, RelativePosition.FRONT, simulator,
                makeGeometry(lane, position, 1.0), compatible);
        this.destinationNode = compatible.equals(PLUS) || compatible.equals(EVERYTHING) ? lane.getParentLink().getEndNode()
                : lane.getParentLink().getStartNode();
    }

    /** {@inheritDoc} */
    @Override
    public final void triggerResponse(final LaneBasedGTU gtu)
    {
        try
        {
            if (gtu.getStrategicalPlanner().getRoute() == null
                    || gtu.getStrategicalPlanner().getRoute().destinationNode().equals(this.destinationNode))
            {
                gtu.destroy();
            }
        }
        catch (NetworkException exception)
        {
            SimLogger.always().error(exception, "Error destroying GTU: {} at destination sensor: {}", gtu, toString());
        }
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "DestinationSensor [Lane=" + this.getLane() + "]";
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public DestinationSensor clone(final CrossSectionElement newCSE, final SimulatorInterface.TimeDoubleUnit newSimulator)
            throws NetworkException
    {
        Throw.when(!(newCSE instanceof Lane), NetworkException.class, "sensors can only be cloned for Lanes");
        Throw.when(!(newSimulator instanceof DEVSSimulatorInterface.TimeDoubleUnit), NetworkException.class,
                "simulator should be a DEVSSimulator");
        return new DestinationSensor((Lane) newCSE, getLongitudinalPosition(), getDetectedGTUTypes(),
                (DEVSSimulatorInterface.TimeDoubleUnit) newSimulator);
    }

}
