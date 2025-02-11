package org.opentrafficsim.opendrive.bindings;

import org.opentrafficsim.opendrive.generated.EContactPoint;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Adapter to change string value in to EContactPoint as it should have been defined in XSD.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class ContactPointAdapter extends XmlAdapter<String, EContactPoint>
{

    /**
     * Constructor.
     */
    public ContactPointAdapter()
    {
        //
    }

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
