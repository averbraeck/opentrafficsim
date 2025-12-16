
package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opentrafficsim.xml.bindings.ArcDirectionAdapter;
import org.opentrafficsim.xml.bindings.BooleanAdapter;
import org.opentrafficsim.xml.bindings.DoublePositiveAdapter;
import org.opentrafficsim.xml.bindings.LaneKeepingPolicyAdapter;
import org.opentrafficsim.xml.bindings.LengthAdapter;
import org.opentrafficsim.xml.bindings.LinearDensityAdapter;
import org.opentrafficsim.xml.bindings.Point2dAdapter;
import org.opentrafficsim.xml.bindings.PositiveLengthAdapter;
import org.opentrafficsim.xml.bindings.PriorityAdapter;
import org.opentrafficsim.xml.bindings.StringAdapter;
import org.opentrafficsim.xml.bindings.StripeLateralSyncAdapter;
import org.opentrafficsim.xml.bindings.types.ArcDirectionType;
import org.opentrafficsim.xml.bindings.types.BooleanType;
import org.opentrafficsim.xml.bindings.types.DoubleType;
import org.opentrafficsim.xml.bindings.types.LaneKeepingPolicyType;
import org.opentrafficsim.xml.bindings.types.LinearDensityType;
import org.opentrafficsim.xml.bindings.types.Point2dType;
import org.opentrafficsim.xml.bindings.types.PriorityType;
import org.opentrafficsim.xml.bindings.types.StringType;
import org.opentrafficsim.xml.bindings.types.StripeLateralSyncType;


/**
 * <p>Java class for anonymous complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType>
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *         <choice>
 *           <element name="Straight" type="{http://www.opentrafficsim.org/ots}EmptyType"/>
 *           <element name="Bezier">
 *             <complexType>
 *               <complexContent>
 *                 <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                   <sequence>
 *                     <element name="Flattener" type="{http://www.opentrafficsim.org/ots}FlattenerType" minOccurs="0"/>
 *                   </sequence>
 *                   <attribute name="Shape" type="{http://www.opentrafficsim.org/ots}DoublePositive" default="1.0" />
 *                   <attribute name="Weighted" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *                 </restriction>
 *               </complexContent>
 *             </complexType>
 *           </element>
 *           <element name="Clothoid">
 *             <complexType>
 *               <complexContent>
 *                 <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                   <sequence>
 *                     <choice>
 *                       <element name="Interpolated" type="{http://www.opentrafficsim.org/ots}EmptyType"/>
 *                       <sequence>
 *                         <element name="Length" type="{http://www.opentrafficsim.org/ots}LengthType"/>
 *                         <element name="StartCurvature" type="{http://www.opentrafficsim.org/ots}LinearDensityType"/>
 *                         <element name="EndCurvature" type="{http://www.opentrafficsim.org/ots}LinearDensityType"/>
 *                       </sequence>
 *                       <sequence>
 *                         <element name="A" type="{http://www.opentrafficsim.org/ots}LengthType"/>
 *                         <element name="StartCurvature" type="{http://www.opentrafficsim.org/ots}LinearDensityType"/>
 *                         <element name="EndCurvature" type="{http://www.opentrafficsim.org/ots}LinearDensityType"/>
 *                       </sequence>
 *                     </choice>
 *                     <sequence>
 *                       <element name="Flattener" type="{http://www.opentrafficsim.org/ots}FlattenerType" minOccurs="0"/>
 *                     </sequence>
 *                   </sequence>
 *                   <attribute name="EndElevation" type="{http://www.opentrafficsim.org/ots}PositiveLengthType" />
 *                 </restriction>
 *               </complexContent>
 *             </complexType>
 *           </element>
 *           <element name="Arc">
 *             <complexType>
 *               <complexContent>
 *                 <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                   <sequence>
 *                     <element name="Flattener" type="{http://www.opentrafficsim.org/ots}FlattenerType" minOccurs="0"/>
 *                   </sequence>
 *                   <attribute name="Radius" use="required" type="{http://www.opentrafficsim.org/ots}PositiveLengthType" />
 *                   <attribute name="Direction" use="required" type="{http://www.opentrafficsim.org/ots}ArcDirectionType" />
 *                 </restriction>
 *               </complexContent>
 *             </complexType>
 *           </element>
 *           <element name="Polyline">
 *             <complexType>
 *               <complexContent>
 *                 <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                   <sequence maxOccurs="unbounded">
 *                     <element name="Coordinate" type="{http://www.opentrafficsim.org/ots}CoordinateType"/>
 *                   </sequence>
 *                 </restriction>
 *               </complexContent>
 *             </complexType>
 *           </element>
 *         </choice>
 *         <choice>
 *           <element name="RoadLayout">
 *             <complexType>
 *               <complexContent>
 *                 <extension base="{http://www.opentrafficsim.org/ots}BasicRoadLayout">
 *                 </extension>
 *               </complexContent>
 *             </complexType>
 *           </element>
 *           <sequence>
 *             <element name="DefinedLayout" type="{http://www.opentrafficsim.org/ots}string"/>
 *             <element name="LaneOverride" maxOccurs="unbounded" minOccurs="0">
 *               <complexType>
 *                 <complexContent>
 *                   <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                     <sequence>
 *                       <element ref="{http://www.opentrafficsim.org/ots}SpeedLimit" maxOccurs="unbounded" minOccurs="0"/>
 *                     </sequence>
 *                     <attribute name="Lane" use="required" type="{http://www.opentrafficsim.org/ots}string" />
 *                   </restriction>
 *                 </complexContent>
 *               </complexType>
 *             </element>
 *             <element name="StripeOverride" maxOccurs="unbounded" minOccurs="0">
 *               <complexType>
 *                 <complexContent>
 *                   <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                     <sequence>
 *                       <element name="Elements" type="{http://www.opentrafficsim.org/ots}StripeElements" minOccurs="0"/>
 *                       <element name="DashOffset" type="{http://www.opentrafficsim.org/ots}DashOffset" minOccurs="0"/>
 *                       <element name="Compatibility" type="{http://www.opentrafficsim.org/ots}StripeCompatibility" maxOccurs="unbounded" minOccurs="0"/>
 *                     </sequence>
 *                     <attribute name="LeftChangeLane" type="{http://www.opentrafficsim.org/ots}boolean" />
 *                     <attribute name="RightChangeLane" type="{http://www.opentrafficsim.org/ots}boolean" />
 *                     <attribute name="LateralSync" type="{http://www.opentrafficsim.org/ots}LateralSync" />
 *                     <attribute name="Stripe" use="required" type="{http://www.opentrafficsim.org/ots}string" />
 *                   </restriction>
 *                 </complexContent>
 *               </complexType>
 *             </element>
 *           </sequence>
 *         </choice>
 *         <element name="TrafficLight" type="{http://www.opentrafficsim.org/ots}TrafficLightType" maxOccurs="unbounded" minOccurs="0"/>
 *         <element name="TrafficLightSensor" type="{http://www.opentrafficsim.org/ots}TrafficLightDetectorType" maxOccurs="unbounded" minOccurs="0"/>
 *       </sequence>
 *       <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}IdType" />
 *       <attribute name="Type" use="required" type="{http://www.opentrafficsim.org/ots}string" />
 *       <attribute name="NodeStart" use="required" type="{http://www.opentrafficsim.org/ots}string" />
 *       <attribute name="NodeEnd" use="required" type="{http://www.opentrafficsim.org/ots}string" />
 *       <attribute name="OffsetStart" type="{http://www.opentrafficsim.org/ots}LengthType" default="0.0 m" />
 *       <attribute name="OffsetEnd" type="{http://www.opentrafficsim.org/ots}LengthType" default="0.0 m" />
 *       <attribute name="LaneKeeping" type="{http://www.opentrafficsim.org/ots}LaneKeepingType" default="KEEPRIGHT" />
 *       <attribute name="Priority" type="{http://www.opentrafficsim.org/ots}PriorityType" />
 *       <attribute name="ConflictId" type="{http://www.opentrafficsim.org/ots}string" />
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "straight",
    "bezier",
    "clothoid",
    "arc",
    "polyline",
    "roadLayout",
    "definedLayout",
    "laneOverride",
    "stripeOverride",
    "trafficLight",
    "trafficLightSensor"
})
@XmlRootElement(name = "Link")
@SuppressWarnings("all") public class Link
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    @XmlElement(name = "Straight")
    protected EmptyType straight;
    @XmlElement(name = "Bezier")
    protected Link.Bezier bezier;
    @XmlElement(name = "Clothoid")
    protected Link.Clothoid clothoid;
    @XmlElement(name = "Arc")
    protected Link.Arc arc;
    @XmlElement(name = "Polyline")
    protected Link.Polyline polyline;
    @XmlElement(name = "RoadLayout")
    protected Link.RoadLayout roadLayout;
    @XmlElement(name = "DefinedLayout", type = String.class)
    @XmlJavaTypeAdapter(StringAdapter.class)
    protected StringType definedLayout;
    @XmlElement(name = "LaneOverride")
    protected List<Link.LaneOverride> laneOverride;
    @XmlElement(name = "StripeOverride")
    protected List<Link.StripeOverride> stripeOverride;
    @XmlElement(name = "TrafficLight")
    protected List<TrafficLightType> trafficLight;
    @XmlElement(name = "TrafficLightSensor")
    protected List<TrafficLightDetectorType> trafficLightSensor;
    @XmlAttribute(name = "Id", required = true)
    protected String id;
    @XmlAttribute(name = "Type", required = true)
    @XmlJavaTypeAdapter(StringAdapter.class)
    protected StringType type;
    @XmlAttribute(name = "NodeStart", required = true)
    @XmlJavaTypeAdapter(StringAdapter.class)
    protected StringType nodeStart;
    @XmlAttribute(name = "NodeEnd", required = true)
    @XmlJavaTypeAdapter(StringAdapter.class)
    protected StringType nodeEnd;
    @XmlAttribute(name = "OffsetStart")
    @XmlJavaTypeAdapter(LengthAdapter.class)
    protected org.opentrafficsim.xml.bindings.types.LengthType offsetStart;
    @XmlAttribute(name = "OffsetEnd")
    @XmlJavaTypeAdapter(LengthAdapter.class)
    protected org.opentrafficsim.xml.bindings.types.LengthType offsetEnd;
    @XmlAttribute(name = "LaneKeeping")
    @XmlJavaTypeAdapter(LaneKeepingPolicyAdapter.class)
    protected LaneKeepingPolicyType laneKeeping;
    @XmlAttribute(name = "Priority")
    @XmlJavaTypeAdapter(PriorityAdapter.class)
    protected PriorityType priority;
    @XmlAttribute(name = "ConflictId")
    @XmlJavaTypeAdapter(StringAdapter.class)
    protected StringType conflictId;

    /**
     * Gets the value of the straight property.
     * 
     * @return
     *     possible object is
     *     {@link EmptyType }
     *     
     */
    public EmptyType getStraight() {
        return straight;
    }

    /**
     * Sets the value of the straight property.
     * 
     * @param value
     *     allowed object is
     *     {@link EmptyType }
     *     
     */
    public void setStraight(EmptyType value) {
        this.straight = value;
    }

    /**
     * Gets the value of the bezier property.
     * 
     * @return
     *     possible object is
     *     {@link Link.Bezier }
     *     
     */
    public Link.Bezier getBezier() {
        return bezier;
    }

    /**
     * Sets the value of the bezier property.
     * 
     * @param value
     *     allowed object is
     *     {@link Link.Bezier }
     *     
     */
    public void setBezier(Link.Bezier value) {
        this.bezier = value;
    }

    /**
     * Gets the value of the clothoid property.
     * 
     * @return
     *     possible object is
     *     {@link Link.Clothoid }
     *     
     */
    public Link.Clothoid getClothoid() {
        return clothoid;
    }

    /**
     * Sets the value of the clothoid property.
     * 
     * @param value
     *     allowed object is
     *     {@link Link.Clothoid }
     *     
     */
    public void setClothoid(Link.Clothoid value) {
        this.clothoid = value;
    }

    /**
     * Gets the value of the arc property.
     * 
     * @return
     *     possible object is
     *     {@link Link.Arc }
     *     
     */
    public Link.Arc getArc() {
        return arc;
    }

    /**
     * Sets the value of the arc property.
     * 
     * @param value
     *     allowed object is
     *     {@link Link.Arc }
     *     
     */
    public void setArc(Link.Arc value) {
        this.arc = value;
    }

    /**
     * Gets the value of the polyline property.
     * 
     * @return
     *     possible object is
     *     {@link Link.Polyline }
     *     
     */
    public Link.Polyline getPolyline() {
        return polyline;
    }

    /**
     * Sets the value of the polyline property.
     * 
     * @param value
     *     allowed object is
     *     {@link Link.Polyline }
     *     
     */
    public void setPolyline(Link.Polyline value) {
        this.polyline = value;
    }

    /**
     * Gets the value of the roadLayout property.
     * 
     * @return
     *     possible object is
     *     {@link Link.RoadLayout }
     *     
     */
    public Link.RoadLayout getRoadLayout() {
        return roadLayout;
    }

    /**
     * Sets the value of the roadLayout property.
     * 
     * @param value
     *     allowed object is
     *     {@link Link.RoadLayout }
     *     
     */
    public void setRoadLayout(Link.RoadLayout value) {
        this.roadLayout = value;
    }

    /**
     * Gets the value of the definedLayout property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public StringType getDefinedLayout() {
        return definedLayout;
    }

    /**
     * Sets the value of the definedLayout property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDefinedLayout(StringType value) {
        this.definedLayout = value;
    }

    /**
     * Gets the value of the laneOverride property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the laneOverride property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getLaneOverride().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Link.LaneOverride }
     * </p>
     * 
     * 
     * @return
     *     The value of the laneOverride property.
     */
    public List<Link.LaneOverride> getLaneOverride() {
        if (laneOverride == null) {
            laneOverride = new ArrayList<>();
        }
        return this.laneOverride;
    }

    /**
     * Gets the value of the stripeOverride property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the stripeOverride property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getStripeOverride().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Link.StripeOverride }
     * </p>
     * 
     * 
     * @return
     *     The value of the stripeOverride property.
     */
    public List<Link.StripeOverride> getStripeOverride() {
        if (stripeOverride == null) {
            stripeOverride = new ArrayList<>();
        }
        return this.stripeOverride;
    }

    /**
     * Gets the value of the trafficLight property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the trafficLight property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getTrafficLight().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TrafficLightType }
     * </p>
     * 
     * 
     * @return
     *     The value of the trafficLight property.
     */
    public List<TrafficLightType> getTrafficLight() {
        if (trafficLight == null) {
            trafficLight = new ArrayList<>();
        }
        return this.trafficLight;
    }

    /**
     * Gets the value of the trafficLightSensor property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the trafficLightSensor property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getTrafficLightSensor().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TrafficLightDetectorType }
     * </p>
     * 
     * 
     * @return
     *     The value of the trafficLightSensor property.
     */
    public List<TrafficLightDetectorType> getTrafficLightSensor() {
        if (trafficLightSensor == null) {
            trafficLightSensor = new ArrayList<>();
        }
        return this.trafficLightSensor;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public StringType getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setType(StringType value) {
        this.type = value;
    }

    /**
     * Gets the value of the nodeStart property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public StringType getNodeStart() {
        return nodeStart;
    }

    /**
     * Sets the value of the nodeStart property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNodeStart(StringType value) {
        this.nodeStart = value;
    }

    /**
     * Gets the value of the nodeEnd property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public StringType getNodeEnd() {
        return nodeEnd;
    }

    /**
     * Sets the value of the nodeEnd property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNodeEnd(StringType value) {
        this.nodeEnd = value;
    }

    /**
     * Gets the value of the offsetStart property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public org.opentrafficsim.xml.bindings.types.LengthType getOffsetStart() {
        if (offsetStart == null) {
            return new LengthAdapter().unmarshal("0.0 m");
        } else {
            return offsetStart;
        }
    }

    /**
     * Sets the value of the offsetStart property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOffsetStart(org.opentrafficsim.xml.bindings.types.LengthType value) {
        this.offsetStart = value;
    }

    /**
     * Gets the value of the offsetEnd property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public org.opentrafficsim.xml.bindings.types.LengthType getOffsetEnd() {
        if (offsetEnd == null) {
            return new LengthAdapter().unmarshal("0.0 m");
        } else {
            return offsetEnd;
        }
    }

    /**
     * Sets the value of the offsetEnd property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOffsetEnd(org.opentrafficsim.xml.bindings.types.LengthType value) {
        this.offsetEnd = value;
    }

    /**
     * Gets the value of the laneKeeping property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public LaneKeepingPolicyType getLaneKeeping() {
        if (laneKeeping == null) {
            return new LaneKeepingPolicyAdapter().unmarshal("KEEPRIGHT");
        } else {
            return laneKeeping;
        }
    }

    /**
     * Sets the value of the laneKeeping property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLaneKeeping(LaneKeepingPolicyType value) {
        this.laneKeeping = value;
    }

    /**
     * Gets the value of the priority property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public PriorityType getPriority() {
        return priority;
    }

    /**
     * Sets the value of the priority property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPriority(PriorityType value) {
        this.priority = value;
    }

    /**
     * Gets the value of the conflictId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public StringType getConflictId() {
        return conflictId;
    }

    /**
     * Sets the value of the conflictId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setConflictId(StringType value) {
        this.conflictId = value;
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
     *       <sequence>
     *         <element name="Flattener" type="{http://www.opentrafficsim.org/ots}FlattenerType" minOccurs="0"/>
     *       </sequence>
     *       <attribute name="Radius" use="required" type="{http://www.opentrafficsim.org/ots}PositiveLengthType" />
     *       <attribute name="Direction" use="required" type="{http://www.opentrafficsim.org/ots}ArcDirectionType" />
     *     </restriction>
     *   </complexContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "flattener"
    })
    public static class Arc
        implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlElement(name = "Flattener")
        protected FlattenerType flattener;
        @XmlAttribute(name = "Radius", required = true)
        @XmlJavaTypeAdapter(PositiveLengthAdapter.class)
        protected org.opentrafficsim.xml.bindings.types.LengthType radius;
        @XmlAttribute(name = "Direction", required = true)
        @XmlJavaTypeAdapter(ArcDirectionAdapter.class)
        protected ArcDirectionType direction;

        /**
         * Gets the value of the flattener property.
         * 
         * @return
         *     possible object is
         *     {@link FlattenerType }
         *     
         */
        public FlattenerType getFlattener() {
            return flattener;
        }

        /**
         * Sets the value of the flattener property.
         * 
         * @param value
         *     allowed object is
         *     {@link FlattenerType }
         *     
         */
        public void setFlattener(FlattenerType value) {
            this.flattener = value;
        }

        /**
         * Gets the value of the radius property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public org.opentrafficsim.xml.bindings.types.LengthType getRadius() {
            return radius;
        }

        /**
         * Sets the value of the radius property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setRadius(org.opentrafficsim.xml.bindings.types.LengthType value) {
            this.radius = value;
        }

        /**
         * Gets the value of the direction property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public ArcDirectionType getDirection() {
            return direction;
        }

        /**
         * Sets the value of the direction property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setDirection(ArcDirectionType value) {
            this.direction = value;
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
     *       <sequence>
     *         <element name="Flattener" type="{http://www.opentrafficsim.org/ots}FlattenerType" minOccurs="0"/>
     *       </sequence>
     *       <attribute name="Shape" type="{http://www.opentrafficsim.org/ots}DoublePositive" default="1.0" />
     *       <attribute name="Weighted" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
     *     </restriction>
     *   </complexContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "flattener"
    })
    public static class Bezier
        implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlElement(name = "Flattener")
        protected FlattenerType flattener;
        @XmlAttribute(name = "Shape")
        @XmlJavaTypeAdapter(DoublePositiveAdapter.class)
        protected DoubleType shape;
        @XmlAttribute(name = "Weighted")
        protected Boolean weighted;

        /**
         * Gets the value of the flattener property.
         * 
         * @return
         *     possible object is
         *     {@link FlattenerType }
         *     
         */
        public FlattenerType getFlattener() {
            return flattener;
        }

        /**
         * Sets the value of the flattener property.
         * 
         * @param value
         *     allowed object is
         *     {@link FlattenerType }
         *     
         */
        public void setFlattener(FlattenerType value) {
            this.flattener = value;
        }

        /**
         * Gets the value of the shape property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public DoubleType getShape() {
            if (shape == null) {
                return new DoublePositiveAdapter().unmarshal("1.0");
            } else {
                return shape;
            }
        }

        /**
         * Sets the value of the shape property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setShape(DoubleType value) {
            this.shape = value;
        }

        /**
         * Gets the value of the weighted property.
         * 
         * @return
         *     possible object is
         *     {@link Boolean }
         *     
         */
        public boolean isWeighted() {
            if (weighted == null) {
                return false;
            } else {
                return weighted;
            }
        }

        /**
         * Sets the value of the weighted property.
         * 
         * @param value
         *     allowed object is
         *     {@link Boolean }
         *     
         */
        public void setWeighted(Boolean value) {
            this.weighted = value;
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
     *       <sequence>
     *         <choice>
     *           <element name="Interpolated" type="{http://www.opentrafficsim.org/ots}EmptyType"/>
     *           <sequence>
     *             <element name="Length" type="{http://www.opentrafficsim.org/ots}LengthType"/>
     *             <element name="StartCurvature" type="{http://www.opentrafficsim.org/ots}LinearDensityType"/>
     *             <element name="EndCurvature" type="{http://www.opentrafficsim.org/ots}LinearDensityType"/>
     *           </sequence>
     *           <sequence>
     *             <element name="A" type="{http://www.opentrafficsim.org/ots}LengthType"/>
     *             <element name="StartCurvature" type="{http://www.opentrafficsim.org/ots}LinearDensityType"/>
     *             <element name="EndCurvature" type="{http://www.opentrafficsim.org/ots}LinearDensityType"/>
     *           </sequence>
     *         </choice>
     *         <sequence>
     *           <element name="Flattener" type="{http://www.opentrafficsim.org/ots}FlattenerType" minOccurs="0"/>
     *         </sequence>
     *       </sequence>
     *       <attribute name="EndElevation" type="{http://www.opentrafficsim.org/ots}PositiveLengthType" />
     *     </restriction>
     *   </complexContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "interpolated",
        "length",
        "startCurvature",
        "endCurvature",
        "a",
        "flattener"
    })
    public static class Clothoid
        implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlElement(name = "Interpolated")
        protected List<EmptyType> interpolated;
        @XmlElement(name = "Length", type = String.class)
        @XmlJavaTypeAdapter(LengthAdapter.class)
        protected List<org.opentrafficsim.xml.bindings.types.LengthType> length;
        @XmlElement(name = "StartCurvature", type = String.class)
        @XmlJavaTypeAdapter(LinearDensityAdapter.class)
        protected List<LinearDensityType> startCurvature;
        @XmlElement(name = "EndCurvature", type = String.class)
        @XmlJavaTypeAdapter(LinearDensityAdapter.class)
        protected List<LinearDensityType> endCurvature;
        @XmlElement(name = "A", type = String.class)
        @XmlJavaTypeAdapter(LengthAdapter.class)
        protected List<org.opentrafficsim.xml.bindings.types.LengthType> a;
        @XmlElement(name = "Flattener")
        protected List<FlattenerType> flattener;
        @XmlAttribute(name = "EndElevation")
        @XmlJavaTypeAdapter(PositiveLengthAdapter.class)
        protected org.opentrafficsim.xml.bindings.types.LengthType endElevation;

        /**
         * Gets the value of the interpolated property.
         * 
         * <p>This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the interpolated property.</p>
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * </p>
         * <pre>
         * getInterpolated().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link EmptyType }
         * </p>
         * 
         * 
         * @return
         *     The value of the interpolated property.
         */
        public List<EmptyType> getInterpolated() {
            if (interpolated == null) {
                interpolated = new ArrayList<>();
            }
            return this.interpolated;
        }

        /**
         * Gets the value of the length property.
         * 
         * <p>This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the length property.</p>
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * </p>
         * <pre>
         * getLength().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link String }
         * </p>
         * 
         * 
         * @return
         *     The value of the length property.
         */
        public List<org.opentrafficsim.xml.bindings.types.LengthType> getLength() {
            if (length == null) {
                length = new ArrayList<>();
            }
            return this.length;
        }

        /**
         * Gets the value of the startCurvature property.
         * 
         * <p>This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the startCurvature property.</p>
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * </p>
         * <pre>
         * getStartCurvature().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link String }
         * </p>
         * 
         * 
         * @return
         *     The value of the startCurvature property.
         */
        public List<LinearDensityType> getStartCurvature() {
            if (startCurvature == null) {
                startCurvature = new ArrayList<>();
            }
            return this.startCurvature;
        }

        /**
         * Gets the value of the endCurvature property.
         * 
         * <p>This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the endCurvature property.</p>
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * </p>
         * <pre>
         * getEndCurvature().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link String }
         * </p>
         * 
         * 
         * @return
         *     The value of the endCurvature property.
         */
        public List<LinearDensityType> getEndCurvature() {
            if (endCurvature == null) {
                endCurvature = new ArrayList<>();
            }
            return this.endCurvature;
        }

        /**
         * Gets the value of the a property.
         * 
         * <p>This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the a property.</p>
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * </p>
         * <pre>
         * getA().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link String }
         * </p>
         * 
         * 
         * @return
         *     The value of the a property.
         */
        public List<org.opentrafficsim.xml.bindings.types.LengthType> getA() {
            if (a == null) {
                a = new ArrayList<>();
            }
            return this.a;
        }

        /**
         * Gets the value of the flattener property.
         * 
         * <p>This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the flattener property.</p>
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * </p>
         * <pre>
         * getFlattener().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link FlattenerType }
         * </p>
         * 
         * 
         * @return
         *     The value of the flattener property.
         */
        public List<FlattenerType> getFlattener() {
            if (flattener == null) {
                flattener = new ArrayList<>();
            }
            return this.flattener;
        }

        /**
         * Gets the value of the endElevation property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public org.opentrafficsim.xml.bindings.types.LengthType getEndElevation() {
            return endElevation;
        }

        /**
         * Sets the value of the endElevation property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setEndElevation(org.opentrafficsim.xml.bindings.types.LengthType value) {
            this.endElevation = value;
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
     *       <sequence>
     *         <element ref="{http://www.opentrafficsim.org/ots}SpeedLimit" maxOccurs="unbounded" minOccurs="0"/>
     *       </sequence>
     *       <attribute name="Lane" use="required" type="{http://www.opentrafficsim.org/ots}string" />
     *     </restriction>
     *   </complexContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "speedLimit"
    })
    public static class LaneOverride
        implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlElement(name = "SpeedLimit")
        protected List<SpeedLimit> speedLimit;
        @XmlAttribute(name = "Lane", required = true)
        @XmlJavaTypeAdapter(StringAdapter.class)
        protected StringType lane;

        /**
         * Gets the value of the speedLimit property.
         * 
         * <p>This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the speedLimit property.</p>
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * </p>
         * <pre>
         * getSpeedLimit().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link SpeedLimit }
         * </p>
         * 
         * 
         * @return
         *     The value of the speedLimit property.
         */
        public List<SpeedLimit> getSpeedLimit() {
            if (speedLimit == null) {
                speedLimit = new ArrayList<>();
            }
            return this.speedLimit;
        }

        /**
         * Gets the value of the lane property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public StringType getLane() {
            return lane;
        }

        /**
         * Sets the value of the lane property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setLane(StringType value) {
            this.lane = value;
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
     *       <sequence maxOccurs="unbounded">
     *         <element name="Coordinate" type="{http://www.opentrafficsim.org/ots}CoordinateType"/>
     *       </sequence>
     *     </restriction>
     *   </complexContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "coordinate"
    })
    public static class Polyline
        implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlElement(name = "Coordinate", required = true, type = String.class)
        @XmlJavaTypeAdapter(Point2dAdapter.class)
        protected List<Point2dType> coordinate;

        /**
         * Gets the value of the coordinate property.
         * 
         * <p>This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the coordinate property.</p>
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * </p>
         * <pre>
         * getCoordinate().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link String }
         * </p>
         * 
         * 
         * @return
         *     The value of the coordinate property.
         */
        public List<Point2dType> getCoordinate() {
            if (coordinate == null) {
                coordinate = new ArrayList<>();
            }
            return this.coordinate;
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
     *     <extension base="{http://www.opentrafficsim.org/ots}BasicRoadLayout">
     *     </extension>
     *   </complexContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class RoadLayout
        extends BasicRoadLayout
        implements Serializable
    {

        private static final long serialVersionUID = 10102L;

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
     *       <sequence>
     *         <element name="Elements" type="{http://www.opentrafficsim.org/ots}StripeElements" minOccurs="0"/>
     *         <element name="DashOffset" type="{http://www.opentrafficsim.org/ots}DashOffset" minOccurs="0"/>
     *         <element name="Compatibility" type="{http://www.opentrafficsim.org/ots}StripeCompatibility" maxOccurs="unbounded" minOccurs="0"/>
     *       </sequence>
     *       <attribute name="LeftChangeLane" type="{http://www.opentrafficsim.org/ots}boolean" />
     *       <attribute name="RightChangeLane" type="{http://www.opentrafficsim.org/ots}boolean" />
     *       <attribute name="LateralSync" type="{http://www.opentrafficsim.org/ots}LateralSync" />
     *       <attribute name="Stripe" use="required" type="{http://www.opentrafficsim.org/ots}string" />
     *     </restriction>
     *   </complexContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "elements",
        "dashOffset",
        "compatibility"
    })
    public static class StripeOverride
        implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlElement(name = "Elements")
        protected StripeElements elements;
        @XmlElement(name = "DashOffset")
        protected DashOffset dashOffset;
        @XmlElement(name = "Compatibility")
        protected List<StripeCompatibility> compatibility;
        @XmlAttribute(name = "LeftChangeLane")
        @XmlJavaTypeAdapter(BooleanAdapter.class)
        protected BooleanType leftChangeLane;
        @XmlAttribute(name = "RightChangeLane")
        @XmlJavaTypeAdapter(BooleanAdapter.class)
        protected BooleanType rightChangeLane;
        @XmlAttribute(name = "LateralSync")
        @XmlJavaTypeAdapter(StripeLateralSyncAdapter.class)
        protected StripeLateralSyncType lateralSync;
        @XmlAttribute(name = "Stripe", required = true)
        @XmlJavaTypeAdapter(StringAdapter.class)
        protected StringType stripe;

        /**
         * Gets the value of the elements property.
         * 
         * @return
         *     possible object is
         *     {@link StripeElements }
         *     
         */
        public StripeElements getElements() {
            return elements;
        }

        /**
         * Sets the value of the elements property.
         * 
         * @param value
         *     allowed object is
         *     {@link StripeElements }
         *     
         */
        public void setElements(StripeElements value) {
            this.elements = value;
        }

        /**
         * Gets the value of the dashOffset property.
         * 
         * @return
         *     possible object is
         *     {@link DashOffset }
         *     
         */
        public DashOffset getDashOffset() {
            return dashOffset;
        }

        /**
         * Sets the value of the dashOffset property.
         * 
         * @param value
         *     allowed object is
         *     {@link DashOffset }
         *     
         */
        public void setDashOffset(DashOffset value) {
            this.dashOffset = value;
        }

        /**
         * Gets the value of the compatibility property.
         * 
         * <p>This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the compatibility property.</p>
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * </p>
         * <pre>
         * getCompatibility().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link StripeCompatibility }
         * </p>
         * 
         * 
         * @return
         *     The value of the compatibility property.
         */
        public List<StripeCompatibility> getCompatibility() {
            if (compatibility == null) {
                compatibility = new ArrayList<>();
            }
            return this.compatibility;
        }

        /**
         * Gets the value of the leftChangeLane property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public BooleanType getLeftChangeLane() {
            return leftChangeLane;
        }

        /**
         * Sets the value of the leftChangeLane property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setLeftChangeLane(BooleanType value) {
            this.leftChangeLane = value;
        }

        /**
         * Gets the value of the rightChangeLane property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public BooleanType getRightChangeLane() {
            return rightChangeLane;
        }

        /**
         * Sets the value of the rightChangeLane property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setRightChangeLane(BooleanType value) {
            this.rightChangeLane = value;
        }

        /**
         * Gets the value of the lateralSync property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public StripeLateralSyncType getLateralSync() {
            return lateralSync;
        }

        /**
         * Sets the value of the lateralSync property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setLateralSync(StripeLateralSyncType value) {
            this.lateralSync = value;
        }

        /**
         * Gets the value of the stripe property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public StringType getStripe() {
            return stripe;
        }

        /**
         * Sets the value of the stripe property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setStripe(StringType value) {
            this.stripe = value;
        }

    }

}
