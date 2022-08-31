package org.opentrafficsim.road.gtu.lane.tactical.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Angle;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeDouble;
import org.opentrafficsim.base.parameters.ParameterTypeDuration;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.base.parameters.constraint.NumericConstraint;
import org.opentrafficsim.core.geometry.DirectedPoint;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlan;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlan.Segment;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;

/**
 * Utility for tactical planners to implement more precise (in terms of physics) vehicle control.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 8 jan. 2019 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public final class Steering
{

    /** Time step parameter. */
    static final ParameterTypeDuration DT =
            new ParameterTypeDuration("DTS", "Steering update time step", Duration.instantiateSI(0.01), NumericConstraint.POSITIVE);

    /** Front tire cornering stiffness. */
    static final ParameterTypeDouble C_FRONT = new ParameterTypeDouble("C_FRONT", "Front tire cornering stiffness", 80000);

    /** Rear tire cornering stiffness. */
    static final ParameterTypeDouble C_REAR = new ParameterTypeDouble("C_REAR", "Rear tire cornering stiffness", 80000);

    /**
     * 
     */
    private Steering()
    {
        // empty constructor
    }

    /**
     * Translates a reference trajectory in to steering angles and a resulting trajectory.
     * @param gtu LaneBasedGTU; GTU
     * @param params Parameters; parameters
     * @param steeringState SteeringState; steering state between operational plans
     * @param referencePlan OperationalPlan; operational reference plan
     * @param feedbackTable FeedbackTable; table of feedback values
     * @return actual operational plan
     * @throws ParameterException undefined parameter
     */
    public static OperationalPlan fromReferencePlan(final LaneBasedGTU gtu, final Parameters params,
            final SteeringState steeringState, final OperationalPlan referencePlan, final FeedbackTable feedbackTable)
            throws ParameterException
    {
        Duration step = Duration.ZERO;
        DirectedPoint pos;
        try
        {
            pos = referencePlan.getLocation(step);
        }
        catch (OperationalPlanException exception)
        {
            throw new RuntimeException(exception); // could not happen, we loop inside the plan duration
        }
        List<OTSPoint3D> points = new ArrayList<>();
        points.add(new OTSPoint3D(pos));
        Angle steeringAngle = steeringState.getSteeringAngle();
        Angle angularError = steeringState.getAngularError();
        double angularErrorDerivative = steeringState.getAngularErrorDerivative();
        Length positionError = steeringState.getPositionError();
        Speed positionErrorDerivative = steeringState.getPositionErrorDerivative();
        while (step.lt(referencePlan.getTotalDuration()))
        {
            Speed speed;
            gtu.getLength();
            gtu.getWidth();
            gtu.getVehicleModel().getMomentOfInertiaAboutZ();
            gtu.getRelativePositions().get(RelativePosition.CENTER_GRAVITY).getDx();
            gtu.getFront();
            gtu.getRear();
            try
            {
                speed = referencePlan.getSpeed(step);
            }
            catch (OperationalPlanException exception)
            {
                throw new RuntimeException(exception); // could not happen, we loop inside the plan duration
            }
            // TODO: apply math

            points.add(new OTSPoint3D(pos)); // with pos being updated to the end position of the current step
            step = step.plus(params.getParameter(DT));
        }
        steeringState.setSteeringAngle(steeringAngle);
        steeringState.setAngularError(angularError);
        steeringState.setAngularErrorDerivative(angularErrorDerivative);
        steeringState.setPositionError(positionError);
        steeringState.setPositionErrorDerivative(positionErrorDerivative);
        OTSLine3D path;
        try
        {
            path = new OTSLine3D(points.toArray(new OTSPoint3D[points.size()]));
        }
        catch (OTSGeometryException exception)
        {
            throw new RuntimeException("The path has too few or too close points.", exception);
        }
        OperationalPlan realPlan;
        try
        {
            realPlan = new OperationalPlan(gtu, path, referencePlan.getStartTime(), referencePlan.getStartSpeed(),
                    referencePlan.getOperationalPlanSegmentList());
        }
        catch (OperationalPlanException exception)
        {
            // TODO: how to handle a shorter (i.e. speed profile assumes more length) or longer path (i.e. impossible to get
            // position on reference trajectory)
            throw new RuntimeException(exception);
        }
        // TODO: return new plan
        return referencePlan;
    }

    /**
     * Translates reference points in to steering angles and a resulting trajectory.
     * @param gtu GTU; GTU
     * @param params Parameters; parameters
     * @param steeringState SteeringState; steering state between operational plans
     * @param points Set&lt;DirectedPoint&gt;; reference points
     * @param segments List&lt;Segment&gt;; speed segments
     * @return operational plan
     * @throws ParameterException undefined parameter
     */
    public static OperationalPlan fromReferencePoints(final GTU gtu, final Parameters params, final SteeringState steeringState,
            final Set<DirectedPoint> points, final List<Segment> segments) throws ParameterException
    {
        // TODO: implement steering control based on reference points
        throw new UnsupportedOperationException();
    }

    /**
     * Object that stores the information the steering utility requires.
     * <p>
     * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 8 jan. 2019 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    public static class SteeringState
    {

        /** Steering angle; this is the angle of the front wheels relative to the vehicle longitudinal axis. */
        private Angle steeringAngle = Angle.ZERO;

        /** Steering angle error. */
        private Angle angularError = Angle.ZERO;

        /** Steering angle error derivative. */
        private double angularErrorDerivative = 0.0;

        /** Position error. */
        private Length positionError = Length.ZERO;

        /** Position error derivative. */
        private Speed positionErrorDerivative = Speed.ZERO;

        /**
         * @return steeringAngle.
         */
        protected Angle getSteeringAngle()
        {
            return this.steeringAngle;
        }

        /**
         * @param steeringAngle Angle; set steeringAngle.
         */
        protected void setSteeringAngle(final Angle steeringAngle)
        {
            this.steeringAngle = steeringAngle;
        }

        /**
         * @return angularError.
         */
        protected Angle getAngularError()
        {
            return this.angularError;
        }

        /**
         * @param angularError Angle; set angularError.
         */
        protected void setAngularError(final Angle angularError)
        {
            this.angularError = angularError;
        }

        /**
         * @return angularErrorDerivative.
         */
        protected double getAngularErrorDerivative()
        {
            return this.angularErrorDerivative;
        }

        /**
         * @param angularErrorDerivative double; set angularErrorDerivative.
         */
        protected void setAngularErrorDerivative(final double angularErrorDerivative)
        {
            this.angularErrorDerivative = angularErrorDerivative;
        }

        /**
         * @return positionError.
         */
        protected Length getPositionError()
        {
            return this.positionError;
        }

        /**
         * @param positionError Length; set positionError.
         */
        protected void setPositionError(final Length positionError)
        {
            this.positionError = positionError;
        }

        /**
         * @return positionErrorDerivative.
         */
        protected Speed getPositionErrorDerivative()
        {
            return this.positionErrorDerivative;
        }

        /**
         * @param positionErrorDerivative Speed; set positionErrorDerivative.
         */
        protected void setPositionErrorDerivative(final Speed positionErrorDerivative)
        {
            this.positionErrorDerivative = positionErrorDerivative;
        }

    }

    /**
     * Class containing feedback values for curvature determination.
     * <p>
     * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 8 jan. 2019 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    public static class FeedbackTable
    {

        /** Feedback vector per speed. */
        private final List<FeedbackVector> feedbackVectors;

        /**
         * @param feedbackVectors List&lt;FeedbackVector&gt;; feedback vector per speed
         */
        public FeedbackTable(final List<FeedbackVector> feedbackVectors)
        {
            Throw.when(feedbackVectors == null || feedbackVectors.size() == 0, IllegalArgumentException.class,
                    "At least one feedback vector should be defined.");
            this.feedbackVectors = feedbackVectors;
        }

        /**
         * Returns the feedback vector pertaining to the speed closest to the input speed.
         * @param speed Speed; speed
         * @return feedback vector pertaining to the speed closest to the input speed
         */
        protected FeedbackVector getAngularErrorFeedback(final Speed speed)
        {
            FeedbackVector feedbackVector = null;
            Speed deviation = Speed.POSITIVE_INFINITY;
            for (int i = 0; i < this.feedbackVectors.size(); i++)
            {
                Speed dev = speed.minus(this.feedbackVectors.get(i).getSpeed()).abs();
                if (dev.lt(deviation))
                {
                    feedbackVector = this.feedbackVectors.get(i);
                    deviation = dev;
                }
            }
            return feedbackVector;
        }

        /**
         * Feedback value for a specific speed.
         * <p>
         * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
         * reserved. <br>
         * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
         * <p>
         * @version $Revision$, $LastChangedDate$, by $Author$, initial version 8 jan. 2019 <br>
         * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
         * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
         * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
         */
        public static class FeedbackVector
        {

            /** Speed. */
            private final Speed speed;

            /** Angular error feedback. */
            private final double angularErrorFeedback;

            /** Angular error derivative feedback. */
            private final double angularErrorDerivateFeedback;

            /** Position error feedback. */
            private final double positionErrorFeedback;

            /** Position error derivative feedback. */
            private final double positionErrorDerivativeFeedback;

            /**
             * @param speed Speed; speed
             * @param angularErrorFeedback double; angular error feedback
             * @param angularErrorDerivateFeedback double; angular error derivative feedback
             * @param positionErrorFeedback double; position error feedback
             * @param positionErrorDerivativeFeedback double; position error derivative feedback
             */
            public FeedbackVector(final Speed speed, final double angularErrorFeedback,
                    final double angularErrorDerivateFeedback, final double positionErrorFeedback,
                    final double positionErrorDerivativeFeedback)
            {
                this.speed = speed;
                this.angularErrorFeedback = angularErrorFeedback;
                this.angularErrorDerivateFeedback = angularErrorDerivateFeedback;
                this.positionErrorFeedback = positionErrorFeedback;
                this.positionErrorDerivativeFeedback = positionErrorDerivativeFeedback;
            }

            /**
             * @return speed.
             */
            protected Speed getSpeed()
            {
                return this.speed;
            }

            /**
             * @return angularErrorFeedback.
             */
            protected double getAngularErrorFeedback()
            {
                return this.angularErrorFeedback;
            }

            /**
             * @return angularErrorDerivateFeedback.
             */
            protected double getAngularErrorDerivateFeedback()
            {
                return this.angularErrorDerivateFeedback;
            }

            /**
             * @return positionErrorFeedback.
             */
            protected double getPositionErrorFeedback()
            {
                return this.positionErrorFeedback;
            }

            /**
             * @return positionErrorDerivativeFeedback.
             */
            protected double getPositionErrorDerivativeFeedback()
            {
                return this.positionErrorDerivativeFeedback;
            }
        }

    }

}
