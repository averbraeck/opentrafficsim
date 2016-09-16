package org.opentrafficsim.road.gtu.strategical.od;

import java.util.ArrayList;
import java.util.List;

import org.opentrafficsim.core.Throw;

/**
 * A category is a set of objects who's class belongs to a certain categorization. One {@code Category} object can specify to
 * which subset of traffic between on origin and destination certain demand data belongs.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Sep 15, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class Category
{

    /** Set of categorization classes. */
    private final List<Object> objects = new ArrayList<>();

    /**
     * Adds given class to the categorization.
     * @param object class to add
     * @throws NullPointerException if clazz is {@code null}
     * @throws IllegalArgumentException if clazz is already in the categorization
     */
    public final void add(final Object object)
    {
        Throw.whenNull(object, "Categorization class may not be empty.");
        Throw.when(this.objects.contains(object), IllegalArgumentException.class,
            "Class %s is already in the categorization", object);
        this.objects.add(object);
    }

    /**
     * Returns the number of category elements defined.
     * @return number of category elements defined
     */
    public final int size()
    {
        return this.objects.size();
    }

    /**
     * Returns the i'th category.
     * @param i index of the category
     * @return the i'th category
     */
    public final Object get(final int i)
    {
        Throw.when(i < 0 || i >= size(), IndexOutOfBoundsException.class,
            "Index %d is out of range for categorization of size %d", i, size());
        return this.objects.get(i);
    }

    /** {@inheritDoc} */
    @Override
    public final int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.objects == null) ? 0 : this.objects.hashCode());
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
        Category other = (Category) obj;
        if (this.objects == null)
        {
            if (other.objects != null)
            {
                return false;
            }
        }
        else if (!this.objects.equals(other.objects))
        {
            return false;
        }
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "Category " + this.objects;
    }

}
