
package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlSeeAlso;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opentrafficsim.xml.bindings.DoubleUnitIntervalAdapter;
import org.opentrafficsim.xml.bindings.OnOffAdapter;
import org.opentrafficsim.xml.bindings.types.BooleanType;
import org.opentrafficsim.xml.bindings.types.DoubleType;


/**
 * <p>Java class for Fuller complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="Fuller">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <attribute name="TemporalAnticipation" type="{http://www.opentrafficsim.org/ots}OnOff" />
 *       <attribute name="FractionOverestimation" type="{http://www.opentrafficsim.org/ots}DoubleUnitInterval" />
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Fuller")
@XmlSeeAlso({
    FullerSummative.class,
    FullerAnticipationReliance.class,
    FullerAttentionMatrix.class
})
@SuppressWarnings("all") public class Fuller
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    /**
     * Apply constant-speed temporal anticipation of surrounding vehicles.
     * 
     */
    @XmlAttribute(name = "TemporalAnticipation")
    @XmlJavaTypeAdapter(OnOffAdapter.class)
    protected BooleanType temporalAnticipation;
    /**
     * Fraction of drivers with overestimation of distances and speed differences
     *           to surrounding vehicles. The others have underestimation.
     * 
     */
    @XmlAttribute(name = "FractionOverestimation")
    @XmlJavaTypeAdapter(DoubleUnitIntervalAdapter.class)
    protected DoubleType fractionOverestimation;

    /**
     * Apply constant-speed temporal anticipation of surrounding vehicles.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public BooleanType getTemporalAnticipation() {
        return temporalAnticipation;
    }

    /**
     * Sets the value of the temporalAnticipation property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     * @see #getTemporalAnticipation()
     */
    public void setTemporalAnticipation(BooleanType value) {
        this.temporalAnticipation = value;
    }

    /**
     * Fraction of drivers with overestimation of distances and speed differences
     *           to surrounding vehicles. The others have underestimation.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public DoubleType getFractionOverestimation() {
        return fractionOverestimation;
    }

    /**
     * Sets the value of the fractionOverestimation property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     * @see #getFractionOverestimation()
     */
    public void setFractionOverestimation(DoubleType value) {
        this.fractionOverestimation = value;
    }

}
