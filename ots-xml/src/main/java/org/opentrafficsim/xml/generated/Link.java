
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
 * <p>Java-Klasse für anonymous complex type.</p>
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.</p>
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
     * Ruft den Wert der straight-Eigenschaft ab.
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
     * Legt den Wert der straight-Eigenschaft fest.
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
     * Ruft den Wert der bezier-Eigenschaft ab.
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
     * Legt den Wert der bezier-Eigenschaft fest.
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
     * Ruft den Wert der clothoid-Eigenschaft ab.
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
     * Legt den Wert der clothoid-Eigenschaft fest.
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
     * Ruft den Wert der arc-Eigenschaft ab.
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
     * Legt den Wert der arc-Eigenschaft fest.
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
     * Ruft den Wert der polyline-Eigenschaft ab.
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
     * Legt den Wert der polyline-Eigenschaft fest.
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
     * Ruft den Wert der roadLayout-Eigenschaft ab.
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
     * Legt den Wert der roadLayout-Eigenschaft fest.
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
     * Ruft den Wert der definedLayout-Eigenschaft ab.
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
     * Legt den Wert der definedLayout-Eigenschaft fest.
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
     * Ruft den Wert der id-Eigenschaft ab.
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
     * Legt den Wert der id-Eigenschaft fest.
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
     * Ruft den Wert der type-Eigenschaft ab.
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
     * Legt den Wert der type-Eigenschaft fest.
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
     * Ruft den Wert der nodeStart-Eigenschaft ab.
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
     * Legt den Wert der nodeStart-Eigenschaft fest.
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
     * Ruft den Wert der nodeEnd-Eigenschaft ab.
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
     * Legt den Wert der nodeEnd-Eigenschaft fest.
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
     * Ruft den Wert der offsetStart-Eigenschaft ab.
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
     * Legt den Wert der offsetStart-Eigenschaft fest.
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
     * Ruft den Wert der offsetEnd-Eigenschaft ab.
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
     * Legt den Wert der offsetEnd-Eigenschaft fest.
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
     * Ruft den Wert der laneKeeping-Eigenschaft ab.
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
     * Legt den Wert der laneKeeping-Eigenschaft fest.
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
     * Ruft den Wert der priority-Eigenschaft ab.
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
     * Legt den Wert der priority-Eigenschaft fest.
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
     * Ruft den Wert der conflictId-Eigenschaft ab.
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
     * Legt den Wert der conflictId-Eigenschaft fest.
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
     * <p>Java-Klasse für anonymous complex type.</p>
     * 
     * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.</p>
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
         * Ruft den Wert der flattener-Eigenschaft ab.
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
         * Legt den Wert der flattener-Eigenschaft fest.
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
         * Ruft den Wert der radius-Eigenschaft ab.
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
         * Legt den Wert der radius-Eigenschaft fest.
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
         * Ruft den Wert der direction-Eigenschaft ab.
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
         * Legt den Wert der direction-Eigenschaft fest.
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
     * <p>Java-Klasse für anonymous complex type.</p>
     * 
     * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.</p>
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
         * Ruft den Wert der flattener-Eigenschaft ab.
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
         * Legt den Wert der flattener-Eigenschaft fest.
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
         * Ruft den Wert der shape-Eigenschaft ab.
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
         * Legt den Wert der shape-Eigenschaft fest.
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
         * Ruft den Wert der weighted-Eigenschaft ab.
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
         * Legt den Wert der weighted-Eigenschaft fest.
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
     * <p>Java-Klasse für anonymous complex type.</p>
     * 
     * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.</p>
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
         * Ruft den Wert der endElevation-Eigenschaft ab.
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
         * Legt den Wert der endElevation-Eigenschaft fest.
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
     * <p>Java-Klasse für anonymous complex type.</p>
     * 
     * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.</p>
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
         * Ruft den Wert der lane-Eigenschaft ab.
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
         * Legt den Wert der lane-Eigenschaft fest.
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
     * <p>Java-Klasse für anonymous complex type.</p>
     * 
     * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.</p>
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
     * <p>Java-Klasse für anonymous complex type.</p>
     * 
     * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.</p>
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
     * <p>Java-Klasse für anonymous complex type.</p>
     * 
     * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.</p>
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
         * Ruft den Wert der elements-Eigenschaft ab.
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
         * Legt den Wert der elements-Eigenschaft fest.
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
         * Ruft den Wert der dashOffset-Eigenschaft ab.
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
         * Legt den Wert der dashOffset-Eigenschaft fest.
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
         * Ruft den Wert der leftChangeLane-Eigenschaft ab.
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
         * Legt den Wert der leftChangeLane-Eigenschaft fest.
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
         * Ruft den Wert der rightChangeLane-Eigenschaft ab.
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
         * Legt den Wert der rightChangeLane-Eigenschaft fest.
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
         * Ruft den Wert der lateralSync-Eigenschaft ab.
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
         * Legt den Wert der lateralSync-Eigenschaft fest.
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
         * Ruft den Wert der stripe-Eigenschaft ab.
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
         * Legt den Wert der stripe-Eigenschaft fest.
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
