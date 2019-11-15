package org.opentrafficsim.core.network;

/**
 * Interface to determine a link weight.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 20 aug. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public interface LinkWeight
{

    /** Default link weight using link length. */
    LinkWeight LENGTH = new LinkWeight()
    {
        /** {@inheritDoc} */
        @Override
        public double getWeight(final Link link)
        {
            return link.getLength().si;
        }

        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return "LENGTH";
        }
    };

    /**
     * Returns the link weight.
     * @param link Link; link
     * @return double; link weight
     */
    double getWeight(Link link);
}
