package org.opentrafficsim.imb.connector;

import org.opentrafficsim.base.modelproperties.AbstractProperty;
import org.opentrafficsim.base.modelproperties.CompoundProperty;
import org.opentrafficsim.base.modelproperties.IntegerProperty;
import org.opentrafficsim.base.modelproperties.PropertyException;
import org.opentrafficsim.base.modelproperties.StringProperty;
import org.opentrafficsim.imb.IMBException;

import nl.tudelft.simulation.language.Throw;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Aug 28, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class OTSIMBConnector extends IMBConnector
{
    /** Key for compound property with IMB settings. */
    public static String PROPERTY_KEY = "IMBProperties";

    /**
     * Create a new OTSIMBConnector by specifying all the details.
     * @param host String; the host that runs the IMB hub server
     * @param port int; the port number on the IMB hub host
     * @param modelName String; the name used to register on the hub host
     * @param modelId integer; usually 1
     * @param federation string; usually "OTS_RT"
     * @throws IMBException when a connection to the IMB hub could not be established
     */
    public OTSIMBConnector(final String host, final int port, final String modelName, final int modelId,
            final String federation) throws IMBException
    {
        super(host, port, modelName, modelId, federation);
    }

    /**
     * Create a new OTSIMBConnector expecting the IMB hub on localhost port 4000.
     * @param modelName String; the name used to register on the hub host
     * @return OTSIMBConnector; a new OTSIMBConnector expecting the IMB hub on localhost port 4000.
     * @throws IMBException when a connection to the IMB hub could not be established
     */
    public static OTSIMBConnector create(final String modelName) throws IMBException
    {
        return new OTSIMBConnector("localhost", 4000, modelName, 1, "OTS_RT");
    }

    /**
     * Construct a new OTSIMBConnector from a CompoundProperty (preferably constructed with the
     * <cite>standardIMBProperties</cite> method of this class.
     * @param compoundProperty CompoundProperty; the compound property with the settings
     * @param modelName String; the name used to register on the hub host
     * @return OTSIMBConnector; a new OTSIMBConnector expecting the IMB hub on localhost port 4000.
     * @throws IMBException when a connection to the IMB hub could not be established
     */
    public static OTSIMBConnector create(final CompoundProperty compoundProperty, final String modelName) throws IMBException
    {
        String host = null;
        int port = -1;
        int modelId = 1;
        String federation = "OTS_RT";

        for (AbstractProperty<?> ap : compoundProperty)
        {
            switch (ap.getKey())
            {
                case "IMBHost":
                    host = ((StringProperty) ap).getValue();
                    break;

                case "IMBPort":
                    port = ((IntegerProperty) ap).getValue();
                    break;

                case "IMBModelId":
                    modelId = ((IntegerProperty) ap).getValue();
                    break;

                case "IMBFederation":
                    federation = ((StringProperty) ap).getValue();
                    break;

                default:
                    System.err.println("Ignoring property " + ap);
            }
        }
        Throw.when(host == null, IMBException.class, "host may not be null");
        System.out.println("IMB: connecting to " + host + ":" + port);
        return new OTSIMBConnector(host, port, modelName, modelId, federation);
    }

    /**
     * Create a CompoundProperty with the settings for an IMB connection.
     * @param displayPriority int; the displayPriority of the created CompoundProperty
     * @return CompoundProperty the default settings
     */
    public static CompoundProperty standardIMBProperties(final int displayPriority)
    {
        try
        {
            CompoundProperty result =
                    new CompoundProperty("IMBProperties", "IMB properties", "IMB properties", null, false, displayPriority);
            result.add(new StringProperty("IMBHost", "IMB hub host", "Name of the IMB hub", "localhost", false, 0));
            result.add(new IntegerProperty("IMBPort", "IMB hub port", "Port on the IMB hub", 4000, 0, 65535, "%d", false, 1));
            result.add(new IntegerProperty("IMBModelId", "IMB model id", "Model id", 1, 0, 9999, "%d", false, 2));
            result.add(new StringProperty("IMBFederation", "IMB federation", "Federation on the IMB hub", "OTS_RT", false, 3));
            return result;
        }
        catch (PropertyException exception)
        {
            exception.printStackTrace();
        }
        return null;
    }
}
