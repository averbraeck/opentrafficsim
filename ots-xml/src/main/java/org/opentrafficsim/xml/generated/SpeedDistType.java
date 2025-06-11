
package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlSeeAlso;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opentrafficsim.xml.bindings.SpeedUnitAdapter;
import org.opentrafficsim.xml.bindings.types.SpeedUnitType;


/**
 * <p>Java-Klasse für SpeedDistType complex type.</p>
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.</p>
 * 
 * <pre>{@code
 * <complexType name="SpeedDistType">
 *   <complexContent>
 *     <extension base="{http://www.opentrafficsim.org/ots}ConstantDistType">
 *       <attribute name="SpeedUnit" use="required" type="{http://www.opentrafficsim.org/ots}SpeedUnitType" />
 *     </extension>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SpeedDistType")
@XmlSeeAlso({
    org.opentrafficsim.xml.generated.ModelType.ModelParameters.SpeedDist.class
})
@SuppressWarnings("all") public class SpeedDistType
    extends ConstantDistType
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    @XmlAttribute(name = "SpeedUnit", required = true)
    @XmlJavaTypeAdapter(SpeedUnitAdapter.class)
    protected SpeedUnitType speedUnit;

    /**
     * Ruft den Wert der speedUnit-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public SpeedUnitType getSpeedUnit() {
        return speedUnit;
    }

    /**
     * Legt den Wert der speedUnit-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSpeedUnit(SpeedUnitType value) {
        this.speedUnit = value;
    }

}
