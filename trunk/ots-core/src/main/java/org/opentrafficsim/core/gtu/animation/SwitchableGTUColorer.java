package org.opentrafficsim.core.gtu.animation;

import java.awt.Color;
import java.rmi.RemoteException;
import java.util.List;

import org.opentrafficsim.core.gtu.GTU;

/**
 * GTU colorer that uses a coloring method that can be switched by the user of the program.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 28 mei 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class SwitchableGTUColorer implements GTUColorer
{
    /** The currently active GTUColorer. */
    private GTUColorer gtuColorer;
    
    /**
     * Construct a new Switchable GTUColorer.
     * @param initialColorer GTUColorer; the initially active GTUColorer.
     */
    public SwitchableGTUColorer(GTUColorer initialColorer)
    {
        this.gtuColorer = initialColorer;
    }
    
    /**
     * Replace the currently active GTUColorer.
     * @param newColorer GTUColorer; the GTUColorer that will replace the currently active GTUColorer
     */
    public void setGTUColorer(GTUColorer newColorer)
    {
        this.gtuColorer = newColorer;
    }

    /** {@inheritDoc} */
    @Override
    public Color getColor(GTU<?> gtu) throws RemoteException
    {
        return this.gtuColorer.getColor(gtu);
    }

    /** {@inheritDoc} */
    @Override
    public List<LegendEntry> getLegend()
    {
        return this.gtuColorer.getLegend();
    }

}
