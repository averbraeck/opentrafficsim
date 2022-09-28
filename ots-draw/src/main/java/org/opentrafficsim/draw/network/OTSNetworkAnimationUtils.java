package org.opentrafficsim.draw.network;

import java.rmi.RemoteException;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.naming.NamingException;

import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.core.network.OTSNetworkUtils;

import nl.tudelft.simulation.dsol.animation.Locatable;
import nl.tudelft.simulation.dsol.animation.D2.Renderable2DInterface;
import nl.tudelft.simulation.dsol.simulators.AnimatorInterface;
import nl.tudelft.simulation.naming.context.ContextInterface;
import nl.tudelft.simulation.naming.context.util.ContextUtil;

/**
 * OTSNetworkAnimationUtils can make a deep clone of a network, including animation, and can destroy the animation.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public final class OTSNetworkAnimationUtils
{
    /** */
    private OTSNetworkAnimationUtils()
    {
        // utility class
    }

    /**
     * Remove all objects and animation in the network.
     * @param network OTSNetwork; the network to destroy
     * @param simulator OTSSimulatorInterface; the simulator of the old network
     */
    @SuppressWarnings("checkstyle:designforextension")
    public static void destroy(final OTSNetwork network, final OTSSimulatorInterface simulator)
    {
        Set<Renderable2DInterface<?>> animationObjects = new LinkedHashSet<>();
        try
        {
            ContextInterface context =
                    ContextUtil.lookupOrCreateSubContext(simulator.getReplication().getContext(), "animation/2D");
            for (Object element : context.values())
            {
                Renderable2DInterface<?> animationObject = (Renderable2DInterface<?>) element;
                animationObjects.add(animationObject);
            }

            for (Renderable2DInterface<?> ao : animationObjects)
            {
                try
                {
                    ao.destroy(simulator);
                }
                catch (Exception e)
                {
                    //
                }
            }
        }
        catch (NamingException | RemoteException exception)
        {
            System.err.println("Error when destroying animation objects");
        }

        // destroy the network, GTUs, Routes, etc.
        OTSNetworkUtils.destroy(network);
    }

    /**
     * Remove all animation objects of the given class.
     * @param clazz Class&lt;?&gt;; the class to remove the animation objects for
     * @param oldSimulator OTSSimulatorInterface; the old simulator
     */
    @SuppressWarnings("checkstyle:designforextension")
    public static void removeAnimation(final Class<?> clazz, final OTSSimulatorInterface oldSimulator)
    {
        if (!(oldSimulator instanceof AnimatorInterface))
        {
            return;
        }

        try
        {
            ContextInterface context =
                    ContextUtil.lookupOrCreateSubContext(oldSimulator.getReplication().getContext(), "animation/2D");
            for (Object element : context.values())
            {
                Renderable2DInterface<?> animationObject = (Renderable2DInterface<?>) element;
                Locatable locatable = animationObject.getSource();
                if (clazz.isAssignableFrom(locatable.getClass()))
                {
                    animationObject.destroy(oldSimulator);
                }
            }
        }
        catch (NamingException | RemoteException exception)
        {
            System.err.println("Error when destroying animation objects for class " + clazz.getSimpleName());
        }
    }

}
