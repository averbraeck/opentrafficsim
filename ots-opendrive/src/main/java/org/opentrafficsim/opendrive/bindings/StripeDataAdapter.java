package org.opentrafficsim.opendrive.bindings;

import org.opentrafficsim.opendrive.generated.ERoadMarkType;
import org.opentrafficsim.road.definitions.DefaultsRoadNl;
import org.opentrafficsim.road.network.StripeData;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Adapter between {@code ERoadMarkType} and {@link StripeData}.
 * <p>
 * Copyright (c) 2024-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class StripeDataAdapter extends XmlAdapter<ERoadMarkType, StripeData>
{

    @Override
    public StripeData unmarshal(final ERoadMarkType v) throws UnsupportedOperationException
    {

        switch (v)
        {
            case BOTTS_DOTS:
            case BROKEN:
                return DefaultsRoadNl.DASHED;
            case BROKEN_BROKEN:
                return DefaultsRoadNl.DOUBLE_DASHED;
            case BROKEN_SOLID:
                return DefaultsRoadNl.RIGHT;
            case CURB:
            case EDGE:
            case SOLID:
                return DefaultsRoadNl.SOLID;
            case SOLID_BROKEN:
                return DefaultsRoadNl.LEFT;
            case SOLID_SOLID:
                return DefaultsRoadNl.DOUBLE_SOLID;
            case CUSTOM:
            case GRASS:
            case NONE:
            default:
                throw new UnsupportedOperationException("Unsupported line type " + v);
        }
    }

    @Override
    public ERoadMarkType marshal(final StripeData v) throws UnsupportedOperationException
    {
        if (v.equals(DefaultsRoadNl.DASHED))
        {
            return ERoadMarkType.BROKEN;
        }
        if (v.equals(DefaultsRoadNl.DOUBLE_DASHED))
        {
            return ERoadMarkType.BROKEN_BROKEN;
        }
        if (v.equals(DefaultsRoadNl.RIGHT))
        {
            return ERoadMarkType.BROKEN_SOLID;
        }
        if (v.equals(DefaultsRoadNl.SOLID))
        {
            return ERoadMarkType.SOLID;
        }
        if (v.equals(DefaultsRoadNl.LEFT))
        {
            return ERoadMarkType.SOLID_BROKEN;
        }
        if (v.equals(DefaultsRoadNl.DOUBLE_SOLID))
        {
            return ERoadMarkType.SOLID_SOLID;
        }
        throw new UnsupportedOperationException("Unsupported road mark " + v);
    }

}
