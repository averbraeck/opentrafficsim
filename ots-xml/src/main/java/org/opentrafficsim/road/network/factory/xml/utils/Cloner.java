package org.opentrafficsim.road.network.factory.xml.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.opentrafficsim.road.network.factory.xml.XmlParserException;

/**
 * Cloner makes a deep clone of any serializable object with serializable fields.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public final class Cloner
{

    /** */
    private Cloner()
    {
        // utility class
    }

    /**
     * Clone an object that is serializable and that has serializable fields.
     * @param object the object to clone
     * @param <T> the type of the object to clone
     * @return the clone of the object
     * @throws XmlParserException on cloning error
     */
    @SuppressWarnings("unchecked")
    public static <T extends Serializable> T clone(final T object) throws XmlParserException
    {
        try
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(object);
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            return (T) ois.readObject();
        }
        catch (SecurityException | IOException | ClassNotFoundException exception)
        {
            throw new XmlParserException(exception);
        }
    }

}
