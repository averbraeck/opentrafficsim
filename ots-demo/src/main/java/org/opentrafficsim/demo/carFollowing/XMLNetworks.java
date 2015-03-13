package org.opentrafficsim.demo.carFollowing;

import java.awt.geom.Rectangle2D;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

import org.opentrafficsim.core.car.LaneBasedIndividualCar;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.following.FixedAccelerationModel;
import org.opentrafficsim.core.gtu.following.GTUFollowingModel;
import org.opentrafficsim.core.gtu.following.IDM;
import org.opentrafficsim.core.gtu.following.IDMPlus;
import org.opentrafficsim.core.gtu.lane.changing.AbstractLaneChangeModel;
import org.opentrafficsim.core.gtu.lane.changing.Altruistic;
import org.opentrafficsim.core.gtu.lane.changing.FixedLaneChangeModel;
import org.opentrafficsim.core.gtu.lane.changing.LaneChangeModel;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.factory.LaneFactory;
import org.opentrafficsim.core.network.geotools.NodeGeotools;
import org.opentrafficsim.core.network.lane.CrossSectionLink;
import org.opentrafficsim.core.network.lane.Lane;
import org.opentrafficsim.core.network.lane.LaneType;
import org.opentrafficsim.core.network.lane.SinkLane;
import org.opentrafficsim.core.unit.AccelerationUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Abs;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Rel;
import org.opentrafficsim.simulationengine.AbstractProperty;
import org.opentrafficsim.simulationengine.CompoundProperty;
import org.opentrafficsim.simulationengine.ControlPanel;
import org.opentrafficsim.simulationengine.IDMPropertySet;
import org.opentrafficsim.simulationengine.ProbabilityDistributionProperty;
import org.opentrafficsim.simulationengine.SelectionProperty;
import org.opentrafficsim.simulationengine.SimpleSimulator;
import org.opentrafficsim.simulationengine.WrappableSimulation;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 4 mrt. 2015 <br>
 * @author <a href="http://Hansvanlint.weblog.tudelft.nl">Hans van Lint</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class XMLNetworks implements WrappableSimulation
{
    /** The properties exhibited by this simulation. */
    private ArrayList<AbstractProperty<?>> properties = new ArrayList<AbstractProperty<?>>();

    /**
     * Define the XMLNetworks.
     */
    public XMLNetworks()
    {
        this.properties.add(new SelectionProperty("Network", "Network", new String[]{"Merge 1 plus 1 into 1",
                "Merge 2 plus 1 into 2", "Merge 2 plus 2 into 4", "Split 1 into 1 plus 1", "Split 2 into 1 plus 2",
                "Split 4 into 2 plus 2",}, 0, false, 0));
    }

    /** {@inheritDoc} */
    @Override
    public SimpleSimulator buildSimulator(ArrayList<AbstractProperty<?>> userModifiedProperties)
            throws SimRuntimeException, RemoteException, NetworkException
    {
        XMLNetworkModel model = new XMLNetworkModel(userModifiedProperties);
        SimpleSimulator result =
                new SimpleSimulator(new DoubleScalar.Abs<TimeUnit>(0.0, TimeUnit.SECOND),
                        new DoubleScalar.Rel<TimeUnit>(0.0, TimeUnit.SECOND), new DoubleScalar.Rel<TimeUnit>(1800.0,
                                TimeUnit.SECOND), model, new Rectangle2D.Double(-50, -200, 600, 400));
        new ControlPanel(result);
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public String shortName()
    {
        return "Test networks";
    }

    /** {@inheritDoc} */
    @Override
    public String description()
    {
        return "<html><h1>Test Networks</h1>Prove that the test networks can be constructed and rendered on screen.</html>";
    }

    /** {@inheritDoc} */
    @Override
    public ArrayList<AbstractProperty<?>> getProperties()
    {
        // Create and return a deep copy of the internal list
        return new ArrayList<AbstractProperty<?>>(this.properties);
    }

}

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 4 mrt. 2015 <br>
 * @author <a href="http://Hansvanlint.weblog.tudelft.nl">Hans van Lint</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
class XMLNetworkModel implements OTSModelInterface
{
    /** */
    private static final long serialVersionUID = 20150304L;

    /** the simulator. */
    private OTSDEVSSimulatorInterface simulator;

    /** User settable properties */
    ArrayList<AbstractProperty<?>> properties = null;

    /** the headway (inter-vehicle time). */
    private DoubleScalar.Rel<TimeUnit> headway;

    /** number of cars created. */
    private int carsCreated = 0;
    
    /** type of all GTUs (required to permit lane changing). */
    GTUType<String> gtuType = new GTUType<String>("Car");

    /** the car following model, e.g. IDM Plus for cars. */
    private GTUFollowingModel carFollowingModelCars;

    /** the car following model, e.g. IDM Plus for trucks. */
    private GTUFollowingModel carFollowingModelTrucks;

    /** The lane change model. */
    protected AbstractLaneChangeModel laneChangeModel = new Altruistic();

    /** The probability that the next generated GTU is a passenger car. */
    private double carProbability;

    /** The random number generator used to decide what kind of GTU to generate. */
    private Random randomGenerator = new Random(12345);

    /**
     * @param userModifiedProperties
     */
    public XMLNetworkModel(ArrayList<AbstractProperty<?>> userModifiedProperties)
    {
        this.properties = userModifiedProperties;
    }

    /** {@inheritDoc} */
    @Override
    public void constructModel(
            SimulatorInterface<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> theSimulator)
            throws SimRuntimeException, RemoteException
    {
        this.simulator = (OTSDEVSSimulatorInterface) theSimulator;
        this.simulator = (OTSDEVSSimulatorInterface) theSimulator;
        NodeGeotools.STR from = new NodeGeotools.STR("From", new Coordinate(0, 0, 0));
        NodeGeotools.STR end = new NodeGeotools.STR("End", new Coordinate(500, 0, 0));
        NodeGeotools.STR from2 = new NodeGeotools.STR("From", new Coordinate(0, -50, 0));
        NodeGeotools.STR firstVia = new NodeGeotools.STR("From", new Coordinate(200, 0, 0));
        NodeGeotools.STR end2 = new NodeGeotools.STR("End", new Coordinate(500, -50, 0));
        NodeGeotools.STR secondVia = new NodeGeotools.STR("End", new Coordinate(300, 0, 0));
        CompoundProperty cp = new CompoundProperty("", "", this.properties, false, 0);
        String networkType = (String) cp.findByShortName("Network").getValue();
        boolean merge = networkType.startsWith("M");
        int lanesOnMain = Integer.parseInt(networkType.split(" ")[merge ? 1 : 5]);
        int lanesOnBranch = Integer.parseInt(networkType.split(" ")[3]);
        int lanesOnCommon = lanesOnMain + lanesOnBranch;
        int lanesOnCommonCompressed = Integer.parseInt(networkType.split(" ")[merge ? 5 : 1]);

        LaneType<String> laneType = new LaneType<String>("CarLane");
        laneType.addPermeability(this.gtuType);
        try
        {
            String carFollowingModelName = null;
            CompoundProperty propertyContainer = new CompoundProperty("", "", this.properties, false, 0);
            AbstractProperty<?> cfmp = propertyContainer.findByShortName("Car following model");
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
            Iterator<AbstractProperty<ArrayList<AbstractProperty<?>>>> iterator =
                    new CompoundProperty("", "", this.properties, false, 0).iterator();
            while (iterator.hasNext())
            {
                AbstractProperty<?> ap = iterator.next();
                if (ap instanceof SelectionProperty)
                {
                    SelectionProperty sp = (SelectionProperty) ap;
                    if ("Car following model".equals(sp.getShortName()))
                    {
                        if ("Car following model".equals(sp.getShortName()))
                        {
                            carFollowingModelName = sp.getValue();
                        }
                    }
                }
                else if (ap instanceof ProbabilityDistributionProperty)
                {
                    ProbabilityDistributionProperty pdp = (ProbabilityDistributionProperty) ap;
                    String modelName = ap.getShortName();
                    if (modelName.equals("Traffic composition"))
                    {
                        this.carProbability = pdp.getValue()[0];
                    }
                }
                else if (ap instanceof CompoundProperty)
                {
                    CompoundProperty compoundProperty = (CompoundProperty) ap;
                    if (ap.getShortName().equals("Output"))
                    {
                        continue; // Output settings are handled elsewhere
                    }
                    if (ap.getShortName().contains("IDM"))
                    {
                        DoubleScalar.Abs<AccelerationUnit> a = IDMPropertySet.getA(compoundProperty);
                        DoubleScalar.Abs<AccelerationUnit> b = IDMPropertySet.getB(compoundProperty);
                        DoubleScalar.Rel<LengthUnit> s0 = IDMPropertySet.getS0(compoundProperty);
                        DoubleScalar.Rel<TimeUnit> tSafe = IDMPropertySet.getTSafe(compoundProperty);
                        GTUFollowingModel gtuFollowingModel = null;
                        if (carFollowingModelName.equals("IDM"))
                        {
                            gtuFollowingModel = new IDM(a, b, s0, tSafe, 1.0);
                        }
                        else if (carFollowingModelName.equals("IDM+"))
                        {
                            gtuFollowingModel = new IDMPlus(a, b, s0, tSafe, 1.0);
                        }
                        else
                        {
                            throw new Error("Unknown gtu following model: " + carFollowingModelName);
                        }
                        if (ap.getShortName().contains(" Car "))
                        {
                            this.carFollowingModelCars = gtuFollowingModel;
                        }
                        else if (ap.getShortName().contains(" Truck "))
                        {
                            this.carFollowingModelTrucks = gtuFollowingModel;
                        }
                        else
                        {
                            throw new Error("Cannot determine gtu type for " + ap.getShortName());
                        }
                    }
                }
            }
            // 600 [veh / hour] on each lane results in a reasonable chance to merge
            this.headway = new DoubleScalar.Rel<TimeUnit>(3600.0 / 600.0, TimeUnit.SECOND);
            setupGenerator(LaneFactory.makeMultiLane("From to FirstVia", from, firstVia, null, merge ? lanesOnMain
                    : lanesOnCommonCompressed, laneType, this.simulator));
            Lane[] common =
                    LaneFactory.makeMultiLane("FirstVia to SecondVia", firstVia, secondVia, null, lanesOnCommon,
                            laneType, this.simulator);
            if (merge)
            {
                for (int i = lanesOnCommonCompressed; i < lanesOnCommon; i++)
                {
                    setupBlock(common[i]);
                }
            }
            setupSink(LaneFactory.makeMultiLane("SecondVia to end", secondVia, end, null, merge
                    ? lanesOnCommonCompressed : lanesOnMain, laneType, this.simulator));
            if (merge)
            {
                setupGenerator(LaneFactory.makeMultiLane("From2 to FirstVia", from2, firstVia, null, lanesOnBranch, 0,
                        lanesOnCommon - lanesOnBranch, laneType, this.simulator));
            }
            else
            {
                setupSink(LaneFactory.makeMultiLane("SecondVia to end2", secondVia, end2, null, lanesOnBranch,
                        lanesOnCommon - lanesOnBranch, 0, laneType, this.simulator));
            }
        }
        catch (NamingException | NetworkException | GTUException exception1)
        {
            exception1.printStackTrace();
        }
    }

    /**
     * Add a generator to an array of Lane.
     * @param lanes Lane[]; the lanes that must get a generator at the start
     * @return Lane[]; the lanes
     * @throws RemoteException
     * @throws SimRuntimeException
     */
    private Lane[] setupGenerator(Lane[] lanes) throws RemoteException, SimRuntimeException
    {
        for (Lane lane : lanes)
        {
            Object[] arguments = new Object[1];
            arguments[0] = lane;
            this.simulator.scheduleEventAbs(new DoubleScalar.Abs<TimeUnit>(0.0, TimeUnit.SECOND), this, this,
                    "generateCar", arguments);
        }
        return lanes;
    }

    /**
     * Append a sink to each lane of an array of Lanes.
     * @param lanes Lane[]; the array of lanes
     * @return Lane[]; the lanes
     * @throws NetworkException
     */
    private Lane[] setupSink(Lane[] lanes) throws NetworkException
    {
        CrossSectionLink<?, ?> link = lanes[0].getParentLink();
        NodeGeotools.STR to = (NodeGeotools.STR) link.getEndNode();
        NodeGeotools.STR from = (NodeGeotools.STR) link.getStartNode();
        double endLinkLength = 50; // [m]
        double endX = to.getX() + (endLinkLength / link.getLength().getSI()) * (to.getX() - from.getX());
        double endY = to.getY() + (endLinkLength / link.getLength().getSI()) * (to.getY() - from.getY());
        NodeGeotools.STR end = new NodeGeotools.STR("END", new Coordinate(endX, endY, to.getZ()));
        CrossSectionLink<?, ?> endLink = LaneFactory.makeLink("endLink", to, end, null);
        for (Lane lane : lanes)
        {
            new SinkLane(endLink, lane.getLateralCenterPosition(1.0), lane.getWidth(1.0), lane.getLaneType(),
                    LongitudinalDirectionality.FORWARD);
        }
        return lanes;
    }

    /**
     * Put a block at the end of a Lane.
     * @param lane Lane; the lane
     * @return Lane; the lane
     * @throws RemoteException
     * @throws NamingException
     * @throws NetworkException
     * @throws SimRuntimeException
     * @throws GTUException
     */
    private Lane setupBlock(Lane lane) throws RemoteException, NamingException, NetworkException, SimRuntimeException,
            GTUException
    {
        DoubleScalar.Rel<LengthUnit> initialPosition = lane.getLength();
        Map<Lane, DoubleScalar.Rel<LengthUnit>> initialPositions = new HashMap<Lane, DoubleScalar.Rel<LengthUnit>>();
        initialPositions.put(lane, initialPosition);
        GTUFollowingModel gfm =
                new FixedAccelerationModel(new DoubleScalar.Abs<AccelerationUnit>(0, AccelerationUnit.SI),
                        new DoubleScalar.Rel<TimeUnit>(java.lang.Double.MAX_VALUE, TimeUnit.SI));
        LaneChangeModel lcm = new FixedLaneChangeModel(null);
        new LaneBasedIndividualCar<Integer>(999999, null /* gtuType */, gfm, lcm, initialPositions,
                new DoubleScalar.Abs<SpeedUnit>(0, SpeedUnit.KM_PER_HOUR), new DoubleScalar.Rel<LengthUnit>(1,
                        LengthUnit.METER), lane.getWidth(1), new DoubleScalar.Abs<SpeedUnit>(0, SpeedUnit.KM_PER_HOUR),
                this.simulator);
        return lane;
    }

    /**
     * Generate cars at a fixed rate (implemented by re-scheduling this method).
     */
    protected final void generateCar(Lane lane)
    {
        boolean generateTruck = this.randomGenerator.nextDouble() > this.carProbability;
        DoubleScalar.Rel<LengthUnit> initialPosition = new DoubleScalar.Rel<LengthUnit>(0, LengthUnit.METER);
        DoubleScalar.Abs<SpeedUnit> initialSpeed = new DoubleScalar.Abs<SpeedUnit>(50, SpeedUnit.KM_PER_HOUR);
        Map<Lane, DoubleScalar.Rel<LengthUnit>> initialPositions = new HashMap<Lane, DoubleScalar.Rel<LengthUnit>>();
        initialPositions.put(lane, initialPosition);
        try
        {
            DoubleScalar.Rel<LengthUnit> vehicleLength =
                    new DoubleScalar.Rel<LengthUnit>(generateTruck ? 15 : 4, LengthUnit.METER);
            GTUFollowingModel gtuFollowingModel =
                    generateTruck ? this.carFollowingModelTrucks : this.carFollowingModelCars;
            new LaneBasedIndividualCar<Integer>(++this.carsCreated, this.gtuType, gtuFollowingModel,
                    this.laneChangeModel, initialPositions, initialSpeed, vehicleLength,
                    new DoubleScalar.Rel<LengthUnit>(1.8, LengthUnit.METER), new DoubleScalar.Abs<SpeedUnit>(200,
                            SpeedUnit.KM_PER_HOUR), this.simulator);
            Object[] arguments = new Object[1];
            arguments[0] = lane;
            this.simulator.scheduleEventRel(this.headway, this, this, "generateCar", arguments);
        }
        catch (RemoteException | SimRuntimeException | NamingException | NetworkException | GTUException exception)
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

}
