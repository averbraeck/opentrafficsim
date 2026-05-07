package org.opentrafficsim.cosim.adapters;

import java.io.IOException;

import org.djunits.value.vdouble.scalar.Frequency;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * Adapter to read and write {@code Frequency} values in JSON files.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Wouter Schakel
 */
public class FrequencyAdapter extends TypeAdapter<Frequency>
{

    @Override
    public void write(final JsonWriter out, final Frequency value) throws IOException
    {
        out.value(value.toString());
    }

    @Override
    public Frequency read(final JsonReader in) throws IOException
    {
        return Frequency.valueOf(in.nextString());
    }

}
