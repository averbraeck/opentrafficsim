package org.opentrafficsim.core.gtu.animation;

import java.awt.Color;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.unit.AccelerationUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 29 mei 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class AccelerationGTUColorer implements GTUColorer
{
    /** The legend. */
    private final ArrayList<LegendEntry> legend;

    /** The deceleration that corresponds to the first entry in the legend. */
    private final DoubleScalar.Abs<AccelerationUnit> maximumDeceleration;

    /** The deceleration that corresponds to the last entry in the legend. */
    private final DoubleScalar.Abs<AccelerationUnit> maximumAcceleration;

    /** Negative scale part of the range of colors (excluding the zero value). */
    private static Color[] DECELERATIONCOLORS = {Color.RED, Color.ORANGE, Color.YELLOW};

    /** Positive scale part of the range of colors (including the zero value). */
    private static Color[] ACCELERATIONCOLORS = {Color.YELLOW, Color.GREEN, Color.BLUE};

    /**
     * Construct a new AccelerationGTUColorer.
     * @param maximumDeceleration DoubleScalar.Abs&ltAccelerationUnit&gt;; the deceleration (negative acceleration) that
     *            corresponds to the first (red) legend entry
     * @param maximumAcceleration DoubleScalar.Abs&ltAccelerationUnit&gt;; the deceleration that corresponds to the last
     *            (blue) legend entry
     */
    public AccelerationGTUColorer(final DoubleScalar.Abs<AccelerationUnit> maximumDeceleration,
            final DoubleScalar.Abs<AccelerationUnit> maximumAcceleration)
    {
        this.maximumDeceleration = maximumDeceleration;
        this.maximumAcceleration = maximumAcceleration;
        this.legend = new ArrayList<LegendEntry>(5);
        DoubleScalar.Abs<AccelerationUnit> zeroValue = new DoubleScalar.Abs<AccelerationUnit>(0, AccelerationUnit.SI);
        for (int index = 0; index < DECELERATIONCOLORS.length - 1; index++)
        {
            double ratio = index * 1.0 / (DECELERATIONCOLORS.length - 1);
            DoubleScalar.Abs<AccelerationUnit> acceleration =
                    DoubleScalar.interpolate(this.maximumDeceleration, zeroValue, ratio).immutable();
            this.legend.add(new LegendEntry(DECELERATIONCOLORS[index], acceleration.toString(), "deceleration"
                    + acceleration.toString()));
        }
        for (int index = 0; index < ACCELERATIONCOLORS.length; index++)
        {
            double ratio = index * 1.0 / (ACCELERATIONCOLORS.length - 1);
            DoubleScalar.Abs<AccelerationUnit> acceleration =
                    DoubleScalar.interpolate(zeroValue, this.maximumAcceleration, ratio).immutable();
            this.legend.add(new LegendEntry(ACCELERATIONCOLORS[index], acceleration.toString(), "acceleration"
                    + acceleration.toString()));
        }
    }

    /** {@inheritDoc} */
    @Override
    public Color getColor(GTU<?> gtu) throws RemoteException
    {
        DoubleScalar.Abs<AccelerationUnit> acceleration = gtu.getAcceleration();
        double ratio;
        if (acceleration.getSI() < 0)
        {
            ratio =
                    DECELERATIONCOLORS.length - 1 - acceleration.getSI() / this.maximumDeceleration.getSI()
                            * (DECELERATIONCOLORS.length - 1);
        }
        else
        {
            ratio =
                    acceleration.getSI() / this.maximumAcceleration.getSI() * (ACCELERATIONCOLORS.length - 1)
                            + DECELERATIONCOLORS.length - 1;
        }
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
        return ColorInterpolator.interpolateColor(this.legend.get(floor).getColor(), this.legend.get(floor + 1)
                .getColor(), ratio - floor);
    }

    /** {@inheritDoc} */
    @Override
    public List<LegendEntry> getLegend()
    {
        return Collections.unmodifiableList(this.legend);
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "Acceleration";
    }

}
