package org.opentrafficsim.cosim.adapters;

import java.io.IOException;

import org.opentrafficsim.cosim.messages.CommandMessage.CommandType;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * Adapter to read and write {@code CommandType} values in JSON files.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Wouter Schakel
 */
public class CommandTypeAdapter extends TypeAdapter<CommandType>
{

    @Override
    public void write(final JsonWriter out, final CommandType value) throws IOException
    {
        out.value(value.printValue());

    }

    @Override
    public CommandType read(final JsonReader in) throws IOException
    {
        String value = in.nextString();
        for (CommandType commandType : CommandType.values())
        {
            if (commandType.printValue().equals(value))
            {
                return commandType;
            }
        }
        throw new IOException("Value " + value + " is not a valid CommandType.");
    }

}
