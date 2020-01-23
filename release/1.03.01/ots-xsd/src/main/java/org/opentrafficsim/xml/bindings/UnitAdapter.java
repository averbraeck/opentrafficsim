package org.opentrafficsim.xml.bindings;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.djunits.value.vdouble.scalar.base.DoubleScalarInterface;
import org.djutils.exceptions.Throw;

/**
 * UnitAdapter is the generic class for all Unit-based XML-adapters.
 * <p>
 * Copyright (c) 2019-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://djunits.org/docs/license.html">DJUNITS License</a>.
 * <p>
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public abstract class UnitAdapter<S extends DoubleScalarInterface<?, S>> extends XmlAdapter<String, S>
{
    /** {@inheritDoc} */
    @Override
    public String marshal(final S scalar)
    {
        Throw.whenNull(scalar, "Marshalling scalar with unit: argument contains null value");
        return scalar.getInUnit() + " " + scalar.getDisplayUnit().getDefaultTextualAbbreviation();
    }

}
