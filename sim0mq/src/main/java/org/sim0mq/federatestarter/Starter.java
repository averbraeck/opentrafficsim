package org.sim0mq.federatestarter;

import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Mar 5, 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class Starter
{

    /**
     * 
     */
    public Starter()
    {
        //
    }

    /**
     * @param args args
     * @throws IOException on error
     */
    public static void main(String[] args) throws IOException
    {
        ProcessBuilder pb = new ProcessBuilder();
        pb.directory(new File("E:/MM1"));
        pb.command("java", "-jar","mm1.jar");
        pb.inheritIO();

        new Thread()
        {
            /** {@inheritDoc} */
            @Override
            public void run()
            {
                try
                {
                    Process process = pb.start();
                }
                catch (IOException exception)
                {
                    exception.printStackTrace();
                }
            }
        }.start();

        final boolean[] stop = new boolean[1];
        stop[0] = false;
        
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher()
        {
            @Override
            public boolean dispatchKeyEvent(KeyEvent ke)
            {
                switch (ke.getID())
                {
                    case KeyEvent.KEY_PRESSED:
                        if (ke.getKeyCode() == KeyEvent.VK_ESCAPE)
                        {
                            //
                        }
                        break;

                    case KeyEvent.KEY_RELEASED:
                        if (ke.getKeyCode() == KeyEvent.VK_ESCAPE)
                        {
                            stop[0] = true;
                        }
                        break;
                }
                return false;
            }
        });
        
        while (!stop[0])
        {
            try
            {
                Thread.sleep(10);
            }
            catch (InterruptedException exception)
            {
                stop[0] = true;
            }
        }
    }

}
