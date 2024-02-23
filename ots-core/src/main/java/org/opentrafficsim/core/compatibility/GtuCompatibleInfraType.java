package org.opentrafficsim.core.compatibility;

import org.opentrafficsim.base.HierarchicalType;
import org.opentrafficsim.base.HierarchicallyTyped;
import org.opentrafficsim.base.OtsRuntimeException;
import org.opentrafficsim.core.gtu.GtuType;

/**
 * GtuCompatibleInfraType is a HierarchicalType that implements the delegation methods for GTU compatibility.
 * <p>
 * Copyright (c) 2022-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 * @param <T> The infrastructure type
 * @param <I> Infrastructure type belonging to hierarchical type
 */
public abstract class GtuCompatibleInfraType<T extends GtuCompatibleInfraType<T, I>, I extends HierarchicallyTyped<T, I>>
        extends HierarchicalType<T, I> implements Compatibility<GtuType, T>
{
    /** */
    private static final long serialVersionUID = 20220928L;

    /** Local cache to delegate compatibility to. */
    private GtuCompatibility<T> gtuCompatibility;

    /**
     * Instantiate an infrastructure type.
     * @param id String; the id
     * @param parent InfraType; the parent
     */
    @SuppressWarnings("unchecked")
    public GtuCompatibleInfraType(final String id, final T parent)
    {
        super(id, parent);
        this.gtuCompatibility = new GtuCompatibility<>((T) this);
    }

    /**
     * Instantiate an infrastructure type without a parent.
     * @param id String; the id
     */
    public GtuCompatibleInfraType(final String id)
    {
        this(id, null);
    }

    /**
     * Add an compatible GtuType to this GtuCompatibility.
     * @param gtuType GtuType; the GtuType to add to the compatible set of this Compatibility
     * @return GtuCompatibility&lt;I&gt;; this GtuCompatibility for method call chaining
     * @throws NullPointerException when <code>gtuType</code> is null
     * @throws OtsRuntimeException when changes are made to compatibility after results have been cached
     */
    public final GtuCompatibility<T> addCompatibleGtuType(final GtuType gtuType)
    {
        // delegate to GtuCompatibility
        return this.gtuCompatibility.addCompatibleGtuType(gtuType);
    }

    /**
     * Add a incompatible GtuType to this GtuCompatibility.
     * @param gtuType GtuType; the GtuType to add to the incompatible set of this Compatibility
     * @return GtuCompatibility&lt;I&gt;; this GtuCompatibility for method call chaining
     * @throws NullPointerException when <code>gtuType</code> is null
     * @throws OtsRuntimeException when changes are made to compatibility after results have been cached
     */
    public final GtuCompatibility<T> addIncompatibleGtuType(final GtuType gtuType)
    {
        // delegate to GtuCompatibility
        return this.gtuCompatibility.addIncompatibleGtuType(gtuType);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isCompatible(final GtuType gtuType)
    {
        // delegate to GtuCompatibility
        return this.gtuCompatibility.isCompatible(gtuType);
    }

    /** {@inheritDoc} */
    @Override
    public Boolean isCompatibleOnInfraLevel(final GtuType gtuType)
    {
        // delegate to GtuCompatibility
        return this.gtuCompatibility.isCompatibleOnInfraLevel(gtuType);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public T getInfrastructure()
    {
        return (T) this;
    }

    /** {@inheritDoc} */
    @Override
    public void clearCompatibilityCache()
    {
        // delegate to GtuCompatibility
        this.gtuCompatibility.clearCompatibilityCache();
    }

    /**
     * Return the GTU compatibility of this infra type.
     * @return gtuCompatibility GtuCompatibility&lt;I&gt;; the GTU compatibility of this infra type
     */
    public GtuCompatibility<T> getGtuCompatibility()
    {
        return this.gtuCompatibility;
    }

}
