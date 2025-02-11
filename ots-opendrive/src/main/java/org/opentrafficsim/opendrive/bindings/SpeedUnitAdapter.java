package org.opentrafficsim.opendrive.bindings;

import org.djunits.unit.SpeedUnit;
import org.opentrafficsim.opendrive.generated.EUnitSpeed;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

/**
 * SpeedUnit adapter.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class SpeedUnitAdapter extends XmlAdapter<EUnitSpeed, SpeedUnit>
{

    /**
     * Constructor.
     */
    public SpeedUnitAdapter()
    {
        //
    }

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
