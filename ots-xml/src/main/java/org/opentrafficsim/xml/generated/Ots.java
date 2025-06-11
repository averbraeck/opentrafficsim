
package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opentrafficsim.xml.bindings.SpaceAdapter;
import org.opentrafficsim.xml.bindings.types.StringType;


/**
 * <p>Java-Klasse für anonymous complex type.</p>
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.</p>
 * 
 * <pre>{@code
 * <complexType>
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *         <element ref="{http://www.opentrafficsim.org/ots}Definitions"/>
 *         <element ref="{http://www.opentrafficsim.org/ots}Network"/>
 *         <element ref="{http://www.opentrafficsim.org/ots}Demand" minOccurs="0"/>
 *         <element ref="{http://www.opentrafficsim.org/ots}Control" minOccurs="0"/>
 *         <element ref="{http://www.opentrafficsim.org/ots}Models" minOccurs="0"/>
 *         <element ref="{http://www.opentrafficsim.org/ots}Scenarios" minOccurs="0"/>
 *         <element ref="{http://www.opentrafficsim.org/ots}Run"/>
 *         <element ref="{http://www.opentrafficsim.org/ots}Animation" minOccurs="0"/>
 *       </sequence>
 *       <attribute name="Space" type="{http://www.opentrafficsim.org/ots}space" default="preserve" />
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "definitions",
    "network",
    "demand",
    "control",
    "models",
    "scenarios",
    "run",
    "animation"
})
@XmlRootElement(name = "Ots")
@SuppressWarnings("all") public class Ots
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    @XmlElement(name = "Definitions", required = true)
    protected Definitions definitions;
    @XmlElement(name = "Network", required = true)
    protected Network network;
    @XmlElement(name = "Demand")
    protected Demand demand;
    @XmlElement(name = "Control")
    protected Control control;
    @XmlElement(name = "Models")
    protected Models models;
    @XmlElement(name = "Scenarios")
    protected Scenarios scenarios;
    @XmlElement(name = "Run", required = true)
    protected Run run;
    @XmlElement(name = "Animation")
    protected Animation animation;
    @XmlAttribute(name = "Space")
    @XmlJavaTypeAdapter(SpaceAdapter.class)
    protected StringType space;

    /**
     * Ruft den Wert der definitions-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Definitions }
     *     
     */
    public Definitions getDefinitions() {
        return definitions;
    }

    /**
     * Legt den Wert der definitions-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Definitions }
     *     
     */
    public void setDefinitions(Definitions value) {
        this.definitions = value;
    }

    /**
     * Ruft den Wert der network-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Network }
     *     
     */
    public Network getNetwork() {
        return network;
    }

    /**
     * Legt den Wert der network-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Network }
     *     
     */
    public void setNetwork(Network value) {
        this.network = value;
    }

    /**
     * Ruft den Wert der demand-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Demand }
     *     
     */
    public Demand getDemand() {
        return demand;
    }

    /**
     * Legt den Wert der demand-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Demand }
     *     
     */
    public void setDemand(Demand value) {
        this.demand = value;
    }

    /**
     * Ruft den Wert der control-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Control }
     *     
     */
    public Control getControl() {
        return control;
    }

    /**
     * Legt den Wert der control-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Control }
     *     
     */
    public void setControl(Control value) {
        this.control = value;
    }

    /**
     * Ruft den Wert der models-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Models }
     *     
     */
    public Models getModels() {
        return models;
    }

    /**
     * Legt den Wert der models-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Models }
     *     
     */
    public void setModels(Models value) {
        this.models = value;
    }

    /**
     * Ruft den Wert der scenarios-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Scenarios }
     *     
     */
    public Scenarios getScenarios() {
        return scenarios;
    }

    /**
     * Legt den Wert der scenarios-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Scenarios }
     *     
     */
    public void setScenarios(Scenarios value) {
        this.scenarios = value;
    }

    /**
     * Ruft den Wert der run-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Run }
     *     
     */
    public Run getRun() {
        return run;
    }

    /**
     * Legt den Wert der run-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Run }
     *     
     */
    public void setRun(Run value) {
        this.run = value;
    }

    /**
     * Ruft den Wert der animation-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Animation }
     *     
     */
    public Animation getAnimation() {
        return animation;
    }

    /**
     * Legt den Wert der animation-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Animation }
     *     
     */
    public void setAnimation(Animation value) {
        this.animation = value;
    }

    /**
     * Ruft den Wert der space-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public StringType getSpace() {
        if (space == null) {
            return new SpaceAdapter().unmarshal("preserve");
        } else {
            return space;
        }
    }

    /**
     * Legt den Wert der space-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSpace(StringType value) {
        this.space = value;
    }

}
