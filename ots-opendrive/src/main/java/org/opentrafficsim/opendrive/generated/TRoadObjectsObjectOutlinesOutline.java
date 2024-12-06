
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
 * Defines a series of corner points, including the height of the object relative to the road reference line. For areas, the points should be listed in counter-clockwise order.
 * An <outline> element shall be followed by one or more <cornerRoad> element or by one or more <cornerLocal> element.
 * 
 * OpenDRIVE 1.4 outline definitions (without <outlines> parent element) shall still be supported.
 * 
 * <p>Java class for t_road_objects_object_outlines_outline complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="t_road_objects_object_outlines_outline">
 *   <complexContent>
 *     <extension base="{http://code.asam.net/simulation/standard/opendrive_schema}_OpenDriveElement">
 *       <sequence>
 *         <choice minOccurs="0">
 *           <element name="cornerRoad" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_road_objects_object_outlines_outline_cornerRoad" maxOccurs="unbounded" minOccurs="0"/>
 *           <element name="cornerLocal" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_road_objects_object_outlines_outline_cornerLocal" maxOccurs="unbounded" minOccurs="0"/>
 *         </choice>
 *         <group ref="{http://code.asam.net/simulation/standard/opendrive_schema}g_additionalData" maxOccurs="unbounded" minOccurs="0"/>
 *       </sequence>
 *       <attribute name="id" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger" />
 *       <attribute name="fillType" type="{http://code.asam.net/simulation/standard/opendrive_schema}e_outlineFillType" />
 *       <attribute name="outer" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_bool" />
 *       <attribute name="closed" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_bool" />
 *       <attribute name="laneType" type="{http://code.asam.net/simulation/standard/opendrive_schema}e_laneType" />
 *     </extension>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "t_road_objects_object_outlines_outline", propOrder = {
    "cornerRoad",
    "cornerLocal",
    "gAdditionalData"
})
@SuppressWarnings("all") public class TRoadObjectsObjectOutlinesOutline
    extends OpenDriveElement
{

    protected List<TRoadObjectsObjectOutlinesOutlineCornerRoad> cornerRoad;
    protected List<TRoadObjectsObjectOutlinesOutlineCornerLocal> cornerLocal;
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
     * ID of the outline. Must be unique within one object.
     * 
     */
    @XmlAttribute(name = "id")
    @XmlSchemaType(name = "nonNegativeInteger")
    protected BigInteger id;
    /**
     * Type used to fill the area inside the outline. For values see UML Model.
     * 
     */
    @XmlAttribute(name = "fillType")
    protected EOutlineFillType fillType;
    /**
     * Defines if outline is an outer outline of the object.
     * 
     */
    @XmlAttribute(name = "outer")
    protected TBool outer;
    /**
     * If true, the outline describes an area, not a linear feature.
     * 
     */
    @XmlAttribute(name = "closed")
    protected TBool closed;
    /**
     * Describes the lane type of the outline. For values see UML Model.
     * 
     */
    @XmlAttribute(name = "laneType")
    protected ELaneType laneType;

    /**
     * Gets the value of the cornerRoad property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the cornerRoad property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getCornerRoad().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TRoadObjectsObjectOutlinesOutlineCornerRoad }
     * </p>
     * 
     * 
     * @return
     *     The value of the cornerRoad property.
     */
    public List<TRoadObjectsObjectOutlinesOutlineCornerRoad> getCornerRoad() {
        if (cornerRoad == null) {
            cornerRoad = new ArrayList<>();
        }
        return this.cornerRoad;
    }

    /**
     * Gets the value of the cornerLocal property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the cornerLocal property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getCornerLocal().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TRoadObjectsObjectOutlinesOutlineCornerLocal }
     * </p>
     * 
     * 
     * @return
     *     The value of the cornerLocal property.
     */
    public List<TRoadObjectsObjectOutlinesOutlineCornerLocal> getCornerLocal() {
        if (cornerLocal == null) {
            cornerLocal = new ArrayList<>();
        }
        return this.cornerLocal;
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
     * ID of the outline. Must be unique within one object.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     * @see #getId()
     */
    public void setId(BigInteger value) {
        this.id = value;
    }

    /**
     * Type used to fill the area inside the outline. For values see UML Model.
     * 
     * @return
     *     possible object is
     *     {@link EOutlineFillType }
     *     
     */
    public EOutlineFillType getFillType() {
        return fillType;
    }

    /**
     * Sets the value of the fillType property.
     * 
     * @param value
     *     allowed object is
     *     {@link EOutlineFillType }
     *     
     * @see #getFillType()
     */
    public void setFillType(EOutlineFillType value) {
        this.fillType = value;
    }

    /**
     * Defines if outline is an outer outline of the object.
     * 
     * @return
     *     possible object is
     *     {@link TBool }
     *     
     */
    public TBool getOuter() {
        return outer;
    }

    /**
     * Sets the value of the outer property.
     * 
     * @param value
     *     allowed object is
     *     {@link TBool }
     *     
     * @see #getOuter()
     */
    public void setOuter(TBool value) {
        this.outer = value;
    }

    /**
     * If true, the outline describes an area, not a linear feature.
     * 
     * @return
     *     possible object is
     *     {@link TBool }
     *     
     */
    public TBool getClosed() {
        return closed;
    }

    /**
     * Sets the value of the closed property.
     * 
     * @param value
     *     allowed object is
     *     {@link TBool }
     *     
     * @see #getClosed()
     */
    public void setClosed(TBool value) {
        this.closed = value;
    }

    /**
     * Describes the lane type of the outline. For values see UML Model.
     * 
     * @return
     *     possible object is
     *     {@link ELaneType }
     *     
     */
    public ELaneType getLaneType() {
        return laneType;
    }

    /**
     * Sets the value of the laneType property.
     * 
     * @param value
     *     allowed object is
     *     {@link ELaneType }
     *     
     * @see #getLaneType()
     */
    public void setLaneType(ELaneType value) {
        this.laneType = value;
    }

}
