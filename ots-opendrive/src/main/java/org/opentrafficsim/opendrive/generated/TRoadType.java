
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
 * A road type element is valid for the entire cross section of a road. It is valid until a new road type element is provided or until the road ends.
 * 
 * <p>Java class for t_road_type complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="t_road_type">
 *   <complexContent>
 *     <extension base="{http://code.asam.net/simulation/standard/opendrive_schema}_OpenDriveElement">
 *       <sequence>
 *         <element name="speed" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_road_type_speed" minOccurs="0"/>
 *         <group ref="{http://code.asam.net/simulation/standard/opendrive_schema}g_additionalData" maxOccurs="unbounded" minOccurs="0"/>
 *       </sequence>
 *       <attribute name="s" use="required" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_grEqZero" />
 *       <attribute name="type" use="required" type="{http://code.asam.net/simulation/standard/opendrive_schema}e_roadType" />
 *       <attribute name="country" type="{http://code.asam.net/simulation/standard/opendrive_schema}e_countryCode" />
 *     </extension>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "t_road_type", propOrder = {
    "speed",
    "gAdditionalData"
})
@SuppressWarnings("all") public class TRoadType
    extends OpenDriveElement
{

    protected TRoadTypeSpeed speed;
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
     * Type of the road defined as enumeration, see UML diagramm
     * 
     */
    @XmlAttribute(name = "type", required = true)
    protected ERoadType type;
    /**
     * Country code of the road, see ISO 3166-1, alpha-2 codes.
     * 
     */
    @XmlAttribute(name = "country")
    protected String country;

    /**
     * Gets the value of the speed property.
     * 
     * @return
     *     possible object is
     *     {@link TRoadTypeSpeed }
     *     
     */
    public TRoadTypeSpeed getSpeed() {
        return speed;
    }

    /**
     * Sets the value of the speed property.
     * 
     * @param value
     *     allowed object is
     *     {@link TRoadTypeSpeed }
     *     
     */
    public void setSpeed(TRoadTypeSpeed value) {
        this.speed = value;
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
     * Type of the road defined as enumeration, see UML diagramm
     * 
     * @return
     *     possible object is
     *     {@link ERoadType }
     *     
     */
    public ERoadType getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link ERoadType }
     *     
     * @see #getType()
     */
    public void setType(ERoadType value) {
        this.type = value;
    }

    /**
     * Country code of the road, see ISO 3166-1, alpha-2 codes.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCountry() {
        return country;
    }

    /**
     * Sets the value of the country property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     * @see #getCountry()
     */
    public void setCountry(String value) {
        this.country = value;
    }

}
