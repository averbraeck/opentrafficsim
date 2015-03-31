package osm;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import nl.tudelft.simulation.dsol.SimRuntimeException;

import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.lane.CrossSectionLink;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.importexport.osm.OSMNetwork;
import org.opentrafficsim.importexport.osm.OSMTag;
import org.opentrafficsim.importexport.osm.input.ReadOSMFile;
import org.opentrafficsim.importexport.osm.output.Convert;
import org.opentrafficsim.simulationengine.SimpleSimulator;

/**
 * Import an OpenStreetMap file.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * .
 * @version 31 dec. 2014 <br>
 * @author <a>Moritz Bergmann</a>
 */
public class ReadOSM
{
    /** */
    private static String loc = "file:///home/moe/Documents/TUD/A3.osm.bz2";

    // private static String loc = "duesseldorf_germany.osm.bz2";
    // private static String loc = "Mettmann.osm";
    // private static String loc = "TUD.osm";// "amsterdam_netherlands.osm.bz2";// "munich.osm.bz2";

    /** */
    private static List<OSMTag> wt;

    /** */
    private static List<String> ft;

    /**
     * 
     */
    private static void makeWanted()
    {
        OSMTag t1 = new OSMTag("highway", "primary");
        wt.add(t1);
        OSMTag t2 = new OSMTag("highway", "secondary");
        wt.add(t2);
        OSMTag t3 = new OSMTag("highway", "tertiary");
        wt.add(t3);
        OSMTag t4 = new OSMTag("highway", "cycleway");
        wt.add(t4);
        OSMTag t5 = new OSMTag("highway", "trunk");
        wt.add(t5);
        OSMTag t6 = new OSMTag("highway", "path");
        wt.add(t6);
        OSMTag t8 = new OSMTag("cyclway", "lane");
        wt.add(t8);
        OSMTag t9 = new OSMTag("highway", "residental");
        wt.add(t9);
        OSMTag t10 = new OSMTag("highway", "service");
        wt.add(t10);
        OSMTag t11 = new OSMTag("highway", "motorway");
        wt.add(t11);
        OSMTag t12 = new OSMTag("highway", "bus_stop");
        wt.add(t12);
    }

    /**
     * 
     */
    private static void makeFilter()
    {
        ft.add("highway");
        ft.add("cycleway");
        ft.add("lanes");
        ft.add("name");
        ft.add("maxspeed");
        ft.add("junction");
        ft.add("oneway");
        ft.add("sidewalk");
        ft.add("busway");
        ft.add("junction");
        ft.add("service");
        ft.add("ele"); // Altitude/elevation
        ft.add("location"); // underground, overground, underwater etc.
        ft.add("layer"); // -5 to 5. 0 not explicitly used.
        ft.add("bridge");
        ft.add("tunnel");
    }

    /**
     * @param args
     */
//    public static void main(final String[] args)
//    {
//        ft = new ArrayList<String>();
//        wt = new ArrayList<Tag>();
//        makeWanted();
//        makeFilter();
//        try
//        {
//            System.out.println("Parsing " + loc);
//            String prefix = "file:///D:/";
//            ReadOSMFile osmf = new ReadOSMFile(/* prefix + */loc, wt, ft);
//            while (!osmf.checkisReaderThreadDead())
//            {
//                System.out.println("Processing");
//            }
//            Network net = osmf.getNetwork();
//            System.out.println("Got Network. Preparing Conversion");
//            System.out.println("Making links");
//            net.makeLinks();
//            System.out.println("Made " + net.getLinks().size() + " links");
//            System.out.println("Removing redundancies");
//            net.removeRedundancy();
//            System.out.println(net.getLinks().size() + " links after redudancy removal");
//            System.out.println("Now converting to OTS internal format");
//            org.opentrafficsim.core.network.Network<String, CrossSectionLink<?, ?>> otsNet =
//                    new org.opentrafficsim.core.network.Network<String, CrossSectionLink<?, ?>>(net.getName());
//            System.out.println("OTS Network named: " + otsNet.getId());
//            for (org.opentrafficsim.importexport.osm.Node osmNode : net.getNodes().values())
//            {
//                try
//                {
//                    otsNet.addNode(Convert.convertNode(osmNode));
//                }
//                catch (NetworkException ne)
//                {
//                    System.out.println(ne.getMessage());
//                }
//            }
//            System.out.println(otsNet.getNodeSet().size() + " OTS Nodes created");
//            for (org.opentrafficsim.importexport.osm.Link osmLink : net.getLinks())
//            {
//                otsNet.add(Convert.convertLink(osmLink));
//            }
//            System.out.println(otsNet.size() + " OTS Links created");
//            try
//            {
//                SimpleSimulator result =
//                        new SimpleSimulator(new DoubleScalar.Abs<TimeUnit>(0.0, TimeUnit.SECOND),
//                                new DoubleScalar.Rel<TimeUnit>(0.0, TimeUnit.SECOND), new DoubleScalar.Rel<TimeUnit>(
//                                        1800.0, TimeUnit.SECOND), null);
//            }
//            catch (SimRuntimeException exception)
//            {
//                exception.printStackTrace();
//            }
//            System.out.println("Done. Terminating.");
//            /*-
//            int countn = 0;
//            for (Long n: net.getNodes().keySet())
//            {
//                countn++;
//                System.out.println(n);
//            }
//            int countw = 0;
//            for (Long w: net.getWays().keySet())
//            {
//                countw++;
//                String s = "";
//                for (Tag t: net.getWay(w).getTags())
//                {   
//                    s = s + t.getKey() + ": " +t.getValue() + " || ";
//                }
//                if(!s.isEmpty())
//                {
//                    System.out.println(s);
//                }
//            }
//            try
//            {
//                net.makeLinks();
//                int countl = 0;
//                for (Link l: net.getLinks())
//                {
//                    countl++;
//                    String s = "";
//                    for (Tag t: l.getTags())
//                    {   
//                        s = s + t.getKey() + ": " +t.getValue() + " || ";
//                    }
//                    if(!s.isEmpty())
//                    {
//                        System.out.println(s);
//                    }
//                }
//                System.out.println("Number of ways: " + countw + " || Number of nodes " + countn + " || Number of links: " + countl);
//            }
//            catch(IOException e)
//            {
//                System.out.println(e);
//            }
//             */}
//        catch (URISyntaxException exception)
//        {
//            exception.printStackTrace();
//        }
//        catch (IOException exception)
//        {
//            exception.printStackTrace();
//        }
//    }

}
