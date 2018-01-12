package org.opentrafficsim.road.gtu.generator;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.ImageObserver;
import java.rmi.RemoteException;
import java.util.Map;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.animation.D2.Renderable2D;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.language.d3.DirectedPoint;

/**
 * Animator that displays generation queues as numbers.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 28 dec. 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class GTUGeneratorAnimation extends Renderable2D<GTUGenerator>
{
    
    /** Default font. */
    private static final Font FONT = new Font("SansSerif", Font.PLAIN, 4);

    /**
     * Constructor.
     * @param source GTUGenerator; generator
     * @param simulator SimulatorInterface&lt;?, ?, ?&gt;; simulator
     * @throws NamingException when animation context cannot be created or retrieved
     * @throws RemoteException when remote context cannot be found
     */
    public GTUGeneratorAnimation(final GTUGenerator source, final SimulatorInterface<?, ?, ?> simulator)
            throws NamingException, RemoteException
    {
        super(source, simulator);
    }

    /** {@inheritDoc} */
    @Override
    public void paint(final Graphics2D graphics, final ImageObserver observer) throws RemoteException
    {
        graphics.setColor(Color.BLACK);
        graphics.setFont(FONT);
        DirectedPoint p = getSource().getLocation();
        Map<DirectedPoint, Integer> map = getSource().getQueueLengths();
        for (DirectedPoint lanePosition : map.keySet())
        {
            graphics.drawString(map.get(lanePosition) + "", (int) (lanePosition.x - p.x), (int) (-lanePosition.y + p.y));
        }
    }

}
