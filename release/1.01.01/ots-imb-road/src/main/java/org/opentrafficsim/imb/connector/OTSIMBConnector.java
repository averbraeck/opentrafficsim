package org.opentrafficsim.imb.connector;

import org.djutils.exceptions.Throw;
import org.opentrafficsim.imb.IMBException;

import nl.tno.imb.TConnection;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameter;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterException;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterInteger;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterMap;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterString;

/**
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
     * @param modelId int; usually 1
     * @param federation String; usually "OTS_RT"
     * @throws IMBException when a connection to the IMB hub could not be established
     */
    public OTSIMBConnector(final String host, final int port, final String modelName, final int modelId,
            final String federation) throws IMBException
    {
        super(host, port, modelName, modelId, federation);
    }

    /**
     * @param connection TConnection; the IMB connection
     * @throws IMBException in case of connection problems
     */
    public OTSIMBConnector(TConnection connection) throws IMBException
    {
        super(connection);
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
     * Construct a new OTSIMBConnector from a InputParameterMap (preferably constructed with the
     * <cite>standardIMBProperties</cite> method of this class.
     * @param inputParameterMap InputParameterMap; the compound property with the settings
     * @param modelName String; the name used to register on the hub host
     * @return OTSIMBConnector; a new OTSIMBConnector expecting the IMB hub on localhost port 4000.
     * @throws IMBException when a connection to the IMB hub could not be established
     */
    public static OTSIMBConnector create(final InputParameterMap inputParameterMap, final String modelName) throws IMBException
    {
        String host = null;
        int port = -1;
        int modelId = 1;
        String federation = "OTS_RT";

        for (InputParameter<?, ?> ap : inputParameterMap.getValue().values())
        {
            switch (ap.getKey())
            {
                case "IMBHost":
                    host = ap.getValue().toString();
                    break;

                case "IMBPort":
                    port = (Integer) ap.getValue();
                    break;

                case "IMBModelId":
                    modelId = (Integer) ap.getValue();
                    break;

                case "IMBFederation":
                    federation = ap.getValue().toString();
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
     * Create a InputParameterMap with the settings for an IMB connection.
     * @param displayPriority int; the displayPriority of the created InputParameterMap
     * @return InputParameterMap the default settings
     */
    public static InputParameterMap standardIMBProperties(final int displayPriority)
    {
        try
        {
            InputParameterMap result =
                    new InputParameterMap("IMBProperties", "IMB properties", "IMB properties", displayPriority);
            result.add(new InputParameterString("IMBHost", "IMB hub host", "Name of the IMB hub", "localhost", 0));
            result.add(new InputParameterInteger("IMBPort", "IMB hub port", "Port on the IMB hub", 4000, 0, 65535, "%d", 1));
            result.add(new InputParameterInteger("IMBModelId", "IMB model id", "Model id", 1, 0, 9999, "%d", 2));
            result.add(new InputParameterString("IMBFederation", "IMB federation", "Federation on the IMB hub", "OTS_RT", 3));
            return result;
        }
        catch (InputParameterException exception)
        {
            exception.printStackTrace();
        }
        return null;
    }

    /**
     * Create a InputParameterMap with the settings for an IMB connection.
     * @param displayPriority int; the displayPriority of the created InputParameterMap
     * @param imbHost String; host that runs the IMB hub
     * @return InputParameterMap the default settings
     */
    public static InputParameterMap standardIMBProperties(final int displayPriority, final String imbHost)
    {
        try
        {
            InputParameterMap result =
                    new InputParameterMap("IMBProperties", "IMB properties", "IMB properties", displayPriority);
            result.add(new InputParameterString("IMBHost", "IMB hub host", "Name of the IMB hub", imbHost, 0));
            result.add(new InputParameterInteger("IMBPort", "IMB hub port", "Port on the IMB hub", 4000, 0, 65535, "%d", 1));
            result.add(new InputParameterInteger("IMBModelId", "IMB model id", "Model id", 1, 0, 9999, "%d", 2));
            result.add(new InputParameterString("IMBFederation", "IMB federation", "Federation on the IMB hub", "OTS_RT", 3));
            return result;
        }
        catch (InputParameterException exception)
        {
            exception.printStackTrace();
        }
        return null;
    }

}
