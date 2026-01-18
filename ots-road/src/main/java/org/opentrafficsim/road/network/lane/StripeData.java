package org.opentrafficsim.road.network.lane;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.base.OtsRuntimeException;
import org.opentrafficsim.base.StripeElement;
import org.opentrafficsim.base.StripeElement.StripeLateralSync;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.LateralDirectionality;

/**
 * Container for data about stripes, independent from the link and curvature.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class StripeData
{

    /** Stripe elements. */
    private List<StripeElement> elements;

    /** Left permeability. */
    private final boolean left;

    /** Right permeability. */
    private final boolean right;

    /** Lateral permeability per GTU type and direction. */
    private final Map<GtuType, Set<LateralDirectionality>> permeabilityMap = new LinkedHashMap<>();

    /** Lateral synchronization. */
    private StripeLateralSync lateralSync = StripeLateralSync.LINK;

    /** Phase synchronization. */
    private StripePhaseSync phaseSync = StripePhaseSync.NONE;

    /** Period based on all stripe elements. */
    private Double period;

    /**
     * Constructor.
     * @param elements list of stripe elements
     * @param left left overall permeability
     * @param right right overall permeability
     */
    public StripeData(final List<StripeElement> elements, final boolean left, final boolean right)
    {
        this.elements = elements;
        this.left = left;
        this.right = right;
    }

    /**
     * Returns the elements.
     * @return elements
     */
    public List<StripeElement> getElements()
    {
        return this.elements;
    }

    /**
     * Sets the elements.
     * @param elements elements
     */
    public void setElements(final List<StripeElement> elements)
    {
        this.elements = elements;
        this.period = null;
    }

    /**
     * Add lateral permeability for a GTU type. This overrules overall stripe permeability. Add NONE to prevent lane changes.
     * Add both LEFT and RIGHT in two calls, to enable lane changes. Add LEFT or RIGHT to enable one direction while prohibiting
     * the other.
     * @param gtuType GTU type to add permeability for
     * @param lateralDirection direction to add compared to the direction of the design line
     */
    public void addPermeability(final GtuType gtuType, final LateralDirectionality lateralDirection)
    {
        if (!this.permeabilityMap.containsKey(gtuType))
        {
            this.permeabilityMap.put(gtuType, new LinkedHashSet<LateralDirectionality>(2));
        }
        this.permeabilityMap.get(gtuType).add(lateralDirection);
    }

    /**
     * Returns whether the given GTU type is allowed to cross the line in the given lateral direction.
     * @param gtuType GTU type to look for.
     * @param lateralDirection direction to look for (LEFT or RIGHT) compared to the direction of the design line.
     * @return whether the road marker is permeable for the GTU type.
     */
    public final boolean isPermeable(final GtuType gtuType, final LateralDirectionality lateralDirection)
    {
        Throw.when(lateralDirection.isNone(), OtsRuntimeException.class,
                "May not request NONE lateral direction for permeability.");
        for (GtuType testGtuType = gtuType; testGtuType != null; testGtuType = testGtuType.getParent().orElse(null))
        {
            Set<LateralDirectionality> set = this.permeabilityMap.get(testGtuType);
            if (null != set)
            {
                return set.contains(lateralDirection);
            }
        }
        return lateralDirection.isLeft() ? this.left : this.right;
    }

    /**
     * Sets the lateral synchronization.
     * @param lateralSync lateral synchronization
     */
    public void setLateralSync(final StripeLateralSync lateralSync)
    {
        this.lateralSync = lateralSync;
    }

    /**
     * Returns the lateral synchronization.
     * @return lateral synchronization
     */
    public StripeLateralSync getLateralSync()
    {
        return this.lateralSync;
    }

    /**
     * Sets the phase synchronization.
     * @param phaseSync phase synchronization
     */
    public void setPhaseSync(final StripePhaseSync phaseSync)
    {
        this.phaseSync = phaseSync;
    }

    /**
     * Returns the phase synchronization.
     * @return phase synchronization
     */
    public StripePhaseSync getPhaseSync()
    {
        return this.phaseSync;
    }

    /**
     * Returns the period of the common dash pattern.
     * @return period of the common dash pattern
     */
    public double getPeriod()
    {
        if (this.period == null)
        {
            this.period = getPeriod(this.elements);
        }
        return this.period;
    }

    /**
     * Returns the period after which the given line gap-dash patterns repeat as a whole. Lengths are rounded to a precision of
     * 0.0001 to find the greatest common divisor.
     * @param elements elements
     * @return period
     */
    public static double getPeriod(final List<StripeElement> elements)
    {
        List<Double> lineLengths = new ArrayList<>();
        for (StripeElement element : elements)
        {
            if (element.dashes() != null)
            {
                double length = 0.0;
                for (Length gapDash : element.dashes())
                {
                    length += gapDash.si;
                }
                lineLengths.add(length);
            }
        }
        return getPeriod(lineLengths);
    }

    /**
     * Returns the period after which the given line gap-dash patterns repeat as a whole. Lengths are rounded to a precision of
     * 0.0001 to find the greatest common divisor.
     * @param lineLengths gap-dash pattern lengths
     * @return period
     */
    private static double getPeriod(final Collection<Double> lineLengths)
    {
        Set<Double> set = new LinkedHashSet<>(lineLengths);
        if (lineLengths.isEmpty())
        {
            return -1.0;
        }
        else if (set.size() == 1)
        {
            return ((long) (lineLengths.iterator().next() * 10000)) / 10000.0;
        }
        long gcd = 1L;
        for (double length : set)
        {
            gcd = BigInteger.valueOf(gcd).gcd(BigInteger.valueOf((long) (length * 10000))).longValue();
        }
        return gcd / 10000.0;
    }

    /**
     * Returns the width, which is the sum of stripe elements.
     * @return width
     */
    public Length getWidth()
    {
        Length width = Length.ZERO;
        for (StripeElement element : getElements())
        {
            width = width.plus(element.width());
        }
        return width;
    }

    /**
     * Method of stripe phase synchronization.
     */
    public enum StripePhaseSync
    {
        /** Do not synchronize. */
        NONE(false),

        /** Synchronize phase to upstream stripe. */
        UPSTREAM(true),

        /** Synchronize phase to downstream stripe. */
        DOWNSTREAM(true);

        /** Whether synchronization should be applied. */
        private final boolean sync;

        /**
         * Constructor.
         * @param sync whether synchronization should be applied
         */
        StripePhaseSync(final boolean sync)
        {
            this.sync = sync;
        }

        /**
         * Returns whether synchronization should be applied.
         * @return whether synchronization should be applied
         */
        public boolean isSync()
        {
            return this.sync;
        }
    }

}
