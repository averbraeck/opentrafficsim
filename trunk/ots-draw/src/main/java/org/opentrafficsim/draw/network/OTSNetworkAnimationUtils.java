package org.opentrafficsim.draw.network;

import java.rmi.RemoteException;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.naming.NamingException;

import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.OTSLink;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.core.network.OTSNetworkUtils;
import org.opentrafficsim.core.network.OTSNode;
import org.opentrafficsim.draw.core.ClonableRenderable2DInterface;

import nl.tudelft.simulation.dsol.animation.Locatable;
import nl.tudelft.simulation.dsol.animation.D2.Renderable2DInterface;
import nl.tudelft.simulation.dsol.simulators.AnimatorInterface;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.naming.context.ContextInterface;
import nl.tudelft.simulation.naming.context.util.ContextUtil;

/**
 * OTSNetworkAnimationUtils can make a deep clone of a network, including animation, and can destroy the animation. <br>
 * <br>
 * Copyright (c) 2003-2022 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public final class OTSNetworkAnimationUtils
{
    /** */
    private OTSNetworkAnimationUtils()
    {
        // utility class
    }

    /**
     * Clone the OTSNetwork, including animation.
     * @param network OTSNetwork; the network to clone
     * @param newId String; the new id of the network
     * @param oldSimulator SimulatorInterface.TimeDoubleUnit; the old simulator for this network
     * @param newSimulator OTSSimulatorInterface; the new simulator for this network
     * @return a clone of this network
     * @throws NetworkException in case the cloning fails
     */
    @SuppressWarnings("checkstyle:designforextension")
    public static OTSNetwork clone(final OTSNetwork network, final String newId,
            final SimulatorInterface.TimeDoubleUnit oldSimulator, final OTSSimulatorInterface newSimulator)
            throws NetworkException
    {
        OTSNetwork newNetwork = OTSNetworkUtils.clone(network, newId, newSimulator);

        // clone the link animation
        for (Link oldLink : network.getLinkMap().values())
        {
            OTSLink newLink = (OTSLink) newNetwork.getLink(oldLink.getId());
            cloneAnimation(oldLink, newLink, oldSimulator, newSimulator);
        }

        // clone the node animation
        for (Node oldNode : network.getNodeMap().values())
        {
            OTSNode newNode = (OTSNode) newNetwork.getNode(oldNode.getId());
            cloneAnimation(oldNode, newNode, oldSimulator, newSimulator);
        }

        // TODO clone the animation of the visible objects

        return newNetwork;
    }

    /**
     * Clone all animation objects for the given class. The given class is the <b>source</b> of the animation objects, as it is
     * not known on beforehand which objects need to be cloned. It is important for cloning that the animation objects implement
     * the CloneableRenderable2DInterface, so they can be cloned with their properties. If not, they will not be taken into
     * account for cloning by this method.
     * @param oldSource Locatable; the old source object that might have one or more animation objects attached to it
     * @param newSource T; the new source object to attach the cloned animation objects to
     * @param oldSimulator SimulatorInterface.TimeDoubleUnit; the old simulator when the old objects can be found
     * @param newSimulator SimulatorInterface.TimeDoubleUnit; the new simulator where the new simulation objects need to be
     *            registered
     * @param <T> locatable type
     */
    @SuppressWarnings("checkstyle:designforextension")
    public static <T extends Locatable> void cloneAnimation(final Locatable oldSource, final T newSource,
            final SimulatorInterface.TimeDoubleUnit oldSimulator, final SimulatorInterface.TimeDoubleUnit newSimulator)
    {
        if (!(oldSimulator instanceof AnimatorInterface) || !(newSimulator instanceof AnimatorInterface))
        {
            return;
        }

        try
        {
            ContextInterface context =
                    ContextUtil.lookupOrCreateSubContext(oldSimulator.getReplication().getContext(), "animation/2D");
            for (Object element : context.values())
            {
                @SuppressWarnings("unchecked")
                Renderable2DInterface<T> animationObject = (Renderable2DInterface<T>) element;
                T locatable = animationObject.getSource();
                if (oldSource.equals(locatable) && animationObject instanceof ClonableRenderable2DInterface)
                {
                    ((ClonableRenderable2DInterface<T>) animationObject).clone(newSource, newSimulator);
                }
            }
        }
        catch (NamingException | RemoteException exception)
        {
            System.err.println("Error when cloning animation objects for object " + oldSource);
        }
    }

    /**
     * Remove all objects and animation in the network.
     * @param network OTSNetwork; the network to destroy
     * @param simulator SimulatorInterface.TimeDoubleUnit; the simulator of the old network
     */
    @SuppressWarnings("checkstyle:designforextension")
    public static void destroy(final OTSNetwork network, final SimulatorInterface.TimeDoubleUnit simulator)
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
     * @param oldSimulator SimulatorInterface.TimeDoubleUnit; the old simulator
     */
    @SuppressWarnings("checkstyle:designforextension")
    public static void removeAnimation(final Class<?> clazz, final SimulatorInterface.TimeDoubleUnit oldSimulator)
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
