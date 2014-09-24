package org.opentrafficsim.core.gtuchild;

import org.opentrafficsim.core.gtuproperties.Perception;
import org.opentrafficsim.core.location.Location;
import org.opentrafficsim.core.velocity.Velocity;

public interface Person<ID, L extends Location, V extends Velocity, P extends Perception> extends Movable<ID, L, V, P> {

}
