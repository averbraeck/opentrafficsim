package org.opentrafficsim.road.gtu.lane.tactical.directedlanechange;

import java.io.Serializable;

import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.tactical.following.AccelerationStep;

/**
 * Acceleration, lane change decision and time until when this movement is committed.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision: 1155 $, $LastChangedDate: 2015-07-26 01:01:13 +0200 (Sun, 26 Jul 2015) $, by $Author: averbraeck $,
 *          initial version 6 feb. 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class DirectedLaneMovementStep implements Serializable
{
    /** */
    private static final long serialVersionUID = 20160400L;

    /** The resulting acceleration and duration of validity. */
    private final AccelerationStep accelerationStep;

    /**
     * Lane change. This has one of the following values:
     * <table ><caption>&nbsp;</caption>
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
    private final LateralDirectionality direction;

    /**
     * Construct a new LaneChangeModelResult.
     * @param accelerationStep AccelerationStep; the acceleration and duration of validity of this result.
     * @param direction LateralDirectionality; this has one of the values:
     *            <table ><caption>&nbsp;</caption>
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
    public DirectedLaneMovementStep(final AccelerationStep accelerationStep, final LateralDirectionality direction)
    {
        this.accelerationStep = accelerationStep;
        this.direction = direction;
    }

    /**
     * @return the acceleration step.
     */
    public final AccelerationStep getGfmr()
    {
        return this.accelerationStep;
    }

    /**
     * @return laneChange. This has one of the values:
     *         <table ><caption>&nbsp;</caption>
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
    public final LateralDirectionality getLaneChange()
    {
        return this.direction;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return this.accelerationStep.toString() + ", "
                + (null == this.direction ? "no lane change" : this.direction.toString());
    }

}
