package org.opentrafficsim.kpi.sampling.filter;

import java.util.Set;

import org.djutils.exceptions.Throw;
import org.opentrafficsim.kpi.interfaces.GtuData;
import org.opentrafficsim.kpi.sampling.DataType;
import org.opentrafficsim.kpi.sampling.TrajectoryAcceptList;

/**
 * Abstract class for defining a type of filter data.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <T> class of meta data
 * @param <G> GTU data type
 */
public abstract class FilterDataType<T, G extends GtuData> extends DataType<T, G>
{

    /**
     * Constructor.
     * @param id id
     * @param description description
     * @param type type class
     */
    public FilterDataType(final String id, final String description, final Class<T> type)
    {
        super(id, description, type);
    }

    /**
     * Determines for a set of {@code trajectory}'s from a single GTU, which may be accepted according to this filter data type.
     * As a single GTU may have several trajectories for a single {@code TrajectoryGroup} object, the specified
     * {@code TrajectoryGroup}'s may have duplicates. As the {@code trajectory}'s are from a single GTU, the filter data is
     * equal for all. Implementations of this method may for instance:
     * <ol>
     * <li>Determine only from the first {@code Trajectory}s' filter data that all may be accepted.</li>
     * <li>Determine for the separate {@code Trajectory}'s whether they are acceptable.</li>
     * <li>The same as 2, but refuse all if any is refused.</li>
     * <li>The same as 2, but accept all if any is accepted.</li>
     * <li>etc.</li>
     * </ol>
     * The default implementation is that of 1, checking that the filter data value is in the provided query set.<br>
     * @param trajectoryAcceptList containing {@code Trajectory}'s and {@code TrajectoryGroup}'s pertaining to a single GTU, all
     *            assumed not accepted
     * @param querySet set of values in the query for this metadata type
     */
    @SuppressWarnings("checkstyle:designforextension")
    public void accept(final TrajectoryAcceptList trajectoryAcceptList, final Set<T> querySet)
    {
        Throw.whenNull(trajectoryAcceptList, "Trajectory accept list may not be null.");
        Throw.whenNull(querySet, "Qeury set may not be null.");
        if (trajectoryAcceptList.getTrajectory(0).contains(this)
                && querySet.contains(trajectoryAcceptList.getTrajectory(0).getFilterData(this)))
        {
            trajectoryAcceptList.acceptAll();
        }
    }

    @Override
    public String toString()
    {
        return "FilterDataType [id=" + getId() + ", description=" + getDescription() + "]";
    }

}
