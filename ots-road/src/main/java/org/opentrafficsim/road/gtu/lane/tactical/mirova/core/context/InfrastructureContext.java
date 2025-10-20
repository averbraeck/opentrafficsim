// ---- Infrastrukturbezogener Kontext ---------------------------------------
package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context;

import org.djunits.value.vdouble.scalar.Length;

public class InfrastructureContext extends ContextCategory {

    public InfrastructureContext() {
        super("Infrastructure");
    }

    public void setRoadType(final String type) { setValue("roadType", type); }
    public String getRoadType() { return getValue("roadType", String.class); }

    public void setDistanceToLaneEnd(final Length dist) { setValue("distToLaneEnd", dist); }
    public Length getDistanceToLaneEnd() { return getValue("distToLaneEnd", Length.class); }

    public void setIsInConstructionZone(final boolean flag) { setValue("constructionZone", flag); }
    public Boolean isInConstructionZone() { return getValue("constructionZone", Boolean.class); }
}
