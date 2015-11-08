package org.opentrafficsim.road.gtu.lane.changing;

import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.following.AccelerationStep;

/**
 * Acceleration, lane change decision and time until when this movement is committed.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision: 1155 $, $LastChangedDate: 2015-07-26 01:01:13 +0200 (Sun, 26 Jul 2015) $, by $Author: averbraeck $,
 *          initial version 6 feb. 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class LaneMovementStep
{
    /** The resulting acceleration and duration of validity. */
    private final AccelerationStep gfmr;

    /**
     * Lane change. This has one of the following values:
     * <table summary="">
     * <tr>
     * <td>null:</td>
     * <td>Stay in the current lane</td>
     * </tr>
     * <tr>
     * <td>LateralDirectionality.LEFT:</td>
     * <td>Move to the Left adjacent lane</td>
     * </tr>
     * <tr>
     * <td>LateralDirectionality.RIGHT:</td>
     * <td>Move to the Right adjacent lane</td>
     * </tr>
     * </table>
     */
    private final LateralDirectionality laneChange;

    /**
     * Construct a new LaneChangeModelResult.
     * @param gfmr GTUFollowingModelResult; the acceleration and duration of validity of this result.
     * @param laneChange LateralDirectionality; this has one of the values:
     *            <table summary="">
     *            <tr>
     *            <td>null:</td>
     *            <td>Stay in the current lane</td>
     *            </tr>
     *            <tr>
     *            <td>LateralDirectionality.LEFT:</td>
     *            <td>Move to the Left adjacent lane</td>
     *            </tr>
     *            <tr>
     *            <td>LateralDirectionality.RIGHT:</td>
     *            <td>Move to the Right adjacent lane</td>
     *            </tr>
     *            </table>
     */
    public LaneMovementStep(final AccelerationStep gfmr, final LateralDirectionality laneChange)
    {
        this.gfmr = gfmr;
        this.laneChange = laneChange;
    }

    /**
     * @return the GTUModelFollowingResult.
     */
    public final AccelerationStep getGfmr()
    {
        return this.gfmr;
    }

    /**
     * @return laneChange. This has one of the values:
     *         <table summary="">
     *         <tr>
     *         <td>null:</td>
     *         <td>Stay in the current lane</td>
     *         </tr>
     *         <tr>
     *         <td>LateralDirectionality.LEFT:</td>
     *         <td>Move to the Left adjacent lane</td>
     *         </tr>
     *         <tr>
     *         <td>LateralDirectionality.RIGHT:</td>
     *         <td>Move to the Right adjacent lane</td>
     *         </tr>
     *         </table>
     */
    public final LateralDirectionality getLaneChange()
    {
        return this.laneChange;
    }

    /** {@inheritDoc} */
    public final String toString()
    {
        return this.gfmr.toString() + ", " + (null == this.laneChange ? "no lane change" : this.laneChange.toString());
    }

}
