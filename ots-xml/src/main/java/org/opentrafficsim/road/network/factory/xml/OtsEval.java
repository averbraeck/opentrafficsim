package org.opentrafficsim.road.network.factory.xml;

import java.util.LinkedHashMap;
import java.util.Map;

import org.djutils.eval.Eval;
import org.djutils.eval.Function;
import org.djutils.metadata.MetaData;

/**
 * Extends {@link Eval} to add default additional functions.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.<br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * @author Wouter Schakel
 */
public class OtsEval extends Eval
{

    /** Custom functions. */
    private static final Map<String, Function> CUSTOM_FUNCTIONS = new LinkedHashMap<>();

    static
    {
        CUSTOM_FUNCTIONS.put("min", new Function()
        {
            @Override
            public String getId()
            {
                return "min";
            }

            @Override
            public MetaData getMetaData()
            {
                return MetaData.NO_META_DATA;
            }

            @Override
            public Object function(final Object[] arguments) throws RuntimeException
            {
                Number out = null;
                for (Object obj : arguments)
                {
                    if (obj instanceof Number num)
                    {
                        out = out == null || Double.compare(num.doubleValue(), out.doubleValue()) < 0 ? num : out;
                    }
                    else
                    {
                        throw new IllegalArgumentException("Unable to evaluate min function on value " + obj);
                    }
                }
                return out;
            }
        });

        CUSTOM_FUNCTIONS.put("max", new Function()
        {
            @Override
            public String getId()
            {
                return "max";
            }

            @Override
            public MetaData getMetaData()
            {
                return MetaData.NO_META_DATA;
            }

            @Override
            public Object function(final Object[] arguments) throws RuntimeException
            {
                Number out = null;
                for (Object obj : arguments)
                {
                    if (obj instanceof Number num)
                    {
                        out = out == null || Double.compare(num.doubleValue(), out.doubleValue()) > 0 ? num : out;
                    }
                    else
                    {
                        throw new IllegalArgumentException("Unable to evaluate max function on value " + obj);
                    }
                }
                return out;
            }
        });
    }

    /**
     * Constructor.
     */
    public OtsEval()
    {
        setUserDefinedFunctions(CUSTOM_FUNCTIONS);
    }

}
