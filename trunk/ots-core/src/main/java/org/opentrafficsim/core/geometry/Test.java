package org.opentrafficsim.core.geometry;

import java.util.ArrayList;
import java.util.List;

import org.opentrafficsim.core.network.NetworkException;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Nov 9, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public final class Test
{
    /** */
    private Test()
    {
    }

    /**
     * @param args args
     * @throws NetworkException on error
     * @throws OTSGeometryException on error
     */
    public static void main(final String[] args) throws NetworkException, OTSGeometryException
    {
        List<OTSPoint3D> line = new ArrayList<>();
        line.add(new OTSPoint3D(77.98, 99.65));
        line.add(new OTSPoint3D(77.66, 99.72));
        line.add(new OTSPoint3D(77.37, 100.1));
        line.add(new OTSPoint3D(70.82, 109.15));
        line.add(new OTSPoint3D(70.75, 109.3));
        line.add(new OTSPoint3D(70.77, 109.42));
        line.add(new OTSPoint3D(70.86, 110.15));
        line.add(new OTSPoint3D(71.68, 115.42));
        OTSLine3D otsLine = new OTSLine3D(line);
        OTSLine3D buffer = OTSBuffering.offsetGeometry(otsLine, 2.0);
        System.out.println(buffer.toString());
    }

}
