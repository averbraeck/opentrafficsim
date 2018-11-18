package org.opentrafficsim.demo.timing;

import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;

import org.djunits.unit.UNITS;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.base.modelproperties.CompoundProperty;
import org.opentrafficsim.base.modelproperties.ProbabilityDistributionProperty;
import org.opentrafficsim.base.modelproperties.Property;
import org.opentrafficsim.base.modelproperties.PropertyException;
import org.opentrafficsim.base.modelproperties.SelectionProperty;
import org.opentrafficsim.core.dsol.OTSSimulationException;
import org.opentrafficsim.core.gtu.AbstractGTU;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.demo.carFollowing.CircularRoad;
import org.opentrafficsim.road.gtu.lane.AbstractLaneBasedGTU;
import org.opentrafficsim.road.modelproperties.IDMPropertySet;
import org.opentrafficsim.simulationengine.SimpleAnimator;
import org.opentrafficsim.simulationengine.WrappableAnimation;

import nl.tudelft.simulation.dsol.SimRuntimeException;

/**
 * <p>
 * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Apr 14, 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class TimeCircularRoadAnimation implements UNITS
{
    /** */
    public TimeCircularRoadAnimation()
    {
        try
        {
            WrappableAnimation simulation = new CircularRoad();
            List<Property<?>> activeProperties = new ArrayList<>();
            activeProperties.addAll(simulation.getProperties());
            for (Property<?> ap : activeProperties)
            {
                if (ap instanceof SelectionProperty)
                {
                    SelectionProperty sp = (SelectionProperty) ap;
                    if ("TacticalPlanner".equals(sp.getKey()))
                    {
                        sp.setValue("DIRECTED/IDM");
                    }
                }
            }
            activeProperties.add(new ProbabilityDistributionProperty("TrafficComposition", "Traffic composition",
                    "<html>Mix of passenger cars and trucks</html>", new String[] { "passenger car", "truck" },
                    new Double[] { 0.8, 0.2 }, false, 5));
            CompoundProperty modelSelection =
                    new CompoundProperty("ModelSelection", "Model selection", "Modeling specific settings", null, false, 300);
            modelSelection.add(new SelectionProperty("SimulationScale", "Simulation scale", "Level of detail of the simulation",
                    new String[] { "Micro", "Macro", "Meta" }, 0, true, 0));
            modelSelection.add(new SelectionProperty("CarFollowingModel", "Car following model", "",
                    new String[] { "IDM", "IDM+" }, 1, false, 1));
            modelSelection.add(IDMPropertySet.makeIDMPropertySet("IDMCar", "Car", new Acceleration(1.56, METER_PER_SECOND_2),
                    new Acceleration(2.09, METER_PER_SECOND_2), new Length(3.0, METER), new Duration(1.2, SECOND), 2));
            modelSelection
                    .add(IDMPropertySet.makeIDMPropertySet("IDMTruck", "Truck", new Acceleration(0.75, METER_PER_SECOND_2),
                            new Acceleration(1.25, METER_PER_SECOND_2), new Length(3.0, METER), new Duration(1.2, SECOND), 3));
            activeProperties.add(modelSelection);
            SimpleAnimator sim = (SimpleAnimator) simulation.buildAnimator(Time.ZERO, Duration.ZERO,
                    new Duration(3600.0, SECOND), activeProperties, null, false);

            sim.setSpeedFactor(1.0E9);

            Thread.sleep(5000);

            sim.scheduleEventRel(new Duration(59.999, MINUTE), this, this, "stop", new Object[] { System.currentTimeMillis() });
            sim.start();
        }
        catch (SimRuntimeException | PropertyException | NetworkException | NamingException | OTSSimulationException
                | InterruptedException exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * Timer.
     * @param startTime long; start time of simulation
     */
    protected void stop(final long startTime)
    {
        System.out.println("Time of animation was : " + (System.currentTimeMillis() - startTime) / 1000 + " seconds");
        System.out.println("Aligned GTU moves     = " + AbstractGTU.ALIGN_COUNT);
        System.out.println("Cached positions      = " + AbstractLaneBasedGTU.CACHED_POSITION);
        System.out.println("Non-cached positions  = " + AbstractLaneBasedGTU.NON_CACHED_POSITION);
        System.exit(0);
    }

    /**
     * @param args String[]; args should be empty
     */
    public static void main(final String[] args)
    {
        AbstractGTU.ALIGNED = true;
        AbstractLaneBasedGTU.CACHING = true;

        new TimeCircularRoadAnimation();
    }

}
