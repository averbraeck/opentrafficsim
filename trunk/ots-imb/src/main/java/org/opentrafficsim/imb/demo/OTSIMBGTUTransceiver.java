package org.opentrafficsim.imb.demo;

import java.rmi.RemoteException;

import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.event.EventInterface;
import nl.tudelft.simulation.event.EventListenerInterface;
import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.imb.transceiver.Connector;
import org.opentrafficsim.imb.transceiver.IMBConnector;
import org.opentrafficsim.simulationengine.properties.AbstractProperty;
import org.opentrafficsim.simulationengine.properties.CompoundProperty;
import org.opentrafficsim.simulationengine.properties.IntegerProperty;
import org.opentrafficsim.simulationengine.properties.PropertyException;
import org.opentrafficsim.simulationengine.properties.StringProperty;

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
public class OTSIMBGTUTransceiver implements EventListenerInterface
{
    /** Observer for gtu move events. */
    private Connector connector = null;

    /** Key for compound property with IMB settings. */
    public static String PROPERTY_KEY = "IMBProperties";

    /**
     * Create a new IMBTransmitter expecting IMB hub on localhost port 4000.
     * @throws Exception when a connection to the IMB hub could not be established
     */
    public OTSIMBGTUTransceiver() throws Exception
    {
        this("localhost" /* "app-usimb01.westeurope.cloudapp.azure.com" */
        /* "vps17642.public.cloudvps.com" *//* "localhost" */, 4000, "GTUObserver", 1, "OTS_RT");
        // this.observer = new IMBObserver("localhost" /* "app-usimb01.westeurope.cloudapp.azure.com" */
        // /* "vps17642.public.cloudvps.com" */ /* "localhost" */, 4000, "GTUObserver", 1, "OTS_RT");
        // System.out.println("Observer is " + this.observer);
    }

    /**
     * Create a new IMBTransmitter.
     * @param hubHost String; the host that runs the IMB hub server
     * @param hubPort int; the port number on the IMB hub host
     * @param modelName String; the name used to register on the hub host
     * @param modelId integer; usually 1
     * @param federation string; usually "OTS_RT"
     * @throws Exception when a connection to the IMB hub could not be established
     */
    public OTSIMBGTUTransceiver(final String hubHost, final int hubPort, final String modelName, final int modelId,
            final String federation) throws Exception
    {
        this.connector = new IMBConnector(hubHost, hubPort, modelName, modelId, federation);
    }

    /**
     * Construct a new IMBTransmitter from a CompoundProperty (preferably constructed with the
     * <cite>standardIMBProperties</cite> method of this class.
     * @param compoundProperty CompoundProperty; the compound property with the settings
     * @throws Exception when a connection to the IMB hub could not be established
     */
    public OTSIMBGTUTransceiver(final CompoundProperty compoundProperty) throws Exception
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
        if (null == host)
        {
            return;
        }
        System.out.println("Connecting to " + host + ":" + port);
        this.connector = new IMBConnector(host, port, "GTUObserver", modelId, federation);
    }

    /**
     * Create a CompoundProperty with the settings for an IMB transmitter.
     * @param displayPriority int; the displayPriority of the created CompoundProperty
     * @return CompoundProperty
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

    /** {@inheritDoc} */
    @Override
    public void notify(final EventInterface event) throws RemoteException
    {
        if (this.connector == null)
        {
            return;
        }

        if (event.getType().equals(GTU.MOVE_EVENT))
        {
            Object[] moveInfo = (Object[]) event.getContent();
            DirectedPoint location = (DirectedPoint) moveInfo[1];
            try
            {
                this.connector.postIMBMessage("GTU", Connector.CHANGE, new Object[] { moveInfo[0].toString(), location.x,
                        location.y, location.z, location.getRotZ() });
            }
            catch (Exception exception)
            {
                exception.printStackTrace();
            }
        }
        else if (event.getType().equals(GTU.DESTROY_EVENT))
        {
            Object[] destroyInfo = (Object[]) event.getContent();
            DirectedPoint location = (DirectedPoint) destroyInfo[1];
            try
            {
                this.connector.postIMBMessage("GTU", Connector.DELETE, new Object[] { destroyInfo[0].toString(), location.x,
                        location.y, location.z, location.getRotZ() });
            }
            catch (Exception exception)
            {
                exception.printStackTrace();
            }
        }
        else if (event.getType().equals(SimulatorInterface.START_EVENT))
        {
            try
            {
                this.connector.postIMBMessage("SIM_Start", Connector.CHANGE, new Object[] {});
            }
            catch (Exception exception)
            {
                exception.printStackTrace();
            }
        }
        else if (event.getType().equals(SimulatorInterface.STOP_EVENT))
        {
            try
            {
                this.connector.postIMBMessage("SIM_Stop", Connector.CHANGE, new Object[] {});
            }
            catch (Exception exception)
            {
                exception.printStackTrace();
            }
        }
    }
}
