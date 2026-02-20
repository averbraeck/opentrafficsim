
package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlSeeAlso;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opentrafficsim.xml.bindings.CarFollowingModelAdapter;
import org.opentrafficsim.xml.bindings.CooperationAdapter;
import org.opentrafficsim.xml.bindings.GapAcceptanceAdapter;
import org.opentrafficsim.xml.bindings.OnOffAdapter;
import org.opentrafficsim.xml.bindings.SynchronizationAdapter;
import org.opentrafficsim.xml.bindings.types.BooleanType;
import org.opentrafficsim.xml.bindings.types.CarFollowingModelType;
import org.opentrafficsim.xml.bindings.types.CooperationType;
import org.opentrafficsim.xml.bindings.types.GapAcceptanceType;
import org.opentrafficsim.xml.bindings.types.SynchronizationType;


/**
 * The default model holds the default settings that might be individually
 *         overruled by models specified for a GTU type.
 * 
 * <p>Java class for LmrsModel complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="LmrsModel">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *         <element name="MandatoryIncentives">
 *           <complexType>
 *             <complexContent>
 *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 <attribute name="Route" type="{http://www.opentrafficsim.org/ots}OnOff" />
 *                 <attribute name="GetInLane" type="{http://www.opentrafficsim.org/ots}OnOff" />
 *               </restriction>
 *             </complexContent>
 *           </complexType>
 *         </element>
 *         <element name="VoluntaryIncentives">
 *           <complexType>
 *             <complexContent>
 *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 <attribute name="Speed" type="{http://www.opentrafficsim.org/ots}OnOff" />
 *                 <attribute name="Keep" type="{http://www.opentrafficsim.org/ots}OnOff" />
 *                 <attribute name="Courtesy" type="{http://www.opentrafficsim.org/ots}OnOff" />
 *                 <attribute name="Queue" type="{http://www.opentrafficsim.org/ots}OnOff" />
 *                 <attribute name="StayOnSlowLanes" type="{http://www.opentrafficsim.org/ots}OnOff" />
 *               </restriction>
 *             </complexContent>
 *           </complexType>
 *         </element>
 *         <element name="AccelerationIncentives">
 *           <complexType>
 *             <complexContent>
 *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 <attribute name="SpeedLimitTransitions" type="{http://www.opentrafficsim.org/ots}OnOff" />
 *                 <attribute name="TrafficLights" type="{http://www.opentrafficsim.org/ots}OnOff" />
 *                 <attribute name="Conflicts" type="{http://www.opentrafficsim.org/ots}OnOff" />
 *                 <attribute name="NoSlowLaneOvertake" type="{http://www.opentrafficsim.org/ots}OnOff" />
 *               </restriction>
 *             </complexContent>
 *           </complexType>
 *         </element>
 *         <element name="SocialInteractions">
 *           <complexType>
 *             <complexContent>
 *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 <attribute name="SocialPressure" type="{http://www.opentrafficsim.org/ots}OnOff" />
 *                 <attribute name="Tailgating" type="{http://www.opentrafficsim.org/ots}OnOff" />
 *                 <attribute name="LaneChanges" type="{http://www.opentrafficsim.org/ots}OnOff" />
 *                 <attribute name="Speed" type="{http://www.opentrafficsim.org/ots}OnOff" />
 *               </restriction>
 *             </complexContent>
 *           </complexType>
 *         </element>
 *         <element name="Perception">
 *           <complexType>
 *             <complexContent>
 *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 <choice>
 *                   <element name="None" type="{http://www.opentrafficsim.org/ots}EmptyType"/>
 *                   <element name="FullerAttentionMatrix" type="{http://www.opentrafficsim.org/ots}FullerAttentionMatrix"/>
 *                   <element name="FullerAnticipationReliance" type="{http://www.opentrafficsim.org/ots}FullerAnticipationReliance"/>
 *                   <element name="FullerSummative" type="{http://www.opentrafficsim.org/ots}FullerSummative"/>
 *                 </choice>
 *               </restriction>
 *             </complexContent>
 *           </complexType>
 *         </element>
 *       </sequence>
 *       <attribute name="CarFollowingModel">
 *         <simpleType>
 *           <union memberTypes=" {http://www.opentrafficsim.org/ots}Expression">
 *             <simpleType>
 *               <restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                 <enumeration value="IDM"/>
 *                 <enumeration value="IDM+"/>
 *               </restriction>
 *             </simpleType>
 *           </union>
 *         </simpleType>
 *       </attribute>
 *       <attribute name="Synchronization">
 *         <simpleType>
 *           <union memberTypes=" {http://www.opentrafficsim.org/ots}Expression">
 *             <simpleType>
 *               <restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                 <enumeration value="PASSIVE"/>
 *                 <enumeration value="PASSIVE_MOVING"/>
 *                 <enumeration value="ALIGN_GAP"/>
 *                 <enumeration value="ACTIVE"/>
 *               </restriction>
 *             </simpleType>
 *           </union>
 *         </simpleType>
 *       </attribute>
 *       <attribute name="Cooperation">
 *         <simpleType>
 *           <union memberTypes=" {http://www.opentrafficsim.org/ots}Expression">
 *             <simpleType>
 *               <restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                 <enumeration value="PASSIVE"/>
 *                 <enumeration value="PASSIVE_MOVING"/>
 *                 <enumeration value="ACTIVE"/>
 *               </restriction>
 *             </simpleType>
 *           </union>
 *         </simpleType>
 *       </attribute>
 *       <attribute name="GapAcceptance">
 *         <simpleType>
 *           <union memberTypes=" {http://www.opentrafficsim.org/ots}Expression">
 *             <simpleType>
 *               <restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                 <enumeration value="INFORMED"/>
 *                 <enumeration value="EGO_HEADWAY"/>
 *               </restriction>
 *             </simpleType>
 *           </union>
 *         </simpleType>
 *       </attribute>
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LmrsModel", propOrder = {
    "mandatoryIncentives",
    "voluntaryIncentives",
    "accelerationIncentives",
    "socialInteractions",
    "perception"
})
@XmlSeeAlso({
    GtuTypeLmrsModel.class
})
@SuppressWarnings("all") public class LmrsModel
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    /**
     * Mandatory lane change incentives that increasingly overrule incongruent
     *             voluntary lane change incentives as the mandatory lane change desire increases.
     * 
     */
    @XmlElement(name = "MandatoryIncentives", required = true)
    protected LmrsModel.MandatoryIncentives mandatoryIncentives;
    /**
     * Voluntary lane change incentives determine lane change desire up to the
     *             level that mandatory lane change desire is not active.
     * 
     */
    @XmlElement(name = "VoluntaryIncentives", required = true)
    protected LmrsModel.VoluntaryIncentives voluntaryIncentives;
    /**
     * Reasons that make drivers decelerate beyond regular car-following or
     *             keeping to their desired speed.
     * 
     */
    @XmlElement(name = "AccelerationIncentives", required = true)
    protected LmrsModel.AccelerationIncentives accelerationIncentives;
    /**
     * Interactions to allow and to be allowed more speed.
     * 
     */
    @XmlElement(name = "SocialInteractions", required = true)
    protected LmrsModel.SocialInteractions socialInteractions;
    @XmlElement(name = "Perception", required = true)
    protected LmrsModel.Perception perception;
    /**
     * IDM: Intelligent Driver Model. IDM+: adaptation of IDM with maximum of
     *           free and car-following acceleration.
     * 
     */
    @XmlAttribute(name = "CarFollowingModel")
    @XmlJavaTypeAdapter(CarFollowingModelAdapter.class)
    protected CarFollowingModelType carFollowingModel;
    /**
     * PASSIVE: follow first leader in target lane. PASSIVE_MOVING: keep moving
     *           below cooperation threshold. ALIGNGAP: align to middle of adjacent gap. ACTIVE: actively consider whether gaps can be
     *           reached in time (not advised).
     * 
     */
    @XmlAttribute(name = "Synchronization")
    @XmlJavaTypeAdapter(SynchronizationAdapter.class)
    protected SynchronizationType synchronization;
    /**
     * PASSIVE: follow potential lane changer. PASSIVE_MOVING: follow potential
     *           lane changer except at low ego-speed. ACTIVE: follow potential lane changer except when it decelerates strongly.
     * 
     */
    @XmlAttribute(name = "Cooperation")
    @XmlJavaTypeAdapter(CooperationAdapter.class)
    protected CooperationType cooperation;
    /**
     * Accept gap based on potential follower acceleration from car-following
     *           model. INFORMED: use headway parameters of potential follower. EGO_HEADWAY use own headway parameters.
     * 
     */
    @XmlAttribute(name = "GapAcceptance")
    @XmlJavaTypeAdapter(GapAcceptanceAdapter.class)
    protected GapAcceptanceType gapAcceptance;

    /**
     * Mandatory lane change incentives that increasingly overrule incongruent
     *             voluntary lane change incentives as the mandatory lane change desire increases.
     * 
     * @return
     *     possible object is
     *     {@link LmrsModel.MandatoryIncentives }
     *     
     */
    public LmrsModel.MandatoryIncentives getMandatoryIncentives() {
        return mandatoryIncentives;
    }

    /**
     * Sets the value of the mandatoryIncentives property.
     * 
     * @param value
     *     allowed object is
     *     {@link LmrsModel.MandatoryIncentives }
     *     
     * @see #getMandatoryIncentives()
     */
    public void setMandatoryIncentives(LmrsModel.MandatoryIncentives value) {
        this.mandatoryIncentives = value;
    }

    /**
     * Voluntary lane change incentives determine lane change desire up to the
     *             level that mandatory lane change desire is not active.
     * 
     * @return
     *     possible object is
     *     {@link LmrsModel.VoluntaryIncentives }
     *     
     */
    public LmrsModel.VoluntaryIncentives getVoluntaryIncentives() {
        return voluntaryIncentives;
    }

    /**
     * Sets the value of the voluntaryIncentives property.
     * 
     * @param value
     *     allowed object is
     *     {@link LmrsModel.VoluntaryIncentives }
     *     
     * @see #getVoluntaryIncentives()
     */
    public void setVoluntaryIncentives(LmrsModel.VoluntaryIncentives value) {
        this.voluntaryIncentives = value;
    }

    /**
     * Reasons that make drivers decelerate beyond regular car-following or
     *             keeping to their desired speed.
     * 
     * @return
     *     possible object is
     *     {@link LmrsModel.AccelerationIncentives }
     *     
     */
    public LmrsModel.AccelerationIncentives getAccelerationIncentives() {
        return accelerationIncentives;
    }

    /**
     * Sets the value of the accelerationIncentives property.
     * 
     * @param value
     *     allowed object is
     *     {@link LmrsModel.AccelerationIncentives }
     *     
     * @see #getAccelerationIncentives()
     */
    public void setAccelerationIncentives(LmrsModel.AccelerationIncentives value) {
        this.accelerationIncentives = value;
    }

    /**
     * Interactions to allow and to be allowed more speed.
     * 
     * @return
     *     possible object is
     *     {@link LmrsModel.SocialInteractions }
     *     
     */
    public LmrsModel.SocialInteractions getSocialInteractions() {
        return socialInteractions;
    }

    /**
     * Sets the value of the socialInteractions property.
     * 
     * @param value
     *     allowed object is
     *     {@link LmrsModel.SocialInteractions }
     *     
     * @see #getSocialInteractions()
     */
    public void setSocialInteractions(LmrsModel.SocialInteractions value) {
        this.socialInteractions = value;
    }

    /**
     * Gets the value of the perception property.
     * 
     * @return
     *     possible object is
     *     {@link LmrsModel.Perception }
     *     
     */
    public LmrsModel.Perception getPerception() {
        return perception;
    }

    /**
     * Sets the value of the perception property.
     * 
     * @param value
     *     allowed object is
     *     {@link LmrsModel.Perception }
     *     
     */
    public void setPerception(LmrsModel.Perception value) {
        this.perception = value;
    }

    /**
     * IDM: Intelligent Driver Model. IDM+: adaptation of IDM with maximum of
     *           free and car-following acceleration.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public CarFollowingModelType getCarFollowingModel() {
        return carFollowingModel;
    }

    /**
     * Sets the value of the carFollowingModel property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     * @see #getCarFollowingModel()
     */
    public void setCarFollowingModel(CarFollowingModelType value) {
        this.carFollowingModel = value;
    }

    /**
     * PASSIVE: follow first leader in target lane. PASSIVE_MOVING: keep moving
     *           below cooperation threshold. ALIGNGAP: align to middle of adjacent gap. ACTIVE: actively consider whether gaps can be
     *           reached in time (not advised).
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public SynchronizationType getSynchronization() {
        return synchronization;
    }

    /**
     * Sets the value of the synchronization property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     * @see #getSynchronization()
     */
    public void setSynchronization(SynchronizationType value) {
        this.synchronization = value;
    }

    /**
     * PASSIVE: follow potential lane changer. PASSIVE_MOVING: follow potential
     *           lane changer except at low ego-speed. ACTIVE: follow potential lane changer except when it decelerates strongly.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public CooperationType getCooperation() {
        return cooperation;
    }

    /**
     * Sets the value of the cooperation property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     * @see #getCooperation()
     */
    public void setCooperation(CooperationType value) {
        this.cooperation = value;
    }

    /**
     * Accept gap based on potential follower acceleration from car-following
     *           model. INFORMED: use headway parameters of potential follower. EGO_HEADWAY use own headway parameters.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public GapAcceptanceType getGapAcceptance() {
        return gapAcceptance;
    }

    /**
     * Sets the value of the gapAcceptance property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     * @see #getGapAcceptance()
     */
    public void setGapAcceptance(GapAcceptanceType value) {
        this.gapAcceptance = value;
    }


    /**
     * <p>Java class for anonymous complex type</p>.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.</p>
     * 
     * <pre>{@code
     * <complexType>
     *   <complexContent>
     *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       <attribute name="SpeedLimitTransitions" type="{http://www.opentrafficsim.org/ots}OnOff" />
     *       <attribute name="TrafficLights" type="{http://www.opentrafficsim.org/ots}OnOff" />
     *       <attribute name="Conflicts" type="{http://www.opentrafficsim.org/ots}OnOff" />
     *       <attribute name="NoSlowLaneOvertake" type="{http://www.opentrafficsim.org/ots}OnOff" />
     *     </restriction>
     *   </complexContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class AccelerationIncentives
        implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        /**
         * Makes drivers slow down before curves and speed bumps.
         * 
         */
        @XmlAttribute(name = "SpeedLimitTransitions")
        @XmlJavaTypeAdapter(OnOffAdapter.class)
        protected BooleanType speedLimitTransitions;
        /**
         * Makes drivers stop for traffic lights.
         * 
         */
        @XmlAttribute(name = "TrafficLights")
        @XmlJavaTypeAdapter(OnOffAdapter.class)
        protected BooleanType trafficLights;
        /**
         * Allows drivers to navigate intersection conflicts.
         * 
         */
        @XmlAttribute(name = "Conflicts")
        @XmlJavaTypeAdapter(OnOffAdapter.class)
        protected BooleanType conflicts;
        /**
         * Makes drivers apply limited deceleration to attempt not to overtake
         *                 vehicles in the faster lane.
         * 
         */
        @XmlAttribute(name = "NoSlowLaneOvertake")
        @XmlJavaTypeAdapter(OnOffAdapter.class)
        protected BooleanType noSlowLaneOvertake;

        /**
         * Makes drivers slow down before curves and speed bumps.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public BooleanType getSpeedLimitTransitions() {
            return speedLimitTransitions;
        }

        /**
         * Sets the value of the speedLimitTransitions property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         * @see #getSpeedLimitTransitions()
         */
        public void setSpeedLimitTransitions(BooleanType value) {
            this.speedLimitTransitions = value;
        }

        /**
         * Makes drivers stop for traffic lights.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public BooleanType getTrafficLights() {
            return trafficLights;
        }

        /**
         * Sets the value of the trafficLights property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         * @see #getTrafficLights()
         */
        public void setTrafficLights(BooleanType value) {
            this.trafficLights = value;
        }

        /**
         * Allows drivers to navigate intersection conflicts.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public BooleanType getConflicts() {
            return conflicts;
        }

        /**
         * Sets the value of the conflicts property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         * @see #getConflicts()
         */
        public void setConflicts(BooleanType value) {
            this.conflicts = value;
        }

        /**
         * Makes drivers apply limited deceleration to attempt not to overtake
         *                 vehicles in the faster lane.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public BooleanType getNoSlowLaneOvertake() {
            return noSlowLaneOvertake;
        }

        /**
         * Sets the value of the noSlowLaneOvertake property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         * @see #getNoSlowLaneOvertake()
         */
        public void setNoSlowLaneOvertake(BooleanType value) {
            this.noSlowLaneOvertake = value;
        }

    }


    /**
     * <p>Java class for anonymous complex type</p>.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.</p>
     * 
     * <pre>{@code
     * <complexType>
     *   <complexContent>
     *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       <attribute name="Route" type="{http://www.opentrafficsim.org/ots}OnOff" />
     *       <attribute name="GetInLane" type="{http://www.opentrafficsim.org/ots}OnOff" />
     *     </restriction>
     *   </complexContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class MandatoryIncentives
        implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        /**
         * Makes drivers follow their route along available infrastructure.
         * 
         */
        @XmlAttribute(name = "Route")
        @XmlJavaTypeAdapter(OnOffAdapter.class)
        protected BooleanType route;
        /**
         * Makes drivers change to the lane for their route once traffic there
         *                 is slow.
         * 
         */
        @XmlAttribute(name = "GetInLane")
        @XmlJavaTypeAdapter(OnOffAdapter.class)
        protected BooleanType getInLane;

        /**
         * Makes drivers follow their route along available infrastructure.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public BooleanType getRoute() {
            return route;
        }

        /**
         * Sets the value of the route property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         * @see #getRoute()
         */
        public void setRoute(BooleanType value) {
            this.route = value;
        }

        /**
         * Makes drivers change to the lane for their route once traffic there
         *                 is slow.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public BooleanType getGetInLane() {
            return getInLane;
        }

        /**
         * Sets the value of the getInLane property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         * @see #getGetInLane()
         */
        public void setGetInLane(BooleanType value) {
            this.getInLane = value;
        }

    }


    /**
     * <p>Java class for anonymous complex type</p>.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.</p>
     * 
     * <pre>{@code
     * <complexType>
     *   <complexContent>
     *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       <choice>
     *         <element name="None" type="{http://www.opentrafficsim.org/ots}EmptyType"/>
     *         <element name="FullerAttentionMatrix" type="{http://www.opentrafficsim.org/ots}FullerAttentionMatrix"/>
     *         <element name="FullerAnticipationReliance" type="{http://www.opentrafficsim.org/ots}FullerAnticipationReliance"/>
     *         <element name="FullerSummative" type="{http://www.opentrafficsim.org/ots}FullerSummative"/>
     *       </choice>
     *     </restriction>
     *   </complexContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "none",
        "fullerAttentionMatrix",
        "fullerAnticipationReliance",
        "fullerSummative"
    })
    public static class Perception
        implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlElement(name = "None")
        protected EmptyType none;
        @XmlElement(name = "FullerAttentionMatrix")
        protected FullerAttentionMatrix fullerAttentionMatrix;
        @XmlElement(name = "FullerAnticipationReliance")
        protected FullerAnticipationReliance fullerAnticipationReliance;
        @XmlElement(name = "FullerSummative")
        protected FullerSummative fullerSummative;

        /**
         * Gets the value of the none property.
         * 
         * @return
         *     possible object is
         *     {@link EmptyType }
         *     
         */
        public EmptyType getNone() {
            return none;
        }

        /**
         * Sets the value of the none property.
         * 
         * @param value
         *     allowed object is
         *     {@link EmptyType }
         *     
         */
        public void setNone(EmptyType value) {
            this.none = value;
        }

        /**
         * Gets the value of the fullerAttentionMatrix property.
         * 
         * @return
         *     possible object is
         *     {@link FullerAttentionMatrix }
         *     
         */
        public FullerAttentionMatrix getFullerAttentionMatrix() {
            return fullerAttentionMatrix;
        }

        /**
         * Sets the value of the fullerAttentionMatrix property.
         * 
         * @param value
         *     allowed object is
         *     {@link FullerAttentionMatrix }
         *     
         */
        public void setFullerAttentionMatrix(FullerAttentionMatrix value) {
            this.fullerAttentionMatrix = value;
        }

        /**
         * Gets the value of the fullerAnticipationReliance property.
         * 
         * @return
         *     possible object is
         *     {@link FullerAnticipationReliance }
         *     
         */
        public FullerAnticipationReliance getFullerAnticipationReliance() {
            return fullerAnticipationReliance;
        }

        /**
         * Sets the value of the fullerAnticipationReliance property.
         * 
         * @param value
         *     allowed object is
         *     {@link FullerAnticipationReliance }
         *     
         */
        public void setFullerAnticipationReliance(FullerAnticipationReliance value) {
            this.fullerAnticipationReliance = value;
        }

        /**
         * Gets the value of the fullerSummative property.
         * 
         * @return
         *     possible object is
         *     {@link FullerSummative }
         *     
         */
        public FullerSummative getFullerSummative() {
            return fullerSummative;
        }

        /**
         * Sets the value of the fullerSummative property.
         * 
         * @param value
         *     allowed object is
         *     {@link FullerSummative }
         *     
         */
        public void setFullerSummative(FullerSummative value) {
            this.fullerSummative = value;
        }

    }


    /**
     * <p>Java class for anonymous complex type</p>.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.</p>
     * 
     * <pre>{@code
     * <complexType>
     *   <complexContent>
     *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       <attribute name="SocialPressure" type="{http://www.opentrafficsim.org/ots}OnOff" />
     *       <attribute name="Tailgating" type="{http://www.opentrafficsim.org/ots}OnOff" />
     *       <attribute name="LaneChanges" type="{http://www.opentrafficsim.org/ots}OnOff" />
     *       <attribute name="Speed" type="{http://www.opentrafficsim.org/ots}OnOff" />
     *     </restriction>
     *   </complexContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class SocialInteractions
        implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        /**
         * Makes drivers assert social pressure on (potential) leaders.
         * 
         */
        @XmlAttribute(name = "SocialPressure")
        @XmlJavaTypeAdapter(OnOffAdapter.class)
        protected BooleanType socialPressure;
        /**
         * Makes drivers reduce their headway as an indication that they want
         *                 to drive faster. This can be by a small or a large amount.
         * 
         */
        @XmlAttribute(name = "Tailgating")
        @XmlJavaTypeAdapter(OnOffAdapter.class)
        protected BooleanType tailgating;
        /**
         * Makes drivers change lane to the slower lane for faster drivers,
         *                 when possible, or delays a lane change to the faster lane.
         * 
         */
        @XmlAttribute(name = "LaneChanges")
        @XmlJavaTypeAdapter(OnOffAdapter.class)
        protected BooleanType laneChanges;
        /**
         * Makes drivers increase their speed when being tailgated.
         * 
         */
        @XmlAttribute(name = "Speed")
        @XmlJavaTypeAdapter(OnOffAdapter.class)
        protected BooleanType speed;

        /**
         * Makes drivers assert social pressure on (potential) leaders.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public BooleanType getSocialPressure() {
            return socialPressure;
        }

        /**
         * Sets the value of the socialPressure property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         * @see #getSocialPressure()
         */
        public void setSocialPressure(BooleanType value) {
            this.socialPressure = value;
        }

        /**
         * Makes drivers reduce their headway as an indication that they want
         *                 to drive faster. This can be by a small or a large amount.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public BooleanType getTailgating() {
            return tailgating;
        }

        /**
         * Sets the value of the tailgating property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         * @see #getTailgating()
         */
        public void setTailgating(BooleanType value) {
            this.tailgating = value;
        }

        /**
         * Makes drivers change lane to the slower lane for faster drivers,
         *                 when possible, or delays a lane change to the faster lane.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public BooleanType getLaneChanges() {
            return laneChanges;
        }

        /**
         * Sets the value of the laneChanges property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         * @see #getLaneChanges()
         */
        public void setLaneChanges(BooleanType value) {
            this.laneChanges = value;
        }

        /**
         * Makes drivers increase their speed when being tailgated.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public BooleanType getSpeed() {
            return speed;
        }

        /**
         * Sets the value of the speed property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         * @see #getSpeed()
         */
        public void setSpeed(BooleanType value) {
            this.speed = value;
        }

    }


    /**
     * <p>Java class for anonymous complex type</p>.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.</p>
     * 
     * <pre>{@code
     * <complexType>
     *   <complexContent>
     *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       <attribute name="Speed" type="{http://www.opentrafficsim.org/ots}OnOff" />
     *       <attribute name="Keep" type="{http://www.opentrafficsim.org/ots}OnOff" />
     *       <attribute name="Courtesy" type="{http://www.opentrafficsim.org/ots}OnOff" />
     *       <attribute name="Queue" type="{http://www.opentrafficsim.org/ots}OnOff" />
     *       <attribute name="StayOnSlowLanes" type="{http://www.opentrafficsim.org/ots}OnOff" />
     *     </restriction>
     *   </complexContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class VoluntaryIncentives
        implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        /**
         * Makes drivers change lane to maintain or achieve their desired
         *                 speed.
         * 
         */
        @XmlAttribute(name = "Speed")
        @XmlJavaTypeAdapter(OnOffAdapter.class)
        protected BooleanType speed;
        /**
         * Makes drivers keep to the slow lane if there is sufficient space.
         * 
         */
        @XmlAttribute(name = "Keep")
        @XmlJavaTypeAdapter(OnOffAdapter.class)
        protected BooleanType keep;
        /**
         * Makes drivers change lane to make space for a lane change of someone
         *                 else.
         * 
         */
        @XmlAttribute(name = "Courtesy")
        @XmlJavaTypeAdapter(OnOffAdapter.class)
        protected BooleanType courtesy;
        /**
         * Makes drivers join the shortest queue near intersections.
         * 
         */
        @XmlAttribute(name = "Queue")
        @XmlJavaTypeAdapter(OnOffAdapter.class)
        protected BooleanType queue;
        /**
         * Makes drivers stay on the two slowest lanes (e.g. rules for trucks).
         * 
         */
        @XmlAttribute(name = "StayOnSlowLanes")
        @XmlJavaTypeAdapter(OnOffAdapter.class)
        protected BooleanType stayOnSlowLanes;

        /**
         * Makes drivers change lane to maintain or achieve their desired
         *                 speed.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public BooleanType getSpeed() {
            return speed;
        }

        /**
         * Sets the value of the speed property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         * @see #getSpeed()
         */
        public void setSpeed(BooleanType value) {
            this.speed = value;
        }

        /**
         * Makes drivers keep to the slow lane if there is sufficient space.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public BooleanType getKeep() {
            return keep;
        }

        /**
         * Sets the value of the keep property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         * @see #getKeep()
         */
        public void setKeep(BooleanType value) {
            this.keep = value;
        }

        /**
         * Makes drivers change lane to make space for a lane change of someone
         *                 else.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public BooleanType getCourtesy() {
            return courtesy;
        }

        /**
         * Sets the value of the courtesy property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         * @see #getCourtesy()
         */
        public void setCourtesy(BooleanType value) {
            this.courtesy = value;
        }

        /**
         * Makes drivers join the shortest queue near intersections.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public BooleanType getQueue() {
            return queue;
        }

        /**
         * Sets the value of the queue property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         * @see #getQueue()
         */
        public void setQueue(BooleanType value) {
            this.queue = value;
        }

        /**
         * Makes drivers stay on the two slowest lanes (e.g. rules for trucks).
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public BooleanType getStayOnSlowLanes() {
            return stayOnSlowLanes;
        }

        /**
         * Sets the value of the stayOnSlowLanes property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         * @see #getStayOnSlowLanes()
         */
        public void setStayOnSlowLanes(BooleanType value) {
            this.stayOnSlowLanes = value;
        }

    }

}
