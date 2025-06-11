
package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlSeeAlso;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opentrafficsim.xml.bindings.AccelerationUnitAdapter;
import org.opentrafficsim.xml.bindings.types.AccelerationUnitType;


/**
 * <p>Java-Klasse für AccelerationDistType complex type.</p>
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.</p>
 * 
 * <pre>{@code
 * <complexType name="AccelerationDistType">
 *   <complexContent>
 *     <extension base="{http://www.opentrafficsim.org/ots}ConstantDistType">
 *       <attribute name="AccelerationUnit" use="required" type="{http://www.opentrafficsim.org/ots}AccelerationUnitType" />
 *     </extension>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AccelerationDistType")
@XmlSeeAlso({
    org.opentrafficsim.xml.generated.ModelType.ModelParameters.AccelerationDist.class
})
@SuppressWarnings("all") public class AccelerationDistType
    extends ConstantDistType
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    @XmlAttribute(name = "AccelerationUnit", required = true)
    @XmlJavaTypeAdapter(AccelerationUnitAdapter.class)
    protected AccelerationUnitType accelerationUnit;

    /**
     * Ruft den Wert der accelerationUnit-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public AccelerationUnitType getAccelerationUnit() {
        return accelerationUnit;
    }

    /**
     * Legt den Wert der accelerationUnit-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAccelerationUnit(AccelerationUnitType value) {
        this.accelerationUnit = value;
    }

}
