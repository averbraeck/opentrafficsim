package org.opentrafficsim.kpi.interfaces;

import java.util.List;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.base.Identifiable;

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
public interface LinkDataInterface extends Identifiable
{

    /**
     * @return length of the link
     */
    Length getLength();

    /**
     * @return list of lanes of the link
     */
    List<? extends LaneDataInterface> getLaneDatas();

    /**
     * @return link id
     */
    @Override
    String getId();

}
