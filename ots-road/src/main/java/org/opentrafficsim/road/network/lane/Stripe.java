package org.opentrafficsim.road.network.lane;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.vector.LengthVector;
import org.djutils.base.Identifiable;
import org.djutils.draw.line.Polygon2d;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.base.Type;
import org.opentrafficsim.core.geometry.OtsLine2d;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;

/**
 * Stripe road marking.
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

    /** Dashes of the stripe. The first length is a gap, than a dash, etc. {@code null} means no dashes. */
    private List<LengthVector> dashes;

    /** Longitudinal offset of dashes. */
    private Length dashOffset = Length.ZERO;

    /**
     * Constructor specifying geometry. Permeability is set according to the stripe type default.
     * @param type stripe type defining appearance and default permeability.
     * @param link link.
     * @param centerLine center line.
     * @param contour contour shape.
     * @param crossSectionSlices cross-section slices.
     * @throws NetworkException when no cross-section slice is defined.
     */
    public Stripe(final StripeType type, final CrossSectionLink link, final OtsLine2d centerLine, final Polygon2d contour,
            final List<CrossSectionSlice> crossSectionSlices) throws NetworkException
    {
        super(link, UUID.randomUUID().toString(), centerLine, contour, crossSectionSlices);
        Throw.whenNull(type, "Type may not be null.");
        this.type = type;
        this.left = type.left();
        this.right = type.right();
        this.dashes = type.dashes();
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
     * Set dashes of the stripe, overruling the default dashes of the stripe type. Each vector in the list defines a line, e.g.
     * for a double line. The first length is a gap, than a dash, etc. {@code null} means no dashes for the line.
     * @param dashes dashes, use {@code null} for no dashes.
     */
    public void setDashes(final List<LengthVector> dashes)
    {
        Throw.whenNull(dashes, "dashes");
        this.dashes = dashes;
    }

    /**
     * Return the dashes. The first length is a gap, than a dash, etc. {@code null} means no dashes.
     * @return dashes
     */
    public List<LengthVector> getDashes()
    {
        return this.dashes;
    }

    /**
     * Set the dash offset.
     * @param dashOffset dash offset
     */
    public void setDashOffset(final Length dashOffset)
    {
        this.dashOffset = dashOffset;
    }

    /**
     * Get the dash offset.
     * @return dash offset
     */
    public Length getDashOffset()
    {
        return this.dashOffset;
    }

    /**
     * Stripe type defines the default permeability, width and dashes.
     * @param id id
     * @param left left lane change allowed by default
     * @param right right lane change allowed by default
     * @param width default width
     * @param dashes list of default dashes, use {@code null} in the list for a solid line
     */
    public record StripeType(String id, boolean left, boolean right, Length width, List<LengthVector> dashes)
            implements Type<StripeType>, Identifiable
    {
        // NOTE: Do NOT use List.of(...) with null values, the class then cannot be loaded due to a NullPointerException during
        // loading of the class, as List.of(...) creates an immutable list that does not allow null values.

        /** Single solid line. */
        public static final StripeType SOLID =
                new StripeType("SOLID", false, false, Length.instantiateSI(0.2), Arrays.asList((LengthVector) null));

        /** Line |¦ allow to go to left, but not to right. */
        public static final StripeType LEFT = new StripeType("LEFT", true, false, Length.instantiateSI(0.6),
                Arrays.asList(null, new LengthVector(new double[] {9, 3})));

        /** Line ¦| allow to go to right, but not to left. */
        public static final StripeType RIGHT = new StripeType("RIGHT", false, true, Length.instantiateSI(0.6),
                Arrays.asList(new LengthVector(new double[] {9, 3}), null));

        /** Dashes ¦ allow to cross in both directions. */
        public static final StripeType DASHED = new StripeType("DASHED", true, true, Length.instantiateSI(0.2),
                List.of(new LengthVector(new double[] {9, 3})));

        /** Double solid line ||, don't cross. */
        public static final StripeType DOUBLE_SOLID =
                new StripeType("DOUBLE_SOLID", false, false, Length.instantiateSI(0.6), Arrays.asList(null, null));

        /** Double dashed line ¦¦, don't cross. */
        public static final StripeType DOUBLE_DASH =
                new StripeType("DOUBLE_DASH", true, true, Length.instantiateSI(0.6),
                        List.of(new LengthVector(new double[] {9, 3}), new LengthVector(new double[] {9, 3})));

        /** Block : allow to cross in both directions. */
        public static final StripeType BLOCK = new StripeType("BLOCK", true, true, Length.instantiateSI(0.4),
                List.of(new LengthVector(new double[] {3, 1})));

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
