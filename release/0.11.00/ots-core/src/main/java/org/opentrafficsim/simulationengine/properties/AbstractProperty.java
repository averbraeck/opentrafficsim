package org.opentrafficsim.simulationengine.properties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Abstract property.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version 18 dec. 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @param <T> type of the property
 */
public abstract class AbstractProperty<T> implements Property<T>, Iterable<AbstractProperty<T>>, Serializable
{
    /** */
    private static final long serialVersionUID = 20150000L;

    /** Key of this property. */
    private final String key;

    /** Determines sorting order when properties are displayed to the user. */
    private final int displayPriority;

    /** The shortName of the property. */
    private String shortName;

    /** The description of the property. */
    private String description;

    /** The property is read-only. */
    private Boolean readOnly = null;

    /** Parent of this AbstractProperty. */
    private CompoundProperty parentProperty = null;

    /**
     * Construct a new AbstractProperty.
     * @param key String; unique (within this property tree) name of the new AbstractProperty
     * @param displayPriority sorting order when properties are displayed to the user
     * @param shortName String; concise description of the property
     * @param description String; long description of the property (may use HTML markup)
     */
    public AbstractProperty(final String key, final int displayPriority, final String shortName, final String description)
    {
        this.key = key;
        this.displayPriority = displayPriority;
        this.shortName = shortName;
        this.description = description;
    }
    
    /**
     * Finalize the readOnly flag.
     * @param readOnlyValue the readonly property value to set
     */
    protected final void setReadOnly(final boolean readOnlyValue)
    {
        this.readOnly = readOnlyValue;
    }
    
    /** {@inheritDoc} */
    @Override
    public final int getDisplayPriority()
    {
        return this.displayPriority;
    }

    /** {@inheritDoc} */
    @Override
    public abstract String htmlStateDescription();

    /** {@inheritDoc} */
    @Override
    public final Iterator<AbstractProperty<T>> iterator()
    {
        return new PropertyIterator(this);
    }

    /** {@inheritDoc} */
    @Override
    public final String getShortName()
    {
        return this.shortName;
    }

    /** {@inheritDoc} */
    @Override
    public final String getDescription()
    {
        return this.description;
    }

    /** {@inheritDoc} */
    @Override
    public final boolean isReadOnly()
    {
        return null != this.readOnly && this.readOnly;
    }

    /**
     * Retrieve the key of this AbstractProperty.
     * @return String; the key of this AbstractProperty
     */
    public final String getKey()
    {
        return this.key;
    }
    
    /**
     * Retrieve an AbstractProperty anywhere in this group that has the specified key.
     * @param propertyKey String; the key
     * @return AbstractProperty; the matching AbstractProperty, or null if no property with the specified key exists in the
     *         group.
     */
    public final AbstractProperty<?> findByKey(final String propertyKey)
    {
        if (this.key.equals(propertyKey))
        {
            return this;
        }
        if (this instanceof CompoundProperty)
        {
            return ((CompoundProperty) this).getPropertyGroup().get(propertyKey);
        }
        if (null == getParent())
        {
            return null;
        }
        return getParent().getPropertyGroup().get(propertyKey);
    }

    /**
     * Set the parent of this AbstractProperty.
     * @param newParent AbstractProperty&lt;?&gt;; the new parent of this AbstractProperty
     */
    protected final void setParent(final CompoundProperty newParent)
    {
        this.parentProperty = newParent;
    }
    
    /**
     * Retrieve the parent property.
     * @return AbstractProperty&lt;?&gt;; the CompoundProperty that is the parent of this AbstractProperty (result is null if this property is not contained in a CompoundProperty)
     */
    protected final CompoundProperty getParent()
    {
        return this.parentProperty;
    }

    /** {@inheritDoc} */
    public final String toString()
    {
        return this.getShortName();
    }

    /**
     * Really simple iterator for properties.
     * <p>
     * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * <p>
     * $LastChangedDate$, @version $Revision$, by $Author$,
     * initial version jan. 2015 <br>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     */
    class PropertyIterator implements Iterator<AbstractProperty<T>>, Serializable
    {
        /** */
        private static final long serialVersionUID = 20150000L;

        /** Next in line in the main CompoundProperty. */
        private int currentIndex;

        /** Full list of AbstractProperties. */
        private final ArrayList<AbstractProperty<T>> list;

        /**
         * Construct a new PropertyIterator.
         * @param ap AbstractProperty; root of the tree to iterate over
         */
        PropertyIterator(final AbstractProperty<T> ap)
        {
            this.currentIndex = 0;
            this.list = new ArrayList<AbstractProperty<T>>();
            addToList(ap);
        }

        /**
         * Recursively add all properties to the list. <br>
         * Compound properties are included <b>before</b> their contents.
         * @param cp AbstractProperty&lt;T&gt;; the property to add (if compound it and all it's children are added)
         */
        @SuppressWarnings("unchecked")
        private void addToList(final AbstractProperty<T> cp)
        {
            this.list.add(cp);
            if (cp instanceof CompoundProperty)
            {
                for (AbstractProperty<?> ap : ((CompoundProperty) cp).getValue())
                {
                    addToList((AbstractProperty<T>) ap);
                }
            }
        }

        /** {@inheritDoc} */
        @Override
        public boolean hasNext()
        {
            return this.currentIndex < this.list.size();
        }

        /** {@inheritDoc} */
        @Override
        public AbstractProperty<T> next()
        {
            return this.list.get(this.currentIndex++);
        }

        /** {@inheritDoc} */
        @Override
        public void remove()
        {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return "PropertyIterator [currentIndex=" + this.currentIndex + ", list=" + this.list + "]";
        }

    }

}
