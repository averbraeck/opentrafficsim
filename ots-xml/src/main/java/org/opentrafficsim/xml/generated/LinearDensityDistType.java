
package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlSeeAlso;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opentrafficsim.xml.bindings.LinearDensityUnitAdapter;
import org.opentrafficsim.xml.bindings.types.LinearDensityUnitType;


/**
 * <p>Java-Klasse für LinearDensityDistType complex type.</p>
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.</p>
 * 
 * <pre>{@code
 * <complexType name="LinearDensityDistType">
 *   <complexContent>
 *     <extension base="{http://www.opentrafficsim.org/ots}ConstantDistType">
 *       <attribute name="LinearDensityUnit" use="required" type="{http://www.opentrafficsim.org/ots}LinearDensityUnitType" />
 *     </extension>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LinearDensityDistType")
@XmlSeeAlso({
    org.opentrafficsim.xml.generated.ModelType.ModelParameters.LinearDensityDist.class
})
@SuppressWarnings("all") public class LinearDensityDistType
    extends ConstantDistType
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    @XmlAttribute(name = "LinearDensityUnit", required = true)
    @XmlJavaTypeAdapter(LinearDensityUnitAdapter.class)
    protected LinearDensityUnitType linearDensityUnit;

    /**
     * Ruft den Wert der linearDensityUnit-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public LinearDensityUnitType getLinearDensityUnit() {
        return linearDensityUnit;
    }

    /**
     * Legt den Wert der linearDensityUnit-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLinearDensityUnit(LinearDensityUnitType value) {
        this.linearDensityUnit = value;
    }

}
