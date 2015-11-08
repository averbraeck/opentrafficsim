package org.opentrafficsim.core.gtu.animation;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.gtu.GTU;

/**
 * Color GTU depending on their speed.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$,
 *          initial version 27 mei 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class VelocityGTUColorer implements GTUColorer
{
    /** The legend. */
    private final ArrayList<LegendEntry> legend;

    /** The speed that corresponds to the last entry in the legend. */
    private final Speed maximumSpeed;

    /**
     * Construct a new VelocityGTUColorer.
     * @param maximumSpeed DoubleScalar.Abs&lt;SpeedUnit&gt;; the speed at (and above) which the returned color will be green
     */
    public VelocityGTUColorer(final Speed maximumSpeed)
    {
        this.maximumSpeed = maximumSpeed;
        this.legend = new ArrayList<LegendEntry>(4);
        Color[] colorTable = {Color.RED, Color.YELLOW, Color.GREEN};
        Speed zeroValue = new Speed(0, maximumSpeed.getUnit());
        for (int index = 0; index < colorTable.length; index++)
        {
            double ratio = index * 1.0 / (colorTable.length - 1);
            Speed speed = Speed.interpolate(zeroValue, maximumSpeed, ratio);
            this.legend.add(new LegendEntry(colorTable[index], speed.toString(), index == 0 ? "stationary" : "driving "
                + speed.toString()));
        }
    }

    /** {@inheritDoc} */
    @Override
    public final Color getColor(final GTU gtu) 
    {
        Speed speed = gtu.getVelocity();
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
        return "Velocity";
    }

}
