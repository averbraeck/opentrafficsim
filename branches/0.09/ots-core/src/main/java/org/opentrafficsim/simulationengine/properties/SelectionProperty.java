package org.opentrafficsim.simulationengine.properties;

import java.io.Serializable;

/**
 * Property that is described by a set of Strings where exactly one can (and must) be true.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version 19 dec. 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class SelectionProperty extends AbstractProperty<String> implements Serializable
{
    /** */
    private static final long serialVersionUID = 20150000L;

    /** The set of options to select from. */
    private String[] options;

    /** Index of the currently selected option. */
    private int currentOption;

    /**
     * Construct a new SelectionProperty.
     * @param key String; the unique key of the new property
     * @param shortName String; name of the new SelectionProperty
     * @param description String; description of the new SelectionProperty (may use HTML mark up)
     * @param options String[]; the possible values of the SelectionProperty
     * @param initialDefaultOption int; the index of the initially selected option
     * @param readOnly boolean; if true the selection cannot be altered.
     * @param displayPriority int; the display priority of the new SelectionProperty
     */
    public SelectionProperty(final String key, final String shortName, final String description, final String[] options,
            final int initialDefaultOption, final boolean readOnly, final int displayPriority)
    {
        super(key, displayPriority, shortName, description);
        // Make a deep copy of options
        this.options = new String[options.length];
        for (int i = 0; i < options.length; i++)
        {
            this.options[i] = options[i];
        }
        this.currentOption = initialDefaultOption;
        setReadOnly(readOnly);
    }

    /** {@inheritDoc} */
    @Override
    public final String getValue()
    {
        return this.options[this.currentOption];
    }

    /**
     * Retrieve the name of one of the options of this SelectionProperty.
     * @param index int; the index of the value
     * @return String; the name of the value at the requested index
     */
    public final String getOptionName(final int index)
    {
        return this.options[index];
    }

    /** {@inheritDoc} */
    @Override
    public final void setValue(final String newValue) throws PropertyException
    {
        if (isReadOnly())
        {
            throw new PropertyException("Cannot modify a read-only SelectionProperty");
        }
        for (int index = 0; index < this.options.length; index++)
        {
            if (this.options[index].equals(newValue))
            {
                this.currentOption = index;
                return;
            }
        }
        throw new PropertyException("The value " + newValue + " is not among the valid options");
    }

    /**
     * Return the names of the options of this SelectionProperty.
     * @return String[]; the names of the options of this SelectionProperty
     */
    public final String[] getOptionNames()
    {
        return this.options.clone();
    }

    /** {@inheritDoc} */
    @Override
    public final String htmlStateDescription()
    {
        return getShortName() + ": " + this.options[this.currentOption];
    }

    /** {@inheritDoc} */
    @Override
    public final AbstractProperty<String> deepCopy()
    {
        return new SelectionProperty(getKey(), getShortName(), getDescription(), this.options, this.currentOption,
                isReadOnly(), getDisplayPriority());
    }

}