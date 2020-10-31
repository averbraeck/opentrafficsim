package org.opentrafficsim.imb.transceiver;

import nl.tno.imb.TByteBuffer;

/**
 * Convert an event received over IMB to an equivalent event for OTS (DSOL).
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Sep 9, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public interface IMBToOTSTransformer
{
    /**
     * Transform an IMB payload to a combination of an OTS (DSOL) event content and an identified listener.
     * @param imbPayload TByteBuffer; the IMB payload
     * @return IMBTransformResult; a combination of an OTS (DSOL) event content and an identified listener
     */
    IMBTransformResult transform(TByteBuffer imbPayload);

}
