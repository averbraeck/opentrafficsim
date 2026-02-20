package org.opentrafficsim.road.network.factory.xml;

import java.io.IOException;

import javax.naming.NamingException;
import javax.xml.parsers.ParserConfigurationException;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.base.OtsRuntimeException;
import org.opentrafficsim.core.dsol.AbstractOtsModel;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.perception.HistoryManagerDevs;
import org.opentrafficsim.road.network.RoadNetwork;
import org.opentrafficsim.road.network.factory.xml.parser.XmlParser;
import org.xml.sax.SAXException;

import jakarta.xml.bind.JAXBException;
import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.experiment.StreamInformation;

/**
 * OTS model based on an XML file. The simulator will be initialized based on the run information in the XML.
 * <p>
 * Copyright (c) 2024-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public abstract class OtsXmlModel extends AbstractOtsModel
{

    /** The network. */
    private RoadNetwork network;

    /** XML parser. */
    private XmlParser xmlParser;

    /**
     * Instantiate an abstract OtsModel. The name and description will be set as the class name. Streams will be default.
     * @param simulator the simulator to use
     * @param resource resource
     */
    public OtsXmlModel(final OtsSimulatorInterface simulator, final String resource)
    {
        super(simulator);
        Throw.whenNull(resource, "resource");
        initialize(resource, null);
    }

    /**
     * Instantiate an abstract OtsModel. The name and description will be set as the class name. Streams will be default.
     * @param simulator the simulator to use
     * @param resource resource
     * @param scenario scenario
     */
    public OtsXmlModel(final OtsSimulatorInterface simulator, final String resource, final String scenario)
    {
        super(simulator);
        Throw.whenNull(resource, "resource");
        initialize(resource, scenario);
    }

    /**
     * Instantiate an abstract OtsModel with an initial set of streams (e.g., with seed management).
     * @param simulator the simulator to use
     * @param shortName a very short description of the simulation
     * @param description a description of the simulation (HTML formatted)
     * @param streamInformation the initial set of streams (e.g., with seed management)
     * @param resource resource
     * @param scenario scenario
     */
    public OtsXmlModel(final OtsSimulatorInterface simulator, final String shortName, final String description,
            final StreamInformation streamInformation, final String resource, final String scenario)
    {
        super(simulator, shortName, description, streamInformation);
        initialize(resource, scenario);
    }

    /**
     * Initializes the simulator based on run information in the XML.
     * @param resource resource
     * @param scenario scenario, may be {@code null}
     */
    private void initialize(final String resource, final String scenario)
    {
        Throw.whenNull(resource, "resource");
        this.network = new RoadNetwork(getShortName(), getSimulator());
        try
        {
            this.xmlParser = new XmlParser(this.network).setResource(resource).setScenario(scenario);
            getSimulator().initialize(Time.ZERO, this.xmlParser.getWarmupPeriod(), this.xmlParser.getRunLength(), this,
                    new HistoryManagerDevs(getSimulator(), this.xmlParser.getHistory(), Duration.ofSI(10.0)));
        }
        catch (SimRuntimeException | NamingException | JAXBException | SAXException | ParserConfigurationException
                | IOException exception)
        {
            throw new OtsRuntimeException("Unable to load or parse XML file.", exception);
        }
    }

    @Override
    public void constructModel() throws SimRuntimeException
    {
        try
        {
            constructModel(this.xmlParser);
        }
        catch (Exception exception)
        {
            throw new SimRuntimeException(exception);
        }
    }

    /**
     * Construct model from XML parser. By default this is as simple as calling {@link XmlParser#build} on the parser.
     * Sub-classes can overwrite and e.g. set a scenario.
     * @param xmlParser XML parser
     * @throws Exception exception
     */
    @SuppressWarnings("hiddenfield")
    public void constructModel(final XmlParser xmlParser) throws Exception
    {
        xmlParser.build();
    }

    @Override
    public RoadNetwork getNetwork()
    {
        return this.network;
    }

}
