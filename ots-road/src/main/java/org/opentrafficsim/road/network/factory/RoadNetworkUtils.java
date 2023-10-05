package org.opentrafficsim.road.network.factory;

import java.io.OutputStream;
import java.io.Writer;

import org.djutils.event.EventProducer;
import org.opentrafficsim.road.network.RoadNetwork;
import org.opentrafficsim.road.network.lane.object.detector.Detector;

import com.thoughtworks.xstream.XStream;

import nl.tudelft.simulation.naming.context.JvmContext;

/**
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public final class RoadNetworkUtils
{

    /** Do not instantiate. */
    private RoadNetworkUtils()
    {
        // do not instantiate.
    }

    /**
     * Make a copy of the network, without GTUs and listeners.
     * @param network RoadNetwork; the network to copy
     * @return a copy of the network
     */
    public static RoadNetwork copy(final RoadNetwork network)
    {
        XStream xstream = new XStream();
        return (RoadNetwork) xstream.fromXML(toXml(network));
    }

    /**
     * Create an xml-version of the network.
     * @param network RoadNetwork; the network to create an xml-version from
     * @return an xml-string with the network
     */
    public static String toXml(final RoadNetwork network)
    {
        XStream xstream = new XStream();
        xstream.omitField(RoadNetwork.class, "gtuMap"); // no GTUs
        xstream.omitField(EventProducer.class, "listeners"); // no listeners
        xstream.omitField(JvmContext.class, "atomicName"); // no JvmContext
        xstream.omitField(JvmContext.class, "elements"); // no JvmContext
        xstream.omitField(Detector.class, "simulator"); // no reference to a simulator
        return xstream.toXML(network);
    }

    /**
     * Create an xml-version of the network.
     * @param network RoadNetwork; the network to create an xml-version from
     * @param out OutputStream; the stream to write the xml-string with the network to
     */
    public static void toXml(final RoadNetwork network, final OutputStream out)
    {
        XStream xstream = new XStream();
        xstream.toXML(network, out);
    }

    /**
     * Create an xml-version of the network.
     * @param network RoadNetwork; the network to create an xml-version from
     * @param writer Writer; the writer to write the xml-string with the network to
     */
    public static void toXml(final RoadNetwork network, final Writer writer)
    {
        XStream xstream = new XStream();
        xstream.toXML(network, writer);
    }
}
