package org.opentrafficsim.core.dsol;

import java.io.Serializable;

/**
 * AbstractOTSSimulationApplication is a class that can be extended by a simulation application that runs from the command line.
 * <br>
 * <br>
 * Copyright (c) 2003-2018 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public abstract class AbstractOTSSimulationApplication implements Serializable
{
    /** */
    private static final long serialVersionUID = 20141216L;

    /** the model. */
    private final OTSModelInterface model;

    /**
     * The application, which is model aware.
     * @param model OTSModelInterface; the model that will be shown in the JFrame
     */
    public AbstractOTSSimulationApplication(final OTSModelInterface model)
    {
        super();
        this.model = model;
    }

    /**
     * @return model
     */
    public final OTSModelInterface getModel()
    {
        return this.model;
    }
}
