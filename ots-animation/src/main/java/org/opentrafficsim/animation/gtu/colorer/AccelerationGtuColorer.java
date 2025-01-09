package org.opentrafficsim.animation.gtu.colorer;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.draw.ColorInterpolator;

/**
 * Color GTUs based on their current acceleration.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 */
public class AccelerationGtuColorer implements GtuColorer, Serializable
{
    /** */
    private static final long serialVersionUID = 201500001L;

    /** The legend. */
    private final ArrayList<LegendEntry> legend;

    /** The deceleration that corresponds to the first entry in the legend. */
    private final Acceleration maximumDeceleration;

    /** The deceleration that corresponds to the last entry in the legend. */
    private final Acceleration maximumAcceleration;

    /** Negative scale part of the range of colors (excluding the zero value). */
    private static Color[] decelerationColors = {Color.MAGENTA, Color.RED, Color.ORANGE, Color.YELLOW};

    /** Positive scale part of the range of colors (including the zero value). */
    private static Color[] accelerationColors = {Color.YELLOW, Color.GREEN, Color.BLUE};

    /**
     * Construct a new AccelerationGtuColorer.
     * @param maximumDeceleration the deceleration (negative acceleration) that corresponds to the first (red) legend entry
     * @param maximumAcceleration the deceleration that corresponds to the last (blue) legend entry
     */
    public AccelerationGtuColorer(final Acceleration maximumDeceleration, final Acceleration maximumAcceleration)
    {
        this.maximumDeceleration = maximumDeceleration;
        this.maximumAcceleration = maximumAcceleration;
        this.legend = new ArrayList<>(6);
        for (int index = 0; index < decelerationColors.length - 1; index++)
        {
            double ratio = index * 1.0 / (decelerationColors.length - 1);
            Acceleration acceleration = Acceleration.interpolate(this.maximumDeceleration, Acceleration.ZERO, ratio);
            String label = acceleration.toString().replaceFirst("\\.0*", ".0");
            this.legend.add(new LegendEntry(decelerationColors[index], label, "deceleration" + label));
        }
        for (int index = 0; index < accelerationColors.length; index++)
        {
            double ratio = index * 1.0 / (accelerationColors.length - 1);
            Acceleration acceleration = Acceleration.interpolate(Acceleration.ZERO, this.maximumAcceleration, ratio);
            String label = acceleration.toString().replaceFirst("\\.0*", ".0");
            this.legend.add(new LegendEntry(accelerationColors[index], label, "acceleration" + label));
        }
    }

    @Override
    public final Color getColor(final Gtu gtu)
    {
        Acceleration acceleration = gtu.getAcceleration();
        double ratio;
        if (acceleration.getSI() < 0)
        {
            ratio = decelerationColors.length - 1
                    - acceleration.getSI() / this.maximumDeceleration.getSI() * (decelerationColors.length - 1);
        }
        else
        {
            ratio = acceleration.getSI() / this.maximumAcceleration.getSI() * (accelerationColors.length - 1)
                    + decelerationColors.length - 1;
        }
        if (ratio <= 0)
        {
            return this.legend.get(0).color();
        }
        if (ratio >= this.legend.size() - 1)
        {
            return this.legend.get(this.legend.size() - 1).color();
        }
        // Interpolate
        int floor = (int) Math.floor(ratio);
        return ColorInterpolator.interpolateColor(this.legend.get(floor).color(), this.legend.get(floor + 1).color(),
                ratio - floor);
    }

    @Override
    public final List<LegendEntry> getLegend()
    {
        return Collections.unmodifiableList(this.legend);
    }

    @Override
    public final String getName()
    {
        return "Acceleration";
    }

}
