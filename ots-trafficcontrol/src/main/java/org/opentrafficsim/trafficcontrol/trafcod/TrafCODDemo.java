package org.opentrafficsim.trafficcontrol.trafcod;

import java.awt.BorderLayout;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.DEVSSimulator;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.core.network.OTSNode;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneType;
import org.opentrafficsim.road.network.lane.changing.LaneKeepingPolicy;
import org.opentrafficsim.road.network.lane.changing.OvertakingConditions;
import org.opentrafficsim.road.network.lane.object.sensor.TrafficLightSensor;
import org.opentrafficsim.road.network.lane.object.trafficlight.SimpleTrafficLight;
import org.opentrafficsim.road.network.lane.object.trafficlight.TrafficLight;
import org.opentrafficsim.simulationengine.SimpleSimulator;
import org.opentrafficsim.trafficcontrol.TrafficControlException;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Nov 16, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class TrafCODDemo
{
    /** The simulator. */
    volatile SimpleSimulator testSimulator;

    /**
     * Entry point.
     * @param args String[]; the command line arguments (not used)
     * @throws InvocationTargetException
     * @throws InterruptedException
     */
    public static void main(final String[] args) throws InvocationTargetException, InterruptedException
    {
        SwingUtilities.invokeLater(new Runnable()
        {

            @Override
            public void run()
            {
                try
                {
                    new TrafCODDemo();
                }
                catch (TrafficControlException | SimRuntimeException | NamingException | InterruptedException exception)
                {
                    exception.printStackTrace();
                }
            }
        });
    }

    /**
     * Test code.
     * @throws TrafficControlException when network cannot be created
     * @throws NamingException when a name clash is detected
     * @throws SimRuntimeException when the simulator does not like what we want it to do
     * @throws InterruptedException unlikely to happen
     */
    public TrafCODDemo() throws TrafficControlException, SimRuntimeException, NamingException, InterruptedException
    {
        JFrame frame = new JFrame("TrafCOD demonstration");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 800);
        JPanel mainPanel = new JPanel(new BorderLayout());
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        frame.add(scrollPane);
        frame.setVisible(true);

        OTSModelInterface model = new OTSModelInterface()
        {
            /** */
            private static final long serialVersionUID = 20161020L;

            /** The TrafCOD evaluator. */
            private TrafCOD trafCOD;

            @Override
            public void constructModel(SimulatorInterface<Time, Duration, OTSSimTimeDouble> theSimulator)
                    throws SimRuntimeException, RemoteException
            {
                try
                {
                    Network network = new OTSNetwork("TrafCOD test network");
                    Map<GTUType, LongitudinalDirectionality> directionalityMap = new HashMap<>();
                    directionalityMap.put(GTUType.ALL, LongitudinalDirectionality.DIR_PLUS);
                    OTSNode nodeX = new OTSNode(network, "Crossing", new OTSPoint3D(0, 0, 0));
                    OTSNode nodeS = new OTSNode(network, "South", new OTSPoint3D(0, -100, 0));
                    OTSNode nodeE = new OTSNode(network, "East", new OTSPoint3D(100, 0, 0));
                    OTSNode nodeN = new OTSNode(network, "North", new OTSPoint3D(0, 100, 0));
                    OTSNode nodeW = new OTSNode(network, "West", new OTSPoint3D(-100, 0, 0));
                    CrossSectionLink linkNX =
                            new CrossSectionLink(network, "LinkNX", nodeN, nodeX, LinkType.ALL, new OTSLine3D(nodeN.getPoint(),
                                    nodeX.getPoint()), directionalityMap, LaneKeepingPolicy.KEEP_RIGHT);
                    CrossSectionLink linkXS =
                            new CrossSectionLink(network, "LinkXS", nodeX, nodeS, LinkType.ALL, new OTSLine3D(nodeX.getPoint(),
                                    nodeS.getPoint()), directionalityMap, LaneKeepingPolicy.KEEP_RIGHT);
                    CrossSectionLink linkWX =
                            new CrossSectionLink(network, "LinkWX", nodeW, nodeX, LinkType.ALL, new OTSLine3D(nodeW.getPoint(),
                                    nodeX.getPoint()), directionalityMap, LaneKeepingPolicy.KEEP_RIGHT);
                    CrossSectionLink linkXE =
                            new CrossSectionLink(network, "LinkXE", nodeX, nodeE, LinkType.ALL, new OTSLine3D(nodeX.getPoint(),
                                    nodeE.getPoint()), directionalityMap, LaneKeepingPolicy.KEEP_RIGHT);
                    Length laneWidth = new Length(3, LengthUnit.METER);
                    Speed speedLimit = new Speed(50, SpeedUnit.KM_PER_HOUR);
                    Lane laneNX =
                            new Lane(linkNX, "laneNX", Length.ZERO, laneWidth, LaneType.ALL,
                                    LongitudinalDirectionality.DIR_PLUS, speedLimit, new OvertakingConditions.None());
                    Lane laneWX =
                            new Lane(linkWX, "laneWX", Length.ZERO, laneWidth, LaneType.ALL,
                                    LongitudinalDirectionality.DIR_PLUS, speedLimit, new OvertakingConditions.None());
                    new Lane(linkXS, "laneXS", Length.ZERO, laneWidth, LaneType.ALL, LongitudinalDirectionality.DIR_PLUS,
                            speedLimit, new OvertakingConditions.None());
                    new Lane(linkXE, "laneWX", Length.ZERO, laneWidth, LaneType.ALL, LongitudinalDirectionality.DIR_PLUS,
                            speedLimit, new OvertakingConditions.None());
                    Set<TrafficLight> trafficLights = new HashSet<>();
                    trafficLights.add(new SimpleTrafficLight("TL08", laneNX, new Length(90, LengthUnit.METER),
                            (OTSDEVSSimulatorInterface) theSimulator));
                    trafficLights.add(new SimpleTrafficLight("TL11", laneWX, new Length(90, LengthUnit.METER),
                            (OTSDEVSSimulatorInterface) theSimulator));
                    Set<TrafficLightSensor> sensors = new HashSet<>();
                    sensors.add(new TrafficLightSensor("D081", laneWX, new Length(86, LengthUnit.METER), laneWX, new Length(88,
                            LengthUnit.METER), null, RelativePosition.FRONT, RelativePosition.REAR,
                            (OTSDEVSSimulatorInterface) theSimulator));
                    sensors.add(new TrafficLightSensor("D082", laneWX, new Length(50, LengthUnit.METER), laneWX, new Length(70,
                            LengthUnit.METER), null, RelativePosition.FRONT, RelativePosition.REAR,
                            (OTSDEVSSimulatorInterface) theSimulator));
                    sensors.add(new TrafficLightSensor("D111", laneNX, new Length(86, LengthUnit.METER), laneNX, new Length(88,
                            LengthUnit.METER), null, RelativePosition.FRONT, RelativePosition.REAR,
                            (OTSDEVSSimulatorInterface) theSimulator));
                    sensors.add(new TrafficLightSensor("D112", laneNX, new Length(50, LengthUnit.METER), laneNX, new Length(70,
                            LengthUnit.METER), null, RelativePosition.FRONT, RelativePosition.REAR,
                            (OTSDEVSSimulatorInterface) theSimulator));
                    this.trafCOD =
                            new TrafCOD("Simple TrafCOD controller", "file:///d:/cppb/trafcod/otsim/simpleTest.tfc",
                                    trafficLights, sensors, (DEVSSimulator<Time, Duration, OTSSimTimeDouble>) theSimulator,
                                    mainPanel);
                }
                catch (Exception exception)
                {
                    exception.printStackTrace();
                }
            }

            @Override
            public SimulatorInterface<Time, Duration, OTSSimTimeDouble> getSimulator() throws RemoteException
            {
                return this.trafCOD.getSimulator();
            }

        };
        Thread simulatorThread = new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    TrafCODDemo.this.testSimulator =
                            new SimpleSimulator(Time.ZERO, Duration.ZERO, new Duration(1, TimeUnit.HOUR), model);
                    TrafCODDemo.this.testSimulator.runUpToAndIncluding(new Time(20, TimeUnit.SECOND));
                    while (TrafCODDemo.this.testSimulator.isRunning())
                    {
                        try
                        {
                            Thread.sleep(100);
                        }
                        catch (InterruptedException exception)
                        {
                            exception.printStackTrace();
                        }
                    }
                    TrafCODDemo.this.testSimulator.cleanUp();
                }
                catch (SimRuntimeException | NamingException exception)
                {
                    exception.printStackTrace();
                }
            }
        };
        simulatorThread.start();
        while (null == this.testSimulator)
        {
            System.out.println("Waiting for simulator to start up");
            Thread.sleep(100);
        }
        frame.revalidate();
        frame.repaint();
        System.out.println("Simulation running...");
    }

}
