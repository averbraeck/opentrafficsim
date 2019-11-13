package org.opentrafficsim.demo.ntm.shapeobjects;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.geotools.data.DefaultTransaction;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.Transaction;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.operation.valid.IsValidOp;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version 13 Nov 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://Hansvanlint.weblog.tudelft.nl">Hans van Lint</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @author <a href="http://www.citg.tudelft.nl">Yufei Yuan</a>
 */

public class ShapeStore
{
    /** */
    private ArrayList<ShapeObject> geoObjects;

    /** */
    private ArrayList<String> variableNames;

    /** */
    private LinkedHashMap<String, String> variableTypeMap;

    /** */
    private LinkedHashMap<String, Class<? extends Object>> attributeClassTypes;

    /**
     * @param variableName
     * @param geometries
     * @param variableTypeMap LinkedHashMap&lt;String,String&gt;;
     * @param attributeClassTypes LinkedHashMap&lt;String,Class&lt;? extends Object&gt;&gt;;
     */
    public ShapeStore(ArrayList<ShapeObject> geoObjects, ArrayList<String> variableNames,
            LinkedHashMap<String, String> variableTypeMap, LinkedHashMap<String, Class<? extends Object>> attributeClassTypes)
    {
        super();
        this.geoObjects = geoObjects;
        this.variableNames = variableNames;
        this.variableTypeMap = variableTypeMap;
        this.attributeClassTypes = attributeClassTypes;
    }

    public void addAttribute(String newField, String type)
    {
        boolean attributeAlreadyExists = false;
        for (String variableName : this.getVariableNames())
        {
            if (newField.equals(variableName))
            {
                attributeAlreadyExists = true;
            }
        }
        if (!attributeAlreadyExists)
        {
            this.getVariableTypeMap().put(newField, type);
            this.getVariableNames().add(newField);
            if (type.equals("Double"))
            {
                this.getAttributeClassTypes().put(newField, Double.class);
                // add empty string
                for (ShapeObject shape : this.getGeoObjects())
                {
                    shape.getValues().add("0.0");
                }
            }
            else if (type.equals("Long"))
            {
                this.getAttributeClassTypes().put(newField, Long.class);
                for (ShapeObject shape : this.getGeoObjects())
                {
                    shape.getValues().add("0.0");
                }

            }
            else if (type.equals("Integer"))
            {
                this.getAttributeClassTypes().put(newField, Integer.class);
                for (ShapeObject shape : this.getGeoObjects())
                {
                    shape.getValues().add("0");
                }

            }
            else if (type.equals("String"))
            {
                this.getAttributeClassTypes().put(newField, String.class);
                for (ShapeObject shape : this.getGeoObjects())
                {
                    shape.getValues().add(" ");
                }

            }

        }
    }

    /**
     * @param file File;
     * @return
     * @throws IOException
     */
    public static ShapeStore openGISFile(File file) throws IOException
    {
        ShapefileDataStore dataStore = (ShapefileDataStore) FileDataStoreFinder.getDataStore(file);
        // feature type name is defaulted to the name of shapefile (without extension)
        SimpleFeatureSource featureSourceAreas = dataStore.getFeatureSource();
        SimpleFeatureCollection featureCollectionAreas = featureSourceAreas.getFeatures();
        SimpleFeatureIterator iterator = featureCollectionAreas.features();
        ShapeStore shapeStore = null;
        try
        {
            boolean attribute = true;
            ArrayList<ShapeObject> shapeObjects = new ArrayList<ShapeObject>();
            ArrayList<String> attributeNames = new ArrayList<String>();
            LinkedHashMap<String, String> attributeTypes = new LinkedHashMap<String, String>();
            LinkedHashMap<String, Class<? extends Object>> attributeClassTypes = new LinkedHashMap<String, Class<? extends Object>>();
            while (iterator.hasNext())
            {
                SimpleFeature feature = iterator.next();

                // at the first feature, the attributes are detected and defined
                if (attribute)
                {
                    Iterator<Property> attributeIterator = feature.getProperties().iterator();
                    while (attributeIterator.hasNext())
                    {
                        String name = attributeIterator.next().getDescriptor().getName().toString();
                        if (!name.equals("the_geom"))
                        {
                            attributeNames.add(name);
                        }
                    }

                    attributeIterator = feature.getProperties().iterator();
                    int index = 0;
                    boolean geom = true;
                    while (attributeIterator.hasNext())
                    {
                        Class<? extends Object> type = attributeIterator.next().getType().getBinding();
                        String line = type.getCanonicalName();
                        String[] nameType = line.split("\\.");
                        if (!geom)
                        {
                            attributeTypes.put(attributeNames.get(index), nameType[nameType.length - 1]);
                            attributeClassTypes.put(attributeNames.get(index), type);
                            index++;
                        }
                        geom = false;
                    }
                    attribute = false;
                }
                // The first element is the Geometry!
                Geometry geometry = (Geometry) feature.getAttribute("the_geom");
                // The others are by type...
                ArrayList<String> values = new ArrayList<String>();
                for (String element : attributeNames)
                {
                    if (!element.equals("the_geom"))
                    {
                        String value = String.valueOf(feature.getAttribute(element));
                        values.add(value);
                    }
                }
                ShapeObject shape = new ShapeObject(geometry, values);
                shapeObjects.add(shape);
            }
            shapeStore = new ShapeStore(shapeObjects, attributeNames, attributeTypes, attributeClassTypes);
        }
        catch (Exception problem)
        {
            problem.printStackTrace();
        }
        finally
        {
            iterator.close();
            dataStore.dispose();
        }
        return shapeStore;
    }

    /** */
    static SimpleFeatureSource featureSource = null;

    /**
     * Create an output shape file name and create the new shape file
     * @param shapes ShapeStore;
     * @param startMap
     * @throws IOException
     */
    public static void createShapeFile(ShapeStore shapes, File newFile) throws IOException
    {
        Map<String, Serializable> params = new LinkedHashMap<String, Serializable>();
        params.put("url", newFile.toURI().toURL());
        params.put("create spatial index", Boolean.TRUE);

        ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();
        ShapefileDataStore newDataStore = null;
        newDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);
        SimpleFeatureType featureType = null;

        featureType = createFeatureType(shapes);
        newDataStore.createSchema(featureType);
        /*
         * A list to collect features as we create them.
         */
        List<SimpleFeature> features = new ArrayList<SimpleFeature>();
        /*
         * GeometryFactory will be used to create the geometry attribute of each feature, using a Point object for the location.
         */
        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(featureType);
        int i = 0;
        /* First line of the data file is the header */
        /* Longitude (= x coord) first ! */
        for (ShapeObject shape : shapes.getGeoObjects())
        {
            // Conditionally force valid polygons
            if ((shape.getDesignLine() instanceof Polygon || shape.getDesignLine() instanceof MultiPolygon)
                    && !IsValidOp.isValid(shape.getDesignLine()))
            {
                shape.getDesignLine().convexHull();
            }
            if (shape.getDesignLine() instanceof MultiPolygon || shape.getDesignLine() instanceof Polygon)
            {
                MultiPolygon multiPolygon = null;
                if (shape.getDesignLine() instanceof Polygon)
                {
                    Polygon[] polygons = new Polygon[1];
                    polygons[0] = (Polygon) shape.getDesignLine();
                    multiPolygon = geometryFactory.createMultiPolygon(polygons);
                }
                else
                {
                    multiPolygon = (MultiPolygon) shape.getDesignLine();
                }
                // Point multiPolygon = area.getDesignLine().getCentroid();
                featureBuilder.add(multiPolygon);
            }
            else if (shape.getDesignLine() instanceof MultiLineString)
            {
                MultiLineString multiLineString = (MultiLineString) shape.getDesignLine();
                featureBuilder.add(multiLineString);
            }
            else if (shape.getDesignLine() instanceof Point)
            {
                Point point = (Point) shape.getDesignLine();
                featureBuilder.add(point);
            }
            else
            {
                System.out.println(shape.getDesignLine());
            }
            // we don't need use the first name ("the_geom") which is left out of the values
            int index = 0;
            for (String value : shape.getValues())
            {
                String type = shapes.getVariableTypeMap().get(shapes.getVariableNames().get(index));
                if (type.equals("Double"))
                {
                    featureBuilder.add(Double.parseDouble(value));
                }
                else if (type.equals("Integer"))
                {
                    featureBuilder.add(Integer.parseInt(value));
                }
                else if (type.equals("Long"))
                {
                    featureBuilder.add(Long.parseLong(value));
                }
                else if (type.equals("String"))
                {
                    featureBuilder.add(value);
                }
                // TODO add the date data type!
                index++;
            }
            SimpleFeature feature = featureBuilder.buildFeature(null);
            features.add(feature);
        }

        /*
         * Write the features to the shapefile
         */
        Transaction transaction = new DefaultTransaction("create");

        String typeName = null;
        typeName = newDataStore.getTypeNames()[0];
        featureSource = newDataStore.getFeatureSource(typeName);
        /*
         * The Shapefile format has a couple limitations: - "the_geom" is always first, and used for the geometry attribute name
         * - "the_geom" must be of type Point, MultiPoint, MuiltiLineString, MultiPolygon - Attribute names are limited in
         * length - Not all data types are supported (example Timestamp represented as Date) Each data store has different
         * limitations so check the resulting SimpleFeatureType.
         */

        if (featureSource instanceof SimpleFeatureStore)
        {
            SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;
            /*
             * SimpleFeatureStore has a method to add features from a SimpleFeatureCollection object, so we use the
             * ListFeatureCollection class to wrap our list of features.
             */
            SimpleFeatureCollection collection = new ListFeatureCollection(featureType, features);
            featureStore.setTransaction(transaction);

            featureStore.addFeatures(collection);
            // Create a map content and add our shapefile to it
            transaction.commit();
            transaction.close();
        }
        else
        {
            System.out.println(typeName + " does not support read/write access");
        }

    }

    /**
     * Here is how you can use a SimpleFeatureType builder to create the schema for your shapefile dynamically.
     * <p>
     * This method is an improvement on the code used in the main method above (where we used DataUtilities.createFeatureType)
     * because we can set a Coordinate Reference System for the FeatureType and a a maximum field length for the 'name' field
     * dddd
     */
    private static SimpleFeatureType createFeatureType(final ShapeStore shapes)
    {
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName("Test");
        builder.setCRS(DefaultGeographicCRS.WGS84); // DefaultGeographicCRS.WGS84 <- Coordinate reference system
        // add attributes in order
        // builder.add("the_geom", MultiPolygon.class);
        builder.add("the_geom", shapes.geoObjects.get(0).getDesignLine().getClass());
        for (String name : shapes.getVariableNames())
        {
            if (shapes.getVariableTypeMap().get(name) == null)
            {
                System.out.println("leeg");
            }
            if (shapes.getVariableTypeMap().get(name).equals("String"))
            {
                builder.length(15).add(name, String.class); // <- 15 chars width for name field
            }
            else if (shapes.getVariableTypeMap().get(name).equals("Integer"))
            {
                builder.add(name, Integer.class);
            }
            else if (shapes.getVariableTypeMap().get(name).equals("Double"))
            {
                builder.add(name, Double.class);
            }
            else if (shapes.getVariableTypeMap().get(name).equals("Long"))
            {
                builder.add(name, Long.class);
            }

        }
        // build the type
        final SimpleFeatureType featureType = builder.buildFeatureType();
        return featureType;
    }

    /**
     * @return variableName.
     */
    public ArrayList<String> getVariableNames()
    {
        return this.variableNames;
    }

    /**
     * @param variableName set variableName.
     */
    public void setVariableNames(ArrayList<String> variableNames)
    {
        this.variableNames = variableNames;
    }

    /**
     * @return variableTypeMap.
     */
    public LinkedHashMap<String, String> getVariableTypeMap()
    {
        return this.variableTypeMap;
    }

    /**
     * @param variableTypeMap LinkedHashMap&lt;String,String&gt;; set variableTypeMap.
     */
    public void setVariableTypeMap(LinkedHashMap<String, String> variableTypeMap)
    {
        this.variableTypeMap = variableTypeMap;
    }

    /**
     * @return attributeClassTypes.
     */
    public LinkedHashMap<String, Class<? extends Object>> getAttributeClassTypes()
    {
        return attributeClassTypes;
    }

    /**
     * @param attributeClassTypes LinkedHashMap&lt;String,Class&lt;? extends Object&gt;&gt;; set attributeClassTypes.
     */
    public void setAttributeClassTypes(LinkedHashMap<String, Class<? extends Object>> attributeClassTypes)
    {
        this.attributeClassTypes = attributeClassTypes;
    }

    /**
     * @return geoObjects.
     */
    public ArrayList<ShapeObject> getGeoObjects()
    {
        return geoObjects;
    }

    /**
     * @param geoObjects ArrayList&lt;ShapeObject&gt;; set geoObjects.
     */
    public void setGeoObjects(ArrayList<ShapeObject> geoObjects)
    {
        this.geoObjects = geoObjects;
    }

}
