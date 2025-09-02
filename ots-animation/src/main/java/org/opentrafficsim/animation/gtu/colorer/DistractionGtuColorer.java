package org.opentrafficsim.animation.gtu.colorer;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class DistractionGtuColorer extends AbstractLegendColorer<Gtu, String> implements Serializable
{

    /** */
    private static final long serialVersionUID = 20181106L;

    /** None color. */
    private static final Color NONE = Color.CYAN;

    /**
     * Constructor.
     * @param distractions DefaultDistraction... distractions to color
     */
    public DistractionGtuColorer(final DefaultDistraction... distractions)
    {
        super(DistractionGtuColorer::getDistractionId, DistractionGtuColorer::getColor, createLegend(distractions));
    }

    /**
     * Value function.
     * @param gtu GTU
     * @return id of distraction, or {@code null} if there is no distraction
     */
    private static String getDistractionId(final Gtu gtu)
    {
        if (gtu.getTacticalPlanner().getPerception() instanceof LanePerception)
        {
            Mental mental = ((LanePerception) gtu.getTacticalPlanner().getPerception()).getMental();
            if (mental != null && mental instanceof Fuller)
            {
                for (Task task : ((Fuller) mental).getTasks())
                {
                    return task.getId();
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
    private static List<LegendEntry> createLegend(final DefaultDistraction... distractions)
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
