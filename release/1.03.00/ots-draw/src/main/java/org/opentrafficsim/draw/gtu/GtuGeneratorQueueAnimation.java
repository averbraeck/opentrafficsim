package org.opentrafficsim.draw.gtu;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.ImageObserver;
import java.rmi.RemoteException;
import java.util.Map;

import javax.naming.NamingException;

import org.opentrafficsim.draw.core.TextAlignment;
import org.opentrafficsim.draw.core.TextAnimation;
import org.opentrafficsim.road.gtu.generator.GtuGeneratorQueue;

import nl.tudelft.simulation.dsol.animation.Locatable;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface.TimeDoubleUnit;
import nl.tudelft.simulation.language.d3.DirectedPoint;

/**
 * Animator that displays generation queues as numbers.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 28 dec. 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class GtuGeneratorQueueAnimation extends TextAnimation
{

    /** */
    private static final long serialVersionUID = 20181018L;

    /** Default font. */
    private static final Font FONT = new Font("SansSerif", Font.PLAIN, 4);

    /**
     * Constructor.
     * @param source GTUGenerator; generator
     * @param simulator SimulatorInterface.TimeDoubleUnit; simulator
     * @throws NamingException when animation context cannot be created or retrieved
     * @throws RemoteException when remote context cannot be found
     */
    public GtuGeneratorQueueAnimation(final GtuGeneratorQueue source, final SimulatorInterface.TimeDoubleUnit simulator)
            throws NamingException, RemoteException
    {
        super(source, "", 0.0f, 0.0f, TextAlignment.CENTER, Color.BLACK, simulator, TextAnimation.RENDERWHEN1);
    }

    /** {@inheritDoc} */
    @Override
    public void paint(final Graphics2D graphics, final ImageObserver observer) throws RemoteException
    {
        graphics.setColor(Color.BLACK);
        graphics.setFont(FONT);
        DirectedPoint p = getSource().getLocation();
        Map<DirectedPoint, Integer> map = ((GtuGeneratorQueue) getSource()).getQueueLengths();
        for (DirectedPoint lanePosition : map.keySet())
        {
            setText(map.get(lanePosition).toString());
            setXY((float) (lanePosition.x - p.x), (float) (lanePosition.y - p.y));
            super.paint(graphics, observer);
        }
    }

    /** {@inheritDoc} */
    @Override
    public TextAnimation clone(final Locatable newSource, final TimeDoubleUnit newSimulator)
            throws RemoteException, NamingException
    {
        return new GtuGeneratorQueueAnimation((GtuGeneratorQueue) newSource, newSimulator);
    }

}
