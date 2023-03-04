package org.opentrafficsim.road.gtu.perception;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;

/**
 * Test the RelativeLane class.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class RelativeLaneTest
{

    /**
     * ] Test the RelativeLane class.
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

        // TODO Wait for Wouter to indicate whether numLanes == 0 is really permitted when lat != LateralDirectionality.NONE
        for (LateralDirectionality ld : new LateralDirectionality[] {LateralDirectionality.LEFT, LateralDirectionality.NONE,
                LateralDirectionality.RIGHT})
        {
            // TODO fix if 0 is only permitted for LateralDirectionality.NONE
            int startAt = ld == LateralDirectionality.NONE ? 0 : 1;
            int endAt = ld == LateralDirectionality.NONE ? 1 : 4;
            for (int numLanes = startAt; numLanes < endAt; numLanes++)
            {
                // System.out.println("Testing " + ld + ", with numLanes " + numLanes);
                RelativeLane rl = new RelativeLane(ld, numLanes);
                assertTrue("toString returns something descriptive", rl.toString().startsWith("RelativeLane"));
                assertEquals("lateral directionality is returned", ld, rl.getLateralDirectionality());
                assertEquals("numLanes is returned", numLanes, rl.getNumLanes());
                if (numLanes == 0)
                {
                    assertEquals("ld should be CURRENT", RelativeLane.CURRENT, rl);
                    assertTrue("ld is CURRENT", rl.isCurrent());
                    assertTrue("toString contains the word CURRENT", rl.toString().contains("CURRENT"));
                }
                else
                {
                    assertNotEquals("ld is not CURRENT", RelativeLane.CURRENT, rl);
                    assertFalse("ld is not CURRENT", rl.isCurrent());
                }
                if (numLanes == 1)
                {
                    if (ld == LateralDirectionality.LEFT)
                    {
                        assertEquals("ld is LEFT", RelativeLane.LEFT, rl);
                        assertTrue("ld is LEFT", rl.isLeft());
                    }
                    else
                    {
                        assertEquals("ld is RIGHT", RelativeLane.RIGHT, rl);
                        assertTrue("ld is RIGHT", rl.isRight());
                    }
                }
                if (numLanes == 2)
                {
                    if (ld == LateralDirectionality.LEFT)
                    {
                        assertEquals("ld is SECOND_LEFT", RelativeLane.SECOND_LEFT, rl);
                        assertTrue("ld is SECOND_LEFT", rl.isSecondLeft());
                    }
                    else
                    {
                        assertEquals("ld is SECOND_RIGHT", RelativeLane.SECOND_RIGHT, rl);
                        assertTrue("ld is SECOND_RIGHT", rl.isSecondRight());
                    }
                }
                if (ld == LateralDirectionality.LEFT)
                {
                    assertTrue("toString contains LEFT", rl.toString().contains("LEFT"));
                }
                if (ld == LateralDirectionality.RIGHT)
                {
                    assertTrue("toString contains RIGHT", rl.toString().contains("RIGHT"));
                }
                if (ld != LateralDirectionality.LEFT || numLanes != 1)
                {
                    assertNotEquals("ld is not LEFT", RelativeLane.LEFT, rl);
                    assertFalse("ld is not LEFT", rl.isLeft());
                }
                if (ld != LateralDirectionality.LEFT || numLanes != 2)
                {
                    assertNotEquals("ld is not SECOND_LEFT", RelativeLane.SECOND_LEFT, rl);
                    assertFalse("ld is not SECOND_LEFT", rl.isSecondLeft());
                }
                if (ld != LateralDirectionality.RIGHT || numLanes != 1)
                {
                    assertNotEquals("ld is not RIGHT", RelativeLane.RIGHT, rl);
                    assertFalse("ld is not RIGHT", rl.isRight());
                }
                if (ld != LateralDirectionality.RIGHT || numLanes != 2)
                {
                    assertNotEquals("ld is not SECOND_RIGHT", RelativeLane.SECOND_RIGHT, rl);
                    assertFalse("ld is not SECOND_RIGHT", rl.isSecondRight());
                }
                RelativeLane leftNeighbor = rl.getLeft();
                if (numLanes == 0)
                {
                    assertEquals("left of CURRENT is LEFT", RelativeLane.LEFT, leftNeighbor);
                }
                if (numLanes == 1 && ld == LateralDirectionality.RIGHT)
                {
                    assertEquals("left of RIGHT is CURRENT", RelativeLane.CURRENT, leftNeighbor);
                }
                if (numLanes > 1 && ld == LateralDirectionality.RIGHT)
                {
                    assertEquals("left of right > 1 is right minus 1",
                            new RelativeLane(LateralDirectionality.RIGHT, numLanes - 1), leftNeighbor);
                }
                assertNotEquals("hashCodes should be different for adjacent relative lanes", leftNeighbor.hashCode(),
                        rl.hashCode());
                RelativeLane rightNeighbor = rl.getRight();
                if (numLanes == 0)
                {
                    assertEquals("right of CURRENT is RIGHT", RelativeLane.RIGHT, rightNeighbor);
                }
                if (numLanes == 1 && ld == LateralDirectionality.LEFT)
                {
                    assertEquals("right of LEFT is CURRENT", RelativeLane.CURRENT, rightNeighbor);
                }
                if (numLanes > 1 && ld == LateralDirectionality.LEFT)
                {
                    assertEquals("right of LEFT > 1 is left minus 1",
                            new RelativeLane(LateralDirectionality.LEFT, numLanes - 1), rightNeighbor);
                }
                assertNotEquals("hashCodes should be different for adjacent relative lanes", rightNeighbor.hashCode(),
                        rl.hashCode());
                for (int delta = -5; delta <= +5; delta++)
                {
                    RelativeLane other = new RelativeLane(delta < 0 ? LateralDirectionality.LEFT
                            : delta > 0 ? LateralDirectionality.RIGHT : LateralDirectionality.NONE, Math.abs(delta));
                    int rank = ld == LateralDirectionality.LEFT ? -numLanes : numLanes;
                    int diff = rank - delta;
                    if (diff > 0)
                    {
                        assertTrue("compareTo returns > 0", rl.compareTo(other) > 0);
                    }
                    if (diff == 0)
                    {
                        assertTrue("compareTo returns 0", rl.compareTo(other) == 0);
                    }
                    if (diff < 0)
                    {
                        assertTrue("compareTo returns < 0", rl.compareTo(other) < 0);
                    }
                }
                assertFalse("Not equal to null", rl.equals(null));
                assertFalse("Not equal to some unrelated object", rl.equals("NO WAY"));
                assertEquals("Equal to itself", rl, rl);
            }
        }

    }

}
