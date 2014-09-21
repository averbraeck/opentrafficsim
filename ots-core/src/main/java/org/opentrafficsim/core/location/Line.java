package org.opentrafficsim.core.location;

import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Jul 8, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @param <ID> ID of this Line
 */
public class Line<ID> implements Edge<ID>
{
    /** ID of this Line. */
    private final ID id;

    /** Length of this Line. */
    private final DoubleScalar.Rel<LengthUnit> length;

    /**
     * @param id ID of the new Line
     * @param length Length of the new Line
     */
    public Line(final ID id, final DoubleScalar.Rel<LengthUnit> length)
    {
        this.id = id;
        this.length = length;
    }

    /** {@inheritDoc} */
    @Override
    public ID getID()
    {
        return this.id;
    }

    /** {@inheritDoc} */
    @Override
    public DoubleScalar.Rel<LengthUnit> getLength()
    {
        return this.length;
    }

}
