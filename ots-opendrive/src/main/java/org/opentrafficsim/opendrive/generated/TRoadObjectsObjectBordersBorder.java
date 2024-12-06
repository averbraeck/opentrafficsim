
package org.opentrafficsim.opendrive.generated;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * Specifies a border along certain outline points.
 * 
 * <p>Java class for t_road_objects_object_borders_border complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="t_road_objects_object_borders_border">
 *   <complexContent>
 *     <extension base="{http://code.asam.net/simulation/standard/opendrive_schema}_OpenDriveElement">
 *       <sequence>
 *         <element name="cornerReference" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_road_objects_object_markings_marking_cornerReference" maxOccurs="unbounded" minOccurs="0"/>
 *         <group ref="{http://code.asam.net/simulation/standard/opendrive_schema}g_additionalData" maxOccurs="unbounded" minOccurs="0"/>
 *       </sequence>
 *       <attribute name="width" use="required" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_grEqZero" />
 *       <attribute name="type" use="required" type="{http://code.asam.net/simulation/standard/opendrive_schema}e_borderType" />
 *       <attribute name="outlineId" use="required" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger" />
 *       <attribute name="useCompleteOutline" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_bool" />
 *     </extension>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "t_road_objects_object_borders_border", propOrder = {
    "cornerReference",
    "gAdditionalData"
})
@SuppressWarnings("all") public class TRoadObjectsObjectBordersBorder
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
     * Border width
     * 
     */
    @XmlAttribute(name = "width", required = true)
    protected double width;
    /**
     * Appearance of border. For values see UML Model.
     * 
     */
    @XmlAttribute(name = "type", required = true)
    protected EBorderType type;
    /**
     * ID of the outline to use
     * 
     */
    @XmlAttribute(name = "outlineId", required = true)
    @XmlSchemaType(name = "nonNegativeInteger")
    protected BigInteger outlineId;
    /**
     * Use all outline points for border. “true” is used as default.
     * 
     */
    @XmlAttribute(name = "useCompleteOutline")
    protected TBool useCompleteOutline;

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
     * Border width
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

    /**
     * Appearance of border. For values see UML Model.
     * 
     * @return
     *     possible object is
     *     {@link EBorderType }
     *     
     */
    public EBorderType getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link EBorderType }
     *     
     * @see #getType()
     */
    public void setType(EBorderType value) {
        this.type = value;
    }

    /**
     * ID of the outline to use
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getOutlineId() {
        return outlineId;
    }

    /**
     * Sets the value of the outlineId property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     * @see #getOutlineId()
     */
    public void setOutlineId(BigInteger value) {
        this.outlineId = value;
    }

    /**
     * Use all outline points for border. “true” is used as default.
     * 
     * @return
     *     possible object is
     *     {@link TBool }
     *     
     */
    public TBool getUseCompleteOutline() {
        return useCompleteOutline;
    }

    /**
     * Sets the value of the useCompleteOutline property.
     * 
     * @param value
     *     allowed object is
     *     {@link TBool }
     *     
     * @see #getUseCompleteOutline()
     */
    public void setUseCompleteOutline(TBool value) {
        this.useCompleteOutline = value;
    }

}
