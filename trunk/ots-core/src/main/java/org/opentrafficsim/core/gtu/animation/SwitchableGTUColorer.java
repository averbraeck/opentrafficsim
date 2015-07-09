package org.opentrafficsim.core.gtu.animation;

import java.awt.Color;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.opentrafficsim.core.gtu.GTU;

/**
 * GTU colorer that uses a coloring method that can be switched by the user of the program.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 28 mei 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class SwitchableGTUColorer implements GTUColorer
{
    /** The currently active GTUColorer. */
    private GTUColorer activeColorer;

    /** list of included colorers. */
    private List<GTUColorer> colorers = new ArrayList<>();

    /**
     * Construct a new Switchable GTUColorer based on a list of colorers.
     * @param activeIndex the index of the initially active colorer in the list (0-based).
     * @param colorers GTUColorers; the list of GTUColorer. List cannot be empty.
     * @throws IndexOutOfBoundsException when activeIndex &lt; 0 or larger than or equal to the number of colorers.
     */
    @SuppressWarnings("checkstyle:redundantthrows")
    public SwitchableGTUColorer(final int activeIndex, final GTUColorer... colorers) throws IndexOutOfBoundsException
    {
        this.colorers.addAll(Arrays.asList(colorers));
        setGTUColorer(activeIndex);
    }

    /**
     * Replace the currently active GTUColorer.
     * @param activeIndex the index of the new active colorer in the list (0-based).
     * @throws IndexOutOfBoundsException when activeIndex &lt; 0 or larger than or equal to the number of colorers.
     */
    @SuppressWarnings("checkstyle:redundantthrows")
    public final void setGTUColorer(final int activeIndex) throws IndexOutOfBoundsException
    {
        this.activeColorer = this.colorers.get(activeIndex);
    }

    /** {@inheritDoc} */
    @Override
    public final Color getColor(final GTU<?> gtu) throws RemoteException
    {
        return this.activeColorer.getColor(gtu);
    }

    /** {@inheritDoc} */
    @Override
    public final List<LegendEntry> getLegend()
    {
        return this.activeColorer.getLegend();
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "Switchable GTU Colorer";
    }

    /**
     * @return the list of colorers.
     */
    public final List<GTUColorer> getColorers()
    {
        return this.colorers;
    }

}
