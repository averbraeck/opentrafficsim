
package org.opentrafficsim.opendrive.generated;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;


/**
 * Specifies a single line in an explicit road mark definition.
 * 
 * <p>Java class for t_road_lanes_laneSection_lcr_lane_roadMark_explicit_line complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="t_road_lanes_laneSection_lcr_lane_roadMark_explicit_line">
 *   <complexContent>
 *     <extension base="{http://code.asam.net/simulation/standard/opendrive_schema}_OpenDriveElement">
 *       <sequence>
 *       </sequence>
 *       <attribute name="length" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       <attribute name="tOffset" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       <attribute name="sOffset" use="required" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_grEqZero" />
 *       <attribute name="rule" type="{http://code.asam.net/simulation/standard/opendrive_schema}e_roadMarkRule" />
 *       <attribute name="width" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     </extension>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "t_road_lanes_laneSection_lcr_lane_roadMark_explicit_line")
@SuppressWarnings("all") public class TRoadLanesLaneSectionLcrLaneRoadMarkExplicitLine
    extends OpenDriveElement
{

    /**
     * Length of the visible line
     * 
     */
    @XmlAttribute(name = "length", required = true)
    protected String length;
    /**
     * Lateral offset from the lane border.
     * If <sway> element is present, the lateral offset follows the sway.
     * 
     */
    @XmlAttribute(name = "tOffset", required = true)
    protected double tOffset;
    /**
     * Offset of start position of the <line> element, relative to the @sOffset  given in the <roadMark> element
     * 
     */
    @XmlAttribute(name = "sOffset", required = true)
    protected double sOffset;
    /**
     * Rule that must be observed when passing the line from inside, that is, from the lane with the lower absolute ID to the lane with the higher absolute ID. For values see UML Model.
     * 
     */
    @XmlAttribute(name = "rule")
    protected ERoadMarkRule rule;
    /**
     * Line width. This attribute supersedes the definition in the <roadMark> element.
     * 
     */
    @XmlAttribute(name = "width")
    protected String width;

    /**
     * Length of the visible line
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
     * Offset of start position of the <line> element, relative to the @sOffset  given in the <roadMark> element
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
     * Rule that must be observed when passing the line from inside, that is, from the lane with the lower absolute ID to the lane with the higher absolute ID. For values see UML Model.
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
     * Line width. This attribute supersedes the definition in the <roadMark> element.
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

}
