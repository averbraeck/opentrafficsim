package org.opentrafficsim.road.network.lane.object.trafficlight;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.network.lane.CrossSectionElement;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * Standard implementation of a traffic light.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class SimpleTrafficLight extends AbstractTrafficLight
{
    /** */
    private static final long serialVersionUID = 201601001L;

    /**
     * @param id String; traffic light id
     * @param lane Lane; lane where the traffic light is located
     * @param longitudinalPosition Length; position of the traffic light on the lane, in the design direction
     * @param simulator OTSSimulatorInterface; the simulator for animation and timed events
     * @throws NetworkException on failure to place the object
     */
    public SimpleTrafficLight(final String id, final Lane lane, final Length longitudinalPosition,
            final OTSSimulatorInterface simulator) throws NetworkException
    {
        super(id, lane, longitudinalPosition, simulator);
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return "SimpleTrafficLight [trafficLightColor=" + getTrafficLightColor() + "]";
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public SimpleTrafficLight clone(final CrossSectionElement newCSE, final OTSSimulatorInterface newSimulator)
            throws NetworkException
    {
        Throw.when(!(newCSE instanceof Lane), NetworkException.class, "traffic lights can only be cloned for Lanes");
        Throw.when(!(newSimulator instanceof OTSSimulatorInterface), NetworkException.class,
                "simulator should be a DEVSSimulator");
        return new SimpleTrafficLight(getId(), (Lane) newCSE, getLongitudinalPosition(), (OTSSimulatorInterface) newSimulator);
    }

}
