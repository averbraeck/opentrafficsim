package org.opentrafficsim.road.network.factory;

import java.io.OutputStream;
import java.io.Writer;

import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.road.network.lane.object.sensor.AbstractSensor;

import com.thoughtworks.xstream.XStream;

import nl.tudelft.simulation.event.EventProducer;
import nl.tudelft.simulation.naming.JVMContext;

/**
 * <p>
 * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Aug 28, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public final class OTSNetworkUtils
{

    /** Do not instantiate. */
    private OTSNetworkUtils()
    {
        // do not instantiate.
    }

    /**
     * Make a copy of the network, without GTUs and listeners.
     * @param network the network to copy
     * @return a copy of the network
     */
    public static OTSNetwork copy(final OTSNetwork network)
    {
        XStream xstream = new XStream();
        return (OTSNetwork) xstream.fromXML(toXml(network));
    }

    /**
     * Create an xml-version of the network.
     * @param network the network to create an xml-version from
     * @return an xml-string with the network
     */
    public static String toXml(final OTSNetwork network)
    {
        XStream xstream = new XStream();
        xstream.omitField(OTSNetwork.class, "gtuMap"); // no GTUs
        xstream.omitField(EventProducer.class, "listeners"); // no listeners
        xstream.omitField(JVMContext.class, "atomicName"); // no JVMContext
        xstream.omitField(JVMContext.class, "elements"); // no JVMContext
        xstream.omitField(AbstractSensor.class, "simulator"); // no reference to a simulator
        return xstream.toXML(network);
    }

    /**
     * Create an xml-version of the network.
     * @param network the network to create an xml-version from
     * @param out the stream to write the xml-string with the network to
     */
    public static void toXml(final OTSNetwork network, final OutputStream out)
    {
        XStream xstream = new XStream();
        xstream.toXML(network, out);
    }

    /**
     * Create an xml-version of the network.
     * @param network the network to create an xml-version from
     * @param writer the writer to write the xml-string with the network to
     */
    public static void toXml(final OTSNetwork network, final Writer writer)
    {
        XStream xstream = new XStream();
        xstream.toXML(network, writer);
    }
}
