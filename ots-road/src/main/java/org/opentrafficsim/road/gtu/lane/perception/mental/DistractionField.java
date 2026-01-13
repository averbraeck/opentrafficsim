package org.opentrafficsim.road.gtu.lane.perception.mental;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.structure.LaneStructure;
import org.opentrafficsim.road.gtu.lane.perception.structure.NavigatingIterable.Entry;
import org.opentrafficsim.road.network.lane.object.RoadSideDistraction;

/**
 * This class perceives all distractions. It stores information on lane, odometer and task demand per distraction. For
 * distraction objects that are behind, the information on odometer allows active distraction for some distance beyond the
 * distraction object. This class listens to lane changes to update the relevant lane information. An instance of this class
 * should be shared among different instances of tasks which each can request the total level of distraction given the filter
 * each supplies to the {@link #getDistraction(BiFunction)} method.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class DistractionField implements EventListener
{

    /** GTU. */
    private final LaneBasedGtu gtu;

    /** Last time distractions were updated. */
    private Duration updateTime = null;

    /** Odometer values at distraction. */
    private final Map<RoadSideDistraction, Double> odos = new LinkedHashMap<>();

    /** Task demand per distraction. */
    private final Map<RoadSideDistraction, Double> taskDemands = new LinkedHashMap<>();

    /** Lanes and the applicable distractions. */
    private Map<RelativeLane, Set<RoadSideDistraction>> lanes = new LinkedHashMap<>();

    /**
     * Constructor.
     * @param gtu GTU
     */
    public DistractionField(final LaneBasedGtu gtu)
    {
        this.gtu = gtu;
        gtu.addListener(this, LaneBasedGtu.LANE_CHANGE_EVENT);
    }

    @Override
    public void notify(final Event event)
    {
        LateralDirectionality dir = LateralDirectionality.valueOf((String) ((Object[]) event.getContent())[1]);
        RelativeLane shift = new RelativeLane(dir, 1);
        Map<RelativeLane, Set<RoadSideDistraction>> newMap = new LinkedHashMap<>();
        this.lanes.entrySet().stream().forEach((e) -> newMap.put(e.getKey().add(shift), e.getValue()));
        this.lanes = newMap;
    }

    /**
     * Returns the level of distraction for the given direction.
     * @param filter filter to retain relevant distractions
     * @return level of distraction for the given direction
     * @throws ParameterException if parameter for lane structure is missing
     */
    public double getDistraction(final BiFunction<RelativeLane, RoadSideDistraction, Boolean> filter) throws ParameterException
    {
        perceiveDistractions();
        double taskDemand = 0.0;
        for (RelativeLane lane : this.lanes.keySet())
        {
            for (RoadSideDistraction distraction : this.lanes.get(lane))
            {
                if (filter.apply(lane, distraction))
                {
                    taskDemand += this.taskDemands.get(distraction);
                }
            }
        }
        return taskDemand;
    }

    /**
     * Caches odometer, lane and task demand for all distractions. Distractions are remembered when passed by their odometer.
     * Once the distraction results in a no-value task demand, it is removed as it is too far behind.
     * @throws ParameterException if parameter for lane structure is missing
     */
    private void perceiveDistractions() throws ParameterException
    {
        if (this.updateTime != null && this.gtu.getSimulator().getSimulatorTime().le(this.updateTime))
        {
            return;
        }
        this.updateTime = this.gtu.getSimulator().getSimulatorTime();

        // update odometer values and set lanes of all downstream distractions
        LaneStructure laneStructure = this.gtu.getTacticalPlanner().getPerception().getLaneStructure();
        double odo = this.gtu.getOdometer().si;
        for (RelativeLane lane : laneStructure.getRootCrossSection())
        {
            for (Entry<RoadSideDistraction> distraction : laneStructure.getDownstreamObjects(lane, RoadSideDistraction.class,
                    RelativePosition.FRONT, false))
            {
                this.odos.put(distraction.object(), odo + distraction.distance().si);
                this.lanes.computeIfAbsent(lane, (l) -> new LinkedHashSet<>()).add(distraction.object());
            }
        }

        // calculate distraction task demand and remove those without a value (indicating it is to far behind)
        Iterator<RoadSideDistraction> distractionIterator = this.odos.keySet().iterator();
        while (distractionIterator.hasNext())
        {
            RoadSideDistraction distraction = distractionIterator.next();
            Double td = distraction.getDistraction(Length.ofSI(odo - this.odos.get(distraction)));
            if (td == null)
            {
                distractionIterator.remove();
                this.taskDemands.remove(distraction);
                this.lanes.values().stream().forEach((s) -> s.remove(distraction));
            }
            else
            {
                this.taskDemands.put(distraction, td);
            }
        }

        // remove all lanes that no longer have any distraction
        Iterator<Set<RoadSideDistraction>> laneIterator = this.lanes.values().iterator();
        while (laneIterator.hasNext())
        {
            if (laneIterator.next().isEmpty())
            {
                laneIterator.remove();
            }
        }
    }

}
