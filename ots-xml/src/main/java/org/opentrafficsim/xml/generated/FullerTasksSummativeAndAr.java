
package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlSeeAlso;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opentrafficsim.xml.bindings.BooleanAdapter;
import org.opentrafficsim.xml.bindings.types.BooleanType;


/**
 * <p>Java class for FullerTasksSummativeAndAr complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="FullerTasksSummativeAndAr">
 *   <complexContent>
 *     <extension base="{http://www.opentrafficsim.org/ots}FullerTasks">
 *       <attribute name="AlternateCarFollowing" type="{http://www.opentrafficsim.org/ots}boolean" />
 *       <attribute name="AlternateLaneChanging" type="{http://www.opentrafficsim.org/ots}boolean" />
 *     </extension>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FullerTasksSummativeAndAr")
@XmlSeeAlso({
    org.opentrafficsim.xml.generated.FullerAnticipationReliance.Tasks.class
})
@SuppressWarnings("all") public class FullerTasksSummativeAndAr
    extends FullerTasks
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    /**
     * Have car-following task based on exponential headway relationship.
     * 
     */
    @XmlAttribute(name = "AlternateCarFollowing")
    @XmlJavaTypeAdapter(BooleanAdapter.class)
    protected BooleanType alternateCarFollowing;
    /**
     * Have lane changing task based on lane change desire.
     * 
     */
    @XmlAttribute(name = "AlternateLaneChanging")
    @XmlJavaTypeAdapter(BooleanAdapter.class)
    protected BooleanType alternateLaneChanging;

    /**
     * Have car-following task based on exponential headway relationship.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public BooleanType getAlternateCarFollowing() {
        return alternateCarFollowing;
    }

    /**
     * Sets the value of the alternateCarFollowing property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     * @see #getAlternateCarFollowing()
     */
    public void setAlternateCarFollowing(BooleanType value) {
        this.alternateCarFollowing = value;
    }

    /**
     * Have lane changing task based on lane change desire.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public BooleanType getAlternateLaneChanging() {
        return alternateLaneChanging;
    }

    /**
     * Sets the value of the alternateLaneChanging property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     * @see #getAlternateLaneChanging()
     */
    public void setAlternateLaneChanging(BooleanType value) {
        this.alternateLaneChanging = value;
    }

}
