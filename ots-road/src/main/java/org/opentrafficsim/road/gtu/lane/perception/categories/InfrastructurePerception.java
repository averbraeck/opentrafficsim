package org.opentrafficsim.road.gtu.lane.perception.categories;

import java.util.SortedSet;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.perception.InfrastructureLaneChangeInfo;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.network.speed.SpeedLimitProspect;

/**
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public interface InfrastructurePerception extends LaneBasedPerceptionCategory
{

    /**
     * Updates the infrastructural lane change info. It starts at the given lane and moves downstream over the network. Whenever
     * a point is encountered where lane changes are required, this information is saved.
     * @param lane RelativeLane; relative lateral lane
     * @throws GTUException if the GTU was not initialized or if the lane is not in the cross section
     * @throws ParameterException if a parameter is not defined
     */
    void updateInfrastructureLaneChangeInfo(RelativeLane lane) throws GTUException, ParameterException;

    /**
     * Updates the speed limit prospect.
     * @param lane RelativeLane; relative lateral lane
     * @throws GTUException if the GTU was not initialized or if the lane is not in the cross section
     * @throws ParameterException if a parameter is not defined
     */
    void updateSpeedLimitProspect(RelativeLane lane) throws GTUException, ParameterException;

    /**
     * Updates the distance over which lane changes remains legally possible.
     * @param lane RelativeLane; lane from which the lane change possibility is requested
     * @param lat LateralDirectionality; LEFT or RIGHT, null not allowed
     * @throws GTUException if the GTU was not initialized or if the lane is not in the cross section
     * @throws ParameterException if a parameter is not defined
     */
    void updateLegalLaneChangePossibility(RelativeLane lane, LateralDirectionality lat) throws GTUException, ParameterException;

    /**
     * Updates the distance over which lane changes remains physically possible.
     * @param lane RelativeLane; lane from which the lane change possibility is requested
     * @param lat LateralDirectionality; LEFT or RIGHT, null not allowed
     * @throws GTUException if the GTU was not initialized or if the lane is not in the cross section
     * @throws ParameterException if a parameter is not defined
     */
    void updatePhysicalLaneChangePossibility(RelativeLane lane, LateralDirectionality lat)
            throws GTUException, ParameterException;

    /**
     * Updates a set of relative lanes representing the cross section. This set consists of all lanes on the current link, and
     * an additional lane on the left and/or right side in case of a merge that is sufficiently nearby.
     * @throws GTUException if the GTU was not initialized
     * @throws ParameterException if a parameter is not defined
     */
    void updateCrossSection() throws GTUException, ParameterException;

    /**
     * Returns infrastructure lane change info of a lane. A set is returned as multiple points may force lane changes. Which
     * point is considered most critical is a matter of driver interpretation and may change over time. This is shown below.
     * Suppose vehicle A needs to take the off-ramp, and that behavior is that the minimum distance per required lane change
     * determines how critical it is. First, 400m before the lane-drop, the off-ramp is critical. 300m downstream, the lane-drop
     * is critical. Info is sorted by distance, closest first.
     * 
     * <pre>
     * _______
     * _ _A_ _\_________
     * _ _ _ _ _ _ _ _ _
     * _________ _ _ ___
     *          \_______
     *     (-)        Lane-drop: 1 lane change  in 400m (400m per lane change)
     *     (--------) Off-ramp:  3 lane changes in 900m (300m per lane change, critical)
     *     
     *     (-)        Lane-drop: 1 lane change  in 100m (100m per lane change, critical)
     *     (--------) Off-ramp:  3 lane changes in 600m (200m per lane change)
     * </pre>
     * 
     * @param lane RelativeLane; relative lateral lane
     * @return infrastructure lane change info of a lane
     */
    SortedSet<InfrastructureLaneChangeInfo> getInfrastructureLaneChangeInfo(RelativeLane lane);

    /**
     * Returns the prospect for speed limits on a lane (dynamic speed limits may vary between lanes).
     * @param lane RelativeLane; relative lateral lane
     * @return prospect for speed limits on a lane
     */
    SpeedLimitProspect getSpeedLimitProspect(RelativeLane lane);

    /**
     * Returns the distance over which a lane change remains legally possible. Negative values indicate the distance over which
     * a lane change is legally not possible.
     * @param fromLane RelativeLane; lane from which the lane change possibility is requested
     * @param lat LateralDirectionality; LEFT or RIGHT, null not allowed
     * @return distance over which a lane change remains possible
     * @throws NullPointerException if {@code lat == null}
     */
    Length getLegalLaneChangePossibility(RelativeLane fromLane, LateralDirectionality lat);

    /**
     * Returns the distance over which a lane change remains physically possible. Negative values indicate the distance over
     * which a lane change is physically not possible.
     * @param fromLane RelativeLane; lane from which the lane change possibility is requested
     * @param lat LateralDirectionality; LEFT or RIGHT, null not allowed
     * @return distance over which a lane change remains possible
     * @throws NullPointerException if {@code lat == null}
     */
    Length getPhysicalLaneChangePossibility(RelativeLane fromLane, LateralDirectionality lat);

    /**
     * Returns a set of relative lanes representing the cross section. Lanes are sorted left to right.
     * @return set of relative lanes representing the cross section
     */
    SortedSet<RelativeLane> getCrossSection();

    /** {@inheritDoc} */
    @Override
    default void updateAll() throws GTUException, ParameterException
    {
        updateCrossSection();
        for (RelativeLane lane : getCrossSection())
        {
            updateInfrastructureLaneChangeInfo(lane);
            updateSpeedLimitProspect(lane);
            updateLegalLaneChangePossibility(lane, LateralDirectionality.LEFT);
            updateLegalLaneChangePossibility(lane, LateralDirectionality.RIGHT);
            updatePhysicalLaneChangePossibility(lane, LateralDirectionality.LEFT);
            updatePhysicalLaneChangePossibility(lane, LateralDirectionality.RIGHT);
        }
    }

}
