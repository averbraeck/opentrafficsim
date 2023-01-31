package org.opentrafficsim.road.network.factory;

import java.io.OutputStream;
import java.io.Writer;

import org.djutils.event.EventProducer;
import org.opentrafficsim.road.network.OtsRoadNetwork;
import org.opentrafficsim.road.network.lane.object.sensor.Detector;

import com.thoughtworks.xstream.XStream;

import nl.tudelft.simulation.naming.context.JVMContext;

/**
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public final class OtsRoadNetworkUtils
{

    /** Do not instantiate. */
    private OtsRoadNetworkUtils()
    {
        // do not instantiate.
    }

    /**
     * Make a copy of the network, without GTUs and listeners.
     * @param network OTSRoadNetwork; the network to copy
     * @return a copy of the network
     */
    public static OtsRoadNetwork copy(final OtsRoadNetwork network)
    {
        XStream xstream = new XStream();
        return (OtsRoadNetwork) xstream.fromXML(toXml(network));
    }

    /**
     * Create an xml-version of the network.
     * @param network OTSRoadNetwork; the network to create an xml-version from
     * @return an xml-string with the network
     */
    public static String toXml(final OtsRoadNetwork network)
    {
        XStream xstream = new XStream();
        xstream.omitField(OtsRoadNetwork.class, "gtuMap"); // no GTUs
        xstream.omitField(EventProducer.class, "listeners"); // no listeners
        xstream.omitField(JVMContext.class, "atomicName"); // no JVMContext
        xstream.omitField(JVMContext.class, "elements"); // no JVMContext
        xstream.omitField(Detector.class, "simulator"); // no reference to a simulator
        return xstream.toXML(network);
    }

    /**
     * Create an xml-version of the network.
     * @param network OTSRoadNetwork; the network to create an xml-version from
     * @param out OutputStream; the stream to write the xml-string with the network to
     */
    public static void toXml(final OtsRoadNetwork network, final OutputStream out)
    {
        XStream xstream = new XStream();
        xstream.toXML(network, out);
    }

    /**
     * Create an xml-version of the network.
     * @param network OTSRoadNetwork; the network to create an xml-version from
     * @param writer Writer; the writer to write the xml-string with the network to
     */
    public static void toXml(final OtsRoadNetwork network, final Writer writer)
    {
        XStream xstream = new XStream();
        xstream.toXML(network, writer);
    }
}
