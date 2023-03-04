package org.opentrafficsim.core.animation.gtu.colorer;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.animation.ColorInterpolator;
import org.opentrafficsim.core.gtu.Gtu;

/**
 * Color GTU depending on their speed.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class SpeedGtuColorer implements GtuColorer, Serializable
{
    /** */
    private static final long serialVersionUID = 20150000L;

    /** The legend. */
    private final ArrayList<LegendEntry> legend;

    /** The speed that corresponds to the last entry in the legend. */
    private final Speed maximumSpeed;

    /**
     * Construct a new SpeedGtuColorer.
     * @param maximumSpeed Speed; the speed at (and above) which the returned color will be green
     */
    public SpeedGtuColorer(final Speed maximumSpeed)
    {
        this.maximumSpeed = maximumSpeed;
        this.legend = new ArrayList<>(4);
        Color[] colorTable = {Color.RED, Color.YELLOW, Color.GREEN};
        Speed zeroSpeed = new Speed(0.0, SpeedUnit.KM_PER_HOUR);
        for (int index = 0; index < colorTable.length; index++)
        {
            double ratio = index * 1.0 / (colorTable.length - 1);
            Speed speed = Speed.interpolate(zeroSpeed, maximumSpeed, ratio);
            String label = speed.toString().replaceFirst("\\.0*|,0*", ".0");
            this.legend.add(new LegendEntry(colorTable[index], label, index == 0 ? "stationary" : "driving " + label));
        }
    }

    /** {@inheritDoc} */
    @Override
    public final Color getColor(final Gtu gtu)
    {
        Speed speed = gtu.getSpeed();
        double ratio = speed.getSI() / this.maximumSpeed.getSI() * (this.legend.size() - 1);
        if (ratio <= 0)
        {
            return this.legend.get(0).getColor();
        }
        if (ratio >= this.legend.size() - 1)
        {
            return this.legend.get(this.legend.size() - 1).getColor();
        }
        // Interpolate
        int floor = (int) Math.floor(ratio);
        return ColorInterpolator.interpolateColor(this.legend.get(floor).getColor(), this.legend.get(floor + 1).getColor(),
                ratio - floor);
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
        return "Speed";
    }

}
