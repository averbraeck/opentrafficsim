package org.opentrafficsim.core.network;

import java.util.Arrays;
import java.util.LinkedHashSet;

import org.djutils.immutablecollections.Immutable;
import org.djutils.immutablecollections.ImmutableHashSet;
import org.djutils.immutablecollections.ImmutableSet;
import org.opentrafficsim.core.gtu.GTUDirectionality;

/**
 * Permitted longitudinal driving directions.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$, initial version Oct 15, 2014 <br>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://www.citg.tudelft.nl">Guus Tamminga</a>
 */
public enum LongitudinalDirectionality
{
    /** Direction the same as the direction of the graph, increasing fractional position when driving in this direction. */
    DIR_PLUS(new GTUDirectionality[] {GTUDirectionality.DIR_PLUS}),
    /** Direction opposite to the direction of the graph, decreasing fractional position when driving in this direction. */
    DIR_MINUS(new GTUDirectionality[] {GTUDirectionality.DIR_MINUS}),
    /** Bidirectional. */
    DIR_BOTH(new GTUDirectionality[] {GTUDirectionality.DIR_PLUS, GTUDirectionality.DIR_MINUS}),
    /** No traffic possible. */
    DIR_NONE(new GTUDirectionality[] {});

    /** Array of permitted driving directions. */
    private final ImmutableSet<GTUDirectionality> directions;

    /**
     * Construct a new LongitudinalDirectionality.
     * @param directions GTUDirectionality[]; array containing the permitted driving directions
     */
    LongitudinalDirectionality(final GTUDirectionality[] directions)
    {
        this.directions = new ImmutableHashSet<>(new LinkedHashSet<>(Arrays.asList(directions)), Immutable.WRAP);
    }

    /**
     * Retrieve the permitted driving directions.
     * @return ImmutableSet&lt;GTUDirectionality&gt;; immutable set containing the permitted driving directions
     */
    public final ImmutableSet<GTUDirectionality> getDirectionalities()
    {
        return this.directions;
    }

    /**
     * This method looks if this directionality "contains" the provided other directionality. The logic table looks as follows:
     * <table border="1">
     * <caption>&nbsp;</caption>
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
     * @param directionality LongitudinalDirectionality; the directionality to compare with
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
     * Easy access method to test if the directionality is BACKWARD.
     * @return whether the directionality is BACKWARD
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

    /**
     * Easy access method to test if the directionality is NONE.
     * @return whether the directionality is NONE
     */
    public final boolean isNone()
    {
        return this.equals(DIR_NONE);
    }

    /**
     * Compute the intersection of this LongitudinalDirectionality with another LongitudinalDirectionality.
     * @param other LongitudinalDirectionality; the other LongitudinalDirectionality
     * @return LongitudinalDirectionality; the intersection of <code>this</code> and <code>other</code>
     */
    public final LongitudinalDirectionality intersect(final LongitudinalDirectionality other)
    {
        if (null == other)
        {
            System.err.println("other LongitudinalDirectionality should not be null; returning DIR_NONE");
            return DIR_NONE;
        }
        switch (other)
        {
            case DIR_BOTH:
                return this;
            case DIR_MINUS:
                if (this.equals(other))
                {
                    return this;
                }
                if (this.equals(DIR_BOTH))
                {
                    return other;
                }
                return DIR_NONE;
            case DIR_NONE:
                return other;
            case DIR_PLUS:
                if (this.equals(other))
                {
                    return this;
                }
                if (this.equals(DIR_BOTH))
                {
                    return other;
                }
                return DIR_NONE;
            default:
                // Cannot happen (unless someone manages to change this enum).
                System.err.println("intersect with ???? (returns DIR_NONE)");
                return DIR_NONE;
        }
    }

    /**
     * Check if a direction is permitted by this LongitudinalDirectionality.
     * @param direction GTUDirectionality; the direction of motion in which a GTU moves, or wants to move
     * @return boolean; true if the direction is permitted by this LongitudinalDirectionality; false if it is not permitted
     */
    public final boolean permits(final GTUDirectionality direction)
    {
        switch (direction)
        {
            case DIR_MINUS:
                return isBackwardOrBoth();
            case DIR_PLUS:
                return isForwardOrBoth();
            default:
                System.out.println("Bad direction: " + direction);
                return false;
        }
    }

    /**
     * Return the inverse if this LongitudinalDirectionality.
     * @return LongitudinalDirectionality; the directional inverse of this LongitudinalDirectionality
     */
    public LongitudinalDirectionality invert()
    {
        switch (this)
        {
            case DIR_BOTH:
                return this;
            case DIR_MINUS:
                return DIR_PLUS;
            case DIR_NONE:
                return this;
            case DIR_PLUS:
                return DIR_MINUS;
            default:
                return this; // cannot happen
        }
    }

}
