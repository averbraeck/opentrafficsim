package org.opentrafficsim.road.gtu.lane.perception.mental;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.structure.NavigatingIterable.Entry;
import org.opentrafficsim.road.network.lane.object.Distraction;

/**
 * Task-demand for road-side distraction.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class TaskRoadSideDistraction extends AbstractTask
{

    /** Odometer values at distraction. */
    private Map<Distraction, Double> odos = new LinkedHashMap<>();

    /** Constructor. */
    public TaskRoadSideDistraction()
    {
        super("road-side distraction");
    }

    @Override
    public double calculateTaskDemand(final LanePerception perception, final LaneBasedGtu gtu, final Parameters parameters)
            throws ParameterException, GtuException
    {
        double odo = gtu.getOdometer().si;

        for (RelativeLane lane : perception.getLaneStructure().getRootCrossSection())
        {
            for (Entry<Distraction> distraction : perception.getLaneStructure().getDownstreamObjects(lane, Distraction.class,
                    RelativePosition.FRONT, false))
            {
                this.odos.put(distraction.object(), odo + distraction.distance().si);
            }
        }

        // loop over all distractions in odos
        Iterator<Distraction> it = this.odos.keySet().iterator();
        double demand = 0.0;
        while (it.hasNext())
        {
            Distraction next = it.next();
            Double distraction = next.getDistraction(Length.instantiateSI(odo - this.odos.get(next)));
            if (distraction == null)
            {
                it.remove();
            }
            else
            {
                demand += distraction;
            }
        }
        return demand;
    }

}
