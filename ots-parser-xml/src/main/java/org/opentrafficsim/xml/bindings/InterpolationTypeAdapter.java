package org.opentrafficsim.xml.bindings;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.opentrafficsim.road.od.Interpolation;

public class InterpolationTypeAdapter extends XmlAdapter<String, Interpolation>
{

    @Override
    public Interpolation unmarshal(final String value)
    {
        return "STEPWISE".equals(value) ? Interpolation.STEPWISE : Interpolation.LINEAR;
    }

    @Override
    public String marshal(final Interpolation value)
    {
        return value.name();
    }

}
