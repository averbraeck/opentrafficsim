package org.opentrafficsim.core.network.factory;

import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;

import javax.xml.parsers.ParserConfigurationException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.language.io.URLResource;

import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.geotools.LinkGeotools;
import org.opentrafficsim.core.network.geotools.NodeGeotools;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Abs;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Rel;
import org.xml.sax.SAXException;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Oct 17, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
/** */
public class TestXMLModel implements OTSModelInterface
{
    /** */
    private static final long serialVersionUID = 1L;

    /** simulator. */
    private OTSDEVSSimulatorInterface simulator;

    /** {@inheritDoc} */
    @Override
    public final void constructModel(final SimulatorInterface<Abs<TimeUnit>, Rel<TimeUnit>, OTSSimTimeDouble> pSimulator)
            throws SimRuntimeException, RemoteException
    {
        this.simulator = (OTSDEVSSimulatorInterface) pSimulator;
        URL url = URLResource.getResource("/circular-road-new-gtu-example.xml");
        // URL url = URLResource.getResource("/ots-infra-example.xml");
        XmlNetworkLaneParser2 nlp =
            new XmlNetworkLaneParser2(String.class, NodeGeotools.class, String.class, Coordinate.class, LinkGeotools.class,
                String.class, this.simulator);
        try
        {
            nlp.build(url);
        }
        catch (NetworkException | ParserConfigurationException | SAXException | IOException exception1)
        {
            exception1.printStackTrace();
        }
    }

    /** {@inheritDoc} */
    @Override
    public final SimulatorInterface<Abs<TimeUnit>, Rel<TimeUnit>, OTSSimTimeDouble> getSimulator()
            throws RemoteException
    {
        return this.simulator;
    }
}
