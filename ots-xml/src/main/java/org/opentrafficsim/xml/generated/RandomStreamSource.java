
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
 * <p>Java-Klasse für RandomStreamSource complex type.</p>
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.</p>
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
     * Ruft den Wert der default-Eigenschaft ab.
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
     * Legt den Wert der default-Eigenschaft fest.
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
     * Ruft den Wert der generation-Eigenschaft ab.
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
     * Legt den Wert der generation-Eigenschaft fest.
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
     * Ruft den Wert der defined-Eigenschaft ab.
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
     * Legt den Wert der defined-Eigenschaft fest.
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
