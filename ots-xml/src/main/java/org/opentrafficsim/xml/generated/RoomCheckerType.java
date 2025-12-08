
package org.opentrafficsim.xml.generated;

import java.io.Serializable;

import org.opentrafficsim.xml.bindings.DurationAdapter;
import org.opentrafficsim.xml.bindings.types.DurationType;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * Cf uses equilibrium car-following, CfBa uses non-equilibrium car-following to
 *         increase flow based on
 *         bounded-acceleration, Ttc uses time-to-collision.
 * 
 * <p>Java class for RoomCheckerType complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="RoomCheckerType">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <choice>
 *         <element name="Cf" type="{http://www.opentrafficsim.org/ots}EmptyType"/>
 *         <element name="CfBa" type="{http://www.opentrafficsim.org/ots}EmptyType"/>
 *         <element name="Ttc" type="{http://www.opentrafficsim.org/ots}DurationType"/>
 *       </choice>
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RoomCheckerType", propOrder = {
    "cf",
    "cfBa",
    "ttc"
})
@SuppressWarnings("all") public class RoomCheckerType
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    @XmlElement(name = "Cf")
    protected EmptyType cf;
    @XmlElement(name = "CfBa")
    protected EmptyType cfBa;
    @XmlElement(name = "Ttc", type = String.class)
    @XmlJavaTypeAdapter(DurationAdapter.class)
    protected DurationType ttc;

    /**
     * Gets the value of the cf property.
     * 
     * @return
     *     possible object is
     *     {@link EmptyType }
     *     
     */
    public EmptyType getCf() {
        return cf;
    }

    /**
     * Sets the value of the cf property.
     * 
     * @param value
     *     allowed object is
     *     {@link EmptyType }
     *     
     */
    public void setCf(EmptyType value) {
        this.cf = value;
    }

    /**
     * Gets the value of the cfBa property.
     * 
     * @return
     *     possible object is
     *     {@link EmptyType }
     *     
     */
    public EmptyType getCfBa() {
        return cfBa;
    }

    /**
     * Sets the value of the cfBa property.
     * 
     * @param value
     *     allowed object is
     *     {@link EmptyType }
     *     
     */
    public void setCfBa(EmptyType value) {
        this.cfBa = value;
    }

    /**
     * Gets the value of the ttc property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public DurationType getTtc() {
        return ttc;
    }

    /**
     * Sets the value of the ttc property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTtc(DurationType value) {
        this.ttc = value;
    }

}
