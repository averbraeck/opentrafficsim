package org.opentrafficsim.road.network.lane;

import java.awt.Color;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.vector.LengthVector;
import org.djutils.base.Identifiable;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.base.StripeElement;
import org.opentrafficsim.base.StripeElement.StripeLateralSync;
import org.opentrafficsim.base.Type;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.LateralDirectionality;

/**
 * Stripe road marking. This class only contains functional information. There is no information on how to draw the stripe, i.e.
 * no color and no information on dashes. The stripe types has information on this, but this only serves as a default towards
 * classes that do draw a stripe.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class Stripe extends CrossSectionElement
{
    /** */
    private static final long serialVersionUID = 20141025L;

    /** Type of the stripe, defining default permeability. */
    private final StripeType type;

    /** Left permeability. */
    private boolean left;

    /** Right permeability. */
    private boolean right;

    /** Lateral permeability per GTU type and direction. */
    private final Map<GtuType, Set<LateralDirectionality>> permeabilityMap = new LinkedHashMap<>();

    /** Stripe elements. */
    private List<StripeElement> elements;

    /** Longitudinal offset of dashes. */
    private Length dashOffset = Length.ZERO;

    /** Lateral synchronization. */
    private StripeLateralSync lateralSync = StripeLateralSync.SNAP;

    /** Phase synchronization. */
    private StripePhaseSync phaseSync = StripePhaseSync.NONE;

    /** Period based on all stripe elements. */
    private Double period;

    /**
     * Constructor specifying geometry. Permeability is set according to the stripe type default.
     * @param type stripe type defining appearance and default permeability.
     * @param id id
     * @param link link
     * @param geometry geometry
     */
    public Stripe(final StripeType type, final String id, final CrossSectionLink link, final CrossSectionGeometry geometry)
    {
        super(link, id, geometry);
        Throw.whenNull(type, "Type may not be null.");
        this.type = type;
        this.left = type.left();
        this.right = type.right();
        this.elements = type.elements;
    }

    /**
     * Returns the stripe type.
     * @return stripe type.
     */
    public StripeType getType()
    {
        return this.type;
    }

    /**
     * Set left permeability, overruling the stripe type default.
     * @param permeability permeability
     */
    public void setLeftPermeability(final boolean permeability)
    {
        this.left = permeability;
    }

    /**
     * Set right permeability, overruling the stripe type default.
     * @param permeability permeability
     */
    public void setRightPermeability(final boolean permeability)
    {
        this.right = permeability;
    }

    /**
     * Add lateral permeability for a GTU type. This overrules permeability based on the stripe type, and those set regardless
     * of GTU type. Add NONE to prevent lane changes. Add LEFT or RIGHT, or both in two calls, to enable lane changes.
     * @param gtuType GTU type to add permeability for.
     * @param lateralDirection direction to add compared to the direction of the design line.
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
        Throw.when(lateralDirection.isNone(), RuntimeException.class,
                "May not request NONE lateral direction for permeability.");
        for (GtuType testGtuType = gtuType; null != testGtuType; testGtuType = testGtuType.getParent())
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
     * Sets the elements.
     * @param elements elements
     */
    public void setElements(final List<StripeElement> elements)
    {
        this.period = null;
        this.elements = elements;
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
     * Sets the dash offset.
     * @param dashOffset dash offset
     */
    public void setDashOffset(final Length dashOffset)
    {
        this.dashOffset = dashOffset;
    }

    /**
     * Returns the dash offset.
     * @return dash offset
     */
    public Length getDashOffset()
    {
        return this.dashOffset;
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
            List<Double> lineLengths = new ArrayList<>();
            for (StripeElement element : this.elements)
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
            this.period = getPeriod(lineLengths);
        }
        return this.period;
    }

    @Override
    public String toString()
    {
        return "Stripe [id=" + this.getFullId() + "]";
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
        
        /** Whether synhronization should be applied. */
        private final boolean sync;
        
        /**
         * Constructor.
         * @param sync whether synhronization should be applied
         */
        StripePhaseSync(final boolean sync)
        {
            this.sync = sync;
        }
        
        /**
         * Returns whether synhronization should be applied.
         * @return whether synhronization should be applied
         */
        public boolean isSync()
        {
            return this.sync;
        }
    }

    /**
     * Stripe type defines the default permeability, width and elements.
     * @param id id
     * @param left left lane change allowed by default
     * @param right right lane change allowed by default
     * @param width default width
     * @param elements list of default elements
     */
    public record StripeType(String id, boolean left, boolean right, Length width, List<StripeElement> elements)
            implements Type<StripeType>, Identifiable
    {

        /** Standard width. */
        private static final Length WIDTH = Length.instantiateSI(0.2);

        /** Single solid line. */
        public static final StripeType SOLID =
                new StripeType("SOLID", false, false, WIDTH, List.of(StripeElement.continuous(WIDTH, Color.WHITE)));

        /** Line |¦ allow to go to left, but not to right. */
        public static final StripeType LEFT = new StripeType("LEFT", true, false, WIDTH.times(3.0),
                List.of(StripeElement.continuous(WIDTH, Color.WHITE), StripeElement.gap(WIDTH),
                        StripeElement.dashed(WIDTH, Color.WHITE, new LengthVector(new double[] {9, 3}))));

        /** Line ¦| allow to go to right, but not to left. */
        public static final StripeType RIGHT = new StripeType("RIGHT", false, true, Length.instantiateSI(0.6),
                List.of(StripeElement.dashed(WIDTH, Color.WHITE, new LengthVector(new double[] {9, 3})),
                        StripeElement.gap(WIDTH), StripeElement.continuous(WIDTH, Color.WHITE)));

        /** Dashes ¦ allow to cross in both directions. */
        public static final StripeType DASHED = new StripeType("DASHED", true, true, Length.instantiateSI(0.2),
                List.of(StripeElement.dashed(WIDTH, Color.WHITE, new LengthVector(new double[] {9, 3}))));

        /** Double solid line ||, don't cross. */
        public static final StripeType DOUBLE_SOLID = new StripeType("DOUBLE_SOLID", false, false, Length.instantiateSI(0.6),
                List.of(StripeElement.continuous(WIDTH, Color.WHITE), StripeElement.gap(WIDTH),
                        StripeElement.continuous(WIDTH, Color.WHITE)));

        /** Double dashed line ¦¦, cross. */
        public static final StripeType DOUBLE_DASH = new StripeType("DOUBLE_DASHED", true, true, Length.instantiateSI(0.6),
                List.of(StripeElement.dashed(WIDTH, Color.WHITE, new LengthVector(new double[] {9, 3})),
                        StripeElement.gap(WIDTH),
                        StripeElement.dashed(WIDTH, Color.WHITE, new LengthVector(new double[] {9, 3}))));

        /** Block : allow to cross in both directions. */
        public static final StripeType BLOCK = new StripeType("BLOCK", true, true, WIDTH.times(2.0),
                List.of(StripeElement.dashed(WIDTH.times(2.0), Color.WHITE, new LengthVector(new double[] {3, 1}))));

        @Override
        public String getId()
        {
            return this.id;
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(this.id);
        }

        @Override
        @SuppressWarnings("needbraces")
        public boolean equals(final Object obj)
        {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            StripeType other = (StripeType) obj;
            return Objects.equals(this.id, other.id);
        }
    }

}
