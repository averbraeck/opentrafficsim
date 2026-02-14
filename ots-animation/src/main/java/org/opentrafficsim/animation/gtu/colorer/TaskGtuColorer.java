package org.opentrafficsim.animation.gtu.colorer;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.draw.ColorInterpolator;
import org.opentrafficsim.draw.colorer.LegendColorer;
import org.opentrafficsim.road.gtu.perception.LanePerception;
import org.opentrafficsim.road.gtu.perception.mental.Mental;
import org.opentrafficsim.road.gtu.perception.mental.ar.ArFuller;
import org.opentrafficsim.road.gtu.perception.mental.ar.ArTask;

/**
 * Colorer for task demand with anticipation reliance indicated by interpolating towards white, for a specific task.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class TaskGtuColorer implements LegendColorer<Gtu>
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
    private final String id;

    /**
     * Constructor.
     * @param id id of the task
     */
    public TaskGtuColorer(final String id)
    {
        this.id = id;
    }

    @Override
    public Color getColor(final Gtu gtu)
    {
        if (gtu.getTacticalPlanner().getPerception() instanceof LanePerception)
        {
            Optional<Mental> mental = ((LanePerception) gtu.getTacticalPlanner().getPerception()).getMental();
            if (mental.isPresent() && mental.get() instanceof ArFuller fuller)
            {
                for (ArTask task : fuller.getTasks())
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

    @Override
    public List<LegendEntry> getLegend()
    {
        return LEGEND;
    }

    @Override
    public String getName()
    {
        return "Task load (" + this.id + ")";
    }

}
