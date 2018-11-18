package org.opentrafficsim.base.modelproperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.djutils.exceptions.Throw;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterType;
import org.opentrafficsim.base.parameters.Parameters;

/**
 * Pick list with PickListItems.
 * <p>
 * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Aug 14, 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <T> type of the ids of the pick items
 */
public class PickList<T> extends ParameterType<T>
{
    /** */
    private static final long serialVersionUID = 20170814L;

    /** The list of item ids. */
    private List<T> itemIds = new ArrayList<>();

    /** The items for each id. */
    private Map<T, PickListItem<T>> items = new HashMap<>();

    /**
     * Construct a new PickList and fill it with the provided items.
     * @param id String; id of the new PickList
     * @param description String; description of the new PickList
     * @param firstItem PickListItem&lt;T&gt;; PickList&lt;T&gt; the first item to add to the new PickList
     * @param additionalItems PickListItem&lt;T&gt;...; any additional items to add to the new PickList; the reason for putting
     *            the first item in a separate argument is to ensure that this constructor cannot construct a PickList with zero
     *            items
     * @throws ParameterException when the ids of the items are not all distinct
     */
    @SafeVarargs
    @SuppressWarnings("unchecked")
    public PickList(final String id, final String description, final PickListItem<T> firstItem,
            final PickListItem<T>... additionalItems) throws ParameterException
    {
        super(id, description, (Class<T>) firstItem.getClass());
        addItem(firstItem);
        for (PickListItem<T> item : additionalItems)
        {
            addItem(item);
        }
    }

    /**
     * Construct a new PickList and fill it with the provided items.
     * @param id String; id of the new PickList
     * @param description String; description of the new PickList
     * @param items List&lt;PickListItem&lt;T&gt;&gt;; the items to add to the new PickList
     * @throws ParameterException when the ids of the items are not all distinct
     */
    @SuppressWarnings("unchecked")
    public PickList(final String id, final String description, final List<PickListItem<T>> items) throws ParameterException
    {
        super(id, description, (Class<T>) getItemZero(items));
        for (PickListItem<T> item : items)
        {
            addItem(item);
        }
    }

    /**
     * @param items2 List&lt;?&gt;; a non-empty list of pick list items.
     * @return Object; item 0 in the list
     * @throws ParameterException when list is null, or empty
     */
    private static Object getItemZero(final List<?> items2) throws ParameterException
    {
        Throw.whenNull(items2, "items may not be null");
        Throw.when(items2.isEmpty(), ParameterException.class, "items may not be empty");
        return items2.get(0).getClass();
    }

    /**
     * Add one item to this PickList.
     * @param item PickListItem&lt;T&gt;; the item to add
     * @throws ParameterException when the id of the provided item matches an existing item
     */
    public final void addItem(final PickListItem<T> item) throws ParameterException
    {
        Throw.when(this.itemIds.contains(item.getId()), ParameterException.class,
                "PickList already contains an item matching id \"%s\"", item.getId());
        this.itemIds.add(item.getId());
        this.items.put(item.getId(), item);
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String printValue(final Parameters parameters) throws ParameterException
    {
        return parameters.getParameter(this).toString();
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return "PickList [ids=" + this.itemIds + ", items=" + this.items + "]";
    }

}
