package org.opentrafficsim.road.gtu.lane;

import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.geometry.OtsGeometryException;
import org.opentrafficsim.core.gtu.GtuErrorHandler;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.generator.characteristics.LaneBasedGtuCharacteristics;
import org.opentrafficsim.road.network.RoadNetwork;
import org.opentrafficsim.road.network.lane.LanePosition;

import java.util.Set;

public class GtuCreator {

    public void createGTU(String obstacleId, LaneBasedGtuCharacteristics template_gtu_type, RoadNetwork net, Speed speed, Set<LanePosition> positions)
            throws GtuException, OtsGeometryException, NetworkException {
        LaneBasedGtu gtu = new LaneBasedGtu(obstacleId, template_gtu_type.getGtuType(), template_gtu_type.getLength(), template_gtu_type.getWidth(),
                template_gtu_type.getMaximumSpeed(), template_gtu_type.getLength().divide(2), net);
        gtu.setMaximumAcceleration(template_gtu_type.getMaximumAcceleration());
        gtu.setMaximumDeceleration(template_gtu_type.getMaximumDeceleration());
        gtu.setVehicleModel(template_gtu_type.getVehicleModel());
        gtu.setNoLaneChangeDistance(null);
        gtu.setInstantaneousLaneChange(false);
        gtu.setErrorHandler(GtuErrorHandler.THROW);

        gtu.init(template_gtu_type.getStrategicalPlannerFactory().create(gtu, template_gtu_type.getRoute(), template_gtu_type.getOrigin(),
                template_gtu_type.getDestination()),positions, speed);
    }
}
