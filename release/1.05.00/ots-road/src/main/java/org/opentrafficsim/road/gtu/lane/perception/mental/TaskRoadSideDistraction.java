package org.opentrafficsim.road.gtu.lane.perception.mental;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedSet;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.LaneStructure.Entry;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.network.lane.object.Distraction;

/**
 * Task-demand for road-side distraction.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 5 apr. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
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

    /** {@inheritDoc} */
    @Override
    public double calculateTaskDemand(final LanePerception perception, final LaneBasedGTU gtu, final Parameters parameters)
            throws ParameterException, GTUException
    {
        Map<RelativeLane, SortedSet<Entry<Distraction>>> map =
                perception.getLaneStructure().getDownstreamObjects(Distraction.class, gtu, RelativePosition.FRONT);
        // (re)put all downstream distractions in the odos map
        double odo = gtu.getOdometer().si;
        for (RelativeLane lane : map.keySet())
        {
            for (Entry<Distraction> entry : map.get(lane))
            {
                this.odos.put(entry.getLaneBasedObject(), odo + entry.getDistance().si);
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
