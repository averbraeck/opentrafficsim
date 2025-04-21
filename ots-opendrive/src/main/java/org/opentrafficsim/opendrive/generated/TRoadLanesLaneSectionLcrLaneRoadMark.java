
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
import org.opentrafficsim.opendrive.bindings.Stripe.Type;
import org.opentrafficsim.opendrive.bindings.StripeTypeAdapter;


/**
 * Defines the style of the line at the outer border of a lane. The style of the center line that separates left and right lanes is determined by the road mark element for the center lane.
 * 
 * <p>Java class for t_road_lanes_laneSection_lcr_lane_roadMark complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="t_road_lanes_laneSection_lcr_lane_roadMark">
 *   <complexContent>
 *     <extension base="{http://code.asam.net/simulation/standard/opendrive_schema}_OpenDriveElement">
 *       <sequence>
 *         <element name="sway" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_road_lanes_laneSection_lcr_lane_roadMark_sway" maxOccurs="unbounded" minOccurs="0"/>
 *         <element name="type" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_road_lanes_laneSection_lcr_lane_roadMark_type" minOccurs="0"/>
 *         <element name="explicit" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_road_lanes_laneSection_lcr_lane_roadMark_explicit" minOccurs="0"/>
 *         <group ref="{http://code.asam.net/simulation/standard/opendrive_schema}g_additionalData" maxOccurs="unbounded" minOccurs="0"/>
 *       </sequence>
 *       <attribute name="sOffset" use="required" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_grEqZero" />
 *       <attribute name="type" use="required" type="{http://code.asam.net/simulation/standard/opendrive_schema}e_roadMarkType" />
 *       <attribute name="weight" type="{http://code.asam.net/simulation/standard/opendrive_schema}e_roadMarkWeight" />
 *       <attribute name="color" use="required" type="{http://code.asam.net/simulation/standard/opendrive_schema}e_roadMarkColor" />
 *       <attribute name="material" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       <attribute name="width" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_grEqZero" />
 *       <attribute name="laneChange" type="{http://code.asam.net/simulation/standard/opendrive_schema}e_road_lanes_laneSection_lcr_lane_roadMark_laneChange" />
 *       <attribute name="height" type="{http://www.w3.org/2001/XMLSchema}double" />
 *     </extension>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "t_road_lanes_laneSection_lcr_lane_roadMark", propOrder = {
    "sway",
    "type",
    "explicit",
    "gAdditionalData"
})
@SuppressWarnings("all") public class TRoadLanesLaneSectionLcrLaneRoadMark
    extends OpenDriveElement
{

    protected List<TRoadLanesLaneSectionLcrLaneRoadMarkSway> sway;
    protected TRoadLanesLaneSectionLcrLaneRoadMarkType type;
    protected TRoadLanesLaneSectionLcrLaneRoadMarkExplicit explicit;
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
     * s-coordinate of start position of the <roadMark> element, relative to the position of the preceding <laneSection> element
     * 
     */
    @XmlAttribute(name = "sOffset", required = true)
    protected double sOffset;
    /**
     * Type of the road mark. For values see UML model.
     * 
     */
    @XmlAttribute(name = "type", required = true)
    @XmlJavaTypeAdapter(StripeTypeAdapter.class)
    protected Type roadMarkType;
    /**
     * Weight of the road mark. This attribute is optional if detailed definition is given below. For values see UML model.
     * 
     */
    @XmlAttribute(name = "weight")
    protected ERoadMarkWeight weight;
    /**
     * Color of the road mark. For values see UML model.
     * 
     */
    @XmlAttribute(name = "color", required = true)
    protected ERoadMarkColor color;
    /**
     * Material of the road mark. Identifiers to be defined by the user, use "standard" as default value.
     * 
     */
    @XmlAttribute(name = "material")
    protected String material;
    /**
     * Width of the road mark. This attribute is optional if detailed definition is given by <line> element.
     * 
     */
    @XmlAttribute(name = "width")
    protected Double width;
    /**
     * Allows a lane change in the indicated direction, taking into account that lanes are numbered in ascending order from right to left. If the attribute is missing, “both” is used as default. For values see UML model.
     * 
     */
    @XmlAttribute(name = "laneChange")
    protected ERoadLanesLaneSectionLcrLaneRoadMarkLaneChange laneChange;
    /**
     * Height of road mark above the road, i.e. thickness of the road mark.
     * 
     */
    @XmlAttribute(name = "height")
    protected Double height;

    /**
     * Gets the value of the sway property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the sway property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getSway().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TRoadLanesLaneSectionLcrLaneRoadMarkSway }
     * </p>
     * 
     * 
     * @return
     *     The value of the sway property.
     */
    public List<TRoadLanesLaneSectionLcrLaneRoadMarkSway> getSway() {
        if (sway == null) {
            sway = new ArrayList<>();
        }
        return this.sway;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link TRoadLanesLaneSectionLcrLaneRoadMarkType }
     *     
     */
    public TRoadLanesLaneSectionLcrLaneRoadMarkType getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link TRoadLanesLaneSectionLcrLaneRoadMarkType }
     *     
     */
    public void setType(TRoadLanesLaneSectionLcrLaneRoadMarkType value) {
        this.type = value;
    }

    /**
     * Gets the value of the explicit property.
     * 
     * @return
     *     possible object is
     *     {@link TRoadLanesLaneSectionLcrLaneRoadMarkExplicit }
     *     
     */
    public TRoadLanesLaneSectionLcrLaneRoadMarkExplicit getExplicit() {
        return explicit;
    }

    /**
     * Sets the value of the explicit property.
     * 
     * @param value
     *     allowed object is
     *     {@link TRoadLanesLaneSectionLcrLaneRoadMarkExplicit }
     *     
     */
    public void setExplicit(TRoadLanesLaneSectionLcrLaneRoadMarkExplicit value) {
        this.explicit = value;
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
     * s-coordinate of start position of the <roadMark> element, relative to the position of the preceding <laneSection> element
     * 
     */
    public double getSOffset() {
        return sOffset;
    }

    /**
     * Sets the value of the sOffset property.
     * 
     */
    public void setSOffset(double value) {
        this.sOffset = value;
    }

    /**
     * Type of the road mark. For values see UML model.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Type getRoadMarkType() {
        return roadMarkType;
    }

    /**
     * Sets the value of the roadMarkType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     * @see #getRoadMarkType()
     */
    public void setRoadMarkType(Type value) {
        this.roadMarkType = value;
    }

    /**
     * Weight of the road mark. This attribute is optional if detailed definition is given below. For values see UML model.
     * 
     * @return
     *     possible object is
     *     {@link ERoadMarkWeight }
     *     
     */
    public ERoadMarkWeight getWeight() {
        return weight;
    }

    /**
     * Sets the value of the weight property.
     * 
     * @param value
     *     allowed object is
     *     {@link ERoadMarkWeight }
     *     
     * @see #getWeight()
     */
    public void setWeight(ERoadMarkWeight value) {
        this.weight = value;
    }

    /**
     * Color of the road mark. For values see UML model.
     * 
     * @return
     *     possible object is
     *     {@link ERoadMarkColor }
     *     
     */
    public ERoadMarkColor getColor() {
        return color;
    }

    /**
     * Sets the value of the color property.
     * 
     * @param value
     *     allowed object is
     *     {@link ERoadMarkColor }
     *     
     * @see #getColor()
     */
    public void setColor(ERoadMarkColor value) {
        this.color = value;
    }

    /**
     * Material of the road mark. Identifiers to be defined by the user, use "standard" as default value.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMaterial() {
        return material;
    }

    /**
     * Sets the value of the material property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     * @see #getMaterial()
     */
    public void setMaterial(String value) {
        this.material = value;
    }

    /**
     * Width of the road mark. This attribute is optional if detailed definition is given by <line> element.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getWidth() {
        return width;
    }

    /**
     * Sets the value of the width property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     * @see #getWidth()
     */
    public void setWidth(Double value) {
        this.width = value;
    }

    /**
     * Allows a lane change in the indicated direction, taking into account that lanes are numbered in ascending order from right to left. If the attribute is missing, “both” is used as default. For values see UML model.
     * 
     * @return
     *     possible object is
     *     {@link ERoadLanesLaneSectionLcrLaneRoadMarkLaneChange }
     *     
     */
    public ERoadLanesLaneSectionLcrLaneRoadMarkLaneChange getLaneChange() {
        return laneChange;
    }

    /**
     * Sets the value of the laneChange property.
     * 
     * @param value
     *     allowed object is
     *     {@link ERoadLanesLaneSectionLcrLaneRoadMarkLaneChange }
     *     
     * @see #getLaneChange()
     */
    public void setLaneChange(ERoadLanesLaneSectionLcrLaneRoadMarkLaneChange value) {
        this.laneChange = value;
    }

    /**
     * Height of road mark above the road, i.e. thickness of the road mark.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getHeight() {
        return height;
    }

    /**
     * Sets the value of the height property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     * @see #getHeight()
     */
    public void setHeight(Double value) {
        this.height = value;
    }

}
