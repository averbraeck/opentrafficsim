
package org.opentrafficsim.opendrive.generated;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlType;


/**
 * Each type definition shall contain one or more line definitions with additional information about the lines that the road mark is composed of.
 * 
 * <p>Java class for t_road_lanes_laneSection_lcr_lane_roadMark_type complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="t_road_lanes_laneSection_lcr_lane_roadMark_type">
 *   <complexContent>
 *     <extension base="{http://code.asam.net/simulation/standard/opendrive_schema}_OpenDriveElement">
 *       <sequence>
 *         <element name="line" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_road_lanes_laneSection_lcr_lane_roadMark_type_line" maxOccurs="unbounded"/>
 *         <group ref="{http://code.asam.net/simulation/standard/opendrive_schema}g_additionalData" maxOccurs="unbounded" minOccurs="0"/>
 *       </sequence>
 *       <attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       <attribute name="width" use="required" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_grEqZero" />
 *     </extension>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "t_road_lanes_laneSection_lcr_lane_roadMark_type", propOrder = {
    "line",
    "gAdditionalData"
})
@SuppressWarnings("all") public class TRoadLanesLaneSectionLcrLaneRoadMarkType
    extends OpenDriveElement
{

    @XmlElement(required = true)
    protected List<TRoadLanesLaneSectionLcrLaneRoadMarkTypeLine> line;
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
     * Name of the road mark type. May be chosen freely.
     * 
     */
    @XmlAttribute(name = "name", required = true)
    protected String name;
    /**
     * Accumulated width of the road mark. In case of several <line> elements this @width is the sum of all @width of <line> elements and spaces in between, necessary to form the road mark. This attribute supersedes the definition in the <roadMark> element.
     * 
     */
    @XmlAttribute(name = "width", required = true)
    protected double width;

    /**
     * Gets the value of the line property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the line property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getLine().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TRoadLanesLaneSectionLcrLaneRoadMarkTypeLine }
     * </p>
     * 
     * 
     * @return
     *     The value of the line property.
     */
    public List<TRoadLanesLaneSectionLcrLaneRoadMarkTypeLine> getLine() {
        if (line == null) {
            line = new ArrayList<>();
        }
        return this.line;
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
     * Name of the road mark type. May be chosen freely.
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
     * Accumulated width of the road mark. In case of several <line> elements this @width is the sum of all @width of <line> elements and spaces in between, necessary to form the road mark. This attribute supersedes the definition in the <roadMark> element.
     * 
     */
    public double getWidth() {
        return width;
    }

    /**
     * Sets the value of the width property.
     * 
     */
    public void setWidth(double value) {
        this.width = value;
    }

}
