package org.opentrafficsim.demo;

import java.awt.Color;
import java.util.LinkedHashMap;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.djutils.immutablecollections.Immutable;
import org.djutils.immutablecollections.ImmutableLinkedHashMap;
import org.djutils.immutablecollections.ImmutableMap;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterSet;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.definitions.DefaultsNl;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.draw.gtu.DefaultCarAnimation.GtuData.GtuMarker;

/**
 * Factory for defaults in demos.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class DefaultsFactory
{

    /** Standard drawing colors for GTU types. */
    public static final ImmutableMap<GtuType, Color> GTU_TYPE_COLORS;

    /** Standard markers for GTU types. */
    public static final ImmutableMap<GtuType, GtuMarker> GTU_TYPE_MARKERS;

    static
    {
        LinkedHashMap<GtuType, Color> colorMap = new LinkedHashMap<>();
        colorMap.put(DefaultsNl.CAR, Color.BLUE);
        colorMap.put(DefaultsNl.TRUCK, Color.RED);
        colorMap.put(DefaultsNl.VEHICLE, Color.GRAY);
        colorMap.put(DefaultsNl.PEDESTRIAN, Color.YELLOW);
        colorMap.put(DefaultsNl.MOTORCYCLE, Color.PINK);
        colorMap.put(DefaultsNl.BICYCLE, Color.GREEN);
        GTU_TYPE_COLORS = new ImmutableLinkedHashMap<>(colorMap, Immutable.WRAP);

        LinkedHashMap<GtuType, GtuMarker> markerMap = new LinkedHashMap<>();
        markerMap.put(DefaultsNl.TRUCK, GtuMarker.SQUARE);
        GTU_TYPE_MARKERS = new ImmutableLinkedHashMap<>(markerMap, Immutable.WRAP);
    }

    /**
     * Do not create instance.
     */
    private DefaultsFactory()
    {
        //
    }

    /**
     * Returns a default set of parameters.
     * @return Default set of parameters.
     */
    public static Parameters getDefaultParameters()
    {

        Parameters params = new ParameterSet().setDefaultParameters(ParameterTypes.class);

        // demos use different value from default LMRS value
        try
        {
            params.setParameter(ParameterTypes.LOOKAHEAD, new Length(250, LengthUnit.SI));
        }
        catch (ParameterException pe)
        {
            throw new RuntimeException("Parameter type 'LOOKAHEAD' could not be set.", pe);
        }

        return params;

    }

}
