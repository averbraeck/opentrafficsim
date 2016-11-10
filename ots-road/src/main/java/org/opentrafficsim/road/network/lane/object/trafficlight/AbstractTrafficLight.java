package org.opentrafficsim.road.network.lane.object.trafficlight;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.object.AbstractLaneBasedObject;
import org.opentrafficsim.road.network.lane.object.LaneBasedObject;

import nl.tudelft.simulation.language.Throw;

/**
 * Basic, abstract implementation of a traffic light.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Nov 30, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public abstract class AbstractTrafficLight extends AbstractLaneBasedObject implements TrafficLight
{
    /** */
    private static final long serialVersionUID = 201601001L;

    /** The color of the traffic light. */
    private TrafficLightColor trafficLightColor;

    /** The simulator to schedule events on. */
    private final OTSDEVSSimulatorInterface simulator;

    /**
     * @param id traffic light id
     * @param lane lane where the traffic light is located
     * @param longitudinalPosition position of the traffic light on the lane, in the design direction
     * @param simulator the simulator for animation and timed events
     * @throws NetworkException on failure to place the object
     */
    public AbstractTrafficLight(final String id, final Lane lane, final Length longitudinalPosition,
            final OTSDEVSSimulatorInterface simulator) throws NetworkException
    {
        super(id, lane, longitudinalPosition, LaneBasedObject.makeGeometry(lane, longitudinalPosition));

        Throw.whenNull(simulator, "Simulator may not be null");
        this.simulator = simulator;

        this.trafficLightColor = TrafficLightColor.RED;
    }

    /** {@inheritDoc} */
    @Override
    public final TrafficLightColor getTrafficLightColor()
    {
        return this.trafficLightColor;
    }

    /** {@inheritDoc} */
    @Override
    public final void setTrafficLightColor(final TrafficLightColor trafficLightColor)
    {
        this.trafficLightColor = trafficLightColor;
        fireTimedEvent(TRAFFICLIGHT_CHANGE_EVENT, new Object[] { getId(), this, trafficLightColor },
                this.simulator.getSimulatorTime().get());
    }

}
