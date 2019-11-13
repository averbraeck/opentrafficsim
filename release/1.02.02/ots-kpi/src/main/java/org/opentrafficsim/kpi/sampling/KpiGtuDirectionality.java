package org.opentrafficsim.kpi.sampling;

/**
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 12 okt. 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public enum KpiGtuDirectionality
{

    /**
     * Driving direction matches the direction of the graph, increasing fractional position when driving in this direction.
     */
    DIR_PLUS,

    /**
     * Driving direction opposite to the direction of the graph, decreasing fractional position when driving in this direction.
     */
    DIR_MINUS;

    /**
     * @return whether the gtu drives in the design direction on the link
     */
    public boolean isPlus()
    {
        return this.equals(DIR_PLUS);
    }

    /**
     * @return whether the gtu drives against the design direction on the link
     */
    public boolean isMinus()
    {
        return this.equals(DIR_MINUS);
    }

}
