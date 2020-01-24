package org.opentrafficsim.core.compatibility;

import java.util.LinkedHashMap;
import java.util.Map;

import org.djutils.exceptions.Throw;
import org.djutils.logger.CategoryLogger;
import org.opentrafficsim.base.HierarchicalType;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.LongitudinalDirectionality;

/**
 * Directional GTUType dependent compatibility.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Aug 25, 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <I> infrastructure type, e.g. LinkType or LaneType, or water way type
 */
public class GTUCompatibility<I extends HierarchicalType<I> & Compatibility<GTUType, I>> implements Compatibility<GTUType, I>
{
    /** The map of GTUTypes to permitted directions of movement. */
    private final Map<GTUType, LongitudinalDirectionality> allowanceMap = new LinkedHashMap<>();

    /** Infrastructure type, e.g. LinkType, LaneType, SensorType. */
    private final I infrastructure;

    /**
     * Construct a new Compatibility object with empty allowed and forbidden sets for both directions.
     * @param infrastructure I; the infrastructure type, e.g. LinkType, LaneType, SensorType.
     */
    public GTUCompatibility(final I infrastructure)
    {
        this.infrastructure = infrastructure;
    }

    /**
     * Construct a new Compatibility and deep copy the allowed and forbidden sets for both directions from an existing
     * Compatibility.
     * @param original GTUCompatibility&lt;I&gt;; the Compatibility from which the allowed and forbidden sets for both
     *            directions will be copied
     */
    public GTUCompatibility(final GTUCompatibility<I> original)
    {
        this.infrastructure = original.infrastructure;
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
                CategoryLogger.always().warn("Unknown type in isCompatible - Cannot happen");
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

    /**
     * Check if a GTUCompatibility does not allow things that the GTUCompatibility of a parent object disallows, e.g. a
     * permitted driving direction on a Lane should not be forbidden on the Link that the Lane is part of.
     * @param parentCompatibility Compatibility&lt;GTUType, ?&gt;; the GTUCompatibility of the parent object
     * @param tryParentsOfGTUType boolean; whether to try parent GTU types
     * @throws GTUException if a conflict is found
     */
    public final void isCompatibleWith(final Compatibility<GTUType, ?> parentCompatibility, final boolean tryParentsOfGTUType)
            throws GTUException
    {
        for (GTUType gtuType : this.allowanceMap.keySet())
        {
            LongitudinalDirectionality ourLD = this.allowanceMap.get(gtuType);
            LongitudinalDirectionality parentLD = parentCompatibility.getDirectionality(gtuType, true);
            if (!parentLD.contains(ourLD))
            {
                throw new GTUException(String.format("GTUType %s has LongitudinalDirectionality %s on child, but %s on parent",
                        ourLD, parentLD));
            }
        }
        // TODO cleverly check only those in the parent(s) that do not conflict with ours.
    }

    /** {@inheritDoc} */
    @Override
    public final LongitudinalDirectionality getDirectionality(final GTUType gtuType, final boolean tryParentsOfGTUType)
    {
        for (GTUType testGTUType = gtuType; null != testGTUType; testGTUType = testGTUType.getParent())
        {
            LongitudinalDirectionality result = this.allowanceMap.get(testGTUType);
            if (null != result)
            {
                return result;
            }
            if (null != this.infrastructure && null != this.infrastructure.getParent())
            {
                result = this.infrastructure.getParent().getDirectionality(testGTUType, false);
                if (null != result)
                {
                    return result;
                }
            }
            if (!tryParentsOfGTUType)
            {
                break;
            }
        }
        return tryParentsOfGTUType ? LongitudinalDirectionality.DIR_NONE : null;
    }
    
}
