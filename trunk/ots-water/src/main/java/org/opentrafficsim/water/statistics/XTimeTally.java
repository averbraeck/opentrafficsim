/**
 * 
 */
package org.opentrafficsim.water.statistics;

import com.thoughtworks.xstream.annotations.XStreamOmitField;

import nl.tudelft.simulation.dsol.simulators.DEVSSimulatorInterface;

/**
 * <br>
 * Copyright (c) 2011-2013 TU Delft, Faculty of TBM, Systems and Simulation <br>
 * This software is licensed without restrictions to Nederlandse Organisatie voor Toegepast Natuurwetenschappelijk Onderzoek TNO
 * (TNO), Erasmus University Rotterdam, Delft University of Technology, Panteia B.V., Stichting Projecten Binnenvaart, Ab Ovo
 * Nederland B.V., Modality Software Solutions B.V., and Rijkswaterstaat - Dienst Water, Verkeer en Leefomgeving, including the
 * right to sub-license sources and derived products to third parties. <br>
 * @version Mar 28, 2013 <br>
 * @author <a href="http://tudelft.nl/averbraeck">Alexander Verbraeck </a>
 */
public class XTimeTally extends XTally
{
    /** */
    @XStreamOmitField
    private static final long serialVersionUID = 1L;

    /** */
    @XStreamOmitField
    private DEVSSimulatorInterface.TimeDoubleUnit simulator;

    /**
     * @param description String; description of the statistic
     * @param simulator DEVSSimulatorInterface.TimeDoubleUnit; the simulator
     */
    public XTimeTally(String description, DEVSSimulatorInterface.TimeDoubleUnit simulator)
    {
        super(description);
        this.simulator = simulator;
    }

    /**
     * tally.
     * @param t double; previous time
     */
    public void tally(final double t)
    {
        super.tally(this.simulator.getSimulatorTime().si - t);
    }

}
