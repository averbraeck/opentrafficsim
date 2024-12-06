package org.opentrafficsim.opendrive.bindings;

import org.djunits.value.vdouble.scalar.Length;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Length adapter.
 * @author wjschakel
 */
public class LengthAdapter extends XmlAdapter<String, Length>
{

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
