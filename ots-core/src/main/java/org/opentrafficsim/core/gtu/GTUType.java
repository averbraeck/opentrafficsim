package org.opentrafficsim.core.gtu;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A GTU type identifies the type of a GTU. <br>
 * GTU types are used to check whether a particular GTU can travel over a particular part of infrastructure. E.g. a
 * (LaneBased)GTU with GTUType CAR can travel over lanes that have a LaneType that has the GTUType CAR in the compatibility set.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$,
 *          initial version Dec 31, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public final class GTUType implements Serializable
{
    /** */
    private static final long serialVersionUID = 20141231L;

    /** The id of the GTUType to make it identifiable. */
    private String id;

    /** ALL GTUType to be used only for permeability and accessibility. */
    public static final GTUType ALL;

    /** NONE GTUType to be used only for permeability and accessibility. */
    public static final GTUType NONE;

    /** The set of previously instantiated GTUTypes. */
    private static final Map<String, GTUType> INSTANTIATEDGTUTYPES = new LinkedHashMap<String, GTUType>();

    /* static block to guarantee that ALL is always on the first place, and NONE on the second, for code reproducibility. */
    static
    {
        ALL = makeGTUType("ALL");
        NONE = makeGTUType("NONE");
    }

    /**
     * @param id The id of the GTUType to make it identifiable.
     */
    private GTUType(final String id)
    {
        this.id = id;
    }

    /**
     * Construct a new GTUType or (if it already exists) return an existing GTUType.
     * @param id String; the id of the GTUType
     * @return GTUType; a new or existing GTUType
     */
    public static synchronized GTUType makeGTUType(final String id)
    {
        GTUType result = INSTANTIATEDGTUTYPES.get(id);
        if (null == result)
        {
            result = new GTUType(id);
            INSTANTIATEDGTUTYPES.put(id, result);
        }
        return result;
    }

    /**
     * @return id.
     */
    public String getId()
    {
        return this.id;
    }

    /** {@inheritDoc} */
    public String toString()
    {
        return "GTUType: " + this.id;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
        return result;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("checkstyle:needbraces")
    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        GTUType other = (GTUType) obj;
        if (this.id == null)
        {
            if (other.id != null)
                return false;
        }
        else if (!this.id.equals(other.id))
            return false;
        return true;
    }

    /**
     * Serialize this multiple-singleton object by serializing the id.
     * @param oos the object output stream to write the object to
     */
    private void writeObject(final ObjectOutputStream oos)
    {
        try
        {
            oos.writeObject(this.id);
        }
        catch (IOException e)
        {
            throw new RuntimeException("error during serialization of GTUType with id " + this.id, e);
        }
    }

    /**
     * Deserialize this multiple-singleton object by deserializing the id. Make sure that the GTU type is created if it does not
     * yet exist.
     * @param ois the object input stream to read the object from
     */
    private void readObject(final ObjectInputStream ois)
    {
        try
        {
            // this.id needs to be set to communicate the value to the readResolve() method
            this.id = (String) ois.readObject();
            GTUType.makeGTUType(this.id);
        }
        catch (ClassNotFoundException | IOException e)
        {
            throw new RuntimeException("error during deserialization of a GTUType", e);
        }
    }

    /**
     * Avoid that a deserialized object is a copy of the already stored object. If the object already existed, return the "old"
     * copy and dereference the deserialized object (this) to be garbage collected. If the object did not exist yet, it was just
     * added to the HashMap as "this" and can therefore be returned.
     * @return the just created object (this), or the already existing object before deserialization.
     */
    private Object readResolve()
    {
        return INSTANTIATEDGTUTYPES.get(this.id);
    }
}
