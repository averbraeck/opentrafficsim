package org.opentrafficsim.xml.bindings.types;

import org.opentrafficsim.xml.bindings.types.GtuPositionType.GtuPosition;

/**
 * Expression type with GtuPosition value.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
// TODO: this type (xsd:GtuPositionType) is not used in XSD
@SuppressWarnings("serial")
public class GtuPositionType extends ExpressionType<GtuPosition>
{

    /**
     * Constructor with value.
     * @param value value, may be {@code null}.
     */
    public GtuPositionType(final GtuPosition value)
    {
        super(value);
    }

    /**
     * Constructor with expression.
     * @param expression expression.
     */
    public GtuPositionType(final String expression)
    {
        super(expression);
    }

    /**
     * Reference point on a GTU.
     * <p>
     * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
     */
    public enum GtuPosition
    {
        /** Front of the GTU. */
        FRONT,

        /** Rear of the GTU. */
        REAR,

        /** Reference position of the GTU. */
        REFERENCE;
    }

}
