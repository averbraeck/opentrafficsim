package org.opentrafficsim.sim0mq.publisher;

import java.util.ArrayList;
import java.util.List;

import org.djunits.value.vdouble.scalar.Time;
import org.djutils.event.TimedEventType;
import org.djutils.metadata.MetaData;
import org.djutils.metadata.ObjectDescriptor;

/**
 * Transceiver for DJUNITS events.
 * <p>
 * Copyright (c) 2020-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public abstract class AbstractEventTransceiver extends AbstractTransceiver
{
    /**
     * Construct a new AbstractEventTransceiver.
     * @param id String; name of the new AbstractEventTransceiver
     * @param addressFields MetaData; address format accepted by the new AbstractEventTransceiver
     * @param eventType TimedEventType; type of the event that the AbstractEventTransceiver can subscribe to in the network
     */
    public AbstractEventTransceiver(final String id, final MetaData addressFields, final TimedEventType eventType)
    {
        super(id, addressFields, constructResultFields(eventType));
    }

    /**
     * Construct a Sim0MQ MetaData object that corresponds to the MetaData of DJUTILS EventType. Classes that do not have a
     * corresponding Sim0MQ type will result in a ClassCastException.
     * @param eventType TimedEventType; the event type
     * @return MetaData; a MetaData object that corresponds to the MetaData of the event type
     * @throws ClassCastException when the <code>eventType</code> contains a class that cannot be carried over Sim0MQ
     */
    public static MetaData constructResultFields(final TimedEventType eventType) throws ClassCastException
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
