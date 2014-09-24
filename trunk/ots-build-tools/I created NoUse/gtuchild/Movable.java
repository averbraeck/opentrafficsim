package org.opentrafficsim.core.gtuchild;

import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.gtuproperties.Perception;
import org.opentrafficsim.core.location.Location;
import org.opentrafficsim.core.velocity.Velocity;

public interface Movable<ID, L extends Location, V extends Velocity, P extends Perception> extends GTU<ID, L, V> {
	P getPerception();
}
