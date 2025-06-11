
package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlSeeAlso;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opentrafficsim.xml.bindings.StringAdapter;
import org.opentrafficsim.xml.bindings.types.StringType;


/**
 * <p>Java-Klasse für HierarchicalType complex type.</p>
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.</p>
 * 
 * <pre>{@code
 * <complexType name="HierarchicalType">
 *   <complexContent>
 *     <extension base="{http://www.opentrafficsim.org/ots}Type">
 *       <attribute name="Parent" type="{http://www.opentrafficsim.org/ots}string" />
 *     </extension>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "HierarchicalType")
@XmlSeeAlso({
    GtuCompatibleInfraType.class,
    GtuType.class
})
@SuppressWarnings("all") public class HierarchicalType
    extends Type
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    @XmlAttribute(name = "Parent")
    @XmlJavaTypeAdapter(StringAdapter.class)
    protected StringType parent;

    /**
     * Ruft den Wert der parent-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public StringType getParent() {
        return parent;
    }

    /**
     * Legt den Wert der parent-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setParent(StringType value) {
        this.parent = value;
    }

}
