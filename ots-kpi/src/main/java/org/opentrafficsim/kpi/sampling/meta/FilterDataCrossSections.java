package org.opentrafficsim.kpi.sampling.meta;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.djutils.exceptions.Throw;
import org.opentrafficsim.kpi.interfaces.GtuData;
import org.opentrafficsim.kpi.sampling.CrossSection;
import org.opentrafficsim.kpi.sampling.LanePosition;
import org.opentrafficsim.kpi.sampling.TrajectoryAcceptList;
import org.opentrafficsim.kpi.sampling.TrajectoryGroup;

/**
 * Accepts trajectories that have passed all cross sections as defined in a query.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class FilterDataCrossSections extends FilterDataType<CrossSection>
{

    /**
     * Constructor.
     */
    public FilterDataCrossSections()
    {
        super("crossSection", "Cross sections");
    }

    /** {@inheritDoc} */
    @Override
    public final CrossSection getValue(final GtuData gtu)
    {
        return null;
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
        // Loop over trajectoryList/trajectoryGroupList combo
        for (int i = 0; i < trajectoryAcceptList.size(); i++)
        {
            TrajectoryGroup<?> trajectoryGroup = trajectoryAcceptList.getTrajectoryGroup(i);
            // Loop over cross sections
            Iterator<CrossSection> crossSectionIterator = querySet.iterator();
            while (crossSectionIterator.hasNext())
            {
                CrossSection crossSection = crossSectionIterator.next();
                // Loop over lanes in cross section
                Iterator<LanePosition> lanePositionIterator = crossSection.getIterator();
                while (lanePositionIterator.hasNext())
                {
                    LanePosition lanePosition = lanePositionIterator.next();
                    // If Trajectories is of same lane, check position
                    if (trajectoryGroup.getLane().equals(lanePosition.getLaneData()))
                    {
                        double position = lanePosition.getPosition().si;
                        float[] x = trajectoryAcceptList.getTrajectory(i).getX();
                        double xStart = x[0];
                        double xEnd = x[x.length - 1];
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

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return "FilterDataCrossSections: [id=" + getId() + "]";
    }

}
