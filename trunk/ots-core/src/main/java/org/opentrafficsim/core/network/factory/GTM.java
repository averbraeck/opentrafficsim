package org.opentrafficsim.core.network.factory;

import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.xml.parsers.ParserConfigurationException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.language.io.URLResource;

import org.opentrafficsim.core.car.LaneBasedIndividualCar;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.animation.DefaultCarAnimation;
import org.opentrafficsim.core.gtu.animation.GTUColorer;
import org.opentrafficsim.core.gtu.following.GTUFollowingModel;
import org.opentrafficsim.core.gtu.following.IDMPlus;
import org.opentrafficsim.core.gtu.generator.ListGTUGenerator;
import org.opentrafficsim.core.gtu.lane.changing.AbstractLaneChangeModel;
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
import org.opentrafficsim.core.network.route.Route;
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
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Oct 17, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
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
     * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
     * reserved. <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version Jun 27, 2015 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
     */
    class TestXMLModel implements OTSModelInterface
    {
        /** */
        private static final long serialVersionUID = 20141121L;

        /** the simulator. */
        private OTSDEVSSimulatorInterface simulator;

        /** the gtuColorer. */
        private final GTUColorer gtuColorer;

        /** The blocking car. */
        private HashMap<Lane, LaneBasedIndividualCar<Integer>> blockMap =
                new HashMap<Lane, LaneBasedIndividualCar<Integer>>();

        /** The blocking car. */
        private LaneBasedIndividualCar<Integer> block = null;

        /** Type of all GTUs. */
        private GTUType<String> gtuType = GTUType.makeGTUType("CAR");

        /** the car following model, e.g. IDM Plus for cars. */
        private GTUFollowingModel carFollowingModelCars;

        /** the car following model, e.g. IDM Plus for trucks. */
        private GTUFollowingModel carFollowingModelTrucks;

        /** The Lane that contains the simulated Cars. */
        private Lane lane;

        /** The lane change model. */
        private AbstractLaneChangeModel laneChangeModel = new Egoistic();

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
            // URL url =
            // URLResource.getResource("/straight-road-new-gtu-example-noCarsTest.xml");
            // URL url =
            // URLResource.getResource("/circular-road-new-gtu-example.xml");
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
                                // "D:/java/ots-core/src/main/resources/gtm_list.txt"
                                "C:/Users/p070518/Documents/workspace-sts-3.6.4.RELEASE/ots-core/src/main/resources/vehicleList.csv"/* HACK */);

                readTrafficLightState(
                        this.simulator,
                        this.gtuColorer,
                        "C:/Users/p070518/Documents/workspace-sts-3.6.4.RELEASE/ots-core/src/main/resources/trafficLightStateList.txt",
                        lane);

            }
            catch (NetworkException | ParserConfigurationException | SAXException | IOException exception1)
            {
                exception1.printStackTrace();
            }
        }

        private void readTrafficLightState(OTSDEVSSimulatorInterface simulator, GTUColorer gtuColorer, String fileName,
                Lane lane)
        {
            /** Reader for the event list. */
            BufferedReader reader;
            try
            {
                reader = new BufferedReader(new FileReader(new File(fileName)));
                scheduleTrafficLigthState(reader, lane);
            }
            catch (FileNotFoundException exception)
            {
                exception.printStackTrace();
            }

        }

        /**
         * Schedule generation of the next GTU.
         */
        private void scheduleTrafficLigthState(BufferedReader reader, Lane lane)
        {
            try
            {
                String line = reader.readLine();
                while (!line.contentEquals(""))
                {
                    String[] words = line.split(",");
                    double when = 0;
                    for (int i = 0; i < 4; i++)
                    {
                        words[i] = words[i].replaceAll("\\s+", "");
                        if (i == 2)
                        {
                            when = java.lang.Double.parseDouble(words[i]);
                        }
                        if (i == 3)
                        {
                            if (words[i].contentEquals("RED"))
                            {
                                Object[] objects = new Object[1];
                                objects[0] = lane;

                                this.simulator.scheduleEventAbs(new DoubleScalar.Abs<TimeUnit>(when, TimeUnit.SECOND),
                                        this, this, "createBlocks", objects);

                            }
                            else if (words[i].contentEquals("GREEN"))
                            {
                                Object[] objects = new Object[1];
                                objects[0] = lane;
                                this.simulator.scheduleEventAbs(new DoubleScalar.Abs<TimeUnit>(when, TimeUnit.SECOND),
                                        this, this, "removeBlocks", objects);

                            }

                        }
                    }
                    line = reader.readLine();
                }
            }
            catch (NumberFormatException exception)
            {
                exception.printStackTrace();
                scheduleTrafficLigthState(reader, lane);
            }
            catch (IOException exception)
            {
                exception.printStackTrace();
            }
            catch (SimRuntimeException exception)
            {
                exception.printStackTrace();
            }
        }

        /** {@inheritDoc} */
        @Override
        public SimulatorInterface<Abs<TimeUnit>, Rel<TimeUnit>, OTSSimTimeDouble> getSimulator() throws RemoteException
        {
            return this.simulator;
        }

        /**
         * Set up the block.
         * @throws RemoteException on communications failure
         */
        protected final void createBlocks(Lane lane) throws RemoteException
        {
            Map<Lane, DoubleScalar.Rel<LengthUnit>> initialPositions =
                    new LinkedHashMap<Lane, DoubleScalar.Rel<LengthUnit>>();
            initialPositions.put(lane, lane.getStopLine().getLongitudinalPosition());
            try
            {
                this.blockMap.put(lane, new LaneBasedIndividualCar<Integer>(999999, this.gtuType, new IDMPlus(),
                        this.laneChangeModel, initialPositions, new DoubleScalar.Abs<SpeedUnit>(0,
                                SpeedUnit.KM_PER_HOUR), new DoubleScalar.Rel<LengthUnit>(1.0, LengthUnit.METER),
                        new DoubleScalar.Rel<LengthUnit>(1.8, LengthUnit.METER), new DoubleScalar.Abs<SpeedUnit>(0,
                                SpeedUnit.KM_PER_HOUR), new Route(new ArrayList<Node<?, ?>>()), this.simulator,
                        DefaultCarAnimation.class, this.gtuColorer));
            }
            catch (RemoteException | SimRuntimeException | NamingException | NetworkException | GTUException exception)
            {
                exception.printStackTrace();
            }
        }

        /**
         * Remove the block.
         */
        protected final void removeBlocks(Lane lane)
        {
            this.blockMap.get(lane).destroy();
            this.blockMap.remove(lane);
        }

    }

}
