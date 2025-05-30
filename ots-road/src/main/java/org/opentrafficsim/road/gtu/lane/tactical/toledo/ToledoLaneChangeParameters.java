package org.opentrafficsim.road.gtu.lane.tactical.toledo;

import java.lang.reflect.Field;

import org.djunits.unit.DurationUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeDouble;
import org.opentrafficsim.base.parameters.ParameterTypeDuration;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.base.parameters.constraint.ConstraintInterface;

/**
 * List of parameters for the model of Toledo (2003).<br>
 * <br>
 * Tomer Toledo (2003) "Integrated Driving Behavior Modeling", Massachusetts Institute of Technology.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */

public final class ToledoLaneChangeParameters
{

    /** Fixed model time step. */
    public static final ParameterTypeDuration DT = new ParameterTypeDuration("dt", "Fixed model time step.",
            new Duration(0.5, DurationUnit.SI), ConstraintInterface.POSITIVE);

    /** Current lane constant. */
    public static final ParameterTypeDouble C_CL = new ParameterTypeDouble("C_CL", "Current lane constant.", 2.128);

    /** Right lane constant. */
    public static final ParameterTypeDouble C_RL = new ParameterTypeDouble("C_RL", "Right lane constant.", -0.369);

    /** Factor on right-most dummy variable. */
    public static final ParameterTypeDouble BETA_RIGHT_MOST =
            new ParameterTypeDouble("Beta_right-most", "Factor on right-most dummy variable.", -1.039);

    /** Factor on speed of front vehicle. */
    public static final ParameterTypeDouble BETA_VFRONT =
            new ParameterTypeDouble("Beta_Vfront", "Factor on speed of front vehicle.", 0.0745);

    /** Factor on spacing of front vehicle. */
    public static final ParameterTypeDouble BETA_SFRONT =
            new ParameterTypeDouble("Beta_Sfront", "Factor on spacing of front vehicle.", 0.0225);

    /** Factor on density. */
    public static final ParameterTypeDouble BETA_DENSITY =
            new ParameterTypeDouble("Beta_density", "Factor on density.", -0.0018);

    /** Factor on heavy neighbour dummy variable. */
    public static final ParameterTypeDouble BETA_HEAVY_NEIGHBOUR =
            new ParameterTypeDouble("Beta_heavy_neighbour", "Factor on heavy neighbour dummy variable.", -0.218);

    /** Factor on tailgate dummy variable. */
    public static final ParameterTypeDouble BETA_TAILGATE =
            new ParameterTypeDouble("Beta_tailgate", "Factor on tailgate dummy variable.", -3.793);

    /** Mandatory lane change power. */
    public static final ParameterTypeDouble THETA_MLC =
            new ParameterTypeDouble("Theta_MLC", "Mandatory lane change power.", -0.358);

    /** Factor on 1 lane change dummy variable. */
    public static final ParameterTypeDouble BETA1 =
            new ParameterTypeDouble("Beta1", "Factor on 1 lane change dummy variable.", -2.269);

    /** Factor on 2 lane change dummy variable. */
    public static final ParameterTypeDouble BETA2 =
            new ParameterTypeDouble("Beta2", "Factor on 2 lane change dummy variable.", -4.466);

    /** Factor on 3 lane change dummy variable. */
    public static final ParameterTypeDouble BETA3 =
            new ParameterTypeDouble("Beta3", "Factor on 3 lane change dummy variable.", -7.265);

    /** Factor on next exit dummy variable. */
    public static final ParameterTypeDouble BETA_NEXT_EXIT =
            new ParameterTypeDouble("Beta_next_exit", "Factor on next exit dummy variable.", -1.264);

    /** Factor on add dummy variable. */
    public static final ParameterTypeDouble BETA_ADD =
            new ParameterTypeDouble("Beta_add", "Factor on add dummy variable.", -0.252);

    /** Factor on individual error term in current lane. */
    public static final ParameterTypeDouble ALPHA_CL =
            new ParameterTypeDouble("Alpha_cl", "Factor on individual error term in current lane.", 0.539);

    /** Factor on individual error term in right lane. */
    public static final ParameterTypeDouble ALPHA_RL =
            new ParameterTypeDouble("Alpha_rl", "Factor on individual error term in right lane.", 1.035);

    /** Individual error term. */
    public static final ParameterTypeDouble ERROR_TERM = new ParameterTypeDouble("Error term", "Individual error term.", 0);

    /** Factor on expected utility maximization of gap acceptance. */
    public static final ParameterTypeDouble BETA_EMU_GA =
            new ParameterTypeDouble("Beta_EMU_GA", "Factor on expected utility maximization of gap acceptance.", 0.0052);

    /** Standard deviation in gap acceptance error term for lead gap. */
    public static final ParameterTypeDouble SIGMA_LEAD =
            new ParameterTypeDouble("Sigma_lead", "Standard deviation in gap acceptance error term for lead gap.", 1.217);

    /** Standard deviation in gap acceptance error term for lead gap. */
    public static final ParameterTypeDouble SIGMA_LAG =
            new ParameterTypeDouble("Sigma_lag", "Standard deviation in gap acceptance error term for lead gap.", 0.622);

    /** Constant in lead critical gap. */
    public static final ParameterTypeDouble C_LEAD = new ParameterTypeDouble("C_lead", "Constant in lead critical gap.", 1.127);

    /** Constant in lag critical gap. */
    public static final ParameterTypeDouble C_LAG = new ParameterTypeDouble("C_lag", "Constant in lag critical gap.", 0.968);

    /** Factor on positive lead speed difference. */
    public static final ParameterTypeDouble BETA_POS_LEAD =
            new ParameterTypeDouble("Beta_pos_lead", "Factor on positive lead speed difference.", -2.178);

    /** Factor on negative lead speed difference. */
    public static final ParameterTypeDouble BETA_NEG_LEAD =
            new ParameterTypeDouble("Beta_neg_lead", "Factor on negative lead speed difference.", -0.153);

    /** Factor on positive lag speed difference. */
    public static final ParameterTypeDouble BETA_POS_LAG =
            new ParameterTypeDouble("Beta_pos_lag", "Factor on positive lag speed difference.", 0.491);

    /** Factor on EMU target lead gap. */
    public static final ParameterTypeDouble BETA_EMU_LEAD =
            new ParameterTypeDouble("Beta_EMU_lead", "Factor on EMU target lead gap.", 0.0045);

    /** Factor on EMU target lag gap. */
    public static final ParameterTypeDouble BETA_EMU_LAG =
            new ParameterTypeDouble("Beta_EMU_lag", "Factor on EMU target lag gap.", 0.0152);

    /** Factor on individual error term in target lane lead gap. */
    public static final ParameterTypeDouble ALPHA_TL_LEAD =
            new ParameterTypeDouble("Alpha_tl_lead", "Factor on individual error term in target lane lead gap.", 0.789);

    /** Factor on individual error term in target lane lag gap. */
    public static final ParameterTypeDouble ALPHA_TL_LAG =
            new ParameterTypeDouble("Alpha_tl_lag", "Factor on individual error term in target lane lag gap.", 0.107);

    /** Constant forward gap utility. */
    public static final ParameterTypeDouble C_FWD_TG =
            new ParameterTypeDouble("C_fwd_tg", "Constant forward gap utility.", -0.837);

    /** Constant forward gap utility. */
    public static final ParameterTypeDouble C_BCK_TG =
            new ParameterTypeDouble("C_bck_tg", "Constant forward gap utility.", 0.913);

    /** Factor on distance to gap. */
    public static final ParameterTypeDouble BETA_DTG =
            new ParameterTypeDouble("Beta_dtg", "Factor on distance to gap.", -2.393);

    /** Factor on effective gap length. */
    public static final ParameterTypeDouble BETA_EG =
            new ParameterTypeDouble("Beta_eg", "Factor on effective gap length.", 0.816);

    /** Factor on front vehicle dummy variable. */
    public static final ParameterTypeDouble BETA_FV =
            new ParameterTypeDouble("Beta_fv", "Factor on front vehicle dummy variable.", -1.662);

    /** Factor on relative gap speed. */
    public static final ParameterTypeDouble BETA_RGS =
            new ParameterTypeDouble("Beta_rgs", "Factor on relative gap speed.", -1.218);

    /** Factor on individual error term in adjacent gap. */
    public static final ParameterTypeDouble ALPHA_ADJ =
            new ParameterTypeDouble("Alpha_adj", "Factor on individual error term in adjacent gap.", 0.675);

    /** Factor on individual error term in backward gap. */
    public static final ParameterTypeDouble ALPHA_BCK =
            new ParameterTypeDouble("Alpha_bck", "Factor on individual error term in backward gap.", 0.239);

    /** Factor on target gap for desired position. */
    public static final ParameterTypeDouble BETA_DP = new ParameterTypeDouble("BETA_DP",
            "Factor on target gap for desired position.", 0.604, ConstraintInterface.UNITINTERVAL);

    /** Constant forward gap acceleration. */
    public static final ParameterTypeDouble C_FWD_ACC =
            new ParameterTypeDouble("C_fwd_acc", "Constant forward gap acceleration.", 0.385, ConstraintInterface.POSITIVE);

    /** Power on desired relative position forward. */
    public static final ParameterTypeDouble BETA_FWD =
            new ParameterTypeDouble("BETA_fwd", "Power on desired relative position forward.", 0.323);

    /** Factor on positive relative speed forward. */
    public static final ParameterTypeDouble LAMBDA_FWD_POS =
            new ParameterTypeDouble("LAMBDA_fwd_pos", "Factor on positive relative speed forward.", 0.0678);

    /** Factor on negative relative speed forward. */
    public static final ParameterTypeDouble LAMBDA_FWD_NEG =
            new ParameterTypeDouble("LAMBDA_fwd_neg", "Factor on negative relative speed forward.", 0.217);

    /** Standard deviation on forward gap acceleration error. */
    public static final ParameterTypeDouble SIGMA_FWD =
            new ParameterTypeDouble("SIGMA_fwd", "Standard deviation on forward gap acceleration error.", Math.exp(-0.540));

    /** Constant backward gap acceleration. */
    public static final ParameterTypeDouble C_BCK_ACC =
            new ParameterTypeDouble("C_bck_acc", "Constant backward gap acceleration.", -0.596, ConstraintInterface.NEGATIVE);

    /** Power on desired relative position backward. */
    public static final ParameterTypeDouble BETA_BCK =
            new ParameterTypeDouble("BETA_bck", "Power on desired relative position backward.", -0.219);

    /** Factor on positive relative speed backward. */
    public static final ParameterTypeDouble LAMBDA_BCK_POS =
            new ParameterTypeDouble("LAMBDA_bck_pos", "Factor on positive relative speed backward.", -0.0832);

    /** Factor on negative relative speed backward. */
    public static final ParameterTypeDouble LAMBDA_BCK_NEG =
            new ParameterTypeDouble("LAMBDA_bck_neg", "Factor on negative relative speed backward.", -0.170);

    /** Standard deviation on backward gap acceleration error. */
    public static final ParameterTypeDouble SIGMA_BCK =
            new ParameterTypeDouble("SIGMA_bck", "Standard deviation on backward gap acceleration error.", Math.exp(0.391));

    /** Constant adjacent gap acceleration. */
    public static final ParameterTypeDouble C_ADJ_ACC =
            new ParameterTypeDouble("C_adj_acc", "Constant adjacent gap acceleration.", 0.131);

    /** Standard deviation on adjacent gap acceleration error. */
    public static final ParameterTypeDouble SIGMA_ADJ =
            new ParameterTypeDouble("SIGMA_adj", "Standard deviation on adjacent gap acceleration error.", Math.exp(-1.202));

    /**
     * Constructor.
     */
    private ToledoLaneChangeParameters()
    {
    }

    /**
     * Fills parameters with default values for all Toledo parameters.
     * @param parameters parameters to fill
     */
    public static void setDefaultParameters(final Parameters parameters)
    {
        for (Field field : ToledoLaneChangeParameters.class.getDeclaredFields())
        {
            ParameterTypeDouble p;
            try
            {
                p = (ParameterTypeDouble) field.get(null);
                parameters.setParameter(p, p.getDefaultValue());
            }
            catch (IllegalArgumentException | IllegalAccessException | ClassCastException exception)
            {
                // if this occurs, this code should perhaps also support other parameter types, or perform other checks
                throw new RuntimeException("A field of ToledoParameters is not a public static ParameterTypeDouble.",
                        exception);
            }
            catch (ParameterException exception)
            {
                throw new RuntimeException("A field of ToledoParameters does not have a default value.", exception);
            }
        }
    }

}
