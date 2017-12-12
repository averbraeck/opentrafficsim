package org.opentrafficsim.road.gtu.generator;

import java.util.Set;

import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.distributions.Generator;
import org.opentrafficsim.core.distributions.ProbabilityException;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.gtu.GTUCharacteristics;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.network.route.RouteGenerator;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTUCharacteristics;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTUCharacteristicsGenerator;
import org.opentrafficsim.road.gtu.strategical.route.LaneBasedStrategicalRoutePlannerFactory;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;

/**
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 16 nov. 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class CharacteristicsGenerator implements LaneBasedGTUCharacteristicsGenerator
{

    /** Strategical factory. */
    private final LaneBasedStrategicalRoutePlannerFactory strategicalFactory;

    /** Route generator. */
    private final RouteGenerator routeGenerator;

    /** Simulator. */
    private final OTSDEVSSimulatorInterface simulator;

    /** GTU type generator. */
    private final Generator<GTUTypeInfo> gtuTypeGenerator;

    /** GTU generation speed. */
    private final Speed generationSpeed;

    /** GTU generation positions. */
    private final Set<DirectedLanePosition> positions;

    /**
     * @param strategicalFactory strategical planner factory
     * @param routeGenerator route generator
     * @param simulator the simulator
     * @param gtuTypeGenerator the generator for the GTU type
     * @param generationSpeed the initial speed
     * @param positions the positions for generation
     */
    public CharacteristicsGenerator(final LaneBasedStrategicalRoutePlannerFactory strategicalFactory,
            final RouteGenerator routeGenerator, final OTSDEVSSimulatorInterface simulator,
            final Generator<GTUTypeInfo> gtuTypeGenerator, final Speed generationSpeed,
            final Set<DirectedLanePosition> positions)
    {
        this.strategicalFactory = strategicalFactory;
        this.routeGenerator = routeGenerator;
        this.simulator = simulator;
        this.gtuTypeGenerator = gtuTypeGenerator;
        this.generationSpeed = generationSpeed;
        this.positions = positions;
    }

    /** {@inheritDoc} */
    @Override
    public final LaneBasedGTUCharacteristics draw() throws ProbabilityException, ParameterException, GTUException
    {
        GTUTypeInfo info = this.gtuTypeGenerator.draw();
        GTUCharacteristics gtuCharacteristics =
                new GTUCharacteristics(info.getGtuType(), info.getLength(), info.getWidth(), info.getMaximumSpeed());
        return new LaneBasedGTUCharacteristics(gtuCharacteristics, this.strategicalFactory, this.routeGenerator.draw(),
                Speed.min(this.generationSpeed, info.getMaximumSpeed()), this.positions);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "CharacteristicsGenerator [strategicalFactory=" + this.strategicalFactory + ", routeGenerator="
                + this.routeGenerator + ", simulator=" + this.simulator + ", gtuTypeGenerator=" + this.gtuTypeGenerator
                + ", generationSpeed=" + this.generationSpeed + ", positions=" + this.positions + "]";
    }

}
