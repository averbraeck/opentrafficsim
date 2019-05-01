package nl.tudelft.simulation.dsol.jetty.sse;

import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.djutils.io.URLResource;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.logger.SimLogger;
import nl.tudelft.simulation.dsol.simulators.AnimatorInterface;
import nl.tudelft.simulation.dsol.simulators.DEVSRealTimeClock;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.dsol.web.animation.D2.HTMLAnimationPanel;
import nl.tudelft.simulation.dsol.web.animation.D2.HTMLGridPanel;
import nl.tudelft.simulation.event.Event;
import nl.tudelft.simulation.event.EventInterface;
import nl.tudelft.simulation.event.EventListenerInterface;

/**
 * DSOLWebServer.java. <br>
 * <br>
 * Copyright (c) 2003-2019 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public abstract class DSOLWebServer implements EventListenerInterface
{
    /** the title for the model window. */
    private final String title;

    /** the simulator. */
    private final SimulatorInterface<?, ?, ?> simulator;

    /** dirty flag for the controls: when the model e.g. stops, the status needs to be changed. */
    private boolean dirtyControls = false;

    /** the animation panel. */
    private HTMLAnimationPanel animationPanel;

    /**
     * @param title String; the title for the model window
     * @param simulator SimulatorInterface&lt;?,?,?&gt;; the simulator
     * @param extent Rectangle2D.Double; the extent to use for the graphics (min/max coordinates)
     * @throws Exception in case jetty crashes
     */
    public DSOLWebServer(final String title, final SimulatorInterface<?, ?, ?> simulator, final Rectangle2D.Double extent)
            throws Exception
    {
        this.title = title;

        this.simulator = simulator;
        try
        {
            simulator.addListener(this, SimulatorInterface.START_EVENT);
            simulator.addListener(this, SimulatorInterface.STOP_EVENT);
        }
        catch (RemoteException re)
        {
            SimLogger.always().warn(re, "Problem adding listeners to Simulator");
        }

        if (this.simulator instanceof AnimatorInterface)
        {
            this.animationPanel = new HTMLAnimationPanel(extent, new Dimension(800, 600), this.simulator);
            // get the already created elements in context(/animation/D2)
            this.animationPanel.notify(
                    new Event(SimulatorInterface.START_REPLICATION_EVENT, this.simulator, this.simulator.getSimulatorTime()));
        }

        new ServerThread().start();
    }

    /** Handle in separate thread to avoid 'lock' of the main application. */
    class ServerThread extends Thread
    {
        @Override
        public void run()
        {
            Server server = new Server(8080);
            ResourceHandler resourceHandler = new ResourceHandler();

            // root folder; to work in Eclipse, as an external jar, and in an embedded jar
            URL homeFolder = URLResource.getResource("/home");
            String webRoot = homeFolder.toExternalForm();
            System.out.println("webRoot is " + webRoot);

            resourceHandler.setDirectoriesListed(true);
            resourceHandler.setWelcomeFiles(new String[] {"index.html"});
            resourceHandler.setResourceBase(webRoot);

            HandlerList handlers = new HandlerList();
            handlers.setHandlers(new Handler[] {resourceHandler, new XHRHandler(DSOLWebServer.this)});
            server.setHandler(handlers);

            try
            {
                server.start();
                server.join();
            }
            catch (Exception exception)
            {
                exception.printStackTrace();
            }
        }
    }

    /**
     * @return title
     */
    public final String getTitle()
    {
        return this.title;
    }

    /**
     * @return simulator
     */
    public final SimulatorInterface<?, ?, ?> getSimulator()
    {
        return this.simulator;
    }

    /**
     * @return animationPanel
     */
    public final HTMLAnimationPanel getAnimationPanel()
    {
        return this.animationPanel;
    }

    /**
     * Try to start the simulator, and return whether the simulator has been started.
     * @return whether the simulator has been started or not
     */
    protected boolean startSimulator()
    {
        if (getSimulator() == null)
        {
            System.out.println("SIMULATOR == NULL");
            return false;
        }
        try
        {
            System.out.println("START THE SIMULATOR");
            getSimulator().start();
        }
        catch (SimRuntimeException exception)
        {
            SimLogger.always().warn(exception, "Problem starting Simulator");
        }
        if (getSimulator().isRunning())
        {
            return true;
        }
        this.dirtyControls = false; // undo the notification
        return false;
    }

    /**
     * Try to stop the simulator, and return whether the simulator has been stopped.
     * @return whether the simulator has been stopped or not
     */
    protected boolean stopSimulator()
    {
        if (getSimulator() == null)
        {
            return true;
        }
        try
        {
            System.out.println("STOP THE SIMULATOR");
            getSimulator().stop();
        }
        catch (SimRuntimeException exception)
        {
            SimLogger.always().warn(exception, "Problem stopping Simulator");
        }
        if (!getSimulator().isRunning())
        {
            return true;
        }
        this.dirtyControls = false; // undo the notification
        return false;
    }

    /**
     * @param speedFactor double; the new speed factor
     */
    protected void setSpeedFactor(final double speedFactor)
    {
        if (this.simulator instanceof DEVSRealTimeClock)
        {
            ((DEVSRealTimeClock<?, ?, ?>) this.simulator).setSpeedFactor(speedFactor);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void notify(EventInterface event) throws RemoteException
    {
        if (event.getType().equals(SimulatorInterface.START_EVENT))
        {
            this.dirtyControls = true;
        }
        else if (event.getType().equals(SimulatorInterface.STOP_EVENT))
        {
            this.dirtyControls = true;
        }
    }

    /**
     * Answer handles the events from the web-based user interface. <br>
     * <br>
     * Copyright (c) 2003-2019 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved.
     * See for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>.
     * The source code and binary code of this software is proprietary information of Delft University of Technology.
     * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
     */
    public static class XHRHandler extends AbstractHandler
    {
        /** web server for callback of actions. */
        final DSOLWebServer webServer;

        /**
         * Create the handler for Servlet requests.
         * @param webServer DSOLWebServer; web server for callback of actions
         */
        public XHRHandler(final DSOLWebServer webServer)
        {
            this.webServer = webServer;
        }

        @Override
        public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
                throws IOException, ServletException
        {
            // System.out.println("target=" + target);
            // System.out.println("baseRequest=" + baseRequest);
            // System.out.println("request=" + request);

            Map<String, String[]> params = request.getParameterMap();
            // System.out.println(params);

            String answer = "<message>ok</message>";

            if (request.getParameter("message") != null)
            {
                String message = request.getParameter("message");
                String[] parts = message.split("\\|");
                String command = parts[0];

                switch (command)
                {
                    case "getTitle":
                    {
                        answer = "<title>" + this.webServer.getTitle() + "</title>";
                        break;
                    }

                    case "init":
                    {
                        boolean simOk = this.webServer.getSimulator() != null;
                        boolean started = simOk ? this.webServer.getSimulator().isRunning() : false;
                        answer = controlButtonResponse(simOk, started);
                        break;
                    }

                    case "windowSize":
                    {
                        if (parts.length != 3)
                            System.err.println("wrong windowSize commmand: " + message);
                        else
                        {
                            int width = Integer.parseInt(parts[1]);
                            int height = Integer.parseInt(parts[2]);
                            this.webServer.getAnimationPanel().setSize(new Dimension(width, height));
                        }
                        break;
                    }

                    case "startStop":
                    {
                        boolean simOk = this.webServer.getSimulator() != null;
                        boolean started = simOk ? this.webServer.getSimulator().isRunning() : false;
                        if (simOk && started)
                            started = !this.webServer.stopSimulator();
                        else if (simOk && !started)
                            started = this.webServer.startSimulator();
                        answer = controlButtonResponse(simOk, started);
                        break;
                    }

                    case "oneEvent":
                    {
                        // TODO
                        boolean started = false;
                        answer = controlButtonResponse(this.webServer.getSimulator() != null, started);
                        break;
                    }

                    case "allEvents":
                    {
                        // TODO
                        boolean started = false;
                        answer = controlButtonResponse(this.webServer.getSimulator() != null, started);
                        break;
                    }

                    case "reset":
                    {
                        // TODO
                        boolean started = false;
                        answer = controlButtonResponse(this.webServer.getSimulator() != null, started);
                        break;
                    }

                    case "animate":
                    {
                        answer = this.webServer.getAnimationPanel().getDrawingCommands();
                        break;
                    }

                    case "arrowDown":
                    {
                        this.webServer.getAnimationPanel().pan(HTMLGridPanel.DOWN, 0.1);
                        break;
                    }

                    case "arrowUp":
                    {
                        this.webServer.getAnimationPanel().pan(HTMLGridPanel.UP, 0.1);
                        break;
                    }

                    case "arrowLeft":
                    {
                        this.webServer.getAnimationPanel().pan(HTMLGridPanel.LEFT, 0.1);
                        break;
                    }

                    case "arrowRight":
                    {
                        this.webServer.getAnimationPanel().pan(HTMLGridPanel.RIGHT, 0.1);
                        break;
                    }

                    case "zoomIn":
                    {
                        if (parts.length == 1)
                            this.webServer.getAnimationPanel().zoom(0.9);
                        else
                        {
                            int x = Integer.parseInt(parts[1]);
                            int y = Integer.parseInt(parts[2]);
                            this.webServer.getAnimationPanel().zoom(0.9, x, y);
                        }
                        break;
                    }

                    case "zoomOut":
                    {
                        if (parts.length == 1)
                            this.webServer.getAnimationPanel().zoom(1.1);
                        else
                        {
                            int x = Integer.parseInt(parts[1]);
                            int y = Integer.parseInt(parts[2]);
                            this.webServer.getAnimationPanel().zoom(1.1, x, y);
                        }
                        break;
                    }

                    default:
                    {
                        System.err.println("Got unknown message from client: " + command);
                        answer = "<message>" + request.getParameter("message") + "</message>";
                        break;
                    }
                }
            }

            if (request.getParameter("slider") != null)
            {
                // System.out.println(request.getParameter("slider") + "\n");
                try
                {
                    int value = Integer.parseInt(request.getParameter("slider"));
                    // values range from 100 to 1400. 100 = 0.1, 400 = 1, 1399 = infinite
                    double speedFactor = 1.0;
                    if (value > 1398)
                        speedFactor = Double.MAX_VALUE;
                    else
                        speedFactor = Math.pow(2.15444, value / 100.0) / 21.5444;
                    this.webServer.setSpeedFactor(speedFactor);
                    // System.out.println("speed factor changed to " + speedFactor);
                }
                catch (NumberFormatException exception)
                {
                    answer = "<message>Error: " + exception.getMessage() + "</message>";
                }
            }

            // System.out.println();

            response.setContentType("text/xml");
            response.setHeader("Cache-Control", "no-cache");
            response.setContentLength(answer.length());
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(answer);
            response.flushBuffer();
            baseRequest.setHandled(true);
        }

        /**
         * @param active boolean; is the simulation active?
         * @param started boolean; has the simulation been started?
         * @return XML message to send to the server
         */
        private String controlButtonResponse(final boolean active, final boolean started)
        {
            if (!active)
            {
                return "<controls>\n" + "<oneEventActive>false</oneEventActive>\n"
                        + "<allEventsActive>false</allEventsActive>\n" + "<startStop>start</startStop>\n"
                        + "<startStopActive>false</startStopActive>\n" + "<resetActive>false</resetActive>\n" + "</controls>\n";
            }
            if (started)
            {
                return "<controls>\n" + "<oneEventActive>false</oneEventActive>\n"
                        + "<allEventsActive>false</allEventsActive>\n" + "<startStop>stop</startStop>\n"
                        + "<startStopActive>true</startStopActive>\n" + "<resetActive>false</resetActive>\n" + "</controls>\n";
            }
            else
            {
                return "<controls>\n" + "<oneEventActive>true</oneEventActive>\n" + "<allEventsActive>true</allEventsActive>\n"
                        + "<startStop>start</startStop>\n" + "<startStopActive>true</startStopActive>\n"
                        + "<resetActive>false</resetActive>\n" + "</controls>\n";
            }
        }
    }

}
