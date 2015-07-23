package org.opentrafficsim.demo.ntm;

import org.opentrafficsim.core.unit.FrequencyUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author: pknoppers
 * $, initial version 23 Feb 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://Hansvanlint.weblog.tudelft.nl">Hans van Lint</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @author <a href="http://www.citg.tudelft.nl">Yufei Yuan</a>
 */
public class InputNTM
{

    /** debugging. */
    public boolean DEBUG = false;

    /** debugging. */
    public boolean WRITEDATA = false;

    /** debugging. */
    public boolean paint = false;

    /** use the bigger areas (true) or the detailed areas (false). */
    public boolean COMPRESS_AREAS = false;

    /** */
    private String inputMap = "";

    /** */
    private String outputMap = "";

    /** */
    private String variantNumber = "";

    /** */
    private double scalingFactorDemand = 1.0;

    /** */
    private boolean increaseDemandAreaByFactor = false;

    /** */
    private String fileCentroids;

    /** */
    private boolean isOnlyCentroidsFileCentroid;

    /** */

    private boolean returnCentroidsCentroid;

    /**
     * @return isOnlyCentroidsFileCentroid.
     */
    public boolean isOnlyCentroidsFileCentroid()
    {
        return this.isOnlyCentroidsFileCentroid;
    }

    /**
     * @param isOnlyCentroidsFileCentroid set isOnlyCentroidsFileCentroid.
     */
    public void setOnlyCentroidsFileCentroid(boolean isOnlyCentroidsFileCentroid)
    {
        this.isOnlyCentroidsFileCentroid = isOnlyCentroidsFileCentroid;
    }

    /**
     * @return returnCentroidsCentroid.
     */
    public boolean isReturnCentroidsCentroid()
    {
        return this.returnCentroidsCentroid;
    }

    /**
     * @param returnCentroidsCentroid set returnCentroidsCentroid.
     */
    public void setReturnCentroidsCentroid(boolean returnCentroidsCentroid)
    {
        this.returnCentroidsCentroid = returnCentroidsCentroid;
    }

    /**
     * @return isOnlyCentroidsFileNode.
     */
    public boolean isOnlyCentroidsFileNode()
    {
        return this.isOnlyCentroidsFileNode;
    }

    /**
     * @param isOnlyCentroidsFileNode set isOnlyCentroidsFileNode.
     */
    public void setOnlyCentroidsFileNode(boolean isOnlyCentroidsFileNode)
    {
        this.isOnlyCentroidsFileNode = isOnlyCentroidsFileNode;
    }

    /**
     * @return returnCentroidsNode.
     */
    public boolean isReturnCentroidsNode()
    {
        return this.returnCentroidsNode;
    }

    /**
     * @param returnCentroidsNode set returnCentroidsNode.
     */
    public void setReturnCentroidsNode(boolean returnCentroidsNode)
    {
        this.returnCentroidsNode = returnCentroidsNode;
    }

    /** */

    private String fileNodes;

    /** */
    private boolean isOnlyCentroidsFileNode;

    /** */

    private boolean returnCentroidsNode;

    /** */

    /** */
    private String fileLinks;

    /** */
    private String lengthUnitLink;

    /** */
    private String fileFeederLinks;

    /** */
    private String fileAreas;

    /** */
    private String fileAreasBig;

    /** */
    private String fileDemand;

    /** */
    private String fileCompressedDemand;

    /** */
    private String fileProfiles;

    /** */
    private String fileNameCapacityRestraint;

    /** */
    private String fileNameCapacityRestraintFactor;

    /** */
    private String fileNameParametersNTM;

    /** */
    private String fileNameCapacityRestraintBig;

    /** */
    private String fileNameCapacityRestraintFactorBig;

    /** */
    private String fileNameParametersNTMBig;

    /** */
    private int numberOfRoutes = 1;

    /** */
    private double weightNewRoutes = 1.0;

    /** */
    private double varianceRoutes = 5.0f;

    /** */
    private boolean reRoute = false;

    /** */
    private DoubleScalar.Rel<TimeUnit> reRouteTimeInterval = null;

    /** */
    DoubleScalar<SpeedUnit> maxSpeed = null;

    /** */
    DoubleScalar<FrequencyUnit> maxCapacity = null;

    /** */
    Double linkCapacityNumberOfHours;

    /**
     * @return linkCapacityNumberOfHours.
     */
    public Double getLinkCapacityNumberOfHours()
    {
        return this.linkCapacityNumberOfHours;
    }

    /**
     * @param linkCapacityNumberOfHours set linkCapacityNumberOfHours.
     */
    public void setLinkCapacityNumberOfHours(Double linkCapacityNumberOfHours)
    {
        this.linkCapacityNumberOfHours = linkCapacityNumberOfHours;
    }

    /**
     * 
     */
    public InputNTM()
    {
        super();
    }

    /**
     * @return dEBUG.
     */
    public boolean isDEBUG()
    {
        return this.DEBUG;
    }

    /**
     * @param dEBUG set dEBUG.
     */
    public void setDEBUG(boolean dEBUG)
    {
        this.DEBUG = dEBUG;
    }

    /**
     * @return wRITEDATA.
     */
    public boolean isWRITEDATA()
    {
        return this.WRITEDATA;
    }

    /**
     * @param wRITEDATA set wRITEDATA.
     */
    public void setWRITEDATA(boolean wRITEDATA)
    {
        this.WRITEDATA = wRITEDATA;
    }

    /**
     * @return paint.
     */
    public boolean isPaint()
    {
        return this.paint;
    }

    /**
     * @param paint set paint.
     */
    public void setPaint(boolean paint)
    {
        this.paint = paint;
    }

    /**
     * @return cOMPRESS_AREAS.
     */
    public boolean isCOMPRESS_AREAS()
    {
        return this.COMPRESS_AREAS;
    }

    /**
     * @param cOMPRESS_AREAS set cOMPRESS_AREAS.
     */
    public void setCOMPRESS_AREAS(boolean cOMPRESS_AREAS)
    {
        this.COMPRESS_AREAS = cOMPRESS_AREAS;
    }

    /**
     * @return inputMap.
     */
    public String getInputMap()
    {
        return this.inputMap;
    }

    /**
     * @param inputMap set inputMap.
     */
    public void setInputMap(String inputMap)
    {
        this.inputMap = inputMap;
    }

    /**
     * @return outputMap.
     */
    public String getOutputMap()
    {
        return this.outputMap;
    }

    /**
     * @param outputMap set outputMap.
     */
    public void setOutputMap(String outputMap)
    {
        this.outputMap = outputMap;
    }

    /**
     * @return variantNumber.
     */
    public String getVariantNumber()
    {
        return this.variantNumber;
    }

    /**
     * @param variantNumber set variantNumber.
     */
    public void setVariantNumber(String variantNumber)
    {
        this.variantNumber = variantNumber;
    }

    /**
     * @return scalingFactorDemand.
     */
    public double getScalingFactorDemand()
    {
        return this.scalingFactorDemand;
    }

    /**
     * @param scalingFactorDemand set scalingFactorDemand.
     */
    public void setScalingFactorDemand(double scalingFactorDemand)
    {
        this.scalingFactorDemand = scalingFactorDemand;
    }

    /**
     * @return fileCentroids.
     */
    public String getFileCentroids()
    {
        return this.fileCentroids;
    }

    /**
     * @param fileCentroids set fileCentroids.
     */
    public void setFileCentroids(String fileCentroids)
    {
        this.fileCentroids = fileCentroids;
    }

    /**
     * @return fileNodes.
     */
    public String getFileNodes()
    {
        return this.fileNodes;
    }

    /**
     * @param fileNodes set fileNodes.
     */
    public void setFileNodes(String fileNodes)
    {
        this.fileNodes = fileNodes;
    }

    /**
     * @return fileLinks.
     */
    public String getFileLinks()
    {
        return this.fileLinks;
    }

    /**
     * @param fileLinks set fileLinks.
     */
    public void setFileLinks(String fileLinks)
    {
        this.fileLinks = fileLinks;
    }

    /**
     * @return fileFeederLinks.
     */
    public String getFileFeederLinks()
    {
        return fileFeederLinks;
    }

    /**
     * @param fileFeederLinks set fileFeederLinks.
     */
    public void setFileFeederLinks(String fileFeederLinks)
    {
        this.fileFeederLinks = fileFeederLinks;
    }

    /**
     * @return fileAreas.
     */
    public String getFileAreas()
    {
        return this.fileAreas;
    }

    /**
     * @param fileAreas set fileAreas.
     */
    public void setFileAreas(String fileAreas)
    {
        this.fileAreas = fileAreas;
    }

    /**
     * @return fileAreasBig.
     */
    public String getFileAreasBig()
    {
        return this.fileAreasBig;
    }

    /**
     * @param fileAreasBig set fileAreasBig.
     */
    public void setFileAreasBig(String fileAreasBig)
    {
        this.fileAreasBig = fileAreasBig;
    }

    /**
     * @return increaseDemandAreaByFactor.
     */
    public boolean isIncreaseDemandAreaByFactor()
    {
        return this.increaseDemandAreaByFactor;
    }

    /**
     * @param increaseDemandAreaByFactor set increaseDemandAreaByFactor.
     */
    public void setIncreaseDemandAreaByFactor(boolean increaseDemandAreaByFactor)
    {
        this.increaseDemandAreaByFactor = increaseDemandAreaByFactor;
    }

    /**
     * @return fileCompressedDemand.
     */
    public String getFileCompressedDemand()
    {
        return this.fileCompressedDemand;
    }

    /**
     * @param fileCompressedDemand set fileCompressedDemand.
     */
    public void setFileCompressedDemand(String fileCompressedDemand)
    {
        this.fileCompressedDemand = fileCompressedDemand;
    }

    /**
     * @return fileProfiles.
     */
    public String getFileProfiles()
    {
        return this.fileProfiles;
    }

    /**
     * @param fileProfiles set fileProfiles.
     */
    public void setFileProfiles(String fileProfiles)
    {
        this.fileProfiles = fileProfiles;
    }

    /**
     * @return fileNameCapacityRestraint.
     */
    public String getFileNameCapacityRestraint()
    {
        return this.fileNameCapacityRestraint;
    }

    /**
     * @param fileNameCapacityRestraint set fileNameCapacityRestraint.
     */
    public void setFileNameCapacityRestraint(String fileNameCapacityRestraint)
    {
        this.fileNameCapacityRestraint = fileNameCapacityRestraint;
    }

    /**
     * @return fileNameCapacityRestraintFactor.
     */
    public String getFileNameCapacityRestraintFactor()
    {
        return this.fileNameCapacityRestraintFactor;
    }

    /**
     * @param fileNameCapacityRestraintFactor set fileNameCapacityRestraintFactor.
     */
    public void setFileNameCapacityRestraintFactor(String fileNameCapacityRestraintFactor)
    {
        this.fileNameCapacityRestraintFactor = fileNameCapacityRestraintFactor;
    }

    /**
     * @return fileNameParametersNTM.
     */
    public String getFileNameParametersNTM()
    {
        return this.fileNameParametersNTM;
    }

    /**
     * @param fileNameParametersNTM set fileNameParametersNTM.
     */
    public void setFileNameParametersNTM(String fileNameParametersNTM)
    {
        this.fileNameParametersNTM = fileNameParametersNTM;
    }

    /**
     * @return fileNameCapacityRestraintBig.
     */
    public String getFileNameCapacityRestraintBig()
    {
        return this.fileNameCapacityRestraintBig;
    }

    /**
     * @param fileNameCapacityRestraintBig set fileNameCapacityRestraintBig.
     */
    public void setFileNameCapacityRestraintBig(String fileNameCapacityRestraintBig)
    {
        this.fileNameCapacityRestraintBig = fileNameCapacityRestraintBig;
    }

    /**
     * @return fileNameCapacityRestraintFactorBig.
     */
    public String getFileNameCapacityRestraintFactorBig()
    {
        return this.fileNameCapacityRestraintFactorBig;
    }

    /**
     * @param fileNameCapacityRestraintFactorBig set fileNameCapacityRestraintFactorBig.
     */
    public void setFileNameCapacityRestraintFactorBig(String fileNameCapacityRestraintFactorBig)
    {
        this.fileNameCapacityRestraintFactorBig = fileNameCapacityRestraintFactorBig;
    }

    /**
     * @return fileNameParametersNTMBig.
     */
    public String getFileNameParametersNTMBig()
    {
        return this.fileNameParametersNTMBig;
    }

    /**
     * @param fileNameParametersNTMBig set fileNameParametersNTMBig.
     */
    public void setFileNameParametersNTMBig(String fileNameParametersNTMBig)
    {
        this.fileNameParametersNTMBig = fileNameParametersNTMBig;
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
     * @return weightNewRoutes.
     */
    public double getWeightNewRoutes()
    {
        return this.weightNewRoutes;
    }

    /**
     * @param weightNewRoutes set weightNewRoutes.
     */
    public void setWeightNewRoutes(double weightNewRoutes)
    {
        this.weightNewRoutes = weightNewRoutes;
    }

    /**
     * @return varianceRoutes.
     */
    public double getVarianceRoutes()
    {
        return this.varianceRoutes;
    }

    /**
     * @param varianceRoutes set varianceRoutes.
     */
    public void setVarianceRoutes(double varianceRoutes)
    {
        this.varianceRoutes = varianceRoutes;
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
     * @return maxSpeed.
     */
    public DoubleScalar<SpeedUnit> getMaxSpeed()
    {
        return this.maxSpeed;
    }

    /**
     * @param maxSpeed set maxSpeed.
     */
    public void setMaxSpeed(DoubleScalar<SpeedUnit> maxSpeed)
    {
        this.maxSpeed = maxSpeed;
    }

    /**
     * @return maxCapacity.
     */
    public DoubleScalar<FrequencyUnit> getMaxCapacity()
    {
        return this.maxCapacity;
    }

    /**
     * @param maxCapacity set maxCapacity.
     */
    public void setMaxCapacity(DoubleScalar<FrequencyUnit> maxCapacity)
    {
        this.maxCapacity = maxCapacity;
    }

    /**
     * @return fileDemand.
     */
    public String getFileDemand()
    {
        return fileDemand;
    }

    /**
     * @param fileDemand set fileDemand.
     */
    public void setFileDemand(String fileDemand)
    {
        this.fileDemand = fileDemand;
    }

    /**
     * @return lengthUnitLink.
     */
    public String getLengthUnitLink()
    {
        return this.lengthUnitLink;
    }

    /**
     * @param lengthUnitLink set lengthUnitLink.
     */
    public void setLengthUnitLink(String lengthUnitLink)
    {
        this.lengthUnitLink = lengthUnitLink;
    }

}
