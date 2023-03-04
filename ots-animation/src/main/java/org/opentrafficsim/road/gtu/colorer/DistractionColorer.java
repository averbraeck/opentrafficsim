package org.opentrafficsim.road.gtu.colorer;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.opentrafficsim.core.animation.gtu.colorer.GtuColorer;
import org.opentrafficsim.core.animation.gtu.colorer.IdGtuColorer;
import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.mental.Fuller;
import org.opentrafficsim.road.gtu.lane.perception.mental.Mental;
import org.opentrafficsim.road.gtu.lane.perception.mental.Task;
import org.opentrafficsim.road.gtu.lane.perception.mental.sdm.DefaultDistraction;

/**
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class DistractionColorer implements GtuColorer, Serializable
{

    /** */
    private static final long serialVersionUID = 20181106L;

    /** None color. */
    private static final Color NONE = Color.MAGENTA.darker().darker();

    /** The legend. */
    private final ArrayList<LegendEntry> legend = new ArrayList<>();

    /** Colors. */
    private final Map<String, Color> colors = new LinkedHashMap<>();

    /**
     * Constructor.
     * @param distractions DefaultDistraction...; DefaultDistraction... distractions to color
     */
    public DistractionColorer(final DefaultDistraction... distractions)
    {
        this.legend.add(new LegendEntry(NONE, "None", "No distraction"));
        for (int i = 0; i < distractions.length; i++)
        {
            Color c = IdGtuColorer.LEGEND.get(i % 10).getColor();
            this.colors.put(distractions[i].getId(), c);
            this.legend.add(new LegendEntry(c, distractions[i].getDescription(), distractions[i].getDescription()));
        }
    }

    /** {@inheritDoc} */
    @Override
    public Color getColor(final Gtu gtu)
    {
        if (gtu.getTacticalPlanner().getPerception() instanceof LanePerception)
        {
            Mental mental = ((LanePerception) gtu.getTacticalPlanner().getPerception()).getMental();
            if (mental != null && mental instanceof Fuller)
            {
                for (Task task : ((Fuller) mental).getTasks())
                {
                    String id = task.getId();
                    if (this.colors.containsKey(id))
                    {
                        return this.colors.get(id);
                    }
                }
            }
        }
        return NONE;
    }

    /** {@inheritDoc} */
    @Override
    public final List<LegendEntry> getLegend()
    {
        return Collections.unmodifiableList(this.legend);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "Distraction";
    }

}
