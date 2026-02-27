package org.opentrafficsim.base.parameters.constraint;

import java.util.Collection;

/**
 * Constraint containing multiple constraints.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <T> value type
 */
public class MultiConstraint<T> implements Constraint<T>
{

    /** Set of constraints. */
    private final Collection<Constraint<? super T>> constraints;

    /** Message of the latest failed constrained. */
    private String failedConstraintMessage = null;

    /** String representation. */
    private final String stringRepresentation;

    /**
     * Constructor.
     * @param constraints constraints
     */
    public MultiConstraint(final Collection<Constraint<? super T>> constraints)
    {
        this.constraints = constraints;
        this.stringRepresentation = String.format("MultiConstraint [contains %d constraints]", this.constraints.size());
    }

    @Override
    public boolean accept(final T value)
    {
        for (Constraint<? super T> constraint : this.constraints)
        {
            if (!constraint.accept(value))
            {
                this.failedConstraintMessage = constraint.failMessage();
                return false;
            }
        }
        return true;
    }

    @Override
    public String failMessage()
    {
        if (this.failedConstraintMessage == null)
        {
            return "A constraint failed for parameter '%s'.";
        }
        // note that we do not synchronize, nor can't we be assured that after accept()=false, this method is (directly) invoked
        return "A constraint failed, most likely: " + this.failedConstraintMessage;
    }

    @Override
    public String toString()
    {
        return this.stringRepresentation;
    }

}
