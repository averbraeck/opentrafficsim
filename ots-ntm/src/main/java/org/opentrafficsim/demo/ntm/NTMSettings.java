package org.opentrafficsim.demo.ntm;

import java.util.Calendar;

import org.djunits.unit.DurationUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;

/**
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version 10 Sep 2014 <br>
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
    private Duration durationSinceMidnight;

    /** Relative time length of simulation */
    private Duration durationOfSimulation;

    /** Name and description of the project */
    private String descriptionProject;

    /** Time step interval NTM */
    private Duration timeStepDurationNTM;

    /** Cell transmission might have different time step interval?? */
    private Duration timeStepDurationCellTransmissionModel;

    /** Time of a certain date */
    private Time absoluteStartTime;

    /** Generate new routes after a certain time interval */
    private Duration reRouteTimeInterval;

    /** */
    private double scalingFactorDemand;

    /** */
    private boolean increaseDemandByArea;

    /** Number of routes for generation */
    private int numberOfRoutes;

    /** */
    private double weightNewRoutes;

    /** */
    private double varianceRoutes;

    /** */
    private boolean reRoute = false;

    /** */
    private String path;

    /**
     * @param startTime Calendar;
     * @param durationOfSimulation Duration;
     * @param descriptionProject String;
     * @param timeStepDurationNTM Duration;
     * @param timeStepDurationCTM Duration;
     * @param reRouteTimeInterval Duration;
     * @param numberOfRoutes int;
     * @param weightNewRoutes double;
     * @param varianceRoutes double;
     * @param reRoute boolean;
     * @param path String;
     * @param scalingFactorDemand double;
     */
    public NTMSettings(Calendar startTime, Duration durationOfSimulation, String descriptionProject,
            Duration timeStepDurationNTM, Duration timeStepDurationCTM, Duration reRouteTimeInterval, int numberOfRoutes,
            double weightNewRoutes, double varianceRoutes, boolean reRoute, String path, boolean increaseDemandByArea,
            double scalingFactorDemand)
    {
        this.setStartTime(new Time(startTime.getTimeInMillis(), TimeUnit.BASE_MILLISECOND));
        int hour = startTime.get(Calendar.HOUR_OF_DAY);
        int minutes = startTime.get(Calendar.MINUTE);
        int seconds = startTime.get(Calendar.SECOND);
        long duration = hour * 60 * 60 + minutes * 60 + seconds;
        // time = startTime.getTimeInMillis()/1000;

        this.durationSinceMidnight = new Duration(duration, DurationUnit.SECOND);
        this.durationOfSimulation = durationOfSimulation;
        this.setDescriptionProject(descriptionProject);
        this.timeStepDurationNTM = timeStepDurationNTM;
        this.setTimeStepDurationCellTransmissionModel(timeStepDurationCTM);
        this.reRouteTimeInterval = reRouteTimeInterval;
        this.numberOfRoutes = numberOfRoutes;
        this.weightNewRoutes = weightNewRoutes;
        this.varianceRoutes = varianceRoutes;
        this.reRoute = reRoute;
        this.path = path;
        this.increaseDemandByArea = increaseDemandByArea;
        this.scalingFactorDemand = scalingFactorDemand;
    }

    /**
     * @param timeStepDurationNTM Duration;
     * @param timeStepDurationCTM Duration;
     */
    public NTMSettings(Duration timeStepDurationNTM, Duration timeStepDurationCTM)
    {
        this.timeStepDurationNTM = timeStepDurationNTM;
        this.setTimeStepDurationCellTransmissionModel(timeStepDurationCTM);
    }

    /**
     * @param timeStepDurationNTM Duration;
     */
    public NTMSettings(Duration timeStepDurationNTM)
    {
        this.timeStepDurationNTM = timeStepDurationNTM;
    }

    /**
     * @return startTimeSinceMidnight
     */
    public Duration getDurationSinceMidnight()
    {
        return this.durationSinceMidnight;
    }

    /**
     * @param startTimeSinceMidnight Duration; set startTimeSinceMidnight
     */
    public void setDurationSinceMidnight(Duration startTimeSinceMidnight)
    {
        this.durationSinceMidnight = startTimeSinceMidnight;
    }

    /**
     * @return durationOfSimulation
     */
    public Duration getDurationOfSimulation()
    {
        return this.durationOfSimulation;
    }

    /**
     * @param timeSpan Duration; set durationOfSimulation
     */
    public void setDurationOfSimulation(Duration timeSpan)
    {
        this.durationOfSimulation = timeSpan;
    }

    /**
     * @return timeStepDuration
     */
    public Duration getTimeStepDurationNTM()
    {
        return this.timeStepDurationNTM;
    }

    /**
     * @param timeStepDuration Duration; set timeStepDuration
     */
    public void setTimeStepDurationNTM(Duration timeStepDuration)
    {
        this.timeStepDurationNTM = timeStepDuration;
    }

    /**
     * @return startTime
     */
    public Time getStartTime()
    {
        return this.absoluteStartTime;
    }

    /**
     * @param startTime Time; set startTime
     */
    public void setStartTime(Time startTime)
    {
        this.absoluteStartTime = startTime;
    }

    /**
     * @return timeStepDurationCellTransmissionModel.
     */
    public Duration getTimeStepDurationCellTransmissionModel()
    {
        return this.timeStepDurationCellTransmissionModel;
    }

    /**
     * @param timeStepDurationCellTransmissionModel Duration; set timeStepDurationCellTransmissionModel.
     */
    public void setTimeStepDurationCellTransmissionModel(Duration timeStepDurationCellTransmissionModel)
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
     * @param descriptionProject String; set descriptionProject
     */
    public void setDescriptionProject(String descriptionProject)
    {
        this.descriptionProject = descriptionProject;
    }

    /**
     * @return reRouteTimeInterval.
     */
    public Duration getReRouteTimeInterval()
    {
        return this.reRouteTimeInterval;
    }

    /**
     * @param reRouteTimeInterval Duration; set reRouteTimeInterval.
     */
    public void setReRouteTimeInterval(Duration reRouteTimeInterval)
    {
        this.reRouteTimeInterval = reRouteTimeInterval;
    }

    /**
     * @return numberOfRoutes.
     */
    public int getNumberOfRoutes()
    {
        return this.numberOfRoutes;
    }

    /**
     * @param numberOfRoutes int; set numberOfRoutes.
     */
    public void setNumberOfRoutes(int numberOfRoutes)
    {
        this.numberOfRoutes = numberOfRoutes;
    }

    /**
     * @return path.
     */
    public String getPath()
    {
        return this.path;
    }

    /**
     * @param path String; set path.
     */
    public void setPath(String path)
    {
        this.path = path;
    }

    /**
     * @return weight_newRoutes.
     */
    public double getWeightNewRoutes()
    {
        return this.weightNewRoutes;
    }

    /**
     * @param weight_newRoutes set weight_newRoutes.
     */
    public void setWeightNewRoutes(double weightNewRoutes)
    {
        this.weightNewRoutes = weightNewRoutes;
    }

    /**
     * @return reRoute.
     */
    public boolean isReRoute()
    {
        return this.reRoute;
    }

    /**
     * @param reRoute boolean; set reRoute.
     */
    public void setReRoute(boolean reRoute)
    {
        this.reRoute = reRoute;
    }

    /**
     * @return varianceRoutes.
     */
    public double getVarianceRoutes()
    {
        return varianceRoutes;
    }

    /**
     * @param varianceRoutes double; set varianceRoutes.
     */
    public void setVarianceRoutes(double varianceRoutes)
    {
        this.varianceRoutes = varianceRoutes;
    }

    /**
     * @return scalingFactorDemand.
     */
    public double getScalingFactorDemand()
    {
        return scalingFactorDemand;
    }

    /**
     * @param scalingFactorDemand double; set scalingFactorDemand.
     */
    public void setScalingFactorDemand(double scalingFactorDemand)
    {
        this.scalingFactorDemand = scalingFactorDemand;
    }

    /**
     * @return increaseDemandByArea.
     */
    public boolean isIncreaseDemandByArea()
    {
        return increaseDemandByArea;
    }

    /**
     * @param increaseDemandByArea boolean; set increaseDemandByArea.
     */
    public void setIncreaseDemandByArea(boolean increaseDemandByArea)
    {
        this.increaseDemandByArea = increaseDemandByArea;
    }

}
