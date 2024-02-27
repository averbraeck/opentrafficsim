package org.opentrafficsim.core.gtu;

/**
 * GTU error handler.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public interface GtuErrorHandler
{

    /** Default implementation that throws the exception. */
    GtuErrorHandler THROW = new GtuErrorHandler()
    {
        @Override
        public void handle(final Gtu gtu, final Exception ex) throws Exception
        {
            throw ex;
        }
    };

    /** GTU error handler that deletes the GTU upon an exception. */
    GtuErrorHandler DELETE = new GtuErrorHandler()
    {
        @Override
        public void handle(final Gtu gtu, final Exception ex) throws Exception
        {
            gtu.getSimulator().getLogger().always().info("Deleting GTU {} due to exception.", gtu.getId());
            gtu.destroy();
        }
    };

    /**
     * Handle exception.
     * @param gtu Gtu; GTU
     * @param ex Exception; exception to handle
     * @throws Exception the exception may be thrown
     */
    void handle(Gtu gtu, Exception ex) throws Exception;

}
