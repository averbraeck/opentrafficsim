package org.opentrafficsim.core.network.factory;

import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.naming.NamingException;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.xml.parsers.ParserConfigurationException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.language.io.URLResource;

import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.animation.GTUColorer;
import org.opentrafficsim.core.gtu.following.IDMPlus;
import org.opentrafficsim.core.gtu.generator.ListGTUGenerator;
import org.opentrafficsim.core.gtu.lane.changing.Egoistic;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.geotools.LinkGeotools;
import org.opentrafficsim.core.network.geotools.NodeGeotools;
import org.opentrafficsim.core.network.lane.CrossSectionElement;
import org.opentrafficsim.core.network.lane.CrossSectionLink;
import org.opentrafficsim.core.network.lane.Lane;
import org.opentrafficsim.core.network.lane.NoTrafficLane;
import org.opentrafficsim.core.network.route.FixedRouteGenerator;
import org.opentrafficsim.core.network.route.RouteGenerator;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Abs;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Rel;
import org.opentrafficsim.simulationengine.AbstractWrappableSimulation;
import org.opentrafficsim.simulationengine.properties.AbstractProperty;
import org.xml.sax.SAXException;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author: pknoppers
 * $, initial version Oct 17, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class GTM extends AbstractWrappableSimulation
{
    /**
     * Main program.
     * @param args String[]; the command line arguments (not used)
     * @throws SimRuntimeException should never happen
     * @throws RemoteException on communications failure
     */
    public static void main(final String[] args) throws RemoteException, SimRuntimeException
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    GTM xmlModel = new GTM();
                    xmlModel.buildSimulator(new ArrayList<AbstractProperty<?>>(), null, true);
                }
                catch (RemoteException | SimRuntimeException | NamingException exception)
                {
                    exception.printStackTrace();
                }
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public final String shortName()
    {
        return "TestXMLModel";
    }

    /** {@inheritDoc} */
    @Override
    public final String description()
    {
        return "TestXMLModel";
    }

    /** {@inheritDoc} */
    @Override
    public final void stopTimersThreads()
    {
        super.stopTimersThreads();
    }

    /** {@inheritDoc} */
    @Override
    protected final JPanel makeCharts()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    protected final OTSModelInterface makeModel(final GTUColorer colorer)
    {
        return new TestXMLModel(colorer);
    }

    /** {@inheritDoc} */
    @Override
    protected final Double makeAnimationRectangle()
    {
        return new Rectangle2D.Double(-1000, -1000, 2000, 2000);
    }

    /**
     * Model to test the XML parser.
     * <p>
     * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. <br>
     * All rights reserved. BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim
     * License</a>.
     * <p>
     * $LastChangedDate$, @version $Revision$, by $Author:
     * pknoppers $, initial version un 27, 2015 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     */
    class TestXMLModel implements OTSModelInterface
    {
        /** */
        private static final long serialVersionUID = 20141121L;

        /** the simulator. */
        private OTSDEVSSimulatorInterface simulator;

        /** the gtuColorer. */
        private final GTUColorer gtuColorer;

        /**
         * @param gtuColorer the GTUColorer to use.
         */
        public TestXMLModel(final GTUColorer gtuColorer)
        {
            super();
            this.gtuColorer = gtuColorer;
        }

        /** {@inheritDoc} */
        @Override
        public final void constructModel(
                final SimulatorInterface<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> pSimulator)
                throws SimRuntimeException, RemoteException
        {
            this.simulator = (OTSDEVSSimulatorInterface) pSimulator;
            // URL url = URLResource.getResource("/straight-road-new-gtu-example-noCarsTest.xml");
            // URL url = URLResource.getResource("/circular-road-new-gtu-example.xml");
            URL url = URLResource.getResource("/gtm.xml");
            XmlNetworkLaneParser nlp =
                    new XmlNetworkLaneParser(String.class, NodeGeotools.class, String.class, Coordinate.class,
                            LinkGeotools.class, String.class, this.simulator, this.gtuColorer);
            try
            {
                @SuppressWarnings("unchecked")
                Network<?, Node<?, ?>, ?> network = nlp.build(url);
                GTUType<String> gtuType = GTUType.makeGTUType("CAR");
                List<Node<?, ?>> fixedRoute = new ArrayList<Node<?, ?>>();
                // TODO add the destination node the the route
                RouteGenerator routeGenerator = new FixedRouteGenerator(fixedRoute);
                Collection<Node<?, ?>> nodes = network.getNodeSet();
                Node<?, ?> fromNode = null;
                for (Node<?, ?> n : nodes)
                {
                    if (n.getId().equals("N1"))
                    {
                        fromNode = n;
                    }
                }
                if (null == fromNode)
                {
                    throw new Error("Cannot find node N1");
                }
                // find the lane
                Lane lane = null;
                for (Link<?, ?> link : fromNode.getLinksOut())
                {
                    if (link.getEndNode().getId().equals("N2"))
                    {
                        if (link instanceof CrossSectionLink)
                        {
                            CrossSectionLink<?, ?> csl = (CrossSectionLink<?, ?>) link;
                            for (CrossSectionElement cse : csl.getCrossSectionElementList())
                            {
                                if (cse instanceof Lane && !(cse instanceof NoTrafficLane))
                                {
                                    lane = (Lane) cse;
                                }
                            }
                        }
                    }
                }
                if (null == lane)
                {
                    throw new NetworkException("Cannot find a Lane on a Link from N1 to N2");
                }
                ListGTUGenerator<String> generator =
                        new ListGTUGenerator<String>("generator 1", this.simulator, gtuType, new IDMPlus(),
                                new Egoistic(), new DoubleScalar.Abs<SpeedUnit>(50, SpeedUnit.KM_PER_HOUR), lane,
                                new DoubleScalar.Rel<LengthUnit>(0, LengthUnit.SI), routeGenerator, this.gtuColorer,
                                "D:/java/ots-core/src/main/resources/gtm_list.txt" /* HACK */);
                /* TODO Replace null for lane as obtained in the commented out code above */

            }
            catch (NetworkException | ParserConfigurationException | SAXException | IOException exception1)
            {
                exception1.printStackTrace();
            }
        }

        /** {@inheritDoc} */
        @Override
        public SimulatorInterface<Abs<TimeUnit>, Rel<TimeUnit>, OTSSimTimeDouble> getSimulator() throws RemoteException
        {
            return this.simulator;
        }

    }

}
