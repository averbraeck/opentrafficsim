package org.opentrafficsim.base.parameters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opentrafficsim.base.parameters.constraint.Constraint;

/**
 * Pick list with StringPickListItems.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Aug 14, 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class StringPickList extends AbstractParameterType<String> implements Constraint<String>
{
    /** */
    private static final long serialVersionUID = 20170814L;

    /** The list of ids. */
    private List<String> ids = new ArrayList<>();

    /** The items for each id. */
    private Map<String, StringPickListItem> items = new HashMap<>();

    /**
     * Construct a new StringPickList and fill it with the provided items.
     * @param id String; id of the new StringPickList
     * @param description String; description of the new StringPickList
     * @param items StringPickList...; the items to add to the new StringPickList
     */
    public StringPickList(final String id, final String description, final StringPickListItem... items)
    {
        super(id, description, String.class);
        for (StringPickListItem item : items)
        {
            addItem(item);
        }
    }

    /**
     * Construct a new StringPickList and fill it with the provided items.
     * @param id String; id of the new StringPickList
     * @param description String; description of the new StringPickList
     * @param items List&lt;StringPickListItem&gt;; the items to add to the new StringPickList
     */
    public StringPickList(final String id, final String description, final List<StringPickListItem> items)
    {
        super(id, description, String.class, null, new StringPickListConstraint());
        ((StringPickListConstraint) getConstraint()).setIds(this.ids);
        for (StringPickListItem item : items)
        {
            addItem(item);
        }
    }

    /**
     * Add one item to this StringPickList.
     * @param item StringPickListItem; the item to add
     */
    public final void addItem(final StringPickListItem item)
    {
        this.ids.add(item.getId());
        this.items.put(item.getId(), item);
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String printValue(final Parameters parameters) throws ParameterException
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public final boolean fails(final String value)
    {
        return !this.ids.contains(value);
    }

    /** {@inheritDoc} */
    @Override
    public final String failMessage()
    {
        StringBuilder result = new StringBuilder();
        result.append("provided value is not one of");
        for (String id : this.ids)
        {
            result.append(" " + id);
        }
        return result.toString();
    }

    /**
     * Constraint checker.
     */
    static class StringPickListConstraint implements Constraint<String>
    {
        /** The list of ids. */
        private List<String> ids;

        /**
         * Set the list of ids.
         * @param ids List&lt;String&gt;; the list of ids (shallow copied)
         */
        public void setIds(final List<String> ids)
        {
            this.ids = ids;
        }

        /** {@inheritDoc} */
        @Override
        public boolean fails(final String value)
        {
            return !this.ids.contains(value);
        }

        /** {@inheritDoc} */
        @Override
        public String failMessage()
        {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return "StringPickListConstraint [ids=" + this.ids + "]";
        }

    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return "StringPickList [ids=" + this.ids + ", items=" + this.items + "]";
    }

}
