package org.opentrafficsim.kpi.sampling.meta;

import java.util.Set;

import org.djutils.base.Identifiable;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.kpi.interfaces.GtuData;
import org.opentrafficsim.kpi.sampling.TrajectoryAcceptList;

/**
 * Abstract class for defining a type of filter data.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <T> class of meta data
 * @param <G> gtu data type
 */
public abstract class FilterDataType<T, G extends GtuData> implements Identifiable
{

    /** Id. */
    private final String id;
    
    /** Description. */
    private final String description;

    /**
     * Constructor.
     * @param id String; id
     * @param description String; description
     */
    public FilterDataType(final String id, final String description)
    {
        Throw.whenNull(id, "Id may not be null.");
        this.id = id;
        this.description = description;
    }

    /**
     * Returns the id.
     * @return id
     */
    @Override
    public final String getId()
    {
        return this.id;
    }

    /**
     * Retrieves the value of the meta data of this type from a GTU.
     * @param gtu G; gtu to retrieve the value from
     * @return value of the meta data of this type from a GTU, may be {@code null} if not applicable.
     */
    public abstract T getValue(G gtu);

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
     * @param trajectoryAcceptList TrajectoryAcceptList; containing {@code Trajectory}'s and {@code TrajectoryGroup}'s
     *            pertaining to a single GTU, all assumed not accepted
     * @param querySet Set&lt;T&gt;; set of values in the query for this metadata type
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

    /**
     * Returns the description.
     * @return String; description.
     */
    public String getDescription()
    {
        return this.description;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "FilterDataType [id=" + this.id + ", description=" + this.description + "]";
    }
    
}
