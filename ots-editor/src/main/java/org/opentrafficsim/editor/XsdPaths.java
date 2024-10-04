package org.opentrafficsim.editor;

/**
 * Class where static fields define paths in the XML. This is to provide a central location to implement changes in the XML
 * Schema. Another place where many paths are defined is {@code DefaultDecorator}. Still, many single attribute/child references
 * are defined throughout other classes.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class XsdPaths
{

    /** Definitions path. */
    public static final String DEFINITIONS = "Ots.Definitions";

    /** Defined road layout path. */
    public static final String DEFINED_ROADLAYOUT = DEFINITIONS + ".RoadLayouts.RoadLayout";

    /** Network path. */
    public static final String NETWORK = "Ots.Network";

    /** Connector path. */
    public static final String CONNECTOR = NETWORK + ".Connector";

    /** Node path. */
    public static final String NODE = NETWORK + ".Node";

    /** Link path. */
    public static final String LINK = NETWORK + ".Link";

    /** Traffic light path. */
    public static final String TRAFFIC_LIGHT = LINK + ".TrafficLight";

    /** Polyline coordinate path. */
    public static final String POLYLINE_COORDINATE = LINK + ".Polyline.Coordinate";

    /** Road layout path. */
    public static final String ROADLAYOUT = LINK + ".RoadLayout";

    /** OD options item path. */
    public static final String OD_OPTIONS_ITEM = "Ots.Demand.OdOptions.OdOptionsItem";

    /** Generator path. */
    public static final String GENERATOR = "Ots.Demand.Generator";

    /** List generator path. */
    public static final String LIST_GENERATOR = "Ots.Demand.ListGenerator";

    /** Sink path. */
    public static final String SINK = "Ots.Demand.Sink";

    /** Correlation path. */
    public static final String CORRELATION = "Ots.Models.Model.ModelParameters.Correlation";

    /** Scenarios path. */
    public static final String SCENARIOS = "Ots.Scenarios";

    /** Default input parameters path. */
    public static final String DEFAULT_INPUT_PARAMETERS = SCENARIOS + ".DefaultInputParameters";

    /** Default string input parameter path. */
    public static final String DEFAULT_INPUT_PARAMETER_STRING = DEFAULT_INPUT_PARAMETERS + ".String";

    /** Scenario path. */
    public static final String SCENARIO = SCENARIOS + ".Scenario";

    /** Input parameters path. */
    public static final String INPUT_PARAMETERS = SCENARIO + ".InputParameters";

}
