package org.opentrafficsim.base.modelproperties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Compound property.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2016-05-28 11:33:31 +0200 (Sat, 28 May 2016) $, @version $Revision: 2051 $, by $Author: averbraeck $,
 * initial version 30 dec. 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class CompoundProperty extends AbstractProperty<List<Property<?>>> implements Serializable
{
    /** */
    private static final long serialVersionUID = 20150000L;

    /** Properties directly contained in this one. */
    private final List<Property<?>> value = new ArrayList<>();

    /** Map of all AbstractProperties known in this property group. */
    private Map<String, Property<?>> propertyGroup = new HashMap<>();

    /**
     * Construct a CompoundProperty.
     * @param key String; the unique key of the new property
     * @param shortName String; the short name of the new CompoundProperty
     * @param description String; description of the new CompoundProperty (may use HTML mark up)
     * @param initialValue Integer; the initial value of the new CompoundProperty
     * @param readOnly boolean; if true this CompoundProperty can not be altered
     * @param displayPriority int; the display priority of the new CompoundProperty
     * @throws PropertyException if <cite>key</cite> is already in use
     */
    public CompoundProperty(final String key, final String shortName, final String description,
            final List<Property<?>> initialValue, final boolean readOnly, final int displayPriority)
            throws PropertyException
    {
        super(key, displayPriority, shortName, description);
        if (null != initialValue)
        {
            for (Property<?> ap : initialValue)
            {
                add(ap);
            }
        }
        setReadOnly(readOnly);
    }

    /** {@inheritDoc} */
    @Override
    public final List<Property<?>> getValue()
    {
        return new ArrayList<Property<?>>(this.value); // return a defensive copy
    }

    /** {@inheritDoc} */
    @Override
    public final void setValue(final List<Property<?>> newValue) throws PropertyException
    {
        for (Property<?> ap : getValue())
        {
            remove(ap); // make good use of the fact that getValue makes a defensive copy
        }
        for (Property<?> ap : newValue)
        {
            add(ap);
        }
    }

    /**
     * Find an embedded Property that has a specified shortName. <br>
     * Return the first matching one, or null if none of the embedded AbstractProperties has the specified name.
     * @param key String; the key of the sought embedded Property
     * @return Property&lt;?&gt;; the first matching embedded AbstractProperty or null if there is no embedded
     *         Property with the specified name
     */
    public final Property<?> findSubPropertyByKey(final String key)
    {
        // System.out.println("Searching property " + name);
        Iterator<Property<?>> i = this.iterator();
        while (i.hasNext())
        {
            Property<?> ap = i.next();
            // System.out.println("Inspecting " + ap.getKey());
            if (ap.getKey().equals(key))
            {
                return ap;
            }
        }
        return null;
    }

    /**
     * Add a Property at a specified position.
     * @param index int; the position where the Property must be added
     * @param ap Property; the property to add
     * @throws PropertyException when this CompoundProperty is read-only, or index is out of range
     */
    public final void add(final int index, final Property<?> ap) throws PropertyException
    {
        if (isReadOnly())
        {
            throw new PropertyException("Cannot modify a read-only CompoundProperty");
        }
        if (index < 0 || index > this.value.size())
        {
            throw new PropertyException("index is out of range");
        }
        if (this.propertyGroup.containsKey(ap.getKey()))
        {
            throw new PropertyException("AbstractProperty " + ap + " is already registered in property group of " + this);
        }
        // Recursively verify that there are no collisions on the key
        for (Property<?> subProperty : ap)
        {
            if (this.propertyGroup.containsKey(subProperty.getKey()))
            {
                throw new PropertyException("A property with key " + subProperty.getKey()
                        + " is already known in this property group");
            }
        }
        // Add all sub-properties to this property group
        for (Property<?> subProperty : ap)
        {
            this.propertyGroup.put(subProperty.getKey(), subProperty);
            if (subProperty instanceof CompoundProperty)
            {
                // Make compound sub properties share our property group map
                ((CompoundProperty) subProperty).setPropertyGroup(this.propertyGroup);
            }
        }
        this.value.add(index, ap);
        ((AbstractProperty<?>) ap).setParent(this);
    }

    /**
     * Add a Property at the end of the list.
     * @param ap Property; the property to add
     * @throws PropertyException when this CompoundProperty is read-only
     */
    public final void add(final Property<?> ap) throws PropertyException
    {
        add(this.value.size(), ap);
    }

    /**
     * Remove a sub property from this CompoundProperty.
     * @param index int; the position of the sub property to remove
     * @throws PropertyException when this CompoundProperty is read-only, or index is out of range
     */
    public final void remove(final int index) throws PropertyException
    {
        if (isReadOnly())
        {
            throw new PropertyException("Cannot modify a read-only CompoundProperty");
        }
        if (index < 0 || index >= this.value.size())
        {
            throw new PropertyException("index is out of range");
        }
        this.propertyGroup.remove(this.value.get(index));
        Property<?> removed = this.value.remove(index);
        ((AbstractProperty<?>) removed).setParent(null);
        if (removed instanceof CompoundProperty)
        {
            ((CompoundProperty) removed).setPropertyGroup(null); // let child CompoundProperty rebuild its property group
        }
    }

    /**
     * Remove a property from this CompoundProperty.
     * @param removeMe AbstractProperty the property that must be removed
     * @throws PropertyException when the supplied property cannot be removed (probably because it is not part of this
     *             CompoundProperty)
     */
    public final void remove(final Property<?> removeMe) throws PropertyException
    {
        int i = this.value.indexOf(removeMe);
        if (i < 0)
        {
            throw new PropertyException("Cannot remove property " + removeMe
                    + " because it is not part of this compound property");
        }
        remove(i);
    }

    /**
     * Return the number of sub properties of this CompoundProperty.
     * @return int; the number of sub properties of this CompoundProperty
     */
    public final int size()
    {
        return this.value.size();
    }

    /**
     * Update the property group when this CompoundProperty is added or removed from another CompoundProperty.
     * @param newPropertyGroup Map&lt;String, AbstractProperty&lt;?&gt;&gt;; if non-null; this is the property group of the new
     *            parent which we are now part of and we must use that in lieu of our own; if null; we are being removed from
     *            our parent and we must rebuild our own property group
     */
    protected final void setPropertyGroup(final Map<String, Property<?>> newPropertyGroup)
    {
        if (null == newPropertyGroup)
        {
            // Rebuild the property group (after removal from parent
            this.propertyGroup = new HashMap<String, Property<?>>();
            for (Property<?> ap : this.value)
            {
                this.propertyGroup.put(ap.getKey(), ap);
            }
        }
        else
        {
            this.propertyGroup = newPropertyGroup;
            for (Property<?> ap : this)
            {
                this.propertyGroup.put(ap.getKey(), ap);
            }
        }
    }

    /**
     * Return the sub property at a specified index.
     * @param index int; the index of the property to retrieve
     * @return AbstractProperty; the sub property at the specified index
     * @throws PropertyException when index is out of range
     */
    public final Property<?> get(final int index) throws PropertyException
    {
        if (index < 0 || index >= this.value.size())
        {
            throw new PropertyException("index is out of range");
        }
        return this.value.get(index);
    }

    /**
     * Return the sub-items in display order.
     * @return ArrayList&lt;AbstractProperty&lt;?&gt;&gt;; the sub-items in display order
     */
    public final List<Property<?>> displayOrderedValue()
    {
        List<Property<?>> result = new ArrayList<>(this.value);
        final List<Property<?>> list = this.value;
        Collections.sort(result, new Comparator<Property<?>>()
        {

            @Override
            public int compare(final Property<?> arg0, final Property<?> arg1)
            {
                int dp0 = arg0.getDisplayPriority();
                int dp1 = arg1.getDisplayPriority();
                if (dp0 < dp1)
                {
                    return -1;
                }
                else if (dp0 > dp1)
                {
                    return 1;
                }
                int i0 = list.indexOf(arg0);
                int i1 = list.indexOf(arg1);
                if (i0 < i1)
                {
                    return -1;
                }
                else if (i0 > i1)
                {
                    return 1;
                }
                return 0;
            }

        });
        /*-
        System.out.println("Sorted " + getShortName());
        int pos = 0;
        for (AbstractProperty<?> ap : result)
        {
            System.out.println(++pos + " - " + ap.getDisplayPriority() + ": " + ap.getShortName());
        }
         */
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public final String htmlStateDescription()
    {
        StringBuilder result = new StringBuilder();
        result.append("<table border=\"1\">");
        result.append("<tr><th align=\"left\">" + getShortName() + "</th></tr>\n");
        for (Property<?> ap : displayOrderedValue())
        {
            result.append("<tr><td>&nbsp;&nbsp;&nbsp;&nbsp;" + ap.htmlStateDescription() + "</td></tr>\n");
        }
        result.append("</table>\n");
        return result.toString();
    }

    /** {@inheritDoc} */
    @Override
    public final CompoundProperty deepCopy()
    {
        ArrayList<Property<?>> copyOfValue = new ArrayList<>();
        for (Property<?> ap : this.value)
        {
            copyOfValue.add(ap.deepCopy());
        }
        try
        {
            return new CompoundProperty(getKey(), getShortName(), getDescription(), copyOfValue, isReadOnly(),
                    getDisplayPriority());
        }
        catch (PropertyException exception)
        {
            System.err.println("Cannot happen");
            exception.printStackTrace();
        }
        return null; // NOTREACHED
    }

    /**
     * Retrieve the property group. DO NOT MODIFY the result.
     * @return Map&lt;String, AbstractProperty&lt;?&gt;&gt;; the property group map
     */
    protected final Map<String, Property<?>> getPropertyGroup()
    {
        return this.propertyGroup;
    }

}
