package org.opentrafficsim.core.gtuchild;

import org.opentrafficsim.core.gtuproperties.*;
import org.opentrafficsim.core.location.Location;
import org.opentrafficsim.core.velocity.Velocity;

public interface MotorizedVehicle<ID, L extends Location, V extends Velocity, P extends Perception, K1 extends Kinetic, K2 extends Kinematic, PP extends PhysicalProperties> extends Vehicle<ID, L, V, P> {
	K1 getKinetic();
	K2 getKinematic();
	PP getPhysicalProperties();
}
