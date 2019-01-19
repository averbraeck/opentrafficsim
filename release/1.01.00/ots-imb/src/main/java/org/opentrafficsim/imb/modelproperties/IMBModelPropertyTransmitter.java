package org.opentrafficsim.imb.modelproperties;

/**
 * This interface defines that should be implemented in order to transmit a model property to an IMB Model Controller.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Oct 26, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public interface IMBModelPropertyTransmitter
{
    /**
     * Construct an Object array containing the objects that must be transmitted. Each of these objects must be a Boolean,
     * Integer, Double, or String,or implement prepare and qWrite.
     * @return Object[]; Object array containing the objects that must be transmitted
     */
    public Object[] buildMessage();

}
