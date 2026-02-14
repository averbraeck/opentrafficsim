package org.opentrafficsim.road.gtu.perception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.perception.RelativeLane;

/**
 * Test the RelativeLane class.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 */
public final class RelativeLaneTest
{

    /** */
    private RelativeLaneTest()
    {
        // do not instantiate test class
    }

    /**
     * Test the RelativeLane class.
     */
    @Test
    @SuppressWarnings({"unlikely-arg-type"})
    public void testRelativeLane()
    {
        try
        {
            new RelativeLane(null, 1);
            fail("Should have thrown a NullPointerException");
        }
        catch (NullPointerException npe)
        {
            // Ignore expected exception
        }

        try
        {
            new RelativeLane(LateralDirectionality.LEFT, -1);
            fail("negative number of lanes should have thrown an IllegalArgumentException");
        }
        catch (IllegalArgumentException iae)
        {
            // Ignore expected exception
        }

        try
        {
            new RelativeLane(LateralDirectionality.NONE, 1);
            fail("lateral directionality NONE with non-zero number of lanes should have thrown an IllegalArgumentException");
        }
        catch (IllegalArgumentException iae)
        {
            // Ignore expected exception
        }

        try
        {
            new RelativeLane(LateralDirectionality.RIGHT, -1);
            fail("negative number of lanes should have thrown an IllegalArgumentException");
        }
        catch (IllegalArgumentException iae)
        {
            // Ignore expected exception
        }

        for (LateralDirectionality ld : LateralDirectionality.values())
        {
            int startAt = ld == LateralDirectionality.NONE ? 0 : 1;
            int endAt = ld == LateralDirectionality.NONE ? 1 : 4;
            for (int numLanes = startAt; numLanes < endAt; numLanes++)
            {
                // System.out.println("Testing " + ld + ", with numLanes " + numLanes);
                RelativeLane rl = new RelativeLane(ld, numLanes);
                assertTrue(rl.toString().startsWith("RelativeLane"), "toString returns something descriptive");
                assertEquals(ld, rl.getLateralDirectionality(), "lateral directionality is returned");
                assertEquals(numLanes, rl.getNumLanes(), "numLanes is returned");
                if (numLanes == 0)
                {
                    assertEquals(RelativeLane.CURRENT, rl, "ld should be CURRENT");
                    assertTrue(rl.isCurrent(), "ld is CURRENT");
                    assertTrue(rl.toString().contains("CURRENT"), "toString contains the word CURRENT");
                }
                else
                {
                    assertNotEquals(RelativeLane.CURRENT, rl, "ld is not CURRENT");
                    assertFalse(rl.isCurrent(), "ld is not CURRENT");
                }
                if (numLanes == 1)
                {
                    if (ld == LateralDirectionality.LEFT)
                    {
                        assertEquals(RelativeLane.LEFT, rl, "ld is LEFT");
                        assertTrue(rl.isLeft(), "ld is LEFT");
                    }
                    else
                    {
                        assertEquals(RelativeLane.RIGHT, rl, "ld is RIGHT");
                        assertTrue(rl.isRight(), "ld is RIGHT");
                    }
                }
                if (numLanes == 2)
                {
                    if (ld == LateralDirectionality.LEFT)
                    {
                        assertEquals(RelativeLane.SECOND_LEFT, rl, "ld is SECOND_LEFT");
                        assertTrue(rl.isSecondLeft(), "ld is SECOND_LEFT");
                    }
                    else
                    {
                        assertEquals(RelativeLane.SECOND_RIGHT, rl, "ld is SECOND_RIGHT");
                        assertTrue(rl.isSecondRight(), "ld is SECOND_RIGHT");
                    }
                }
                if (ld == LateralDirectionality.LEFT)
                {
                    assertTrue(rl.toString().contains("LEFT"), "toString contains LEFT");
                }
                if (ld == LateralDirectionality.RIGHT)
                {
                    assertTrue(rl.toString().contains("RIGHT"), "toString contains RIGHT");
                }
                if (ld != LateralDirectionality.LEFT || numLanes != 1)
                {
                    assertNotEquals(RelativeLane.LEFT, rl, "ld is not LEFT");
                    assertFalse(rl.isLeft(), "ld is not LEFT");
                }
                if (ld != LateralDirectionality.LEFT || numLanes != 2)
                {
                    assertNotEquals(RelativeLane.SECOND_LEFT, rl, "ld is not SECOND_LEFT");
                    assertFalse(rl.isSecondLeft(), "ld is not SECOND_LEFT");
                }
                if (ld != LateralDirectionality.RIGHT || numLanes != 1)
                {
                    assertNotEquals(RelativeLane.RIGHT, rl, "ld is not RIGHT");
                    assertFalse(rl.isRight(), "ld is not RIGHT");
                }
                if (ld != LateralDirectionality.RIGHT || numLanes != 2)
                {
                    assertNotEquals(RelativeLane.SECOND_RIGHT, rl, "ld is not SECOND_RIGHT");
                    assertFalse(rl.isSecondRight(), "ld is not SECOND_RIGHT");
                }
                RelativeLane leftNeighbor = rl.getLeft();
                if (numLanes == 0)
                {
                    assertEquals(RelativeLane.LEFT, leftNeighbor, "left of CURRENT is LEFT");
                }
                if (numLanes == 1 && ld == LateralDirectionality.RIGHT)
                {
                    assertEquals(RelativeLane.CURRENT, leftNeighbor, "left of RIGHT is CURRENT");
                }
                if (numLanes > 1 && ld == LateralDirectionality.RIGHT)
                {
                    assertEquals(new RelativeLane(LateralDirectionality.RIGHT, numLanes - 1), leftNeighbor,
                            "left of right > 1 is right minus 1");
                }
                assertNotEquals(leftNeighbor.hashCode(), rl.hashCode(),
                        "hashCodes should be different for adjacent relative lanes");
                RelativeLane rightNeighbor = rl.getRight();
                if (numLanes == 0)
                {
                    assertEquals(RelativeLane.RIGHT, rightNeighbor, "right of CURRENT is RIGHT");
                }
                if (numLanes == 1 && ld == LateralDirectionality.LEFT)
                {
                    assertEquals(RelativeLane.CURRENT, rightNeighbor, "right of LEFT is CURRENT");
                }
                if (numLanes > 1 && ld == LateralDirectionality.LEFT)
                {
                    assertEquals(new RelativeLane(LateralDirectionality.LEFT, numLanes - 1), rightNeighbor,
                            "right of LEFT > 1 is left minus 1");
                }
                assertNotEquals(rightNeighbor.hashCode(), rl.hashCode(),
                        "hashCodes should be different for adjacent relative lanes");
                for (int delta = -5; delta <= +5; delta++)
                {
                    RelativeLane other = new RelativeLane(delta < 0 ? LateralDirectionality.LEFT
                            : delta > 0 ? LateralDirectionality.RIGHT : LateralDirectionality.NONE, Math.abs(delta));
                    int rank = ld == LateralDirectionality.LEFT ? -numLanes : numLanes;
                    int diff = rank - delta;
                    if (diff > 0)
                    {
                        assertTrue(rl.compareTo(other) > 0, "compareTo returns > 0");
                    }
                    if (diff == 0)
                    {
                        assertTrue(rl.compareTo(other) == 0, "compareTo returns 0");
                    }
                    if (diff < 0)
                    {
                        assertTrue(rl.compareTo(other) < 0, "compareTo returns < 0");
                    }
                }
                assertFalse(rl.equals(null), "Not equal to null");
                assertFalse(rl.equals("NO WAY"), "Not equal to some unrelated object");
                assertEquals(rl, rl, "Equal to itself");
            }
        }

    }

}
