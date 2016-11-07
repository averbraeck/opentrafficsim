package org.opentrafficsim.core.network;

/**
 * Permitted longitudinal driving directions.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version Oct 15, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 */
public enum LongitudinalDirectionality
{
    /** Direction the same as the direction of the graph, increasing fractional position when driving in this direction. */
    DIR_PLUS,
    /** Direction opposite to the direction of the graph, decreasing fractional position when driving in this direction. */
    DIR_MINUS,
    /** Bidirectional. */
    DIR_BOTH,
    /** No traffic possible. */
    DIR_NONE;

    /**
     * This method looks if this directionality "contains" the provided other directionality. The logic table looks as follows:
     * <table border="1" summary="">
     * <tr>
     * <td><b>THIS &darr; &nbsp; OTHER &rarr;</b></td>
     * <td><b>DIR_BOTH&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</b></td>
     * <td><b>DIR_PLUS&nbsp;&nbsp;&nbsp;</b></td>
     * <td><b>DIR_MINUS</b></td>
     * <td><b>DIR_NONE&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</b></td>
     * </tr>
     * <tr>
     * <td><b>DIR_BOTH</b></td>
     * <td>true</td>
     * <td>true</td>
     * <td>true</td>
     * <td>true</td>
     * </tr>
     * <tr>
     * <td><b>DIR_PLUS</b></td>
     * <td>false</td>
     * <td>true</td>
     * <td>false</td>
     * <td>true</td>
     * </tr>
     * <tr>
     * <td><b>DIR_MINUS</b></td>
     * <td>false</td>
     * <td>false</td>
     * <td>true</td>
     * <td>true</td>
     * </tr>
     * <tr>
     * <td><b>DIR_NONE</b></td>
     * <td>false</td>
     * <td>false</td>
     * <td>false</td>
     * <td>true</td>
     * </tr>
     * </table>
     * @param directionality the directionality to compare with
     * @return whether this directionality "contains" the provided other directionality
     */
    public final boolean contains(final LongitudinalDirectionality directionality)
    {
        return (this.equals(directionality) || this.equals(DIR_BOTH) || directionality.equals(DIR_NONE)) ? true : false;
    }

    /**
     * Easy access method to test if the directionality is FORWARD or BOTH.
     * @return whether the directionality is FORWARD or BOTH
     */
    public final boolean isForwardOrBoth()
    {
        return this.equals(DIR_PLUS) || this.equals(DIR_BOTH);
    }

    /**
     * Easy access method to test if the directionality is BACKWARD or BOTH.
     * @return whether the directionality is BACKWARD or BOTH
     */
    public final boolean isBackwardOrBoth()
    {
        return this.equals(DIR_MINUS) || this.equals(DIR_BOTH);
    }

    /**
     * Easy access method to test if the directionality is FORWARD.
     * @return whether the directionality is FORWARD
     */
    public final boolean isForward()
    {
        return this.equals(DIR_PLUS);
    }

    /**
     * Easy access method to test if the directionality is BACKWARD or BOTH.
     * @return whether the directionality is BACKWARD or BOTH
     */
    public final boolean isBackward()
    {
        return this.equals(DIR_MINUS);
    }
    
    /**
     * Easy access method to test if the directionality is BACKWARD or BOTH.
     * @return whether the directionality is BACKWARD or BOTH
     */
    public final boolean isBoth()
    {
        return this.equals(DIR_BOTH);
    }
}
