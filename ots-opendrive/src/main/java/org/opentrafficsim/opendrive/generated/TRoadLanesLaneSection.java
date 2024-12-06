
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
 * Lanes may be split into multiple lane sections. Each lane section contains a fixed number of lanes. Every time the number of lanes changes, a new lane section is required. The distance between two succeeding lane sections shall not be zero.
 * 
 * <p>Java class for t_road_lanes_laneSection complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="t_road_lanes_laneSection">
 *   <complexContent>
 *     <extension base="{http://code.asam.net/simulation/standard/opendrive_schema}_OpenDriveElement">
 *       <sequence>
 *         <element name="left" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_road_lanes_laneSection_left" minOccurs="0"/>
 *         <element name="center" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_road_lanes_laneSection_center"/>
 *         <element name="right" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_road_lanes_laneSection_right" minOccurs="0"/>
 *         <group ref="{http://code.asam.net/simulation/standard/opendrive_schema}g_additionalData" maxOccurs="unbounded" minOccurs="0"/>
 *       </sequence>
 *       <attribute name="s" use="required" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_grEqZero" />
 *       <attribute name="singleSide" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_bool" />
 *     </extension>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "t_road_lanes_laneSection", propOrder = {
    "left",
    "center",
    "right",
    "gAdditionalData"
})
@SuppressWarnings("all") public class TRoadLanesLaneSection
    extends OpenDriveElement
{

    protected TRoadLanesLaneSectionLeft left;
    @XmlElement(required = true)
    protected TRoadLanesLaneSectionCenter center;
    protected TRoadLanesLaneSectionRight right;
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
     * s-coordinate of start position
     * 
     */
    @XmlAttribute(name = "s", required = true)
    protected double s;
    /**
     * Lane section element is valid for one side only (left, center, or right), depending on the child elements.
     * 
     */
    @XmlAttribute(name = "singleSide")
    protected TBool singleSide;

    /**
     * Gets the value of the left property.
     * 
     * @return
     *     possible object is
     *     {@link TRoadLanesLaneSectionLeft }
     *     
     */
    public TRoadLanesLaneSectionLeft getLeft() {
        return left;
    }

    /**
     * Sets the value of the left property.
     * 
     * @param value
     *     allowed object is
     *     {@link TRoadLanesLaneSectionLeft }
     *     
     */
    public void setLeft(TRoadLanesLaneSectionLeft value) {
        this.left = value;
    }

    /**
     * Gets the value of the center property.
     * 
     * @return
     *     possible object is
     *     {@link TRoadLanesLaneSectionCenter }
     *     
     */
    public TRoadLanesLaneSectionCenter getCenter() {
        return center;
    }

    /**
     * Sets the value of the center property.
     * 
     * @param value
     *     allowed object is
     *     {@link TRoadLanesLaneSectionCenter }
     *     
     */
    public void setCenter(TRoadLanesLaneSectionCenter value) {
        this.center = value;
    }

    /**
     * Gets the value of the right property.
     * 
     * @return
     *     possible object is
     *     {@link TRoadLanesLaneSectionRight }
     *     
     */
    public TRoadLanesLaneSectionRight getRight() {
        return right;
    }

    /**
     * Sets the value of the right property.
     * 
     * @param value
     *     allowed object is
     *     {@link TRoadLanesLaneSectionRight }
     *     
     */
    public void setRight(TRoadLanesLaneSectionRight value) {
        this.right = value;
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
     * s-coordinate of start position
     * 
     */
    public double getS() {
        return s;
    }

    /**
     * Sets the value of the s property.
     * 
     */
    public void setS(double value) {
        this.s = value;
    }

    /**
     * Lane section element is valid for one side only (left, center, or right), depending on the child elements.
     * 
     * @return
     *     possible object is
     *     {@link TBool }
     *     
     */
    public TBool getSingleSide() {
        return singleSide;
    }

    /**
     * Sets the value of the singleSide property.
     * 
     * @param value
     *     allowed object is
     *     {@link TBool }
     *     
     * @see #getSingleSide()
     */
    public void setSingleSide(TBool value) {
        this.singleSide = value;
    }

}
