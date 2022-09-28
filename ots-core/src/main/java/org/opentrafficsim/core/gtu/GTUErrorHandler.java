package org.opentrafficsim.core.gtu;

/**
 * GTU error handler.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public interface GTUErrorHandler
{

    /** Default implementation that throws the exception. */
    GTUErrorHandler THROW = new GTUErrorHandler()
    {
        @Override
        public void handle(final GTU gtu, final Exception ex) throws Exception
        {
            throw ex;
        }
    };

    /** GTU error handler that deletes the GTU upon an exception. */
    GTUErrorHandler DELETE = new GTUErrorHandler()
    {
        @Override
        public void handle(final GTU gtu, final Exception ex) throws Exception
        {
            gtu.getSimulator().getLogger().always().info("Deleting GTU {} due to exception.", gtu.getId());
            gtu.destroy();
        }
    };

    /**
     * Handle exception.
     * @param gtu GTU; GTU
     * @param ex Exception; exception to handle
     * @throws Exception the exception may be thrown
     */
    void handle(GTU gtu, Exception ex) throws Exception;

}
