package org.opentrafficsim.animation.gtu.colorer;

import java.util.Optional;

import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.draw.colorer.IdColorer;

/**
 * Color GTUs based on their id. If the id ends on one or more digits, the value that those digits constitute is used.
 * Otherwise, the hash code of the string representation of the id is used.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class IdGtuColorer extends IdColorer<Gtu>
{

    /**
     * Constructor.
     */
    public IdGtuColorer()
    {
        super((gtu) -> Optional.ofNullable(gtu.getId()));
    }

}
