package org.opentrafficsim.imb.kpi.demo;

import java.util.HashMap;
import java.util.Map;

import org.djunits.unit.FrequencyUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.imb.IMBException;
import org.opentrafficsim.imb.connector.IMBConnector;
import org.opentrafficsim.imb.kpi.IMBSampler;
import org.opentrafficsim.imb.kpi.ImbKpiTransceiver;
import org.opentrafficsim.imb.kpi.LaneData;
import org.opentrafficsim.imb.kpi.LinkData;
import org.opentrafficsim.imb.kpi.NodeData;
import org.opentrafficsim.kpi.sampling.KpiGtuDirectionality;
import org.opentrafficsim.kpi.sampling.KpiLaneDirection;
import org.opentrafficsim.kpi.sampling.Query;
import org.opentrafficsim.kpi.sampling.meta.MetaDataSet;

import nl.tudelft.simulation.language.d3.CartesianPoint;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 4 nov. 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */

public class VissimQueryKPI
{

    /**
     * @param args
     */
    public static void main(String[] args)
    {

        // connection
        String host = "vps17642.public.cloudvps.com";
        int port = 4000;
        String modelName = "VISSIM";
        int modelId = 1;
        String federation = "OTS_RT";
        if (args.length > 0 && args.length < 3)
        {
            throw new RuntimeException("Need three arguments in KPI module.");
        }
        if (args.length == 3)
        {
            host = args[0];
            port = Integer.valueOf(args[1]);
            federation = args[2];
        }

        // nodes
        Map<String, NodeData> nodes = new HashMap<>();
        for (int i = 0; i < net.length; i++)
        {
            for (int j = 0; j < 2; j++)
            {
                if (!nodes.containsKey(net[i][j]))
                {
                    nodes.put(net[i][j], new NodeData(net[i][j], new CartesianPoint(0.0, 0.0, 0.0)));
                }
            }
        }

        // links
        Map<String, LinkData> links = new HashMap<>();
        for (int i = 0; i < net.length; i++)
        {
            links.put(net[i][2], new LinkData(net[i][2], nodes.get(net[i][0]), nodes.get(net[i][1]),
                    new Length(Double.valueOf(net[i][3]), LengthUnit.SI)));
        }

        // lanes
        Map<String, LaneData> lanes = new HashMap<>();
        for (int i = 0; i < net.length; i++)
        {
            int n = Integer.valueOf(net[i][4]);
            for (int j = 1; j <= n; j++)
            {
                String id = net[i][2] + "." + j;
                lanes.put(id, new LaneData(links.get(net[i][2]), id, links.get(net[i][2]).getLength()));
            }
        }

        // sampler
        IMBSampler sampler;
        try
        {
            sampler = new IMBSampler(host, port, modelName, modelId, federation);
        }
        catch (IMBException exception)
        {
            throw new RuntimeException("Could not connect the Vissim query KPI to the IMB.", exception);
        }

        // query
        String queryDescription = "Default query for VISSIM model.";
        MetaDataSet metaDataSet = new MetaDataSet();
        Query query = new Query(sampler, queryDescription, metaDataSet, new Frequency(2.0, FrequencyUnit.PER_MINUTE));
        for (String laneId : lanes.keySet())
        {
            query.addSpaceTimeRegion(new KpiLaneDirection(lanes.get(laneId), KpiGtuDirectionality.DIR_PLUS), Length.ZERO,
                    lanes.get(laneId).getLength(), Time.ZERO, new Time(1, TimeUnit.HOUR));
        }

        // transceiver of KPI data
        try
        {
            IMBConnector connector = new IMBConnector(host, port, modelName, modelId, federation);
            sampler.setImbKpiTransceiver(
                    new ImbKpiTransceiver(connector, Time.ZERO, modelName, query, new Duration(30.0, TimeUnit.SI)));
        }
        catch (IMBException exception)
        {
            exception.printStackTrace();
        }

    }

    /**
     * Hard-coded vissim network: from node, to node, link id, length [m], number of lanes. Lane id's are defined as linkId.#.
     * Lane lengths are equal to the link lengths.
     */
    private final static String[][] net = new String[][] { new String[] { "98", "105", "10062", "3.86", "1" },
            new String[] { "98", "105", "10063", "3.86", "1" }, new String[] { "106", "101", "10060", "3.81", "1" },
            new String[] { "106", "103", "10061", "3.83", "1" }, new String[] { "60", "107", "10066", "21.26", "1" },
            new String[] { "110", "107", "10067", "8.22", "1" }, new String[] { "108", "99", "10064", "25.11", "1" },
            new String[] { "108", "99", "10065", "25.11", "1" }, new String[] { "327", "328", "165", "51.34", "2" },
            new String[] { "126", "121", "10080", "17.31", "1" }, new String[] { "110", "27", "10068", "137.92", "1" },
            new String[] { "110", "329", "10069", "158.40", "1" }, new String[] { "28", "29", "10012", "9.51", "2" },
            new String[] { "126", "123", "10081", "19.62", "1" }, new String[] { "265", "266", "133", "157.20", "1" },
            new String[] { "263", "264", "132", "142.13", "1" }, new String[] { "261", "262", "131", "107.87", "1" },
            new String[] { "259", "260", "130", "35.15", "1" }, new String[] { "273", "274", "137", "57.80", "1" },
            new String[] { "271", "272", "136", "57.80", "1" }, new String[] { "269", "270", "135", "124.43", "1" },
            new String[] { "267", "268", "134", "127.02", "1" }, new String[] { "164", "119", "10105", "110.90", "1" },
            new String[] { "144", "163", "10104", "10.44", "1" }, new String[] { "277", "278", "139", "66.33", "1" },
            new String[] { "275", "276", "138", "66.33", "1" }, new String[] { "156", "161", "10101", "11.54", "1" },
            new String[] { "329", "330", "166", "261.86", "3" }, new String[] { "146", "113", "10103", "115.52", "1" },
            new String[] { "162", "113", "10102", "103.99", "1" }, new String[] { "47", "48", "24", "816.44", "2" },
            new String[] { "49", "50", "25", "182.51", "2" }, new String[] { "51", "52", "26", "157.13", "1" },
            new String[] { "53", "54", "27", "93.81", "2" }, new String[] { "39", "40", "20", "60.87", "2" },
            new String[] { "41", "42", "21", "1456.31", "2" }, new String[] { "43", "44", "22", "110.12", "1" },
            new String[] { "45", "46", "23", "29.15", "3" }, new String[] { "317", "318", "160", "70.95", "1" },
            new String[] { "324", "327", "10146", "7.56", "2" }, new String[] { "55", "56", "28", "262.55", "4" },
            new String[] { "57", "58", "29", "73.82", "3" }, new String[] { "319", "320", "161", "68.62", "1" },
            new String[] { "90", "303", "10136", "11.93", "1" }, new String[] { "326", "335", "10149", "7.62", "1" },
            new String[] { "7", "8", "4", "56.82", "1" }, new String[] { "15", "16", "8", "301.40", "2" },
            new String[] { "323", "324", "163", "51.11", "2" }, new String[] { "138", "125", "10093", "12.14", "2" },
            new String[] { "237", "238", "119", "25.50", "1" }, new String[] { "26", "109", "10071", "312.21", "1" },
            new String[] { "16", "19", "10059", "14.49", "2" }, new String[] { "318", "307", "10058", "10.95", "1" },
            new String[] { "94", "95", "10057", "4.93", "1" }, new String[] { "288", "295", "10056", "6.64", "1" },
            new String[] { "6", "87", "10010", "18.03", "2" }, new String[] { "52", "41", "10054", "337.90", "1" },
            new String[] { "330", "297", "10053", "7.84", "2" }, new String[] { "334", "59", "10052", "11.27", "1" },
            new String[] { "120", "41", "10051", "8.76", "2" }, new String[] { "86", "79", "10050", "6.52", "1" },
            new String[] { "239", "240", "120", "25.50", "1" }, new String[] { "241", "242", "121", "36.63", "1" },
            new String[] { "243", "244", "122", "36.63", "1" }, new String[] { "245", "246", "123", "28.66", "1" },
            new String[] { "247", "248", "124", "28.66", "1" }, new String[] { "249", "250", "125", "57.15", "1" },
            new String[] { "251", "252", "126", "57.95", "1" }, new String[] { "253", "254", "127", "19.70", "1" },
            new String[] { "255", "256", "128", "19.67", "1" }, new String[] { "257", "258", "129", "108.25", "1" },
            new String[] { "180", "183", "10114", "2.97", "1" }, new String[] { "174", "177", "10115", "17.04", "1" },
            new String[] { "331", "332", "167", "133.04", "4" }, new String[] { "180", "181", "10113", "7.68", "1" },
            new String[] { "332", "53", "10026", "39.64", "2" }, new String[] { "150", "145", "10096", "13.61", "1" },
            new String[] { "235", "236", "118", "41.32", "1" }, new String[] { "117", "118", "59", "80.44", "1" },
            new String[] { "115", "116", "58", "78.53", "1" }, new String[] { "109", "110", "55", "313.42", "1" },
            new String[] { "107", "108", "54", "79.51", "2" }, new String[] { "113", "114", "57", "918.90", "2" },
            new String[] { "111", "112", "56", "238.80", "1" }, new String[] { "101", "102", "51", "67.18", "1" },
            new String[] { "99", "100", "50", "164.45", "1" }, new String[] { "105", "106", "53", "40.13", "2" },
            new String[] { "103", "104", "52", "373.22", "1" }, new String[] { "176", "167", "10118", "16.62", "1" },
            new String[] { "114", "133", "10089", "24.44", "1" }, new String[] { "332", "49", "10027", "43.11", "2" },
            new String[] { "118", "147", "10108", "11.21", "1" }, new String[] { "325", "326", "164", "50.85", "1" },
            new String[] { "74", "87", "10049", "122.83", "2" }, new String[] { "104", "27", "10017", "399.02", "1" },
            new String[] { "44", "31", "10016", "97.56", "1" }, new String[] { "22", "103", "10070", "114.12", "1" },
            new String[] { "4", "9", "10040", "106.63", "2" }, new String[] { "78", "65", "10041", "138.66", "2" },
            new String[] { "76", "79", "10042", "12.66", "2" }, new String[] { "52", "41", "10029", "318.47", "1" },
            new String[] { "300", "75", "10044", "3.90", "2" }, new String[] { "82", "73", "10045", "5.28", "2" },
            new String[] { "72", "301", "10046", "19.79", "1" }, new String[] { "84", "77", "10047", "13.84", "1" },
            new String[] { "229", "230", "115", "88.60", "1" }, new String[] { "227", "228", "114", "83.61", "1" },
            new String[] { "175", "176", "88", "8.81", "1" }, new String[] { "177", "178", "89", "416.80", "1" },
            new String[] { "221", "222", "111", "43.34", "1" }, new String[] { "219", "220", "110", "43.34", "1" },
            new String[] { "225", "226", "113", "65.06", "1" }, new String[] { "223", "224", "112", "43.38", "1" },
            new String[] { "163", "164", "82", "12.86", "1" }, new String[] { "165", "166", "83", "333.72", "1" },
            new String[] { "159", "160", "80", "367.71", "1" }, new String[] { "161", "162", "81", "13.66", "1" },
            new String[] { "171", "172", "86", "19.56", "1" }, new String[] { "173", "174", "87", "7.62", "1" },
            new String[] { "167", "168", "84", "349.30", "1" }, new String[] { "169", "170", "85", "16.69", "1" },
            new String[] { "146", "159", "10106", "46.62", "1" }, new String[] { "30", "31", "10013", "14.29", "2" },
            new String[] { "36", "115", "10077", "27.92", "1" }, new String[] { "102", "329", "10073", "137.05", "1" },
            new String[] { "148", "151", "10094", "3.90", "1" }, new String[] { "36", "113", "10076", "23.99", "2" },
            new String[] { "296", "187", "10128", "27.19", "1" }, new String[] { "154", "119", "10100", "127.53", "1" },
            new String[] { "5", "6", "3", "56.13", "2" }, new String[] { "13", "14", "7", "87.97", "1" },
            new String[] { "136", "139", "10091", "25.36", "1" }, new String[] { "46", "35", "10021", "9.32", "2" },
            new String[] { "72", "299", "10075", "18.69", "2" }, new String[] { "304", "315", "10134", "4.06", "1" },
            new String[] { "90", "303", "10135", "11.93", "1" }, new String[] { "30", "33", "10014", "23.03", "1" },
            new String[] { "336", "319", "10137", "6.70", "1" }, new String[] { "186", "289", "10130", "19.42", "1" },
            new String[] { "92", "293", "10131", "0.00", "1" }, new String[] { "72", "81", "10132", "19.51", "2" },
            new String[] { "304", "317", "10133", "3.80", "1" }, new String[] { "158", "153", "10097", "9.29", "1" },
            new String[] { "310", "313", "10035", "12.56", "1" }, new String[] { "68", "321", "10139", "3.21", "3" },
            new String[] { "215", "216", "108", "87.20", "1" }, new String[] { "217", "218", "109", "43.38", "1" },
            new String[] { "70", "9", "10039", "25.48", "3" }, new String[] { "70", "13", "10038", "27.50", "1" },
            new String[] { "203", "204", "102", "46.91", "1" }, new String[] { "205", "206", "103", "62.52", "1" },
            new String[] { "199", "200", "100", "45.54", "1" }, new String[] { "201", "202", "101", "47.88", "1" },
            new String[] { "211", "212", "106", "33.56", "1" }, new String[] { "213", "214", "107", "37.56", "1" },
            new String[] { "207", "208", "104", "62.45", "1" }, new String[] { "209", "210", "105", "84.15", "1" },
            new String[] { "77", "78", "39", "71.45", "2" }, new String[] { "75", "76", "38", "192.09", "2" },
            new String[] { "12", "67", "10138", "5.59", "3" }, new String[] { "65", "66", "33", "861.78", "2" },
            new String[] { "63", "64", "32", "242.25", "3" }, new String[] { "61", "62", "31", "91.81", "1" },
            new String[] { "59", "60", "30", "86.58", "1" }, new String[] { "73", "74", "37", "172.76", "2" },
            new String[] { "71", "72", "36", "509.55", "3" }, new String[] { "69", "70", "35", "391.52", "3" },
            new String[] { "67", "68", "34", "36.36", "3" }, new String[] { "136", "137", "10090", "19.25", "2" },
            new String[] { "136", "141", "10092", "22.85", "1" }, new String[] { "84", "77", "10048", "13.84", "1" },
            new String[] { "124", "147", "10109", "36.48", "1" }, new String[] { "322", "63", "10141", "7.88", "3" },
            new String[] { "10", "11", "10004", "3.20", "3" }, new String[] { "298", "325", "10143", "12.77", "1" },
            new String[] { "322", "89", "10142", "9.09", "1" }, new String[] { "328", "65", "10145", "4.40", "2" },
            new String[] { "150", "143", "10095", "13.16", "1" }, new String[] { "298", "323", "10147", "11.73", "2" },
            new String[] { "14", "87", "10005", "127.34", "1" }, new String[] { "88", "15", "10037", "14.28", "2" },
            new String[] { "96", "297", "10148", "629.79", "1" }, new String[] { "176", "129", "10119", "108.57", "1" },
            new String[] { "20", "27", "10007", "11.17", "2" }, new String[] { "16", "17", "10006", "122.77", "1" },
            new String[] { "119", "120", "60", "731.84", "2" }, new String[] { "121", "122", "61", "91.86", "1" },
            new String[] { "123", "124", "62", "90.92", "1" }, new String[] { "125", "126", "63", "211.45", "2" },
            new String[] { "127", "128", "64", "61.35", "2" }, new String[] { "129", "130", "65", "2515.52", "2" },
            new String[] { "131", "132", "66", "91.76", "1" }, new String[] { "133", "134", "67", "92.50", "1" },
            new String[] { "135", "136", "68", "2288.24", "2" }, new String[] { "137", "138", "69", "715.82", "2" },
            new String[] { "38", "31", "10024", "158.98", "1" }, new String[] { "54", "35", "10025", "288.52", "2" },
            new String[] { "46", "37", "10022", "9.25", "1" }, new String[] { "48", "45", "10023", "10.49", "2" },
            new String[] { "40", "35", "10020", "221.51", "2" }, new String[] { "2", "7", "10001", "32.43", "1" },
            new String[] { "316", "309", "10036", "5.59", "1" }, new String[] { "8", "3", "10002", "11.40", "1" },
            new String[] { "174", "137", "10116", "111.32", "1" }, new String[] { "42", "111", "10031", "1378.62", "1" },
            new String[] { "2", "5", "10000", "30.96", "2" }, new String[] { "8", "3", "10003", "11.40", "1" },
            new String[] { "166", "169", "10126", "6.28", "1" }, new String[] { "114", "129", "10087", "24.08", "2" },
            new String[] { "50", "57", "10030", "20.55", "2" }, new String[] { "188", "291", "10127", "0.00", "1" },
            new String[] { "3", "4", "2", "118.92", "2" }, new String[] { "321", "322", "162", "126.30", "4" },
            new String[] { "64", "305", "10033", "14.31", "3" }, new String[] { "11", "12", "6", "63.83", "3" },
            new String[] { "112", "57", "10032", "17.01", "1" }, new String[] { "56", "331", "10152", "0.00", "4" },
            new String[] { "122", "159", "10107", "12.76", "1" }, new String[] { "334", "329", "10144", "8.06", "3" },
            new String[] { "302", "83", "10043", "4.93", "1" }, new String[] { "320", "321", "10140", "5.88", "1" },
            new String[] { "184", "175", "10117", "12.59", "1" }, new String[] { "197", "198", "99", "48.25", "1" },
            new String[] { "195", "196", "98", "83.79", "2" }, new String[] { "32", "41", "10019", "676.88", "1" },
            new String[] { "34", "39", "10018", "10.45", "1" }, new String[] { "333", "334", "168", "47.53", "4" },
            new String[] { "335", "336", "169", "129.99", "1" }, new String[] { "332", "51", "10028", "44.39", "1" },
            new String[] { "181", "182", "91", "49.61", "1" }, new String[] { "179", "180", "90", "368.08", "1" },
            new String[] { "185", "186", "93", "291.44", "1" }, new String[] { "183", "184", "92", "58.83", "1" },
            new String[] { "189", "190", "95", "24.67", "1" }, new String[] { "187", "188", "94", "131.86", "1" },
            new String[] { "193", "194", "97", "75.82", "2" }, new String[] { "191", "192", "96", "22.27", "1" },
            new String[] { "21", "22", "11", "74.14", "1" }, new String[] { "19", "20", "10", "570.77", "2" },
            new String[] { "25", "26", "13", "81.82", "1" }, new String[] { "23", "24", "12", "86.14", "2" },
            new String[] { "29", "30", "15", "139.73", "2" }, new String[] { "27", "28", "14", "136.32", "2" },
            new String[] { "33", "34", "17", "70.53", "1" }, new String[] { "31", "32", "16", "704.21", "2" },
            new String[] { "37", "38", "19", "210.12", "1" }, new String[] { "35", "36", "18", "1118.59", "2" },
            new String[] { "154", "147", "10099", "41.64", "1" }, new String[] { "158", "155", "10098", "10.75", "1" },
            new String[] { "36", "49", "10082", "862.24", "1" }, new String[] { "233", "234", "117", "41.25", "1" },
            new String[] { "310", "311", "10034", "11.32", "1" }, new String[] { "334", "61", "10074", "9.34", "1" },
            new String[] { "231", "232", "116", "112.02", "1" }, new String[] { "299", "300", "151", "190.45", "2" },
            new String[] { "297", "298", "150", "681.40", "2" }, new String[] { "303", "304", "153", "91.51", "2" },
            new String[] { "301", "302", "152", "190.24", "1" }, new String[] { "307", "308", "155", "74.37", "1" },
            new String[] { "305", "306", "154", "328.61", "3" }, new String[] { "311", "312", "157", "34.04", "1" },
            new String[] { "309", "310", "156", "14.78", "1" }, new String[] { "315", "316", "159", "73.19", "1" },
            new String[] { "313", "314", "158", "76.24", "1" }, new String[] { "20", "21", "10008", "12.34", "1" },
            new String[] { "20", "25", "10009", "11.88", "1" }, new String[] { "134", "177", "10123", "31.59", "1" },
            new String[] { "58", "333", "10151", "16.33", "3" }, new String[] { "132", "167", "10122", "8.78", "1" },
            new String[] { "140", "177", "10121", "15.19", "1" }, new String[] { "170", "173", "10112", "13.87", "1" },
            new String[] { "182", "137", "10120", "122.05", "1" }, new String[] { "114", "131", "10088", "24.40", "1" },
            new String[] { "212", "209", "10083", "73.31", "1" }, new String[] { "95", "96", "48", "61.27", "1" },
            new String[] { "97", "98", "49", "168.13", "1" }, new String[] { "91", "92", "46", "295.15", "1" },
            new String[] { "93", "94", "47", "20.05", "1" }, new String[] { "87", "88", "44", "155.40", "2" },
            new String[] { "89", "90", "45", "49.21", "1" }, new String[] { "83", "84", "42", "51.23", "1" },
            new String[] { "85", "86", "43", "84.59", "1" }, new String[] { "79", "80", "40", "467.01", "3" },
            new String[] { "81", "82", "41", "192.06", "2" }, new String[] { "1", "2", "1", "552.22", "2" },
            new String[] { "42", "43", "10015", "1193.24", "1" }, new String[] { "9", "10", "5", "158.50", "3" },
            new String[] { "166", "171", "10125", "4.20", "1" }, new String[] { "17", "18", "9", "93.85", "1" },
            new String[] { "142", "167", "10124", "33.09", "1" }, new String[] { "288", "93", "10055", "6.13", "1" },
            new String[] { "289", "290", "146", "514.16", "1" }, new String[] { "291", "292", "147", "159.01", "1" },
            new String[] { "287", "288", "144", "58.01", "1" }, new String[] { "62", "103", "10072", "110.36", "1" },
            new String[] { "283", "284", "142", "76.40", "1" }, new String[] { "285", "286", "143", "76.40", "1" },
            new String[] { "279", "280", "140", "56.92", "1" }, new String[] { "281", "282", "141", "56.92", "1" },
            new String[] { "126", "119", "10079", "16.62", "2" }, new String[] { "36", "117", "10078", "24.92", "1" },
            new String[] { "293", "294", "148", "116.58", "1" }, new String[] { "295", "296", "149", "24.30", "1" },
            new String[] { "153", "154", "77", "74.09", "1" }, new String[] { "151", "152", "76", "312.62", "1" },
            new String[] { "149", "150", "75", "326.92", "1" }, new String[] { "147", "148", "74", "92.15", "1" },
            new String[] { "145", "146", "73", "66.33", "1" }, new String[] { "143", "144", "72", "69.34", "1" },
            new String[] { "141", "142", "71", "89.25", "1" }, new String[] { "139", "140", "70", "80.61", "1" },
            new String[] { "116", "159", "10110", "39.78", "1" }, new String[] { "157", "158", "79", "290.16", "1" },
            new String[] { "155", "156", "78", "73.25", "1" }, new String[] { "186", "95", "10129", "29.01", "1" },
            new String[] { "6", "85", "10011", "18.25", "1" }, new String[] { "18", "289", "10084", "5.27", "1" },
            new String[] { "18", "187", "10085", "20.06", "1" }, new String[] { "172", "129", "10111", "126.62", "1" },
            new String[] { "294", "287", "10086", "4.88", "1" } };

}
