
package org.opentrafficsim.opendrive.generated;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;


/**
 * A road mark may consist of one or more elements. Multiple elements are usually positioned side-by-side. A line definition is valid for a given length of the lane and will be repeated automatically.
 * 
 * <p>Java class for t_road_lanes_laneSection_lcr_lane_roadMark_type_line complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="t_road_lanes_laneSection_lcr_lane_roadMark_type_line">
 *   <complexContent>
 *     <extension base="{http://code.asam.net/simulation/standard/opendrive_schema}_OpenDriveElement">
 *       <sequence>
 *       </sequence>
 *       <attribute name="length" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       <attribute name="space" use="required" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_grEqZero" />
 *       <attribute name="tOffset" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       <attribute name="sOffset" use="required" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_grEqZero" />
 *       <attribute name="rule" type="{http://code.asam.net/simulation/standard/opendrive_schema}e_roadMarkRule" />
 *       <attribute name="width" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       <attribute name="color" type="{http://code.asam.net/simulation/standard/opendrive_schema}e_roadMarkColor" />
 *     </extension>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "t_road_lanes_laneSection_lcr_lane_roadMark_type_line")
@SuppressWarnings("all") public class TRoadLanesLaneSectionLcrLaneRoadMarkTypeLine
    extends OpenDriveElement
{

    /**
     * Length of the visible part
     * 
     */
    @XmlAttribute(name = "length", required = true)
    protected String length;
    /**
     * Length of the gap between the visible parts
     * 
     */
    @XmlAttribute(name = "space", required = true)
    protected double space;
    /**
     * Lateral offset from the lane border.
     * If <sway> element is present, the lateral offset follows the sway.
     * 
     */
    @XmlAttribute(name = "tOffset", required = true)
    protected double tOffset;
    /**
     * Initial longitudinal offset of the line definition from the start of the road mark definition
     * 
     */
    @XmlAttribute(name = "sOffset", required = true)
    protected double sOffset;
    /**
     * Rule that must be observed when passing the line from inside, for example, from the lane with the lower absolute ID to the lane with the higher absolute ID. For values see UML Model.
     * 
     */
    @XmlAttribute(name = "rule")
    protected ERoadMarkRule rule;
    /**
     * Line width
     * 
     */
    @XmlAttribute(name = "width")
    protected String width;
    /**
     * Line color. If given, this attribute supersedes the definition in the <roadMark> element. For values see UML Model.
     * 
     */
    @XmlAttribute(name = "color")
    protected ERoadMarkColor color;

    /**
     * Length of the visible part
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLength() {
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
    public void setLength(String value) {
        this.length = value;
    }

    /**
     * Length of the gap between the visible parts
     * 
     */
    public double getSpace() {
        return space;
    }

    /**
     * Sets the value of the space property.
     * 
     */
    public void setSpace(double value) {
        this.space = value;
    }

    /**
     * Lateral offset from the lane border.
     * If <sway> element is present, the lateral offset follows the sway.
     * 
     */
    public double getTOffset() {
        return tOffset;
    }

    /**
     * Sets the value of the tOffset property.
     * 
     */
    public void setTOffset(double value) {
        this.tOffset = value;
    }

    /**
     * Initial longitudinal offset of the line definition from the start of the road mark definition
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
     * Rule that must be observed when passing the line from inside, for example, from the lane with the lower absolute ID to the lane with the higher absolute ID. For values see UML Model.
     * 
     * @return
     *     possible object is
     *     {@link ERoadMarkRule }
     *     
     */
    public ERoadMarkRule getRule() {
        return rule;
    }

    /**
     * Sets the value of the rule property.
     * 
     * @param value
     *     allowed object is
     *     {@link ERoadMarkRule }
     *     
     * @see #getRule()
     */
    public void setRule(ERoadMarkRule value) {
        this.rule = value;
    }

    /**
     * Line width
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWidth() {
        return width;
    }

    /**
     * Sets the value of the width property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     * @see #getWidth()
     */
    public void setWidth(String value) {
        this.width = value;
    }

    /**
     * Line color. If given, this attribute supersedes the definition in the <roadMark> element. For values see UML Model.
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

}
