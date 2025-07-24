//
// Diese Datei wurde mit der Eclipse Implementation of JAXB, v2.3.7 generiert 
// Siehe https://eclipse-ee4j.github.io/jaxb-ri 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2025.07.24 um 01:18:43 PM CEST 
//


package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opentrafficsim.xml.bindings.SpaceAdapter;
import org.opentrafficsim.xml.bindings.types.StringType;


/**
 * <p>Java-Klasse für anonymous complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://www.opentrafficsim.org/ots}Definitions"/&gt;
 *         &lt;element ref="{http://www.opentrafficsim.org/ots}Network"/&gt;
 *         &lt;element ref="{http://www.opentrafficsim.org/ots}Demand" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opentrafficsim.org/ots}Control" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opentrafficsim.org/ots}Models" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opentrafficsim.org/ots}Scenarios" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opentrafficsim.org/ots}Run"/&gt;
 *         &lt;element ref="{http://www.opentrafficsim.org/ots}Animation" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="Space" type="{http://www.opentrafficsim.org/ots}space" default="preserve" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
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
@Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
public class Ots
    implements Serializable
{

    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    private final static long serialVersionUID = 10102L;
    @XmlElement(name = "Definitions", required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected Definitions definitions;
    @XmlElement(name = "Network", required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected Network network;
    @XmlElement(name = "Demand")
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected Demand demand;
    @XmlElement(name = "Control")
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected Control control;
    @XmlElement(name = "Models")
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected Models models;
    @XmlElement(name = "Scenarios")
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected Scenarios scenarios;
    @XmlElement(name = "Run", required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected Run run;
    @XmlElement(name = "Animation")
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected Animation animation;
    @XmlAttribute(name = "Space")
    @XmlJavaTypeAdapter(SpaceAdapter.class)
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected StringType space;

    /**
     * Ruft den Wert der definitions-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Definitions }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public void setSpace(StringType value) {
        this.space = value;
    }

}
