package org.opentrafficsim.road.network.factory.nwb;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import org.opengis.feature.Feature;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.road.network.factory.nwb.ShapeFileReader.FeatureQualifier;
import org.opentrafficsim.road.network.factory.nwb.ShapeFileReader1.RoadData;
import org.opentrafficsim.road.network.factory.nwb.ShapeFileReader1.RoadDataQualifier;

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
     * @throws IOException
     * @throws MalformedURLException
     * @throws OTSGeometryException
     */
    public static void main(final String[] args) throws MalformedURLException, IOException, OTSGeometryException
    {
        long start = System.nanoTime();
        ShapeFileReader shr = new ShapeFileReader("c:\\NWB" + File.separator + "wegen", 20190401,
                "Wegvakken" + File.separator + "Wegvakken.shp");
        List<Feature> result = shr.readShapeFile(new FeatureQualifier()
        {

            @Override
            public boolean qualify(Feature feature)
            {
                // System.out.println("value of wegnummer property is \"" + feature.getProperty("WEGNUMMER").getValue() + "\"");
                return ((String) feature.getProperty("WEGNUMMER").getValue()).contains("470");
            }
        });
        long end = System.nanoTime();
        System.out.println(String.format("Data collection time %.3fs", (end - start) / 1e9));
        System.out.println("Retrieved " + result.size() + " records");
        System.out.println(String.format("Data collection time %.3fs", (end - start) / 1e9));
        System.out.println("Retrieved " + result.size() + " records");
        FeatureViewer viewer = new FeatureViewer();
        viewer.showRoadData(result);
    }

    /**
     * Program entry point.
     * @param args String[]; the command line arguments
     * @throws IOException
     * @throws MalformedURLException
     * @throws OTSGeometryException
     */
    public static void mainOld(final String[] args) throws MalformedURLException, IOException, OTSGeometryException
    {
        long start = System.nanoTime();
        ShapeFileReader1 shr = new ShapeFileReader1(20190401);
        List<RoadData> result = shr.readRoadData(new RoadDataQualifier()
        {
            @Override
            public boolean qualify(final RoadData roadData)
            {
                // Reject all shapes that are not entirely within a bounding box around Delft.
                for (OTSPoint3D p : roadData.designLine.getPoints())
                {
                    if (p.x < 80000 || p.y < 443000 || p.x > 88000 || p.y > 450000)
                    {
                        return false;
                    }
                }
                // return true;
                return roadData.roadNumber == 470;
                // return roadData.beginDistance > 0; // never happens
            };
        });
        long end = System.nanoTime();
        System.out.println(String.format("Data collection time %.3fs", (end - start) / 1e9));
        // for (RoadData roadData : result)
        // {
        // System.out.println(roadData);
        // }
        System.out.println("Retrieved " + result.size() + " records");
        ShapeViewer viewer = new ShapeViewer();
        viewer.showRoadData(result);
    }
}
