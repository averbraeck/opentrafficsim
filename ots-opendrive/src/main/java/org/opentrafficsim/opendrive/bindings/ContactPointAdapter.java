package org.opentrafficsim.opendrive.bindings;

import org.opentrafficsim.opendrive.generated.EContactPoint;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Adapter to change string value in to EContactPoint as it should have been defined in XSD.
 * @author wjschakel
 */
public class ContactPointAdapter extends XmlAdapter<String, EContactPoint>
{

    @Override
    public EContactPoint unmarshal(final String v)
    {
        return EContactPoint.fromValue(v);
    }

    @Override
    public String marshal(final EContactPoint v)
    {
        return v.name();
    }

}
