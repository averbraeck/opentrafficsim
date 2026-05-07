package org.opentrafficsim.cosim.adapters;

import java.io.IOException;

import org.djunits.value.vdouble.scalar.Length;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * Adapter to read and write {@code Length} values in JSON files.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Wouter Schakel
 */
public class LengthAdapter extends TypeAdapter<Length>
{

    @Override
    public void write(final JsonWriter out, final Length value) throws IOException
    {
        out.value(value.toString());
    }

    @Override
    public Length read(final JsonReader in) throws IOException
    {
        return Length.valueOf(in.nextString());
    }

}
