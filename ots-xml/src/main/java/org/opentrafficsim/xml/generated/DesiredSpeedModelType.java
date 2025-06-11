
package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opentrafficsim.xml.bindings.ClassAdapter;
import org.opentrafficsim.xml.bindings.types.ClassType;


/**
 * <p>Java-Klasse für DesiredSpeedModelType complex type.</p>
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.</p>
 * 
 * <pre>{@code
 * <complexType name="DesiredSpeedModelType">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <choice>
 *         <element name="Idm" type="{http://www.opentrafficsim.org/ots}EmptyType"/>
 *         <element name="Socio" type="{http://www.opentrafficsim.org/ots}DesiredSpeedModelType"/>
 *         <element name="Class" type="{http://www.opentrafficsim.org/ots}ClassNameType"/>
 *       </choice>
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DesiredSpeedModelType", propOrder = {
    "idm",
    "socio",
    "clazz"
})
@SuppressWarnings("all") public class DesiredSpeedModelType
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    @XmlElement(name = "Idm")
    protected EmptyType idm;
    /**
     * SOCIO wraps another desired speed model and adapts it with social
     *             consideration.
     * 
     */
    @XmlElement(name = "Socio")
    protected DesiredSpeedModelType socio;
    @XmlElement(name = "Class", type = String.class)
    @XmlJavaTypeAdapter(ClassAdapter.class)
    protected ClassType clazz;

    /**
     * Ruft den Wert der idm-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link EmptyType }
     *     
     */
    public EmptyType getIdm() {
        return idm;
    }

    /**
     * Legt den Wert der idm-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link EmptyType }
     *     
     */
    public void setIdm(EmptyType value) {
        this.idm = value;
    }

    /**
     * SOCIO wraps another desired speed model and adapts it with social
     *             consideration.
     * 
     * @return
     *     possible object is
     *     {@link DesiredSpeedModelType }
     *     
     */
    public DesiredSpeedModelType getSocio() {
        return socio;
    }

    /**
     * Legt den Wert der socio-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link DesiredSpeedModelType }
     *     
     * @see #getSocio()
     */
    public void setSocio(DesiredSpeedModelType value) {
        this.socio = value;
    }

    /**
     * Ruft den Wert der clazz-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public ClassType getClazz() {
        return clazz;
    }

    /**
     * Legt den Wert der clazz-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setClazz(ClassType value) {
        this.clazz = value;
    }

}
