package org.opentrafficsim.trafficcontrol.trafcod;

import java.awt.BorderLayout;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;
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
import org.opentrafficsim.base.modelproperties.Property;
import org.opentrafficsim.base.modelproperties.PropertyException;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.animation.GTUColorer;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.core.network.OTSNode;
import org.opentrafficsim.road.network.factory.LaneFactory;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneType;
import org.opentrafficsim.road.network.lane.object.sensor.TrafficLightSensor;
import org.opentrafficsim.road.network.lane.object.trafficlight.SimpleTrafficLight;
import org.opentrafficsim.road.network.lane.object.trafficlight.TrafficLight;
import org.opentrafficsim.simulationengine.AbstractWrappableAnimation;
import org.opentrafficsim.simulationengine.OTSSimulationException;
import org.opentrafficsim.simulationengine.SimpleSimulatorInterface;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Nov 18, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class TrafCODDemo extends AbstractWrappableAnimation
{

    /** */
    private static final long serialVersionUID = 20161118L;

    /**
     * Main program.
     * @param args String[]; the command line arguments (not used)
     * @throws SimRuntimeException should never happen
     */
    public static void main(final String[] args) throws SimRuntimeException
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    TrafCODDemo model = new TrafCODDemo();
                    // 1 hour simulation run for testing
                    model.buildAnimator(new Time(0.0, TimeUnit.SECOND), new Duration(0.0, TimeUnit.SECOND), new Duration(60.0,
                            TimeUnit.MINUTE), new ArrayList<Property<?>>(), null, true);
                }
                catch (SimRuntimeException | NamingException | OTSSimulationException | PropertyException exception)
                {
                    exception.printStackTrace();
                }
            }
        });
    }

    /** TrafCOD controller display. */
    JPanel controllerDisplayPanel = new JPanel(new BorderLayout());

    /** {@inheritDoc} */
    @Override
    public String shortName()
    {
        return "TrafCOD demonstration";
    }

    /** {@inheritDoc} */
    @Override
    public String description()
    {
        return "TrafCOD demonstration";
    }

    /** {@inheritDoc} */
    @Override
    protected JPanel makeCharts(SimpleSimulatorInterface simulator) throws OTSSimulationException, PropertyException
    {
        JScrollPane scrollPane = new JScrollPane(this.controllerDisplayPanel);
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(scrollPane);
        return wrapper;
    }

    /** {@inheritDoc} */
    @Override
    protected OTSModelInterface makeModel(GTUColorer colorer) throws OTSSimulationException
    {
        return new TrafCODModel();
    }

    /** {@inheritDoc} */
    @Override
    protected Double makeAnimationRectangle()
    {
        return new Rectangle2D.Double(-110, -110, 220, 220);
    }

    /**
     * The simulation model.
     */
    class TrafCODModel implements OTSModelInterface
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
                Speed speedLimit = new Speed(50, SpeedUnit.KM_PER_HOUR);
                Set<GTUType> compatibility = new HashSet<GTUType>();
                compatibility.add(GTUType.ALL);
                LaneType laneType = new LaneType("CarLane", compatibility);
                Lane laneNX =
                        LaneFactory.makeMultiLane(network, "LinkNX", nodeN, nodeX, null, 1, laneType, speedLimit,
                                (OTSDEVSSimulatorInterface) theSimulator, LongitudinalDirectionality.DIR_PLUS)[0];
                Lane laneWX =
                        LaneFactory.makeMultiLane(network, "LinkWX", nodeW, nodeX, null, 1, laneType, speedLimit,
                                (OTSDEVSSimulatorInterface) theSimulator, LongitudinalDirectionality.DIR_PLUS)[0];
                LaneFactory.makeMultiLane(network, "LinkXE", nodeX, nodeE, null, 1, laneType, speedLimit,
                        (OTSDEVSSimulatorInterface) theSimulator, LongitudinalDirectionality.DIR_PLUS);
                LaneFactory.makeMultiLane(network, "LinkXS", nodeX, nodeS, null, 1, laneType, speedLimit,
                        (OTSDEVSSimulatorInterface) theSimulator, LongitudinalDirectionality.DIR_PLUS);
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
                        new TrafCOD("Simple TrafCOD controller", "file:///d:/cppb/trafcod/otsim/simpleTest.tfc", trafficLights,
                                sensors, (DEVSSimulator<Time, Duration, OTSSimTimeDouble>) theSimulator,
                                TrafCODDemo.this.controllerDisplayPanel);

                // this.trafCOD.traceVariablesOfStream(TrafCOD.NO_STREAM, true);
                // this.trafCOD.traceVariablesOfStream(11, true);
                // this.trafCOD.traceVariable("MRV", 11, true);
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

    }

}
