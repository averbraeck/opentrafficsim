
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
 * Specifies a marking that is either attached to one side of the object bounding box or referencing outline points.
 * 
 * <p>Java class for t_road_objects_object_markings_marking complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="t_road_objects_object_markings_marking">
 *   <complexContent>
 *     <extension base="{http://code.asam.net/simulation/standard/opendrive_schema}_OpenDriveElement">
 *       <sequence>
 *         <element name="cornerReference" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_road_objects_object_markings_marking_cornerReference" maxOccurs="unbounded" minOccurs="0"/>
 *         <group ref="{http://code.asam.net/simulation/standard/opendrive_schema}g_additionalData" maxOccurs="unbounded" minOccurs="0"/>
 *       </sequence>
 *       <attribute name="side" type="{http://code.asam.net/simulation/standard/opendrive_schema}e_sideType" />
 *       <attribute name="weight" type="{http://code.asam.net/simulation/standard/opendrive_schema}e_roadMarkWeight" />
 *       <attribute name="width" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       <attribute name="color" use="required" type="{http://code.asam.net/simulation/standard/opendrive_schema}e_roadMarkColor" />
 *       <attribute name="zOffset" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_grEqZero" />
 *       <attribute name="spaceLength" use="required" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_grEqZero" />
 *       <attribute name="lineLength" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       <attribute name="startOffset" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       <attribute name="stopOffset" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *     </extension>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "t_road_objects_object_markings_marking", propOrder = {
    "cornerReference",
    "gAdditionalData"
})
@SuppressWarnings("all") public class TRoadObjectsObjectMarkingsMarking
    extends OpenDriveElement
{

    protected List<TRoadObjectsObjectMarkingsMarkingCornerReference> cornerReference;
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
     * Side of the bounding box described in <object> element in the local coordinate system u/v. For values see UML model.
     * 
     */
    @XmlAttribute(name = "side")
    protected ESideType side;
    /**
     * Optical "weight" of the marking. For values see UML model.
     * 
     */
    @XmlAttribute(name = "weight")
    protected ERoadMarkWeight weight;
    /**
     * Width of the marking.
     * 
     */
    @XmlAttribute(name = "width")
    protected String width;
    /**
     * Color of the marking. For values see UML model.
     * 
     */
    @XmlAttribute(name = "color", required = true)
    protected ERoadMarkColor color;
    /**
     * Height of road mark above the road, i.e. thickness of the road mark
     * 
     */
    @XmlAttribute(name = "zOffset")
    protected Double zOffset;
    /**
     * Length of the gap between the visible parts
     * 
     */
    @XmlAttribute(name = "spaceLength", required = true)
    protected double spaceLength;
    /**
     * Length of the visible part
     * 
     */
    @XmlAttribute(name = "lineLength", required = true)
    protected String lineLength;
    /**
     * Lateral offset in u-direction from start of bounding box side where the first marking starts
     * 
     */
    @XmlAttribute(name = "startOffset", required = true)
    protected double startOffset;
    /**
     * Lateral offset in u-direction from end of bounding box side where the marking ends
     * 
     */
    @XmlAttribute(name = "stopOffset", required = true)
    protected double stopOffset;

    /**
     * Gets the value of the cornerReference property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the cornerReference property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getCornerReference().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TRoadObjectsObjectMarkingsMarkingCornerReference }
     * </p>
     * 
     * 
     * @return
     *     The value of the cornerReference property.
     */
    public List<TRoadObjectsObjectMarkingsMarkingCornerReference> getCornerReference() {
        if (cornerReference == null) {
            cornerReference = new ArrayList<>();
        }
        return this.cornerReference;
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
     * Side of the bounding box described in <object> element in the local coordinate system u/v. For values see UML model.
     * 
     * @return
     *     possible object is
     *     {@link ESideType }
     *     
     */
    public ESideType getSide() {
        return side;
    }

    /**
     * Sets the value of the side property.
     * 
     * @param value
     *     allowed object is
     *     {@link ESideType }
     *     
     * @see #getSide()
     */
    public void setSide(ESideType value) {
        this.side = value;
    }

    /**
     * Optical "weight" of the marking. For values see UML model.
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
     * Width of the marking.
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
     * Color of the marking. For values see UML model.
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
     * Height of road mark above the road, i.e. thickness of the road mark
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getZOffset() {
        return zOffset;
    }

    /**
     * Sets the value of the zOffset property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     * @see #getZOffset()
     */
    public void setZOffset(Double value) {
        this.zOffset = value;
    }

    /**
     * Length of the gap between the visible parts
     * 
     */
    public double getSpaceLength() {
        return spaceLength;
    }

    /**
     * Sets the value of the spaceLength property.
     * 
     */
    public void setSpaceLength(double value) {
        this.spaceLength = value;
    }

    /**
     * Length of the visible part
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLineLength() {
        return lineLength;
    }

    /**
     * Sets the value of the lineLength property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     * @see #getLineLength()
     */
    public void setLineLength(String value) {
        this.lineLength = value;
    }

    /**
     * Lateral offset in u-direction from start of bounding box side where the first marking starts
     * 
     */
    public double getStartOffset() {
        return startOffset;
    }

    /**
     * Sets the value of the startOffset property.
     * 
     */
    public void setStartOffset(double value) {
        this.startOffset = value;
    }

    /**
     * Lateral offset in u-direction from end of bounding box side where the marking ends
     * 
     */
    public double getStopOffset() {
        return stopOffset;
    }

    /**
     * Sets the value of the stopOffset property.
     * 
     */
    public void setStopOffset(double value) {
        this.stopOffset = value;
    }

}
