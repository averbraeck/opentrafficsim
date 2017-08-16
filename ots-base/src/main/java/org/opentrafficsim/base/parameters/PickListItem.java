package org.opentrafficsim.base.parameters;

/**
 * Value that can be used in a PickList.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$,
 *          initial version Aug 14, 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <T> type of the pick ids (usually String or Integer)
 */
public class PickListItem<T>
{
    /** Id of this pick list item. */
    private final T id;

    /** Display text of this pick list item. */
    private final String displayText;
    
    /** Description (may use HTML formatting tags). */
    private final String description;

    /**
     * Construct a new PickListItem.
     * @param id T; the id of the new PickListItem
     * @param displayText String; the text to show to the user in the pick list
     * @param description String; the description of this pick list item (may use HTML formatting) 
     */
    public PickListItem(final T id, final String displayText, final String description)
    {
        this.id = id;
        this.displayText = displayText;
        this.description = description;
    }

    /**
     * Construct a new PickListItem.
     * @param id T; the id of the new PickListItem
     * @param displayText String; the text to show to the user in the pick list
     */
    public PickListItem(final T id, final String displayText)
    {
        this(id, displayText, displayText);
    }

    /**
     * Retrieve the id of this PickListItem.
     * @return T; the id of this PickListItem
     */
    public final T getId()
    {
        return this.id;
    }

    /**
     * Retrieve the display text of this PickListItem.
     * @return String; the display text of this PickListItem
     */
    public final String getDisplayText()
    {
        return this.displayText;
    }

    /**
     * Retrieve the description of this PickListItem.
     * @return String; the description of this PickListItem
     */
    public final String getDescription()
    {
        return this.description;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return "PickListItem [id=" + this.id + ", displayText=" + this.displayText + ", description=" + this.description
                + "]";
    }
   
}
