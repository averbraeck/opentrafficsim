package org.opentrafficsim.demo.IDMPlus;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.swing.JOptionPane;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.experiment.Experiment;
import nl.tudelft.simulation.dsol.experiment.Replication;
import nl.tudelft.simulation.dsol.experiment.ReplicationMode;
import nl.tudelft.simulation.dsol.experiment.Treatment;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

import org.opentrafficsim.car.Car;
import org.opentrafficsim.core.dsol.OTSDEVSSimulator;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.gtu.following.GTUFollowingModel;
import org.opentrafficsim.core.gtu.following.GTUFollowingModel.GTUFollowingModelResult;
import org.opentrafficsim.core.gtu.following.IDMPlus;
import org.opentrafficsim.core.network.CrossSectionLink;
import org.opentrafficsim.core.network.Lane;
import org.opentrafficsim.core.network.LaneType;
import org.opentrafficsim.core.network.LinearGeometry;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.unit.FrequencyUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Abs;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Rel;
import org.opentrafficsim.demo.geometry.LaneFactory;
import org.opentrafficsim.demo.geometry.Node;
import org.opentrafficsim.graphs.AccelerationContourPlot;
import org.opentrafficsim.graphs.ContourPlot;
import org.opentrafficsim.graphs.DensityContourPlot;
import org.opentrafficsim.graphs.FlowContourPlot;
import org.opentrafficsim.graphs.SpeedContourPlot;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

/**
 * Simulate a single lane road of 5 km length. Vehicles are generated at a constant rate of 1500 veh/hour. At time 300s
 * a blockade is inserted at position 4 km; this blockade is removed at time 500s. The used car following algorithm is
 * IDM+ <a href="http://opentrafficsim.org/downloads/MOTUS%20reference.pdf"><i>Integrated Lane Change Model with
 * Relaxation and Synchronization</i>, by Wouter J. Schakel, Victor L. Knoop and Bart van Arem, 2012</a>. <br>
 * Output is a set of block charts:
 * <ul>
 * <li>Traffic density</li>
 * <li>Speed</li>
 * <li>Flow</li>
 * <li>Acceleration</li>
 * </ul>
 * All these graphs display simulation time along the horizontal axis and distance along the road along the vertical
 * axis.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Aug 1, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public final class ContourPlots
{
    /**
     * This class should never be instantiated.
     */
    private ContourPlots()
    {
        // Prevent instantiation of this class
    }

    /** the helper model. */
    protected static class Model implements OTSModelInterface
    {
        /** */
        private static final long serialVersionUID = 20141027L;

        /** The simulator. */
        private OTSDEVSSimulator simulator;

        /** {@inheritDoc} */
        @Override
        public final void constructModel(
                final SimulatorInterface<Abs<TimeUnit>, Rel<TimeUnit>, OTSSimTimeDouble> theSimulator)
                throws SimRuntimeException, RemoteException
        {
            this.simulator = (OTSDEVSSimulator) theSimulator;
        }

        /** {@inheritDoc} */
        @Override
        public final OTSDEVSSimulator getSimulator() throws RemoteException
        {
            return this.simulator;
        }

    }

    /**
     * Create the simplest possible simulator.
     * @return OTSDEVSSimulator
     * @throws RemoteException on network error
     * @throws SimRuntimeException on ???
     * @throws NamingException on ???
     */
    public static OTSDEVSSimulator makeSimulator() throws RemoteException, SimRuntimeException, NamingException
    {
        OTSDEVSSimulator simulator = new OTSDEVSSimulator();
        Model model = new Model();
        Context context = new InitialContext();
        Experiment<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> exp =
                new Experiment<>(context);
        Treatment<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> tr =
                new Treatment<>(exp, "tr1", new OTSSimTimeDouble(new DoubleScalar.Abs<TimeUnit>(0, TimeUnit.SECOND)),
                        new DoubleScalar.Rel<TimeUnit>(0, TimeUnit.SECOND), new DoubleScalar.Rel<TimeUnit>(3600.0,
                                TimeUnit.SECOND));
        exp.setTreatment(tr);
        exp.setModel(model);
        Replication<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> rep =
                new Replication<>(context, exp);
        simulator.initialize(rep, ReplicationMode.TERMINATING);
        return simulator;
    }

    /**
     * Main for stand alone running.
     * @param args String[]; the program arguments (not used)
     * @throws RemoteException
     * @throws NetworkException
     * @throws SimRuntimeException
     * @throws NamingException
     */
    public static void main(final String[] args) throws RemoteException, NetworkException, SimRuntimeException,
            NamingException
    {
        JOptionPane.showMessageDialog(null, "ContourPlot", "Start experiment", JOptionPane.INFORMATION_MESSAGE);
        ArrayList<ContourPlot> contourPlots = new ArrayList<ContourPlot>();
        DoubleScalar.Rel<LengthUnit> minimumDistance = new DoubleScalar.Rel<LengthUnit>(0, LengthUnit.METER);
        DoubleScalar.Rel<LengthUnit> maximumDistance = new DoubleScalar.Rel<LengthUnit>(5000, LengthUnit.METER);
        ContourPlot cp;
        int left = 200;
        int deltaLeft = 100;
        int top = 100;
        int deltaTop = 50;

        cp = new DensityContourPlot("DensityPlot", minimumDistance, maximumDistance);
        cp.setTitle("Density Contour Graph");
        cp.setBounds(left + contourPlots.size() * deltaLeft, top + contourPlots.size() * deltaTop, 600, 400);
        cp.pack();
        cp.setVisible(true);
        contourPlots.add(cp);

        cp = new SpeedContourPlot("SpeedPlot", minimumDistance, maximumDistance);
        cp.setTitle("Speed Contour Graph");
        cp.setBounds(left + contourPlots.size() * deltaLeft, top + contourPlots.size() * deltaTop, 600, 400);
        cp.pack();
        cp.setVisible(true);
        contourPlots.add(cp);

        cp = new FlowContourPlot("FlowPlot", minimumDistance, maximumDistance);
        cp.setTitle("FLow Contour Graph");
        cp.setBounds(left + contourPlots.size() * deltaLeft, top + contourPlots.size() * deltaTop, 600, 400);
        cp.pack();
        cp.setVisible(true);
        contourPlots.add(cp);

        cp = new AccelerationContourPlot("AccelerationPlot", minimumDistance, maximumDistance);
        cp.setTitle("Acceleration Contour Graph");
        cp.setBounds(left + contourPlots.size() * deltaLeft, top + contourPlots.size() * deltaTop, 600, 400);
        cp.pack();
        cp.setVisible(true);
        contourPlots.add(cp);

        OTSDEVSSimulator simulator = makeSimulator();
        GTUFollowingModel carFollowingModel = new IDMPlus(simulator);
        DoubleScalar.Rel<LengthUnit> initialPosition = new DoubleScalar.Rel<LengthUnit>(0, LengthUnit.METER);
        DoubleScalar.Rel<SpeedUnit> initialSpeed = new DoubleScalar.Rel<SpeedUnit>(100, SpeedUnit.KM_PER_HOUR);
        DoubleScalar.Abs<SpeedUnit> speedLimit = new DoubleScalar.Abs<SpeedUnit>(100, SpeedUnit.KM_PER_HOUR);
        final double endTime = 1800; // [s]
        final double headway = 3600.0 / 1500.0; // 1500 [veh / hour] == 2.4s headway
        double thisTick = 0;
        final double tick = 0.5;
        int carsCreated = 0;
        ArrayList<Car<Integer>> cars = new ArrayList<Car<Integer>>();
        double nextSourceTick = 0;
        double nextMoveTick = 0;
        Node from = new Node("From", new Coordinate(minimumDistance.getSI(), 0, 0));
        Node to = new Node("To", new Coordinate(maximumDistance.getSI(), 0, 0));
        Lane lane = LaneFactory.makeLane("Lane", from, to);
        // TODO Major rewrite needed here
        while (thisTick < endTime)
        {
            // System.out.println("thisTick is " + thisTick);
            if (thisTick == nextSourceTick)
            {
                // Time to generate another car
                DoubleScalar.Abs<TimeUnit> initialTime = new DoubleScalar.Abs<TimeUnit>(thisTick, TimeUnit.SECOND);
                // new Car<>(id, gtuType, length, width, maximumVelocity, gtuFollowingModel,
                // initialLongitudinalPositions, initialSpeed, simulator)
                DoubleScalar.Rel<LengthUnit> width = new DoubleScalar.Rel<LengthUnit>(1.7, LengthUnit.METER);
                DoubleScalar.Rel<LengthUnit> length = new DoubleScalar.Rel<LengthUnit>(4, LengthUnit.METER);
                DoubleScalar.Abs<SpeedUnit> maximumVelocity =
                        new DoubleScalar.Abs<SpeedUnit>(200, SpeedUnit.KM_PER_HOUR);
                Map<Lane, DoubleScalar.Rel<LengthUnit>> initialPositions = new HashMap<>();
                initialPositions.put(lane, initialPosition);
                System.out.println("getSimulatorTime returns " + simulator.getSimulatorTime());
                Car<Integer> car =
                        new Car<Integer>(++carsCreated, null, length, width, maximumVelocity, carFollowingModel,
                                initialPositions, speedLimit, simulator);
                cars.add(0, car);
                // System.out.println(String.format("thisTick=%.1f, there are now %d vehicles", thisTick, cars.size()));
                nextSourceTick += headway;
            }
            if (thisTick == nextMoveTick)
            {
                // Time to move all vehicles forward (this works even though they do not have simultaneous clock ticks)
                /*
                 * Debugging if (thisTick == 700) { DoubleScalarAbs<TimeUnit> now = new
                 * DoubleScalarAbs<TimeUnit>(thisTick, TimeUnit.SECOND); for (int i = 0; i < cars.size(); i++)
                 * System.out.println(cars.get(i).toString(now)); }
                 */
                /*
                 * TODO: Currently all cars have to be moved "manually". This functionality should go to the simulator.
                 */
                for (int carIndex = 0; carIndex < cars.size(); carIndex++)
                {
                    DoubleScalar.Abs<TimeUnit> now = new DoubleScalar.Abs<TimeUnit>(thisTick, TimeUnit.SECOND);
                    Car<Integer> car = cars.get(carIndex);
                    if (car.positionOfRear().getLongitudinalPosition().getSI() > maximumDistance.getSI())
                    {
                        cars.remove(carIndex);
                        break;
                    }
                    Collection<Car<Integer>> leaders = new ArrayList<Car<Integer>>();
                    if (carIndex < cars.size() - 1)
                    {
                        leaders.add(cars.get(carIndex + 1));
                    }
                    if (thisTick >= 300 && thisTick < 500)
                    {
                        // Add a stationary car at 4000m to simulate an opening bridge
                        Map<Lane, DoubleScalar.Rel<LengthUnit>> initialPositions = new HashMap<>();
                        initialPositions.put(lane, new DoubleScalar.Rel<LengthUnit>(4000, LengthUnit.METER));

                        Car<Integer> block =
                                new Car<Integer>(99999, null, new DoubleScalar.Rel<LengthUnit>(4, LengthUnit.METER),
                                        new DoubleScalar.Rel<LengthUnit>(1.7, LengthUnit.METER), null, null,
                                        initialPositions, new DoubleScalar.Abs<SpeedUnit>(0, SpeedUnit.KM_PER_HOUR),
                                        simulator);
                        leaders.add(block);
                    }
                    GTUFollowingModelResult cfmr = carFollowingModel.computeAcceleration(car, leaders, speedLimit);
                    car.setState(cfmr);
                    // Add the movement of this Car to the contour plots
                    for (ContourPlot contourPlot : contourPlots)
                    {
                        contourPlot.addData(car);
                    }
                }
                nextMoveTick += tick;
            }
            thisTick = Math.min(nextSourceTick, nextMoveTick);
        }
        // Notify the contour plots that the underlying data has changed
        for (ContourPlot contourPlot : contourPlots)
        {
            contourPlot.reGraph();
        }
    }

}
