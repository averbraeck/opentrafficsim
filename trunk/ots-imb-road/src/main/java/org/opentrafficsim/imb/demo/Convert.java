package org.opentrafficsim.imb.demo;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.tudelft.simulation.language.io.URLResource;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Oct 18, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class Convert
{
    private static List<String> nodesEB = new ArrayList<>();

    private static List<String> nodesWB = new ArrayList<>();

    private static Map<Coordinate, String> coordinateMap = new HashMap<>();

    private static List<String> linksEB = new ArrayList<>();

    private static List<String> linksWB = new ArrayList<>();

    private static List<List<Object>> linesEB = new ArrayList<>();

    private static List<List<Object>> linesWB = new ArrayList<>();

    private static List<Object> parseLine(final String line)
    {
        String l = line;
        l = l.replaceAll("\n", "");
        l = l.replaceAll("\r", "");
        List<Object> out = new ArrayList<>();
        while (l.length() > 0)
        {
            if (l.charAt(0) == '[')
            {
                int p = l.indexOf("]", 1);
                out.add(parseLine(l.substring(1, p)));
                l = l.substring(p + 2).trim();
            }
            else if (l.charAt(0) == '"')
            {
                int p = l.indexOf("\"", 1);
                out.add(l.substring(1, p));
                l = l.substring(p + 2).trim();
            }
            else
            {
                int p = l.indexOf(",");
                if (p == -1)
                {
                    out.add(l.trim());
                    l = "";
                }
                else
                {
                    out.add(l.substring(0, p));
                    l = l.substring(p + 1).trim();
                }
            }
        }
        return out;
    }

    private static void parseAllLines(final String[] lines, final String dir, final List<List<Object>> linesList)
    {
        boolean first = true;
        for (String line : lines)
        {
            if (first)
            {
                first = false;
                continue;
            }
            List<Object> list = parseLine(line);
            linesList.add(list);
        }
    }

    private static void writeNodes(final List<List<Object>> linesList, final String dir, final List<String> nodes)
    {
        for (List<Object> line : linesList)
        {
            writeNodes(line, dir, nodes, 0, 1);
            if (line.get(5).toString().equalsIgnoreCase("true"))
            {
                writeNodes(line, dir, nodes, 11, 12);
            }
            if (line.get(6).toString().equalsIgnoreCase("true"))
            {
                writeNodes(line, dir, nodes, 16, 17);
            }
        }
        System.out.println();
    }

    private static void writeNodes(final List<Object> line, final String dir, final List<String> nodes, final int xi,
            final int yi)
    {
        List<String> xc = (List<String>) line.get(xi);
        List<String> yc = (List<String>) line.get(yi);

        Coordinate cf = new Coordinate(Double.parseDouble(xc.get(0)), Double.parseDouble(yc.get(0)));
        String sf = "N" + (nodes.size() + 1) + dir;
        if (!coordinateMap.keySet().contains(cf))
        {
            System.out.println("<NODE NAME=" + "\"" + sf + "\" COORDINATE=\"" + cf + "\" />");
            nodes.add(sf);
            coordinateMap.put(cf, sf);
        }

        Coordinate cl = new Coordinate(Double.parseDouble(xc.get(xc.size() - 1)), Double.parseDouble(yc.get(yc.size() - 1)));
        String sl = "N" + (nodes.size() + 1) + dir;
        if (!coordinateMap.keySet().contains(cl))
        {
            System.out.println("<NODE NAME=" + "\"" + sl + "\" COORDINATE=\"" + cl + "\" />");
            nodes.add(sl);
            coordinateMap.put(cl, sl);
        }
    }

    private static void writeLinks(final List<List<Object>> linesList, final String dir, final List<String> links)
    {
        for (List<Object> line : linesList)
        {
            writeLinks(line, dir, links, 0, 1, 2, 0.0, 0.0);
            if (line.get(5).toString().equalsIgnoreCase("true"))
            {
                writeLinks(line, dir, links, 11, 12, 14, 0.0, 0.0); // merge
            }
            if (line.get(6).toString().equalsIgnoreCase("true"))
            {
                writeLinks(line, dir, links, 16, 17, 19, 0.0, 0.0); // diverge
            }
        }
    }

    private static void writeLinks(final List<Object> line, final String dir, final List<String> links, final int xi,
            final int yi, final int li, final double startOffset, final double endOffset)
    {
        List<String> xc = (List<String>) line.get(xi);
        List<String> yc = (List<String>) line.get(yi);

        Coordinate cf = new Coordinate(Double.parseDouble(xc.get(0)), Double.parseDouble(yc.get(0)));
        Coordinate cl = new Coordinate(Double.parseDouble(xc.get(xc.size() - 1)), Double.parseDouble(yc.get(yc.size() - 1)));

        String nodef = coordinateMap.get(cf);
        String nodel = coordinateMap.get(cl);

        String linkName = "L" + (links.size() + 1) + dir;
        int nrLanes = Integer.parseInt((String) line.get(li));

        System.out.print("<LINK NAME=\"" + linkName + "\" NODESTART=\"" + nodef + "\" NODEEND=\"" + nodel + "\" ROADLAYOUT=\"");
        System.out.print((nrLanes == 1) ? "HW1\"" : (nrLanes == 2) ? "HW2\"" : (nrLanes == 3) ? "HW3\"" : "HW4\"");
        if (startOffset != 0.0)
        {
            System.out.print(" OFFSETSTART=\"" + startOffset + " m\"");
        }
        if (endOffset != 0.0)
        {
            System.out.print(" OFFSETEND=\"" + endOffset + " m\"");
        }
        System.out.println(">");
        if (xc.size() > 2)
        {
            System.out.print("<POLYLINE INTERMEDIATEPOINTS=\"");
            boolean first = true;
            for (int i = 1; i < xc.size() - 1; i++)
            {
                System.out.print((first ? "" : " ") + "(" + xc.get(i) + "," + yc.get(i) + ")");
                first = false;
            }
            System.out.println("\" />");
        }
        else
        {
            System.out.println("<STRAIGHT />");
        }
        System.out.println("</LINK>\n");
        links.add(linkName);
    }

    private static void writeRoutes(final String dir, final List<String> nodes, final boolean reverse)
    {
        System.out.print("<ROUTE NAME=\"" + dir + "\" NODELIST=\"");
        if (!reverse)
        {
            System.out.print(nodes.get(0));
            for (int i = 1; i < nodes.size(); i++)
            {
                System.out.print(" " + nodes.get(i));
            }
        }
        else
        {
            System.out.print(nodes.get(nodes.size() - 1));
            for (int i = nodes.size() - 2; i >= 0; --i)
            {
                System.out.print(" " + nodes.get(i));
            }
        }
        System.out.println("\" />");
    }

    /**
     * @param args args
     * @throws IOException on i/o error
     * @throws URISyntaxException
     */
    public static void main(final String[] args) throws IOException, URISyntaxException
    {
        String a58EB = new String(Files.readAllBytes(Paths.get(URLResource.getResource("/A58_EB.txt").toURI())));
        String[] eb = a58EB.split("\n");
        String a58WB = new String(Files.readAllBytes(Paths.get(URLResource.getResource("/A58_WB.txt").toURI())));
        String[] wb = a58WB.split("\n");

        parseAllLines(eb, "EB", linesEB);
        parseAllLines(wb, "WB", linesWB);

        writeNodes(linesEB, "EB", nodesEB);
        writeNodes(linesWB, "WB", nodesWB);

        // writeRoutes("RouteEB", nodesEB, false);
        // writeRoutes("RouteWB", nodesWB, true);
        // System.out.println();

        writeLinks(linesEB, "EB", linksEB);
        writeLinks(linesWB, "WB", linksWB);
    }

    /** */
    private static class Coordinate
    {
        final double x, y;

        /**
         * @param x
         * @param y
         */
        public Coordinate(double x, double y)
        {
            super();
            this.x = x;
            this.y = y;
        }

        /** {@inheritDoc} */
        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            long temp;
            temp = Double.doubleToLongBits(this.x);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(this.y);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            return result;
        }

        /** {@inheritDoc} */
        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Coordinate other = (Coordinate) obj;
            if (Double.doubleToLongBits(this.x) != Double.doubleToLongBits(other.x))
                return false;
            if (Double.doubleToLongBits(this.y) != Double.doubleToLongBits(other.y))
                return false;
            return true;
        }

        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return "(" + this.x + "," + this.y + ")";
        }

    }
}
