package org.opentrafficsim.demo.ntm;

import java.util.Calendar;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Abs;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Rel;

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
     * Start time of the simulation since Midnight, translated from Calendar to seconds since.
     */
    private Abs<TimeUnit> startTimeSinceMidnight;

    /** relative time length of simulation */
    private Rel<TimeUnit> durationOfSimulation;

    /** name and description of the project */
    private String descriptionProject;

    /** time step interval NTM */
    private DoubleScalar.Rel<TimeUnit> timeStepDurationNTM;

    /** cell transmission might have different time step interval?? */
    private DoubleScalar.Rel<TimeUnit> timeStepDurationCellTransmissionModel;

    /** time of a certain date */
    private DoubleScalar.Abs<TimeUnit> absoluteStartTime;

    /** generate new routes after a certain time interval */
    private DoubleScalar.Rel<TimeUnit> reRouteTimeInterval;
    
    /** number of routes for generation*/
    private int numberOfRoutes;

    /**
     * @param startTime
     * @param durationOfSimulation
     * @param descriptionProject
     * @param timeStepDurationNTM
     * @param timeStepDurationCTM
     */
    public NTMSettings(Calendar startTime, Rel<TimeUnit> durationOfSimulation, String descriptionProject,
            Rel<TimeUnit> timeStepDurationNTM, Rel<TimeUnit> timeStepDurationCTM, Rel<TimeUnit> reRouteTimeInterval, int numberOfRoutes)
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
        this.timeStepDurationNTM = timeStepDurationNTM;
        this.setTimeStepDurationCellTransmissionModel(timeStepDurationCTM);
        this.reRouteTimeInterval = reRouteTimeInterval;
        this.numberOfRoutes = numberOfRoutes;
    }

    /**
     * @param startTime
     * @param durationOfSimulation
     * @param descriptionProject
     * @param timeStepDuration
     */
    public NTMSettings(Rel<TimeUnit> timeStepDurationNTM, Rel<TimeUnit> timeStepDurationCTM)
    {
        this.timeStepDurationNTM = timeStepDurationNTM;
        this.setTimeStepDurationCellTransmissionModel(timeStepDurationCTM);
    }

    /**
     * @param startTime
     * @param durationOfSimulation
     * @param descriptionProject
     * @param timeStepDuration
     */
    public NTMSettings(Rel<TimeUnit> timeStepDurationNTM)
    {
        this.timeStepDurationNTM = timeStepDurationNTM;
    }

    /**
     * @return startTimeSinceMidnight
     */
    public Abs<TimeUnit> getStartTimeSinceMidnight()
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
    public Rel<TimeUnit> getDurationOfSimulation()
    {
        return this.durationOfSimulation;
    }

    /**
     * @param timeSpan set durationOfSimulation
     */
    public void setDurationOfSimulation(Rel<TimeUnit> timeSpan)
    {
        this.durationOfSimulation = timeSpan;
    }

    /**
     * @return timeStepDuration
     */
    public Rel<TimeUnit> getTimeStepDurationNTM()
    {
        return this.timeStepDurationNTM;
    }

    /**
     * @param timeStepDuration set timeStepDuration
     */
    public void setTimeStepDurationNTM(Rel<TimeUnit> timeStepDuration)
    {
        this.timeStepDurationNTM = timeStepDuration;
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
     * @return timeStepDurationCellTransmissionModel.
     */
    public DoubleScalar.Rel<TimeUnit> getTimeStepDurationCellTransmissionModel()
    {
        return timeStepDurationCellTransmissionModel;
    }

    /**
     * @param timeStepDurationCellTransmissionModel set timeStepDurationCellTransmissionModel.
     */
    public void setTimeStepDurationCellTransmissionModel(
            DoubleScalar.Rel<TimeUnit> timeStepDurationCellTransmissionModel)
    {
        this.timeStepDurationCellTransmissionModel = timeStepDurationCellTransmissionModel;
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

    /**
     * @return reRouteTimeInterval.
     */
    public DoubleScalar.Rel<TimeUnit> getReRouteTimeInterval()
    {
        return reRouteTimeInterval;
    }

    /**
     * @param reRouteTimeInterval set reRouteTimeInterval.
     */
    public void setReRouteTimeInterval(DoubleScalar.Rel<TimeUnit> reRouteTimeInterval)
    {
        this.reRouteTimeInterval = reRouteTimeInterval;
    }

    /**
     * @return numberOfRoutes.
     */
    public int getNumberOfRoutes()
    {
        return numberOfRoutes;
    }

    /**
     * @param numberOfRoutes set numberOfRoutes.
     */
    public void setNumberOfRoutes(int numberOfRoutes)
    {
        this.numberOfRoutes = numberOfRoutes;
    }

}
