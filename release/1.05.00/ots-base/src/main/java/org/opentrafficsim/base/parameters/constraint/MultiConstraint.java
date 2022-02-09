package org.opentrafficsim.base.parameters.constraint;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Constraint containing multiple constraints.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 11 sep. 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <T> value type
 */
public class MultiConstraint<T> implements Constraint<T>
{

    /** Set of constraints. */
    private final Set<Constraint<T>> constraints;

    /** Message of the latest failed constrained. */
    private String failedConstraintMessage = null;

    /** String representation. */
    private final String stringRepresentation;

    /**
     * Creates a {@code MultiConstraint} from given constraints.
     * @param constraints Constraint&lt;T&gt;...; constraints
     * @param <T> value type
     * @return {@code MultiConstraint}
     */
    @SafeVarargs
    public static final <T> MultiConstraint<T> create(final Constraint<T>... constraints)
    {
        Set<Constraint<T>> set = new LinkedHashSet<>();
        for (Constraint<T> constraint : constraints)
        {
            set.add(constraint);
        }
        return new MultiConstraint<>(set);
    }

    /**
     * Constructor.
     * @param constraints Set&lt;Constraint&lt;T&gt;&gt;; constraints
     */
    public MultiConstraint(final Set<Constraint<T>> constraints)
    {
        this.constraints = constraints;
        this.stringRepresentation = String.format("MultiConstraint [contains %d constraints]", this.constraints.size());
    }

    /** {@inheritDoc} */
    @Override
    public boolean accept(final T value)
    {
        for (Constraint<T> constraint : this.constraints)
        {
            if (!constraint.accept(value))
            {
                this.failedConstraintMessage = constraint.failMessage();
                return false;
            }
        }
        return true;
    }

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return this.stringRepresentation;
    }

}
