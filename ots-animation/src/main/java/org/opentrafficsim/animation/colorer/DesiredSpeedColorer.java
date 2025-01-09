package org.opentrafficsim.animation.colorer;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.animation.gtu.colorer.GtuColorer;
import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.draw.ColorInterpolator;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;

/**
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class DesiredSpeedColorer implements GtuColorer, Serializable
{
    /** */
    private static final long serialVersionUID = 20150000L;

    /** The legend. */
    private final ArrayList<LegendEntry> legend;

    /** The speed that corresponds to the first entry in the legend. */
    private final Speed minimumSpeed;

    /** The speed that corresponds to the last entry in the legend. */
    private final Speed maximumSpeed;

    /**
     * Construct a new SpeedGtuColorer.
     * @param minimumSpeed the speed at (and below) which the returned color will be red
     * @param maximumSpeed the speed at (and above) which the returned color will be green
     */
    public DesiredSpeedColorer(final Speed minimumSpeed, final Speed maximumSpeed)
    {
        this.minimumSpeed = minimumSpeed;
        this.maximumSpeed = maximumSpeed;
        this.legend = new ArrayList<>(4);
        Color[] colorTable = {Color.RED, Color.YELLOW, Color.GREEN};
        for (int index = 0; index < colorTable.length; index++)
        {
            double ratio = index * 1.0 / (colorTable.length - 1);
            Speed speed = Speed.interpolate(minimumSpeed, maximumSpeed, ratio);
            String label = speed.toString().replaceFirst("\\.0*|,0*", ".0");
            this.legend.add(new LegendEntry(colorTable[index], label, index == 0 ? "stationary" : "driving " + label));
        }
        this.legend.add(new LegendEntry(Color.WHITE, "unknown", "unknown"));
    }

    @Override
    public final Color getColor(final Gtu gtu)
    {
        if (gtu instanceof LaneBasedGtu)
        {
            Speed speed = ((LaneBasedGtu) gtu).getDesiredSpeed();
            double ratio = (speed.si - this.minimumSpeed.si) / (this.maximumSpeed.si - this.minimumSpeed.si)
                    * (this.legend.size() - 2);
            if (ratio <= 0)
            {
                return this.legend.get(0).color();
            }
            if (ratio >= this.legend.size() - 2)
            {
                return this.legend.get(this.legend.size() - 2).color();
            }
            // Interpolate
            int floor = (int) Math.floor(ratio);
            return ColorInterpolator.interpolateColor(this.legend.get(floor).color(), this.legend.get(floor + 1).color(),
                    ratio - floor);
        }
        return Color.WHITE;
    }

    @Override
    public final List<LegendEntry> getLegend()
    {
        return Collections.unmodifiableList(this.legend);
    }

    @Override
    public final String toString()
    {
        return "Desired speed";
    }

}
