package org.opentrafficsim.base;

/**
 * HierarchicallyTyped is the interface of objects that are of a HierarchicalType. Examples are Gtu (of type GtuType),
 * Link (of type LinkType), and Detector (of type DetectorType). By making these objects HierarchicallyTyped, they can 
 * return their correct 'type' as well as check whether they belong to a certain hierarchical type. 
 * <p>
 * Copyright (c) 2022-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 * @param <T> The HierarchicalType of the typing object
 * @param <I> The type of the typed object
 */
public interface HierarchicallyTyped<T extends HierarchicalType<T, I>, I extends HierarchicallyTyped<T, I>>
{
    /**
     * Return the assigned type of the typed object.
     * @return T; the assigned type of the typed object
     */
    T getType();
    
    /**
     * Return whether the object if has type 'type', or one of the subtypes of 'type'. An example is
     * <code>gtu.isOfType(Types.BUS)</code> or <code>link.isOfType(Types.HIGHWAY)</code>.
     * @param type T; the type to check against
     * @return boolean; whether the object if has type 'type', or one of the subtypes of 'type'
     */
    default boolean isOfType(final T type)
    {
        return getType().isOfType(type);
    }
}
