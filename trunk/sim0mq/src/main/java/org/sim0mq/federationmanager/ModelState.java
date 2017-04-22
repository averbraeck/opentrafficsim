package org.sim0mq.federationmanager;

/**
 * State of a model, to be used in a state transition model of model execution by a FederationManager.
 * <p>
 * Copyright (c) 2016-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://sim0mq.org/docs/current/license.html">Sim0MQ License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Apr 20, 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public enum ModelState
{
    /** Model has not yet been started. */
    NOT_STARTED,
    
    /** Model has been started, e.g. bij a FederateStarter. */
    STARTED,
    
    /** SimRunControl has been sent. */
    RUNCONTROL,
    
    /** Parameter(s) have been sent. */
    PARAMETERS,
    
    /** Simulator has been started. */
    SIMULATORSTARTED,
    
    /** Simulator has ended. */
    SIMULATORENDED,
    
    /** Statistics have been gathered. */
    STATISTICSGATHERED,
    
    /** Model terminated. */
    TERMINATED,
    
    /** Error occurred. Sequence should stop. */
    ERROR;
    
    /**
     * Return whether the model has not yet started.
     * @return boolean; whether the model has not yet started
     */
    public boolean isNotStarted()
    {
        return this.equals(NOT_STARTED);
    }

    /**
     * Return whether the model has passed the started phase.
     * @return boolean; whether the model has passed the started phase
     */
    public boolean isStarted()
    {
        return this.ordinal() >= STARTED.ordinal() && !this.equals(ERROR);
    }

    /**
     * Return whether the run control has been sent.
     * @return boolean; whether the run control has been sent
     */
    public boolean isRunControl()
    {
        return this.ordinal() >= RUNCONTROL.ordinal() && !this.equals(ERROR);
    }

    /**
     * Return whether the parameter(s) have been sent.
     * @return boolean; whether the parameter(s) have been sent
     */
    public boolean isParameters()
    {
        return this.ordinal() >= PARAMETERS.ordinal() && !this.equals(ERROR);
    }
    
    /**
     * Return whether the simulator has been started.
     * @return boolean; whether the simulator has been started
     */
    public boolean isSimulatorStarted()
    {
        return this.ordinal() >= SIMULATORSTARTED.ordinal() && !this.equals(ERROR);
    }
    
    /**
     * Return whether the simulator has ended.
     * @return boolean; whether the simulator has ended
     */
    public boolean isSimulatorEnded()
    {
        return this.ordinal() >= SIMULATORENDED.ordinal() && !this.equals(ERROR);
    }
    
    /**
     * Return whether the statistics have been gathered.
     * @return boolean; whether the statistics have been gathered
     */
    public boolean isStatisticsGathered()
    {
        return this.ordinal() >= STATISTICSGATHERED.ordinal() && !this.equals(ERROR);
    }
    
    /**
     * Return whether the model has been terminated.
     * @return boolean; whether the model has been terminated
     */
    public boolean isTerminated()
    {
        return this.ordinal() >= TERMINATED.ordinal() && !this.equals(ERROR);
    }
    
    /**
     * Return whether the federation is in the error state.
     * @return boolean; whether the federation is in the error state
     */
    public boolean isError()
    {
        return this.equals(ERROR);
    }

}

