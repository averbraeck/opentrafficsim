package nl.grontmij.smarttraffic.lane;

import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.animation.GTUColorer;
import org.opentrafficsim.core.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.core.network.factory.xml.XmlNetworkLaneParser;
import org.opentrafficsim.core.network.lane.AbstractSensor;
import org.opentrafficsim.core.network.lane.CrossSectionElement;
import org.opentrafficsim.core.network.lane.CrossSectionLink;
import org.opentrafficsim.core.network.lane.Lane;
import org.opentrafficsim.core.network.route.CompleteRoute;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Abs;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Rel;
import org.opentrafficsim.simulationengine.properties.AbstractProperty;
import org.xml.sax.SAXException;

/**
 * <p>
 * @version Oct 17, 2014 <br>
 * @version $Revision$, $LastChangedDate$, by $Author$,
 *          initial version Oct 17, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 */

public class GTM extends AbstractWrappableSimulationST
{
    /** the start time of the simulation to be able to display and report correct times. */
    public static Instant startTimeSimulation;

    /** a map from the signal group name, e.g., 225_08 to the traffic lights, e.g., [225_08.1, 225_08.2, 225_08.3]. */
    public static Map<String, List<TrafficLight>> signalGroupToTrafficLights = new HashMap<>();

    /** max speed of cars in km/h. */
    public static final double MAXSPEED = 80;

    /** max speed of cars in km/h. */
    public static final double NUMBEROFDAYS = 1;

    /**
     * Main program. GTMModel has the model details.
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
        return new GTMModel();
    }

    /** {@inheritDoc} */
    @Override
    protected final Rectangle2D.Double makeAnimationRectangle()
    {
        return new Rectangle2D.Double(-100, 5500, 600, 300);
    }

    /**
     * Model for GTM.
     * <p>
     * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$,
     *          initial version Jun 27, 2015 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
     */

    class GTMModel implements OTSModelInterface
    {
        /** */
        private static final long serialVersionUID = 20150801L;

        /** the simulator. */
        private OTSDEVSSimulatorInterface simulator;

        /** {@inheritDoc} */
        @Override
        public final void constructModel(
            final SimulatorInterface<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> pSimulator)
            throws SimRuntimeException, RemoteException
        {
            this.simulator = (OTSDEVSSimulatorInterface) pSimulator;

            // Base directory (relative to user dir)
            String dirBase = System.getProperty("user.dir") + "/src/main/resources/";

            // Geef hier de file met het netwerk
            URL url = URLResource.getResource(dirBase + "network.xml");

            // Bouw het netwerk
            XmlNetworkLaneParser nlp = new XmlNetworkLaneParser(this.simulator);
            OTSNetwork<?, ?, ?> network = null;
            try
            {
                network = nlp.build(url);
                makeSignalGroupTrafficLightMap(network);
            }
            catch (NetworkException | ParserConfigurationException | SAXException | IOException | NamingException
                | GTUException | OTSGeometryException exception1)
            {
                exception1.printStackTrace();
            }

            // read the configuration files for VLOG (detector/signalgroup: both index and name
            String dirConfigVri = "configVRI/";
            String dirLoggings = "VRI-loggings/";
            // het nummer van de N201 wordt gebruikt in de bestanden
            String wegNummer = "201";
            // Geef de numers van de VRI's
            // Data van "311" ontbreekt in de meetperiode. Laat weg.
            String[] vriNummer = {"225", "231", "234", "239", "245", "249", "291", "297", "302", "308", "314"};

            // in de configVriList worden de vri configuraties opgeslagen. De ConfigVri bevat de detectoren (index, naam) en de
            // signaalgroepen (index, naam)
            HashMap<String, ConfigVri> configVriList = null;
            configVriList = ConfigFile.readVlogConfigFiles(dirConfigVri, dirBase, wegNummer, vriNummer);

            // read and define detectors from the network. in mapSensors staan alle detectoren (met de naam als zoeksleutel
            HashMap<String, AbstractSensor> mapSensor = new HashMap<String, AbstractSensor>();
            // Vervolgens worden de verschillende typen ook nog in aparte HashMaps opgeslagen
            HashMap<String, GenerateSensor> mapSensorGenerateCars = new HashMap<String, GenerateSensor>();
            HashMap<String, KillSensor> mapSensorKillCars = new HashMap<String, KillSensor>();
            HashMap<String, CheckSensor> mapSensorCheckCars = new HashMap<String, CheckSensor>();
            // alle detectoren uit het netwerk worden verzameld
            ReadNetworkData.readDetectors(this.simulator, network, configVriList, mapSensor, mapSensorGenerateCars,
                mapSensorKillCars, mapSensorCheckCars);

            // read the historical (at a later stage streaming) VLOG data
            // start met inlezen files vanaf tijdstip ....
            // nu wordt er nog data van alleen 1 juni ingelezen
            int year = 2015;
            int month = 6;
            int day = 1;
            long hour = 2;
            int minute = 0;
            int second = 0;
            int tenth = 0;
            Instant timeVLog =
                Instant.parse(String.format("%04d-%02d-%02dT%02d:%02d:%02d.%02dZ", year, month, day, hour, minute, second,
                    tenth));
            startTimeSimulation =
                Instant.parse(String.format("%04d-%02d-%02dT%02d:%02d:%02d.%02dZ", year, month, day, 0, 0, 0, 0));
            // start the simulation at 06:00, but make times relative to 00:00 to display the right time.
            /*
             * read the vlog data with both detector and signalgroup. Data van alle detectoren worden nu de pulsen toegevoegd
             * (tijdstip en waarde detectie/signaal). Deze worden opgeslagen in de mapSensor, maar tegelijkertijd ook in de
             * mappen mapSensorGenerateCars, mapSensorKillCars en mapSensorCheckCars (omdat daar een verwijzing naar dezelfde
             * objecten is).
             */
            ReadVLog.readVlogZipFiles(mapSensor, configVriList, timeVLog, dirBase + dirLoggings, wegNummer, vriNummer,
                this.simulator);

            // connect the detector pulses to the simulator and generate Cars
            // Module that provides actions if a pulse from a detector is
            // activated: creeren van een voertuig als een detector "af" gaat (waarde wordt nul)
            GTUType<String> gtuType = GTUType.makeGTUType("CAR");
            int generateCar = 0;
            Map<String, CompleteRoute> routes = new HashMap<>();
            for (String rName : network.getRouteMap().keySet())
            {
                try
                {
                    routes.put(rName, new CompleteRoute(rName, network.getRouteMap().get(rName).getNodes()));
                }
                catch (NetworkException exception)
                {
                    exception.printStackTrace();
                }
            }
            new ScheduleGenerateCars(gtuType, simulator, mapSensorGenerateCars, generateCar, routes);

            new ReportNumbers(network, simulator);

            // - Compare the (INTERMEDIATE) pulse to vehicles in the simulation
            //
            // - if no car is matched: Generate a car
            // - if matched: reposition that car, and perhaps other cars
            // - de range om te zoeken naar voertuigen:
            // ------de eerste waarde is de afstand in meters stroomOPwaarts van het voertuig
            // ------de tweede waarde is de afstand in meters stroomAFwaarts van het voertuig

//            try
//            {
//                new ScheduleCheckPulses(gtuType, simulator, mapSensorCheckCars, 400, 400, new ArrayList<CompleteRoute>(routes
//                    .values()));
//            }
//            catch (NetworkException | GTUException | NamingException e)
//            {
//                e.printStackTrace();
//            }

            // - Kill a car (EXIT)
            // connect to the sensorKill

            // module that detects a car in the simulation passing a
            // detector and provide actions:
            // - if the car is too far downstream, and not matched by a
            // pulse: we assume it is not matched --> Action: delete the
            // car.
            // -

            // module for logging information
            // car: time and distance traveled since started
            // detector: log all cars that pass (ID and time)
            // road: time and ID for every car entered and exited

            // learning algorithms:
            // if the cars are traveling faster/slower than the pulses:
            // decrease/increase the maximum speed

        }

        /**
         * Get the traffic lights with their name.
         * @param network the parsed network.
         */
        private void makeSignalGroupTrafficLightMap(final OTSNetwork<?, ?, ?> network)
        {
            for (Link<?, ?> link : network.getLinkMap().values())
            {
                if (link instanceof CrossSectionLink)
                {
                    @SuppressWarnings({"rawtypes", "unchecked"})
                    List<CrossSectionElement<?, ?>> cseList = ((CrossSectionLink) link).getCrossSectionElementList();
                    for (CrossSectionElement<?, ?> cse : cseList)
                    {
                        if (cse instanceof Lane)
                        {
                            Lane<?, ?> lane = (Lane<?, ?>) cse;
                            List<LaneBasedGTU<?>> gtus = new ArrayList<>(lane.getGtuList());
                            for (LaneBasedGTU<?> gtu : gtus)
                            {
                                if (gtu instanceof TrafficLight)
                                {
                                    TrafficLight trafficLight = (TrafficLight) gtu;
                                    String signalGroupName = trafficLight.getId().split("\\.")[0];
                                    if (!GTM.signalGroupToTrafficLights.containsKey(signalGroupName))
                                    {
                                        GTM.signalGroupToTrafficLights.put(signalGroupName, new ArrayList<TrafficLight>());
                                    }
                                    GTM.signalGroupToTrafficLights.get(signalGroupName).add(trafficLight);
                                    // XXX hack: van verkeerslicht 311 ontbreekt alle data in de meetperiode.
                                    // Zet de lichten dus op groen...
                                    if (trafficLight.getId().startsWith("311"))
                                    {
                                        trafficLight.changeColor(1);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            System.out.println(GTM.signalGroupToTrafficLights);
        }

        /** {@inheritDoc} */
        @Override
        public SimulatorInterface<Abs<TimeUnit>, Rel<TimeUnit>, OTSSimTimeDouble> getSimulator() throws RemoteException
        {
            return this.simulator;
        }

    }

}
