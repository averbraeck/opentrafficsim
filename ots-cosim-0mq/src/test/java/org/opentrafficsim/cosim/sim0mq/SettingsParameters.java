package org.opentrafficsim.cosim.sim0mq;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.djunits.value.vdouble.scalar.base.DoubleScalarRel;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterType;
import org.opentrafficsim.cosim.AbstractOtsTransceiver;
import org.opentrafficsim.cosim.Parameters;
import org.opentrafficsim.cosim.messages.DeleteMessage;
import org.opentrafficsim.cosim.messages.PlanMessage;
import org.opentrafficsim.cosim.messages.ReadyMessage;
import org.opentrafficsim.cosim.messages.VehicleMessage;
import org.opentrafficsim.cosim.tactical.ScenarioTacticalPlanner;
import org.opentrafficsim.road.gtu.tactical.lmrs.LmrsFactory;

import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.OptionSpec;

/**
 * Prints markdown tables for settings and parameters supported in co-simulation.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Wouter Schakel
 */
public final class SettingsParameters
{

    /** Map of parameter types by their id. */
    private static final Map<String, ParameterType<?>> PARAMETER_MAP = Parameters.PARAMETER_MAP;

    /**
     * Constructor.
     */
    private SettingsParameters()
    {
        //
    }

    /**
     * Prints all supported settings and parameters.
     * @param args command line arguments
     * @throws ParameterException if a parameter has no default value
     */
    public static void main(final String[] args) throws ParameterException
    {
        printSettings();
        System.out.println();
        System.out.println("== Parameters ==");
        printParameters();
    }

    /**
     * Prints a row for markdown table.
     * @param pad padding character
     * @param values column values
     * @param size columns sizes
     */
    private static void printRow(final String pad, final String[] values, final int[] size)
    {
        StringBuilder str = new StringBuilder();
        String sep = "|" + pad;
        for (int i = 0; i < values.length; i++)
        {
            str.append(sep);
            str.append(String.format("%1$-" + size[i] + "s", values[i]).replace(" ", pad));
            sep = pad + "|" + pad;
        }
        str.append(pad + "|");
        System.out.println(str);
    }

    // ===== Settings ======

    /**
     * Prints all supported settings.
     */
    private static void printSettings()
    {

        // Create transceiver to obtain options from
        AbstractOtsTransceiver transceiver = new AbstractOtsTransceiver()
        {
            @Override
            protected void send(final ReadyMessage readeMessage)
            {
            }

            @Override
            protected void send(final PlanMessage planMessage)
            {
            }

            @Override
            protected void send(final DeleteMessage deleteMessage)
            {
            }

            @Override
            protected void send(final VehicleMessage vehicleMessage)
            {
            }
        };

        // Loop options in different specs and store name and description in maps
        CommandSpec sim0mqSpec = CommandSpec.forAnnotatedObject(new OtsTransceiverSim0mq());
        CommandSpec transceiverSpec = CommandSpec.forAnnotatedObject(transceiver);
        CommandSpec modelSpec = CommandSpec.forAnnotatedObject(new LmrsFactory<>(ScenarioTacticalPlanner::new));
        Map<String, String> sim0mqMap = new LinkedHashMap<>();
        Map<String, String> transceiverMap = new LinkedHashMap<>();
        Map<String, String> modelMap = new LinkedHashMap<>();
        for (OptionSpec option : sim0mqSpec.options())
        {
            sim0mqMap.put(option.longestName(), option.description()[0]);
        }
        for (OptionSpec option : transceiverSpec.options())
        {
            transceiverMap.put(option.longestName(), option.description()[0]);
        }
        for (OptionSpec option : modelSpec.options())
        {
            modelMap.put(option.longestName(), option.description()[0]);
        }

        // Remove fields from deeper components so we can create tables at each level
        transceiverMap.keySet().forEach((k) -> sim0mqMap.remove(k));
        modelMap.keySet().forEach((k) -> transceiverMap.remove(k));
        modelMap.keySet().forEach((k) -> sim0mqMap.remove(k));

        // Remove fields from picocli we do not want to use and other settings not supported by the transceiver
        for (String option : Set.of("--help", "--version", "--gtuTypes"))
        {
            sim0mqMap.remove(option);
            transceiverMap.remove(option);
            modelMap.remove(option);
        }

        System.out.println("== Sim0mq settings ==");
        printSettingsMap(sim0mqMap);
        System.out.println();
        System.out.println("== Transceiver settings ==");
        printSettingsMap(transceiverMap);
        System.out.println();
        System.out.println("== Model settings ==");
        printSettingsMap(modelMap);

    }

    /**
     * Prints map with settings as markdown table.
     * @param map map with settings
     */
    private static void printSettingsMap(final Map<String, String> map)
    {
        String settingCol = "Setting";
        String descriptionCol = "Description";
        int settingLength = settingCol.length();
        int descriptionLength = descriptionCol.length();
        for (Entry<String, String> entry : map.entrySet())
        {
            settingLength = Math.max(settingLength, entry.getKey().length());
            descriptionLength = Math.max(descriptionLength, entry.getValue().length());
        }

        int[] size = new int[] {settingLength, descriptionLength};
        printRow(" ", new String[] {settingCol, descriptionCol}, size);
        printRow("-", new String[] {"", ""}, size);

        for (Entry<String, String> entry : map.entrySet())
        {
            printRow(" ", new String[] {entry.getKey(), entry.getValue()}, size);
        }
    }

    // ===== Parameters =====

    /**
     * Print parameters table.
     * @throws ParameterException if a parameter has no default value
     */
    private static void printParameters() throws ParameterException
    {
        String parameterCol = "Parameter";
        String typeCol = "Type";
        String defaultCol = "Default";
        String descriptionCol = "Description";
        int parameterLength = parameterCol.length();
        int typeLength = typeCol.length();
        int defaultLength = defaultCol.length();
        int descriptionLength = descriptionCol.length();
        for (ParameterType<?> parameter : PARAMETER_MAP.values())
        {
            parameterLength = Math.max(parameterLength, parameter.getId().length());
            typeLength = Math.max(typeLength, parameter.getValueClass().getSimpleName().length() + 2);
            defaultLength = Math.max(defaultLength, defaultValue(parameter.getDefaultValue()).length());
            descriptionLength = Math.max(descriptionLength, parameter.getDescription().length());
        }

        int[] size = new int[] {parameterLength, typeLength, defaultLength, descriptionLength};
        printRow(" ", new String[] {parameterCol, typeCol, defaultCol, descriptionCol}, size);
        printRow("-", new String[] {"", "", "", ""}, size);

        for (ParameterType<?> parameter : PARAMETER_MAP.values())
        {
            printRow(" ", new String[] {parameter.getId(), "`" + parameter.getValueClass().getSimpleName() + "`",
                    defaultValue(parameter.getDefaultValue()), parameter.getDescription()}, size);
        }
    }

    /**
     * Returns a nice string for a default value.
     * @param value default value
     * @return nice string for a default value
     */
    private static String defaultValue(final Object value)
    {
        if (value instanceof DoubleScalarRel<?, ?> unit)
        {
            return roundedValue(unit.getInUnit()) + " " + unit.getDisplayUnit().getId();
        }
        if (value instanceof Double doubleValue)
        {
            return roundedValue(doubleValue);
        }
        throw new IllegalArgumentException("Default value is not a DJNUITS value nor a Double.");
    }

    /**
     * Returns a nice string for a double value.
     * @param doubleValue value
     * @return nice string for a double value
     */
    private static String roundedValue(final double doubleValue)
    {
        String attempt1 = "" + doubleValue;
        String attempt2 = String.format(Locale.US, "%.6f", doubleValue);
        while (attempt2.endsWith("0") && !attempt2.endsWith(".0"))
        {
            attempt2 = attempt2.substring(0, attempt2.length() - 1);
        }
        return attempt1.length() < attempt2.length() ? attempt1 : attempt2;
    }

}
