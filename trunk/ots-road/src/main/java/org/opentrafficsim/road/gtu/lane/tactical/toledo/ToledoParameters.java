package org.opentrafficsim.road.gtu.lane.tactical.toledo;

import java.lang.reflect.Field;

import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristics;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterTypeDouble;

/**
 * List of parameters for the model of Toledo (2003).<br>
 * <br>
 * Tomer Toledo (2003) "Integrated Driving Behavior Modeling", Massachusetts Institute of Technology.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Jun 21, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */

public final class ToledoParameters
{

    /** */
    public static final ParameterTypeDouble C_CL = new ParameterTypeDouble("C_CL", "Current lane constant.", 2.128);

    /** */
    public static final ParameterTypeDouble C_RL = new ParameterTypeDouble("C_RL", "Right lane constant.", -0.369);

    /** */
    public static final ParameterTypeDouble BETA_RIGHT_MOST = new ParameterTypeDouble("Beta_right-most",
        "Factor on right-most dummy variable.", -1.039);

    /** */
    public static final ParameterTypeDouble BETA_VFRONT = new ParameterTypeDouble("Beta_Vfront",
        "Factor on speed of front vehicle.", 0.0745);

    /** */
    public static final ParameterTypeDouble BETA_SFRONT = new ParameterTypeDouble("Beta_Sfront",
        "Factor on spacing of front vehicle.", 0.0225);

    /** */
    public static final ParameterTypeDouble BETA_DENSITY = new ParameterTypeDouble("Beta_density", "Factor on density.",
        -0.0018);

    /** */
    public static final ParameterTypeDouble BETA_HEAVY_NEIGHBOUR = new ParameterTypeDouble("Beta_heavy_neighbour",
        "Factor on heavy neighbour dummy variable.", -0.218);

    /** */
    public static final ParameterTypeDouble BETA_TAILGATE = new ParameterTypeDouble("Beta_tailgate",
        "Factor on tailgate dummy variable.", -3.793);

    /** */
    public static final ParameterTypeDouble THETA_MLC = new ParameterTypeDouble("Theta_MLC", "Mandatory lane change power.",
        -0.358);

    /** */
    public static final ParameterTypeDouble BETA1 = new ParameterTypeDouble("Beta1",
        "Factor on 1 lane change dummy variable.", -2.269);

    /** */
    public static final ParameterTypeDouble BETA2 = new ParameterTypeDouble("Beta2",
        "Factor on 2 lane change dummy variable.", -4.466);

    /** */
    public static final ParameterTypeDouble BETA3 = new ParameterTypeDouble("Beta3",
        "Factor on 3 lane change dummy variable.", -7.265);

    /** */
    public static final ParameterTypeDouble BETA_NEXT_EXIT = new ParameterTypeDouble("Beta_next_exit",
        "Factor on next exit dummy variable.", -1.264);

    /** */
    public static final ParameterTypeDouble BETA_ADD = new ParameterTypeDouble("Beta_add", "Factor on add dummy variable.",
        -0.252);

    /** */
    public static final ParameterTypeDouble ALPHA_CL = new ParameterTypeDouble("Alpha_cl",
        "Factor on individual error term in current lane.", 0.539);

    /** */
    public static final ParameterTypeDouble ALPHA_RL = new ParameterTypeDouble("Alpha_rl",
        "Factor on individual error term in right lane.", 1.035);

    /** */
    public static final ParameterTypeDouble ERROR_TERM = new ParameterTypeDouble("Error term", "Individual error term.", 0);

    /** */
    public static final ParameterTypeDouble BETA_EMU_GA = new ParameterTypeDouble("Beta_EMU_GA",
        "Factor on expected utility maximization of gap acceptance.", 0.0052);

    /** */
    public static final ParameterTypeDouble SIGMA_LEAD = new ParameterTypeDouble("Sigma_lead",
        "Standard deviation in gap acceptance error term for lead gap.", 1.217);

    /** */
    public static final ParameterTypeDouble SIGMA_LAG = new ParameterTypeDouble("Sigma_lag",
        "Standard deviation in gap acceptance error term for lead gap.", 0.622);

    /** */
    public static final ParameterTypeDouble C_LEAD = new ParameterTypeDouble("C_lead", "Constant in lead critical gap.",
        1.127);

    /** */
    public static final ParameterTypeDouble C_LAG = new ParameterTypeDouble("C_lag", "Constant in lag critical gap.", 0.968);

    /** */
    public static final ParameterTypeDouble BETA_POS_LEAD = new ParameterTypeDouble("Beta_pos_lead",
        "Factor on positive lead speed difference.", -2.178);

    /** */
    public static final ParameterTypeDouble BETA_NEG_LEAD = new ParameterTypeDouble("Beta_neg_lead",
        "Factor on negative lead speed difference.", -0.153);

    /** */
    public static final ParameterTypeDouble BETA_POS_LAG = new ParameterTypeDouble("Beta_pos_lag",
        "Factor on positive lag speed difference.", 0.491);

    /** */
    public static final ParameterTypeDouble BETA_EMU_LEAD = new ParameterTypeDouble("Beta_EMU_lead",
        "Factor on EMU target lead gap.", 0.0045);

    /** */
    public static final ParameterTypeDouble BETA_EMU_LAG = new ParameterTypeDouble("Beta_EMU_lag",
        "Factor on EMU target gap gap.", 0.0152);

    /** */
    public static final ParameterTypeDouble ALPHA_TL_LEAD = new ParameterTypeDouble("Alpha_tl_lead",
        "Factor on individual error term in target lane lead gap.", 0.789);

    /** */
    public static final ParameterTypeDouble ALPHA_TL_LAG = new ParameterTypeDouble("Alpha_tl_lag",
        "Factor on individual error term in target lane lag gap.", 0.107);

    /** */
    public static final ParameterTypeDouble C_FWD =
        new ParameterTypeDouble("C_fwd", "Constant forward gap utility.", -0.837);

    /** */
    public static final ParameterTypeDouble C_BCK = new ParameterTypeDouble("C_fwd", "Constant forward gap utility.", 0.913);

    /** */
    public static final ParameterTypeDouble BETA_DTG = new ParameterTypeDouble("Beta_dtg", "Factor on distance to gap.",
        -2.393);

    /** */
    public static final ParameterTypeDouble BETA_EG = new ParameterTypeDouble("Beta_eg", "Factor on effective gap length.",
        0.816);

    /** */
    public static final ParameterTypeDouble BETA_FV = new ParameterTypeDouble("Beta_fv",
        "Factor on front vehicle dummy variable.", -1.662);

    /** */
    public static final ParameterTypeDouble BETA_RGS = new ParameterTypeDouble("Beta_rgs", "Factor on relative gap speed.",
        -1.218);

    /** */
    public static final ParameterTypeDouble ALPHA_ADJ = new ParameterTypeDouble("Alpha_adj",
        "Factor on individual error term in adjacent gap.", 0.675);

    /** */
    public static final ParameterTypeDouble ALPHA_BCK = new ParameterTypeDouble("Alpha_bck",
        "Factor on individual error term in backward gap.", 0.239);

    /**
     * 
     */
    private ToledoParameters()
    {
    }

    /**
     * Fills behavioral characteristics with default values for all Toledo parameters.
     * @param behavioralCharacteristics behavioral characteristics to fill
     */
    public static void setDefaultParameters(final BehavioralCharacteristics behavioralCharacteristics)
    {
        for (Field field : ToledoParameters.class.getDeclaredFields())
        {
            ParameterTypeDouble p;
            try
            {
                p = (ParameterTypeDouble) field.get(null);
                behavioralCharacteristics.setParameter(p, p.getDefaultValue());
            }
            catch (IllegalArgumentException | IllegalAccessException | ClassCastException exception)
            {
                // if this occurs, this code should perhaps also support other parameter types, or perform other checks
                throw new RuntimeException("A field of ToledoParameters is not a public static ParameterTypeDouble.");
            }
            catch (ParameterException exception)
            {
                throw new RuntimeException("A field of ToledoParameters does not have a default value.");
            }
        }
    }

}
