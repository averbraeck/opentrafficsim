package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask;

import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.perception.EgoPerception;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.road.gtu.lane.perception.categories.DirectDefaultSimplePerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.InfrastructurePerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.TrafficPerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.NeighborsPerception;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ActionAdvice;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPattern;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.VehicleTypes.AbstractMirovaVehicle;

public abstract class DrivingTask
{
    /**
     * Abstract base class representing a driving task in a traffic simulation. This class serves as the foundation for
     * implementing specific driving tasks, such as lane changes, merging, or cooperative behavior. It provides the structure
     * for evaluating the activation of a task, calculating its utility, and deciding on maneuver advice. Attributes: -
     * contextVehicle: The vehicle associated with this driving task. - latestUtility: The most recent utility value calculated
     * for this task.
     */

    protected AbstractMirovaVehicle AbstractMirovaVehicle;

    protected Double latestUtility;

    private InfrastructurePerception infrastructurePerception;

    private TrafficPerception trafficPerception;

    private EgoPerception egoPerception;

    private NeighborsPerception neighborsPerception;

    private DirectDefaultSimplePerception directDefaultSimplePerception;

    private Parameters parameters;

    /**
     * @param AbstractMirovaVehicle
     * @throws OperationalPlanException
     */
    public DrivingTask(final AbstractMirovaVehicle AbstractMirovaVehicle) throws OperationalPlanException
    {
        this.AbstractMirovaVehicle = AbstractMirovaVehicle;
        this.latestUtility = null;
        this.infrastructurePerception =
                AbstractMirovaVehicle.getLanePerception().getPerceptionCategory(InfrastructurePerception.class);
        this.trafficPerception = AbstractMirovaVehicle.getLanePerception().getPerceptionCategory(TrafficPerception.class);
        this.egoPerception = AbstractMirovaVehicle.getLanePerception().getPerceptionCategory(EgoPerception.class);
        this.neighborsPerception = AbstractMirovaVehicle.getLanePerception().getPerceptionCategory(NeighborsPerception.class);
        this.directDefaultSimplePerception =
                AbstractMirovaVehicle.getLanePerception().getPerceptionCategory(DirectDefaultSimplePerception.class);
        this.parameters  = AbstractMirovaVehicle.getGtu().getParameters();
    }

    /**
     * Determines whether the driving task should be activated. If the utility of the task (calculated using calculateUtility)
     * is greater than or equal to 100, the task is activated.
     * @return true if the task should be activated, false otherwise.
     * @throws ParameterException
     */
    public boolean getActivation() throws ParameterException
    {
        return calculateDesire() >= 100;
    }



    /**
     * Executes the decision tree to determine the appropriate maneuver advice. The decision tree should be implemented in
     * subclasses to reflect the specific logic of the driving task.
     * @return the advice for the next maneuver.
     * @throws IllegalArgumentException
     * @throws NullPointerException
     * @throws ParameterException
     * @throws OperationalPlanException
     */
    public abstract ManeuverPattern decideManeuverPattern() throws OperationalPlanException, ParameterException, NullPointerException, IllegalArgumentException;

    public AbstractMirovaVehicle getAbstractMirovaVehicle()
    {
        return this.AbstractMirovaVehicle;
    }

    public InfrastructurePerception getInfrastructurePerception() {
        return this.infrastructurePerception;
    }

    public TrafficPerception getTrafficPerception() {
        return this.trafficPerception;
    }

    public EgoPerception getEgoPerception() {
        return this.egoPerception;
    }

    public NeighborsPerception getNeighborsPerception() {
        return this.neighborsPerception;
    }

    public DirectDefaultSimplePerception getDirectDefaultSimplePerception() {
        return this.directDefaultSimplePerception;
    }

    public Parameters getParameters() {
        return this.parameters;
    }

    /**
     * Placeholder for calculating the utility of the merging maneuver.
     * <p>
     * This method can be extended to evaluate the benefits or costs of the merging maneuver based on various factors such as
     * traffic density, gap size, and vehicle speed.
     * </p>
     * @return the utility value (currently always 0.0)
     * @throws ParameterException
     */
    public double calculateDesire() throws ParameterException
    {
        return 0;
    }

}
