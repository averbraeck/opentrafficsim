package org.opentrafficsim.kpi.sampling.meta;

import java.util.Set;

import org.djutils.exceptions.Throw;
import org.opentrafficsim.base.Identifiable;
import org.opentrafficsim.kpi.interfaces.GtuDataInterface;
import org.opentrafficsim.kpi.sampling.TrajectoryAcceptList;

/**
 * Abstract class for defining a type of filter data.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 * @param <T> class of meta data
 */
public abstract class FilterDataType<T> implements Identifiable
{

    /** Id. */
    private final String id;

    /**
     * Constructor.
     * @param id String; id
     */
    public FilterDataType(final String id)
    {
        Throw.whenNull(id, "Id may not be null.");
        this.id = id;
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
     * @param gtu GtuDataInterface; gtu to retrieve the value from
     * @return value of the meta data of this type from a GTU, may be {@code null} if not applicable.
     */
    public abstract T getValue(GtuDataInterface gtu);

    /**
     * Formats the value into a string. If the value is numeric, the default implementation is:
     * 
     * <pre>
     * String.format(format, value.si);
     * </pre>
     * 
     * @param format String; format
     * @param value T; value
     * @return formatted value
     */
    public abstract String formatValue(String format, T value);

    /**
     * Determines for a set of {@code trajectory}'s from a single GTU, which may be accepted according to this meta data type.
     * As a single GTU may have several trajectories for a single {@code TrajectoryGroup} object, the specified
     * {@code TrajectoryGroup}'s may have duplicates. As the {@code trajectory}'s are from a single GTU, the meta data is equal
     * for all. Implementations of this method may for instance:
     * <ol>
     * <li>Determine only from the first {@code Trajectory}s' meta data that all may be accepted.</li>
     * <li>Determine for the separate {@code Trajectory}'s whether they are acceptable.</li>
     * <li>The same as 2, but refuse all if any is refused.</li>
     * <li>The same as 2, but accept all if any is accepted.</li>
     * <li>etc.</li>
     * </ol>
     * The default implementation is that of 1, checking that the meta data value is in the provided query set.<br>
     * @param trajectoryAcceptList TrajectoryAcceptList; containing {@code Trajectory}'s and {@code TrajectoryGroup}'s
     *            pertaining to a single GTU
     * @param querySet Set&lt;T&gt;; set of values in the query for this metadata type
     */
    @SuppressWarnings("checkstyle:designforextension")
    public void accept(final TrajectoryAcceptList trajectoryAcceptList, final Set<T> querySet)
    {
        Throw.whenNull(trajectoryAcceptList, "Trajectory accept list may not be null.");
        Throw.whenNull(querySet, "Qeury set may not be null.");
        if (trajectoryAcceptList.getTrajectory(0).contains(this)
                && querySet.contains(trajectoryAcceptList.getTrajectory(0).getMetaData(this)))
        {
            trajectoryAcceptList.acceptAll();
        }
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return "FilterDataType [id=" + this.id + "]";
    }

}
