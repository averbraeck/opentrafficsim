package org.sim0mq.publisher;

import java.rmi.RemoteException;

import org.djunits.Throw;
import org.opentrafficsim.base.Identifiable;

/**
 * Machine interpretable description of a transceiver.
 * <p>
 * Copyright (c) 2020-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2020-02-13 11:08:16 +0100 (Thu, 13 Feb 2020) $, @version $Revision: 6383 $, by $Author: pknoppers $,
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public interface TransceiverInterface extends Identifiable
{
    /**
     * Number of arguments needed in a subscription request.
     * @return int; the number of arguments needed in a subscription request
     */
    int addressFieldLevels();

    /**
     * Retrieve the java class of one argument needed in a subscription request.
     * @param addressLevel int; index of the argument
     * @return Class&lt;?&gt;; java class of the argument
     */
    Class<?> getAddressFieldClass(int addressLevel);

    /**
     * Retrieve the description of one argument needed in a subscription request.
     * @param addressLevel int; index of the argument
     * @return String; description of the argument
     */
    String getAddressFieldDescription(int addressLevel);

    /**
     * Retrieve the TransceiverMetadata object that can be used to get valid values for one argument in a subscription request.
     * @param addressLevel int; index of the argument
     * @return TransceiverMetaData; to be used to get valid values for argument <code>addressLevel</code>, or null if valid
     *         values for the argument at index <code>addressLevel</code> can not be obtained through a TransceiverMetaData
     *         object
     */
    default TransceiverInterface getIdSource(int addressLevel)
    {
        Throw.when(addressLevel < 0 || addressLevel >= addressFieldLevels(), IndexOutOfBoundsException.class,
                "Invalid addressLevel");
        return null;
    }

    /**
     * Report the number of objects in a result of the transceiver.
     * @return int; the number of objects in a result from the transceiver
     */
    int getResultFieldCount();

    /**
     * Return the java class name of one element in the result of the transceiver.
     * @param index int; the index of the object to report upon
     * @return Class&lt;?&gt;; java class of the object in the result
     */
    Class<?> getResultFieldClass(int index);

    /**
     * Return a description of one element in the result of the transceiver.
     * @param index int; the index of the object for which the description must be returned
     * @return String; description of the element
     */
    String getResultFieldDescription(int index);

    /**
     * Retrieve the data.
     * @param address Object[]; the address of the data to retrieve
     * @return Object[]; the retrieved data
     * @throws RemoteException when communication needed to retrieve the data failed
     */
    Object[] get(Object[] address) throws RemoteException;

    /**
     * Verify that an address has the correct composition.
     * @param address Object[]; the address to verify
     */
    default void verifyAddressComponents(final Object[] address)
    {
        if (addressFieldLevels() == 0 && address == null)
        {
            return;
        }
        Throw.whenNull(address, "address may not be null");
        Throw.when(addressFieldLevels() != address.length, IndexOutOfBoundsException.class,
                "Wrong number of elements in address (expected %d, got %d)", addressFieldLevels(), address.length);
        Throw.when(address.length != addressFieldLevels(), IndexOutOfBoundsException.class,
                "address should have same number of elements as reference");
        for (int index = 0; index < address.length; index++)
        {
            if (!(getAddressFieldClass(index).isAssignableFrom(address[index].getClass())))
            {
                throw new ClassCastException(String.format("address[%d] (%s) is not a valid %s", index, address[index],
                        getAddressFieldClass(index).getName()));
            }
        }
    }

    /**
     * Wrapper for field description and class name.
     */
    class FieldDescriptor
    {
        /** Description. */
        private final String description;

        /** Class. */
        private final Class<?> theClass;

        /**
         * Construct a new FieldDescription object.
         * @param description String; description
         * @param theClass Class&lt;?&gt;; class
         */
        FieldDescriptor(final String description, final Class<?> theClass)
        {
            this.description = description;
            this.theClass = theClass;
        }

        /**
         * Retrieve the description of the object.
         * @return String; description of the object
         */
        String getFieldDescription()
        {
            return this.description;
        }

        /**
         * Retrieve the Class of the object.
         * @return String; class name of the object
         */
        Class<?> getFieldClass()
        {
            return this.theClass;
        }

        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return "FieldDescriptor [description=" + description + ", theClass=" + theClass + "]";
        }

    }

}
