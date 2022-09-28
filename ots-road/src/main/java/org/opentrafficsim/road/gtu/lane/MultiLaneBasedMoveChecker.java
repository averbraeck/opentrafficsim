package org.opentrafficsim.road.gtu.lane;

import java.util.LinkedHashSet;
import java.util.Set;

import org.djutils.exceptions.Throw;
import org.opentrafficsim.core.network.OTSNetwork;

/**
 * Checker that invokes multiple checkers.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Aug 6, 2019 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class MultiLaneBasedMoveChecker extends AbstractLaneBasedMoveChecker
{

    /** Checkers. */
    private Set<AbstractLaneBasedMoveChecker> checkers = new LinkedHashSet<>();

    /**
     * Constructor.
     * @param network OTSNetwork; network
     */
    public MultiLaneBasedMoveChecker(final OTSNetwork network)
    {
        super(network);
    }

    /**
     * Constructor.
     * @param network OTSNetwork; network
     * @param checkers AbstractLaneBasedMoveChecker...; checkers
     */
    public MultiLaneBasedMoveChecker(final OTSNetwork network, final AbstractLaneBasedMoveChecker... checkers)
    {
        super(network);
        for (AbstractLaneBasedMoveChecker checker : checkers)
        {
            addChecker(checker);
        }
    }

    /**
     * Add checker.
     * @param checker AbstractLaneBasedMoveChecker; checker to add
     */
    final void addChecker(final AbstractLaneBasedMoveChecker checker)
    {
        Throw.whenNull(checker, "Checker may not be null.");
        this.checkers.add(checker);
    }

    /** {@inheritDoc} */
    @Override
    public void checkMove(final LaneBasedGTU gtu) throws Exception
    {
        for (AbstractLaneBasedMoveChecker checker : this.checkers)
        {
            checker.checkMove(gtu);
        }
    }

}
