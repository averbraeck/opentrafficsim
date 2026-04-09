package org.opentrafficsim.road.gtu.lane.tactical.mirova.util.logging.extendeddata;

import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.kpi.interfaces.GtuData;
import org.opentrafficsim.kpi.sampling.data.ExtendedDataString;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.MirovaTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.following.W99ParameterTypes;
import org.opentrafficsim.road.network.sampling.GtuDataRoad;

/** current W99 Driving Mode. */
public class ExtendedDataW99DrivingMode extends ExtendedDataString<GtuData>
{
    /** Single instance. */
    public static final ExtendedDataW99DrivingMode INSTANCE = new ExtendedDataW99DrivingMode();

    /**
     *
     */
    public ExtendedDataW99DrivingMode()
    {
        super("W99DrivingMode", "Current W99 Driving Mode");
    }

    /** Wert je GTU (Sampler-Einstiegspunkt). */
    @Override
    public String getValue(final GtuData gtu)
    {
        if (gtu instanceof GtuDataRoad road)
        {
            LaneBasedGtu lgtu = road.getGtu();
            try
            {
                return lgtu.getParameters().getParameter(W99ParameterTypes.CURRENT_DRIVING_MODE).toString();
            }
            catch (ParameterException exception)
            {
                exception.printStackTrace();
            }
        }
        return "none";
    }

    @Override
    public String toString()
    {
        return "Current W99 Driving Mode";
    }
}
