
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
 * <p>Java class for DesiredSpeedModelType complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
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
     * Socio wraps another desired speed model and adapts it with social
     *             consideration.
     * 
     */
    @XmlElement(name = "Socio")
    protected DesiredSpeedModelType socio;
    @XmlElement(name = "Class", type = String.class)
    @XmlJavaTypeAdapter(ClassAdapter.class)
    protected ClassType clazz;

    /**
     * Gets the value of the idm property.
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
     * Sets the value of the idm property.
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
     * Socio wraps another desired speed model and adapts it with social
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
     * Sets the value of the socio property.
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
     * Gets the value of the clazz property.
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
     * Sets the value of the clazz property.
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
