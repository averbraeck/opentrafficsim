package org.opentrafficsim.sim0mq.publisher;

import java.util.ArrayList;
import java.util.List;

import org.djunits.value.vdouble.scalar.Time;
import org.djutils.event.EventType;
import org.djutils.metadata.MetaData;
import org.djutils.metadata.ObjectDescriptor;

/**
 * Transceiver for DJUNITS events.
 * <p>
 * Copyright (c) 2020-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public abstract class AbstractEventTransceiver extends AbstractTransceiver
{
    /**
     * Construct a new AbstractEventTransceiver.
     * @param id name of the new AbstractEventTransceiver
     * @param addressFields address format accepted by the new AbstractEventTransceiver
     * @param eventType type of the event that the AbstractEventTransceiver can subscribe to in the network
     */
    public AbstractEventTransceiver(final String id, final MetaData addressFields, final EventType eventType)
    {
        super(id, addressFields, constructResultFields(eventType));
    }

    /**
     * Construct a Sim0MQ MetaData object that corresponds to the MetaData of DJUTILS EventType. Classes that do not have a
     * corresponding Sim0MQ type will result in a ClassCastException.
     * @param eventType the event type
     * @return a MetaData object that corresponds to the MetaData of the event type
     * @throws ClassCastException when the <code>eventType</code> contains a class that cannot be carried over Sim0MQ
     */
    public static MetaData constructResultFields(final EventType eventType) throws ClassCastException
    {
        List<ObjectDescriptor> resultList = new ArrayList<>();
        resultList.add(new ObjectDescriptor("TimeStamp", "Time", Time.class));
        for (int index = 0; index < eventType.getMetaData().size(); index++)
        {
            ObjectDescriptor od = eventType.getMetaData().getObjectDescriptor(index);
            switch (od.getObjectClass().getName())
            {
                case "java.lang.String":
                case "java.lang.Double":
                case "org.djunits.value.vdouble.scalar.Acceleration":
                case "org.djunits.value.vdouble.scalar.Direction":
                case "org.djunits.value.vdouble.scalar.Length":
                case "org.djunits.value.vdouble.scalar.Speed":
                case "org.djunits.value.vdouble.scalar.Time":
                case "org.djunits.value.vdouble.vector.PositionVector":
                    resultList.add(od);
                    break;

                default:
                    throw new ClassCastException("No conversion for class " + od.getObjectClass().getName());
            }
        }

        return new MetaData(eventType.getMetaData().getName(), eventType.getMetaData().getDescription(),
                resultList.toArray(new ObjectDescriptor[0]));
    }

}
