package org.opentrafficsim.remotecontrol;

import java.io.IOException;

import javax.naming.NamingException;

import org.djunits.value.ValueRuntimeException;
import org.djutils.cli.Checkable;
import org.djutils.cli.CliUtil;
import org.djutils.logger.CategoryLogger;
import org.djutils.logger.LogCategory;
import org.djutils.serialization.SerializationException;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.network.NetworkException;
import org.pmw.tinylog.Level;
import org.sim0mq.Sim0MQException;
import org.sim0mq.publisher.Sim0MQPublisher;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * Sim0MQ controlled OTS
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Apr 18, 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public final class Sim0MQControlledOTSNew
{
    /**
     * Do not instantiate.
     */
    private Sim0MQControlledOTSNew()
    {
        // Do not instantiate
    }
    
    /**
     * The command line options.
     */
    @Command(description = "Sim0MQ Remotely Controlled OTS", name = "Sim0MQOTS", mixinStandardHelpOptions = true,
            version = "1.0")
    public static class Options implements Checkable
    {
        /** The IP port. */
        @Option(names = { "-p", "--port" }, description = "Internet port to use", defaultValue = "8888")
        private int port;

        /**
         * Retrieve the port.
         * @return int; the port
         */
        public final int getPort()
        {
            return this.port;
        }

        @Override
        public final void check() throws Exception
        {
            if (this.port <= 0 || this.port > 65535)
            {
                throw new Exception("Port should be between 1 and 65535");
            }
        }
    }

    /**
     * Program entry point.
     * @param args String[]; the command line arguments
     * @throws OTSGeometryException on error
     * @throws NetworkException on error
     * @throws NamingException on error
     * @throws ValueRuntimeException on error
     * @throws SimRuntimeException on error
     * @throws ParameterException on error
     * @throws SerializationException on error
     * @throws Sim0MQException on error
     * @throws IOException on error
     */
    public static void main(final String[] args) throws NetworkException, OTSGeometryException, NamingException,
            ValueRuntimeException, ParameterException, SimRuntimeException, Sim0MQException, SerializationException, IOException
    {
        CategoryLogger.setAllLogLevel(Level.WARNING);
        CategoryLogger.setLogCategories(LogCategory.ALL);
        Options options = new Options();
        CliUtil.execute(options, args); // register Unit converters, parse the command line, etc..
        int port = options.getPort();
        System.out.println("Creating OTS server listening on port " + port + " on all interfaces");
        new Sim0MQPublisher(port); // Should not return until it receives a DIE command, or the connection is closed
        System.out.println("Sim0MQControlledOTSNew exits");
    }

}
