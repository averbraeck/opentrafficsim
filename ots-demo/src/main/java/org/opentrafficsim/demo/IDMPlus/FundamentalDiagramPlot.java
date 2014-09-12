package org.opentrafficsim.demo.IDMPlus;

import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JOptionPane;

import org.opentrafficsim.car.Car;
import org.opentrafficsim.car.following.CarFollowingModel;
import org.opentrafficsim.car.following.CarFollowingModel.CarFollowingModelResult;
import org.opentrafficsim.car.following.IDMPlus;
import org.opentrafficsim.core.dsol.OTSDEVSSimulator;
import org.opentrafficsim.core.location.Line;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.graphs.FundamentalDiagram;

/**
 * Simulate a single lane road of 5 km length. A traffic detector is located at 3.5 km. This detector counts passing
 * vehicles and measures their speeds. These values are accumulated per minute. Vehicles are generated at a constant
 * rate of 1500 veh/hour. At time 300s a blockade is inserted at position 4 km; this blockade is removed at time 500s.
 * The used car following algorithm is IDM+ <a
 * href="http://opentrafficsim.org/downloads/MOTUS%20reference.pdf"><i>Integrated Lane Change Model with Relaxation and
 * Synchronization</i>, by Wouter J. Schakel, Victor L. Knoop and Bart van Arem, 2012</a>. <br />
 * Output is a graph showing a point for each minute of measured values. Above each point is shown the time at the end
 * of the measurement interval. The points are connected by lines. <br />
 * Right click in the graph and select another layout (under <cite>Set layout</cite> to change the quantity graphed
 * along each axis.
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
public class FundamentalDiagramPlot
{
    /**
     * Main for stand alone running.
     * @param args String[]; the program arguments (not used)
     */
    public static void main(final String[] args)
    {
        // TODO: a constant flow encountering a temporary blockage is not the way to obtain a nice Fundamental Diagram.
        // Instead a bottleneck is needed an gradually increasing flow followed by gradually decreasing flow.
        // If you fix this; make sure to fix the description of this demo accordingly
        JOptionPane.showMessageDialog(null, "FundamentalDiagramPlot", "Start experiment",
                JOptionPane.INFORMATION_MESSAGE);
        // DoubleScalarAbs<LengthUnit> minimumDistance = new DoubleScalarAbs<LengthUnit>(0, LengthUnit.METER);
        DoubleScalar.Abs<LengthUnit> maximumDistance = new DoubleScalar.Abs<LengthUnit>(5000, LengthUnit.METER);
        DoubleScalar.Abs<LengthUnit> detectorLocation = new DoubleScalar.Abs<LengthUnit>(3500, LengthUnit.METER);
        FundamentalDiagram fd =
                new FundamentalDiagram("Fundamental Diagram at " + detectorLocation.getValueSI() + "m", 1,
                        new DoubleScalar.Rel<TimeUnit>(1, TimeUnit.MINUTE), detectorLocation);
        fd.setTitle("Fundamental Diagram Graph");
        fd.setBounds(0, 0, 600, 400);
        fd.pack();
        fd.setVisible(true);
        OTSDEVSSimulator simulator = new OTSDEVSSimulator();
        CarFollowingModel carFollowingModel = new IDMPlus<Line<String>>();
        DoubleScalar.Abs<LengthUnit> initialPosition = new DoubleScalar.Abs<LengthUnit>(0, LengthUnit.METER);
        DoubleScalar.Rel<SpeedUnit> initialSpeed = new DoubleScalar.Rel<SpeedUnit>(100, SpeedUnit.KM_PER_HOUR);
        DoubleScalar.Abs<SpeedUnit> speedLimit = new DoubleScalar.Abs<SpeedUnit>(100, SpeedUnit.KM_PER_HOUR);
        final double endTime = 1800; // [s]
        final double headway = 3600.0 / 1500.0; // 1500 [veh / hour] == 2.4s headway
        double thisTick = 0;
        int carsCreated = 0;
        ArrayList<Car> cars = new ArrayList<Car>();
        double nextSourceTick = 0;
        double nextMoveTick = 0;
        double idmPlusTick = 0.5;
        while (thisTick < endTime)
        {
            // System.out.println("thisTick is " + thisTick);
            if (thisTick == nextSourceTick)
            {
                // Time to generate another car
                DoubleScalar.Abs<TimeUnit> initialTime = new DoubleScalar.Abs<TimeUnit>(thisTick, TimeUnit.SECOND);
                Car car =
                        new Car(++carsCreated, simulator, carFollowingModel, initialTime, initialPosition, initialSpeed);
                cars.add(0, car);
                // System.out.println(String.format("thisTick=%.1f, there are now %d vehicles", thisTick, cars.size()));
                nextSourceTick += headway;
            }
            if (thisTick == nextMoveTick)
            {
                // Time to move all vehicles forward (this works even though they do not have simultaneous clock ticks)
                // Debugging
                /*-
                if (thisTick == 700)
                {
                    DoubleScalarAbs<TimeUnit> now = new DoubleScalarAbs<TimeUnit>(thisTick, TimeUnit.SECOND);
                    for (int i = 0; i < cars.size(); i++)
                        System.out.println(cars.get(i).toString(now));
                }
                 */
                /*
                 * TODO: Currently all cars have to be moved "manually". This functionality should go to the simulator.
                 */
                for (int carIndex = 0; carIndex < cars.size(); carIndex++)
                {
                    DoubleScalar.Abs<TimeUnit> now = new DoubleScalar.Abs<TimeUnit>(thisTick, TimeUnit.SECOND);
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
                                new Car(99999, simulator, carFollowingModel, now, new DoubleScalar.Abs<LengthUnit>(4000,
                                        LengthUnit.METER), new DoubleScalar.Rel<SpeedUnit>(0, SpeedUnit.KM_PER_HOUR));
                        leaders.add(block);
                    }
                    CarFollowingModelResult cfmr = carFollowingModel.computeAcceleration(car, leaders, speedLimit);
                    car.setState(cfmr);
                    DoubleScalar.Abs<TimeUnit> lowerBound = car.getLastEvaluationTime();
                    DoubleScalar.Abs<TimeUnit> upperBound = car.getNextEvaluationTime();
                    if (car.getPosition(lowerBound).getValueSI() <= detectorLocation.getValueSI()
                            && car.getPosition(upperBound).getValueSI() > detectorLocation.getValueSI())
                    {
                        // This car passes the detector; add the movement of this Car to the fundamental diagram plot
                        // Figure out at what time the car passes the detector.
                        // For this demo we use bisection to converge to the correct time.
                        final double maximumTimeError = 0.01; // [s]
                        DoubleScalar.Abs<TimeUnit> passingTime = lowerBound;
                        while (upperBound.getValueSI() - lowerBound.getValueSI() > maximumTimeError)
                        {
                            passingTime =
                                    new DoubleScalar.Abs<TimeUnit>(
                                            (lowerBound.getValueSI() + upperBound.getValueSI()) / 2, TimeUnit.SECOND);
                            DoubleScalar.Abs<LengthUnit> position = car.getPosition(passingTime);
                            if (position.getValueSI() > detectorLocation.getValueSI())
                                lowerBound = passingTime;
                            else
                                upperBound = passingTime;
                        }
                        fd.addData(0, car, passingTime);
                    }
                }
                nextMoveTick += idmPlusTick;
            }
            thisTick = Math.min(nextSourceTick, nextMoveTick);
        }
        // Notify the trajectory plot that the underlying data has changed
        fd.reGraph();
    }

}
