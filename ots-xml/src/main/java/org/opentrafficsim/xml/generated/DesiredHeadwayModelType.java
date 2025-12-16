
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
 * <p>Java class for DesiredHeadwayModelType complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="DesiredHeadwayModelType">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <choice>
 *         <element name="Idm" type="{http://www.opentrafficsim.org/ots}EmptyType"/>
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
@XmlType(name = "DesiredHeadwayModelType", propOrder = {
    "idm",
    "clazz"
})
@SuppressWarnings("all") public class DesiredHeadwayModelType
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    @XmlElement(name = "Idm")
    protected EmptyType idm;
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
