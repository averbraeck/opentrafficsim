package org.opentrafficsim.base.modelproperties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Abstract property.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2016-05-28 11:33:31 +0200 (Sat, 28 May 2016) $, @version $Revision: 2051 $, by $Author: averbraeck $,
 * initial version 18 dec. 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @param <T> type of the property
 */
public abstract class AbstractProperty<T> implements Property<T>, Serializable
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
    public final Iterator<Property<?>> iterator()
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

    /** {@inheritDoc} */
    @Override
    public final Property<?> findByKey(final String propertyKey)
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

    /** {@inheritDoc} */
    @Override
    public final CompoundProperty getParent()
    {
        return this.parentProperty;
    }

    /** {@inheritDoc} */
    @Override
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
     * $LastChangedDate: 2016-05-28 11:33:31 +0200 (Sat, 28 May 2016) $, @version $Revision: 2051 $, by $Author: averbraeck $,
     * initial version jan. 2015 <br>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     */
    class PropertyIterator implements Iterator<Property<?>>, Serializable
    {
        /** */
        private static final long serialVersionUID = 20150000L;

        /** Next in line in the main CompoundProperty. */
        private int currentIndex;

        /** Full list of AbstractProperties. */
        private final ArrayList<Property<?>> list;

        /**
         * Construct a new PropertyIterator.
         * @param ap AbstractProperty; root of the tree to iterate over
         */
        PropertyIterator(final Property<T> ap)
        {
            this.currentIndex = 0;
            this.list = new ArrayList<Property<?>>();
            addToList(ap);
        }

        /**
         * Recursively add all properties to the list. <br>
         * Compound properties are included <b>before</b> their contents.
         * @param cp AbstractProperty&lt;T&gt;; the property to add (if compound it and all it's children are added)
         */
        private void addToList(final Property<?> cp)
        {
            this.list.add(cp);
            if (cp instanceof CompoundProperty)
            {
                for (Property<?> ap : ((CompoundProperty) cp).getValue())
                {
                    addToList(ap);
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
        public Property<?> next()
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
