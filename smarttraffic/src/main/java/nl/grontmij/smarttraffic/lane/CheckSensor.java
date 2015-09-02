/**
 * 
 */
package nl.grontmij.smarttraffic.lane;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.naming.NamingException;

import org.djunits.unit.LengthUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.DoubleScalar;
import org.djunits.value.vdouble.scalar.DoubleScalar.Rel;
import org.opentrafficsim.core.car.LaneBasedIndividualCar;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.lane.AbstractSensor;
import org.opentrafficsim.core.network.lane.Lane;
import org.opentrafficsim.core.network.route.CompleteRoute;

/**
 * @author p070518
 */
public class CheckSensor extends AbstractSensor
{

    /** */
    private static final long serialVersionUID = 20141231L;

    private String sensorType;

    private String nameJunction;

    private List<CheckSensor> sensorsParallel = new ArrayList<CheckSensor>();

    private HashMap<DoubleScalar.Abs<TimeUnit>, Integer> statusByTime = new HashMap<DoubleScalar.Abs<TimeUnit>, Integer>();
    
    private Integer currentStatus;

    // private ArrayList<LaneBasedGTU> gtusDetected = new
    // ArrayList<LaneBasedGTU>();

    private Boolean exitLaneSensor = null;

    private Boolean trafficLightSensor = null;

    /**
     * @param lane
     * @param longitudinalPositionFromEnd
     * @param nameSensor
     * @param nameJunction
     */
    public CheckSensor(Lane lane, Length.Rel longitudinalPositionFromEnd, final RelativePosition.TYPE front,
        String nameSensor, final OTSSimulatorInterface simulator)
    {
        super(lane, longitudinalPositionFromEnd, front, nameSensor, simulator);
        try
        {
            new CheckSensorAnimation(this, simulator);
        }
        catch (RemoteException | NamingException exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * @return true if the sensor is less than 10 m before the end, the lane is on one of the main routes, and the next lane
     *         does not end as part of one of the main routes (the end node of the next lane is not part of the main routes).
     */
    public boolean isExitLaneSensor(List<CompleteRoute> routes)
    {
        if (this.exitLaneSensor == null)
        {
            this.exitLaneSensor = false;
            if (getLane().getLength().getSI() - getLongitudinalPositionSI() < 10.0) // less
                                                                                    // than
                                                                                    // 10.0
                                                                                    // m
                                                                                    // before
                                                                                    // end?
            {
                if (isOnMainRoute(getLane(), routes))
                {
                    if (getLane().nextLanes(GTM.GTUTYPE).size() > 0)
                    {
                        Lane nextLane = getLane().nextLanes(GTM.GTUTYPE).iterator().next();
                        if (!isOnMainRoute(nextLane, routes))
                            this.exitLaneSensor = true;
                    }
                }
            }
        }
        return this.exitLaneSensor;
    }

    public boolean isTrafficLightSensor()
    {
        if (this.trafficLightSensor == null)
        {
            this.trafficLightSensor = false;
            for (LaneBasedGTU gtu : getLane().getGtuList())
            {
                if (gtu instanceof TrafficLight)
                {
                    try
                    {
                        if (Math.abs(getLongitudinalPositionSI() - gtu.position(getLane(), gtu.getReference()).getSI()) < 10.0)
                        {
                            this.trafficLightSensor = true;
                        }
                    }
                    catch (RemoteException | NetworkException exception)
                    {
                        exception.printStackTrace();
                    }
                }
            }
        }
        return this.trafficLightSensor;
    }

    private boolean isOnMainRoute(Lane lane, List<CompleteRoute> routes)
    {
        boolean onMain = false;
        for (CompleteRoute route : routes)
        {
            if (route.containsLink(lane.getParentLink()))
            {
                onMain = true;
            }
        }
        return onMain;
    }

    public String getSensorType()
    {
        return this.sensorType;
    }

    public String getNameJunction()
    {
        return this.nameJunction;
    }

    public List<CheckSensor> getSensorsParallel()
    {
        return this.sensorsParallel;
    }

    public HashMap<DoubleScalar.Abs<TimeUnit>, Integer> getStatusByTime()
    {
        return this.statusByTime;
    }

    public void setStatusByTime(HashMap<DoubleScalar.Abs<TimeUnit>, Integer> statusByTime)
    {
        this.statusByTime = statusByTime;
    }

    public void addStatusByTime(DoubleScalar.Abs<TimeUnit> timeNow, Integer status)
    {
        this.statusByTime.put(timeNow, status);
    }

    /**
     * {@inheritDoc} <br>
     * For this method, we assume that the right sensor triggered this method. In this case the sensor that indicates the front
     * of the GTU. The code triggering the sensor therefore has to do the checking for sensor type.
     */
    @Override
    public void trigger(final LaneBasedGTU gtu)
    {
    	// add the sensor that has been triggered
        if (GTM.listGTUsInNetwork.get((LaneBasedIndividualCar) gtu) != null)
        {
            GTM.listGTUsInNetwork.get((LaneBasedIndividualCar) gtu).add(this);
        }
        else
        {
            LinkedList<CheckSensor> linkedList = new LinkedList<CheckSensor>();
            linkedList.add(this);
            GTM.listGTUsInNetwork.put((LaneBasedIndividualCar) gtu, linkedList);
        }
        ReportNumbers.reportPassingVehicles(GTM.outputFileVehiclesTriggered, gtu, "C" + this.getName(), this.getSimulator());
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "SensorAtLane [getLane()=" + this.getLane() + ", getLongitudinalPosition()=" + this.getLongitudinalPosition()
            + ", getPositionType()=" + this.getPositionType() + "]";
    }

	public Integer getCurrentStatus() {
		return currentStatus;
	}

	public void setCurrentStatus(Integer currentStatus) {
		this.currentStatus = currentStatus;
	}

}
