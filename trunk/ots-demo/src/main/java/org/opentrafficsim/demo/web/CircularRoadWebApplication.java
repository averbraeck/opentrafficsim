package org.opentrafficsim.demo.web;

import java.awt.geom.Rectangle2D;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.dsol.OTSAnimator;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.demo.CircularRoadModel;
import org.opentrafficsim.draw.core.OTSDrawingException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.jetty.test.sse.DSOLWebServer;
import nl.tudelft.simulation.dsol.swing.gui.inputparameters.TabbedParameterDialog;

/**
 * CircularRoadWebpplication.java. <br>
 * <br>
 * Copyright (c) 2003-2018 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class CircularRoadWebApplication extends DSOLWebServer
{
    /**
     * @param title String; the tile for the model
     * @param simulator OTSSimulatorInterface; the simulator
     * @throws Exception on jetty error
     */
    public CircularRoadWebApplication(final String title, final OTSSimulatorInterface simulator) throws Exception
    {
        super(title, simulator, new Rectangle2D.Double(-400, -400, 800, 800));
    }

    /**
     * @param args String[]; arguments, expected to be empty
     * @throws Exception on error
     */
    public static void main(final String[] args) throws Exception
    {
        try
        {
            OTSAnimator simulator = new OTSAnimator();
            final CircularRoadModel otsModel = new CircularRoadModel(simulator);
            if (TabbedParameterDialog.process(otsModel.getInputParameterMap()))
            {
                simulator.initialize(Time.ZERO, Duration.ZERO, Duration.createSI(3600.0), otsModel);
                new CircularRoadWebApplication("Circular Road", simulator);
            }
        }
        catch (SimRuntimeException | NamingException | RemoteException | OTSDrawingException exception)
        {
            exception.printStackTrace();
        }
    }
}

