package org.opentrafficsim.road.gtu.colorer;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.animation.ColorInterpolator;
import org.opentrafficsim.core.animation.gtu.colorer.GTUColorer;
import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;

/**
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 3 mrt. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class DesiredSpeedColorer implements GTUColorer, Serializable
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
     * Construct a new SpeedGTUColorer.
     * @param minimumSpeed Speed; the speed at (and below) which the returned color will be red
     * @param maximumSpeed Speed; the speed at (and above) which the returned color will be green
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

    /** {@inheritDoc} */
    @Override
    public final Color getColor(final GTU gtu)
    {
        if (gtu instanceof LaneBasedGTU)
        {
            Speed speed = ((LaneBasedGTU) gtu).getDesiredSpeed();
            double ratio = (speed.si - this.minimumSpeed.si) / (this.maximumSpeed.si - this.minimumSpeed.si)
                    * (this.legend.size() - 2);
            if (ratio <= 0)
            {
                return this.legend.get(0).getColor();
            }
            if (ratio >= this.legend.size() - 2)
            {
                return this.legend.get(this.legend.size() - 2).getColor();
            }
            // Interpolate
            int floor = (int) Math.floor(ratio);
            return ColorInterpolator.interpolateColor(this.legend.get(floor).getColor(), this.legend.get(floor + 1).getColor(),
                    ratio - floor);
        }
        return Color.WHITE;
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
        return "Desired speed";
    }

}
