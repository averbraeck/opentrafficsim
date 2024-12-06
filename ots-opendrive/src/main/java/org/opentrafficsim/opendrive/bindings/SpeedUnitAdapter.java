package org.opentrafficsim.opendrive.bindings;

import org.djunits.unit.SpeedUnit;
import org.opentrafficsim.opendrive.generated.EUnitSpeed;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

/**
 * SpeedUnit adapter.
 * @author wjschakel
 */
public class SpeedUnitAdapter extends XmlAdapter<EUnitSpeed, SpeedUnit>
{

    @Override
    public SpeedUnit unmarshal(final EUnitSpeed v)
    {
        switch (v)
        {
            case KM_H:
                return SpeedUnit.KM_PER_HOUR;
            case MPH:
                return SpeedUnit.MILE_PER_HOUR;
            case M_S:
                return SpeedUnit.METER_PER_SECOND;
            default:
                return SpeedUnit.SI;
        }
    }

    @Override
    public EUnitSpeed marshal(final SpeedUnit v)
    {
        if (SpeedUnit.KM_PER_HOUR.equals(v))
        {
            return EUnitSpeed.KM_H;
        }
        if (SpeedUnit.MILE_PER_HOUR.equals(v))
        {
            return EUnitSpeed.MPH;
        }
        return EUnitSpeed.M_S;
    }

}
