package org.opentrafficsim.core.gtu;

/**
 * GTU error handler.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Aug 6, 2019 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
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
