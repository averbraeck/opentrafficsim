//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2019.03.29 at 04:36:04 PM CET 
//


package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://www.opentrafficsim.org/ots}GTUMIX" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opentrafficsim.org/ots}OD" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opentrafficsim.org/ots}GENERATOR" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opentrafficsim.org/ots}LISTGENERATOR" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opentrafficsim.org/ots}SINK" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "gtumix",
    "od",
    "generator",
    "listgenerator",
    "sink"
})
@XmlRootElement(name = "NETWORKDEMAND")
@Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-29T04:36:04+01:00", comments = "JAXB RI v2.3.0")
public class NETWORKDEMAND
    implements Serializable
{

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-29T04:36:04+01:00", comments = "JAXB RI v2.3.0")
    private final static long serialVersionUID = 10102L;
    @XmlElement(name = "GTUMIX")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-29T04:36:04+01:00", comments = "JAXB RI v2.3.0")
    protected List<GTUMIX> gtumix;
    @XmlElement(name = "OD")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-29T04:36:04+01:00", comments = "JAXB RI v2.3.0")
    protected List<OD> od;
    @XmlElement(name = "GENERATOR")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-29T04:36:04+01:00", comments = "JAXB RI v2.3.0")
    protected List<GENERATOR> generator;
    @XmlElement(name = "LISTGENERATOR")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-29T04:36:04+01:00", comments = "JAXB RI v2.3.0")
    protected List<LISTGENERATOR> listgenerator;
    @XmlElement(name = "SINK")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-29T04:36:04+01:00", comments = "JAXB RI v2.3.0")
    protected List<SINK> sink;

    /**
     * Gets the value of the gtumix property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the gtumix property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getGTUMIX().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link GTUMIX }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-29T04:36:04+01:00", comments = "JAXB RI v2.3.0")
    public List<GTUMIX> getGTUMIX() {
        if (gtumix == null) {
            gtumix = new ArrayList<GTUMIX>();
        }
        return this.gtumix;
    }

    /**
     * Gets the value of the od property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the od property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getOD().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link OD }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-29T04:36:04+01:00", comments = "JAXB RI v2.3.0")
    public List<OD> getOD() {
        if (od == null) {
            od = new ArrayList<OD>();
        }
        return this.od;
    }

    /**
     * Gets the value of the generator property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the generator property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getGENERATOR().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link GENERATOR }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-29T04:36:04+01:00", comments = "JAXB RI v2.3.0")
    public List<GENERATOR> getGENERATOR() {
        if (generator == null) {
            generator = new ArrayList<GENERATOR>();
        }
        return this.generator;
    }

    /**
     * Gets the value of the listgenerator property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the listgenerator property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLISTGENERATOR().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link LISTGENERATOR }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-29T04:36:04+01:00", comments = "JAXB RI v2.3.0")
    public List<LISTGENERATOR> getLISTGENERATOR() {
        if (listgenerator == null) {
            listgenerator = new ArrayList<LISTGENERATOR>();
        }
        return this.listgenerator;
    }

    /**
     * Gets the value of the sink property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the sink property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSINK().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SINK }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-29T04:36:04+01:00", comments = "JAXB RI v2.3.0")
    public List<SINK> getSINK() {
        if (sink == null) {
            sink = new ArrayList<SINK>();
        }
        return this.sink;
    }

}
