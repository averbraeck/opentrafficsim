package org.opentrafficsim.core.compatibility;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.djutils.exceptions.Throw;
import org.opentrafficsim.base.HierarchicalType;
import org.opentrafficsim.base.OtsRuntimeException;
import org.opentrafficsim.core.gtu.GtuType;

/**
 * Compatibility between a GtuType and a certain type of infrastructure. Infrastructure can be any hierarchical structure: a
 * LinkType, a LaneType, a SensorType, etc.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 * @param <I> infrastructure type, e.g. LinkType or LaneType, or water way type
 */
public class GtuCompatibility<I extends HierarchicalType<I> & Compatibility<GtuType, I>> implements Compatibility<GtuType, I>
{
    /** The map of GtuTypes that define on this infrastructure level what GtuTypes are compatible or not. */
    private final Map<GtuType, Boolean> levelCompatibilityMap = new LinkedHashMap<>();

    /** The cached map of GtuTypes that have been resolved. */
    private final Map<GtuType, Boolean> cachedCompatibilityMap = new LinkedHashMap<>();

    /** Infrastructure for which this compatibility definition holds, e.g. a LinkType, a LaneType, or a SensorType. */
    private final I infrastructure;

    /**
     * Construct a new Compatibility object with empty compatible and forbidden sets.
     * @param infrastructure I; the infrastructure type, e.g. LinkType, LaneType, SensorType.
     */
    public GtuCompatibility(final I infrastructure)
    {
        Throw.whenNull(infrastructure, "infrastructure cannot be null");
        this.infrastructure = infrastructure;
    }

    /**
     * Construct a new Compatibility and deep copy the compatible and forbidden sets from an existing Compatibility.
     * @param original GtuCompatibility&lt;I&gt;; the Compatibility from which the compatible and forbidden sets will be copied
     */
    public GtuCompatibility(final GtuCompatibility<I> original)
    {
        this.infrastructure = original.infrastructure;
        this.levelCompatibilityMap.putAll(original.levelCompatibilityMap);
    }

    /**
     * Determine if this Compatibility allows or denies a particular GtuType.
     * @param gtuType GtuType; the GtuType to check
     * @return boolean; true if the GtuType is compatible; false if the GtuType is not compatible
     */
    @Override
    public boolean isCompatible(final GtuType gtuType)
    {
        if (!this.cachedCompatibilityMap.containsKey(gtuType))
        {
            boolean foundTrue = false;
            boolean foundFalse = false;
            I infra = this.infrastructure;
            while (infra != null)
            {
                GtuType gType = gtuType;
                while (gType != null)
                {
                    if (infra.isCompatibleOnInfraLevel(gType) != null)
                    {
                        if (infra.isCompatibleOnInfraLevel(gType))
                        {
                            foundTrue = true;
                        }
                        else
                        {
                            foundFalse = true;
                        }
                    }
                    gType = gType.getParent();
                }
                infra = infra.getParent();
            }
            if (foundFalse)
            {
                this.cachedCompatibilityMap.put(gtuType, false);
            }
            else if (foundTrue)
            {
                this.cachedCompatibilityMap.put(gtuType, true);
            }
            else
            {
                this.cachedCompatibilityMap.put(gtuType, false);
            }
        }
        return this.cachedCompatibilityMap.get(gtuType);
    }

    /** {@inheritDoc} */
    @Override
    public Boolean isCompatibleOnInfraLevel(final GtuType gtuType)
    {
        return this.levelCompatibilityMap.get(gtuType);
    }

    /**
     * Add an compatible GtuType to this GtuCompatibility.
     * @param gtuType GtuType; the GtuType to add to the compatible set of this Compatibility
     * @return GTUCompatibility&lt;I&gt;; this GtuCompatibility for method call chaining
     * @throws NullPointerException when <code>gtuType</code> is null
     * @throws OtsRuntimeException when changes are made to compatibility after results have been cached
     */
    public final GtuCompatibility<I> addCompatibleGtuType(final GtuType gtuType) throws NullPointerException
    {
        Throw.whenNull(gtuType, "gtuType may not be null");
        clearCompatibilityCache();
        this.levelCompatibilityMap.put(gtuType, true);
        return this;
    }

    /**
     * Add a incompatible GtuType to this GtuCompatibility.
     * @param gtuType GtuType; the GtuType to add to the incompatible set of this Compatibility
     * @return GTUCompatibility&lt;I&gt;; this GtuCompatibility for method call chaining
     * @throws NullPointerException when <code>gtuType</code> is null
     * @throws OtsRuntimeException when changes are made to compatibility after results have been cached
     */
    public final GtuCompatibility<I> addIncompatibleGtuType(final GtuType gtuType) throws NullPointerException
    {
        Throw.whenNull(gtuType, "gtuType may not be null");
        clearCompatibilityCache();
        this.levelCompatibilityMap.put(gtuType, false);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public I getInfrastructure()
    {
        return this.infrastructure;
    }

    /** {@inheritDoc} */
    @Override
    public void clearCompatibilityCache()
    {
        this.cachedCompatibilityMap.clear();
        for (I infra: getInfrastructure().getChildren())
        {
            infra.clearCompatibilityCache();
        }
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode()
    {
        return Objects.hash(this.infrastructure, this.levelCompatibilityMap);
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:needbraces")
    public boolean equals(final Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        GtuCompatibility<?> other = (GtuCompatibility<?>) obj;
        return Objects.equals(this.infrastructure, other.infrastructure)
                && Objects.equals(this.levelCompatibilityMap, other.levelCompatibilityMap);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "GtuCompatibility [levelCompatibilityMap=" + this.levelCompatibilityMap + "]";
    }

}
