package org.opentrafficsim.opendrive.bindings;

import org.opentrafficsim.opendrive.generated.ERoadLinkElementType;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Adapter to change string value in to ERoadLinkElementType as it should have been defined in XSD.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class RoadLinkTypeAdapter extends XmlAdapter<String, ERoadLinkElementType>
{

    /**
     * Constructor.
     */
    public RoadLinkTypeAdapter()
    {
        //
    }

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
