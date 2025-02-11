package org.opentrafficsim.opendrive.bindings;

import org.opentrafficsim.opendrive.generated.ERoadMarkType;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Adapter for road mark and OTS stripe type.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class StripeTypeAdapter extends XmlAdapter<ERoadMarkType, Stripe.Type>
{

    /**
     * Constructor.
     */
    public StripeTypeAdapter()
    {
        //
    }

    @Override
    public Stripe.Type unmarshal(final ERoadMarkType v) throws UnsupportedOperationException
    {
        switch (v)
        {
            case BOTTS_DOTS:
            case BROKEN:
            case BROKEN_BROKEN:
                return Stripe.Type.DASHED;
            case BROKEN_SOLID:
                return Stripe.Type.RIGHT;
            case CURB:
            case EDGE:
            case SOLID:
                return Stripe.Type.SOLID;
            case SOLID_BROKEN:
                return Stripe.Type.LEFT;
            case SOLID_SOLID:
                return Stripe.Type.DOUBLE;
            case CUSTOM:
            case GRASS:
            case NONE:
            default:
                throw new UnsupportedOperationException("Unsupported line type " + v);
        }
    }

    @Override
    public ERoadMarkType marshal(final Stripe.Type v) throws UnsupportedOperationException
    {
        switch (v)
        {
            case BLOCK:
            case DASHED:
                return ERoadMarkType.BROKEN;
            case DOUBLE:
                return ERoadMarkType.SOLID_SOLID;
            case LEFT:
                return ERoadMarkType.SOLID_BROKEN;
            case RIGHT:
                return ERoadMarkType.BROKEN_SOLID;
            case SOLID:
                return ERoadMarkType.SOLID;
            default:
                throw new UnsupportedOperationException("Unsupported road mark " + v);
        }
    }
}
