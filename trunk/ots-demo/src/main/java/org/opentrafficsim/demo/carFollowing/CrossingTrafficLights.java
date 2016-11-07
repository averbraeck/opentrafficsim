package org.opentrafficsim.demo.carFollowing;

import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.unit.UNITS;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.base.modelproperties.CompoundProperty;
import org.opentrafficsim.base.modelproperties.Property;
import org.opentrafficsim.base.modelproperties.PropertyException;
import org.opentrafficsim.base.modelproperties.SelectionProperty;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.animation.GTUColorer;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristics;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.core.network.OTSNode;
import org.opentrafficsim.core.units.distributions.ContinuousDistDoubleScalar;
import org.opentrafficsim.road.gtu.animation.DefaultCarAnimation;
import org.opentrafficsim.road.gtu.lane.LaneBasedIndividualGTU;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedGTUFollowingTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.following.GTUFollowingModelOld;
import org.opentrafficsim.road.gtu.lane.tactical.following.IDMOld;
import org.opentrafficsim.road.gtu.lane.tactical.following.IDMPlusOld;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.gtu.strategical.route.LaneBasedStrategicalRoutePlanner;
import org.opentrafficsim.road.modelproperties.IDMPropertySet;
import org.opentrafficsim.road.network.factory.LaneFactory;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneType;
import org.opentrafficsim.road.network.lane.object.sensor.Sensor;
import org.opentrafficsim.road.network.lane.object.sensor.SinkSensor;
import org.opentrafficsim.road.network.lane.object.trafficlight.SimpleTrafficLight;
import org.opentrafficsim.road.network.lane.object.trafficlight.TrafficLight;
import org.opentrafficsim.road.network.lane.object.trafficlight.TrafficLightColor;
import org.opentrafficsim.simulationengine.AbstractWrappableAnimation;
import org.opentrafficsim.simulationengine.OTSSimulationException;
import org.opentrafficsim.simulationengine.SimpleSimulatorInterface;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.gui.swing.HTMLPanel;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.jstats.distributions.DistTriangular;
import nl.tudelft.simulation.jstats.streams.MersenneTwister;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Demonstration of a crossing with traffic lights.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2016-10-28 16:34:11 +0200 (Fri, 28 Oct 2016) $, @version $Revision: 2429 $, by $Author: pknoppers $,
 * initial version 12 nov. 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class CrossingTrafficLights extends AbstractWrappableAnimation implements UNITS
{
    /** */
    private static final long serialVersionUID = 1L;

    /** The model. */
    private CrossingTrafficLightstModel model;

    /**
     * Create a CrossingTrafficLights simulation.
     * @throws PropertyException when a property is not handled
     */
    public CrossingTrafficLights() throws PropertyException
    {
        this.properties.add(new SelectionProperty("LaneChanging", "Lane changing",
                "<html>The lane change strategies vary in politeness.<br>"
                        + "Two types are implemented:<ul><li>Egoistic (looks only at personal gain).</li>"
                        + "<li>Altruistic (assigns effect on new and current follower the same weight as "
                        + "the personal gain).</html>",
                new String[] { "Egoistic", "Altruistic" }, 0, false, 500));
        this.properties.add(new SelectionProperty("TacticalPlanner", "Tactical planner",
                "<html>The tactical planner determines if a lane change is desired and possible.</html>",
                new String[] { "MOBIL", "LMRS", "Toledo" }, 0, false, 600));
    }

    /** {@inheritDoc} */
    @Override
    public final void stopTimersThreads()
    {
        super.stopTimersThreads();
        this.model = null;
    }

    /**
     * Main program.
     * @param args String[]; the command line arguments (not used)
     * @throws SimRuntimeException when simulation cannot be created with given parameters
     */
    public static void main(final String[] args) throws SimRuntimeException
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @SuppressWarnings("synthetic-access")
            @Override
            public void run()
            {
                try
                {
                    CrossingTrafficLights crossingTrafficLights = new CrossingTrafficLights();
                    List<Property<?>> localProperties = crossingTrafficLights.getProperties();
                    localProperties.add(new SelectionProperty("CarFollowingModel", "Car following model",
                            "<html>The car following model determines "
                                    + "the acceleration that a vehicle will make taking into account "
                                    + "nearby vehicles, infrastructural restrictions (e.g. speed limit, "
                                    + "curvature of the road) capabilities of the vehicle and personality "
                                    + "of the driver.</html>",
                            new String[] { "IDM", "IDM+" }, 1, false, 1));
                    localProperties.add(IDMPropertySet.makeIDMPropertySet("IDMCar", "Car",
                            new Acceleration(1.56, METER_PER_SECOND_2), new Acceleration(2.09, METER_PER_SECOND_2),
                            new Length(3.0, METER), new Duration(1.2, SECOND), 2));
                    localProperties.add(IDMPropertySet.makeIDMPropertySet("IDMTruck", "Truck",
                            new Acceleration(0.75, METER_PER_SECOND_2), new Acceleration(1.25, METER_PER_SECOND_2),
                            new Length(3.0, METER), new Duration(1.2, SECOND), 3));

                    crossingTrafficLights.buildAnimator(new Time(0.0, SECOND), new Duration(0.0, SECOND),
                            new Duration(3600.0, SECOND), localProperties, null, true);

                    crossingTrafficLights.panel.getTabbedPane().addTab("info", crossingTrafficLights.makeInfoPane());
                }
                catch (SimRuntimeException | NamingException | OTSSimulationException | PropertyException exception)
                {
                    exception.printStackTrace();
                }
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    protected final Rectangle2D.Double makeAnimationRectangle()
    {
        return new Rectangle2D.Double(-600, -600, 1200, 1200);
    }

    /** {@inheritDoc} */
    @Override
    protected final OTSModelInterface makeModel(final GTUColorer colorer)
    {
        this.model = new CrossingTrafficLightstModel(this.savedUserModifiedProperties, colorer);
        return this.model;
    }

    /**
     * @return an info pane to be added to the tabbed pane.
     */
    protected final JComponent makeInfoPane()
    {
        // Make the info tab
        String helpSource = "/" + CrossingTrafficLightstModel.class.getPackage().getName().replace('.', '/') + "/IDMPlus.html";
        URL page = CrossingTrafficLightstModel.class.getResource(helpSource);
        if (page != null)
        {
            try
            {
                HTMLPanel htmlPanel = new HTMLPanel(page);
                return new JScrollPane(htmlPanel);
            }
            catch (IOException exception)
            {
                exception.printStackTrace();
            }
        }
        return new JPanel();
    }

    /** {@inheritDoc} */
    @Override
    protected final JPanel makeCharts(final SimpleSimulatorInterface simulator) throws OTSSimulationException, PropertyException
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public final String shortName()
    {
        return "Crossing with Traffic Lights";
    }

    /** {@inheritDoc} */
    @Override
    public final String description()
    {
        return "<html><h1>Simulation of a crossing with traffic lights</h1>"
                + "Simulation of four double lane roads with a crossing in the middle.</html>";
    }

}

/**
 * Simulate four double lane roads with a crossing in the middle.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2016-10-28 16:34:11 +0200 (Fri, 28 Oct 2016) $, @version $Revision: 2429 $, by $Author: pknoppers $,
 * initial version ug 1, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
class CrossingTrafficLightstModel implements OTSModelInterface, UNITS
{
    /** */
    private static final long serialVersionUID = 20140815L;

    /** The simulator. */
    private OTSDEVSSimulatorInterface simulator;

    /** The network. */
    private final OTSNetwork network = new OTSNetwork("network");

    /** the random stream for this demo. */
    private StreamInterface stream = new MersenneTwister(555);

    /** The headway (inter-vehicle time) distribution. */
    private ContinuousDistDoubleScalar.Rel<Duration, TimeUnit> headwayDistribution =
            new ContinuousDistDoubleScalar.Rel<>(new DistTriangular(this.stream, 7, 9, 15), TimeUnit.SECOND);

    /** The speed distribution. */
    private ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> speedDistribution =
            new ContinuousDistDoubleScalar.Rel<>(new DistTriangular(this.stream, 50, 60, 70), SpeedUnit.KM_PER_HOUR);

    /** Number of cars created. */
    private int carsCreated = 0;

    /** Type of all GTUs. */
    private GTUType gtuType = new GTUType("Car");

    /** The car following model, e.g. IDM Plus for cars. */
    private GTUFollowingModelOld carFollowingModel;

    /** User settable properties. */
    private List<Property<?>> properties = null;

    /** The GTUColorer for the generated vehicles. */
    private final GTUColorer gtuColorer;

    /** The speed limit on all Lanes. */
    private Speed speedLimit = new Speed(80, KM_PER_HOUR);

    /**
     * @param properties the user settable properties
     * @param gtuColorer the default and initial GTUColorer, e.g. a DefaultSwitchableTUColorer.
     */
    CrossingTrafficLightstModel(final List<Property<?>> properties, final GTUColorer gtuColorer)
    {
        this.properties = properties;
        this.gtuColorer = gtuColorer;
    }

    /** {@inheritDoc} */
    @Override
    public final void constructModel(final SimulatorInterface<Time, Duration, OTSSimTimeDouble> theSimulator)
            throws SimRuntimeException, RemoteException
    {
        this.simulator = (OTSDEVSSimulatorInterface) theSimulator;
        try
        {
            OTSNode[][] nodes = new OTSNode[4][4];
            nodes[0][0] = new OTSNode(this.network, "sn1", new OTSPoint3D(10, -500));
            nodes[0][1] = new OTSNode(this.network, "sn2", new OTSPoint3D(10, -20));
            nodes[0][2] = new OTSNode(this.network, "sn3", new OTSPoint3D(10, +20));
            nodes[0][3] = new OTSNode(this.network, "sn4", new OTSPoint3D(10, +5000));

            nodes[1][0] = new OTSNode(this.network, "we1", new OTSPoint3D(-500, -10));
            nodes[1][1] = new OTSNode(this.network, "we2", new OTSPoint3D(-20, -10));
            nodes[1][2] = new OTSNode(this.network, "we3", new OTSPoint3D(+20, -10));
            nodes[1][3] = new OTSNode(this.network, "we4", new OTSPoint3D(+5000, -10));

            nodes[2][0] = new OTSNode(this.network, "ns1", new OTSPoint3D(-10, +500));
            nodes[2][1] = new OTSNode(this.network, "ns2", new OTSPoint3D(-10, +20));
            nodes[2][2] = new OTSNode(this.network, "ns3", new OTSPoint3D(-10, -20));
            nodes[2][3] = new OTSNode(this.network, "ns4", new OTSPoint3D(-10, -5000));

            nodes[3][0] = new OTSNode(this.network, "ew1", new OTSPoint3D(+500, 10));
            nodes[3][1] = new OTSNode(this.network, "ew2", new OTSPoint3D(+20, 10));
            nodes[3][2] = new OTSNode(this.network, "ew3", new OTSPoint3D(-20, 10));
            nodes[3][3] = new OTSNode(this.network, "ew4", new OTSPoint3D(-5000, 10));

            Set<GTUType> compatibility = new HashSet<>();
            compatibility.add(this.gtuType);
            LaneType laneType = new LaneType("CarLane", compatibility);

            Map<Lane, SimpleTrafficLight> trafficLights = new HashMap<>();

            for (int i = 0; i < 4; i++)
            {
                for (int j = 0; j < 3; j++)
                {
                    Lane[] lanes = LaneFactory.makeMultiLane(this.network,
                            "Lane_" + nodes[i][j].getId() + "-" + nodes[i][j + 1].getId(), nodes[i][j], nodes[i][j + 1], null,
                            2, laneType, this.speedLimit, this.simulator, LongitudinalDirectionality.DIR_PLUS);
                    if (j == 0)
                    {
                        for (Lane lane : lanes)
                        {
                            this.simulator.scheduleEventRel(this.headwayDistribution.draw(), this, this, "generateCar",
                                    new Object[] { lane });
                            SimpleTrafficLight tl = new SimpleTrafficLight(lane.getId() + "_TL", lane,
                                    new Length(lane.getLength().minus(new Length(10.0, LengthUnit.METER))), this.simulator);
                            lane.addLaneBasedObject(tl);
                            trafficLights.put(lane, tl);
                            if (i == 0 || i == 2)
                            {
                                this.simulator.scheduleEventRel(new Duration(0.0, TimeUnit.SECOND), this, this, "changeTL",
                                    new Object[] { tl });
                            }
                            else
                            {
                                this.simulator.scheduleEventRel(new Duration(75.0, TimeUnit.SECOND), this, this, "changeTL",
                                        new Object[] { tl });
                            }
                        }
                    }
                    if (j == 2)
                    {
                        for (Lane lane : lanes)
                        {
                            Sensor sensor = new SinkSensor(lane, new Length(500.0, METER), this.simulator);
                            lane.addSensor(sensor, GTUType.ALL);
                        }
                    }
                }
            }

            String carFollowingModelName = null;
            CompoundProperty propertyContainer = new CompoundProperty("", "", "", this.properties, false, 0);
            Property<?> cfmp = propertyContainer.findByKey("CarFollowingModel");
            if (null == cfmp)
            {
                throw new Error("Cannot find \"Car following model\" property");
            }
            if (cfmp instanceof SelectionProperty)
            {
                carFollowingModelName = ((SelectionProperty) cfmp).getValue();
            }
            else
            {
                throw new Error("\"Car following model\" property has wrong type");
            }
            for (Property<?> ap : new CompoundProperty("", "", "", this.properties, false, 0))
            {
                if (ap instanceof SelectionProperty)
                {
                    SelectionProperty sp = (SelectionProperty) ap;
                    if ("CarFollowingModel".equals(sp.getKey()))
                    {
                        carFollowingModelName = sp.getValue();
                    }
                }
                else if (ap instanceof CompoundProperty)
                {
                    CompoundProperty cp = (CompoundProperty) ap;
                    if (ap.getKey().equals("OutputGraphs"))
                    {
                        continue; // Output settings are handled elsewhere
                    }
                    if (ap.getKey().contains("IDM"))
                    {
                        Acceleration a = IDMPropertySet.getA(cp);
                        Acceleration b = IDMPropertySet.getB(cp);
                        Length s0 = IDMPropertySet.getS0(cp);
                        Duration tSafe = IDMPropertySet.getTSafe(cp);
                        GTUFollowingModelOld gtuFollowingModel = null;
                        if (carFollowingModelName.equals("IDM"))
                        {
                            gtuFollowingModel = new IDMOld(a, b, s0, tSafe, 1.0);
                        }
                        else if (carFollowingModelName.equals("IDM+"))
                        {
                            gtuFollowingModel = new IDMPlusOld(a, b, s0, tSafe, 1.0);
                        }
                        else
                        {
                            throw new Error("Unknown gtu following model: " + carFollowingModelName);
                        }
                        this.carFollowingModel = gtuFollowingModel;
                    }
                }
            }
        }
        catch (SimRuntimeException | NamingException | NetworkException | OTSGeometryException | PropertyException exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * Change the traffic light to a new color.
     * @param tl the traffic light
     * @throws SimRuntimeException when scheduling fails
     */
    protected final void changeTL(final TrafficLight tl) throws SimRuntimeException
    {
        if (tl.getTrafficLightColor().isRed())
        {
            tl.setTrafficLightColor(TrafficLightColor.GREEN);
            this.simulator.scheduleEventRel(new Duration(60.0, TimeUnit.SECOND), this, this, "changeTL", new Object[] { tl });
        }
        else if (tl.getTrafficLightColor().isGreen())
        {
            tl.setTrafficLightColor(TrafficLightColor.YELLOW);
            this.simulator.scheduleEventRel(new Duration(15.0, TimeUnit.SECOND), this, this, "changeTL", new Object[] { tl });
        }
        else if (tl.getTrafficLightColor().isYellow())
        {
            tl.setTrafficLightColor(TrafficLightColor.RED);
            this.simulator.scheduleEventRel(new Duration(75.0, TimeUnit.SECOND), this, this, "changeTL", new Object[] { tl });
        }
    }

    /**
     * Generate cars at a fixed rate (implemented by re-scheduling this method).
     * @param lane the lane to generate the car on
     */
    protected final void generateCar(final Lane lane)
    {
        Length initialPosition = new Length(10, METER);
        Speed initialSpeed = new Speed(0, KM_PER_HOUR);
        Set<DirectedLanePosition> initialPositions = new LinkedHashSet<>(1);
        try
        {
            initialPositions.add(new DirectedLanePosition(lane, initialPosition, GTUDirectionality.DIR_PLUS));
            Length vehicleLength = new Length(4, METER);
            GTUFollowingModelOld gtuFollowingModel = this.carFollowingModel;
            if (null == gtuFollowingModel)
            {
                throw new Error("gtuFollowingModel is null");
            }
            BehavioralCharacteristics behavioralCharacteristics = DefaultsFactory.getDefaultBehavioralCharacteristics();
            LaneBasedIndividualGTU gtu = new LaneBasedIndividualGTU("" + (++this.carsCreated), this.gtuType, vehicleLength,
                    new Length(1.8, METER), this.speedDistribution.draw(), this.simulator, this.network);
            LaneBasedStrategicalPlanner strategicalPlanner = new LaneBasedStrategicalRoutePlanner(behavioralCharacteristics,
                    new LaneBasedGTUFollowingTacticalPlanner(gtuFollowingModel, gtu), gtu);
            gtu.initWithAnimation(strategicalPlanner, initialPositions, initialSpeed, DefaultCarAnimation.class,
                    this.gtuColorer);
            this.simulator.scheduleEventRel(this.headwayDistribution.draw(), this, this, "generateCar", new Object[] { lane });
        }
        catch (SimRuntimeException | NamingException | NetworkException | GTUException | OTSGeometryException exception)
        {
            exception.printStackTrace();
        }
    }

    /** {@inheritDoc} */
    @Override
    public final SimulatorInterface<Time, Duration, OTSSimTimeDouble> getSimulator() throws RemoteException
    {
        return this.simulator;
    }
}
