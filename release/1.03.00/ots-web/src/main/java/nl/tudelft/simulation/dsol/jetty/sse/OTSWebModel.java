package nl.tudelft.simulation.dsol.jetty.sse;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.web.animation.WebAnimationToggles;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.animation.Locatable;
import nl.tudelft.simulation.dsol.animation.D2.Renderable2DInterface;
import nl.tudelft.simulation.dsol.logger.SimLogger;
import nl.tudelft.simulation.dsol.simulators.AnimatorInterface;
import nl.tudelft.simulation.dsol.simulators.DEVSRealTimeClock;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.dsol.web.animation.D2.HTMLAnimationPanel;
import nl.tudelft.simulation.dsol.web.animation.D2.HTMLGridPanel;
import nl.tudelft.simulation.dsol.web.animation.D2.ToggleButtonInfo;
import nl.tudelft.simulation.event.Event;
import nl.tudelft.simulation.event.EventInterface;
import nl.tudelft.simulation.event.EventListenerInterface;
import nl.tudelft.simulation.introspection.Property;
import nl.tudelft.simulation.introspection.beans.BeanIntrospector;

/**
 * OTSWebModel.java. <br>
 * <br>
 * Copyright (c) 2003-2018 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class OTSWebModel implements EventListenerInterface
{
    /** the title for the model window. */
    private final String title;

    /** the simulator. */
    private final OTSSimulatorInterface simulator;

    /** dirty flag for the controls: when the model e.g. stops, the status needs to be changed. */
    private boolean dirtyControls = false;

    /** the animation panel. */
    private HTMLAnimationPanel animationPanel;

    /** Timer update interval in msec. */
    private long lastWallTIme = -1;

    /** Simulation time time. */
    private double prevSimTime = 0;
    
    /** has the model been killed? */
    private boolean killed = false;

    /**
     * @param title String; the title for the model window
     * @param simulator SimulatorInterface&lt;?,?,?&gt;; the simulator
     * @throws Exception in case jetty crashes
     */
    public OTSWebModel(final String title, final OTSSimulatorInterface simulator) throws Exception
    {
        this.title = title;
        this.simulator = simulator;
        Rectangle2D extent = new Rectangle2D.Double(-200, -200, 400, 400);
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
            WebAnimationToggles.setTextAnimationTogglesStandard(this.animationPanel);
            // get the already created elements in context(/animation/D2)
            this.animationPanel.notify(
                    new Event(SimulatorInterface.START_REPLICATION_EVENT, this.simulator, this.simulator.getSimulatorTime()));
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
    public final OTSSimulatorInterface getSimulator()
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
     * @return killed
     */
    public final boolean isKilled()
    {
        return this.killed;
    }

    /**
     * @param killed set killed
     */
    public final void setKilled(final boolean killed)
    {
        this.killed = killed;
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
    public void notify(final EventInterface event) throws RemoteException
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
     * Delegate handle method from the main web server for this particular model.
     * @param target t
     * @param baseRequest br
     * @param request r
     * @param response re
     * @throws IOException on error
     * @throws ServletException on error
     */
    @SuppressWarnings({"checkstyle:needbraces", "checkstyle:methodlength"})
    public void handle(final String target, final Request baseRequest, final HttpServletRequest request,
            final HttpServletResponse response) throws IOException, ServletException
    {
        // System.out.println("target=" + target);
        // System.out.println("baseRequest=" + baseRequest);
        // System.out.println("request=" + request);

        if (this.killed)
        {
            return;
        }
        
        Map<String, String[]> params = request.getParameterMap();
        // System.out.println(params);

        String answer = "<message>ok</message>";

        if (request.getParameter("message") != null)
        {
            String message = request.getParameter("message");
            String[] parts = message.split("\\|");
            String command = parts[0];
            HTMLAnimationPanel animationPanel = getAnimationPanel();

            switch (command)
            {
                case "getTitle":
                {
                    answer = "<title>" + getTitle() + "</title>";
                    break;
                }

                case "init":
                {
                    boolean simOk = getSimulator() != null;
                    boolean started = simOk ? getSimulator().isRunning() : false;
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
                        animationPanel.setSize(new Dimension(width, height));
                    }
                    break;
                }

                case "startStop":
                {
                    boolean simOk = getSimulator() != null;
                    boolean started = simOk ? getSimulator().isRunning() : false;
                    if (simOk && started)
                        started = !stopSimulator();
                    else if (simOk && !started)
                        started = startSimulator();
                    answer = controlButtonResponse(simOk, started);
                    break;
                }

                case "oneEvent":
                {
                    // TODO
                    boolean started = false;
                    answer = controlButtonResponse(getSimulator() != null, started);
                    break;
                }

                case "allEvents":
                {
                    // TODO
                    boolean started = false;
                    answer = controlButtonResponse(getSimulator() != null, started);
                    break;
                }

                case "reset":
                {
                    // TODO
                    boolean started = false;
                    answer = controlButtonResponse(getSimulator() != null, started);
                    break;
                }

                case "animate":
                {
                    answer = animationPanel.getDrawingCommands();
                    break;
                }

                case "arrowDown":
                {
                    animationPanel.pan(HTMLGridPanel.DOWN, 0.1);
                    break;
                }

                case "arrowUp":
                {
                    animationPanel.pan(HTMLGridPanel.UP, 0.1);
                    break;
                }

                case "arrowLeft":
                {
                    animationPanel.pan(HTMLGridPanel.LEFT, 0.1);
                    break;
                }

                case "arrowRight":
                {
                    animationPanel.pan(HTMLGridPanel.RIGHT, 0.1);
                    break;
                }

                case "pan":
                {
                    if (parts.length == 3)
                    {
                        int dx = Integer.parseInt(parts[1]);
                        int dy = Integer.parseInt(parts[2]);
                        double scale =
                                Renderable2DInterface.Util.getScale(animationPanel.getExtent(), animationPanel.getSize());
                        Rectangle2D.Double extent = (Rectangle2D.Double) animationPanel.getExtent();
                        extent.setRect((extent.getMinX() - dx * scale), (extent.getMinY() + dy * scale), extent.getWidth(),
                                extent.getHeight());
                    }
                    break;
                }

                case "introspect":
                {
                    if (parts.length == 3)
                    {
                        int x = Integer.parseInt(parts[1]);
                        int y = Integer.parseInt(parts[2]);
                        List<Locatable> targets = new ArrayList<Locatable>();
                        try
                        {
                            Point2D point = Renderable2DInterface.Util.getWorldCoordinates(new Point2D.Double(x, y),
                                    animationPanel.getExtent(), animationPanel.getSize());
                            for (Renderable2DInterface<?> renderable : animationPanel.getElements())
                            {
                                if (animationPanel.isShowElement(renderable)
                                        && renderable.contains(point, animationPanel.getExtent(), animationPanel.getSize()))
                                {
                                    if (renderable.getSource() instanceof GTU)
                                    {
                                        targets.add(renderable.getSource());
                                    }
                                }
                            }
                        }
                        catch (Exception exception)
                        {
                            SimLogger.always().warn(exception, "getSelectedObjects");
                        }
                        if (targets.size() > 0)
                        {
                            Object introspectedObject = targets.get(0);
                            Property[] properties = new BeanIntrospector().getProperties(introspectedObject);
                            SortedMap<String, Property> propertyMap = new TreeMap<>();
                            for (Property property : properties)
                                propertyMap.put(property.getName(), property);
                            answer = "<introspection>\n";
                            for (Property property : propertyMap.values())
                            {
                                answer += "<property><field>" + property.getName() + "</field><value>" + property.getValue()
                                        + "</value></property>\n";
                            }
                            answer += "<introspection>\n";
                        }
                        else
                        {
                            answer = "<none />";
                        }
                    }
                    break;
                }

                case "zoomIn":
                {
                    if (parts.length == 1)
                        animationPanel.zoom(0.9);
                    else
                    {
                        int x = Integer.parseInt(parts[1]);
                        int y = Integer.parseInt(parts[2]);
                        animationPanel.zoom(0.9, x, y);
                    }
                    break;
                }

                case "zoomOut":
                {
                    if (parts.length == 1)
                        animationPanel.zoom(1.1);
                    else
                    {
                        int x = Integer.parseInt(parts[1]);
                        int y = Integer.parseInt(parts[2]);
                        animationPanel.zoom(1.1, x, y);
                    }
                    break;
                }

                case "zoomAll":
                {
                    animationPanel.zoomAll();
                    break;
                }

                case "home":
                {
                    animationPanel.home();
                    break;
                }

                case "toggleGrid":
                {
                    animationPanel.setShowGrid(!animationPanel.isShowGrid());
                    break;
                }

                case "getTime":
                {
                    double now = Math.round(getSimulator().getSimulatorTime().si * 1000) / 1000d;
                    int seconds = (int) Math.floor(now);
                    int fractionalSeconds = (int) Math.floor(1000 * (now - seconds));
                    String timeText = String.format("  %02d:%02d:%02d.%03d  ", seconds / 3600, seconds / 60 % 60, seconds % 60,
                            fractionalSeconds);
                    answer = timeText;
                    break;
                }

                case "getSpeed":
                {
                    double simTime = getSimulator().getSimulatorTime().si;
                    double speed = getSimulationSpeed(simTime);
                    String speedText = "";
                    if (!Double.isNaN(speed))
                    {
                        speedText = String.format("% 5.2fx  ", speed);
                    }
                    answer = speedText;
                    break;
                }

                case "getToggles":
                {
                    answer = getToggles(animationPanel);
                    break;
                }

                // we expect something of the form toggle|class|Node|true or toggle|gis|streets|false
                case "toggle":
                {
                    if (parts.length != 4)
                        System.err.println("wrong toggle commmand: " + message);
                    else
                    {
                        String toggleName = parts[1];
                        boolean gis = parts[2].equals("gis");
                        boolean show = parts[3].equals("true");
                        if (gis)
                        {
                            if (show)
                                animationPanel.showGISLayer(toggleName);
                            else
                                animationPanel.hideGISLayer(toggleName);
                        }
                        else
                        {
                            if (show)
                                animationPanel.showClass(toggleName);
                            else
                                animationPanel.hideClass(toggleName);
                        }
                    }
                    break;
                }

                default:
                {
                    System.err.println("OTSWebModel: Got unknown message from client: " + command);
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
                setSpeedFactor(speedFactor);
                // System.out.println("speed factor changed to " + speedFactor);
            }
            catch (NumberFormatException exception)
            {
                answer = "<message>Error: " + exception.getMessage() + "</message>";
            }
        }

        // System.out.println(answer);

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
            return "<controls>\n" + "<oneEventActive>false</oneEventActive>\n" + "<allEventsActive>false</allEventsActive>\n"
                    + "<startStop>start</startStop>\n" + "<startStopActive>false</startStopActive>\n"
                    + "<resetActive>false</resetActive>\n" + "</controls>\n";
        }
        if (started)
        {
            return "<controls>\n" + "<oneEventActive>false</oneEventActive>\n" + "<allEventsActive>false</allEventsActive>\n"
                    + "<startStop>stop</startStop>\n" + "<startStopActive>true</startStopActive>\n"
                    + "<resetActive>false</resetActive>\n" + "</controls>\n";
        }
        else
        {
            return "<controls>\n" + "<oneEventActive>true</oneEventActive>\n" + "<allEventsActive>true</allEventsActive>\n"
                    + "<startStop>start</startStop>\n" + "<startStopActive>true</startStopActive>\n"
                    + "<resetActive>false</resetActive>\n" + "</controls>\n";
        }
    }

    /**
     * Return the toggle button info for the toggle panel.
     * @param panel the HTMLAnimationPanel
     * @return the String that can be parsed by the select.html iframe
     */
    private String getToggles(final HTMLAnimationPanel panel)
    {
        String ret = "<toggles>\n";
        for (ToggleButtonInfo toggle : panel.getToggleButtons())
        {
            if (toggle instanceof ToggleButtonInfo.Text)
            {
                ret += "<text>" + toggle.getName() + "</text>\n";
            }
            else if (toggle instanceof ToggleButtonInfo.LocatableClass)
            {
                ret += "<class>" + toggle.getName() + "," + toggle.isVisible() + "</class>\n";
            }
            else if (toggle instanceof ToggleButtonInfo.Gis)
            {
                ret += "<gis>" + toggle.getName() + "," + ((ToggleButtonInfo.Gis) toggle).getLayerName() + ","
                        + toggle.isVisible() + "</gis>\n";
            }
        }
        ret += "</toggles>\n";
        return ret;
    }

    /**
     * Returns the simulation speed.
     * @param simTime double; simulation time
     * @return simulation speed
     */
    private double getSimulationSpeed(final double simTime)
    {
        long now = System.currentTimeMillis();
        if (this.lastWallTIme < 0 || this.lastWallTIme == now)
        {
            this.lastWallTIme = now;
            this.prevSimTime = simTime;
            return Double.NaN;
        }
        double speed = (simTime - this.prevSimTime) / (0.001 * (now - this.lastWallTIme));
        this.prevSimTime = simTime;
        this.lastWallTIme = now;
        return speed;
    }
}
