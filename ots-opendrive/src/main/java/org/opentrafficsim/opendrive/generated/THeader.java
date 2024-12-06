
package org.opentrafficsim.opendrive.generated;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlType;


/**
 * The <header> element is the very first element within the <OpenDRIVE> element.
 * 
 * <p>Java class for t_header complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="t_header">
 *   <complexContent>
 *     <extension base="{http://code.asam.net/simulation/standard/opendrive_schema}_OpenDriveElement">
 *       <sequence>
 *         <element name="geoReference" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_header_GeoReference" minOccurs="0"/>
 *         <element name="offset" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_header_Offset" minOccurs="0"/>
 *         <group ref="{http://code.asam.net/simulation/standard/opendrive_schema}g_additionalData" maxOccurs="unbounded" minOccurs="0"/>
 *       </sequence>
 *       <attribute name="revMajor" use="required" type="{http://www.w3.org/2001/XMLSchema}integer" fixed="1" />
 *       <attribute name="revMinor" use="required" type="{http://www.w3.org/2001/XMLSchema}integer" />
 *       <attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       <attribute name="version" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       <attribute name="date" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       <attribute name="north" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       <attribute name="south" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       <attribute name="east" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       <attribute name="west" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       <attribute name="vendor" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     </extension>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "t_header", propOrder = {
    "geoReference",
    "offset",
    "gAdditionalData"
})
@SuppressWarnings("all") public class THeader
    extends OpenDriveElement
{

    protected THeaderGeoReference geoReference;
    protected THeaderOffset offset;
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
     * Major revision number of OpenDRIVE format
     * 
     */
    @XmlAttribute(name = "revMajor", required = true)
    protected BigInteger revMajor;
    /**
     * Minor revision number of OpenDRIVE format; 6 for OpenDrive 1.6
     * 
     */
    @XmlAttribute(name = "revMinor", required = true)
    protected BigInteger revMinor;
    /**
     * Database name
     * 
     */
    @XmlAttribute(name = "name")
    protected String name;
    /**
     * Version of this road network
     * 
     */
    @XmlAttribute(name = "version")
    protected String version;
    /**
     * Time/date of database creation according to ISO 8601 
     * (preference: YYYY-MM-DDThh:mm:ss)
     * 
     */
    @XmlAttribute(name = "date")
    protected String date;
    /**
     * Maximum inertial y value
     * 
     */
    @XmlAttribute(name = "north")
    protected Double north;
    /**
     * Minimum inertial y value
     * 
     */
    @XmlAttribute(name = "south")
    protected Double south;
    /**
     * Maximum inertial x value
     * 
     */
    @XmlAttribute(name = "east")
    protected Double east;
    /**
     * Minimum inertial x value
     * 
     */
    @XmlAttribute(name = "west")
    protected Double west;
    /**
     * Vendor name
     * 
     */
    @XmlAttribute(name = "vendor")
    protected String vendor;

    /**
     * Gets the value of the geoReference property.
     * 
     * @return
     *     possible object is
     *     {@link THeaderGeoReference }
     *     
     */
    public THeaderGeoReference getGeoReference() {
        return geoReference;
    }

    /**
     * Sets the value of the geoReference property.
     * 
     * @param value
     *     allowed object is
     *     {@link THeaderGeoReference }
     *     
     */
    public void setGeoReference(THeaderGeoReference value) {
        this.geoReference = value;
    }

    /**
     * Gets the value of the offset property.
     * 
     * @return
     *     possible object is
     *     {@link THeaderOffset }
     *     
     */
    public THeaderOffset getOffset() {
        return offset;
    }

    /**
     * Sets the value of the offset property.
     * 
     * @param value
     *     allowed object is
     *     {@link THeaderOffset }
     *     
     */
    public void setOffset(THeaderOffset value) {
        this.offset = value;
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
     * Major revision number of OpenDRIVE format
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getRevMajor() {
        if (revMajor == null) {
            return new BigInteger("1");
        } else {
            return revMajor;
        }
    }

    /**
     * Sets the value of the revMajor property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     * @see #getRevMajor()
     */
    public void setRevMajor(BigInteger value) {
        this.revMajor = value;
    }

    /**
     * Minor revision number of OpenDRIVE format; 6 for OpenDrive 1.6
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getRevMinor() {
        return revMinor;
    }

    /**
     * Sets the value of the revMinor property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     * @see #getRevMinor()
     */
    public void setRevMinor(BigInteger value) {
        this.revMinor = value;
    }

    /**
     * Database name
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
     * Version of this road network
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the value of the version property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     * @see #getVersion()
     */
    public void setVersion(String value) {
        this.version = value;
    }

    /**
     * Time/date of database creation according to ISO 8601 
     * (preference: YYYY-MM-DDThh:mm:ss)
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDate() {
        return date;
    }

    /**
     * Sets the value of the date property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     * @see #getDate()
     */
    public void setDate(String value) {
        this.date = value;
    }

    /**
     * Maximum inertial y value
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getNorth() {
        return north;
    }

    /**
     * Sets the value of the north property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     * @see #getNorth()
     */
    public void setNorth(Double value) {
        this.north = value;
    }

    /**
     * Minimum inertial y value
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getSouth() {
        return south;
    }

    /**
     * Sets the value of the south property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     * @see #getSouth()
     */
    public void setSouth(Double value) {
        this.south = value;
    }

    /**
     * Maximum inertial x value
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getEast() {
        return east;
    }

    /**
     * Sets the value of the east property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     * @see #getEast()
     */
    public void setEast(Double value) {
        this.east = value;
    }

    /**
     * Minimum inertial x value
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getWest() {
        return west;
    }

    /**
     * Sets the value of the west property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     * @see #getWest()
     */
    public void setWest(Double value) {
        this.west = value;
    }

    /**
     * Vendor name
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVendor() {
        return vendor;
    }

    /**
     * Sets the value of the vendor property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     * @see #getVendor()
     */
    public void setVendor(String value) {
        this.vendor = value;
    }

}
