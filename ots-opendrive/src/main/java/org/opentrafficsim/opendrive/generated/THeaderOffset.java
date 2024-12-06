
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
 * To avoid large coordinates, an offset of the whole dataset may be applied using the <offset> element. It enables inertial relocation and re-orientation of datasets. The dataset is first translated by @x, @y, and @z. Afterwards, it is rotated by @hdg around the new origin. Rotation around the z-axis should be avoided.In OpenDRIVE, the offset of a database is represented by the <offset> element within the <header> element.
 * 
 * <p>Java class for t_header_Offset complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="t_header_Offset">
 *   <complexContent>
 *     <extension base="{http://code.asam.net/simulation/standard/opendrive_schema}_OpenDriveElement">
 *       <sequence>
 *         <group ref="{http://code.asam.net/simulation/standard/opendrive_schema}g_additionalData" maxOccurs="unbounded" minOccurs="0"/>
 *       </sequence>
 *       <attribute name="x" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       <attribute name="y" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       <attribute name="z" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       <attribute name="hdg" use="required" type="{http://www.w3.org/2001/XMLSchema}float" />
 *     </extension>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "t_header_Offset", propOrder = {
    "gAdditionalData"
})
@SuppressWarnings("all") public class THeaderOffset
    extends OpenDriveElement
{

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
     * Inertial x offset
     * 
     */
    @XmlAttribute(name = "x", required = true)
    protected double x;
    /**
     * Inertial y offset
     * 
     */
    @XmlAttribute(name = "y", required = true)
    protected double y;
    /**
     * Inertial z offset
     * 
     */
    @XmlAttribute(name = "z", required = true)
    protected double z;
    /**
     * Heading offset (rotation around resulting z-axis)
     * 
     */
    @XmlAttribute(name = "hdg", required = true)
    protected float hdg;

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
     * Inertial x offset
     * 
     */
    public double getX() {
        return x;
    }

    /**
     * Sets the value of the x property.
     * 
     */
    public void setX(double value) {
        this.x = value;
    }

    /**
     * Inertial y offset
     * 
     */
    public double getY() {
        return y;
    }

    /**
     * Sets the value of the y property.
     * 
     */
    public void setY(double value) {
        this.y = value;
    }

    /**
     * Inertial z offset
     * 
     */
    public double getZ() {
        return z;
    }

    /**
     * Sets the value of the z property.
     * 
     */
    public void setZ(double value) {
        this.z = value;
    }

    /**
     * Heading offset (rotation around resulting z-axis)
     * 
     */
    public float getHdg() {
        return hdg;
    }

    /**
     * Sets the value of the hdg property.
     * 
     */
    public void setHdg(float value) {
        this.hdg = value;
    }

}
