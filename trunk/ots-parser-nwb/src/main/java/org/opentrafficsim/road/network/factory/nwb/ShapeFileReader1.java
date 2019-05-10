package org.opentrafficsim.road.network.factory.nwb;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.MultiLineString;
import org.opengis.feature.Feature;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;

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
public class ShapeFileReader1
{
    /** URL to base of NWB shape files. */
    public static final String BASEDIR = "c:\\NWB";

    /** Sub directory with road names, geometry, authority, house number ranges. */
    public static final String ROADS_SUBDIR = "wegen";

    /** Sub directory with basic road information. */
    public static final String ROADS_DEFINITIONS = "Wegvakken" + File.separator + "Wegvakken.shp";

    /** Sub directory with locations of distance markers. */
    public static final String ROADS_DISTANCE_MARKERS = "hectopunten";

    /** Used to format dates retrieved from the shape files. */
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyyMMdd");

    /** Date for which shape files must be accessed. */
    private final int date;

    /** Directory and prefix for the road definition shape files. */
    private final String roadsShapeFile;

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
                if (dirDate > bestDate && dirDate <= this.date)
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
     * Construct a new NWB shape file reader.
     * @param date int; date encoded in 8 digits like YYYYMMDD.
     * @throws IOException
     * @throws MalformedURLException
     */
    public ShapeFileReader1(final int date) throws MalformedURLException, IOException
    {
        this.date = date;
        // Figure out what is available
        int bestDate = findBestDate(date, new File(BASEDIR + File.separator + ROADS_SUBDIR));
        System.out.println("Best matching directory is " + bestDate);
        this.roadsShapeFile = String.format("%s%s%s%s%08d%s%s", BASEDIR, File.separator, ROADS_SUBDIR, File.separator, bestDate,
                File.separator, ROADS_DEFINITIONS);
        System.out.println("shape files at \"" + this.roadsShapeFile + "\"");

    }

    /**
     * Read the basic road data.
     * @param qualifier RoadDataQualifier; will be used to determine which RoadData objects shall be returned
     * @return List&lt;RoadData&gt;; the road data
     * @throws IOException when anything goes wrong
     * @throws OTSGeometryException
     */
    public List<RoadData> readRoadData(final RoadDataQualifier qualifier) throws IOException, OTSGeometryException
    {
        List<RoadData> result = new ArrayList<>();
        DataStore dataStore =
                DataStoreFinder.getDataStore(Collections.singletonMap("url", new File(this.roadsShapeFile).toURI().toURL()));
        FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = dataStore.getFeatureSource(dataStore.getTypeNames()[0]);

        FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection = featureSource.getFeatures();
        FeatureIterator<SimpleFeature> featureIterator = featureCollection.features();

        while (featureIterator.hasNext())
        {
            Feature feature = featureIterator.next();
            RoadData roadData = new RoadData(feature);
            if (qualifier.qualify(roadData))
            {
                result.add(roadData);
            }
        }
        featureIterator.close();
        dataStore.dispose();
        return result;
    }

    /**
     * Stores all information that we may ever need from one record of the "wegvakken" shape files. See
     * <a href="http://publicaties.minienm.nl/download-bijlage/90775/releasedatum-28-11-2017.zip">this document</a>
     */
    static class RoadData
    {
        /** The weg vak Id (key to all other road properties). */
        final int wvkId;

        /** From junction (JTE_ID_BEG). */
        final int fromJunction;

        /** To junction (JTE_ID_END). */
        final int toJunction;

        /** Date from which this record is valid (WVK_BEGDAT). */
        final int fromDate;

        /** Road authority letter {R(ijk), P(rovince), G(emeente), W(aterschap), T (other)} (WEGBEHSRT). */
        final char roadAuthority;

        /** Road number (without any letters or other junk) (extracted from WEGNUMMER). */
        final short roadNumber;

        /** Road letter {A, N, #} (extracted from WEGNUMMER). */
        final char roadLetter;

        /** Sub road letter (used to distinguish parts of roads, divided by discontinuities in distance) (WEGDEELLTR). */
        final char partLetter;

        /** Letter on the distance posts (mostly to identify connector roads) (HECTO_LTTR). */
        final char hectoLetter;

        /**
         * Relative position from parallel road with same name {N(orth), Z(South), W(est), O(East), L(left of design line),
         * R(ight of design line), #(None)} (RPE_CODE).
         */
        final char relativePosition;

        /** Roadway sub type (BST_CODE). */
        final String roadwaySubType;

        /** Administrative direction {H(een; matches design line), T(erug; against design line)}, (ADMRICHTNG). */
        final char admDirection;

        /** Driving direction {H(een; matches design line), T(erug; against to design line)} (RIJRICHTNG). */
        final char drivingDirection;

        /** Name of the street (STT_NAAM). */
        final String streetName;

        /** Name of the city or village (WBSNAAMNEN). */
        final String localityName;

        /** Name of the municipality (GME_NAAM). */
        final String municipality;

        /** House number on the left (looking along design line) {E(ven), O(dd), N(one), B(oth), #(unknown)} (HNRSTRLNKS). */
        final char houseNumbersLeft;

        /** House number on the right (looking along design line) {E(ven), O(dd), N(one), B(oth), #(any)} (HNRSTRRHTS). */
        final char houseNumbersRight;

        /** First house number on the left (looking along design line) (-1 if none) (E_HNR_LNKS). */
        final short firstHouseNumberLeft;

        /** First house number on the right (looking along design line) (-1 if none) (E_HNR_RHTS). */
        final short firstHouseNumberRight;

        /** Last house number on the left (looking along design line) (-1 if none) (L_HNR_LNKS). */
        final short lastHouseNumberLeft;

        /** Last house number on the right (looking along design line) (-1 if none) (E_HNR_RHTS). */
        final short lastHouseNumberRight;

        /** Distance value at start of section in m (BEGAFSTAND). */
        final float beginDistance;

        /** Distance value at end of section in m(ENDAFSTAND). */
        final float endDistance;

        /** Posted distance at begin (converted to m) (BEGINKM). */
        final float beginRange;

        /** Posted distance at end (converted to m) (EINDKM). */
        final float endRange;

        /** Position relative to direction of increasing distance posts {L(eft), R(ight), M(id), #(None)} (POS_TOV_WOL). */
        final char position;

        /** Name of the road authority (WEGBEHNAAM). */
        final String authorityName;

        /** The design line. */
        final OTSLine3D designLine;

        /*-
         * Not parsed and stored:
         * WEGTYPE: two letter abbreviation of EGTYPE_OMS
         * WGTYPE_OMS: description of purpose of road 
         */

        /**
         * Load an int value that might be null and convert it to a short.
         * @param feature Feature; the data about one record in the "wegvakken" shape file
         * @param key String; key of the property
         * @param whenNull short; value to return when the value was null
         * @return short; the value, or <code>whenNull</code> if the value was null
         */
        private short getShort(final Feature feature, final String key, short whenNull)
        {
            Integer v = (Integer) feature.getProperty(key).getValue();
            return (short) (null == v ? -1 : v);
        }

        /**
         * Load a floating point value multiply it by a <code>factor</code> and convert it to float. If the value is null,
         * return the <code>whenNull</code> value.
         * @param feature Feature; the data about one record in the "wegvakken" shape file
         * @param key String; key of the property
         * @param factor double; multiplier for the retrieved value
         * @param whenNull float; value to return if the database contains null for the requested field
         * @return float; the loaded value multiplied by factor, or <code>whenNull</code> if the loaded value was null
         */
        private float getFloat(final Feature feature, final String key, final double factor, float whenNull)
        {
            Double d = (Double) feature.getProperty(key).getValue();
            if (null == d)
            {
                return -1f;
            }
            return (float) (d * factor);
        }

        /**
         * Construct a new RoadData object.
         * @param feature Feature; the data about one record in the "wegvakken" shape file
         * @throws IOException when some value is not available of out of range TODO: don't use IOException for everything that
         *             goes wrong
         * @throws OTSGeometryException
         */
        public RoadData(final Feature feature) throws IOException, OTSGeometryException
        {
            // for (Property property : feature.getProperties())
            // {
            // System.out.println("Property " + property.getName() + ": " + property.getType());
            // }
            this.wvkId = Math.toIntExact((Long) feature.getProperty("WVK_ID").getValue());
            this.fromDate = Integer.parseInt(SDF.format((Date) feature.getProperty("WVK_BEGDAT").getValue()));
            this.fromJunction = Math.toIntExact((Long) feature.getProperty("JTE_ID_BEG").getValue());
            this.toJunction = Math.toIntExact((Long) feature.getProperty("JTE_ID_END").getValue());
            this.relativePosition = ((String) feature.getProperty("RPE_CODE").getValue()).charAt(0);
            this.roadAuthority = ((String) feature.getProperty("WEGBEHSRT").getValue()).charAt(0);
            String admDir = ((String) feature.getProperty("ADMRICHTNG").getValue());
            this.admDirection = admDir.length() > 0 ? admDir.charAt(0) : '#';
            String driveDir = ((String) feature.getProperty("RIJRICHTNG").getValue());
            this.drivingDirection = driveDir.length() > 0 ? driveDir.charAt(0) : '#';
            this.roadwaySubType = ((String) feature.getProperty("BST_CODE").getValue());
            String roadLetterNumber = ((String) feature.getProperty("WEGNUMMER").getValue());
            roadLetterNumber = roadLetterNumber.replaceAll("[#]", "");
            if (roadLetterNumber.startsWith("N"))
            {
                this.roadLetter = 'N';
                roadLetterNumber = roadLetterNumber.substring(1);
            }
            else if (roadLetterNumber.startsWith("A"))
            {
                this.roadLetter = 'A';
                roadLetterNumber = roadLetterNumber.substring(1);
            }
            else
            {
                this.roadLetter = '#';
            }
            short rn = -1;
            try
            {
                if (roadLetterNumber.length() > 0)
                {
                    rn = (short) Integer.parseInt(roadLetterNumber);
                }
            }
            catch (NumberFormatException nfe)
            {
                System.out.println("bad road number: \"" + roadLetterNumber + "\"");
            }
            this.roadNumber = rn;
            this.streetName = (String) feature.getProperty("STT_NAAM").getValue();
            this.localityName = (String) feature.getProperty("WPSNAAMNEN").getValue();
            this.municipality = (String) feature.getProperty("GME_NAAM").getValue();
            String stringValue = ((String) feature.getProperty("HNRSTRLNKS").getValue());
            this.houseNumbersLeft = stringValue.length() > 0 ? stringValue.charAt(0) : '#';
            stringValue = ((String) feature.getProperty("HNRSTRRHTS").getValue());
            this.houseNumbersRight = stringValue.length() > 0 ? stringValue.charAt(0) : '#';
            this.firstHouseNumberLeft = getShort(feature, "E_HNR_LNKS", (short) -1);
            this.firstHouseNumberRight = getShort(feature, "E_HNR_RHTS", (short) -1);
            this.lastHouseNumberLeft = getShort(feature, "L_HNR_LNKS", (short) -1);
            this.lastHouseNumberRight = getShort(feature, "L_HNR_RHTS", (short) -1);
            this.beginDistance = getShort(feature, "BEGAFSTAND", (short) -1);
            this.endDistance = getShort(feature, "ENDAFSTAND", (short) -1);
            this.beginRange = getFloat(feature, "BEGINKM", 1000, -1f);
            this.endRange = getFloat(feature, "EINDKM", 1000, -1f);
            stringValue = ((String) feature.getProperty("POS_TV_WOL").getValue());
            this.position = stringValue.length() > 0 ? stringValue.charAt(0) : '#';
            this.partLetter = ((String) feature.getProperty("WEGDEELLTR").getValue()).charAt(0);
            this.hectoLetter = ((String) feature.getProperty("HECTO_LTTR").getValue()).charAt(0);
            this.authorityName = ((String) feature.getProperty("WEGBEHNAAM").getValue());
            GeometryAttribute geometry = feature.getDefaultGeometryProperty();
            MultiLineString multiLineString = (MultiLineString) geometry.getValue();
            Coordinate[] coordinates = multiLineString.getCoordinates();
            this.designLine = new OTSLine3D(coordinates);
            // String wegType = (String) feature.getProperty("WEGTYPE").getValue();
            // if (wegType.trim().length() > 0)
            // {
            // System.out.println("WEGTYPE code is " + wegType + ": " + ((String) feature.getProperty("WGTYPE_OMS").getValue())
            // + " record is " + this);
            // }

        }

        @Override
        public String toString()
        {
            return "RoadData [wvkId=" + this.wvkId + ", since=" + this.fromDate + ", from=" + this.fromJunction + ", to="
                    + this.toJunction + ", roadAuthority=" + roadAuthority + ", autorityName=" + this.authorityName + ", nr="
                    + this.roadNumber + ", streetName=" + this.streetName + ", locality=" + this.localityName
                    + ", municipality=" + this.municipality + ", wdl=" + this.partLetter + ", hl=" + this.hectoLetter
                    + ", admDir=" + this.admDirection + ", drivingDir=" + this.drivingDirection + ", rst=" + this.roadwaySubType
                    + ", hnl=" + this.houseNumbersLeft + ":" + this.firstHouseNumberLeft + ".." + this.lastHouseNumberLeft
                    + ", hnr=" + this.houseNumbersRight + ":" + this.firstHouseNumberRight + ".." + this.lastHouseNumberRight
                    + ", distance=" + this.beginDistance + ".." + this.endDistance + ", m=" + this.beginRange + ".."
                    + this.endRange + ", pos=" + this.position + ", coordinates=" + this.designLine + "]";
        }

    }

    /**
     * Determine if a RoadData object should be stored.
     */
    interface RoadDataQualifier
    {
        /**
         * Determine if a RoadData object should be saved for use.
         * @param roadData RoadData; the RoadData object
         * @return boolean; true of the RoadData should be saved; false if it should be discarded
         */
        boolean qualify(RoadData roadData);
    }
    
}
