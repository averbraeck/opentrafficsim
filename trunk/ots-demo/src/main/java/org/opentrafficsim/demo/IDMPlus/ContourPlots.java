package org.opentrafficsim.demo.IDMPlus;

import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JOptionPane;

import nl.tudelft.simulation.dsol.simulators.DEVSSimulator;

import org.opentrafficsim.car.Car;
import org.opentrafficsim.car.following.CarFollowingModel;
import org.opentrafficsim.car.following.IDMPlus;
import org.opentrafficsim.car.following.CarFollowingModel.CarFollowingModelResult;
import org.opentrafficsim.core.location.Line;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalarAbs;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalarRel;
import org.opentrafficsim.graphs.AccelerationContourPlot;
import org.opentrafficsim.graphs.ContourPlot;
import org.opentrafficsim.graphs.DensityContourPlot;
import org.opentrafficsim.graphs.FlowContourPlot;
import org.opentrafficsim.graphs.SpeedContourPlot;

/**
 * Simulate a single lane road of 5 km length. Vehicles are generated at a constant rate of 1500 veh/hour. At time 300s
 * a blockade is inserted at position 4 km; this blockade is removed at time 500s. The used car following algorithm is
 * IDM+ <a href="http://opentrafficsim.org/downloads/MOTUS%20reference.pdf"><i>Integrated Lane Change Model with
 * Relaxation and Synchronization</i>, by Wouter J. Schakel, Victor L. Knoop and Bart van Arem, 2012</a>.
 * <br /> Output is a set of block charts:
 * <ul>
 * <li>Traffic density</li>
 * <li>Speed</li>
 * <li>Flow</li>
 * <li>Acceleration</li>
 * </ul>
 * All these graphs display simulation time along the horizontal axis and distance along the road along the vertical axis.
 * <p>
 * Copyright (c) 2002-2014 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights
 * reserved.
 * <p>
 * See for project information <a href="http://www.simulation.tudelft.nl/"> www.simulation.tudelft.nl</a>.
 * <p>
 * The OpenTrafficSim project is distributed under the following BSD-style license:<br>
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * <ul>
 * <li>Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer.</li>
 * <li>Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution.</li>
 * <li>Neither the name of Delft University of Technology, nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.</li>
 * </ul>
 * This software is provided by the copyright holders and contributors "as is" and any express or implied warranties,
 * including, but not limited to, the implied warranties of merchantability and fitness for a particular purpose are
 * disclaimed. In no event shall the copyright holder or contributors be liable for any direct, indirect, incidental,
 * special, exemplary, or consequential damages (including, but not limited to, procurement of substitute goods or
 * services; loss of use, data, or profits; or business interruption) however caused and on any theory of liability,
 * whether in contract, strict liability, or tort (including negligence or otherwise) arising in any way out of the use
 * of this software, even if advised of the possibility of such damage.
 * @version Aug 1, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class ContourPlots
{
    /**
     * Main for stand alone running.
     * @param args String[]; the program arguments (not used)
     */
    public static void main(final String[] args)
    {
        JOptionPane.showMessageDialog(null, "ContourPlot", "Start experiment", JOptionPane.INFORMATION_MESSAGE);
        ArrayList<ContourPlot> contourPlots = new ArrayList<ContourPlot>();
        DoubleScalarAbs<LengthUnit> minimumDistance = new DoubleScalarAbs<LengthUnit>(0, LengthUnit.METER);
        DoubleScalarAbs<LengthUnit> maximumDistance = new DoubleScalarAbs<LengthUnit>(5000, LengthUnit.METER);
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

        DEVSSimulator simulator = new DEVSSimulator();
        CarFollowingModel carFollowingModel = new IDMPlus<Line<String>>();
        DoubleScalarAbs<LengthUnit> initialPosition = new DoubleScalarAbs<LengthUnit>(0, LengthUnit.METER);
        DoubleScalarRel<SpeedUnit> initialSpeed = new DoubleScalarRel<SpeedUnit>(100, SpeedUnit.KM_PER_HOUR);
        DoubleScalarAbs<SpeedUnit> speedLimit = new DoubleScalarAbs<SpeedUnit>(100, SpeedUnit.KM_PER_HOUR);
        final double endTime = 1800; // [s]
        final double headway = 3600.0 / 1500.0; // 1500 [veh / hour] == 2.4s headway
        double thisTick = 0;
        final double tick = 0.5;
        int carsCreated = 0;
        ArrayList<Car> cars = new ArrayList<Car>();
        double nextSourceTick = 0;
        double nextMoveTick = 0;
        while (thisTick < endTime)
        {
            // System.out.println("thisTick is " + thisTick);
            if (thisTick == nextSourceTick)
            {
                // Time to generate another car
                DoubleScalarAbs<TimeUnit> initialTime = new DoubleScalarAbs<TimeUnit>(thisTick, TimeUnit.SECOND);
                Car car =
                        new Car(++carsCreated, simulator, carFollowingModel, initialTime, initialPosition, initialSpeed);
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
                    DoubleScalarAbs<TimeUnit> now = new DoubleScalarAbs<TimeUnit>(thisTick, TimeUnit.SECOND);
                    Car car = cars.get(carIndex);
                    if (car.getPosition(now).getValueSI() > maximumDistance.getValueSI())
                    {
                        cars.remove(carIndex);
                        break;
                    }
                    Collection<Car> leaders = new ArrayList<Car>();
                    if (carIndex < cars.size() - 1)
                        leaders.add(cars.get(carIndex + 1));
                    if (thisTick >= 300 && thisTick < 500)
                    {
                        // Add a stationary car at 4000m to simulate an opening bridge
                        Car block =
                                new Car(99999, simulator, carFollowingModel, now, new DoubleScalarAbs<LengthUnit>(4000,
                                        LengthUnit.METER), new DoubleScalarRel<SpeedUnit>(0, SpeedUnit.KM_PER_HOUR));
                        leaders.add(block);
                    }
                    CarFollowingModelResult cfmr = carFollowingModel.computeAcceleration(car, leaders, speedLimit);
                    car.setState(cfmr);
                    // Add the movement of this Car to the contour plots
                    for (ContourPlot contourPlot : contourPlots)
                        contourPlot.addData(car);
                }
                nextMoveTick += tick;
            }
            thisTick = Math.min(nextSourceTick, nextMoveTick);
        }
        // Notify the contour plots that the underlying data has changed
        for (ContourPlot contourPlot : contourPlots)
            contourPlot.reGraph();
    }

}
