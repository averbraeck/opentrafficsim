package org.opentrafficsim.demo.ntm.IO;

/**
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$, initial version 4 Nov 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://Hansvanlint.weblog.tudelft.nl">Hans van Lint</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @author <a href="http://www.citg.tudelft.nl">Yufei Yuan</a>
 */

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;

import org.geotools.data.DefaultTransaction;
import org.geotools.data.Transaction;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.geotools.swing.JMapFrame;
import org.geotools.swing.action.SafeAction;
import org.geotools.swing.data.JFileDataStoreChooser;
import org.geotools.util.ProgressListener;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.operation.valid.IsValidOp;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureVisitor;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opentrafficsim.demo.ntm.Area;

/**
 * This example reads data for point locations and associated attributes from a comma separated text (CSV) file and exports them
 * as a new shapefile. It illustrates how to build a feature type.
 * <p>
 * Note: to keep things simple in the code below the input file should not have additional spaces or tabs between fields.
 */
public class WriteToShp
{
    static SimpleFeatureSource featureSource = null;

    public static void createShape(Map<String, Area> areas)
    {
        /*
         * Get an output file name and create the new shapefile
         */
        String file = "D:/gtamminga/My Documents/03 Case The Hague NTM/TNO data/selectedAreasGT.shp";
        File newFile = getNewShapeFile(file);
        boolean DEBUG = false;
        ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();

        Map<String, Serializable> params = new LinkedHashMap<String, Serializable>();
        try
        {
            params.put("url", newFile.toURI().toURL());
        }
        catch (MalformedURLException exception)
        {
            exception.printStackTrace();
        }
        params.put("create spatial index", Boolean.TRUE);

        ShapefileDataStore newDataStore = null;
        try
        {
            newDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);
        }
        catch (IOException exception)
        {
            exception.printStackTrace();
        }

        SimpleFeatureType TYPE;
        if (!DEBUG)
        {
            TYPE = createFeatureTypeMultiPolygon();
        }
        else
        {
            TYPE = createFeatureTypePoint();
        }
        try
        {
            newDataStore.createSchema(TYPE);
        }
        catch (IOException exception)
        {
            exception.printStackTrace();
        }

        /*
         * A list to collect features as we create them.
         */
        List<SimpleFeature> features = new ArrayList<SimpleFeature>();
        /*
         * GeometryFactory will be used to create the geometry attribute of each feature, using a Point object for the location.
         */
        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(TYPE);
        int i = 0;
        /* First line of the data file is the header */
        /* Longitude (= x coord) first ! */
        if (DEBUG)
        {
            Point point = geometryFactory.createPoint(new Coordinate(300001.0, 280000.0));
            String Id = "1";
            featureBuilder.add(point);
            featureBuilder.add(Id);
            featureBuilder.add(200);
            SimpleFeature feature = featureBuilder.buildFeature(null);
            features.add(feature);
            point = geometryFactory.createPoint(new Coordinate(300201.0, 287000.0));
            Id = "2";
            featureBuilder.add(point);
            featureBuilder.add(Id);
            featureBuilder.add(300);
            feature = featureBuilder.buildFeature(null);
            features.add(feature);
        }
        if (!DEBUG)
        {
            for (Area area : areas.values())
            {
                // Conditionally force valid polygons
                if ((area.getGeometry() instanceof Polygon || area.getGeometry() instanceof MultiPolygon)
                        && !IsValidOp.isValid(area.getGeometry()))
                {
                    area.getGeometry().convexHull(); // or even: geom = geom.getEnvelope();
                }
                if (area.getGeometry().getGeometryType().equals("MultiPolygon")
                        || area.getGeometry().getGeometryType().equals("Polygon"))
                {
                    String Id = Integer.toString(i);
                    MultiPolygon multiPolygon = null;
                    if (area.getGeometry().getGeometryType().equals("Polygon"))
                    {
                        Polygon[] polygons = new Polygon[1];
                        polygons[0] = (Polygon) area.getGeometry();
                        multiPolygon = geometryFactory.createMultiPolygon(polygons);
                    }
                    else
                    {
                        multiPolygon = (MultiPolygon) area.getGeometry();
                    }
                    // Point multiPolygon = area.getDesignLine().getCentroid();
                    featureBuilder.add(multiPolygon);
                    featureBuilder.add(area.getCentroidNr());
                    SimpleFeature feature = featureBuilder.buildFeature(null);
                    features.add(feature);
                    i++;
                }
                else
                {
                    System.out.println(area.getGeometry().getGeometryType());

                }
            }
        }

        /*
         * Write the features to the shapefile
         */
        Transaction transaction = new DefaultTransaction("create");

        String typeName = null;
        try
        {
            typeName = newDataStore.getTypeNames()[0];
        }
        catch (IOException exception)
        {
            exception.printStackTrace();
        }

        try
        {
            featureSource = newDataStore.getFeatureSource(typeName);
        }
        catch (IOException exception)
        {
            exception.printStackTrace();
        }
        SimpleFeatureType SHAPE_TYPE = featureSource.getSchema();
        /*
         * The Shapefile format has a couple limitations: - "the_geom" is always first, and used for the geometry attribute name
         * - "the_geom" must be of type Point, MultiPoint, MuiltiLineString, MultiPolygon - Attribute names are limited in
         * length - Not all data types are supported (example Timestamp represented as Date) Each data store has different
         * limitations so check the resulting SimpleFeatureType.
         */
        System.out.println("SHAPE:" + SHAPE_TYPE);

        if (featureSource instanceof SimpleFeatureStore)
        {
            SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;
            /*
             * SimpleFeatureStore has a method to add features from a SimpleFeatureCollection object, so we use the
             * ListFeatureCollection class to wrap our list of features.
             */
            SimpleFeatureCollection collection = new ListFeatureCollection(TYPE, features);
            featureStore.setTransaction(transaction);

            try
            {
                featureStore.addFeatures(collection);
                // Create a map content and add our shapefile to it
                transaction.commit();
            }
            catch (Exception problem)
            {
                problem.printStackTrace();
                try
                {
                    transaction.rollback();
                }
                catch (IOException exception)
                {
                    exception.printStackTrace();
                }
            }
            finally
            {
                try
                {
                    if (DEBUG)
                    {
                        MapContent map = new MapContent();
                        map.setTitle("Quickstart");
                        Style style = SLD.createSimpleStyle(featureStore.getSchema());
                        Layer layer = new FeatureLayer(featureStore, style);
                        map.addLayer(layer);
                        // Create a JMapFrame with custom toolbar buttons
                        JMapFrame mapFrame = new JMapFrame(map);
                        mapFrame.enableToolBar(true);
                        mapFrame.enableStatusBar(true);

                        JToolBar toolbar = mapFrame.getToolBar();
                        toolbar.addSeparator();
                        ValidateGeometryAction test = new WriteToShp.ValidateGeometryAction();
                        toolbar.add(new JButton(new WriteToShp.ValidateGeometryAction()));

                        // Display the map frame. When it is closed the application will exit
                        mapFrame.setSize(800, 600);
                        mapFrame.setVisible(true);
                    }
                    transaction.close();
                }
                catch (IOException exception)
                {
                    exception.printStackTrace();
                }
            }
            // System.exit(0); // success!
        }
        else
        {
            System.out.println(typeName + " does not support read/write access");
            // System.exit(1);
        }

    }

    /**
     * Prompt the user for the name and path to use for the output shapefile
     * @param csvFile the input csv file used to create a default shapefile name
     * @return name and path for the shapefile as a new File object
     */
    private static File getNewShapeFile(String file)
    {
        String newPath = file;
        JFileDataStoreChooser chooser = new JFileDataStoreChooser("shp");
        chooser.setDialogTitle("Save shapefile");
        chooser.setSelectedFile(new File(newPath));

        int returnVal = chooser.showSaveDialog(null);
        if (returnVal != JFileDataStoreChooser.APPROVE_OPTION)
        {
            // the user cancelled the dialog
            System.exit(0);
        }
        File newFile = chooser.getSelectedFile();
        return newFile;
    }

    /**
     * Here is how you can use a SimpleFeatureType builder to create the schema for your shapefile dynamically.
     * <p>
     * This method is an improvement on the code used in the main method above (where we used DataUtilities.createFeatureType)
     * because we can set a Coordinate Reference System for the FeatureType and a a maximum field length for the 'name' field
     * dddd
     */
    private static SimpleFeatureType createFeatureTypeMultiPolygon()
    {
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName("Region");
        builder.setCRS(DefaultGeographicCRS.WGS84); // DefaultGeographicCRS.WGS84 <- Coordinate reference system
        // add attributes in order
        builder.add("the_geom", MultiPolygon.class);
        builder.length(15).add("Name", String.class); // <- 15 chars width for name field
        builder.add("Number", Integer.class);
        // build the type
        final SimpleFeatureType REGION = builder.buildFeatureType();
        return REGION;
    }

    /**
     * Here is how you can use a SimpleFeatureType builder to create the schema for your shapefile dynamically.
     * <p>
     * This method is an improvement on the code used in the main method above (where we used DataUtilities.createFeatureType)
     * because we can set a Coordinate Reference System for the FeatureType and a a maximum field length for the 'name' field
     * dddd
     */
    private static SimpleFeatureType createFeatureTypePoint()
    {
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName("Point");
        builder.setCRS(DefaultGeographicCRS.WGS84); // <- Coordinate reference system
        // add attributes in order
        builder.add("the_geom", Point.class);
        builder.length(15).add("Name", String.class); // <- 15 chars width for name field
        builder.add("Number", Integer.class);

        // build the type
        final SimpleFeatureType POINT = builder.buildFeatureType();
        return POINT;
    }

    private static int validateFeatureGeometry(ProgressListener progress) throws Exception
    {
        final SimpleFeatureCollection featureCollection = featureSource.getFeatures();

        // Rather than use an iterator, create a FeatureVisitor to check each fature
        class ValidationVisitor implements FeatureVisitor
        {
            public int numInvalidGeometries = 0;

            public void visit(Feature f)
            {
                SimpleFeature feature = (SimpleFeature) f;
                Geometry geom = (Geometry) feature.getDefaultGeometry();
                if (geom != null && !geom.isValid())
                {
                    numInvalidGeometries++;
                    System.out.println("Invalid Geoemtry: " + feature.getID());
                }
            }
        }

        ValidationVisitor visitor = new ValidationVisitor();

        // Pass visitor and the progress bar to feature collection
        featureCollection.accepts(visitor, progress);
        return visitor.numInvalidGeometries;
    }

    public static class ValidateGeometryAction extends SafeAction
    {
        ValidateGeometryAction()
        {
            super("Validate geometry");
            putValue(Action.SHORT_DESCRIPTION, "Check each geometry");
        }

        public void action(ActionEvent e) throws Throwable
        {
            int numInvalid = validateFeatureGeometry(null);
            String msg;
            if (numInvalid == 0)
            {
                msg = "All feature geometries are valid";
            }
            else
            {
                msg = "Invalid geometries: " + numInvalid;
            }
            JOptionPane.showMessageDialog(null, msg, "Geometry results", JOptionPane.INFORMATION_MESSAGE);
        }
    }

}
