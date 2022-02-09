package org.opentrafficsim.road.gtu.colorer;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.djunits.value.vdouble.scalar.Duration;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.core.animation.ColorInterpolator;
import org.opentrafficsim.core.animation.gtu.colorer.GTUColorer;
import org.opentrafficsim.core.gtu.GTU;

/**
 * Colors the reaction time.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 31 jan. 2019 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class ReactionTimeColorer implements GTUColorer
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
    private final List<LegendEntry> legend;

    /** Reaction time. */
    private final Duration maxReactionTime;

    /**
     * Constructor.
     * @param maxReactionTime Duration; maximum reaction time.
     */
    public ReactionTimeColorer(final Duration maxReactionTime)
    {
        this.maxReactionTime = maxReactionTime;
        this.legend = new ArrayList<>();
        this.legend.add(new LegendEntry(LOW, maxReactionTime.times(0.0).toString(), "small reaction time"));
        this.legend.add(new LegendEntry(MID, maxReactionTime.times(0.5).toString(), "medium reaction time"));
        this.legend.add(new LegendEntry(HIGH, maxReactionTime.toString(), "large reaction time"));
        this.legend.add(new LegendEntry(NA, "N/A", "N/A"));
    }

    /** {@inheritDoc} */
    @Override
    public Color getColor(final GTU drawable)
    {
        Duration tr = drawable.getParameters().getParameterOrNull(ParameterTypes.TR);
        if (tr == null)
        {
            return NA;
        }
        if (tr.si < .5 * this.maxReactionTime.si)
        {
            return ColorInterpolator.interpolateColor(LOW, MID, tr.si / (.5 * this.maxReactionTime.si));
        }
        else if (tr.si <= this.maxReactionTime.si)
        {
            return ColorInterpolator.interpolateColor(MID, HIGH,
                    (tr.si - .5 * this.maxReactionTime.si) / (.5 * this.maxReactionTime.si));
        }
        return HIGH;
    }

    /** {@inheritDoc} */
    @Override
    public List<LegendEntry> getLegend()
    {
        return this.legend;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "Reaction time";
    }

}
