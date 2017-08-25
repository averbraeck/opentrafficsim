package compatibility;

import java.util.HashMap;
import java.util.Map;

import nl.tudelft.simulation.language.Throw;

import org.opentrafficsim.base.HierarchicalType;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.LongitudinalDirectionality;

/**
 * Directional GTUType dependent compatibility.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Aug 25, 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <I> infrastructure type, e.g. LinkType or LaneType, or water way type
 */
public class GTUCompatibility<I extends HierarchicalType<I>> implements Compatibility<GTUType, I>
{
    /** The map of GTUTypes to permitted directions of movement. */
    private final Map<GTUType, LongitudinalDirectionality> allowanceMap = new HashMap<>();

    /**
     * Construct a new Compatibility object with empty allowed and forbidden sets for both directions.
     */
    public GTUCompatibility()
    {
        // Nothing to do here
    }

    /**
     * Construct a new Compatibility and deep copy the allowed and forbidden sets for both directions from an existing
     * Compatibility.
     * @param original Compatibility; the Compatibility from which the allowed and forbidden sets for both directions will be
     *            copied
     */
    public GTUCompatibility(final GTUCompatibility<I> original)
    {
        this.allowanceMap.putAll(original.allowanceMap);
    }

    /**
     * Determine if this Compatibility allows or denies a particular GTUType.
     * @param gtuType GTUType; the GTUType to check
     * @param directionality GTUDirectionality; the GTUDirectionality in which the GTUType wants to move
     * @return Boolean; true if the GTUType is compatible; false if the GTUType is not compatible; null if this Compatibility
     *         cannot decide (the Compatibility of a super type should then determine whether the GTUType is compatible)
     */
    @Override
    public final Boolean isCompatible(final GTUType gtuType, final GTUDirectionality directionality)
    {
        LongitudinalDirectionality allowedDirections = this.allowanceMap.get(gtuType);
        if (null == allowedDirections)
        {
            return null;
        }
        switch (allowedDirections)
        {
            case DIR_BOTH:
                return true;
            case DIR_MINUS:
                return GTUDirectionality.DIR_MINUS == directionality;
            case DIR_NONE:
                return false;
            case DIR_PLUS:
                return GTUDirectionality.DIR_PLUS == directionality;
            default:
                System.err.println("Cannot happen");
                return null;
        }
    }

    /**
     * Add a GTUType to this GTUCompatibility.
     * @param gtuType GTUType; the GTUType to add to the allowed set of this Compatibility
     * @param directionality LongitudinalDirectionality; directionality for which the GTUType must be added
     * @return GTYUCompatibility&lt;I&gt;; this GTUCompatibility for method call chaining
     * @throws NullPointerException when <code>directionality</code> is null
     */
    public final GTUCompatibility<I> addAllowedGTUType(final GTUType gtuType, final LongitudinalDirectionality directionality)
            throws NullPointerException
    {
        Throw.whenNull(directionality, "Directionality for GTUType %s may not be null", gtuType);
        this.allowanceMap.put(gtuType, directionality);
        return this;
    }

    /**
     * Remove a GTUType from the allowed set of this Compatibility. This method cannot fail; no warning is issued when the
     * GTUType is not currently in the allowed set.
     * @param gtuType GTUType; the GTUType to remove from the allowed set
     * @param directionality LongitudinalDirectionality; the longitudinal directionality for which the GTUType must be removed
     * @return GTYUCompatibility&lt;I&gt;; this GTUCompatibility for method call chaining
     */
    public final GTUCompatibility<I> removeAllowedGTUType(final GTUType gtuType,
            final LongitudinalDirectionality directionality)
    {
        this.allowanceMap.remove(gtuType);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public final LongitudinalDirectionality getDirectionality(final GTUType gtuType)
    {
        for (GTUType testGTUType = gtuType; null != testGTUType; testGTUType = testGTUType.getParent())
        {
            LongitudinalDirectionality result = this.allowanceMap.get(gtuType);
            if (null != result)
            {
                return result;
            }
        }
        return LongitudinalDirectionality.DIR_NONE;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "GTUCompatibility [allowanceMap=" + this.allowanceMap + "]";
    }

    /** {@inheritDoc} */
    @Override
    public final int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.allowanceMap == null) ? 0 : this.allowanceMap.hashCode());
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public final boolean equals(final Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        GTUCompatibility<?> other = (GTUCompatibility<?>) obj;
        if (this.allowanceMap == null)
        {
            if (other.allowanceMap != null)
            {
                return false;
            }
        }
        else if (!this.allowanceMap.equals(other.allowanceMap))
        {
            return false;
        }
        return true;
    }

}
