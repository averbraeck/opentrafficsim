
package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opentrafficsim.xml.bindings.StringAdapter;
import org.opentrafficsim.xml.bindings.types.StringType;


/**
 * <p>Java class for RandomStreamSource complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="RandomStreamSource">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <choice>
 *         <element name="Default" type="{http://www.opentrafficsim.org/ots}EmptyType"/>
 *         <element name="Generation" type="{http://www.opentrafficsim.org/ots}EmptyType"/>
 *         <element name="Defined" type="{http://www.opentrafficsim.org/ots}string"/>
 *       </choice>
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RandomStreamSource", propOrder = {
    "_default",
    "generation",
    "defined"
})
@SuppressWarnings("all") public class RandomStreamSource
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    @XmlElement(name = "Default")
    protected EmptyType _default;
    @XmlElement(name = "Generation")
    protected EmptyType generation;
    @XmlElement(name = "Defined", type = String.class)
    @XmlJavaTypeAdapter(StringAdapter.class)
    protected StringType defined;

    /**
     * Gets the value of the default property.
     * 
     * @return
     *     possible object is
     *     {@link EmptyType }
     *     
     */
    public EmptyType getDefault() {
        return _default;
    }

    /**
     * Sets the value of the default property.
     * 
     * @param value
     *     allowed object is
     *     {@link EmptyType }
     *     
     */
    public void setDefault(EmptyType value) {
        this._default = value;
    }

    /**
     * Gets the value of the generation property.
     * 
     * @return
     *     possible object is
     *     {@link EmptyType }
     *     
     */
    public EmptyType getGeneration() {
        return generation;
    }

    /**
     * Sets the value of the generation property.
     * 
     * @param value
     *     allowed object is
     *     {@link EmptyType }
     *     
     */
    public void setGeneration(EmptyType value) {
        this.generation = value;
    }

    /**
     * Gets the value of the defined property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public StringType getDefined() {
        return defined;
    }

    /**
     * Sets the value of the defined property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDefined(StringType value) {
        this.defined = value;
    }

}
