package org.sim0mq.message.types;

import org.djunits.unit.Unit;
import org.djunits.value.Scalar;
import org.sim0mq.message.EndianUtil;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Mar 4, 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public final class TypesUtil
{

    /**
     * Utility class.
     */
    private TypesUtil()
    {
        // Utility class.
    }

    /**
     * Encode a unit scalar, big endian into the given byte array.
     * @param scalar the djunits scalar to encode
     * @param message the ZeroMQ byte array to put the value in
     * @param pointer the first byte to consider
     * @return the new pointer into the array
     */
    /*-
    public static <U extends Unit<U>> int EncodeDoubleScalar(
            final Scalar<U> scalar, final byte[] message, final int pointer)
    {
        int p = pointer;
        byte code = Sim0MQUnitType.getUnitCode(scalar.getUnit());
        message[p++] = code;
        if (code < 101)
        {
            byte displayCode = Sim0MQDisplayType.getDisplayCode(code, scalar.getUnit());
            message[p++] = code;
        }
        else
        {
            byte[] displayCode = Sim0MQDisplayType.getMoneyDisplayCode(code, scalar.getUnit());
            message[p++] = displayCode[0];
            message[p++] = displayCode[1];
        }
        double si = scalar.doubleValue();
        p = EndianUtil.encodeDoubleBigEndian(si, message, p);
        return p;
    }
     */
}
