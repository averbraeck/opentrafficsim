package org.opentrafficsim.simulationengine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

/**
 * Compound property.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 30 dec. 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class CompoundProperty extends AbstractProperty<ArrayList<AbstractProperty<?>>>
{
    /** Properties contained in this one. */
    private ArrayList<AbstractProperty<?>> value;

    /** The shortName of the property. */
    private String shortName;

    /** The description of the property. */
    private String description;

    /** The property is read-only. */
    private final Boolean readOnly;

    /**
     * Construct an CompoundProperty.
     * @param shortName String; the short name of the new CompoundProperty
     * @param description String; description of the new CompoundProperty (may use HTML mark up)
     * @param initialValue Integer; the initial value of the new CompoundProperty
     * @param readOnly boolean; if true this CompoundProperty can not be altered
     * @param displayPriority int; the display priority of the new CompoundProperty
     */
    public CompoundProperty(final String shortName, final String description,
            final ArrayList<AbstractProperty<?>> initialValue, final boolean readOnly, final int displayPriority)
    {
        super(displayPriority);
        this.shortName = shortName;
        this.description = description;
        this.value = null == initialValue ? new ArrayList<AbstractProperty<?>>() : initialValue;
        this.readOnly = readOnly;
    }

    /** {@inheritDoc} */
    @Override
    public final ArrayList<AbstractProperty<?>> getValue()
    {
        return new ArrayList<AbstractProperty<?>>(this.value);
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
    public final void setValue(final ArrayList<AbstractProperty<?>> newValue) throws IncompatiblePropertyException
    {
        if (this.readOnly)
        {
            throw new IncompatiblePropertyException("Cannot modify a read-only CompoundProperty");
        }
        this.value = newValue;
    }

    /** {@inheritDoc} */
    @Override
    public final boolean isReadOnly()
    {
        return this.readOnly;
    }

    /**
     * Find an embedded AbstractProperty that has a specified shortName. <br>
     * Return the first matching one, or null if none of the embedded AbstractProperties has the specified name.
     * @param name String; the name of the sought embedded AbstractProperty
     * @return AbstractProperty&lt;?&gt;; the first matching embedded AbstractProperty or null if there is no embedded
     *         AbstractProperty with the specified name
     */
    public final AbstractProperty<?> findByShortName(final String name)
    {
        //System.out.println("Searching property " + name);
        Iterator<AbstractProperty<ArrayList<AbstractProperty<?>>>> i = this.iterator();
        while (i.hasNext())
        {
            AbstractProperty<?> ap = i.next();
            //System.out.println("Inspecting " + ap.getShortName());
            if (ap.getShortName().equals(name))
            {
                return ap;
            }
        }
        return null;
    }

    /**
     * Add an AbstractProperty at a specified position.
     * @param index int; the position where the AbstractProperty must be added
     * @param ap AbstractProperty; the property to add
     * @throws IncompatiblePropertyException when this CompoundProperty is read-only, or index is out of range
     */
    public final void add(final int index, final AbstractProperty<?> ap) throws IncompatiblePropertyException
    {
        if (this.readOnly)
        {
            throw new IncompatiblePropertyException("Cannot modify a read-only CompoundProperty");
        }
        if (index < 0 || index > this.value.size())
        {
            throw new IncompatiblePropertyException("index is out of range");
        }
        this.value.add(index, ap);
    }

    /**
     * Add an AbstractProperty at the end of the list.
     * @param ap AbstractProperty; the property to add
     * @throws IncompatiblePropertyException when this CompoundProperty is read-only
     */
    public final void add(final AbstractProperty<?> ap) throws IncompatiblePropertyException
    {
        add(this.value.size(), ap);
    }

    /**
     * Remove a sub property from this CompoundProperty.
     * @param index int; the position of the sub property to remove
     * @throws IncompatiblePropertyException when this CompoundProperty is read-only, or index is out of range
     */
    public final void remove(final int index) throws IncompatiblePropertyException
    {
        if (this.readOnly)
        {
            throw new IncompatiblePropertyException("Cannot modify a read-only CompoundProperty");
        }
        if (index < 0 || index >= this.value.size())
        {
            throw new IncompatiblePropertyException("index is out of range");
        }
        this.value.remove(index);
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
     * Return the sub property at a specified index.
     * @param index int; the index of the property to retrieve
     * @return AbstractProperty; the sub property at the specified index
     * @throws IncompatiblePropertyException when index is out of range
     */
    public final AbstractProperty<?> get(final int index) throws IncompatiblePropertyException
    {
        if (index < 0 || index >= this.value.size())
        {
            throw new IncompatiblePropertyException("index is out of range");
        }
        return this.value.get(index);
    }

    /**
     * Return the sub-items in display order.
     * @return ArrayList&lt;AbstractProperty&lt;?&gt;&gt;; the sub-items in display order
     */
    public final ArrayList<AbstractProperty<?>> displayOrderedValue()
    {
        ArrayList<AbstractProperty<?>> result = new ArrayList<AbstractProperty<?>>(this.value);
        final ArrayList<AbstractProperty<?>> list = this.value;
        Collections.sort(result, new Comparator<AbstractProperty<?>>()
        {

            @Override
            public int compare(final AbstractProperty<?> arg0, final AbstractProperty<?> arg1)
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

}
