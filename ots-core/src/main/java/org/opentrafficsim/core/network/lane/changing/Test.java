package org.opentrafficsim.core.network.lane.changing;

import org.opentrafficsim.core.OTS_SCALAR;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNode;
import org.opentrafficsim.core.network.lane.CrossSectionLink;
import org.opentrafficsim.core.network.lane.Lane;
import org.opentrafficsim.core.network.lane.LaneType;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Sep 14, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class Test implements OTS_SCALAR
{
    public void makeLane() throws OTSGeometryException, NetworkException
    {
        LaneType laneType = new LaneType("HIGHWAY");
        OTSNode node1 = new OTSNode("n1", new OTSPoint3D(0.0, 0.0));
        OTSNode node2 = new OTSNode("n2", new OTSPoint3D(100.0, 0.0));
        OTSLine3D line12 = new OTSLine3D(new OTSPoint3D[]{node1.getPoint(), node2.getPoint()});
//        CrossSectionLink link12 = new CrossSectionLink("l12", node1, node2, line12, LaneKeepingPolicy.KEEP_RIGHT);
//        Lane lane12 =
//            new Lane(link12, "A1", new Length.Rel(0.0, METER), new Length.Rel(0.0, METER), new Length.Rel(3.0, METER),
//                new Length.Rel(3.0, METER), laneType, LongitudinalDirectionality.FORWARD, new Speed.Abs(100.0,
//                    KM_PER_HOUR), new OvertakingConditions.LeftAlwaysRightSpeed(new Speed.Abs(25.0, KM_PER_HOUR));
    }
}
