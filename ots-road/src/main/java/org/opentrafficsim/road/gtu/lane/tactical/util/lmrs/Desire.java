package org.opentrafficsim.road.gtu.lane.tactical.util.lmrs;

import org.djunits.value.vdouble.scalar.Dimensionless;
import org.opentrafficsim.base.OtsRuntimeException;
import org.opentrafficsim.core.network.LateralDirectionality;

/**
 * Reflects the level of lane change desire a driver experiences in both the left and right direction. This may be either total
 * desire, or only for a single lane change incentive. Desire is defined as ranging from 0 to 1, where 0 means no desire and 1
 * means full desire. Values above 1 are not valid and should be limited to 1. Values below 0 are allowed and reflect that a
 * lane change is undesired (which is different from not desired).
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param left Left desire.
 * @param right Right desire.
 */
public record Desire(double left, double right)
{

    /** Easy access and efficient zero desired. */
    public static final Desire ZERO = new Desire(0.0, 0.0);

    /**
     * Constructor which sets the supplied desire. Desire is limited to a maximum of 1.
     * @param left Left desire.
     * @param right Right desire.
     */
    public Desire(final double left, final double right)
    {
        this.left = left <= 1 ? left : 1;
        this.right = right <= 1 ? right : 1;
    }

    /**
     * Constructor which sets the supplied desire. Desire is limited to a maximum of 1.
     * @param left Left desire.
     * @param right Right desire.
     */
    public Desire(final Dimensionless left, final Dimensionless right)
    {
        this(left.si, right.si);
    }

    /**
     * Returns desire in the given direction.
     * @param dir Direction for the desire to return.
     * @return Desire in the given direction.
     */
    public final double get(final LateralDirectionality dir)
    {
        if (dir.equals(LateralDirectionality.LEFT))
        {
            return this.left;
        }
        if (dir.equals(LateralDirectionality.RIGHT))
        {
            return this.right;
        }
        throw new OtsRuntimeException("Lateral direction may not be NONE.");
    }

    /**
     * Returns whether the left desire is larger than (or equal to) the right.
     * @return Returns whether the left desire is larger than (or equal to) the right.
     */
    public final boolean leftIsLargerOrEqual()
    {
        return this.left >= this.right;
    }

}
