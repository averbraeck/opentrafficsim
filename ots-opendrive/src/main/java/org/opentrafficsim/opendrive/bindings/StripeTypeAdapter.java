package org.opentrafficsim.opendrive.bindings;

import org.opentrafficsim.opendrive.generated.ERoadMarkType;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Adapter for road mark and OTS stripe type.
 * @author wjschakel
 */
public class StripeTypeAdapter extends XmlAdapter<ERoadMarkType, Stripe.Type>
{
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
