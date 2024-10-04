package org.opentrafficsim.road.gtu.lane.tactical.lanechangemobil;

import java.io.Serializable;

import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.tactical.following.AccelerationStep;

/**
 * Acceleration, lane change decision and time until when this movement is committed.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class LaneMovementStep implements Serializable
{
    /** */
    private static final long serialVersionUID = 20150206L;

    /** The resulting acceleration and duration of validity. */
    private final AccelerationStep gfmr;

    /**
     * Lane change. This has one of the following values:
     * <table >
     * <caption>&nbsp;</caption>
     * <tr>
     * <td>null:</td>
     * <td>Stay in the current lane</td>
     * </tr>
     * <tr>
     * <td>LateralDirectionality.LEFT:</td>
     * <td>Move to the Left adjacent lane, as seen from the GTU in forward driving direction</td>
     * </tr>
     * <tr>
     * <td>LateralDirectionality.RIGHT:</td>
     * <td>Move to the Right adjacent lane, as seen from the GTU in forward driving direction</td>
     * </tr>
     * </table>
     */
    private final LateralDirectionality laneChange;

    /**
     * Construct a new LaneChangeModelResult.
     * @param gfmr the acceleration and duration of validity of this result.
     * @param laneChange this has one of the values:
     *            <table >
     *            <caption>&nbsp;</caption>
     *            <tr>
     *            <td>null:</td>
     *            <td>Stay in the current lane</td>
     *            </tr>
     *            <tr>
     *            <td>LateralDirectionality.LEFT:</td>
     *            <td>Move to the Left adjacent lane, as seen from the GTU in forward driving direction</td>
     *            </tr>
     *            <tr>
     *            <td>LateralDirectionality.RIGHT:</td>
     *            <td>Move to the Right adjacent lane, as seen from the GTU in forward driving direction</td>
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
     *         <table >
     *         <caption>&nbsp;</caption>
     *         <tr>
     *         <td>null:</td>
     *         <td>Stay in the current lane</td>
     *         </tr>
     *         <tr>
     *         <td>LateralDirectionality.LEFT:</td>
     *         <td>Move to the Left adjacent lane, as seen from the GTU in forward driving direction</td>
     *         </tr>
     *         <tr>
     *         <td>LateralDirectionality.RIGHT:</td>
     *         <td>Move to the Right adjacent lane, as seen from the GTU in forward driving direction</td>
     *         </tr>
     *         </table>
     */
    public final LateralDirectionality getLaneChangeDirection()
    {
        return this.laneChange;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return this.gfmr.toString() + ", " + (null == this.laneChange ? "no lane change" : this.laneChange.toString());
    }

}
