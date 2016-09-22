package org.opentrafficsim.road.gtu.strategical.od;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.opentrafficsim.core.Throw;

/**
 * A categorization determines for what part of traffic certain demand data is applicable. By default, this is always for a
 * given origin-destination pair and time period. A categorization adds to this additional segregation. For example, per lane,
 * per vehicle class, etc.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Sep 15, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class Categorization implements Serializable
{

    /** Empty categorization. */
    public static final Categorization UNCATEGORIZED = new Categorization("Uncategorized");
    
    /** */
    private static final long serialVersionUID = 20160921L;
    
    /** Id. */
    private final String id;
    
    /** Set of categorization classes. */
    private final List<Class<?>> classes = new ArrayList<>();
    
    /**
     * @param id id
     */
    private Categorization(final String id)
    {
        Throw.whenNull(id, "Id may not be null.");
        this.id = id;
    }
    
    /**
     * @param id Id
     * @param class1 1st class
     * @param classes other classes
     * @throws IllegalArgumentException if any class is given multiple times
     * @throws NullPointerException if any input is null
     */
    public Categorization(final String id, final Class<?> class1, final Class<?>... classes)
    {
        this(id);
        Throw.whenNull(class1, "Classes may not be null.");
        this.classes.add(class1);
        for (Class<?> clazz : classes)
        {
            Throw.whenNull(clazz, "Classes may not be null.");
            Throw.when(this.classes.contains(clazz), IllegalArgumentException.class,
                "Class %s is given multiple times.", clazz);
            this.classes.add(clazz);
        }
    }

    /**
     * Returns the number of category classes defined.
     * @return number of category classes defined
     */
    public final int size()
    {
        return this.classes.size();
    }
    
    /**
     * Returns the i'th class.
     * @param i index of the class
     * @return the i'th class
     * @throws IndexOutOfBoundsException if index i is out of bounds
     */
    public final Class<?> get(final int i)
    {
        Throw.when(i < 0 || i >= size(), IndexOutOfBoundsException.class,
            "Index %d is out of range for categorization of size %d.", i, size());
        return this.classes.get(i);
    }

    /**
     * @return id.
     */
    public final String getId()
    {
        return this.id;
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
        return "Categorization [id=" + this.id + ", classes=" + this.classes + "]";
    }

}
