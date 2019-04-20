package org.opentrafficsim.road.network.lane.object.trafficlight;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.network.lane.CrossSectionElement;
import org.opentrafficsim.road.network.lane.Lane;

import nl.tudelft.simulation.dsol.simulators.DEVSSimulatorInterface;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

/**
 * Standard implementation of a traffic light.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Nov 30, 2015 <br>
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
     * @param simulator DEVSSimulatorInterface.TimeDoubleUnit; the simulator for animation and timed events
     * @throws NetworkException on failure to place the object
     */
    public SimpleTrafficLight(final String id, final Lane lane, final Length longitudinalPosition,
            final DEVSSimulatorInterface.TimeDoubleUnit simulator) throws NetworkException
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
    public SimpleTrafficLight clone(final CrossSectionElement newCSE, final SimulatorInterface.TimeDoubleUnit newSimulator)
            throws NetworkException
    {
        Throw.when(!(newCSE instanceof Lane), NetworkException.class, "traffic lights can only be cloned for Lanes");
        Throw.when(!(newSimulator instanceof DEVSSimulatorInterface.TimeDoubleUnit), NetworkException.class,
                "simulator should be a DEVSSimulator");
        return new SimpleTrafficLight(getId(), (Lane) newCSE, getLongitudinalPosition(),
                (DEVSSimulatorInterface.TimeDoubleUnit) newSimulator);
    }

}
