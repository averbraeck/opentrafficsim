package org.opentrafficsim.xml.bindings;

import org.djunits.value.vdouble.scalar.base.DoubleScalar;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.xml.bindings.types.ExpressionType;

/**
 * ScalarAdapter is the generic class for all Unit-based XML-adapters.
 * <p>
 * Copyright (c) 2019-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://djunits.org/docs/license.html">DJUNITS License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <S> unit type
 * @param <E> expression type
 */
public abstract class ScalarAdapter<S extends DoubleScalar<?, S>, E extends ExpressionType<S>> extends ExpressionAdapter<S, E>
{

    @Override
    public String marshal(final E value)
    {
        Throw.whenNull(value, "Marshalling scalar with unit: argument contains null value");
        return marshal(value, (s) -> s.getInUnit() + " " + s.getDisplayUnit().getDefaultTextualAbbreviation());
    }

}
