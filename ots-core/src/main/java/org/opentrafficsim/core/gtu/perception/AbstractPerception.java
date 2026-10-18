package org.opentrafficsim.core.gtu.perception;

import java.util.Map;
import java.util.Optional;

import org.djutils.exceptions.Throw;
import org.opentrafficsim.base.collections.IndexedMap;
import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;

/**
 * Implements {@code Perception} by housing categories.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Alexander Verbraeck
 * @author Peter Knoppers
 * @author Wouter Schakel
 * @param <G> GTU type
 */
public abstract class AbstractPerception<G extends Gtu> implements Perception<G>
{

    /** Set of available perception categories. */
    private final Map<PerceptionCategory.Type<?>, PerceptionCategory<?, ?, ?>> perceptionCategories = new IndexedMap<>();

    /** GTU. */
    private G gtu;

    /**
     * Construct perception.
     * @param gtu GTU
     */
    public AbstractPerception(final G gtu)
    {
        this.gtu = gtu;
    }

    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public G getGtu()
    {
        return this.gtu;
    }

    @Override
    public <P extends Perception<? extends G>> void addPerceptionCategory(
            final PerceptionCategory<? extends G, ? extends P, ?> perceptionCategory)
    {
        // guarantees correct combination of class and perception category
        this.perceptionCategories.put(perceptionCategory.getType(), perceptionCategory);
    }

    @Override
    public boolean contains(final PerceptionCategory.Type<?> category)
    {
        return this.perceptionCategories.containsKey(category);
    }

    @Override
    public <T extends PerceptionCategory<?, ?, ?>> T getPerceptionCategory(final PerceptionCategory.Type<T> category)
            throws OperationalPlanException
    {
        Optional<T> cat = getPerceptionCategoryOptional(category);
        Throw.when(cat.isEmpty(), NullPointerException.class, "Perception category %s is not present.", category);
        return cat.orElseThrow(() -> new NullPointerException("Perception category " + category + " is not present."));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends PerceptionCategory<?, ?, ?>> Optional<T> getPerceptionCategoryOptional(
            final PerceptionCategory.Type<T> category)
    {
        return Optional.ofNullable((T) this.perceptionCategories.get(category));
    }

    @Override
    public void removePerceptionCategory(final PerceptionCategory<?, ?, ?> category)
    {
        this.perceptionCategories.remove(category.getType());
    }

    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        StringBuilder s = new StringBuilder("Perception [");
        String sep = "";
        for (PerceptionCategory<?, ?, ?> cat : this.perceptionCategories.values())
        {
            s.append(sep);
            s.append(cat);
            sep = ", ";
        }
        s.append("]");
        return s.toString();
    }

}
