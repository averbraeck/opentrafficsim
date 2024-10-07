package org.opentrafficsim.road.gtu.lane;

import java.util.LinkedHashSet;
import java.util.Set;

import org.djutils.exceptions.Throw;
import org.opentrafficsim.core.network.Network;

/**
 * Checker that invokes multiple checkers.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class MultiLaneBasedMoveChecker extends AbstractLaneBasedMoveChecker
{

    /** Checkers. */
    private Set<AbstractLaneBasedMoveChecker> checkers = new LinkedHashSet<>();

    /**
     * Constructor.
     * @param network network
     */
    public MultiLaneBasedMoveChecker(final Network network)
    {
        super(network);
    }

    /**
     * Constructor.
     * @param network network
     * @param checkers checkers
     */
    public MultiLaneBasedMoveChecker(final Network network, final AbstractLaneBasedMoveChecker... checkers)
    {
        super(network);
        for (AbstractLaneBasedMoveChecker checker : checkers)
        {
            addChecker(checker);
        }
    }

    /**
     * Add checker.
     * @param checker checker to add
     */
    final void addChecker(final AbstractLaneBasedMoveChecker checker)
    {
        Throw.whenNull(checker, "Checker may not be null.");
        this.checkers.add(checker);
    }

    /** {@inheritDoc} */
    @Override
    public void checkMove(final LaneBasedGtu gtu) throws Exception
    {
        for (AbstractLaneBasedMoveChecker checker : this.checkers)
        {
            checker.checkMove(gtu);
        }
    }

}
