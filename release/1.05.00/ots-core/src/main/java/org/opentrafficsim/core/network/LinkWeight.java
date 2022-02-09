package org.opentrafficsim.core.network;

/**
 * Interface to determine a link weight.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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

    /** Link weight with very high penalty for Connectors. */
    LinkWeight LENGTH_NO_CONNECTORS = new LinkWeight()
    {
        /**
         * Length that should encourage Dijkstra to not include links that have this length. On the other hand, this value 
         * (when on an unavoidable link) should not cause underflow problems.
         */
        static final double PROHIBITIVE_CONNECTOR_LENGTH = 1000000;

        /** {@inheritDoc} */
        @Override
        public double getWeight(final Link link)
        {
            if (link instanceof OTSLink)
            {
                OTSLink otsLink = (OTSLink) link;
                if (otsLink.getLinkType().isConnector())
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
