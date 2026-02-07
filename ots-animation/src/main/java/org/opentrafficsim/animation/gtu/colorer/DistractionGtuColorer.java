package org.opentrafficsim.animation.gtu.colorer;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.draw.Colors;
import org.opentrafficsim.draw.colorer.AbstractLegendColorer;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.mental.Fuller;
import org.opentrafficsim.road.gtu.lane.perception.mental.Mental;
import org.opentrafficsim.road.gtu.lane.perception.mental.Task;
import org.opentrafficsim.road.gtu.lane.perception.mental.sdm.DefaultDistraction;

/**
 * Distraction colorer, which shows which distraction is active. This only works on GTUs with the Fuller mental model.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class DistractionGtuColorer extends AbstractLegendColorer<Gtu, String>
{

    /** None color. */
    private static final Color NONE = Color.CYAN;

    /**
     * Constructor.
     * @param distractions distractions to color
     */
    public DistractionGtuColorer(final Set<DefaultDistraction> distractions)
    {
        super((g) -> Optional.ofNullable(getDistractionId(g, asIds(distractions))), DistractionGtuColorer::getColor,
                createLegend(distractions));
    }

    /**
     * Returns the set of distractions as a set of their ids.
     * @param distractions distractions
     * @return distraction ids
     */
    private static Set<String> asIds(final Set<DefaultDistraction> distractions)
    {
        return distractions.stream().map((d) -> d.getId()).collect(Collectors.toSet());
    }

    /**
     * Value function.
     * @param gtu GTU
     * @param distraction ids of considered distractions
     * @return id of distraction, or {@code null} if there is no distraction
     */
    private static String getDistractionId(final Gtu gtu, final Set<String> distraction)
    {
        if (gtu.getTacticalPlanner().getPerception() instanceof LanePerception)
        {
            Optional<Mental> mental = ((LanePerception) gtu.getTacticalPlanner().getPerception()).getMental();
            if (mental.isPresent() && mental.get() instanceof Fuller fuller)
            {
                for (Task task : fuller.getTasks())
                {
                    if (distraction.contains(task.getId()))
                    {
                        return task.getId();
                    }
                }
            }
        }
        return null;
    }

    /**
     * Color function.
     * @param id distraction id
     * @return color for the distraction
     */
    private static Color getColor(final String id)
    {
        if (id == null)
        {
            return NONE;
        }
        return Colors.getIdColor(id, Colors.ENUMERATE);
    }

    /**
     * Creates legend.
     * @param distractions distractions
     * @return legend
     */
    private static List<LegendEntry> createLegend(final Set<DefaultDistraction> distractions)
    {
        List<LegendEntry> list = new ArrayList<>();
        list.add(new LegendEntry(NONE, "None", "None"));
        for (DefaultDistraction distraction : distractions)
        {
            list.add(new LegendEntry(getColor(distraction.getId()), distraction.getId(), distraction.getDescription()));
        }
        return list;
    }

    @Override
    public final String getName()
    {
        return "Distraction";
    }

}
