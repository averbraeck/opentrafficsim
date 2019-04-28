package nl.tudelft.simulation.dsol.web.animation.peer;

import java.awt.peer.LabelPeer;

/**
 * HTMLContainer.java. <br>
 * <br>
 * Copyright (c) 2003-2019 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class HTMLLabel extends HTMLComponent implements LabelPeer
{

    /**
     * 
     */
    public HTMLLabel()
    {
        System.out.println("HTMLLabel.<init>");
    }

    /** {@inheritDoc} */
    @Override
    public void setText(String label)
    {
        System.out.println("HTMLLabel.setText()");
    }

    /** {@inheritDoc} */
    @Override
    public void setAlignment(int alignment)
    {
        System.out.println("HTMLLabel.setAlignment()");
    }

}
