package org.opentrafficsim.kpi.sampling.meta;

import java.util.Set;

import org.opentrafficsim.kpi.interfaces.GtuDataInterface;
import org.opentrafficsim.kpi.sampling.TrajectoryAcceptList;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Sep 22, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <T> class of meta data
 */
public abstract class MetaDataType<T>
{

    /** Id. */
    private final String id;

    /**
     * @param id id
     */
    public MetaDataType(final String id)
    {
        this.id = id;
    }

    /**
     * @return id.
     */
    public final String getId()
    {
        return this.id;
    }

    /**
     * Retrieves the value of the meta data of this type from a GTU.
     * @param gtu gtu to retrieve the value from
     * @return value of the meta data of this type from a GTU, may be {@code null} if not applicable.
     */
    public abstract T getValue(final GtuDataInterface gtu);
    
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
     * The default implementation is that of 1.<br>
     * @param trajectoryAcceptList containing {@code Trajectory}'s and {@code TrajectoryGroup}'s pertaining to a single GTU
     * @param querySet set of values in the query for this metadata type
     */
    @SuppressWarnings("checkstyle:designforextension")
    public void accept(final TrajectoryAcceptList trajectoryAcceptList, final Set<T> querySet)
    {
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
        return "MetaDataType [id=" + this.id + "]";
    }

}
