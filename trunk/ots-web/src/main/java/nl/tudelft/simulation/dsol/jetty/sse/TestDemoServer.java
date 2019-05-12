package nl.tudelft.simulation.dsol.jetty.sse;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.djunits.unit.Unit;
import org.djunits.value.vdouble.scalar.AbstractDoubleScalar;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.djunits.value.vfloat.scalar.AbstractFloatScalar;
import org.djutils.io.URLResource;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.SessionIdManager;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.session.DefaultSessionCache;
import org.eclipse.jetty.server.session.DefaultSessionIdManager;
import org.eclipse.jetty.server.session.NullSessionDataStore;
import org.eclipse.jetty.server.session.SessionCache;
import org.eclipse.jetty.server.session.SessionDataStore;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.opentrafficsim.core.animation.gtu.colorer.DefaultSwitchableGTUColorer;
import org.opentrafficsim.core.dsol.OTSAnimator;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.draw.factory.DefaultAnimationFactory;
import org.opentrafficsim.web.test.CircularRoadModel;
import org.opentrafficsim.web.test.TJunctionModel;

import nl.tudelft.simulation.dsol.model.inputparameters.InputParameter;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterBoolean;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterDistContinuousSelection;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterDistDiscreteSelection;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterDouble;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterDoubleScalar;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterFloat;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterFloatScalar;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterInteger;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterLong;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterMap;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterSelectionList;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterSelectionMap;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterString;

/**
 * DSOLWebServer.java. <br>
 * <br>
 * Copyright (c) 2003-2019 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class TestDemoServer
{
    /** the map of sessionIds to OTSModelInterface that handles the animation and updates for the started model. */
    final Map<String, OTSModelInterface> sessionModelMap = new HashMap<>();

    /** the map of sessionIds to OTSWebModel that handles the animation and updates for the started model. */
    final Map<String, OTSWebModel> sessionWebModelMap = new HashMap<>();

    /**
     * Run a SuperDemo OTS Web server.
     * @param args String[]; not used
     * @throws Exception o Jetty error
     */
    public static void main(final String[] args) throws Exception
    {
        new TestDemoServer();
    }

    /**
     * @throws Exception in case jetty crashes
     */
    public TestDemoServer() throws Exception
    {
        new ServerThread().start();
    }

    /** Handle in separate thread to avoid 'lock' of the main application. */
    class ServerThread extends Thread
    {
        @Override
        public void run()
        {
            Server server = new Server(8080);
            ResourceHandler resourceHandler = new MyResourceHandler();

            // root folder; to work in Eclipse, as an external jar, and in an embedded jar
            URL homeFolder = URLResource.getResource("/home");
            String webRoot = homeFolder.toExternalForm();
            System.out.println("webRoot is " + webRoot);

            resourceHandler.setDirectoriesListed(true);
            resourceHandler.setWelcomeFiles(new String[] {"testdemo.html"});
            resourceHandler.setResourceBase(webRoot);

            SessionIdManager idManager = new DefaultSessionIdManager(server);
            server.setSessionIdManager(idManager);

            SessionHandler sessionHandler = new SessionHandler();
            SessionCache sessionCache = new DefaultSessionCache(sessionHandler);
            SessionDataStore sessionDataStore = new NullSessionDataStore();
            sessionCache.setSessionDataStore(sessionDataStore);
            sessionHandler.setSessionCache(sessionCache);

            HandlerList handlers = new HandlerList();
            handlers.setHandlers(new Handler[] {resourceHandler, sessionHandler, new XHRHandler(TestDemoServer.this)});
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

    /** */
    class MyResourceHandler extends ResourceHandler
    {

        /** {@inheritDoc} */
        @Override
        public Resource getResource(final String path)
        {
            System.out.println(path);
            return super.getResource(path);
        }

        /** {@inheritDoc} */
        @Override
        public void handle(final String target, final Request baseRequest, final HttpServletRequest request,
                final HttpServletResponse response) throws IOException, ServletException
        {
            /*-
            System.out.println("target      = " + target);
            System.out.println("baseRequest = " + baseRequest);
            System.out.println("request     = " + request);
            System.out.println("request.param " + request.getParameterMap());
            System.out.println();
             */

            if (target.startsWith("/parameters.html"))
            {
                String modelId = request.getParameterMap().get("model")[0];
                String sessionId = request.getParameterMap().get("sessionId")[0];
                if (!TestDemoServer.this.sessionModelMap.containsKey(sessionId))
                {
                    System.out.println("parameters: " + modelId);
                    OTSAnimator simulator = new OTSAnimator();
                    simulator.setAnimation(false);
                    OTSModelInterface model = null;
                    if (modelId.toLowerCase().contains("circularroad"))
                        model = new CircularRoadModel(simulator);
                    else if (modelId.toLowerCase().contains("tjunction"))
                        model = new TJunctionModel(simulator);
                    if (model != null)
                        TestDemoServer.this.sessionModelMap.put(sessionId, model);
                    else
                        System.err.println("Could not find model " + modelId);
                }
            }

            if (target.startsWith("/model.html"))
            {
                String modelId = request.getParameterMap().get("model")[0];
                String sessionId = request.getParameterMap().get("sessionId")[0];
                if (TestDemoServer.this.sessionModelMap.containsKey(sessionId)
                        && !TestDemoServer.this.sessionWebModelMap.containsKey(sessionId))
                {
                    System.out.println("startModel: " + modelId);
                    OTSModelInterface model = TestDemoServer.this.sessionModelMap.get(sessionId);
                    OTSSimulatorInterface simulator = model.getSimulator();
                    try
                    {
                        simulator.initialize(Time.ZERO, Duration.ZERO, Duration.createSI(3600.0), model);
                        OTSWebModel webModel = new OTSWebModel(model.getShortName(), simulator);
                        TestDemoServer.this.sessionWebModelMap.put(sessionId, webModel);
                        DefaultAnimationFactory.animateNetwork(model.getNetwork(), simulator,
                                new DefaultSwitchableGTUColorer());
                    }
                    catch (Exception exception)
                    {
                        exception.printStackTrace();
                    }
                }
            }

            // handle whatever needs to be done...
            super.handle(target, baseRequest, request, response);
        }
    }

    /**
     * Answer handles the events from the web-based user interface for a demo. <br>
     * <br>
     * Copyright (c) 2003-2019 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved.
     * See for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>.
     * The source code and binary code of this software is proprietary information of Delft University of Technology.
     * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
     */
    public static class XHRHandler extends AbstractHandler
    {
        /** web server for callback of actions. */
        private final TestDemoServer webServer;

        /**
         * Create the handler for Servlet requests.
         * @param webServer DSOLWebServer; web server for callback of actions
         */
        public XHRHandler(final TestDemoServer webServer)
        {
            this.webServer = webServer;
        }

        /** {@inheritDoc} */
        @Override
        public void handle(final String target, final Request baseRequest, final HttpServletRequest request,
                final HttpServletResponse response) throws IOException, ServletException
        {
            if (request.getParameterMap().containsKey("sessionId"))
            {
                String sessionId = request.getParameterMap().get("sessionId")[0];
                if (this.webServer.sessionWebModelMap.containsKey(sessionId))
                {
                    this.webServer.sessionWebModelMap.get(sessionId).handle(target, baseRequest, request, response);
                }
                else if (this.webServer.sessionModelMap.containsKey(sessionId))
                {
                    OTSModelInterface model = this.webServer.sessionModelMap.get(sessionId);
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
                                answer = "<title>" + model.getShortName() + "</title>";
                                break;
                            }

                            case "getParameterMap":
                            {
                                answer = makeParameterMap(model);
                                break;
                            }

                            case "setParameters":
                            {
                                answer = setParameters(model, message);
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

                    response.setContentType("text/xml");
                    response.setHeader("Cache-Control", "no-cache");
                    response.setContentLength(answer.length());
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.getWriter().write(answer);
                    response.flushBuffer();
                    baseRequest.setHandled(true);
                }
            }
        }

        /**
         * Make the parameter set that can be interpreted by the parameters.html page.
         * @param model the model with parameters
         * @return an XML string with the parameters
         */
        private String makeParameterMap(OTSModelInterface model)
        {
            StringBuffer answer = new StringBuffer();
            answer.append("<parameters>\n");
            InputParameterMap inputParameterMap = model.getInputParameterMap();
            for (InputParameter<?, ?> tab : inputParameterMap.getSortedSet())
            {
                if (!(tab instanceof InputParameterMap))
                {
                    System.err.println("Input parameter " + tab.getShortName() + " cannot be displayed in a tab");
                }
                else
                {
                    answer.append("<tab>" + tab.getDescription() + "</tab>\n");
                    InputParameterMap tabbedMap = (InputParameterMap) tab;
                    for (InputParameter<?, ?> parameter : tabbedMap.getSortedSet())
                    {
                        addParameterField(answer, parameter);
                    }
                }
            }
            answer.append("</parameters>\n");
            return answer.toString();
        }

        /**
         * Add the right type of field for this parameter to the string buffer.
         * @param answer StringBuffer; the buffer to add the XML-info for the parameter
         * @param parameter InputParameter&lt;?,?&gt;; the input parameter to display
         */
        public void addParameterField(final StringBuffer answer, final InputParameter<?, ?> parameter)
        {
            if (parameter instanceof InputParameterDouble)
            {
                InputParameterDouble pd = (InputParameterDouble) parameter;
                answer.append("<double key='" + pd.getExtendedKey() + "' name='" + pd.getShortName() + "' description='"
                        + pd.getDescription() + "'>" + pd.getValue() + "</double>\n");
            }
            else if (parameter instanceof InputParameterFloat)
            {
                InputParameterFloat pf = (InputParameterFloat) parameter;
                answer.append("<float key='" + pf.getExtendedKey() + "' name='" + pf.getShortName() + "' description='"
                        + pf.getDescription() + "'>" + pf.getValue() + "</float>\n");
            }
            else if (parameter instanceof InputParameterBoolean)
            {
                InputParameterBoolean pb = (InputParameterBoolean) parameter;
                answer.append("<boolean key='" + pb.getExtendedKey() + "' name='" + pb.getShortName() + "' description='"
                        + pb.getDescription() + "'>" + pb.getValue() + "</boolean>\n");
            }
            else if (parameter instanceof InputParameterLong)
            {
                InputParameterLong pl = (InputParameterLong) parameter;
                answer.append("<long key='" + pl.getExtendedKey() + "' name='" + pl.getShortName() + "' description='"
                        + pl.getDescription() + "'>" + pl.getValue() + "</long>\n");
            }
            else if (parameter instanceof InputParameterInteger)
            {
                InputParameterInteger pi = (InputParameterInteger) parameter;
                answer.append("<integer key='" + pi.getExtendedKey() + "' name='" + pi.getShortName() + "' description='"
                        + pi.getDescription() + "'>" + pi.getValue() + "</integer>\n");
            }
            else if (parameter instanceof InputParameterString)
            {
                InputParameterString ps = (InputParameterString) parameter;
                answer.append("<string key='" + ps.getExtendedKey() + "' name='" + ps.getShortName() + "' description='"
                        + ps.getDescription() + "'>" + ps.getValue() + "</string>\n");
            }
            else if (parameter instanceof InputParameterDoubleScalar)
            {
                InputParameterDoubleScalar<?, ?> pds = (InputParameterDoubleScalar<?, ?>) parameter;
                String val = getValueInUnit(pds);
                List<String> units = getUnits(pds);
                answer.append("<doubleScalar key='" + pds.getExtendedKey() + "' name='" + pds.getShortName() + "' description='"
                        + pds.getDescription() + "'><value>" + val + "</value>\n");
                for (String unit : units)
                {
                    Unit<?> unitValue = pds.getUnitParameter().getOptions().get(unit);
                    if (unitValue.equals(pds.getUnitParameter().getValue()))
                        answer.append("<unit chosen='true'>" + unit + "</unit>\n");
                    else
                        answer.append("<unit chosen='false'>" + unit + "</unit>\n");
                }
                answer.append("</doubleScalar>\n");
            }
            else if (parameter instanceof InputParameterFloatScalar)
            {
                InputParameterFloatScalar<?, ?> pds = (InputParameterFloatScalar<?, ?>) parameter;
                String val = getValueInUnit(pds);
                List<String> units = getUnits(pds);
                answer.append("<floatScalar key='" + pds.getExtendedKey() + "' name='" + pds.getShortName() + "' description='"
                        + pds.getDescription() + "'><value>" + val + "</value>\n");
                for (String unit : units)
                {
                    Unit<?> unitValue = pds.getUnitParameter().getOptions().get(unit);
                    if (unitValue.equals(pds.getUnitParameter().getValue()))
                        answer.append("<unit chosen='true'>" + unit + "</unit>\n");
                    else
                        answer.append("<unit chosen='false'>" + unit + "</unit>\n");
                }
                answer.append("</floatScalar>\n");
            }
            else if (parameter instanceof InputParameterSelectionList<?>)
            {
                // TODO InputParameterSelectionList
            }
            else if (parameter instanceof InputParameterDistDiscreteSelection)
            {
                // TODO InputParameterSelectionList
            }
            else if (parameter instanceof InputParameterDistContinuousSelection)
            {
                // TODO InputParameterDistContinuousSelection
            }
            else if (parameter instanceof InputParameterSelectionMap<?, ?>)
            {
                // TODO InputParameterSelectionMap
            }
        }

        /**
         * @param parameter double scalar input parameter
         * @return default value in the unit
         */
        private <U extends Unit<U>,
                T extends AbstractDoubleScalar<U, T>> String getValueInUnit(InputParameterDoubleScalar<U, T> parameter)
        {
            return "" + parameter.getDefaultTypedValue().getInUnit(parameter.getDefaultTypedValue().getUnit());
        }

        /**
         * @param parameter double scalar input parameter
         * @return abbreviations for the units
         */
        private <U extends Unit<U>,
                T extends AbstractDoubleScalar<U, T>> List<String> getUnits(InputParameterDoubleScalar<U, T> parameter)
        {
            List<String> unitList = new ArrayList<>();
            for (String option : parameter.getUnitParameter().getOptions().keySet())
            {
                unitList.add(option.toString());
            }
            return unitList;
        }

        /**
         * @param parameter double scalar input parameter
         * @return default value in the unit
         */
        private <U extends Unit<U>,
                T extends AbstractFloatScalar<U, T>> String getValueInUnit(InputParameterFloatScalar<U, T> parameter)
        {
            return "" + parameter.getDefaultTypedValue().getInUnit(parameter.getDefaultTypedValue().getUnit());
        }

        /**
         * @param parameter double scalar input parameter
         * @return abbreviations for the units
         */
        private <U extends Unit<U>,
                T extends AbstractFloatScalar<U, T>> List<String> getUnits(InputParameterFloatScalar<U, T> parameter)
        {
            List<String> unitList = new ArrayList<>();
            for (String option : parameter.getUnitParameter().getOptions().keySet())
            {
                unitList.add(option.toString());
            }
            return unitList;
        }

        /**
         * Make the parameter set that can be interpreted by the parameters.html page.
         * @param model the model with parameters
         * @param message the key-value pairs of the set parameters
         * @return the errors if they are detected. If none, errors is set to "OK"
         */
        private String setParameters(OTSModelInterface model, String message)
        {
            String errors = "OK";
            InputParameterMap inputParameters = model.getInputParameterMap();
            String[] parts = message.split("\\|");
            Map<String, String> unitMap = new HashMap<>();
            for (int i = 1; i < parts.length - 3; i += 3)
            {
                String id = parts[i].trim().replaceFirst("model.", "");
                String type = parts[i + 1].trim();
                String val = parts[i + 2].trim();
                if (type.equals("UNIT"))
                {
                    unitMap.put(id, val);
                }
            }
            for (int i = 1; i < parts.length - 3; i += 3)
            {
                String id = parts[i].trim().replaceFirst("model.", "");
                String type = parts[i + 1].trim();
                String val = parts[i + 2].trim();

                try
                {
                    if (type.equals("DOUBLE"))
                    {
                        InputParameterDouble param = (InputParameterDouble) inputParameters.get(id);
                        param.setDoubleValue(Double.valueOf(val));
                    }
                    else if (type.equals("FLOAT"))
                    {
                        InputParameterFloat param = (InputParameterFloat) inputParameters.get(id);
                        param.setFloatValue(Float.valueOf(val));
                    }
                    else if (type.equals("BOOLEAN"))
                    {
                        InputParameterBoolean param = (InputParameterBoolean) inputParameters.get(id);
                        param.setBooleanValue(val.toUpperCase().startsWith("T"));
                    }
                    else if (type.equals("LONG"))
                    {
                        InputParameterLong param = (InputParameterLong) inputParameters.get(id);
                        param.setLongValue(Long.valueOf(val));
                    }
                    else if (type.equals("INTEGER"))
                    {
                        InputParameterInteger param = (InputParameterInteger) inputParameters.get(id);
                        param.setIntValue(Integer.valueOf(val));
                    }
                    else if (type.equals("STRING"))
                    {
                        InputParameterString param = (InputParameterString) inputParameters.get(id);
                        param.setStringValue(val);
                    }
                    if (type.equals("DOUBLESCALAR"))
                    {
                        InputParameterDoubleScalar<?, ?> param = (InputParameterDoubleScalar<?, ?>) inputParameters.get(id);
                        param.getDoubleParameter().setDoubleValue(Double.valueOf(val));
                        String unitString = unitMap.get(id);
                        if (unitString == null)
                            System.err.println("Could not find unit for Doublevalie parameter with id=" + id);
                        else
                        {
                            Unit<?> unit = param.getUnitParameter().getOptions().get(unitString);
                            if (unit == null)
                                System.err.println(
                                        "Could not find unit " + unitString + " for Doublevalie parameter with id=" + id);
                            else
                            {
                                param.getUnitParameter().setObjectValue(unit);
                                param.setCalculatedValue(); // it will retrieve the set double value and unit
                            }
                        }
                    }
                }
                catch (Exception exception)
                {
                    if (errors.equals("OK"))
                        errors = "ERRORS IN INPUT VALUES:\n";
                    errors += "Field " + id + ": " + exception.getMessage() + "\n";
                }
            }
            return errors;
        }

    }

}
