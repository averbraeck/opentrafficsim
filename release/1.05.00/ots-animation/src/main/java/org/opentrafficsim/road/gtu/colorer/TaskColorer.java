package org.opentrafficsim.road.gtu.colorer;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.opentrafficsim.core.animation.ColorInterpolator;
import org.opentrafficsim.core.animation.gtu.colorer.GTUColorer;
import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.mental.Fuller;
import org.opentrafficsim.road.gtu.lane.perception.mental.Mental;
import org.opentrafficsim.road.gtu.lane.perception.mental.Task;

/**
 * Colorer for task demand with anticipation reliance indicated by interpolating towards white, for a specific task.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 30 jan. 2019 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class TaskColorer implements GTUColorer
{

    /** Full. */
    static final Color HIGH = Color.RED;

    /** Medium. */
    static final Color MID = Color.YELLOW;

    /** Zero. */
    static final Color LOW = Color.GREEN;

    /** Fully compensated by anticipation reliance. */
    static final Color AR = Color.WHITE;

    /** Not available. */
    static final Color NA = Color.BLUE;

    /** Legend. */
    static final List<LegendEntry> LEGEND;

    static
    {
        LEGEND = new ArrayList<>();
        LEGEND.add(new LegendEntry(LOW, "low", "low task load"));
        LEGEND.add(new LegendEntry(MID, "medium", "medium task load"));
        LEGEND.add(new LegendEntry(HIGH, "high", "high task load"));
        LEGEND.add(new LegendEntry(AR, "fully anticipation reliant", "task load compensated with anticipation reliance"));
        LEGEND.add(new LegendEntry(NA, "N/A", "N/A"));
    }

    /** Id. */
    final String id;

    /**
     * Constructor.
     * @param id String; id
     */
    public TaskColorer(final String id)
    {
        this.id = id;
    }

    /** {@inheritDoc} */
    @Override
    public Color getColor(final GTU gtu)
    {
        if (gtu.getTacticalPlanner().getPerception() instanceof LanePerception)
        {
            Mental mental = ((LanePerception) gtu.getTacticalPlanner().getPerception()).getMental();
            if (mental != null && mental instanceof Fuller)
            {
                for (Task task : ((Fuller) mental).getTasks())
                {
                    if (task.getId().equals(this.id))
                    {
                        double taskDemand = task.getTaskDemand();
                        if (taskDemand == 0.0)
                        {
                            return LOW;
                        }
                        double anticipationReliance = task.getAnticipationReliance();
                        Color base = taskDemand < 0.5 ? ColorInterpolator.interpolateColor(LOW, MID, taskDemand / 0.5)
                                : ColorInterpolator.interpolateColor(MID, HIGH, (taskDemand - 0.5) / 0.5);
                        return ColorInterpolator.interpolateColor(base, AR, anticipationReliance / taskDemand);
                    }
                }
            }
        }
        return NA;
    }

    /** {@inheritDoc} */
    @Override
    public List<LegendEntry> getLegend()
    {
        return LEGEND;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "Task load (" + this.id + ")";
    }

}
