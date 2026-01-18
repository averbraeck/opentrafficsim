package org.opentrafficsim.kpi.sampling.filter;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import org.djutils.exceptions.Throw;
import org.opentrafficsim.kpi.interfaces.GtuData;
import org.opentrafficsim.kpi.sampling.CrossSection;
import org.opentrafficsim.kpi.sampling.CrossSection.LanePosition;
import org.opentrafficsim.kpi.sampling.Trajectory;
import org.opentrafficsim.kpi.sampling.TrajectoryAcceptList;
import org.opentrafficsim.kpi.sampling.TrajectoryGroup;

/**
 * Accepts trajectories that have passed all cross sections as defined in a query.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class FilterDataCrossSections extends FilterDataType<CrossSection, GtuData>
{

    /**
     * Constructor.
     */
    public FilterDataCrossSections()
    {
        super("crossSection", "Cross sections", CrossSection.class);
    }

    /**
     * Accepts all trajectory's or rejects all trajectory's depending on whether all cross sections have been crossed.
     */
    @Override
    public final void accept(final TrajectoryAcceptList trajectoryAcceptList, final Set<CrossSection> querySet)
    {
        Throw.whenNull(trajectoryAcceptList, "Trajectory accept list may not be null.");
        Throw.whenNull(querySet, "Qeury set may not be null.");
        Set<CrossSection> crossedCrossSections = new LinkedHashSet<>();
        for (int i = 0; i < trajectoryAcceptList.size(); i++)
        {
            TrajectoryGroup<?> trajectoryGroup = trajectoryAcceptList.getTrajectoryGroup(i);
            for (CrossSection crossSection : querySet)
            {
                for (LanePosition lanePosition : crossSection)
                {
                    // If trajectoryGroup is of same lane, check position
                    if (trajectoryGroup.getLane().equals(lanePosition.lane()))
                    {
                        double position = lanePosition.position().si;
                        Trajectory<?> trajectory = trajectoryAcceptList.getTrajectory(i);
                        double xStart = trajectory.getX(0);
                        double xEnd = trajectory.getX(trajectory.size() - 1);
                        if ((xStart < position && position < xEnd) || (xEnd < position && position < xStart))
                        {
                            // Trajectory was up- and downstream of the location, so the location was crossed
                            crossedCrossSections.add(crossSection);
                        }
                    }
                }
            }
        }
        if (querySet.equals(crossedCrossSections))
        {
            trajectoryAcceptList.acceptAll();
        }
    }

    @Override
    public String toString()
    {
        return "FilterDataCrossSections: [id=" + getId() + "]";
    }

    @Override
    public Optional<CrossSection> getValue(final GtuData gtu)
    {
        return Optional.empty(); // value is not used to check whether trajectory is accepted
    }

}
