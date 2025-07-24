//
// Diese Datei wurde mit der Eclipse Implementation of JAXB, v2.3.7 generiert 
// Siehe https://eclipse-ee4j.github.io/jaxb-ri 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2025.07.24 um 01:18:43 PM CEST 
//


package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opentrafficsim.xml.bindings.ArcDirectionAdapter;
import org.opentrafficsim.xml.bindings.DoublePositiveAdapter;
import org.opentrafficsim.xml.bindings.LaneKeepingPolicyAdapter;
import org.opentrafficsim.xml.bindings.LengthAdapter;
import org.opentrafficsim.xml.bindings.LinearDensityAdapter;
import org.opentrafficsim.xml.bindings.Point2dAdapter;
import org.opentrafficsim.xml.bindings.PositiveLengthAdapter;
import org.opentrafficsim.xml.bindings.PriorityAdapter;
import org.opentrafficsim.xml.bindings.StringAdapter;
import org.opentrafficsim.xml.bindings.types.ArcDirectionType;
import org.opentrafficsim.xml.bindings.types.DoubleType;
import org.opentrafficsim.xml.bindings.types.LaneKeepingPolicyType;
import org.opentrafficsim.xml.bindings.types.LinearDensityType;
import org.opentrafficsim.xml.bindings.types.Point2dType;
import org.opentrafficsim.xml.bindings.types.PriorityType;
import org.opentrafficsim.xml.bindings.types.StringType;


/**
 * <p>Java-Klasse für anonymous complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;choice&gt;
 *           &lt;element name="Straight" type="{http://www.opentrafficsim.org/ots}EmptyType"/&gt;
 *           &lt;element name="Bezier"&gt;
 *             &lt;complexType&gt;
 *               &lt;complexContent&gt;
 *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                   &lt;sequence&gt;
 *                     &lt;element name="Flattener" type="{http://www.opentrafficsim.org/ots}FlattenerType" minOccurs="0"/&gt;
 *                   &lt;/sequence&gt;
 *                   &lt;attribute name="Shape" type="{http://www.opentrafficsim.org/ots}DoublePositive" default="1.0" /&gt;
 *                   &lt;attribute name="Weighted" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" /&gt;
 *                 &lt;/restriction&gt;
 *               &lt;/complexContent&gt;
 *             &lt;/complexType&gt;
 *           &lt;/element&gt;
 *           &lt;element name="Clothoid"&gt;
 *             &lt;complexType&gt;
 *               &lt;complexContent&gt;
 *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                   &lt;sequence&gt;
 *                     &lt;choice&gt;
 *                       &lt;element name="Interpolated" type="{http://www.opentrafficsim.org/ots}EmptyType"/&gt;
 *                       &lt;sequence&gt;
 *                         &lt;element name="Length" type="{http://www.opentrafficsim.org/ots}LengthType"/&gt;
 *                         &lt;element name="StartCurvature" type="{http://www.opentrafficsim.org/ots}LinearDensityType"/&gt;
 *                         &lt;element name="EndCurvature" type="{http://www.opentrafficsim.org/ots}LinearDensityType"/&gt;
 *                       &lt;/sequence&gt;
 *                       &lt;sequence&gt;
 *                         &lt;element name="A" type="{http://www.opentrafficsim.org/ots}LengthType"/&gt;
 *                         &lt;element name="StartCurvature" type="{http://www.opentrafficsim.org/ots}LinearDensityType"/&gt;
 *                         &lt;element name="EndCurvature" type="{http://www.opentrafficsim.org/ots}LinearDensityType"/&gt;
 *                       &lt;/sequence&gt;
 *                     &lt;/choice&gt;
 *                     &lt;sequence&gt;
 *                       &lt;element name="Flattener" type="{http://www.opentrafficsim.org/ots}FlattenerType" minOccurs="0"/&gt;
 *                     &lt;/sequence&gt;
 *                   &lt;/sequence&gt;
 *                   &lt;attribute name="EndElevation" type="{http://www.opentrafficsim.org/ots}PositiveLengthType" /&gt;
 *                 &lt;/restriction&gt;
 *               &lt;/complexContent&gt;
 *             &lt;/complexType&gt;
 *           &lt;/element&gt;
 *           &lt;element name="Arc"&gt;
 *             &lt;complexType&gt;
 *               &lt;complexContent&gt;
 *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                   &lt;sequence&gt;
 *                     &lt;element name="Flattener" type="{http://www.opentrafficsim.org/ots}FlattenerType" minOccurs="0"/&gt;
 *                   &lt;/sequence&gt;
 *                   &lt;attribute name="Radius" use="required" type="{http://www.opentrafficsim.org/ots}PositiveLengthType" /&gt;
 *                   &lt;attribute name="Direction" use="required" type="{http://www.opentrafficsim.org/ots}ArcDirectionType" /&gt;
 *                 &lt;/restriction&gt;
 *               &lt;/complexContent&gt;
 *             &lt;/complexType&gt;
 *           &lt;/element&gt;
 *           &lt;element name="Polyline"&gt;
 *             &lt;complexType&gt;
 *               &lt;complexContent&gt;
 *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                   &lt;sequence maxOccurs="unbounded"&gt;
 *                     &lt;element name="Coordinate" type="{http://www.opentrafficsim.org/ots}CoordinateType"/&gt;
 *                   &lt;/sequence&gt;
 *                 &lt;/restriction&gt;
 *               &lt;/complexContent&gt;
 *             &lt;/complexType&gt;
 *           &lt;/element&gt;
 *         &lt;/choice&gt;
 *         &lt;choice&gt;
 *           &lt;element name="RoadLayout"&gt;
 *             &lt;complexType&gt;
 *               &lt;complexContent&gt;
 *                 &lt;extension base="{http://www.opentrafficsim.org/ots}BasicRoadLayout"&gt;
 *                 &lt;/extension&gt;
 *               &lt;/complexContent&gt;
 *             &lt;/complexType&gt;
 *           &lt;/element&gt;
 *           &lt;sequence&gt;
 *             &lt;element name="DefinedLayout" type="{http://www.opentrafficsim.org/ots}string"/&gt;
 *             &lt;element name="LaneOverride" maxOccurs="unbounded" minOccurs="0"&gt;
 *               &lt;complexType&gt;
 *                 &lt;complexContent&gt;
 *                   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                     &lt;sequence&gt;
 *                       &lt;element ref="{http://www.opentrafficsim.org/ots}SpeedLimit" maxOccurs="unbounded" minOccurs="0"/&gt;
 *                     &lt;/sequence&gt;
 *                     &lt;attribute name="Lane" use="required" type="{http://www.opentrafficsim.org/ots}string" /&gt;
 *                   &lt;/restriction&gt;
 *                 &lt;/complexContent&gt;
 *               &lt;/complexType&gt;
 *             &lt;/element&gt;
 *           &lt;/sequence&gt;
 *         &lt;/choice&gt;
 *         &lt;element name="TrafficLight" type="{http://www.opentrafficsim.org/ots}TrafficLightType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="TrafficLightSensor" type="{http://www.opentrafficsim.org/ots}TrafficLightDetectorType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}IdType" /&gt;
 *       &lt;attribute name="Type" use="required" type="{http://www.opentrafficsim.org/ots}string" /&gt;
 *       &lt;attribute name="NodeStart" use="required" type="{http://www.opentrafficsim.org/ots}string" /&gt;
 *       &lt;attribute name="NodeEnd" use="required" type="{http://www.opentrafficsim.org/ots}string" /&gt;
 *       &lt;attribute name="OffsetStart" type="{http://www.opentrafficsim.org/ots}LengthType" default="0.0 m" /&gt;
 *       &lt;attribute name="OffsetEnd" type="{http://www.opentrafficsim.org/ots}LengthType" default="0.0 m" /&gt;
 *       &lt;attribute name="LaneKeeping" type="{http://www.opentrafficsim.org/ots}LaneKeepingType" default="KEEPRIGHT" /&gt;
 *       &lt;attribute name="Priority" type="{http://www.opentrafficsim.org/ots}PriorityType" /&gt;
 *       &lt;attribute name="ConflictId" type="{http://www.opentrafficsim.org/ots}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
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
    "trafficLight",
    "trafficLightSensor"
})
@XmlRootElement(name = "Link")
@Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
public class Link
    implements Serializable
{

    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    private final static long serialVersionUID = 10102L;
    @XmlElement(name = "Straight")
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected EmptyType straight;
    @XmlElement(name = "Bezier")
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected Link.Bezier bezier;
    @XmlElement(name = "Clothoid")
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected Link.Clothoid clothoid;
    @XmlElement(name = "Arc")
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected Link.Arc arc;
    @XmlElement(name = "Polyline")
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected Link.Polyline polyline;
    @XmlElement(name = "RoadLayout")
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected Link.RoadLayout roadLayout;
    @XmlElement(name = "DefinedLayout", type = String.class)
    @XmlJavaTypeAdapter(StringAdapter.class)
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected StringType definedLayout;
    @XmlElement(name = "LaneOverride")
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected List<Link.LaneOverride> laneOverride;
    @XmlElement(name = "TrafficLight")
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected List<TrafficLightType> trafficLight;
    @XmlElement(name = "TrafficLightSensor")
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected List<TrafficLightDetectorType> trafficLightSensor;
    @XmlAttribute(name = "Id", required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected String id;
    @XmlAttribute(name = "Type", required = true)
    @XmlJavaTypeAdapter(StringAdapter.class)
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected StringType type;
    @XmlAttribute(name = "NodeStart", required = true)
    @XmlJavaTypeAdapter(StringAdapter.class)
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected StringType nodeStart;
    @XmlAttribute(name = "NodeEnd", required = true)
    @XmlJavaTypeAdapter(StringAdapter.class)
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected StringType nodeEnd;
    @XmlAttribute(name = "OffsetStart")
    @XmlJavaTypeAdapter(LengthAdapter.class)
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected org.opentrafficsim.xml.bindings.types.LengthType offsetStart;
    @XmlAttribute(name = "OffsetEnd")
    @XmlJavaTypeAdapter(LengthAdapter.class)
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected org.opentrafficsim.xml.bindings.types.LengthType offsetEnd;
    @XmlAttribute(name = "LaneKeeping")
    @XmlJavaTypeAdapter(LaneKeepingPolicyAdapter.class)
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected LaneKeepingPolicyType laneKeeping;
    @XmlAttribute(name = "Priority")
    @XmlJavaTypeAdapter(PriorityAdapter.class)
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected PriorityType priority;
    @XmlAttribute(name = "ConflictId")
    @XmlJavaTypeAdapter(StringAdapter.class)
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected StringType conflictId;

    /**
     * Ruft den Wert der straight-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link EmptyType }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public void setDefinedLayout(StringType value) {
        this.definedLayout = value;
    }

    /**
     * Gets the value of the laneOverride property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the laneOverride property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLaneOverride().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Link.LaneOverride }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public List<Link.LaneOverride> getLaneOverride() {
        if (laneOverride == null) {
            laneOverride = new ArrayList<Link.LaneOverride>();
        }
        return this.laneOverride;
    }

    /**
     * Gets the value of the trafficLight property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the trafficLight property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTrafficLight().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TrafficLightType }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public List<TrafficLightType> getTrafficLight() {
        if (trafficLight == null) {
            trafficLight = new ArrayList<TrafficLightType>();
        }
        return this.trafficLight;
    }

    /**
     * Gets the value of the trafficLightSensor property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the trafficLightSensor property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTrafficLightSensor().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TrafficLightDetectorType }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public List<TrafficLightDetectorType> getTrafficLightSensor() {
        if (trafficLightSensor == null) {
            trafficLightSensor = new ArrayList<TrafficLightDetectorType>();
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
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public void setConflictId(StringType value) {
        this.conflictId = value;
    }


    /**
     * <p>Java-Klasse für anonymous complex type.
     * 
     * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
     * 
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;sequence&gt;
     *         &lt;element name="Flattener" type="{http://www.opentrafficsim.org/ots}FlattenerType" minOccurs="0"/&gt;
     *       &lt;/sequence&gt;
     *       &lt;attribute name="Radius" use="required" type="{http://www.opentrafficsim.org/ots}PositiveLengthType" /&gt;
     *       &lt;attribute name="Direction" use="required" type="{http://www.opentrafficsim.org/ots}ArcDirectionType" /&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "flattener"
    })
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public static class Arc
        implements Serializable
    {

        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        private final static long serialVersionUID = 10102L;
        @XmlElement(name = "Flattener")
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected FlattenerType flattener;
        @XmlAttribute(name = "Radius", required = true)
        @XmlJavaTypeAdapter(PositiveLengthAdapter.class)
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected org.opentrafficsim.xml.bindings.types.LengthType radius;
        @XmlAttribute(name = "Direction", required = true)
        @XmlJavaTypeAdapter(ArcDirectionAdapter.class)
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected ArcDirectionType direction;

        /**
         * Ruft den Wert der flattener-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link FlattenerType }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public void setDirection(ArcDirectionType value) {
            this.direction = value;
        }

    }


    /**
     * <p>Java-Klasse für anonymous complex type.
     * 
     * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
     * 
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;sequence&gt;
     *         &lt;element name="Flattener" type="{http://www.opentrafficsim.org/ots}FlattenerType" minOccurs="0"/&gt;
     *       &lt;/sequence&gt;
     *       &lt;attribute name="Shape" type="{http://www.opentrafficsim.org/ots}DoublePositive" default="1.0" /&gt;
     *       &lt;attribute name="Weighted" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" /&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "flattener"
    })
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public static class Bezier
        implements Serializable
    {

        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        private final static long serialVersionUID = 10102L;
        @XmlElement(name = "Flattener")
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected FlattenerType flattener;
        @XmlAttribute(name = "Shape")
        @XmlJavaTypeAdapter(DoublePositiveAdapter.class)
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected DoubleType shape;
        @XmlAttribute(name = "Weighted")
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected Boolean weighted;

        /**
         * Ruft den Wert der flattener-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link FlattenerType }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public void setWeighted(Boolean value) {
            this.weighted = value;
        }

    }


    /**
     * <p>Java-Klasse für anonymous complex type.
     * 
     * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
     * 
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;sequence&gt;
     *         &lt;choice&gt;
     *           &lt;element name="Interpolated" type="{http://www.opentrafficsim.org/ots}EmptyType"/&gt;
     *           &lt;sequence&gt;
     *             &lt;element name="Length" type="{http://www.opentrafficsim.org/ots}LengthType"/&gt;
     *             &lt;element name="StartCurvature" type="{http://www.opentrafficsim.org/ots}LinearDensityType"/&gt;
     *             &lt;element name="EndCurvature" type="{http://www.opentrafficsim.org/ots}LinearDensityType"/&gt;
     *           &lt;/sequence&gt;
     *           &lt;sequence&gt;
     *             &lt;element name="A" type="{http://www.opentrafficsim.org/ots}LengthType"/&gt;
     *             &lt;element name="StartCurvature" type="{http://www.opentrafficsim.org/ots}LinearDensityType"/&gt;
     *             &lt;element name="EndCurvature" type="{http://www.opentrafficsim.org/ots}LinearDensityType"/&gt;
     *           &lt;/sequence&gt;
     *         &lt;/choice&gt;
     *         &lt;sequence&gt;
     *           &lt;element name="Flattener" type="{http://www.opentrafficsim.org/ots}FlattenerType" minOccurs="0"/&gt;
     *         &lt;/sequence&gt;
     *       &lt;/sequence&gt;
     *       &lt;attribute name="EndElevation" type="{http://www.opentrafficsim.org/ots}PositiveLengthType" /&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
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
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public static class Clothoid
        implements Serializable
    {

        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        private final static long serialVersionUID = 10102L;
        @XmlElement(name = "Interpolated")
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected List<EmptyType> interpolated;
        @XmlElement(name = "Length", type = String.class)
        @XmlJavaTypeAdapter(LengthAdapter.class)
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected List<org.opentrafficsim.xml.bindings.types.LengthType> length;
        @XmlElement(name = "StartCurvature", type = String.class)
        @XmlJavaTypeAdapter(LinearDensityAdapter.class)
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected List<LinearDensityType> startCurvature;
        @XmlElement(name = "EndCurvature", type = String.class)
        @XmlJavaTypeAdapter(LinearDensityAdapter.class)
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected List<LinearDensityType> endCurvature;
        @XmlElement(name = "A", type = String.class)
        @XmlJavaTypeAdapter(LengthAdapter.class)
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected List<org.opentrafficsim.xml.bindings.types.LengthType> a;
        @XmlElement(name = "Flattener")
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected List<FlattenerType> flattener;
        @XmlAttribute(name = "EndElevation")
        @XmlJavaTypeAdapter(PositiveLengthAdapter.class)
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected org.opentrafficsim.xml.bindings.types.LengthType endElevation;

        /**
         * Gets the value of the interpolated property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the interpolated property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getInterpolated().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link EmptyType }
         * 
         * 
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public List<EmptyType> getInterpolated() {
            if (interpolated == null) {
                interpolated = new ArrayList<EmptyType>();
            }
            return this.interpolated;
        }

        /**
         * Gets the value of the length property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the length property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getLength().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link String }
         * 
         * 
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public List<org.opentrafficsim.xml.bindings.types.LengthType> getLength() {
            if (length == null) {
                length = new ArrayList<org.opentrafficsim.xml.bindings.types.LengthType>();
            }
            return this.length;
        }

        /**
         * Gets the value of the startCurvature property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the startCurvature property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getStartCurvature().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link String }
         * 
         * 
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public List<LinearDensityType> getStartCurvature() {
            if (startCurvature == null) {
                startCurvature = new ArrayList<LinearDensityType>();
            }
            return this.startCurvature;
        }

        /**
         * Gets the value of the endCurvature property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the endCurvature property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getEndCurvature().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link String }
         * 
         * 
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public List<LinearDensityType> getEndCurvature() {
            if (endCurvature == null) {
                endCurvature = new ArrayList<LinearDensityType>();
            }
            return this.endCurvature;
        }

        /**
         * Gets the value of the a property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the a property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getA().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link String }
         * 
         * 
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public List<org.opentrafficsim.xml.bindings.types.LengthType> getA() {
            if (a == null) {
                a = new ArrayList<org.opentrafficsim.xml.bindings.types.LengthType>();
            }
            return this.a;
        }

        /**
         * Gets the value of the flattener property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the flattener property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getFlattener().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link FlattenerType }
         * 
         * 
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public List<FlattenerType> getFlattener() {
            if (flattener == null) {
                flattener = new ArrayList<FlattenerType>();
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
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public void setEndElevation(org.opentrafficsim.xml.bindings.types.LengthType value) {
            this.endElevation = value;
        }

    }


    /**
     * <p>Java-Klasse für anonymous complex type.
     * 
     * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
     * 
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;sequence&gt;
     *         &lt;element ref="{http://www.opentrafficsim.org/ots}SpeedLimit" maxOccurs="unbounded" minOccurs="0"/&gt;
     *       &lt;/sequence&gt;
     *       &lt;attribute name="Lane" use="required" type="{http://www.opentrafficsim.org/ots}string" /&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "speedLimit"
    })
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public static class LaneOverride
        implements Serializable
    {

        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        private final static long serialVersionUID = 10102L;
        @XmlElement(name = "SpeedLimit")
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected List<SpeedLimit> speedLimit;
        @XmlAttribute(name = "Lane", required = true)
        @XmlJavaTypeAdapter(StringAdapter.class)
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected StringType lane;

        /**
         * Gets the value of the speedLimit property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the speedLimit property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getSpeedLimit().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link SpeedLimit }
         * 
         * 
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public List<SpeedLimit> getSpeedLimit() {
            if (speedLimit == null) {
                speedLimit = new ArrayList<SpeedLimit>();
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
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public void setLane(StringType value) {
            this.lane = value;
        }

    }


    /**
     * <p>Java-Klasse für anonymous complex type.
     * 
     * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
     * 
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;sequence maxOccurs="unbounded"&gt;
     *         &lt;element name="Coordinate" type="{http://www.opentrafficsim.org/ots}CoordinateType"/&gt;
     *       &lt;/sequence&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "coordinate"
    })
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public static class Polyline
        implements Serializable
    {

        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        private final static long serialVersionUID = 10102L;
        @XmlElement(name = "Coordinate", required = true, type = String.class)
        @XmlJavaTypeAdapter(Point2dAdapter.class)
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected List<Point2dType> coordinate;

        /**
         * Gets the value of the coordinate property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the coordinate property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getCoordinate().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link String }
         * 
         * 
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public List<Point2dType> getCoordinate() {
            if (coordinate == null) {
                coordinate = new ArrayList<Point2dType>();
            }
            return this.coordinate;
        }

    }


    /**
     * <p>Java-Klasse für anonymous complex type.
     * 
     * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
     * 
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;extension base="{http://www.opentrafficsim.org/ots}BasicRoadLayout"&gt;
     *     &lt;/extension&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public static class RoadLayout
        extends BasicRoadLayout
        implements Serializable
    {

        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        private final static long serialVersionUID = 10102L;

    }

}
