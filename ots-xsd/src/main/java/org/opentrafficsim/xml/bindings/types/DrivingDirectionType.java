package org.opentrafficsim.xml.bindings.types;

/**
 * LongitudinalDirectionalityType contains the directionality that can easily be converted into OTS's
 * LongitudinalDirectionality.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public enum DrivingDirectionType
{
    /** Direction the same as the direction of the graph, increasing fractional position when driving in this direction. */
    DIR_PLUS,
    /** Direction opposite to the direction of the graph, decreasing fractional position when driving in this direction. */
    DIR_MINUS,
    /** Bidirectional. */
    DIR_BOTH,
    /** No traffic possible. */
    DIR_NONE;
}
