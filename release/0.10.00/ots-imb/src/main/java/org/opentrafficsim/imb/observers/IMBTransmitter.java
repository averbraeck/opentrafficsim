package org.opentrafficsim.imb.observers;

import java.rmi.RemoteException;

import org.opentrafficsim.core.gtu.GTU;

import nl.tudelft.simulation.event.EventInterface;
import nl.tudelft.simulation.event.EventListenerInterface;
import nl.tudelft.simulation.language.d3.DirectedPoint;

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
public class IMBTransmitter implements EventListenerInterface
{
    /** Observer for gtu move events. */
    private Observer observer = null;

    /** */
    public IMBTransmitter()
    {
        try
        {
            this.observer = new IMBObserver("localhost" /* "app-usimb01.westeurope.cloudapp.azure.com" */
            /* "vps17642.public.cloudvps.com" */ /* "localhost" */, 4000, "GTUObserver", 1, "OTS_RT");
            System.out.println("Observer is " + this.observer);
        }
        catch (Exception exception1)
        {
            System.out.println("Observer creation failed; GTU movements will not be sent to observer");
        }
    }

    /** {@inheritDoc} */
    @Override
    public void notify(final EventInterface event) throws RemoteException
    {
        if (this.observer == null)
        {
            return;
        }

        if (event.getType().equals(GTU.MOVE_EVENT))
        {
            Object[] moveInfo = (Object[]) event.getContent();
            DirectedPoint location = (DirectedPoint) moveInfo[1];
            try
            {
                this.observer.postMessage("GTU", Observer.CHANGE,
                        new Object[] { moveInfo[0].toString(), location.x, location.y, location.z, location.getRotZ() });
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
                this.observer.postMessage("GTU", Observer.DELETE,
                        new Object[] { destroyInfo[0].toString(), location.x, location.y, location.z, location.getRotZ() });
            }
            catch (Exception exception)
            {
                exception.printStackTrace();
            }
        }
    }
}


