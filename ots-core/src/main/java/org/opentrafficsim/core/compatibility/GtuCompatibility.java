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
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <T> infrastructure type, e.g. LinkType or LaneType, or water way type
 */
public class GtuCompatibility<T extends HierarchicalType<T, ?> & Compatibility<GtuType, T>> implements Compatibility<GtuType, T>
{
    /** The map of GtuTypes that define on this infrastructure level what GtuTypes are compatible or not. */
    private final Map<GtuType, Boolean> levelCompatibilityMap = new LinkedHashMap<>();

    /** The cached map of GtuTypes that have been resolved. */
    private final Map<GtuType, Boolean> cachedCompatibilityMap = new LinkedHashMap<>();

    /** Infrastructure for which this compatibility definition holds, e.g. a LinkType, a LaneType, or a SensorType. */
    private final T infrastructure;

    /**
     * Construct a new Compatibility object with empty compatible and forbidden sets.
     * @param infrastructure the infrastructure type, e.g. LinkType, LaneType, SensorType.
     */
    public GtuCompatibility(final T infrastructure)
    {
        Throw.whenNull(infrastructure, "infrastructure cannot be null");
        this.infrastructure = infrastructure;
    }

    /**
     * Construct a new Compatibility and deep copy the compatible and forbidden sets from an existing Compatibility.
     * @param original the Compatibility from which the compatible and forbidden sets will be copied
     */
    public GtuCompatibility(final GtuCompatibility<T> original)
    {
        this.infrastructure = original.infrastructure;
        this.levelCompatibilityMap.putAll(original.levelCompatibilityMap);
    }

    /**
     * Determine if this Compatibility allows or denies a particular GtuType.
     * @param gtuType the GtuType to check
     * @return true if the GtuType is compatible; false if the GtuType is not compatible
     */
    @Override
    public boolean isCompatible(final GtuType gtuType)
    {
        if (!this.cachedCompatibilityMap.containsKey(gtuType))
        {
            boolean foundTrue = false;
            boolean foundFalse = false;
            T infra = this.infrastructure;
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
                    gType = gType.getParent().orElse(null);
                }
                infra = infra.getParent().orElse(null);
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

    @Override
    public Boolean isCompatibleOnInfraLevel(final GtuType gtuType)
    {
        return this.levelCompatibilityMap.get(gtuType);
    }

    /**
     * Add an compatible GtuType to this GtuCompatibility.
     * @param gtuType the GtuType to add to the compatible set of this Compatibility
     * @return this GtuCompatibility for method call chaining
     * @throws NullPointerException when <code>gtuType</code> is null
     * @throws OtsRuntimeException when changes are made to compatibility after results have been cached
     */
    public final GtuCompatibility<T> addCompatibleGtuType(final GtuType gtuType) throws NullPointerException
    {
        Throw.whenNull(gtuType, "gtuType may not be null");
        clearCompatibilityCache();
        this.levelCompatibilityMap.put(gtuType, true);
        return this;
    }

    /**
     * Add a incompatible GtuType to this GtuCompatibility.
     * @param gtuType the GtuType to add to the incompatible set of this Compatibility
     * @return this GtuCompatibility for method call chaining
     * @throws NullPointerException when <code>gtuType</code> is null
     * @throws OtsRuntimeException when changes are made to compatibility after results have been cached
     */
    public final GtuCompatibility<T> addIncompatibleGtuType(final GtuType gtuType) throws NullPointerException
    {
        Throw.whenNull(gtuType, "gtuType may not be null");
        clearCompatibilityCache();
        this.levelCompatibilityMap.put(gtuType, false);
        return this;
    }

    @Override
    public T getInfrastructure()
    {
        return this.infrastructure;
    }

    @Override
    public void clearCompatibilityCache()
    {
        this.cachedCompatibilityMap.clear();
        for (T infra : getInfrastructure().getChildren())
        {
            infra.clearCompatibilityCache();
        }
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.infrastructure, this.levelCompatibilityMap);
    }

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

    @Override
    public final String toString()
    {
        return "GtuCompatibility [levelCompatibilityMap=" + this.levelCompatibilityMap + "]";
    }

}
