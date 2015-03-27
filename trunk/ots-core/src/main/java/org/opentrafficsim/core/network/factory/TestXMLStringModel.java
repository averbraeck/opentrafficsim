package org.opentrafficsim.core.network.factory;

import java.io.IOException;
import java.rmi.RemoteException;

import javax.xml.parsers.ParserConfigurationException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

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
public class TestXMLStringModel implements OTSModelInterface
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
        
        String s =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                    + "\n<NETWORK xmlns=\"http://www.opentrafficsim.org/ots-infra\" "
                    + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
                    + "\n  xsi:schemaLocation=\"http://www.opentrafficsim.org/ots-infra ots-infra.xsd\">"
                    + "\n"
                    + "\n  <GLOBAL WIDTH=\"3.6m\" SPEED=\"100km/h\" />"
                    + "\n"
                    + "\n  <GTU NAME=\"CAR\" GTUTYPE=\"CAR\" FOLLOWING=\"IDM+\" LENGTH=\"UNIF(5,7) m\" "
                    + "WIDTH=\"UNIF(1.7, 2) m\" LANECHANGE=\"EGOISTIC\""
                    + "\n    MAXSPEED=\"CONST(120) km/h\" />"
                    + "\n"
                    + "\n  <ROUTE NAME=\"CIRCLE\" NODELIST=\"NE N1 N2 N1\" />"
                    + "\n"
                    + "\n  <NODE NAME=\"N1\" COORDINATE=\"(0.0, 0.0)\" ANGLE=\"90\" />"
                    + "\n  <NODE NAME=\"N2\" />"
                    + "\n  <NODE NAME=\"NE\" />"
                    + "\n"
                    + "\n  <LINK NAME=\"L1\" FROM=\"N1\" TO=\"N2\" ELEMENTS=\"|A1:D:A2|\">"
                    + "\n    <ARC RADIUS=\"1000m\" ANGLE=\"180\" DIRECTION=\"L\" />"
                    + "\n  </LINK>"
                    + "\n"
                    + "\n  <LINK NAME=\"L2\" FROM=\"N2\" TO=\"N1\" ELEMENTS=\"|A1:D:A2|\">"
                    + "\n    <ARC RADIUS=\"1000m\" ANGLE=\"180\" DIRECTION=\"L\" />"
                    + "\n  </LINK>"
                    + "\n"
                    + "\n  <LINK NAME=\"ENTRY\" FROM=\"NE\" TO=\"N1\" ELEMENTS=\"|A1:D:A2|\">"
                    + "\n    <STRAIGHT LENGTH=\"200m\" />"
                    + "\n    <GENERATOR LANE=\"A1\" IAT=\"EXPO(3.0) s\" INITIALSPEED=\"TRIA(80,90,100) km/h\" "
                    + "MAXGTU=\"50\" GTU=\"CAR\" ROUTE=\"CIRCLE\" />"
                    + "\n    <GENERATOR LANE=\"A2\" IAT=\"EXPO(4.5) s\" INITIALSPEED=\"TRIA(80,90,100) km/h\" "
                    + "MAXGTU=\"25\" GTU=\"CAR\" ROUTE=\"CIRCLE\" />"
                    + "\n  </LINK>" + "\n" + "\n</NETWORK>";
        XmlNetworkLaneParser nlp =
                new XmlNetworkLaneParser(String.class, NodeGeotools.class, String.class, Coordinate.class, LinkGeotools.class,
                    String.class, this.simulator);

        try
        {
            nlp.build(s);
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
