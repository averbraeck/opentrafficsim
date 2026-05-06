package org.opentrafficsim.cosim.adapters;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * Adapter to use on any {@code List} attribute that can be defined as a scalar in JSON when the length is 1. To assign this
 * adapter use {@code @JsonAdapter(ScalarAsListAdapter.class)} on the attribute.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <E> underlying type within the list
 */
public class ScalarAsListAdapter<E> implements JsonSerializer<List<E>>, JsonDeserializer<List<E>>
{

    @Override
    public JsonElement serialize(final List<E> src, final Type typeOfSrc, final JsonSerializationContext context)
    {
        if (src.size() == 1)
        {
            return context.serialize(src.get(0));
        }
        JsonArray element = new JsonArray();
        src.forEach((t) -> element.add(context.serialize(t)));
        return element;
    }

    @Override
    public List<E> deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context)
            throws JsonParseException
    {
        List<E> out = new ArrayList<>();
        if (json.isJsonArray())
        {
            JsonArray element = json.getAsJsonArray();
            element.asList().forEach((t) -> out.add(context.deserialize(t, resolveType(typeOfT))));
        }
        else
        {
            out.add(context.deserialize(json, resolveType(typeOfT)));
        }
        return out;
    }

    /**
     * Returns the underlying type within the list.
     * @param typeOfT type token of the list attribute
     * @return underlying type within the list
     */
    private Type resolveType(final Type typeOfT)
    {
        if (typeOfT instanceof ParameterizedType parameterizedTypeOfT)
        {
            return parameterizedTypeOfT.getActualTypeArguments()[0];
        }
        return Object.class;
    }

}
