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
    private final static String[][] net = new String[][] { new String[] { "461", "462", "10062", "7.71", "1" },
            new String[] { "463", "464", "10063", "8.02", "1" }, new String[] { "463", "458", "10060", "7.18", "1" },
            new String[] { "457", "460", "10061", "6.81", "1" }, new String[] { "459", "470", "10066", "23.42", "1" },
            new String[] { "469", "472", "10067", "12.55", "1" }, new String[] { "471", "466", "10064", "30.32", "1" },
            new String[] { "465", "468", "10065", "30.82", "1" }, new String[] { "467", "328", "165", "51.34", "2" },
            new String[] { "327", "498", "10080", "24.54", "1" }, new String[] { "497", "474", "10068", "18.77", "1" },
            new String[] { "473", "476", "10069", "29.91", "1" }, new String[] { "475", "362", "10012", "14.69", "2" },
            new String[] { "361", "500", "10081", "25.86", "1" }, new String[] { "499", "266", "133", "157.20", "1" },
            new String[] { "265", "264", "132", "142.13", "1" }, new String[] { "263", "262", "131", "107.87", "1" },
            new String[] { "261", "260", "130", "35.15", "1" }, new String[] { "259", "274", "137", "57.80", "1" },
            new String[] { "273", "272", "136", "57.80", "1" }, new String[] { "271", "270", "135", "124.43", "1" },
            new String[] { "269", "268", "134", "127.02", "1" }, new String[] { "267", "548", "10105", "13.89", "1" },
            new String[] { "547", "546", "10104", "10.74", "1" }, new String[] { "545", "278", "139", "66.33", "1" },
            new String[] { "277", "276", "138", "66.33", "1" }, new String[] { "275", "540", "10101", "15.03", "1" },
            new String[] { "539", "330", "166", "261.86", "3" }, new String[] { "329", "544", "10103", "15.33", "1" },
            new String[] { "543", "542", "10102", "12.60", "1" }, new String[] { "541", "48", "24", "816.44", "2" },
            new String[] { "47", "50", "25", "182.51", "2" }, new String[] { "49", "52", "26", "157.13", "1" },
            new String[] { "51", "54", "27", "93.81", "2" }, new String[] { "53", "40", "20", "60.87", "2" },
            new String[] { "39", "42", "21", "1456.31", "2" }, new String[] { "41", "44", "22", "110.12", "1" },
            new String[] { "43", "46", "23", "29.15", "3" }, new String[] { "45", "318", "160", "70.95", "1" },
            new String[] { "317", "630", "10146", "8.04", "2" }, new String[] { "629", "56", "28", "262.64", "4" },
            new String[] { "55", "58", "29", "73.82", "3" }, new String[] { "57", "320", "161", "68.62", "1" },
            new String[] { "319", "610", "10136", "18.63", "1" }, new String[] { "609", "636", "10149", "9.74", "1" },
            new String[] { "635", "8", "4", "56.82", "1" }, new String[] { "7", "16", "8", "301.40", "2" },
            new String[] { "15", "324", "163", "51.11", "2" }, new String[] { "323", "524", "10093", "16.63", "2" },
            new String[] { "523", "238", "119", "25.50", "1" }, new String[] { "237", "480", "10071", "28.13", "1" },
            new String[] { "479", "456", "10059", "20.53", "2" }, new String[] { "455", "454", "10058", "12.22", "1" },
            new String[] { "453", "452", "10057", "6.98", "1" }, new String[] { "451", "450", "10056", "8.94", "1" },
            new String[] { "449", "358", "10010", "22.21", "2" }, new String[] { "357", "446", "10054", "21.60", "1" },
            new String[] { "445", "444", "10053", "12.86", "2" }, new String[] { "443", "442", "10052", "15.68", "1" },
            new String[] { "441", "440", "10051", "16.35", "2" }, new String[] { "439", "438", "10050", "13.35", "1" },
            new String[] { "437", "240", "120", "25.50", "1" }, new String[] { "239", "242", "121", "36.63", "1" },
            new String[] { "241", "244", "122", "36.63", "1" }, new String[] { "243", "246", "123", "28.66", "1" },
            new String[] { "245", "248", "124", "28.66", "1" }, new String[] { "247", "250", "125", "57.15", "1" },
            new String[] { "249", "252", "126", "57.95", "1" }, new String[] { "251", "254", "127", "19.70", "1" },
            new String[] { "253", "256", "128", "19.67", "1" }, new String[] { "255", "258", "129", "108.25", "1" },
            new String[] { "257", "566", "10114", "6.76", "1" }, new String[] { "565", "568", "10115", "21.47", "1" },
            new String[] { "567", "332", "167", "133.04", "4" }, new String[] { "331", "564", "10113", "8.31", "1" },
            new String[] { "563", "390", "10026", "47.68", "2" }, new String[] { "389", "530", "10096", "15.95", "1" },
            new String[] { "529", "236", "118", "41.32", "1" }, new String[] { "235", "118", "59", "80.44", "1" },
            new String[] { "117", "116", "58", "78.53", "1" }, new String[] { "115", "110", "55", "313.42", "1" },
            new String[] { "109", "108", "54", "79.51", "2" }, new String[] { "107", "114", "57", "918.90", "2" },
            new String[] { "113", "112", "56", "238.80", "1" }, new String[] { "111", "102", "51", "67.18", "1" },
            new String[] { "101", "100", "50", "164.45", "1" }, new String[] { "99", "106", "53", "40.13", "2" },
            new String[] { "105", "104", "52", "373.22", "1" }, new String[] { "103", "574", "10118", "22.82", "1" },
            new String[] { "573", "516", "10089", "30.90", "1" }, new String[] { "515", "392", "10027", "49.33", "2" },
            new String[] { "391", "554", "10108", "14.15", "1" }, new String[] { "553", "326", "164", "50.85", "1" },
            new String[] { "325", "436", "10049", "24.02", "2" }, new String[] { "435", "372", "10017", "26.26", "1" },
            new String[] { "371", "370", "10016", "23.79", "1" }, new String[] { "369", "478", "10070", "16.50", "1" },
            new String[] { "477", "418", "10040", "16.76", "2" }, new String[] { "417", "420", "10041", "15.49", "2" },
            new String[] { "419", "422", "10042", "21.25", "2" }, new String[] { "421", "396", "10029", "18.85", "1" },
            new String[] { "395", "426", "10044", "10.58", "2" }, new String[] { "425", "428", "10045", "14.10", "2" },
            new String[] { "427", "430", "10046", "25.53", "1" }, new String[] { "429", "432", "10047", "19.60", "1" },
            new String[] { "431", "230", "115", "88.60", "1" }, new String[] { "229", "228", "114", "83.61", "1" },
            new String[] { "227", "176", "88", "8.81", "1" }, new String[] { "175", "178", "89", "416.80", "1" },
            new String[] { "177", "222", "111", "43.34", "1" }, new String[] { "221", "220", "110", "43.34", "1" },
            new String[] { "219", "226", "113", "65.06", "1" }, new String[] { "225", "224", "112", "43.38", "1" },
            new String[] { "223", "164", "82", "12.86", "1" }, new String[] { "163", "166", "83", "333.72", "1" },
            new String[] { "165", "160", "80", "367.71", "1" }, new String[] { "159", "162", "81", "13.66", "1" },
            new String[] { "161", "172", "86", "19.56", "1" }, new String[] { "171", "174", "87", "7.62", "1" },
            new String[] { "173", "168", "84", "349.30", "1" }, new String[] { "167", "170", "85", "16.69", "1" },
            new String[] { "169", "550", "10106", "51.27", "1" }, new String[] { "549", "364", "10013", "19.80", "2" },
            new String[] { "363", "492", "10077", "35.52", "1" }, new String[] { "491", "484", "10073", "21.83", "1" },
            new String[] { "483", "526", "10094", "7.62", "1" }, new String[] { "525", "490", "10076", "29.56", "2" },
            new String[] { "489", "594", "10128", "29.66", "1" }, new String[] { "593", "538", "10100", "17.94", "1" },
            new String[] { "537", "6", "3", "56.13", "2" }, new String[] { "5", "14", "7", "87.97", "1" },
            new String[] { "13", "520", "10091", "34.54", "1" }, new String[] { "519", "380", "10021", "18.05", "2" },
            new String[] { "379", "488", "10075", "26.51", "2" }, new String[] { "487", "606", "10134", "7.90", "1" },
            new String[] { "605", "608", "10135", "17.30", "1" }, new String[] { "607", "366", "10014", "26.61", "1" },
            new String[] { "365", "612", "10137", "8.28", "1" }, new String[] { "611", "598", "10130", "22.38", "1" },
            new String[] { "597", "600", "10131", "0.15", "1" }, new String[] { "599", "602", "10132", "25.55", "2" },
            new String[] { "601", "604", "10133", "8.63", "1" }, new String[] { "603", "532", "10097", "14.77", "1" },
            new String[] { "531", "408", "10035", "14.63", "1" }, new String[] { "407", "616", "10139", "8.74", "3" },
            new String[] { "615", "216", "108", "87.20", "1" }, new String[] { "215", "218", "109", "43.38", "1" },
            new String[] { "217", "416", "10039", "30.57", "3" }, new String[] { "415", "414", "10038", "31.75", "1" },
            new String[] { "413", "204", "102", "46.91", "1" }, new String[] { "203", "206", "103", "62.52", "1" },
            new String[] { "205", "200", "100", "45.54", "1" }, new String[] { "199", "202", "101", "47.88", "1" },
            new String[] { "201", "212", "106", "33.56", "1" }, new String[] { "211", "214", "107", "37.56", "1" },
            new String[] { "213", "208", "104", "62.45", "1" }, new String[] { "207", "210", "105", "84.15", "1" },
            new String[] { "209", "78", "39", "71.45", "2" }, new String[] { "77", "76", "38", "192.09", "2" },
            new String[] { "75", "614", "10138", "8.34", "3" }, new String[] { "613", "66", "33", "861.78", "2" },
            new String[] { "65", "64", "32", "242.25", "3" }, new String[] { "63", "62", "31", "91.81", "1" },
            new String[] { "61", "60", "30", "86.58", "1" }, new String[] { "59", "74", "37", "172.76", "2" },
            new String[] { "73", "72", "36", "509.55", "3" }, new String[] { "71", "70", "35", "391.52", "3" },
            new String[] { "69", "68", "34", "36.36", "3" }, new String[] { "67", "518", "10090", "25.72", "2" },
            new String[] { "517", "522", "10092", "29.67", "1" }, new String[] { "521", "434", "10048", "22.20", "1" },
            new String[] { "433", "556", "10109", "38.98", "1" }, new String[] { "555", "620", "10141", "11.78", "3" },
            new String[] { "619", "346", "10004", "5.53", "3" }, new String[] { "345", "624", "10143", "15.51", "1" },
            new String[] { "623", "622", "10142", "15.56", "1" }, new String[] { "621", "628", "10145", "6.59", "2" },
            new String[] { "627", "528", "10095", "14.12", "1" }, new String[] { "527", "632", "10147", "12.65", "2" },
            new String[] { "631", "348", "10005", "13.16", "1" }, new String[] { "347", "412", "10037", "15.52", "2" },
            new String[] { "411", "634", "10148", "16.67", "1" }, new String[] { "633", "576", "10119", "19.51", "1" },
            new String[] { "575", "352", "10007", "14.40", "2" }, new String[] { "351", "350", "10006", "29.45", "1" },
            new String[] { "349", "120", "60", "731.84", "2" }, new String[] { "119", "122", "61", "91.86", "1" },
            new String[] { "121", "124", "62", "90.92", "1" }, new String[] { "123", "126", "63", "211.45", "2" },
            new String[] { "125", "128", "64", "61.35", "2" }, new String[] { "127", "130", "65", "2516.84", "2" },
            new String[] { "129", "132", "66", "91.76", "1" }, new String[] { "131", "134", "67", "92.50", "1" },
            new String[] { "133", "136", "68", "2287.27", "2" }, new String[] { "135", "138", "69", "715.82", "2" },
            new String[] { "137", "386", "10024", "21.53", "1" }, new String[] { "385", "388", "10025", "23.69", "2" },
            new String[] { "387", "382", "10022", "17.01", "1" }, new String[] { "381", "384", "10023", "14.72", "2" },
            new String[] { "383", "378", "10020", "22.54", "2" }, new String[] { "377", "340", "10001", "39.92", "1" },
            new String[] { "339", "410", "10036", "8.24", "1" }, new String[] { "409", "342", "10002", "17.25", "1" },
            new String[] { "341", "570", "10116", "16.97", "1" }, new String[] { "569", "400", "10031", "28.34", "1" },
            new String[] { "399", "338", "10000", "39.26", "2" }, new String[] { "337", "344", "10003", "17.37", "1" },
            new String[] { "343", "590", "10126", "6.38", "1" }, new String[] { "589", "512", "10087", "30.29", "2" },
            new String[] { "511", "398", "10030", "28.34", "2" }, new String[] { "397", "592", "10127", "0.15", "1" },
            new String[] { "591", "4", "2", "118.92", "2" }, new String[] { "3", "322", "162", "126.30", "4" },
            new String[] { "321", "404", "10033", "20.02", "3" }, new String[] { "403", "12", "6", "63.83", "3" },
            new String[] { "11", "402", "10032", "23.38", "1" }, new String[] { "401", "640", "10152", "0.15", "4" },
            new String[] { "639", "552", "10107", "16.69", "1" }, new String[] { "551", "626", "10144", "10.98", "3" },
            new String[] { "625", "424", "10043", "10.53", "1" }, new String[] { "423", "618", "10140", "11.43", "1" },
            new String[] { "617", "572", "10117", "17.52", "1" }, new String[] { "571", "198", "99", "48.25", "1" },
            new String[] { "197", "196", "98", "83.79", "2" }, new String[] { "195", "376", "10019", "20.91", "1" },
            new String[] { "375", "374", "10018", "19.03", "1" }, new String[] { "373", "334", "168", "47.53", "4" },
            new String[] { "333", "336", "169", "129.99", "1" }, new String[] { "335", "394", "10028", "52.26", "1" },
            new String[] { "393", "182", "91", "49.61", "1" }, new String[] { "181", "180", "90", "368.08", "1" },
            new String[] { "179", "186", "93", "291.44", "1" }, new String[] { "185", "184", "92", "58.83", "1" },
            new String[] { "183", "190", "95", "24.67", "1" }, new String[] { "189", "188", "94", "131.86", "1" },
            new String[] { "187", "194", "97", "75.82", "2" }, new String[] { "193", "192", "96", "22.27", "1" },
            new String[] { "191", "22", "11", "74.14", "1" }, new String[] { "21", "20", "10", "570.77", "2" },
            new String[] { "19", "26", "13", "81.82", "1" }, new String[] { "25", "24", "12", "86.14", "2" },
            new String[] { "23", "30", "15", "139.73", "2" }, new String[] { "29", "28", "14", "136.32", "2" },
            new String[] { "27", "34", "17", "70.53", "1" }, new String[] { "33", "32", "16", "699.43", "2" },
            new String[] { "31", "38", "19", "210.12", "1" }, new String[] { "37", "36", "18", "1118.59", "2" },
            new String[] { "35", "536", "10099", "46.21", "1" }, new String[] { "535", "534", "10098", "16.22", "1" },
            new String[] { "533", "502", "10082", "17.40", "1" }, new String[] { "501", "234", "117", "41.25", "1" },
            new String[] { "233", "406", "10034", "13.94", "1" }, new String[] { "405", "486", "10074", "15.06", "1" },
            new String[] { "485", "232", "116", "112.02", "1" }, new String[] { "231", "300", "151", "190.45", "2" },
            new String[] { "299", "298", "150", "681.40", "2" }, new String[] { "297", "304", "153", "91.51", "2" },
            new String[] { "303", "302", "152", "190.24", "1" }, new String[] { "301", "308", "155", "74.37", "1" },
            new String[] { "307", "306", "154", "328.61", "3" }, new String[] { "305", "312", "157", "34.04", "1" },
            new String[] { "311", "310", "156", "14.78", "1" }, new String[] { "309", "316", "159", "73.19", "1" },
            new String[] { "315", "314", "158", "76.24", "1" }, new String[] { "313", "354", "10008", "15.37", "1" },
            new String[] { "353", "356", "10009", "16.70", "1" }, new String[] { "355", "584", "10123", "36.94", "1" },
            new String[] { "583", "638", "10151", "19.01", "3" }, new String[] { "637", "582", "10122", "15.01", "1" },
            new String[] { "581", "580", "10121", "18.47", "1" }, new String[] { "579", "562", "10112", "16.83", "1" },
            new String[] { "561", "578", "10120", "16.36", "1" }, new String[] { "577", "514", "10088", "29.16", "1" },
            new String[] { "513", "504", "10083", "3.58", "1" }, new String[] { "503", "96", "48", "61.27", "1" },
            new String[] { "95", "98", "49", "168.13", "1" }, new String[] { "97", "92", "46", "295.15", "1" },
            new String[] { "91", "94", "47", "20.05", "1" }, new String[] { "93", "88", "44", "155.40", "2" },
            new String[] { "87", "90", "45", "49.21", "1" }, new String[] { "89", "84", "42", "51.23", "1" },
            new String[] { "83", "86", "43", "84.59", "1" }, new String[] { "85", "80", "40", "467.01", "3" },
            new String[] { "79", "82", "41", "192.06", "2" }, new String[] { "81", "2", "1", "552.22", "2" },
            new String[] { "1", "368", "10015", "21.32", "1" }, new String[] { "367", "10", "5", "158.50", "3" },
            new String[] { "9", "588", "10125", "6.13", "1" }, new String[] { "587", "18", "9", "93.85", "1" },
            new String[] { "17", "586", "10124", "38.02", "1" }, new String[] { "585", "448", "10055", "8.84", "1" },
            new String[] { "447", "290", "146", "514.16", "1" }, new String[] { "289", "292", "147", "159.01", "1" },
            new String[] { "291", "288", "144", "58.01", "1" }, new String[] { "287", "482", "10072", "34.35", "1" },
            new String[] { "481", "284", "142", "76.40", "1" }, new String[] { "283", "286", "143", "76.40", "1" },
            new String[] { "285", "280", "140", "56.92", "1" }, new String[] { "279", "282", "141", "56.92", "1" },
            new String[] { "281", "496", "10079", "24.97", "2" }, new String[] { "495", "494", "10078", "30.34", "1" },
            new String[] { "493", "294", "148", "116.58", "1" }, new String[] { "293", "296", "149", "24.30", "1" },
            new String[] { "295", "154", "77", "74.09", "1" }, new String[] { "153", "152", "76", "312.62", "1" },
            new String[] { "151", "150", "75", "326.92", "1" }, new String[] { "149", "148", "74", "92.15", "1" },
            new String[] { "147", "146", "73", "66.33", "1" }, new String[] { "145", "144", "72", "69.34", "1" },
            new String[] { "143", "142", "71", "89.25", "1" }, new String[] { "141", "140", "70", "80.61", "1" },
            new String[] { "139", "558", "10110", "42.72", "1" }, new String[] { "557", "158", "79", "290.16", "1" },
            new String[] { "157", "156", "78", "73.25", "1" }, new String[] { "155", "596", "10129", "42.32", "1" },
            new String[] { "595", "360", "10011", "22.19", "1" }, new String[] { "359", "506", "10084", "7.32", "1" },
            new String[] { "505", "508", "10085", "21.16", "1" }, new String[] { "507", "560", "10111", "16.00", "1" },
            new String[] { "559", "510", "10086", "4.95", "1" }, };

}
