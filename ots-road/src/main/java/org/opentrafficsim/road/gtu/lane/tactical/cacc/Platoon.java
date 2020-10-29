package org.opentrafficsim.road.gtu.lane.tactical.cacc;

import java.util.ArrayList;
import java.util.List;

import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.network.LateralDirectionality;

import nl.tudelft.simulation.dsol.SimRuntimeException;

/**
 * Platoon meta data.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 22 okt. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class Platoon
{

    /** List of GTUs. */
    private final List<String> gtuList = new ArrayList<>();

    /** Lane change direction. */
    private LateralDirectionality laneChangeDir = LateralDirectionality.NONE;

    /** List of changed GTUs. */
    private List<String> changedGtus = new ArrayList<>();

    /**
     * Adds a GTU to the platoon. This is done by a generator listener.
     * @param id String; GTU id
     */
    public void addGtu(final String id)
    {
        this.gtuList.add(id);
    }

    /**
     * Returns the size of the platoon.
     * @return int; size of the platoon
     */
    public int size()
    {
        return this.gtuList.size();
    }

    /**
     * Returns the GTU id of the index
     * @param index int; index position of gtuList
     * @return String; id of gtu on index position; NULL is none?
     */
    public String getId(int index)
    {
        return this.gtuList.get(index);
    }

    /**
     * Add a lane change as it's finished. This will reset the lane change when all GTUs in the platoon changed lane.
     * @param gtu GTU; gtu
     */
    public void addLaneChange(final GTU gtu)
    {
        this.changedGtus.add(gtu.getId());
        if (this.changedGtus.size() == size())
        {
            // all GTUs in platoon changed
            try
            {
                gtu.getSimulator().scheduleEventNow(this, this, "endLaneChangeProcess", null);
            }
            catch (SimRuntimeException exception)
            {
                throw new RuntimeException(exception);
            }
        }
    }

    /**
     * End lane change process.
     */
    @SuppressWarnings("unused")
    private void endLaneChangeProcess()
    {
        this.laneChangeDir = LateralDirectionality.NONE;
        this.changedGtus.clear();
    }

    /**
     * Returns whether the platoon is ready for a lane change (i.e. not in the process of a lane change).
     * @return boolean; whether the platoon is ready for a lane change (i.e. not in the process of a lane change)
     */
    public boolean canInitiateLaneChangeProcess()
    {
        return this.laneChangeDir.isNone();
    }

    /**
     * Initiates a lane change.
     * @param laneChangeDirection LateralDirectionality; direction of the lane change
     */
    public void initiateLaneChange(final LateralDirectionality laneChangeDirection)
    {
        this.laneChangeDir = laneChangeDirection;
    }

    /**
     * Returns the index of the GTU in the platoon, with the leader {@code 0}.
     * @param gtuId String; GTU id
     * @return int; index of the GTU in the platoon, with the leader {@code 0}
     */
    public int getIndex(final String gtuId)
    {
        return this.gtuList.indexOf(gtuId);
    }

    /**
     * Returns whether the GTU is the platoon.
     * @param gtuId String; GTU id
     * @return boolean; whether the GTU is the platoon
     */
    public boolean isInPlatoon(final String gtuId)
    {
        return this.gtuList.contains(gtuId);
    }

    /**
     * Returns the direction in which the GTU should change lane.
     * @param gtuId String; GTU id
     * @return LateralDirectionality; direction to change to, or {@code NONE}
     */
    public LateralDirectionality shouldChangeLane(final String gtuId)
    {
        if (this.changedGtus.isEmpty()) // No trucks have changed yet
        {
            return size() == (getIndex(gtuId) + 1) ? this.laneChangeDir : LateralDirectionality.NONE;
        }
        else if (this.changedGtus.contains(gtuId))
        {
            return LateralDirectionality.NONE;
        }
        else
        {
            return this.laneChangeDir;
        }

    }

    /**
     * Returns true when a lane change is in progress.
     * @return boolean; whether a lane change is currently in progress
     */
    public boolean laneChangeInProgress()
    {
        return !this.changedGtus.isEmpty();
    }

    /**
     * Number of gtus that have changed lanes.
     * @return int; number of gtus changed lanes currently
     */
    public int numberOfChanged()
    {
        return this.changedGtus.size();
    }

}
