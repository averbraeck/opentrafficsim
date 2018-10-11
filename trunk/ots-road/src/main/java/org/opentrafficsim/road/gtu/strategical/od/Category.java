package org.opentrafficsim.road.gtu.strategical.od;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import nl.tudelft.simulation.language.Throw;

/**
 * A category is a set of objects who's class belongs to a certain categorization. One {@code Category} object can specify to
 * which subset of traffic between on origin and destination certain demand data belongs.
 * <p>
 * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Sep 15, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class Category implements Serializable
{

    /** Empty category. */
    public static final Category UNCATEGORIZED = new Category(Categorization.UNCATEGORIZED);

    /** */
    private static final long serialVersionUID = 20160921L;

    /** Categorization. */
    private final Categorization categorization;

    /** List of objects defining the category. */
    private final List<Object> objects = new ArrayList<>();

    /**
     * @param categorization Categorization; categorization
     */
    private Category(final Categorization categorization)
    {
        Throw.whenNull(categorization, "Categorization may not be null.");
        this.categorization = categorization;
    }

    /**
     * @param categorization Categorization; categorization
     * @param object1 Object; 1st object
     * @param objects Object...; other objects
     * @throws IllegalArgumentException if the objects do not comply with the categorization
     * @throws NullPointerException if any input is null
     */
    public Category(final Categorization categorization, final Object object1, final Object... objects)
    {
        this(categorization);
        Throw.when(categorization.size() != objects.length + 1, IllegalArgumentException.class,
                "Objects do not comply with the categorization; bad number of objects.");
        Throw.whenNull(object1, "Objects may not be null.");
        Throw.when(!categorization.get(0).isAssignableFrom(object1.getClass()), IllegalArgumentException.class,
                "Objects do not comply with the categorization; object 1 is of type %s, should be %s.", object1.getClass(),
                categorization.get(0));
        for (int i = 1; i < categorization.size(); i++)
        {
            Throw.whenNull(objects[i - 1], "Objects may not be null.");
            Throw.when(!categorization.get(i).isAssignableFrom(objects[i - 1].getClass()), IllegalArgumentException.class,
                    "Objects do not comply with the categorization; object %d is of type %s, should be %s.", i + 1,
                    objects[i - 1].getClass(), categorization.get(i));
        }
        this.objects.add(object1);
        for (Object object : objects)
        {
            this.objects.add(object);
        }
    }

    /**
     * Returns the i'th object.
     * @param i int; index of the object
     * @return the i'th object
     * @throws IndexOutOfBoundsException if index i is out of bounds
     */
    public final Object get(final int i)
    {
        Throw.when(i < 0 || i >= this.objects.size(), IndexOutOfBoundsException.class,
                "Index %d is out of range for categorization of size %d", i, this.objects.size());
        return this.objects.get(i);
    }

    /**
     * @return categorization.
     */
    public final Categorization getCategorization()
    {
        return this.categorization;
    }

    /**
     * Returns the object of this category pertaining to the specified class from the category.
     * @param clazz Class&lt;T&gt;; class from categorization to get the category object for
     * @param <T> type of the object
     * @return Object; object of this category pertaining to the specified class from the category
     */
    @SuppressWarnings("unchecked")
    public final <T> T get(final Class<T> clazz)
    {
        Throw.when(!this.categorization.entails(clazz), RuntimeException.class,
                "Trying to get a categorization object by class, with the class not belonging to the categorization.");
        for (Object obj : this.objects)
        {
            if (clazz.isAssignableFrom(obj.getClass()))
            {
                return (T) obj; // assignable so safe
            }
        }
        throw new RuntimeException(
                "Categorization contains no object assignable to given class, even though the categorization entails the class.");
    }

    /** {@inheritDoc} */
    @Override
    public final int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.categorization == null) ? 0 : this.categorization.hashCode());
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
        if (this.categorization == null)
        {
            if (other.categorization != null)
            {
                return false;
            }
        }
        else if (!this.categorization.equals(other.categorization))
        {
            return false;
        }
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
        return "Category [" + this.objects + "]";
    }

}
