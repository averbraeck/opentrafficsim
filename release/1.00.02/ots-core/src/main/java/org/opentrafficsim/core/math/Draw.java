package org.opentrafficsim.core.math;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import nl.tudelft.simulation.jstats.streams.StreamInterface;
import nl.tudelft.simulation.language.Throw;

/**
 * Utility to draw from a collection.
 * <p>
 * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 23 aug. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public final class Draw
{

    /** Constructor. */
    private Draw()
    {
        // no instance
    }

    /**
     * Returns a randomly drawn element using draw weights.
     * @param map Map&lt;E, Double&gt;; map of elements and respective weights
     * @param stream StreamInterface; random number stream
     * @param <E> element type
     * @return E; randomly drawn element
     */
    public static <E> E drawWeighted(final Map<E, ? extends Double> map, final StreamInterface stream)
    {
        Throw.whenNull(map, "Map may not be null.");
        Throw.whenNull(stream, "Stream may not be null.");
        Throw.when(map.isEmpty(), IllegalArgumentException.class, "Map may not be empty.");
        double sumProb = 0.0;
        for (E e : map.keySet())
        {
            double w = map.get(e);
            Throw.when(w < 0.0, IllegalArgumentException.class, "Probabilities should be at least 0.0.");
            sumProb += w;
        }
        Throw.when(sumProb == 0.0, IllegalArgumentException.class, "Probabilities should not add to 0.0.");

        if (map.size() == 1)
        {
            return map.keySet().iterator().next();
        }
        double r = stream.nextDouble() * sumProb;
        sumProb = 0.0;
        E last = null;
        for (E e : map.keySet())
        {
            double f = map.get(e);
            if (sumProb <= r && r <= sumProb + f)
            {
                return e;
            }
            sumProb += f;
            last = e;
        }
        return last; // rounding error
    }

    /**
     * Returns a randomly drawn element using uniform weights.
     * @param collection Collection&lt;E&gt;; collection of elements
     * @param stream StreamInterface; random number stream
     * @param <E> element type
     * @return E; randomly drawn element
     */
    public static <E> E draw(final Collection<E> collection, final StreamInterface stream)
    {
        Throw.whenNull(collection, "Collection may not be null.");
        Throw.whenNull(stream, "Stream may not be null.");
        Throw.when(collection.isEmpty(), IllegalArgumentException.class, "Collection may not be empty.");
        int n = (int) (collection.size() * stream.nextDouble());
        Iterator<E> it = collection.iterator();
        int m = 0;
        while (m < n)
        {
            m++;
            it.next();
        }
        return it.next();
    }

}
