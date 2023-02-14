package org.opentrafficsim.road.network.lane.object.trafficlight;

import java.rmi.RemoteException;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * Standard implementation of a traffic light.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class SimpleTrafficLight extends AbstractTrafficLight
{
    /** */
    private static final long serialVersionUID = 201601001L;

    /**
     * @param id String; traffic light id
     * @param lane Lane; lane where the traffic light is located
     * @param longitudinalPosition Length; position of the traffic light on the lane, in the design direction
     * @param simulator OtsSimulatorInterface; the simulator for animation and timed events
     * @throws NetworkException on failure to place the object
     */
    public SimpleTrafficLight(final String id, final Lane lane, final Length longitudinalPosition,
            final OtsSimulatorInterface simulator) throws NetworkException
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
    public double getZ() throws RemoteException
    {
        return -0.0001;
    }

}
