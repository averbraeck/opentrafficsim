package nl.tudelft.simulation.dsol.jetty.sse;

import java.io.IOException;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

/**
 * DSOLWebServer.java. <br>
 * <br>
 * Copyright (c) 2003-2019 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class TestWebServer
{
    /**
     * Run a Test Web server
     * @param args String[]; not used
     * @throws Exception o Jetty error
     */
    public static void main(final String[] args) throws Exception
    {
        new TestWebServer();
    }

    /**
     * @throws Exception in case jetty crashes
     */
    public TestWebServer() throws Exception
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
            resourceHandler.setWelcomeFiles(new String[] {"test.html"});
            resourceHandler.setResourceBase(webRoot);

            SessionIdManager idManager = new DefaultSessionIdManager(server);
            server.setSessionIdManager(idManager);

            SessionHandler sessionHandler = new SessionHandler();
            SessionCache sessionCache = new DefaultSessionCache(sessionHandler);
            SessionDataStore sessionDataStore = new NullSessionDataStore();
            sessionCache.setSessionDataStore(sessionDataStore);
            sessionHandler.setSessionCache(sessionCache);

            HandlerList handlers = new HandlerList();
            handlers.setHandlers(new Handler[] {resourceHandler, sessionHandler, new XHRHandler(TestWebServer.this)});
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

    class MyResourceHandler extends ResourceHandler
    {

        /** {@inheritDoc} */
        @Override
        public Resource getResource(String path)
        {
            System.out.println(path);
            return super.getResource(path);
        }

        /** {@inheritDoc} */
        @Override
        public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
                throws IOException, ServletException
        {
            System.out.println("target      = " + target);
            System.out.println("baseRequest = " + baseRequest);
            System.out.println("request     = " + request);
            System.out.println("request.param " + request.getParameterMap());
            System.out.println();

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
        final TestWebServer webServer;

        /**
         * Create the handler for Servlet requests.
         * @param webServer DSOLWebServer; web server for callback of actions
         */
        public XHRHandler(final TestWebServer webServer)
        {
            this.webServer = webServer;
        }

        @Override
        public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
                throws IOException, ServletException
        {
            String answer = "OK";
            
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
