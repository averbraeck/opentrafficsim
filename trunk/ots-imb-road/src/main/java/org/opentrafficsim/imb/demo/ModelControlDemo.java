package org.opentrafficsim.imb.demo;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.naming.NamingException;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.djunits.unit.LengthUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.unit.UNITS;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.base.modelproperties.BooleanProperty;
import org.opentrafficsim.base.modelproperties.CompoundProperty;
import org.opentrafficsim.base.modelproperties.ContinuousProperty;
import org.opentrafficsim.base.modelproperties.IntegerProperty;
import org.opentrafficsim.base.modelproperties.ProbabilityDistributionProperty;
import org.opentrafficsim.base.modelproperties.Property;
import org.opentrafficsim.base.modelproperties.PropertyException;
import org.opentrafficsim.base.modelproperties.SelectionProperty;
import org.opentrafficsim.core.dsol.OTSDEVSRealTimeClock;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.animation.DefaultSwitchableGTUColorer;
import org.opentrafficsim.core.gtu.animation.GTUColorer;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristics;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.core.network.OTSNode;
import org.opentrafficsim.graphs.LaneBasedGTUSampler;
import org.opentrafficsim.imb.IMBException;
import org.opentrafficsim.imb.connector.IMBConnector;
import org.opentrafficsim.imb.transceiver.urbanstrategy.GTUTransceiver;
import org.opentrafficsim.imb.transceiver.urbanstrategy.LaneGTUTransceiver;
import org.opentrafficsim.imb.transceiver.urbanstrategy.LinkGTUTransceiver;
import org.opentrafficsim.imb.transceiver.urbanstrategy.NetworkTransceiver;
import org.opentrafficsim.imb.transceiver.urbanstrategy.NodeTransceiver;
import org.opentrafficsim.imb.transceiver.urbanstrategy.SensorGTUTransceiver;
import org.opentrafficsim.imb.transceiver.urbanstrategy.SimulatorTransceiver;
import org.opentrafficsim.road.gtu.animation.DefaultCarAnimation;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.LaneBasedIndividualGTU;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedCFLCTacticalPlannerFactory;
import org.opentrafficsim.road.gtu.lane.tactical.following.AbstractIDM;
import org.opentrafficsim.road.gtu.lane.tactical.following.GTUFollowingModelOld;
import org.opentrafficsim.road.gtu.lane.tactical.following.IDMOld;
import org.opentrafficsim.road.gtu.lane.tactical.following.IDMPlusFactory;
import org.opentrafficsim.road.gtu.lane.tactical.following.IDMPlusOld;
import org.opentrafficsim.road.gtu.lane.tactical.lanechangemobil.AbstractLaneChangeModel;
import org.opentrafficsim.road.gtu.lane.tactical.lanechangemobil.Altruistic;
import org.opentrafficsim.road.gtu.lane.tactical.lanechangemobil.Egoistic;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.LMRSFactory;
import org.opentrafficsim.road.gtu.lane.tactical.toledo.ToledoFactory;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlannerFactory;
import org.opentrafficsim.road.gtu.strategical.route.LaneBasedStrategicalRoutePlannerFactory;
import org.opentrafficsim.road.modelproperties.IDMPropertySet;
import org.opentrafficsim.road.network.factory.LaneFactory;
import org.opentrafficsim.road.network.lane.AbstractSensor;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneType;
import org.opentrafficsim.road.network.lane.SensorAnimation;

import nl.tno.imb.TConnection;
import nl.tno.imb.mc.ModelParameters;
import nl.tno.imb.mc.ModelStarter;
import nl.tno.imb.mc.ModelState;
import nl.tno.imb.mc.Parameter;
import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.animation.D2.AnimationPanel;
import nl.tudelft.simulation.dsol.experiment.Replication;
import nl.tudelft.simulation.dsol.experiment.ReplicationMode;
import nl.tudelft.simulation.dsol.simulators.AnimatorInterface;
import nl.tudelft.simulation.dsol.simulators.Simulator;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.event.Event;
import nl.tudelft.simulation.language.d3.DirectedPoint;

/**
 * Demonstrate an implementation of ModelEvent.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Oct 21, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class ModelControlDemo extends ModelStarter
{
    /** The thread that does the work. */
    private CircularRoadIMB model = null;

    /**
     * @param args
     * @param providedModelName
     * @param providedModelId
     * @throws IMBException
     */
    public ModelControlDemo(String[] args, String providedModelName, int providedModelId) throws IMBException
    {
        super(args, providedModelName, providedModelId);
    }

    /**
     * Find a particular property in a list of (compound) properties. <br>
     * Re-computing the keyPath is somewhat inefficient. Could be improved if we do not use the iterator of Property.
     * @param properties List&lt;Property&lt;?&gt;&gt;; the list
     * @param key String; the key path of the sought property
     * @return Property&lt;?&gt;; the first matching property, or null if no property with the given key was found
     */
    private Property<?> findPropertyInList(final List<Property<?>> properties, final String key)
    {
        for (Property<?> property : properties)
        {
            for (Property<?> p : property)
            {
                String keyPath = p.getKey();
                for (Property<?> parent = p.getParent(); null != parent; parent = parent.getParent())
                {
                    keyPath = parent.getKey() + "." + keyPath;
                }
                // System.out.println("Comparing property key path " + keyPath + " to key " + key);
                if (key.equals(keyPath))
                {
                    return property;
                }
            }
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void startModel(final ModelParameters parameters, final TConnection tConnection)
    {
        System.out.println("startModel called");
        System.out.println("parameters: " + parameters);
        System.out.println("Connection: " + this.connection);
        String dataSource = null;
        List<Property<?>> properties = CircularRoadIMB.getSupportedProperties();

        for (String parameterName : parameters.getParameterNames())
        {
            if (parameterName.equals("Federation"))
            {
                continue;
            }
            else if (parameterName.equals("DataSource"))
            {
                dataSource = (String) parameters.getParameterByName(parameterName).getValue();
                continue;
            }
            int pos = parameterName.indexOf(" (");
            if (pos < 0)
            {
                System.out.println("ignoring parameter " + parameterName);
                continue;
            }
            String strippedName = parameterName.substring(0, pos);
            switch (strippedName)
            {
                case "Truck fraction":
                {
                    Property<?> p = findPropertyInList(properties, "TrafficComposition");
                    if (null == p || !(p instanceof ProbabilityDistributionProperty))
                    {
                        System.err.println("Property " + p + " is not a ProbalityDistributionProperty");
                    }
                    else
                    {
                        ProbabilityDistributionProperty pdp = (ProbabilityDistributionProperty) p;
                        Double[] values = pdp.getValue();
                        values[1] = (double) parameters.getParameterByName(parameterName).getValue();
                        values[0] = 1.0 - values[1];
                        try
                        {
                            System.out.println("Setting TrafficComposition to " + values);
                            pdp.setValue(values);
                        }
                        catch (PropertyException exception)
                        {
                            exception.printStackTrace();
                        }
                    }
                    break;
                }
                default:
                    System.out.println("Ignoring parameter " + parameterName);
                    break;
            }
        }
        System.out.println("Not doing anything with dataSource " + dataSource);
        // TODO: do something with the dataSource
        try
        {
            IMBConnector simulationIMBConnector = new IMBConnector(this.connection);
            System.out.println("IMBConnector for simulation is " + simulationIMBConnector);
            this.model =
                    new CircularRoadIMB(new DefaultSwitchableGTUColorer(), new OTSNetwork(""), properties,
                            simulationIMBConnector);
            Replication<Time, Duration, OTSSimTimeDouble> replication =
                    new Replication<Time, Duration, OTSSimTimeDouble>("rep1", new OTSSimTimeDouble(Time.ZERO), Duration.ZERO,
                            new Duration(1, TimeUnit.HOUR), this.model);
            OTSDEVSRealTimeClock simulator = new OTSDEVSRealTimeClock();
            simulator.initialize(replication, ReplicationMode.TERMINATING);
            signalModelState(ModelState.READY);
            System.out.println("Reported ModelState.READY");
        }
        catch (PropertyException | IMBException | SimRuntimeException | NamingException exception)
        {
            exception.printStackTrace();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void stopModel()
    {
        System.out.println("stopModel called");
        try
        {
            ((Simulator<Time, Duration, OTSSimTimeDouble>) this.model.getSimulator()).cleanUp();
            this.model.closeWindow();
            this.model = null;
        }
        catch (RemoteException exception)
        {
            exception.printStackTrace();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void quitApplication()
    {
        if (null != this.model)
        {
            try
            {
                // clean up; even if stopModel was not called before quitApplication
                ((Simulator<Time, Duration, OTSSimTimeDouble>) this.model.getSimulator()).cleanUp();
            }
            catch (Exception exception)
            {
                System.out.println("caught Exception in quitApplication:");
                exception.printStackTrace();
            }
        }
        System.out.println("quitApplication called");
    }

    /** {@inheritDoc} */
    @Override
    public void parameterRequest(ModelParameters parameters)
    {
        System.out.println("serving parameter request");
        System.out.println("received parameters: " + parameters);
        double currentTruckFraction = Double.NaN;
        List<Property<?>> propertyList = CircularRoadIMB.getSupportedProperties();
        for (Property<?> property : propertyList)
        {
            Property<?> truckFraction = property.findByKey("TrafficComposition");
            if (null != truckFraction)
            {
                currentTruckFraction = ((ProbabilityDistributionProperty) truckFraction).getValue()[1];
                break;
            }
        }
        if (Double.isNaN(currentTruckFraction))
        {
            currentTruckFraction = 0.2;
            System.out.println("Could not find the traffic composition property");
        }
        parameters.addParameter(new Parameter("Truck fraction (range 0.0 - 1.0)", currentTruckFraction));
        System.out.println("(possibly) modified paramters: " + parameters);
    }

    /**
     * Entry point
     * @param args
     * @throws InvocationTargetException
     * @throws InterruptedException
     */
    public static void main(final String[] args) throws InvocationTargetException, InterruptedException
    {
        SwingUtilities.invokeAndWait(new Runnable()
        {

            @Override
            public void run()
            {
                try
                {
                    new ModelControlDemo(args, "Demo Model", 1234);
                }
                catch (IMBException exception)
                {
                    exception.printStackTrace();
                }
            }
        });
    }

    /**
     * Simulate traffic on a circular, two-lane road.
     * <p>
     * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * <p>
     * $LastChangedDate: 2016-08-24 13:50:36 +0200 (Wed, 24 Aug 2016) $, @version $Revision: 2144 $, by $Author: pknoppers $,
     * initial version 1 nov. 2014 <br>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     */
    static class CircularRoadIMB implements OTSModelInterface, UNITS
    {
        /** */
        private static final long serialVersionUID = 20141121L;

        /** The simulator. */
        private OTSDEVSSimulatorInterface simulator;

        /** Number of cars created. */
        private int carsCreated = 0;

        /** The car following model, e.g. IDM Plus for cars. */
        private GTUFollowingModelOld carFollowingModelCars;

        /** The car following model, e.g. IDM Plus for trucks. */
        private GTUFollowingModelOld carFollowingModelTrucks;

        /** The probability that the next generated GTU is a passenger car. */
        private double carProbability;

        /** The lane change model. */
        private AbstractLaneChangeModel laneChangeModel;

        /** Minimum distance. */
        private Length minimumDistance = new Length(0, METER);

        /** The speed limit. */
        private Speed speedLimit = new Speed(100, KM_PER_HOUR);

        /** The plots. */
        private List<LaneBasedGTUSampler> plots = new ArrayList<LaneBasedGTUSampler>();

        /** User settable properties. */
        private final List<Property<?>> properties;

        /** The sequence of Lanes that all vehicles will follow. */
        private List<List<Lane>> paths = new ArrayList<>();

        /** The random number generator used to decide what kind of GTU to generate. */
        private Random randomGenerator = new Random(12345);

        /** The GTUColorer for the generated vehicles. */
        private final GTUColorer gtuColorer;

        /** Strategical planner generator for cars. */
        private LaneBasedStrategicalPlannerFactory<LaneBasedStrategicalPlanner> strategicalPlannerGeneratorCars = null;

        /** Strategical planner generator for cars. */
        private LaneBasedStrategicalPlannerFactory<LaneBasedStrategicalPlanner> strategicalPlannerGeneratorTrucks = null;

        /** the network as created by the AbstractWrappableIMBAnimation. */
        private final OTSNetwork network;

        /** Connector to the IMB hub. */
        private final IMBConnector imbConnector;

        /** The window with the animation. */
        private JFrame frame;

        /**
         * @param gtuColorer the default and initial GTUColorer, e.g. a DefaultSwitchableTUColorer.
         * @param network Network; the network
         * @param properties AbstractProperty; properties that configure this simulation
         * @param imbConnector IMBConnector; connection to the IMB hub
         * @throws PropertyException
         */
        CircularRoadIMB(final GTUColorer gtuColorer, final OTSNetwork network, final List<Property<?>> properties,
                final IMBConnector imbConnector) throws PropertyException
        {
            this.properties = properties;
            this.gtuColorer = gtuColorer;
            this.network = network;
            this.imbConnector = imbConnector;
        }

        /**
         * Construct and return the list of properties that the user may modify.
         * @return List&lt;AbstractProperty&gt;; the list of properties that the user may modify
         */
        public static List<Property<?>> getSupportedProperties()
        {
            List<Property<?>> result = new ArrayList<>();
            result.add(new SelectionProperty("LaneChanging", "Lane changing",
                    "<html>The lane change strategies vary in politeness.<br>"
                            + "Two types are implemented:<ul><li>Egoistic (looks only at personal gain).</li>"
                            + "<li>Altruistic (assigns effect on new and current follower the same weight as "
                            + "the personal gain).</html>", new String[] { "Egoistic", "Altruistic" }, 0, false, 500));
            result.add(new SelectionProperty("TacticalPlanner", "Tactical planner",
                    "<html>The tactical planner determines if a lane change is desired and possible.</html>", new String[] {
                            "MOBIL", "LMRS", "Toledo" }, 0, false, 600));
            result.add(new IntegerProperty("TrackLength", "Track length", "Circumference of the track", 2000, 500, 6000,
                    "Track length %dm", false, 10));
            result.add(new ContinuousProperty("MeanDensity", "Mean density", "Number of vehicles per km", 40.0, 5.0, 45.0,
                    "Density %.1f veh/km", false, 11));
            result.add(new ContinuousProperty("DensityVariability", "Density variability",
                    "Variability of the number of vehicles per km", 0.0, 0.0, 1.0, "%.1f", false, 12));
            List<Property<?>> outputProperties = new ArrayList<>();
            try
            {
                for (int lane = 1; lane <= 2; lane++)
                {
                    String laneId = String.format("Lane %d ", lane);
                    outputProperties.add(new BooleanProperty(laneId + "Density", laneId + " Density", laneId
                            + "Density contour plot", true, false, 0));
                    outputProperties.add(new BooleanProperty(laneId + "Flow", laneId + " Flow", laneId + "Flow contour plot",
                            true, false, 1));
                    outputProperties.add(new BooleanProperty(laneId + "Speed", laneId + " Speed",
                            laneId + "Speed contour plot", true, false, 2));
                    outputProperties.add(new BooleanProperty(laneId + "Acceleration", laneId + " Acceleration", laneId
                            + "Acceleration contour plot", true, false, 3));
                    outputProperties.add(new BooleanProperty(laneId + "Trajectories", laneId + " Trajectories", laneId
                            + "Trajectory (time/distance) diagram", true, false, 4));
                }
                result.add(new CompoundProperty("OutputGraphs", "Output graphs", "Select the graphical output",
                        outputProperties, true, 1000));
                result.add(new ProbabilityDistributionProperty("TrafficComposition", "Traffic composition",
                        "<html>Mix of passenger cars and trucks</html>", new String[] { "passenger car", "truck" },
                        new Double[] { 0.8, 0.2 }, false, 10));
                result.add(new SelectionProperty("CarFollowingModel", "Car following model",
                        "<html>The car following model determines "
                                + "the acceleration that a vehicle will make taking into account "
                                + "nearby vehicles, infrastructural restrictions (e.g. speed limit, "
                                + "curvature of the road) capabilities of the vehicle and personality "
                                + "of the driver.</html>", new String[] { "IDM", "IDM+" }, 1, false, 1));
                result.add(IDMPropertySet.makeIDMPropertySet("IDMCar", "Car", new Acceleration(1.0, METER_PER_SECOND_2),
                        new Acceleration(1.5, METER_PER_SECOND_2), new Length(2.0, METER), new Duration(1.0, SECOND), 2));
                result.add(IDMPropertySet.makeIDMPropertySet("IDMTruck", "Truck", new Acceleration(0.5, METER_PER_SECOND_2),
                        new Acceleration(1.25, METER_PER_SECOND_2), new Length(2.0, METER), new Duration(1.0, SECOND), 3));
            }
            catch (PropertyException exception)
            {
                exception.printStackTrace();
            }
            return result;
        }

        /**
         * Retrieve the Network.
         * @return Network; the network
         */
        public Network getNetwork()
        {
            return this.network;
        }

        /**
         * @param index int; the rank number of the path
         * @return List&lt;Lane&gt;; the set of lanes for the specified index
         */
        public List<Lane> getPath(final int index)
        {
            return this.paths.get(index);
        }

        /** {@inheritDoc} */
        @Override
        public void constructModel(final SimulatorInterface<Time, Duration, OTSSimTimeDouble> theSimulator)
                throws SimRuntimeException, RemoteException
        {
            AnimationPanel panel = null;
            if (theSimulator instanceof AnimatorInterface)
            {
                this.frame = new JFrame("Circular Road Simulation with IMB Model Control");
                panel =
                        new AnimationPanel(new Rectangle2D.Double(-1000, -1000, 2000, 2000), new Dimension(1000, 1000),
                                theSimulator);
                this.frame.add(panel);
                this.frame.setSize(new Dimension(1000, 1000));
                this.frame.setVisible(true);
                // Tell the animation to build the list of animation objects.
                panel.notify(new Event(SimulatorInterface.START_REPLICATION_EVENT, theSimulator, null));
            }
            OTSDEVSSimulatorInterface imbAnimator = (OTSDEVSSimulatorInterface) theSimulator;
            if (null != this.imbConnector)
            {
                try
                {
                    System.out.println("CirularRoadIMB: constructModel called; Connecting to IMB");
                    new NetworkTransceiver(this.imbConnector, imbAnimator, this.network);
                    new NodeTransceiver(this.imbConnector, imbAnimator, this.network);
                    new LinkGTUTransceiver(this.imbConnector, imbAnimator, this.network);
                    new LaneGTUTransceiver(this.imbConnector, imbAnimator, this.network);
                    new GTUTransceiver(this.imbConnector, imbAnimator, this.network);
                    new SensorGTUTransceiver(this.imbConnector, imbAnimator, this.network);
                    new SimulatorTransceiver(this.imbConnector, imbAnimator);
                }
                catch (IMBException exception)
                {
                    throw new SimRuntimeException(exception);
                }
            }
            final int laneCount = 2;
            for (int laneIndex = 0; laneIndex < laneCount; laneIndex++)
            {
                this.paths.add(new ArrayList<Lane>());
            }
            this.simulator = (OTSDEVSSimulatorInterface) theSimulator;
            double radius = 6000 / 2 / Math.PI;
            double headway = 40;
            double headwayVariability = 0;
            try
            {
                // Get car-following model name
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

                // Get car-following model parameter
                for (Property<?> ap : new CompoundProperty("", "", "", this.properties, false, 0))
                {
                    if (ap instanceof CompoundProperty)
                    {
                        CompoundProperty cp = (CompoundProperty) ap;
                        // System.out.println("Checking compound property " + cp);
                        if (ap.getKey().contains("IDM"))
                        {
                            System.out.println("Car following model name appears to be " + ap.getKey());
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
                            if (ap.getKey().contains("Car"))
                            {
                                this.carFollowingModelCars = gtuFollowingModel;
                            }
                            else if (ap.getKey().contains("Truck"))
                            {
                                this.carFollowingModelTrucks = gtuFollowingModel;
                            }
                            else
                            {
                                throw new Error("Cannot determine gtu type for " + ap.getKey());
                            }
                        }
                    }
                }

                // Get lane change model
                cfmp = propertyContainer.findByKey("LaneChanging");
                if (null == cfmp)
                {
                    throw new Error("Cannot find \"Lane changing\" property");
                }
                if (cfmp instanceof SelectionProperty)
                {
                    String laneChangeModelName = ((SelectionProperty) cfmp).getValue();
                    if ("Egoistic".equals(laneChangeModelName))
                    {
                        this.laneChangeModel = new Egoistic();
                    }
                    else if ("Altruistic".equals(laneChangeModelName))
                    {
                        this.laneChangeModel = new Altruistic();
                    }
                    else
                    {
                        throw new Error("Lane changing " + laneChangeModelName + " not implemented");
                    }
                }
                else
                {
                    throw new Error("\"Lane changing\" property has wrong type");
                }

                // Get remaining properties
                for (Property<?> ap : new CompoundProperty("", "", "", this.properties, false, 0))
                {
                    if (ap instanceof SelectionProperty)
                    {
                        SelectionProperty sp = (SelectionProperty) ap;
                        if ("TacticalPlanner".equals(sp.getKey()))
                        {
                            String tacticalPlannerName = sp.getValue();
                            if ("MOBIL".equals(tacticalPlannerName))
                            {
                                this.strategicalPlannerGeneratorCars =
                                        new LaneBasedStrategicalRoutePlannerFactory(new LaneBasedCFLCTacticalPlannerFactory(
                                                this.carFollowingModelCars, this.laneChangeModel));
                                this.strategicalPlannerGeneratorTrucks =
                                        new LaneBasedStrategicalRoutePlannerFactory(new LaneBasedCFLCTacticalPlannerFactory(
                                                this.carFollowingModelTrucks, this.laneChangeModel));
                            }
                            else if ("LMRS".equals(tacticalPlannerName))
                            {
                                // provide default parameters with the car-following model
                                BehavioralCharacteristics defaultBehavioralCFCharacteristics = new BehavioralCharacteristics();
                                defaultBehavioralCFCharacteristics.setDefaultParameters(AbstractIDM.class);
                                this.strategicalPlannerGeneratorCars =
                                        new LaneBasedStrategicalRoutePlannerFactory(new LMRSFactory(new IDMPlusFactory(),
                                                defaultBehavioralCFCharacteristics));
                                this.strategicalPlannerGeneratorTrucks =
                                        new LaneBasedStrategicalRoutePlannerFactory(new LMRSFactory(new IDMPlusFactory(),
                                                defaultBehavioralCFCharacteristics));
                            }
                            else if ("Toledo".equals(tacticalPlannerName))
                            {
                                this.strategicalPlannerGeneratorCars =
                                        new LaneBasedStrategicalRoutePlannerFactory(new ToledoFactory());
                                this.strategicalPlannerGeneratorTrucks =
                                        new LaneBasedStrategicalRoutePlannerFactory(new ToledoFactory());
                            }
                            else
                            {
                                throw new Error("Don't know how to create a " + tacticalPlannerName + " tactical planner");
                            }
                        }
                    }
                    else if (ap instanceof ProbabilityDistributionProperty)
                    {
                        ProbabilityDistributionProperty pdp = (ProbabilityDistributionProperty) ap;
                        if (ap.getKey().equals("TrafficComposition"))
                        {
                            this.carProbability = pdp.getValue()[0];
                        }
                    }
                    else if (ap instanceof IntegerProperty)
                    {
                        IntegerProperty ip = (IntegerProperty) ap;
                        if ("TrackLength".equals(ip.getKey()))
                        {
                            radius = ip.getValue() / 2 / Math.PI;
                        }
                    }
                    else if (ap instanceof ContinuousProperty)
                    {
                        ContinuousProperty cp = (ContinuousProperty) ap;
                        if (cp.getKey().equals("MeanDensity"))
                        {
                            headway = 1000 / cp.getValue();
                        }
                        if (cp.getKey().equals("DensityVariability"))
                        {
                            headwayVariability = cp.getValue();
                        }
                    }
                    else if (ap instanceof CompoundProperty)
                    {
                        if (ap.getKey().equals("OutputGraphs"))
                        {
                            continue; // Output settings are handled elsewhere
                        }
                    }
                }
                GTUType gtuType = new GTUType("car");
                Set<GTUType> compatibility = new HashSet<GTUType>();
                compatibility.add(gtuType);
                LaneType laneType = new LaneType("CarLane", compatibility);
                OTSNode start = new OTSNode(this.network, "Start", new OTSPoint3D(radius, 0, 0));
                OTSNode halfway = new OTSNode(this.network, "Halfway", new OTSPoint3D(-radius, 0, 0));

                OTSPoint3D[] coordsHalf1 = new OTSPoint3D[127];
                for (int i = 0; i < coordsHalf1.length; i++)
                {
                    double angle = Math.PI * (1 + i) / (1 + coordsHalf1.length);
                    coordsHalf1[i] = new OTSPoint3D(radius * Math.cos(angle), radius * Math.sin(angle), 0);
                }
                Lane[] lanes1 =
                        LaneFactory.makeMultiLane(this.network, "FirstHalf", start, halfway, coordsHalf1, laneCount, laneType,
                                this.speedLimit, this.simulator, LongitudinalDirectionality.DIR_PLUS);
                OTSPoint3D[] coordsHalf2 = new OTSPoint3D[127];
                for (int i = 0; i < coordsHalf2.length; i++)
                {
                    double angle = Math.PI + Math.PI * (1 + i) / (1 + coordsHalf2.length);
                    coordsHalf2[i] = new OTSPoint3D(radius * Math.cos(angle), radius * Math.sin(angle), 0);
                }
                Lane[] lanes2 =
                        LaneFactory.makeMultiLane(this.network, "SecondHalf", halfway, start, coordsHalf2, laneCount, laneType,
                                this.speedLimit, this.simulator, LongitudinalDirectionality.DIR_PLUS);
                for (int laneIndex = 0; laneIndex < laneCount; laneIndex++)
                {
                    this.paths.get(laneIndex).add(lanes1[laneIndex]);
                    this.paths.get(laneIndex).add(lanes2[laneIndex]);
                }
                // create a sensor on every lane
                int sensorNr = 0;
                for (Lane lane : lanes1)
                {
                    SimpleSilentSensor sensor =
                            new SimpleSilentSensor("sensor " + ++sensorNr, lane, new Length(10.0, LengthUnit.METER),
                                    RelativePosition.FRONT, imbAnimator);
                    lane.addSensor(sensor, gtuType);
                }
                for (Lane lane : lanes2)
                {
                    SimpleSilentSensor sensor =
                            new SimpleSilentSensor("sensor" + ++sensorNr, lane, new Length(20.0, LengthUnit.METER),
                                    RelativePosition.REAR, imbAnimator);
                    lane.addSensor(sensor, gtuType);
                }
                // Put the (not very evenly spaced) cars on the track
                double variability = (headway - 20) * headwayVariability;
                System.out.println("headway is " + headway + " variability limit is " + variability);
                Random random = new Random(12345);
                for (int laneIndex = 0; laneIndex < laneCount; laneIndex++)
                {
                    double lane1Length = lanes1[laneIndex].getLength().getSI();
                    double trackLength = lane1Length + lanes2[laneIndex].getLength().getSI();
                    for (double pos = 0; pos <= trackLength - headway - variability;)
                    {
                        Lane lane = pos >= lane1Length ? lanes2[laneIndex] : lanes1[laneIndex];
                        // Actual headway is uniformly distributed around headway
                        double laneRelativePos = pos > lane1Length ? pos - lane1Length : pos;
                        double actualHeadway = headway + (random.nextDouble() * 2 - 1) * variability;
                        // System.out.println(lane + ", len=" + lane.getLength() + ", pos=" + laneRelativePos);
                        generateCar(new Length(laneRelativePos, METER), lane, gtuType);
                        pos += actualHeadway;
                    }
                }
                // Schedule regular updates of the graph
                this.simulator.scheduleEventAbs(new Time(9.999, SECOND), this, this, "drawGraphs", null);
            }
            catch (SimRuntimeException | NamingException | NetworkException | GTUException | OTSGeometryException
                    | PropertyException exception)
            {
                exception.printStackTrace();
            }
            if (null != panel)
            {
                // panel.zoom(1.0);
                ((AnimatorInterface) this.simulator).updateAnimation();
            }
        }

        /**
         * Close and destroy the window. Please shut down and cleanup the simulator first.
         */
        @SuppressWarnings("unchecked")
        public void closeWindow()
        {
            try
            {
                ((Simulator<Time, Duration, OTSSimTimeDouble>) this.simulator).cleanUp();
            }
            catch (Exception exception)
            {
                exception.printStackTrace();
            }
            this.frame.dispose();
        }

        /**
         * Notify the contour plots that the underlying data has changed.
         */
        protected final void drawGraphs()
        {
            for (LaneBasedGTUSampler plot : this.plots)
            {
                plot.reGraph();
            }
            // Re schedule this method
            try
            {
                this.simulator.scheduleEventAbs(new Time(this.simulator.getSimulatorTime().get().getSI() + 10, SECOND), this,
                        this, "drawGraphs", null);
            }
            catch (SimRuntimeException exception)
            {
                exception.printStackTrace();
            }

        }

        /**
         * Generate cars at a fixed rate (implemented by re-scheduling this method).
         * @param initialPosition Length; the initial position of the new cars
         * @param lane Lane; the lane on which the new cars are placed
         * @param gtuType GTUType&lt;String&gt;; the type of the new cars
         * @throws NamingException on ???
         * @throws SimRuntimeException cannot happen
         * @throws NetworkException on network inconsistency
         * @throws GTUException when something goes wrong during construction of the car
         * @throws OTSGeometryException when the initial position is outside the center line of the lane
         */
        protected final void generateCar(final Length initialPosition, final Lane lane, final GTUType gtuType)
                throws NamingException, NetworkException, SimRuntimeException, GTUException, OTSGeometryException
        {

            // GTU itself
            boolean generateTruck = this.randomGenerator.nextDouble() > this.carProbability;
            Length vehicleLength = new Length(generateTruck ? 15 : 4, METER);
            LaneBasedIndividualGTU gtu =
                    new LaneBasedIndividualGTU("" + (++this.carsCreated), gtuType, vehicleLength, new Length(1.8, METER),
                            new Speed(200, KM_PER_HOUR), this.simulator, this.network);

            // strategical planner
            LaneBasedStrategicalPlanner strategicalPlanner;
            if (!generateTruck)
            {
                strategicalPlanner = this.strategicalPlannerGeneratorCars.create(gtu);
            }
            else
            {
                strategicalPlanner = this.strategicalPlannerGeneratorTrucks.create(gtu);
            }

            // init
            Set<DirectedLanePosition> initialPositions = new LinkedHashSet<>(1);
            initialPositions.add(new DirectedLanePosition(lane, initialPosition, GTUDirectionality.DIR_PLUS));
            Speed initialSpeed = new Speed(0, KM_PER_HOUR);
            gtu.initWithAnimation(strategicalPlanner, initialPositions, initialSpeed, DefaultCarAnimation.class,
                    this.gtuColorer);
        }

        /** {@inheritDoc} */
        @Override
        public SimulatorInterface<Time, Duration, OTSSimTimeDouble> getSimulator() throws RemoteException
        {
            return this.simulator;
        }

        /**
         * @return plots
         */
        public final List<LaneBasedGTUSampler> getPlots()
        {
            return this.plots;
        }

        /**
         * @return minimumDistance
         */
        public final Length getMinimumDistance()
        {
            return this.minimumDistance;
        }

        /**
         * Stop simulation and throw an Error.
         * @param theSimulator OTSDEVSSimulatorInterface; the simulator
         * @param errorMessage String; the error message
         */
        public void stopSimulator(final OTSDEVSSimulatorInterface theSimulator, final String errorMessage)
        {
            System.out.println("Error: " + errorMessage);
            try
            {
                if (theSimulator.isRunning())
                {
                    theSimulator.stop();
                }
            }
            catch (SimRuntimeException exception)
            {
                exception.printStackTrace();
            }
            throw new Error(errorMessage);
        }

        /**
         * Simple sensor that does not provide output, but is drawn on the Lanes.
         * <p>
         * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
         * reserved. <br>
         * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
         * </p>
         * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck
         * $, initial version Sep 18, 2016 <br>
         * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
         * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
         * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
         */
        public static class SimpleSilentSensor extends AbstractSensor
        {
            /** */
            private static final long serialVersionUID = 20150130L;

            /**
             * @param lane the lane of the sensor.
             * @param position the position of the sensor
             * @param triggerPosition RelativePosition.TYPE; the relative position type (e.g., FRONT, REAR) of the vehicle that
             *            triggers the sensor.
             * @param id the id of the sensor.
             * @param simulator the simulator to enable animation.
             * @throws NetworkException when the position on the lane is out of bounds w.r.t. the center line of the lane
             * @throws OTSGeometryException when the geometry of the sensor cannot be calculated, e.g. when the lane width is
             *             zero, or the position is beyond or before the lane length
             */
            public SimpleSilentSensor(final String id, final Lane lane, final Length position,
                    final RelativePosition.TYPE triggerPosition, final OTSDEVSSimulatorInterface simulator)
                    throws NetworkException, OTSGeometryException
            {
                super(id, lane, position, triggerPosition, simulator, Length.ZERO, makeGeometry(lane, position));
                try
                {
                    new SensorAnimation(this, position, simulator, Color.RED);
                }
                catch (RemoteException | NamingException exception)
                {
                    exception.printStackTrace();
                }
            }

            /**
             * Make a geometry perpendicular to the center line of the lane at the given position.
             * @param lane Lane; the lane where the sensor resides
             * @param position Length; The length of the object in the longitudinal direction, on the center line of the lane
             * @return a geometry perpendicular to the center line that describes the sensor
             * @throws OTSGeometryException when the line is ill-formed
             */
            private static final OTSLine3D makeGeometry(final Lane lane, final Length position) throws OTSGeometryException
            {
                DirectedPoint sp = lane.getCenterLine().getLocationExtended(position);
                double w45 = 0.45 * lane.getWidth(position).si;
                double a = sp.getRotZ() + Math.PI / 2.0;
                OTSPoint3D p1 = new OTSPoint3D(sp.x + w45 * Math.cos(a), sp.y - w45 * Math.sin(a), sp.z + 0.0001);
                OTSPoint3D p2 = new OTSPoint3D(sp.x - w45 * Math.cos(a), sp.y + w45 * Math.sin(a), sp.z + 0.0001);
                return new OTSLine3D(p1, p2);
            }

            /** {@inheritDoc} */
            @Override
            public final void triggerResponse(final LaneBasedGTU gtu)
            {
                // do nothing.
            }

            /** {@inheritDoc} */
            @Override
            public final String toString()
            {
                return "SimpleSilentSensor [Lane=" + this.getLane() + "]";
            }
        }
    }

}
