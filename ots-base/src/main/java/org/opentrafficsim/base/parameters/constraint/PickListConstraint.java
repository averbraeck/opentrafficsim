package org.opentrafficsim.base.parameters.constraint;

import java.util.List;

/**
 * Constraint checker for PickList.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$,
 *          initial version Aug 16, 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <T> type of the objects in the pick list
 */
public class PickListConstraint<T> implements Constraint<T>
{
    /** The list of ids. */
    private List<T> ids;

    /**
     * Set the list of ids.
     * @param ids List&lt;T&gt;; the list of ids (shallow copied)
     */
    public final void setIds(final List<T> ids)
    {
        this.ids = ids;
    }

    /** {@inheritDoc} */
    @Override
    public final boolean fails(final T value)
    {
        return !this.ids.contains(value);
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String failMessage()
    {
        return "Provided PickListItem not among [ids=" + this.ids + "]";
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return "PickListConstraint [ids=" + this.ids + "]";
    }

}
