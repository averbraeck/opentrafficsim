package org.opentrafficsim.simulationengine;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Abstract property.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 18 dec. 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @param <T> type of the property
 */
public abstract class AbstractProperty<T> implements Iterable<AbstractProperty<T>>
{
    /** Determines sorting order when properties are displayed to the user. */
    private final int displayPriority;

    /**
     * @param displayPriority sorting order when properties are displayed to the user.
     */
    public AbstractProperty(final int displayPriority)
    {
        super();
        this.displayPriority = displayPriority;
    }

    /**
     * Retrieve the current value of the property.
     * @return T; the current value of the property
     */
    public abstract T getValue();

    /**
     * Return a short description of the property.
     * @return String; a short description of the property
     */
    public abstract String getShortName();

    /**
     * Return a description of the property (may use HTML markup).
     * @return String; the description of the property
     */
    public abstract String getDescription();

    /**
     * Change the value of the property.
     * @param newValue T; the new value for the property
     * @throws PropertyException when this Property is read-only, or newValue is not valid
     */
    public abstract void setValue(T newValue) throws PropertyException;

    /**
     * Return true if the property can not be altered.
     * @return boolean; true if this property can not be altered, false if this property can be altered
     */
    public abstract boolean isReadOnly();

    /**
     * Display priority determines the order in which properties should be displayed. Properties with lower values
     * should be displayed above or before those with higher values.
     * @return int; the display priority of this AbstractProperty
     */
    public final int getDisplayPriority()
    {
        return this.displayPriority;
    }

    /**
     * Generate a description of the state of this property in HTML (excluding the &lt;html&gt; at the start and the
     * &lt;/html&gt; at the end. The result can be embedded in a html-table.
     * @return String; the description of this property and the current state in HTML
     */
    public abstract String HTMLStateDescription();

    /** {@inheritDoc} */
    @Override
    public final Iterator<AbstractProperty<T>> iterator()
    {
        return new PropertyIterator(this);
    }

    /** {@inheritDoc} */
    public final String toString()
    {
        return this.getShortName();
    }

    /**
     * Really simple iterator for properties.
     * <p>
     * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
     * reserved. <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version 6 jan. 2015 <br>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     */
    class PropertyIterator implements Iterator<AbstractProperty<T>>
    {
        /** Next in line in the main CompoundProperty. */
        private int currentIndex;

        /** Full list of AbstractProperties. */
        private final ArrayList<AbstractProperty<T>> list;

        /**
         * Construct a new PropertyIterator.
         * @param ap AbstractProperty; root of the tree to iterate over
         */
        public PropertyIterator(final AbstractProperty<T> ap)
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

    }

}
