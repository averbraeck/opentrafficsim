package org.opentrafficsim.road.network.factory.nwb;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opengis.feature.Feature;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.road.network.factory.nwb.ShapeFileReader.FeatureQualifier;

/**
 * Access to the NWB (Nationaal WegenBestand - Dutch National Road database) shape files.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 4 may 2019 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class Main
{

    /**
     * Program entry point.
     * @param args String[]; the command line arguments
     * @throws IOException on I/O error
     * @throws MalformedURLException on wrong URL for shape file
     * @throws OTSGeometryException when lines are invalid
     */
    public static void main(final String[] args) throws MalformedURLException, IOException, OTSGeometryException
    {
        String baseDir = "c:\\NWB";
        int date = 20190401;
        long start = System.nanoTime();
        ShapeFileReader shr =
                new ShapeFileReader(baseDir + File.separator + "wegen", date, "Wegvakken" + File.separator + "Wegvakken.shp");
        List<Feature> result = shr.readShapeFile(new FeatureQualifier()
        {

            @Override
            public boolean qualify(Feature feature)
            {
                try
                {
                    for (OTSPoint3D p : FeatureViewer.designLine(feature).getPoints())
                    {
                        if (p.x < 80000 || p.y < 444000 || p.x > 90000 || p.y > 455000)
                        {
                            return false;
                        }
                    }
                }
                catch (OTSGeometryException e)
                {
                    e.printStackTrace();
                }
                return true;
                // String roadNumber = ((String) feature.getProperty("WEGNUMMER").getValue());
                // return roadNumber.contains("020") || roadNumber.contains("470") || roadNumber.contains("013")
                // || roadNumber.contains("471") || roadNumber.contains("209") || roadNumber.contains("472")
                // || roadNumber.contains("473");
            }
        });
        long end = System.nanoTime();
        System.out.println(String.format("Data collection time %.3fs", (end - start) / 1e9));
        System.out.println("Retrieved " + result.size() + " records");
        FeatureViewer viewer = new FeatureViewer(50, 50, 1000, 800);
        viewer.showRoadData(result);

        Map<Integer, Feature> wvkMap = new HashMap<>();
        for (Feature feature : result)
        {
            wvkMap.put(Math.toIntExact((Long) feature.getProperty("WVK_ID").getValue()), feature);
        }
        start = System.nanoTime();
        ShapeFileReader lanes = new ShapeFileReader(baseDir + File.separator + "wegvakken", date,
                "Rijstroken" + File.separator + "Rijstroken.shp");
        List<Feature> laneData = lanes.readShapeFile(new FeatureQualifier()
        {

            @Override
            public boolean qualify(Feature feature)
            {
                // System.out.println(feature);
                Integer wvkId = Math.toIntExact((Long) feature.getProperty("WVK_ID").getValue());
                return wvkMap.containsKey(wvkId);
                // if (((String) feature.getProperty("WEGNUMMER").getValue()).contains("013"))
                // {
                // System.out.println(feature);
                // }
                // return ((String) feature.getProperty("WEGNUMMER").getValue()).contains("013");
            }
        });
        end = System.nanoTime();
        System.out.println(String.format("Data collection time %.3fs", (end - start) / 1e9));
        System.out.println("Retrieved " + laneData.size() + " records");
        if (laneData.size() > 0)
        {
            FeatureViewer viewer2 = new FeatureViewer(450, 50, 1000, 800);
            viewer2.showRoadData(laneData);
        }
    }

}
