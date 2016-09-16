package org.opentrafficsim.road.gtu.strategical.od;

import java.util.ArrayList;
import java.util.List;

import org.opentrafficsim.core.Throw;

/**
 * A categorization determines for what part of traffic certain demand data is applicable. By default, this is always for a
 * given origin-destination pair and time period. A categorization adds to this additional segregation. For example, per lane,
 * per vehicle class, etc.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Sep 15, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */

public class Categorization
{

    /** Set of categorization classes. */
    private final List<Class<?>> classes = new ArrayList<>();

    /**
     * Adds given class to the categorization.
     * @param clazz class to add
     * @throws NullPointerException if clazz is {@code null}
     * @throws IllegalArgumentException if clazz is already in the categorization
     */
    public final void add(final Class<?> clazz)
    {
        Throw.whenNull(clazz, "Categorization class may not be empty.");
        Throw.when(this.classes.contains(clazz), IllegalArgumentException.class,
            "Class %s is already in the categorization", clazz);
        this.classes.add(clazz);
    }

    /**
     * Returns whether there is no categorization defined.
     * @return whether there is no categorization defined
     */
    public final boolean isEmpty()
    {
        return this.classes.isEmpty();
    }

    /**
     * Returns the number of category elements defined.
     * @return number of category elements defined
     */
    public final int size()
    {
        return this.classes.size();
    }

    /**
     * Returns the i'th category.
     * @param i index of the category
     * @return the i'th category
     */
    public final Class<?> get(final int i)
    {
        Throw.when(i < 0 || i >= size(), IndexOutOfBoundsException.class,
            "Index %d is out of range for categorization of size %d.", i, size());
        return this.classes.get(i);
    }

    /**
     * Returns whether the given category complies with this categorization.
     * @param category category
     * @return whether the given category complies with this categorization
     */
    public final boolean complies(final Category category)
    {
        if (category.size() != size())
        {
            return false;
        }
        for (int i = 0; i < size(); i++)
        {
            if (!this.classes.get(i).isAssignableFrom(category.get(i).getClass()))
            {
                return false;
            }
        }
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public final int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.classes == null) ? 0 : this.classes.hashCode());
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
        Categorization other = (Categorization) obj;
        if (this.classes == null)
        {
            if (other.classes != null)
            {
                return false;
            }
        }
        else if (!this.classes.equals(other.classes))
        {
            return false;
        }
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "Categorization [classes=" + this.classes + "]";
    }

}
