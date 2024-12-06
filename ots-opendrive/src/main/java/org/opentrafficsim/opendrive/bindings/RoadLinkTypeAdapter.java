package org.opentrafficsim.opendrive.bindings;

import org.opentrafficsim.opendrive.generated.ERoadLinkElementType;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Adapter to change string value in to ERoadLinkElementType as it should have been defined in XSD.
 * @author wjschakel
 */
public class RoadLinkTypeAdapter extends XmlAdapter<String, ERoadLinkElementType>
{

    @Override
    public ERoadLinkElementType unmarshal(final String v)
    {
        return ERoadLinkElementType.fromValue(v);
    }

    @Override
    public String marshal(final ERoadLinkElementType v)
    {
        return v.name();
    }

}
