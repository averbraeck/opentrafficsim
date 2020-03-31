package org.sim0mq.publisher;

import org.opentrafficsim.base.Identifiable;

/**
 * Machine interpretable description of a transceiver.
 * <p>
 * Copyright (c) 2020-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2020-02-13 11:08:16 +0100 (Thu, 13 Feb 2020) $, @version $Revision: 6383 $, by $Author: pknoppers $,
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public interface TransceiverMetaData extends Identifiable
{
    /**
     * Number of arguments needed in a subscription request.
     * @return int; the number of arguments needed in a subscription request
     */
    int addressLevels();

    /**
     * Enquire the java class name of one argument needed in a subscription request.
     * @param addressLevel int; index of the argument
     * @return String; java class name of the argument
     */
    String getAddressFieldClass(int addressLevel);

    /**
     * Retrieve the description of one argument needed in a subscription request.
     * @param addressLevel int; index of the argument
     * @return String; description of the argument
     */
    String getAddressFieldDescription(int addressLevel);

    /**
     * Retrieve the TransceiverMetadata object that can be used to get valid values for one argument in a subscription request.
     * @param addressLevel int; index of the argument
     * @return TransceiverMetaData; to be used to get valid values for argument <code>addressLevel</code>, or null if valid
     *         values for the argument at index <code>addressLevel</code> can not be obtained through a TransceiverMetaData
     *         object
     */
    TransceiverMetaData getIdSource(int addressLevel);
    
}
