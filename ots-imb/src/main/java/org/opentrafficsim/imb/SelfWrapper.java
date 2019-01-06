package org.opentrafficsim.imb;

import nl.tno.imb.TByteBuffer;

/**
 * Interface for objects that implement prepare and qWrite.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Oct 27, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public interface SelfWrapper
{
    /**
     * Perform the prepare operation.
     * @param tByteBuffer TByteBuffer; the buffer to prepare for reception of the object that implements this interface
     */
    public void prepare(TByteBuffer tByteBuffer);

    /**
     * Perform the qWrite operation.
     * @param tByteBuffer TByteBuffer; the buffer on which to apply the qWrite operation
     */
    public void qWrite(TByteBuffer tByteBuffer);

}
