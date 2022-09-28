package org.opentrafficsim.core.compatibility;

import org.opentrafficsim.base.HierarchicalType;
import org.opentrafficsim.base.OTSRuntimeException;
import org.opentrafficsim.core.gtu.GTUType;

/**
 * GtuCompatibleInfraType is a HierarchicalType that implements the delegation methods for GTU compatibility.
 * <p>
 * Copyright (c) 2022-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 * @param <I> The infrastructure type
 */
public abstract class GtuCompatibleInfraType<I extends GtuCompatibleInfraType<I>> extends HierarchicalType<I>
        implements Compatibility<GTUType, I>
{
    /** Local cache to delegate compatibility to. */
    private GtuCompatibility<I> gtuCompatibility;

    /**
     * Instantiate an infrastructure type.
     * @param id String; the id
     * @param parent InfraType; the parent
     */
    @SuppressWarnings("unchecked")
    public GtuCompatibleInfraType(final String id, final I parent)
    {
        super(id, parent);
        this.gtuCompatibility = new GtuCompatibility<>((I) this);
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
     * Add an compatible GTUType to this GtuCompatibility.
     * @param gtuType GTUType; the GTUType to add to the compatible set of this Compatibility
     * @return GtuCompatibility&lt;I&gt;; this GtuCompatibility for method call chaining
     * @throws NullPointerException when <code>gtuType</code> is null
     * @throws OTSRuntimeException when changes are made to compatibility after results have been cached
     */
    public final GtuCompatibility<I> addCompatibleGTUType(final GTUType gtuType)
    {
        // delegate to GtuCompatibility
        return this.gtuCompatibility.addCompatibleGTUType(gtuType);
    }

    /**
     * Add a incompatible GTUType to this GtuCompatibility.
     * @param gtuType GTUType; the GTUType to add to the incompatible set of this Compatibility
     * @return GtuCompatibility&lt;I&gt;; this GtuCompatibility for method call chaining
     * @throws NullPointerException when <code>gtuType</code> is null
     * @throws OTSRuntimeException when changes are made to compatibility after results have been cached
     */
    public final GtuCompatibility<I> addIncompatibleGTUType(final GTUType gtuType)
    {
        // delegate to GtuCompatibility
        return this.gtuCompatibility.addIncompatibleGTUType(gtuType);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isCompatible(final GTUType gtuType)
    {
        // delegate to GtuCompatibility
        return this.gtuCompatibility.isCompatible(gtuType);
    }

    /** {@inheritDoc} */
    @Override
    public Boolean isCompatibleOnInfraLevel(final GTUType gtuType)
    {
        // delegate to GtuCompatibility
        return this.gtuCompatibility.isCompatibleOnInfraLevel(gtuType);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public I getInfrastructure()
    {
        return (I) this;
    }

    /** {@inheritDoc} */
    @Override
    public void clearCompatibilityCache()
    {
        // delegate to GtuCompatibility
        this.gtuCompatibility.clearCompatibilityCache();
    }

}
