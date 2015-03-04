package org.opentrafficsim.demo.carFollowing;

import java.awt.geom.Rectangle2D;
import java.rmi.RemoteException;
import java.util.ArrayList;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.factory.LaneFactory;
import org.opentrafficsim.core.network.geotools.NodeGeotools;
import org.opentrafficsim.core.network.lane.CrossSectionLink;
import org.opentrafficsim.core.network.lane.Lane;
import org.opentrafficsim.core.network.lane.LaneType;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Abs;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Rel;
import org.opentrafficsim.simulationengine.AbstractProperty;
import org.opentrafficsim.simulationengine.CompoundProperty;
import org.opentrafficsim.simulationengine.ControlPanel;
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
     * Construct the XMLNetworks.
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
        try
        {
            LaneFactory.makeMultiLane("From to FirstVia", from, firstVia, null, merge ? lanesOnMain
                    : lanesOnCommonCompressed, laneType, this.simulator);
            LaneFactory.makeMultiLane("FirstVia to SecondVia", firstVia, secondVia, null, lanesOnCommon, laneType,
                    this.simulator);
            LaneFactory.makeMultiLane("SecondVia to end", secondVia, end, null, merge ? lanesOnCommonCompressed
                    : lanesOnMain, laneType, this.simulator);
            if (merge)
            {
                LaneFactory.makeMultiLane("From2 to FirstVia", from2, firstVia, null, lanesOnBranch, 0, lanesOnCommon
                        - lanesOnBranch, laneType, this.simulator);
            }
            else
            {
                LaneFactory.makeMultiLane("SecondVia to end2", secondVia, end2, null, lanesOnBranch, lanesOnCommon
                        - lanesOnBranch, 0, laneType, this.simulator);

            }
        }
        catch (NamingException | NetworkException exception1)
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
