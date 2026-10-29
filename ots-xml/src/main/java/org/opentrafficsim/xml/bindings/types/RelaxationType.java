package org.opentrafficsim.xml.bindings.types;

import org.opentrafficsim.road.gtu.tactical.util.lmrs.Relaxation;

/**
 * RelaxationType.java.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.<br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * @author Alexander Verbraeck
 * @author Peter Knoppers
 * @author Wouter Schakel
 */
public class RelaxationType extends ExpressionType<Relaxation>
{

    /** Serialization version UID. */
    private static final long serialVersionUID = 20261026L;

    /** Function to convert output from expression to the right type. */
    private static final SerializableFunction<Object, Relaxation> TO_TYPE =
            SerializableFunction.ofStaticField(Relaxation.class);

    /**
     * Constructor with value.
     * @param value value, may be {@code null}.
     */
    public RelaxationType(final Relaxation value)
    {
        super(value);
    }

    /**
     * Constructor with expression.
     * @param expression expression.
     */
    public RelaxationType(final String expression)
    {
        super(expression, TO_TYPE);
    }

}
