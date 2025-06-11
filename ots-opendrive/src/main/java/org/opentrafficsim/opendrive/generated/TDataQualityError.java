
package org.opentrafficsim.opendrive.generated;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;


/**
 * The absolute or relative errors of road data are described by <error> elements within the <dataQuality> element.
 * 
 * <p>Java-Klasse für t_dataQuality_Error complex type.</p>
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.</p>
 * 
 * <pre>{@code
 * <complexType name="t_dataQuality_Error">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *       </sequence>
 *       <attribute name="xyAbsolute" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       <attribute name="zAbsolute" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       <attribute name="xyRelative" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       <attribute name="zRelative" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "t_dataQuality_Error")
@SuppressWarnings("all") public class TDataQualityError {

    /**
     * Absolute error of the road data in x/y direction
     * 
     */
    @XmlAttribute(name = "xyAbsolute", required = true)
    protected double xyAbsolute;
    /**
     * Absolute error of the road data in z direction
     * 
     */
    @XmlAttribute(name = "zAbsolute", required = true)
    protected double zAbsolute;
    /**
     * Relative error of the road data in x/y direction
     * 
     */
    @XmlAttribute(name = "xyRelative", required = true)
    protected double xyRelative;
    /**
     * Relative error of the road data in z direction
     * 
     */
    @XmlAttribute(name = "zRelative", required = true)
    protected double zRelative;

    /**
     * Absolute error of the road data in x/y direction
     * 
     */
    public double getXyAbsolute() {
        return xyAbsolute;
    }

    /**
     * Legt den Wert der xyAbsolute-Eigenschaft fest.
     * 
     */
    public void setXyAbsolute(double value) {
        this.xyAbsolute = value;
    }

    /**
     * Absolute error of the road data in z direction
     * 
     */
    public double getZAbsolute() {
        return zAbsolute;
    }

    /**
     * Legt den Wert der zAbsolute-Eigenschaft fest.
     * 
     */
    public void setZAbsolute(double value) {
        this.zAbsolute = value;
    }

    /**
     * Relative error of the road data in x/y direction
     * 
     */
    public double getXyRelative() {
        return xyRelative;
    }

    /**
     * Legt den Wert der xyRelative-Eigenschaft fest.
     * 
     */
    public void setXyRelative(double value) {
        this.xyRelative = value;
    }

    /**
     * Relative error of the road data in z direction
     * 
     */
    public double getZRelative() {
        return zRelative;
    }

    /**
     * Legt den Wert der zRelative-Eigenschaft fest.
     * 
     */
    public void setZRelative(double value) {
        this.zRelative = value;
    }

}
