package org.opentrafficsim.demo.ntm;

import java.util.Calendar;

import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.DoubleScalar;
import org.djunits.value.vdouble.scalar.DoubleScalar.Abs;
import org.djunits.value.vdouble.scalar.DoubleScalar.Rel;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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

    /** */
    private double scalingFactorDemand;

    /** */
    private boolean increaseDemandByArea;

    /** number of routes for generation */
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
     * @param startTime
     * @param durationOfSimulation
     * @param descriptionProject
     * @param timeStepDurationNTM
     * @param timeStepDurationCTM
     * @param reRouteTimeInterval
     * @param numberOfRoutes
     * @param weightNewRoutes
     * @param varianceRoutes
     * @param reRoute
     * @param path
     * @param scalingFactorDemand
     */
    public NTMSettings(Calendar startTime, Rel<TimeUnit> durationOfSimulation, String descriptionProject,
        Rel<TimeUnit> timeStepDurationNTM, Rel<TimeUnit> timeStepDurationCTM, Rel<TimeUnit> reRouteTimeInterval,
        int numberOfRoutes, double weightNewRoutes, double varianceRoutes, boolean reRoute, String path,
        boolean increaseDemandByArea, double scalingFactorDemand)
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
        this.weightNewRoutes = weightNewRoutes;
        this.varianceRoutes = varianceRoutes;
        this.reRoute = reRoute;
        this.path = path;
        this.increaseDemandByArea = increaseDemandByArea;
        this.scalingFactorDemand = scalingFactorDemand;
    }

    /**
     * @param timeStepDurationNTM
     * @param timeStepDurationCTM
     */
    public NTMSettings(Rel<TimeUnit> timeStepDurationNTM, Rel<TimeUnit> timeStepDurationCTM)
    {
        this.timeStepDurationNTM = timeStepDurationNTM;
        this.setTimeStepDurationCellTransmissionModel(timeStepDurationCTM);
    }

    /**
     * @param timeStepDurationNTM
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
        return this.timeStepDurationCellTransmissionModel;
    }

    /**
     * @param timeStepDurationCellTransmissionModel set timeStepDurationCellTransmissionModel.
     */
    public void setTimeStepDurationCellTransmissionModel(DoubleScalar.Rel<TimeUnit> timeStepDurationCellTransmissionModel)
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
        return this.reRouteTimeInterval;
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
        return this.numberOfRoutes;
    }

    /**
     * @param numberOfRoutes set numberOfRoutes.
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
     * @param path set path.
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
     * @param reRoute set reRoute.
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
     * @param varianceRoutes set varianceRoutes.
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
     * @param scalingFactorDemand set scalingFactorDemand.
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
     * @param increaseDemandByArea set increaseDemandByArea.
     */
    public void setIncreaseDemandByArea(boolean increaseDemandByArea)
    {
        this.increaseDemandByArea = increaseDemandByArea;
    }

}
