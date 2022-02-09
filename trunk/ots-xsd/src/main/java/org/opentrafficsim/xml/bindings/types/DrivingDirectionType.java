package org.opentrafficsim.xml.bindings.types;

/**
 * LongitudinalDirectionalityType contains the directionality that can easily be converted into OTS's
 * LongitudinalDirectionality. <br>
 * <br>
 * Copyright (c) 2003-2022 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
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
