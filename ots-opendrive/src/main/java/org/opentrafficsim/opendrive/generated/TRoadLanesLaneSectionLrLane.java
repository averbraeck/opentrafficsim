
package org.opentrafficsim.opendrive.generated;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlSeeAlso;
import jakarta.xml.bind.annotation.XmlType;


/**
 * Lane elements are included in left/center/right elements. Lane elements should represent the lanes from left to right, that is, with descending ID.
 * 
 * <p>Java class for t_road_lanes_laneSection_lr_lane complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="t_road_lanes_laneSection_lr_lane">
 *   <complexContent>
 *     <extension base="{http://code.asam.net/simulation/standard/opendrive_schema}_OpenDriveElement">
 *       <sequence>
 *         <element name="link" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_road_lanes_laneSection_lcr_lane_link" minOccurs="0"/>
 *         <choice maxOccurs="unbounded">
 *           <element name="border" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_road_lanes_laneSection_lr_lane_border" maxOccurs="unbounded" minOccurs="0"/>
 *           <element name="width" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_road_lanes_laneSection_lr_lane_width" maxOccurs="unbounded" minOccurs="0"/>
 *         </choice>
 *         <element name="roadMark" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_road_lanes_laneSection_lcr_lane_roadMark" maxOccurs="unbounded" minOccurs="0"/>
 *         <element name="material" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_road_lanes_laneSection_lr_lane_material" maxOccurs="unbounded" minOccurs="0"/>
 *         <element name="speed" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_road_lanes_laneSection_lr_lane_speed" maxOccurs="unbounded" minOccurs="0"/>
 *         <element name="access" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_road_lanes_laneSection_lr_lane_access" maxOccurs="unbounded" minOccurs="0"/>
 *         <element name="height" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_road_lanes_laneSection_lr_lane_height" maxOccurs="unbounded" minOccurs="0"/>
 *         <element name="rule" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_road_lanes_laneSection_lr_lane_rule" maxOccurs="unbounded" minOccurs="0"/>
 *         <group ref="{http://code.asam.net/simulation/standard/opendrive_schema}g_additionalData" maxOccurs="unbounded" minOccurs="0"/>
 *       </sequence>
 *       <attribute name="type" use="required" type="{http://code.asam.net/simulation/standard/opendrive_schema}e_laneType" />
 *       <attribute name="level" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_bool" />
 *     </extension>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "t_road_lanes_laneSection_lr_lane", propOrder = {
    "link",
    "borderOrWidth",
    "roadMark",
    "material",
    "speed",
    "access",
    "height",
    "rule",
    "gAdditionalData"
})
@XmlSeeAlso({
    TRoadLanesLaneSectionCenterLane.class,
    TRoadLanesLaneSectionLeftLane.class,
    TRoadLanesLaneSectionRightLane.class
})
@SuppressWarnings("all") public class TRoadLanesLaneSectionLrLane
    extends OpenDriveElement
{

    protected TRoadLanesLaneSectionLcrLaneLink link;
    @XmlElements({
        @XmlElement(name = "border", type = TRoadLanesLaneSectionLrLaneBorder.class),
        @XmlElement(name = "width", type = TRoadLanesLaneSectionLrLaneWidth.class)
    })
    protected List<OpenDriveElement> borderOrWidth;
    protected List<TRoadLanesLaneSectionLcrLaneRoadMark> roadMark;
    protected List<TRoadLanesLaneSectionLrLaneMaterial> material;
    protected List<TRoadLanesLaneSectionLrLaneSpeed> speed;
    protected List<TRoadLanesLaneSectionLrLaneAccess> access;
    protected List<TRoadLanesLaneSectionLrLaneHeight> height;
    protected List<TRoadLanesLaneSectionLrLaneRule> rule;
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
     * Type of the lane. For values see UML model.
     * 
     */
    @XmlAttribute(name = "type", required = true)
    protected ELaneType type;
    /**
     * "true" = keep lane on level, that is, do not apply superelevation;
     * "false" = apply superelevation to this lane (default, also used if attribute level is missing)
     * 
     */
    @XmlAttribute(name = "level")
    protected TBool level;

    /**
     * Gets the value of the link property.
     * 
     * @return
     *     possible object is
     *     {@link TRoadLanesLaneSectionLcrLaneLink }
     *     
     */
    public TRoadLanesLaneSectionLcrLaneLink getLink() {
        return link;
    }

    /**
     * Sets the value of the link property.
     * 
     * @param value
     *     allowed object is
     *     {@link TRoadLanesLaneSectionLcrLaneLink }
     *     
     */
    public void setLink(TRoadLanesLaneSectionLcrLaneLink value) {
        this.link = value;
    }

    /**
     * Gets the value of the borderOrWidth property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the borderOrWidth property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getBorderOrWidth().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TRoadLanesLaneSectionLrLaneBorder }
     * {@link TRoadLanesLaneSectionLrLaneWidth }
     * </p>
     * 
     * 
     * @return
     *     The value of the borderOrWidth property.
     */
    public List<OpenDriveElement> getBorderOrWidth() {
        if (borderOrWidth == null) {
            borderOrWidth = new ArrayList<>();
        }
        return this.borderOrWidth;
    }

    /**
     * Gets the value of the roadMark property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the roadMark property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getRoadMark().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TRoadLanesLaneSectionLcrLaneRoadMark }
     * </p>
     * 
     * 
     * @return
     *     The value of the roadMark property.
     */
    public List<TRoadLanesLaneSectionLcrLaneRoadMark> getRoadMark() {
        if (roadMark == null) {
            roadMark = new ArrayList<>();
        }
        return this.roadMark;
    }

    /**
     * Gets the value of the material property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the material property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getMaterial().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TRoadLanesLaneSectionLrLaneMaterial }
     * </p>
     * 
     * 
     * @return
     *     The value of the material property.
     */
    public List<TRoadLanesLaneSectionLrLaneMaterial> getMaterial() {
        if (material == null) {
            material = new ArrayList<>();
        }
        return this.material;
    }

    /**
     * Gets the value of the speed property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the speed property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getSpeed().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TRoadLanesLaneSectionLrLaneSpeed }
     * </p>
     * 
     * 
     * @return
     *     The value of the speed property.
     */
    public List<TRoadLanesLaneSectionLrLaneSpeed> getSpeed() {
        if (speed == null) {
            speed = new ArrayList<>();
        }
        return this.speed;
    }

    /**
     * Gets the value of the access property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the access property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getAccess().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TRoadLanesLaneSectionLrLaneAccess }
     * </p>
     * 
     * 
     * @return
     *     The value of the access property.
     */
    public List<TRoadLanesLaneSectionLrLaneAccess> getAccess() {
        if (access == null) {
            access = new ArrayList<>();
        }
        return this.access;
    }

    /**
     * Gets the value of the height property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the height property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getHeight().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TRoadLanesLaneSectionLrLaneHeight }
     * </p>
     * 
     * 
     * @return
     *     The value of the height property.
     */
    public List<TRoadLanesLaneSectionLrLaneHeight> getHeight() {
        if (height == null) {
            height = new ArrayList<>();
        }
        return this.height;
    }

    /**
     * Gets the value of the rule property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the rule property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getRule().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TRoadLanesLaneSectionLrLaneRule }
     * </p>
     * 
     * 
     * @return
     *     The value of the rule property.
     */
    public List<TRoadLanesLaneSectionLrLaneRule> getRule() {
        if (rule == null) {
            rule = new ArrayList<>();
        }
        return this.rule;
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
     * Type of the lane. For values see UML model.
     * 
     * @return
     *     possible object is
     *     {@link ELaneType }
     *     
     */
    public ELaneType getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link ELaneType }
     *     
     * @see #getType()
     */
    public void setType(ELaneType value) {
        this.type = value;
    }

    /**
     * "true" = keep lane on level, that is, do not apply superelevation;
     * "false" = apply superelevation to this lane (default, also used if attribute level is missing)
     * 
     * @return
     *     possible object is
     *     {@link TBool }
     *     
     */
    public TBool getLevel() {
        return level;
    }

    /**
     * Sets the value of the level property.
     * 
     * @param value
     *     allowed object is
     *     {@link TBool }
     *     
     * @see #getLevel()
     */
    public void setLevel(TBool value) {
        this.level = value;
    }

}
