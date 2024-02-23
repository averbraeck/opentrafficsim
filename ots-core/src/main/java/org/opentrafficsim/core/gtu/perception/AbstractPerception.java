package org.opentrafficsim.core.gtu.perception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.NetworkException;

/**
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 * @param <G> GTU type
 */
public abstract class AbstractPerception<G extends Gtu> implements Perception<G>
{

    /** */
    private static final long serialVersionUID = 20160729L;

    /** Set of available perception categories. */
    private final Map<Class<? extends PerceptionCategory<?, ?>>, PerceptionCategory<?, ?>> perceptionCategories =
            new LinkedHashMap<>();

    /** Cache, avoiding loop and isAssignableFrom. */
    private final Map<Class<? extends PerceptionCategory<?, ?>>, PerceptionCategory<?, ?>> cachedCategories =
            new LinkedHashMap<>();

    /** GTU. */
    private G gtu;

    /**
     * Construct perception.
     * @param gtu G; GTU
     */
    public AbstractPerception(final G gtu)
    {
        this.gtu = gtu;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public G getGtu()
    {
        return this.gtu;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public final <T extends PerceptionCategory<?, ?>> void addPerceptionCategory(final T perceptionCategory)
    {
        // guarantees correct combination of class and perception category
        this.perceptionCategories.put((Class<T>) perceptionCategory.getClass(), perceptionCategory);
        this.cachedCategories.clear();
    }

    /** {@inheritDoc} */
    @Override
    public final <T extends PerceptionCategory<?, ?>> boolean contains(final Class<T> category)
    {
        if (this.cachedCategories.containsKey(category))
        {
            return true;
        }
        for (Class<?> clazz : this.perceptionCategories.keySet())
        {
            if (category.isAssignableFrom(clazz))
            {
                // isAssignableFrom takes care of implementation of the category
                this.cachedCategories.put(category, this.perceptionCategories.get(clazz));
                return true;
            }
        }
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public final <T extends PerceptionCategory<?, ?>> T getPerceptionCategory(final Class<T> category)
            throws OperationalPlanException
    {
        T cat = getPerceptionCategoryOrNull(category);
        if (cat != null)
        {
            return cat;
        }
        throw new OperationalPlanException("Perception category" + category + " is not present.");
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("unchecked")
    public final <T extends PerceptionCategory<?, ?>> T getPerceptionCategoryOrNull(final Class<T> category)
    {
        T implementation = (T) this.cachedCategories.get(category);
        if (implementation != null)
        {
            return implementation;
        }
        for (Class<?> clazz : this.perceptionCategories.keySet())
        {
            if (category.isAssignableFrom(clazz))
            {
                // addPerceptionCategory guarantees correct combination of class and perception category
                // isAssignableFrom takes care of implementation of the category
                implementation = (T) this.perceptionCategories.get(clazz);
                this.cachedCategories.put(category, implementation);
                return implementation;
            }
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public final void removePerceptionCategory(final PerceptionCategory<?, ?> perceptionCategory)
    {
        for (Class<?> category : this.perceptionCategories.keySet())
        {
            if (perceptionCategory.getClass().isAssignableFrom(category))
            {
                // addPerceptionCategory guarantees correct combination of class and perception category
                // isAssignableFrom takes care of implementation of the category
                this.perceptionCategories.remove(perceptionCategory.getClass());
                this.cachedCategories.clear();
                return;
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public void perceive() throws GtuException, NetworkException, ParameterException
    {
        for (PerceptionCategory<?, ?> category : this.perceptionCategories.values())
        {
            category.updateAll();
        }
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        StringBuilder s = new StringBuilder("Perception [");
        String sep = "";
        for (PerceptionCategory<?, ?> cat : this.perceptionCategories.values())
        {
            s.append(sep);
            s.append(cat);
            sep = ", ";
        }
        s.append("]");
        return s.toString();
    }

}
