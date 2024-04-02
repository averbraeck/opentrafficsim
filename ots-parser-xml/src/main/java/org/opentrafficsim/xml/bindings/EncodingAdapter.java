package org.opentrafficsim.xml.bindings;

import org.opentrafficsim.xml.bindings.types.EncodingType;
import org.opentrafficsim.xml.bindings.types.EncodingType.Encoding;

/**
 * Adapter for Encoding expression type.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class EncodingAdapter extends StaticFieldAdapter<Encoding, EncodingType>
{

    /**
     * Constructor.
     */
    public EncodingAdapter()
    {
        super(Encoding.class, EncodingType.class);
    }

}
