package org.opentrafficsim.draw.graphs;

import java.util.Iterator;
import java.util.List;

/**
 * Common functionality between a cross-section and a path.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 23 okt. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <S> underlying type of path sections
 */
public abstract class AbstractGraphSpace<S> implements Iterable<S>
{

    /** Series names. */
    private final List<String> seriesNames;

    /**
     * Constructor.
     * @param seriesNames List&lt;String&gt;; names of series
     */
    public AbstractGraphSpace(final List<String> seriesNames)
    {
        this.seriesNames = seriesNames;
    }

    /**
     * Returns the name of the series.
     * @param series int; series
     * @return String; name of the series
     */
    public final String getName(final int series)
    {
        return this.seriesNames.get(series);
    }

    /**
     * Returns the number of series.
     * @return int; number of series
     */
    public final int getNumberOfSeries()
    {
        return this.seriesNames.size();
    }

    /**
     * Returns an iterator over the sources on the given series.
     * @param series int; number of the series
     * @return Iterator&lt;S&gt;; iterator over the sources on the given series
     */
    abstract Iterator<S> iterator(int series);

}
