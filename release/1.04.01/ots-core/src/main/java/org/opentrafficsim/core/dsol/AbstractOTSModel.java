package org.opentrafficsim.core.dsol;

import nl.tudelft.simulation.dsol.model.AbstractDSOLModel;

/**
 * AbstractOTSModel is the base class for a model that runs on an OTSSimulator. <br>
 * <br>
 * Copyright (c) 2003-2020 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public abstract class AbstractOTSModel extends AbstractDSOLModel.TimeDoubleUnit<OTSSimulatorInterface>
        implements OTSModelInterface
{
    /** */
    private static final long serialVersionUID = 1L;

    /** a very short description of the simulation. */
    private String shortName;

    /** a description of the simulation (HTML formatted). */
    private String description;

    /**
     * Instantiate an abstract OTSModel.
     * @param simulator OTSSimulatorInterface; the simulator to use
     * @param shortName String; a very short description of the simulation
     * @param description String; a description of the simulation (HTML formatted)
     */
    public AbstractOTSModel(final OTSSimulatorInterface simulator, final String shortName, final String description)
    {
        super(simulator);
        this.shortName = shortName;
        this.description = description;
    }

    /**
     * Instantiate an abstract OTSModel. The name and description will be set as the class name.
     * @param simulator OTSSimulatorInterface; the simulator to use
     */
    public AbstractOTSModel(final OTSSimulatorInterface simulator)
    {
        super(simulator);
        this.shortName = getClass().getSimpleName();
        this.description = getClass().getSimpleName();
    }

    /** {@inheritDoc} */
    @Override
    public final String getShortName()
    {
        return this.shortName;
    }

    /**
     * @param shortName String; set shortName
     */
    public final void setShortName(final String shortName)
    {
        this.shortName = shortName;
    }

    /** {@inheritDoc} */
    @Override
    public final String getDescription()
    {
        return this.description;
    }

    /**
     * @param description String; set description
     */
    public final void setDescription(final String description)
    {
        this.description = description;
    }

}
