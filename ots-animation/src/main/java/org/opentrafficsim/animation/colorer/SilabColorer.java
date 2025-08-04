package org.opentrafficsim.animation.colorer;

import org.opentrafficsim.animation.gtu.colorer.GtuColorer;
import org.opentrafficsim.core.gtu.Gtu;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Fixed color colorer.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class SilabColorer implements GtuColorer, Serializable
{

    /** */
    private static final long serialVersionUID = 20250804L;

    private String avId;
    private String userId;

    public SilabColorer(String avId, String userId) {
        this.avId = avId;
        this.userId = userId;
    }


    /** {@inheritDoc} */
    @Override
    public Color getColor(final Gtu gtu)
    {
        if (gtu.getId().equals(this.avId))
        {
            return Color.MAGENTA;
        } else if (gtu.getId().equals(this.userId)) {
            return Color.BLUE;
        } else {
            return Color.BLACK;
        }
    }

    /** {@inheritDoc} */
    @Override
    public List<LegendEntry> getLegend()
    {
        return new ArrayList<>();
    }
}
