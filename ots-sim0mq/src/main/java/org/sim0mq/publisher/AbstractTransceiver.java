package org.sim0mq.publisher;

import org.djunits.Throw;
import org.djutils.metadata.MetaData;

/**
 * Common code for most implementations of TranceiverInterface.
 * <p>
 * Copyright (c) 2020-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public abstract class AbstractTransceiver implements TransceiverInterface
{
    /** The address field descriptors. */
    private final MetaData addressFields;

    /** The result field descriptors. */
    private final MetaData resultFields;

    /** The id of the AbstractTransceiver. */
    private final String id;

    /**
     * Construct a new AbstractTransceiver.
     * @param id String; the id of the new AbstractTransceiver
     * @param addressFields MetaData; description of the elements of an address that the <code>get</code> method of this
     *            AbstractTransceiver can handle
     * @param resultFields MetaData; description of the result of the <code>get</code> method
     */
    public AbstractTransceiver(final String id, final MetaData addressFields, final MetaData resultFields)
    {
        Throw.whenNull(id, "id may not be null");
        Throw.whenNull(addressFields, "addressFieldDescriptors may not be null");
        Throw.whenNull(resultFields, "resultFieldDescriptors may not be null");
        this.id = id;
        this.addressFields = addressFields;
        this.resultFields = resultFields;
    }

    /** {@inheritDoc} */
    @Override
    public final String getId()
    {
        return this.id;
    }

    /** {@inheritDoc} */
    @Override
    public final MetaData getAddressFields()
    {
        return this.addressFields;
    }

    /** {@inheritDoc} */
    @Override
    public final MetaData getResultFields()
    {
        return this.resultFields;
    }

    /**
     * Verify the composition of an Object[].
     * @param metaData MetaData; the expected composition
     * @param address Object[]; the object array that must be verified
     * @return String; null if metaData is OK; descriptive text on error
     */
    public static String verifyMetaData(final MetaData metaData, final Object[] address)
    {
        if ((metaData.size() == 0 || metaData.size() == 1) && address == null)
        {
            return null;
        }
        if (metaData.equals(MetaData.NO_META_DATA)) // anything goes
        {
            return null;
        }
        if (null == address)
        {
            return ("Address may not be null");
        }
        if (address.length != metaData.size())
        {
            return String.format("Address for %s has wrong length (expected %d, got %d)", metaData.getName(), metaData.size(),
                    address.length);
        }
        for (int index = 0; index < address.length; index++)
        {
            Object object = address[index];
            if ((null != object) && (!(metaData.getObjectClass(index).isAssignableFrom(object.getClass()))))
            {
                return String.format("objectArray[%d] (%s) cannot be used for %s", index, address[index],
                        metaData.getObjectClass(index).getName());
            }
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "AbstractTransceiver [id=" + id + ", addressFields=" + addressFields + ", resultFields=" + resultFields + "]";
    }

}
