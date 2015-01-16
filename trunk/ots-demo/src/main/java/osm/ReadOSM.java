package osm;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.opentrafficsim.importexport.osm.Network;
import org.opentrafficsim.importexport.osm.Tag;
import org.opentrafficsim.importexport.osm.input.ReadOSMFile;

/**
 *  * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>.
 * @version 31 dec. 2014 <br> 
 * @author <a>Moritz Bergmann</a>
 */
public class ReadOSM
{
    /** */
    private static String loc = "duesseldorf_germany.osm.bz2";
    //private static String loc = "Mettmann.osm";
    //private static String loc = "TUD.osm";// "amsterdam_netherlands.osm.bz2";// "munich.osm.bz2";

    /** */
    private static List<Tag> wt;
    
    /** */
    private static List<String> ft;
    
    /**
     * 
     */
    private static void makeWanted()
    {
        Tag t1 = new Tag("highway", "primary");
        wt.add(t1);
        Tag t2 = new Tag("highway", "secondary");
        wt.add(t2);
        Tag t3 = new Tag("highway", "tertiary");
        wt.add(t3);
        Tag t4 = new Tag("highway", "cycleway");
        wt.add(t4);
        Tag t5 = new Tag("highway", "trunk");
        wt.add(t5);
        Tag t6 = new Tag("highway", "path");
        wt.add(t6);
        Tag t8 = new Tag("cyclway", "lane");
        wt.add(t8);
        Tag t9 = new Tag("highway", "residental");
        wt.add(t9);
        Tag t10 = new Tag("highway", "service");
        wt.add(t10);
        Tag t11 = new Tag("highway", "motorway");
        wt.add(t11);
        Tag t12 = new Tag("highway", "bus_stop");
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
        ft.add("ele"); //Altitude/elevation
        ft.add("location"); //underground, overground, underwater etc.
        ft.add("layer"); // -5 to 5. 0 not explicitly used.
        ft.add("bridge");
        ft.add("tunnel");
    }

    /**
     * @param args 
     */
    public static void main(final String[] args)
    {
        ft = new ArrayList<String>();
        wt = new ArrayList<Tag>();
        makeWanted();
        makeFilter();
        try
        {
            System.out.println("Parsing " + loc);
            String prefix = "file:///D:/";
            ReadOSMFile osmf = new ReadOSMFile(prefix + loc, wt, ft);
            while (!osmf.checkisReaderThreadDead())
            {
                System.out.println("Processing");
            }
            Network net = osmf.getNetwork();
            System.out.println("Got Network. Start Conversion");
            System.out.println("Making links");
            net.makeLinks();
            System.out.println("Made " + net.getLinks().size() + " links");
            System.out.println("Removing redundancies");
            net.removeRedundancy();
            System.out.println(net.getLinks().size() + " links after redudancy removal");
            System.out.println("Not yet converting to OTS internal format - terminating");
            /*int countn = 0;
            for (Long n: net.getNodes().keySet())
            {
                countn++;
                System.out.println(n);
            }
            int countw = 0;
            for (Long w: net.getWays().keySet())
            {
                countw++;
                String s = "";
                for (Tag t: net.getWay(w).getTags())
                {   
                    s = s + t.getKey() + ": " +t.getValue() + " || ";
                }
                if(!s.isEmpty())
                {
                    System.out.println(s);
                }
            }
            try
            {
                net.makeLinks();
                int countl = 0;
                for (Link l: net.getLinks())
                {
                    countl++;
                    String s = "";
                    for (Tag t: l.getTags())
                    {   
                        s = s + t.getKey() + ": " +t.getValue() + " || ";
                    }
                    if(!s.isEmpty())
                    {
                        System.out.println(s);
                    }
                }
                System.out.println("Number of ways: " + countw + " || Number of nodes " + countn + " || Number of links: " + countl);
            }
            catch(IOException e)
            {
                System.out.println(e);
            }
        */}
        catch (URISyntaxException exception)
        {
            exception.printStackTrace();
        }
        catch (IOException exception)
        {
            exception.printStackTrace();
        }
    }

}
