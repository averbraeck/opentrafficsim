package org.opentrafficsim.road.network.lane.object.trafficlight;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.object.AbstractLaneBasedObject;
import org.opentrafficsim.road.network.lane.object.LaneBasedObject;

/**
 * Basic, abstract implementation of a traffic light.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public abstract class AbstractTrafficLight extends AbstractLaneBasedObject implements TrafficLight
{
    /** */
    private static final long serialVersionUID = 201601001L;

    /** The color of the traffic light. */
    private TrafficLightColor trafficLightColor;

    /** The simulator to schedule events on. */
    private final OtsSimulatorInterface simulator;

    /** Default elevation of a traffic light (above zero; don't use this for lanes at non-zero elevation). */
    public static final Length DEFAULT_TRAFFICLIGHT_ELEVATION = new Length(1, LengthUnit.METER);

    /**
     * Construct an AbstractTrafficLight with specified elevation.
     * @param id String; traffic light id
     * @param lane Lane; lane where the traffic light is located
     * @param longitudinalPosition Length; position of the traffic light on the lane, in the design direction
     * @param simulator OTSSimulatorInterface; the simulator for animation and timed events
     * @param height Length; the elevation of the traffic light
     * @throws NetworkException on failure to place the object
     */
    public AbstractTrafficLight(final String id, final Lane lane, final Length longitudinalPosition,
            final OtsSimulatorInterface simulator, final Length height) throws NetworkException
    {
        super(id, lane, longitudinalPosition, LaneBasedObject.makeGeometry(lane, longitudinalPosition), height);

        Throw.whenNull(simulator, "Simulator may not be null");
        this.simulator = simulator;
        this.trafficLightColor = TrafficLightColor.RED;

        init();
    }

    /**
     * Construct an AbstractTrafficLight at default elevation (use only on roads at elevation 0).
     * @param id String; traffic light id
     * @param lane Lane; lane where the traffic light is located
     * @param longitudinalPosition Length; position of the traffic light on the lane, in the design direction
     * @param simulator OTSSimulatorInterface; the simulator for animation and timed events
     * @throws NetworkException on failure to place the object
     */
    public AbstractTrafficLight(final String id, final Lane lane, final Length longitudinalPosition,
            final OtsSimulatorInterface simulator) throws NetworkException
    {
        this(id, lane, longitudinalPosition, simulator, DEFAULT_TRAFFICLIGHT_ELEVATION);
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
        fireTimedEvent(TRAFFICLIGHT_CHANGE_EVENT, new Object[] {getId(), this, trafficLightColor},
                this.simulator.getSimulatorTime());
    }

}
