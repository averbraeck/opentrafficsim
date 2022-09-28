package org.opentrafficsim.draw.gtu;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.ImageObserver;
import java.rmi.RemoteException;
import java.util.Map;

import javax.naming.NamingException;

import org.djutils.draw.point.OrientedPoint3d;
import org.djutils.logger.CategoryLogger;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.DirectedPoint;
import org.opentrafficsim.draw.core.TextAlignment;
import org.opentrafficsim.draw.core.TextAnimation;
import org.opentrafficsim.road.gtu.generator.GtuGeneratorQueue;

import nl.tudelft.simulation.dsol.animation.Locatable;

/**
 * Animator that displays generation queues as numbers.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class GtuGeneratorQueueAnimation extends TextAnimation
{

    /** */
    private static final long serialVersionUID = 20181018L;

    /** Default font. */
    private static final Font FONT = new Font("SansSerif", Font.PLAIN, 4);

    /**
     * Constructor.
     * @param source GtuGenerator; generator
     * @param simulator OTSSimulatorInterface; simulator
     * @throws NamingException when animation context cannot be created or retrieved
     * @throws RemoteException when remote context cannot be found
     */
    public GtuGeneratorQueueAnimation(final GtuGeneratorQueue source, final OTSSimulatorInterface simulator)
            throws NamingException, RemoteException
    {
        super(source, "", 0.0f, 0.0f, TextAlignment.CENTER, Color.BLACK, simulator, TextAnimation.RENDERWHEN1);
    }

    /** {@inheritDoc} */
    @Override
    public void paint(final Graphics2D graphics, final ImageObserver observer)
    {
        try
        {
            graphics.setColor(Color.BLACK);
            graphics.setFont(FONT);
            OrientedPoint3d p = (OrientedPoint3d) getSource().getLocation();
            Map<DirectedPoint, Integer> map = ((GtuGeneratorQueue) getSource()).getQueueLengths();
            for (DirectedPoint lanePosition : map.keySet())
            {
                setText(map.get(lanePosition).toString());
                setXY((float) (lanePosition.x - p.x), (float) (lanePosition.y - p.y));
                super.paint(graphics, observer);
            }
        }
        catch (RemoteException exception)
        {
            CategoryLogger.always().warn(exception);
        }
    }

    /** {@inheritDoc} */
    @Override
    public TextAnimation clone(final Locatable newSource, final OTSSimulatorInterface newSimulator)
            throws RemoteException, NamingException
    {
        return new GtuGeneratorQueueAnimation((GtuGeneratorQueue) newSource, newSimulator);
    }

}
