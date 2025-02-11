package org.opentrafficsim.opendrive.bindings;

import org.djunits.value.vdouble.scalar.Length;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Length adapter.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class LengthAdapter extends XmlAdapter<String, Length>
{

    /**
     * Constructor.
     */
    public LengthAdapter()
    {
        //
    }

    @Override
    public Length unmarshal(final String v)
    {
        String str = v.strip();
        if (Character.isAlphabetic(str.charAt(str.length() - 1)))
        {
            return Length.valueOf(str); // m, km, ft and mile supported in same abbreviation in Length
        }
        return Length.instantiateSI(Double.valueOf(str));
    }

    @Override
    public String marshal(final Length v)
    {
        return v.toString();
    }

}
