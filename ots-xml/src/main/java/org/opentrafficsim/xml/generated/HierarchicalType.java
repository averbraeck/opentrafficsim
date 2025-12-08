
package org.opentrafficsim.xml.generated;

import java.io.Serializable;

import org.opentrafficsim.xml.bindings.StringAdapter;
import org.opentrafficsim.xml.bindings.types.StringType;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlSeeAlso;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for HierarchicalType complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
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
     * Gets the value of the parent property.
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
     * Sets the value of the parent property.
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
