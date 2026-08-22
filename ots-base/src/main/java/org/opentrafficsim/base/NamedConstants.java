package org.opentrafficsim.base;

/**
 * Interface for named types. Classes implementing this interface are expected to have their constants organized as:
 * <pre>
 * interface NamedType extends Named
 * {
 *     public static final NamedType NAME_OF_CONSTANT = new NamedType()
 *     {
 *         public String name() { return "NAME_OF_CONSTANT"; }
 *     };
 * }
 * </pre>
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.<br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * @author Wouter Schakel
 */
public interface NamedConstants
{

    /**
     * Returns the name of the value.
     * @return name of the value
     */
    String name();

}
