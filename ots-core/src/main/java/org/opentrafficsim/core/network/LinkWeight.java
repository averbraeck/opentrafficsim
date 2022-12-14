package org.opentrafficsim.core.network;

/**
 * Interface to determine a link weight.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
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

    /** Link weight with very high penalty for Connectors. */
    LinkWeight LENGTH_NO_CONNECTORS = new LinkWeight()
    {
        /**
         * Length that should encourage Dijkstra to not include links that have this length. On the other hand, this value (when
         * on an unavoidable link) should not cause underflow problems.
         */
        static final double PROHIBITIVE_CONNECTOR_LENGTH = 1000000;

        /** {@inheritDoc} */
        @Override
        public double getWeight(final Link link)
        {
            if (link instanceof OtsLink)
            {
                OtsLink otsLink = (OtsLink) link;
                if (otsLink.getType().isConnector())
                {
                    return PROHIBITIVE_CONNECTOR_LENGTH;
                }
            }
            return link.getLength().si;
        }

        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return "LENGTH_NO_CONNECTORS";
        }
    };

    /**
     * Returns the link weight.
     * @param link Link; link
     * @return double; link weight
     */
    double getWeight(Link link);
}
