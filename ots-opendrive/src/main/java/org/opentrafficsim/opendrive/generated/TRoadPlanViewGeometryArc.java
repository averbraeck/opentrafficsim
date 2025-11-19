
package org.opentrafficsim.opendrive.generated;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;


/**
 * An arc describes a road reference line with constant curvature. In OpenDRIVE, an arc is represented by an &lt;arc&gt; element within the &lt;geometry&gt; element.
 * 
 * <p>Java class for t_road_planView_geometry_arc complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="t_road_planView_geometry_arc">
 *   <complexContent>
 *     <extension base="{http://code.asam.net/simulation/standard/opendrive_schema}_OpenDriveElement">
 *       <sequence>
 *       </sequence>
 *       <attribute name="curvature" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *     </extension>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "t_road_planView_geometry_arc")
@SuppressWarnings("all") public class TRoadPlanViewGeometryArc
    extends OpenDriveElement
{

    /**
     * Constant curvature throughout the element
     * 
     */
    @XmlAttribute(name = "curvature", required = true)
    protected double curvature;

    /**
     * Constant curvature throughout the element
     * 
     */
    public double getCurvature() {
        return curvature;
    }

    /**
     * Sets the value of the curvature property.
     * 
     */
    public void setCurvature(double value) {
        this.curvature = value;
    }

}
