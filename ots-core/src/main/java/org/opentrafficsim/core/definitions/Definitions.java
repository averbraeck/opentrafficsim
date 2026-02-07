package org.opentrafficsim.core.definitions;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.djutils.exceptions.Throw;
import org.djutils.immutablecollections.Immutable;
import org.djutils.immutablecollections.ImmutableHashMap;
import org.djutils.immutablecollections.ImmutableMap;
import org.opentrafficsim.base.HierarchicalType;

/**
 * The Definitions interface contains access to the core definitions that can be used to interpret the Network and the
 * PerceivableContext. Example interfaces allow the retrieval of GtuTypes and LinkTypes.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class Definitions
{

    /**
     * Constructor.
     */
    public Definitions()
    {
        //
    }

    /** Map of maps of types per id. */
    private Map<Class<? extends HierarchicalType<?, ?>>, Map<String, HierarchicalType<?, ?>>> typeMap = new LinkedHashMap<>();

    /**
     * Add a type (e.g. a GtuType instance).
     * @param <T> type type (e.g. GtuType).
     * @param typeClass class of type (e.g. GtuType.class).
     * @param t type instance (e.g DefaultsNl.CAR).
     */
    public <T extends HierarchicalType<T, ?>> void add(final Class<T> typeClass, final T t)
    {
        Throw.whenNull(typeClass, "Type class may not be null.");
        Throw.whenNull(t, "Type may not be null.");
        this.typeMap.computeIfAbsent(typeClass, (key) -> Collections.synchronizedMap(new LinkedHashMap<>())).put(t.getId(), t);
    }

    /**
     * Obtain a type by its id.
     * @param <T> type type (e.g. GtuType).
     * @param typeClass class of type (e.g. GtuType.class).
     * @param id id of the class.
     * @return instance with given id, empty if it is not present.
     */
    @SuppressWarnings("unchecked")
    public <T extends HierarchicalType<T, ?>> Optional<T> get(final Class<T> typeClass, final String id)
    {
        Throw.whenNull(typeClass, "Type class may not be null.");
        Throw.whenNull(id, "Id may not be null.");
        return Optional.ofNullable((T) this.typeMap
                .computeIfAbsent(typeClass, (key) -> Collections.synchronizedMap(new LinkedHashMap<>())).get(id));
    }

    /**
     * Obtain a type by its id.
     * @param <T> type type (e.g. GtuType).
     * @param typeClass class of type (e.g. GtuType.class).
     * @param id id of the class.
     * @return instance with given id, empty if it is not present.
     * @throws NoSuchElementException when the type is not defined
     */
    public <T extends HierarchicalType<T, ?>> T getOrThrow(final Class<T> typeClass, final String id)
    {
        return get(typeClass, id).orElseThrow(
                () -> new NoSuchElementException("Type " + typeClass.getSimpleName() + " not defined for id " + id));
    }

    /**
     * Obtain all present type of given type type.
     * @param <T> type type (e.g. GtuType).
     * @param typeClass class of type (e.g. GtuType.class).
     * @return map of all types of give type type, empty if there are no types of the type type.
     */
    @SuppressWarnings("unchecked")
    public <T extends HierarchicalType<T, ?>> ImmutableMap<String, T> getAll(final Class<T> typeClass)
    {
        Throw.whenNull(typeClass, "Type class may not be null.");
        return new ImmutableHashMap<>((Map<String, T>) this.typeMap.computeIfAbsent(typeClass,
                (key) -> Collections.synchronizedMap(new LinkedHashMap<>())), Immutable.WRAP);
    }

}
