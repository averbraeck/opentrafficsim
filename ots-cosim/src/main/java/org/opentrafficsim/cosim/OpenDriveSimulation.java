package org.opentrafficsim.cosim;

import javax.xml.parsers.ParserConfigurationException;

import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.opendrive.parser.OpenDriveParser;
import org.opentrafficsim.road.gtu.generator.characteristics.LaneBasedGtuCharacteristicsGeneratorOd;
import org.opentrafficsim.road.gtu.tactical.lmrs.LmrsFactory;
import org.opentrafficsim.road.network.RoadNetwork;
import org.xml.sax.SAXException;

import jakarta.xml.bind.JAXBException;

/**
 * OpenDRIVE simulation.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class OpenDriveSimulation implements CoSimulation
{

    /** Network. */
    private final RoadNetwork network;

    /** GTU characteristics generator. */
    private final LaneBasedGtuCharacteristicsGeneratorOd charateristicsGeneratorOd;

    /** Parser. */
    private final OpenDriveParser parser;

    /**
     * Constructor.
     * @param simulator simulator
     * @param tacticalFactory tactical planner factory
     * @param networkString OpenDRIVE string
     * @param useRoadName whether to use the road name to identify origins and destinations
     * @throws JAXBException if the network file cannot be parsed
     * @throws SAXException if the network file cannot be parsed
     * @throws ParserConfigurationException if the network file cannot be parsed
     * @throws NetworkException if there is any network inconsistency
     */
    public OpenDriveSimulation(final OtsSimulatorInterface simulator, final LmrsFactory<?> tacticalFactory,
            final String networkString, final boolean useRoadName)
            throws JAXBException, SAXException, ParserConfigurationException, NetworkException
    {
        this.parser = OpenDriveParser.parseFileString(networkString).setUseRoadName(useRoadName);
        this.network = new RoadNetwork("OtsOpenDriveNetwork", simulator);
        this.parser.build(this.network);
        OpenDriveParser.buildConflicts(this.network);
        this.charateristicsGeneratorOd = CoSimulation.createCharactersticsGenertorInstance(simulator, tacticalFactory);
    }

    @Override
    public RoadNetwork getNetwork()
    {
        return this.network;
    }

    @Override
    public LaneBasedGtuCharacteristicsGeneratorOd getGtuCharacteristicsGeneratorOd()
    {
        return this.charateristicsGeneratorOd;
    }

    @Override
    public String getOrigin(final String id, final Boolean designDirection)
    {
        return this.parser.getOrigin(id, designDirection).getId();
    }

    @Override
    public String getDestination(final String id, final Boolean designDirection)
    {
        return this.parser.getDestination(id, designDirection).getId();
    }

    @Override
    public RouteObjectType getRouteObjectType()
    {
        return RouteObjectType.ROAD;
    }

}
