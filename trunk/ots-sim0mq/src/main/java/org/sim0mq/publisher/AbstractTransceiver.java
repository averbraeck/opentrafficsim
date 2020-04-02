package org.sim0mq.publisher;

import java.util.Arrays;

import org.djunits.Throw;

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
    private final FieldDescriptor[] addressFieldDescriptors;

    /** The result field descriptors. */
    private final FieldDescriptor[] resultFieldDescriptors;

    /** The id of the AbstractTransceiver. */
    private final String id;

    /**
     * Construct a new AbstractTransceiver.
     * @param id String; the id of the new AbstractTransceiver
     * @param addressFieldDescriptors FieldDescriptor[]; description of the elements of an address that the <code>get</code>
     *            method of this AbstractTransceiver can handle
     * @param resultFieldDescriptors FieldDescriptor[]; description of the result of the <code>get</code> method
     */
    public AbstractTransceiver(final String id, final FieldDescriptor[] addressFieldDescriptors,
            final FieldDescriptor[] resultFieldDescriptors)
    {
        Throw.whenNull(id, "id may not be null");
        Throw.whenNull(addressFieldDescriptors, "addressFieldDescriptors may not be null");
        Throw.whenNull(resultFieldDescriptors, "resultFieldDescriptors may not be null");
        this.id = id;
        this.addressFieldDescriptors = addressFieldDescriptors;
        this.resultFieldDescriptors = resultFieldDescriptors;
    }

    /** {@inheritDoc} */
    @Override
    public final String getId()
    {
        return this.id;
    }

    /** {@inheritDoc} */
    @Override
    public final int addressFieldLevels()
    {
        return this.addressFieldDescriptors.length;
    }

    /** {@inheritDoc} */
    @Override
    public final Class<?> getAddressFieldClass(final int addressLevel)
    {
        return this.addressFieldDescriptors[addressLevel].getFieldClass();
    }

    /** {@inheritDoc} */
    @Override
    public final String getAddressFieldDescription(final int addressLevel)
    {
        return this.addressFieldDescriptors[addressLevel].getFieldDescription();
    }

    /** {@inheritDoc} */
    @Override
    public final int getResultFieldCount()
    {
        return this.resultFieldDescriptors.length;
    }

    /** {@inheritDoc} */
    @Override
    public final Class<?> getResultFieldClass(final int index)
    {
        return this.resultFieldDescriptors[index].getFieldClass();
    }

    /** {@inheritDoc} */
    @Override
    public final String getResultFieldDescription(final int index)
    {
        return this.resultFieldDescriptors[index].getFieldDescription();
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "AbstractTransceiver [id=" + id + ", addressFieldDescriptors=" + Arrays.toString(addressFieldDescriptors)
                + ", resultFieldDescriptors=" + Arrays.toString(resultFieldDescriptors) + "]";
    }

}
