// ---- Makroskopische Verkehrsgrößen ---------------------------------------
package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context;

import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.LinearDensity;

public class MacroTrafficContext extends ContextCategory {

    public MacroTrafficContext() {
        super("MacroTraffic");
    }

    public void setMeanSpeed(final Speed speed) { setValue("meanSpeed", speed); }
    public Speed getMeanSpeed() { return getValue("meanSpeed", Speed.class); }

    public void setDensity(final LinearDensity density) { setValue("density", density); }
    public LinearDensity getDensity() { return getValue("density", LinearDensity.class); }

    public void setFlow(final double flow) { setValue("flow", flow); }
    public Double getFlow() { return getValue("flow", Double.class); }
}
