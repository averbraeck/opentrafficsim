package org.opentrafficsim.road.network.sampling;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

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
     * Determines for a set of {@code trajectory}'s from a single GTU, which may be accepted according to this meta data type.
     * As a single GTU may have several trajectories for a single {@code Trajectories} object, the specified
     * {@code Trajectories}'s may have duplicates. As the {@code trajectory}'s are from a single GTU, the meta data is equal for
     * all. Implementations of this method may for instance:
     * <ol>
     * <li>Determine only from the first {@code Trajectory}s' meta data that all may be accepted.</li>
     * <li>Determine for the separate {@code Trajectory}'s whether they are acceptable.</li>
     * <li>The same as 2, but refuse all if any is refused.</li>
     * <li>The same as 2, but accept all if any is accepted.</li>
     * <li>etc.</li>
     * </ol>
     * The default implementation is that of 1.<br>
     * @param trajectoryList list of trajectory's from a single GTU within the span of a single query
     * @param trajectoriesList list of trajectories from which the respective trajectory's have been taken
     * @param querySet set of values in the query for this metadata type
     * @return boolean array of length equal to {@code trajectoryList} and {@code trajectoriesList}
     */
    @SuppressWarnings("checkstyle:designforextension")
    protected boolean[] accept(final List<Trajectory> trajectoryList, final List<Trajectories> trajectoriesList,
        final Set<T> querySet)
    {
        boolean[] out = new boolean[trajectoryList.size()];
        if (trajectoryList.get(0).contains(this) && querySet.contains(trajectoryList.get(0).getMetaData(this)))
        {
            Arrays.fill(out, true);
        }
        return out;
    }

}
