
package org.opentrafficsim.opendrive.generated;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.opendrive.bindings.LaneKeepingPolicy;
import org.opentrafficsim.opendrive.bindings.LaneKeepingPolicyAdapter;
import org.opentrafficsim.opendrive.bindings.LengthAdapter;


/**
 * In OpenDRIVE, the road network is represented by &lt;road&gt; elements. Each road runs along one road reference line. A road shall have at least one lane with a width larger than 0.
 * OpenDRIVE roads may be roads in the real road network or artificial road network created for application use. Each road is described by one or more &lt;road&gt; elements. One &lt;road&gt; element may cover a long stretch of a road, shorter stretches between junctions, or even several roads. A new &lt;road&gt; element should only start if the properties of the road cannot be described within the previous &lt;road&gt; element or if a junction is required.
 * 
 * <p>Java class for t_road complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="t_road">
 *   <complexContent>
 *     <extension base="{http://code.asam.net/simulation/standard/opendrive_schema}_OpenDriveElement">
 *       <sequence>
 *         <element name="link" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_road_link" minOccurs="0"/>
 *         <element name="type" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_road_type" maxOccurs="unbounded" minOccurs="0"/>
 *         <element name="planView" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_road_planView"/>
 *         <element name="elevationProfile" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_road_elevationProfile" minOccurs="0"/>
 *         <element name="lateralProfile" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_road_lateralProfile" minOccurs="0"/>
 *         <element name="lanes" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_road_lanes"/>
 *         <element name="objects" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_road_objects" minOccurs="0"/>
 *         <element name="signals" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_road_signals" minOccurs="0"/>
 *         <element name="surface" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_road_surface" minOccurs="0"/>
 *         <element name="railroad" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_road_railroad" minOccurs="0"/>
 *         <group ref="{http://code.asam.net/simulation/standard/opendrive_schema}g_additionalData" maxOccurs="unbounded" minOccurs="0"/>
 *       </sequence>
 *       <attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       <attribute name="length" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       <attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       <attribute name="junction" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       <attribute name="rule" type="{http://code.asam.net/simulation/standard/opendrive_schema}e_trafficRule" />
 *     </extension>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "t_road", propOrder = {
    "link",
    "type",
    "planView",
    "elevationProfile",
    "lateralProfile",
    "lanes",
    "objects",
    "signals",
    "surface",
    "railroad",
    "gAdditionalData"
})
@SuppressWarnings("all") public class TRoad
    extends OpenDriveElement
{

    protected TRoadLink link;
    protected List<TRoadType> type;
    @XmlElement(required = true)
    protected TRoadPlanView planView;
    protected TRoadElevationProfile elevationProfile;
    protected TRoadLateralProfile lateralProfile;
    @XmlElement(required = true)
    protected TRoadLanes lanes;
    protected TRoadObjects objects;
    protected TRoadSignals signals;
    protected TRoadSurface surface;
    protected TRoadRailroad railroad;
    /**
     * OpenDRIVE offers the possibility to include external data. The processing of this data depends on the application.
     * Additional data may be placed at any position in OpenDRIVE.
     * 
     */
    @XmlElements({
        @XmlElement(name = "include", type = TInclude.class),
        @XmlElement(name = "userData", type = TUserData.class),
        @XmlElement(name = "dataQuality", type = TDataQuality.class)
    })
    protected List<Object> gAdditionalData;
    /**
     * Name of the road. May be chosen freely.
     * 
     */
    @XmlAttribute(name = "name")
    protected String name;
    /**
     * Total length of the reference line in the xy-plane. Change in length due to elevation is not considered
     * 
     */
    @XmlAttribute(name = "length", required = true)
    @XmlJavaTypeAdapter(LengthAdapter.class)
    protected Length length;
    /**
     * Unique ID within the database. If it represents an integer number, it should comply to uint32_t and stay within the given range.
     * 
     */
    @XmlAttribute(name = "id", required = true)
    protected String id;
    /**
     * ID of the junction to which the road belongs as a connecting road (= -1 for none)
     * 
     */
    @XmlAttribute(name = "junction", required = true)
    protected String junction;
    /**
     * Basic rule for using the road; RHT=right-hand traffic, LHT=left-hand traffic. When this attribute is missing, RHT is assumed.
     * 
     */
    @XmlAttribute(name = "rule")
    @XmlJavaTypeAdapter(LaneKeepingPolicyAdapter.class)
    protected LaneKeepingPolicy rule;

    /**
     * Gets the value of the link property.
     * 
     * @return
     *     possible object is
     *     {@link TRoadLink }
     *     
     */
    public TRoadLink getLink() {
        return link;
    }

    /**
     * Sets the value of the link property.
     * 
     * @param value
     *     allowed object is
     *     {@link TRoadLink }
     *     
     */
    public void setLink(TRoadLink value) {
        this.link = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the type property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getType().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TRoadType }
     * </p>
     * 
     * 
     * @return
     *     The value of the type property.
     */
    public List<TRoadType> getType() {
        if (type == null) {
            type = new ArrayList<>();
        }
        return this.type;
    }

    /**
     * Gets the value of the planView property.
     * 
     * @return
     *     possible object is
     *     {@link TRoadPlanView }
     *     
     */
    public TRoadPlanView getPlanView() {
        return planView;
    }

    /**
     * Sets the value of the planView property.
     * 
     * @param value
     *     allowed object is
     *     {@link TRoadPlanView }
     *     
     */
    public void setPlanView(TRoadPlanView value) {
        this.planView = value;
    }

    /**
     * Gets the value of the elevationProfile property.
     * 
     * @return
     *     possible object is
     *     {@link TRoadElevationProfile }
     *     
     */
    public TRoadElevationProfile getElevationProfile() {
        return elevationProfile;
    }

    /**
     * Sets the value of the elevationProfile property.
     * 
     * @param value
     *     allowed object is
     *     {@link TRoadElevationProfile }
     *     
     */
    public void setElevationProfile(TRoadElevationProfile value) {
        this.elevationProfile = value;
    }

    /**
     * Gets the value of the lateralProfile property.
     * 
     * @return
     *     possible object is
     *     {@link TRoadLateralProfile }
     *     
     */
    public TRoadLateralProfile getLateralProfile() {
        return lateralProfile;
    }

    /**
     * Sets the value of the lateralProfile property.
     * 
     * @param value
     *     allowed object is
     *     {@link TRoadLateralProfile }
     *     
     */
    public void setLateralProfile(TRoadLateralProfile value) {
        this.lateralProfile = value;
    }

    /**
     * Gets the value of the lanes property.
     * 
     * @return
     *     possible object is
     *     {@link TRoadLanes }
     *     
     */
    public TRoadLanes getLanes() {
        return lanes;
    }

    /**
     * Sets the value of the lanes property.
     * 
     * @param value
     *     allowed object is
     *     {@link TRoadLanes }
     *     
     */
    public void setLanes(TRoadLanes value) {
        this.lanes = value;
    }

    /**
     * Gets the value of the objects property.
     * 
     * @return
     *     possible object is
     *     {@link TRoadObjects }
     *     
     */
    public TRoadObjects getObjects() {
        return objects;
    }

    /**
     * Sets the value of the objects property.
     * 
     * @param value
     *     allowed object is
     *     {@link TRoadObjects }
     *     
     */
    public void setObjects(TRoadObjects value) {
        this.objects = value;
    }

    /**
     * Gets the value of the signals property.
     * 
     * @return
     *     possible object is
     *     {@link TRoadSignals }
     *     
     */
    public TRoadSignals getSignals() {
        return signals;
    }

    /**
     * Sets the value of the signals property.
     * 
     * @param value
     *     allowed object is
     *     {@link TRoadSignals }
     *     
     */
    public void setSignals(TRoadSignals value) {
        this.signals = value;
    }

    /**
     * Gets the value of the surface property.
     * 
     * @return
     *     possible object is
     *     {@link TRoadSurface }
     *     
     */
    public TRoadSurface getSurface() {
        return surface;
    }

    /**
     * Sets the value of the surface property.
     * 
     * @param value
     *     allowed object is
     *     {@link TRoadSurface }
     *     
     */
    public void setSurface(TRoadSurface value) {
        this.surface = value;
    }

    /**
     * Gets the value of the railroad property.
     * 
     * @return
     *     possible object is
     *     {@link TRoadRailroad }
     *     
     */
    public TRoadRailroad getRailroad() {
        return railroad;
    }

    /**
     * Sets the value of the railroad property.
     * 
     * @param value
     *     allowed object is
     *     {@link TRoadRailroad }
     *     
     */
    public void setRailroad(TRoadRailroad value) {
        this.railroad = value;
    }

    /**
     * OpenDRIVE offers the possibility to include external data. The processing of this data depends on the application.
     * Additional data may be placed at any position in OpenDRIVE.
     * 
     * Gets the value of the gAdditionalData property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the gAdditionalData property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getGAdditionalData().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TDataQuality }
     * {@link TInclude }
     * {@link TUserData }
     * </p>
     * 
     * 
     * @return
     *     The value of the gAdditionalData property.
     */
    public List<Object> getGAdditionalData() {
        if (gAdditionalData == null) {
            gAdditionalData = new ArrayList<>();
        }
        return this.gAdditionalData;
    }

    /**
     * Name of the road. May be chosen freely.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     * @see #getName()
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Total length of the reference line in the xy-plane. Change in length due to elevation is not considered
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Length getLength() {
        return length;
    }

    /**
     * Sets the value of the length property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     * @see #getLength()
     */
    public void setLength(Length value) {
        this.length = value;
    }

    /**
     * Unique ID within the database. If it represents an integer number, it should comply to uint32_t and stay within the given range.
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
     * @see #getId()
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * ID of the junction to which the road belongs as a connecting road (= -1 for none)
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getJunction() {
        return junction;
    }

    /**
     * Sets the value of the junction property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     * @see #getJunction()
     */
    public void setJunction(String value) {
        this.junction = value;
    }

    /**
     * Basic rule for using the road; RHT=right-hand traffic, LHT=left-hand traffic. When this attribute is missing, RHT is assumed.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public LaneKeepingPolicy getRule() {
        return rule;
    }

    /**
     * Sets the value of the rule property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     * @see #getRule()
     */
    public void setRule(LaneKeepingPolicy value) {
        this.rule = value;
    }

}
