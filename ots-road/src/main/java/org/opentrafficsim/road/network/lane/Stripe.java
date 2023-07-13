package org.opentrafficsim.road.network.lane;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.djutils.exceptions.Throw;
import org.opentrafficsim.core.geometry.OtsLine3d;
import org.opentrafficsim.core.geometry.OtsShape;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;

/**
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class Stripe extends CrossSectionElement
{
    /** */
    private static final long serialVersionUID = 20141025L;

    /** Type of the stripe, defining default permeability. */
    private final Type type;

    /** Type to overrule the normal type, e.g. rush-hour lanes without changing the appearance of the stripe. */
    private Type overruleType;

    /** Lateral permeability per GTU type and direction. */
    private final Map<GtuType, Set<LateralDirectionality>> permeabilityMap = new LinkedHashMap<>();

    /**
     * Constructor specifying geometry.
     * @param type Type; stripe type defining appearance and default permeability.
     * @param link CrossSectionLink; link.
     * @param centerLine OtsLine3d; center line.
     * @param contour OtsShape; contour shape.
     * @param crossSectionSlices List&lt;CrossSectionSlice&gt;; cross-section slices.
     * @throws NetworkException when no cross-section slice is defined.
     */
    public Stripe(final Type type, final CrossSectionLink link, final OtsLine3d centerLine, final OtsShape contour,
            final List<CrossSectionSlice> crossSectionSlices) throws NetworkException
    {
        super(link, UUID.randomUUID().toString(), centerLine, contour, crossSectionSlices);
        Throw.whenNull(type, "Type may not be null.");
        this.type = type;
    }

    /**
     * Returns the stripe type.
     * @return Type; stripe type.
     */
    public Type getType()
    {
        return this.type;
    }

    /**
     * Sets an overruling stripe type. This can be used for e.g. rush-hour lanes, without changing the appearance of the stripe.
     * Note that custom set permeabilities (addPermeability()) remain active.
     * @param overruleType Type; overruling stripe type.
     */
    public void setOverruleType(final Type overruleType)
    {
        this.overruleType = overruleType;
    }

    /**
     * Clears the overrule type, after which the normal type will hold.
     */
    public void clearOverruleType()
    {
        this.overruleType = null;
    }

    /**
     * Returns the currently active stripe type.
     * @return Type; the currently active stripe type.
     */
    private Type activeType()
    {
        return this.overruleType == null ? this.type : this.overruleType;
    }

    /** {@inheritDoc} */
    @Override
    public final double getZ()
    {
        return -0.0002;
    }

    /**
     * Add lateral permeability for a GTU type in the direction of the design line of the overarching CrossSectionLink. Add NONE
     * to prevent lane changes relative to the stripe type. Add LEFT or RIGHT, or both in two calls, to enable lane changes
     * relative to the stripe type.
     * @param gtuType GtuType; GTU type to add permeability for.
     * @param lateralDirection LateralDirectionality; direction to add compared to the direction of the design line.
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
     * @param gtuType GtuType; GTU type to look for.
     * @param lateralDirection LateralDirectionality; direction to look for (LEFT or RIGHT) compared to the direction of the
     *            design line.
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
        return lateralDirection.isLeft() ? activeType().left() : activeType().right;
    }

    /**
     * Defines the visible type of the stripe, and the standard permeability that pertains to it.
     * <p>
     * Copyright (c) 2022-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
     */
    public enum Type
    {
        /** Single solid line. */
        SOLID(false, false),

        /** Line |¦ allow to go to left, but not to right. */
        LEFT(true, false),

        /** Line ¦| allow to go to right, but not to left. */
        RIGHT(false, true),

        /** Dashes ¦ allow to cross in both directions. */
        DASHED(true, true),

        /** Double solid line ||, don't cross. */
        DOUBLE(false, false),

        /** Block : allow to cross in both directions. */
        BLOCK(true, true);

        /** Left permeable. */
        private final boolean left;

        /** Right permeable. */
        private final boolean right;

        /**
         * Constructor setting permeability.
         * @param left boolean; left permeability.
         * @param right boolean; right permeability.
         */
        Type(final boolean left, final boolean right)
        {
            this.left = left;
            this.right = right;
        }

        /**
         * Returns the left permeability.
         * @return boolean; left permeability.
         */
        public boolean left()
        {
            return this.left;
        }

        /**
         * Returns the right permeability.
         * @return boolean; right permeability.
         */
        public boolean right()
        {
            return this.right;
        }
    }

}
