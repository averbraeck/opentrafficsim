package org.opentrafficsim.road.network.factory.nwb;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.geotools.data.DataStore;
import org.geotools.data.FeatureSource;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

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
public class ShapeFileReader
{
    /** Root of the shape files. */
    final File baseDir;

    /** Date closest to (but not later than) requested by the user. */
    final int date;

    /** The .shp file. */
    final File subDirAndShapeFile;

    /**
     * Find the last date, no later than the specified <code>date</code> for which there is a sub directory.
     * @param date int; date encoded as an 8-digit number (YYYYMMDD)
     * @param dir File; directory to scan for sub-directories that have 8-digit numerical names
     * @return int; the 8-digit number that identifies the best matching sub directory
     * @throws FileNotFoundException when no suitable sub directory exists
     */
    public int findBestDate(final int date, final File dir) throws FileNotFoundException
    {
        int bestDate = 0;
        for (File file : dir.listFiles())
        {
            if (!file.isDirectory())
            {
                continue;
            }
            try
            {
                int dirDate = Integer.parseInt(file.getName());
                if (dirDate > bestDate && dirDate <= date)
                {
                    bestDate = dirDate;
                }
            }
            catch (NumberFormatException nfe)
            {
                // Ignore files and directories that are not an 8-digit date.
            }
        }
        if (0 == bestDate)
        {
            throw new FileNotFoundException("no directory found with numerical name not later than requested");
        }
        return bestDate;
    }

    /**
     * Construct a new ShapeFileReader.
     * @param baseDir String; name of the directory under which the sub-directories with names representing the date of a
     *            version of the shape file database (formatted like YYYYMMDD) are stored
     * @param date int; requested date (formatted like YYYYMMDD)
     * @param subDirAndShapeFile String; name of the sub-directory with the shape file relative to the date-named directory
     * @throws FileNotFoundException when a needed directory does not exist
     */
    public ShapeFileReader(final String baseDir, final int date, final String subDirAndShapeFile) throws FileNotFoundException
    {
        this.baseDir = new File(baseDir);
        this.date = findBestDate(date, this.baseDir);
        this.subDirAndShapeFile =
                new File(this.baseDir.getAbsolutePath() + File.separator + this.date + File.separator + subDirAndShapeFile);
    }

    /**
     * Read a shape file and collect records that match a caller-provided check.
     * @param qualifier FeatureQualifier; the check to use
     * @return List&lt;Feature&gt;; the Features that passed the check
     * @throws IOException when reading a file failed
     */
    public List<Feature> readShapeFile(final FeatureQualifier qualifier) throws IOException
    {
        List<Feature> result = new ArrayList<>();
        DataStore dataStore = (ShapefileDataStore) FileDataStoreFinder.getDataStore(subDirAndShapeFile);
        FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = dataStore.getFeatureSource(dataStore.getTypeNames()[0]);

        FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection = featureSource.getFeatures();
        FeatureIterator<SimpleFeature> featureIterator = featureCollection.features();
        while (featureIterator.hasNext())
        {
            Feature feature = featureIterator.next();
            if (qualifier.qualify(feature))
            {
                result.add(feature);
            }
        }
        featureIterator.close();
        dataStore.dispose();
        return result;
    }

    /**
     * Determine if a Feature object should be stored.
     */
    interface FeatureQualifier
    {
        /**
         * Determine if a Feature object should be saved for use.
         * @param feature Feature; the Feature object
         * @return boolean; true of the Feature should be saved; false if it should be discarded
         */
        boolean qualify(Feature feature);
    }

}
