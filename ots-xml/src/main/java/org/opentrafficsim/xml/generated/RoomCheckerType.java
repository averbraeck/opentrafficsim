
package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opentrafficsim.xml.bindings.DurationAdapter;
import org.opentrafficsim.xml.bindings.types.DurationType;


/**
 * Cf uses equilibrium car-following, CfBa uses non-equilibrium car-following to
 *         increase flow based on
 *         bounded-acceleration, Ttc uses time-to-collision.
 * 
 * <p>Java-Klasse für RoomCheckerType complex type.</p>
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.</p>
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
     * Ruft den Wert der cf-Eigenschaft ab.
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
     * Legt den Wert der cf-Eigenschaft fest.
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
     * Ruft den Wert der cfBa-Eigenschaft ab.
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
     * Legt den Wert der cfBa-Eigenschaft fest.
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
     * Ruft den Wert der ttc-Eigenschaft ab.
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
     * Legt den Wert der ttc-Eigenschaft fest.
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
