package org.opentrafficsim.cosim;

import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.road.gtu.generator.characteristics.LaneBasedGtuCharacteristicsGeneratorOd;
import org.opentrafficsim.road.gtu.tactical.lmrs.LmrsFactory;
import org.opentrafficsim.road.network.factory.xml.OtsXmlModel;

/**
 * Simulation based on an OTS XML. The XML is should use default NL.CAR and NL.TRUCK GTU types.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class OtsSimulation extends OtsXmlModel implements CoSimulation
{

    /** GTU characteristics generator. */
    private final LaneBasedGtuCharacteristicsGeneratorOd charateristicsGeneratorOd;

    /**
     * Constructor.
     * @param simulator simulator
     * @param tacticalFactory tactical planner factory
     * @param networkString OTS XML string
     */
    public OtsSimulation(final OtsSimulatorInterface simulator, final LmrsFactory<?> tacticalFactory,
            final String networkString)
    {
        super(simulator, networkString);
        this.charateristicsGeneratorOd = CoSimulation.createCharactersticsGenertorInstance(simulator, tacticalFactory);
    }

    @Override
    public LaneBasedGtuCharacteristicsGeneratorOd getGtuCharacteristicsGeneratorOd()
    {
        return this.charateristicsGeneratorOd;
    }

}
