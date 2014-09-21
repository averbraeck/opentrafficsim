package org.opentrafficsim.demo.ntm;

import java.util.Calendar;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 10 Sep 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://Hansvanlint.weblog.tudelft.nl">Hans van Lint</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @author <a href="http://www.citg.tudelft.nl">Yufei Yuan</a>
 */
public class NTMSettings
{

    /**
     * Start time of the simulation since Midnight, translated from Calendar to seconds since
     */
    private DoubleScalar.Abs<TimeUnit> startTimeSinceMidnight;

    /** */
    private DoubleScalar.Abs<TimeUnit> durationOfSimulation;

    /** */
    private String descriptionProject;

    /** */
    private DoubleScalar.Abs<TimeUnit> timeStepLengthSimulation;

    /** */
    private DoubleScalar.Abs<TimeUnit> absoluteStartTime;

    /**
     * @param startTime
     * @param durationOfSimulation
     * @param descriptionProject
     * @param timeStepDuration
     */
    public NTMSettings(Calendar startTime, DoubleScalar.Abs<TimeUnit> durationOfSimulation, String descriptionProject,
            DoubleScalar.Abs<TimeUnit> timeStepDuration)
    {
        this.setStartTime(new DoubleScalar.Abs<TimeUnit>(startTime.getTimeInMillis(), TimeUnit.MILLISECOND));
        int hour = startTime.get(Calendar.HOUR_OF_DAY);
        int minutes = startTime.get(Calendar.MINUTE);
        int seconds = startTime.get(Calendar.SECOND);
        long time = hour * 60 * 60 + minutes * 60 + seconds;
        // time = startTime.getTimeInMillis()/1000;

        this.startTimeSinceMidnight = new DoubleScalar.Abs<TimeUnit>(time, TimeUnit.SECOND);
        this.durationOfSimulation = durationOfSimulation;
        this.setDescriptionProject(descriptionProject);
        this.timeStepLengthSimulation = timeStepDuration;
    }

    /**
     * @return startTimeSinceMidnight
     */
    public DoubleScalar.Abs<TimeUnit> getStartTimeSinceMidnight()
    {
        return this.startTimeSinceMidnight;
    }

    /**
     * @param startTimeSinceMidnight set startTimeSinceMidnight
     */
    public void setStartTimeSinceMidnight(DoubleScalar.Abs<TimeUnit> startTimeSinceMidnight)
    {
        this.startTimeSinceMidnight = startTimeSinceMidnight;
    }

    /**
     * @return durationOfSimulation
     */
    public DoubleScalar.Abs<TimeUnit> getDurationOfSimulation()
    {
        return this.durationOfSimulation;
    }

    /**
     * @param durationOfSimulation set durationOfSimulation
     */
    public void setDurationOfSimulation(DoubleScalar.Abs<TimeUnit> durationOfSimulation)
    {
        this.durationOfSimulation = durationOfSimulation;
    }

    /**
     * @return timeStepDuration
     */
    public DoubleScalar.Abs<TimeUnit> getTimeStepDuration()
    {
        return this.timeStepLengthSimulation;
    }

    /**
     * @param timeStepDuration set timeStepDuration
     */
    public void setTimeStepDuration(DoubleScalar.Abs<TimeUnit> timeStepDuration)
    {
        this.timeStepLengthSimulation = timeStepDuration;
    }

    /**
     * @return startTime
     */
    public DoubleScalar.Abs<TimeUnit> getStartTime()
    {
        return this.absoluteStartTime;
    }

    /**
     * @param startTime set startTime
     */
    public void setStartTime(DoubleScalar.Abs<TimeUnit> startTime)
    {
        this.absoluteStartTime = startTime;
    }

    /**
     * @return descriptionProject
     */
    public String getDescriptionProject()
    {
        return this.descriptionProject;
    }

    /**
     * @param descriptionProject set descriptionProject
     */
    public void setDescriptionProject(String descriptionProject)
    {
        this.descriptionProject = descriptionProject;
    }

}
