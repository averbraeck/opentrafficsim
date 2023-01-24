package org.opentrafficsim.kpi.sampling;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Locale.Category;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.djunits.Throw;
import org.opentrafficsim.base.CompressedFileWriter;

/**
 * Writer of table in to a csv file.
 * <p>
 * Copyright (c) 2022-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public final class TableCsvWriter
{

    /** Default marshaller. */
    private static final Marshaller<Object> DEFAULT_MARSHALLER = new Marshaller<>()
    {
        /** {@inheritDoc} */
        @Override
        public String marshal(final Locale locale, final String format, final Object value)
        {
            return Marshaller.marshalDefault(locale, format, value);
        }

    };

    // some property names are shortened regarding hidden fields, it is preferred to have full names in the methods setting them

    /** Format. */
    private String form = "%.3f";

    /** Locale. */
    private Locale loc = Locale.getDefault(Category.FORMAT);

    /** Separator. */
    private String sep = ",";

    /** Delimiter. */
    private String delim = System.lineSeparator();

    /** Compression. */
    private Compression comp = Compression.NONE;

    /** Registered marshallers. */
    private final LinkedHashMap<Class<?>, Marshaller<?>> marshallers = new LinkedHashMap<>();

    /**
     * Private.
     */
    private TableCsvWriter()
    {
        //
    }

    /**
     * Creates a writer with default values as listed below.
     * <ul>
     * <li>format is {@code "%.3f"}</li>
     * <li>locale is {@code Locale.getDefault(Category.FORMAT)}</li>
     * <li>separator is {@code ","}</li>
     * <li>delimiter is {@code System.lineSeparator()}</li>
     * <li>compression is {@code Compression.NONE}</li>
     * </ul>
     * For data types for which no marshaller is registered, a default marshaller is used. This will do the following:
     * <ul>
     * <li>For {@code Integer} or a sub-class, {@code value.toString()} (e.g. prints as "1").</li>
     * <li>Otherwise for {@code Number} or a sub-class, {@code String.format(locale, format, value.doubleValue())}.</li>
     * <li>Otherwise {@code value.toString()}.
     * </ul>
     * @return TableCsvWriter; writer.
     */
    public static TableCsvWriter create()
    {
        return new TableCsvWriter();
    }

    /**
     * Set locale.
     * @param locale Locale; locale.
     * @return TableCsvWriter; for method chaining.
     */
    public TableCsvWriter setLocale(final Locale locale)
    {
        Throw.whenNull(locale, "locale may not be null.");
        this.loc = locale;
        return this;
    }

    /**
     * Set separator (separates values).
     * @param separator String; separator.
     * @return TableCsvWriter; for method chaining.
     */
    public TableCsvWriter setSeparator(final String separator)
    {
        Throw.whenNull(separator, "separator may not be null.");
        this.sep = separator;
        return this;
    }

    /**
     * Set delimiter (delimits a line in the file).
     * @param delimiter String; delimiter.
     * @return TableCsvWriter; for method chaining.
     */
    public TableCsvWriter setDelimiter(final String delimiter)
    {
        Throw.whenNull(delimiter, "delimiter may not be null.");
        this.delim = delimiter;
        return this;
    }

    /**
     * Set format.
     * @param format String; format.
     * @return TableCsvWriter; for method chaining.
     */
    public TableCsvWriter setFormat(final String format)
    {
        Throw.whenNull(format, "format may not be null.");
        this.form = format;
        return this;
    }

    /**
     * Set compression.
     * @param compression Compression; compression.
     * @return TableCsvWriter; for method chaining.
     */
    public TableCsvWriter setCompression(final Compression compression)
    {
        Throw.whenNull(compression, "compression may not be null.");
        this.comp = compression;
        return this;
    }

    /**
     * Register a marshaller for a data type.
     * @param <T> data type.
     * @param clazz Class&lt;T&gt;; class of the data type.
     * @param marshaller Marshaller&lt;? super T&gt;; marshaller.
     * @return TableCsvWriter; for method chaining.
     */
    public <T> TableCsvWriter registerMarshaller(final Class<T> clazz, final Marshaller<? super T> marshaller)
    {
        Throw.whenNull(clazz, "clazz may not be null.");
        Throw.whenNull(marshaller, "marshaller may not be null.");
        this.marshallers.put(clazz, marshaller);
        return this;
    }

    /**
     * Writes table to a file. Additional to this file, also a header file is written. It will get the name of the given file,
     * appended with ".header".
     * @param table Table; table.
     * @param file String; file.
     */
    public void write(final Table table, final String file)
    {
        Throw.when(!file.regionMatches(true, file.length() - 4, ".csv", 0, 4), IllegalArgumentException.class,
                "File should end with '.csv'");
        if (table.isEmpty())
        {
            return;
        }
        writeHeaderFile(table, file);
        try (BufferedWriter bw = CompressedFileWriter.create(file, Compression.ZIP.equals(this.comp)))
        {
            // header line
            bw.write(table.getColumns().stream().map((c) -> c.getId()).collect(Collectors.joining(this.sep)));
            bw.write(this.delim);
            // data rows
            for (Row row : table)
            {
                bw.write(IntStream.range(0, table.getNumberOfColumns()).boxed().map((i) -> marshal(table, row, i))
                        .collect(Collectors.joining(this.sep)));
                bw.write(this.delim);
            }
        }
        catch (IOException exception)
        {
            throw new RuntimeException("Could not write to file " + file, exception);
        }
    }

    /**
     * Writes a header file, describing all the columns.
     * @param table Table; table.
     * @param file String; file name of the csv file.
     */
    private void writeHeaderFile(final Table table, final String file)
    {
        try (BufferedWriter bw = CompressedFileWriter.create(file + ".header", false))
        {
            bw.write(table.getId());
            bw.write(this.delim);
            bw.write(table.getDescription());
            bw.write(this.delim);
            bw.write(this.loc.toString());
            bw.write(this.delim);
            bw.write(this.sep);
            bw.write(this.delim);
            bw.write(this.delim);
            for (Column<?> column : table.getColumns())
            {
                bw.write(column.getId());
                bw.write(this.delim);
                bw.write(column.getDescription());
                bw.write(this.delim);
                bw.write(column.getValueType().getName());
                bw.write(this.delim);
                if (column.getUnit() != null)
                {
                    bw.write("[" + column.getUnit() + "]");
                    bw.write(this.delim);
                }
                bw.write(this.delim);
            }
        }
        catch (IOException exception)
        {
            throw new RuntimeException("Could not write to file " + file + ".header", exception);
        }
    }

    /**
     * Marshal a value.
     * @param <K> marshaller value type.
     * @param <T> value type.
     * @param table Table; table.
     * @param row Row; row.
     * @param columnIndex int; column index.
     * @return String; marshalled value.
     */
    @SuppressWarnings("unchecked")
    private <K, T extends K> String marshal(final Table table, final Row row, final int columnIndex)
    {
        T value = (T) row.getValue(columnIndex);
        if (null == value)
        {
            return "";
        }
        Class<T> clazz = (Class<T>) table.getColumn(columnIndex).getValueType();
        Marshaller<K> marshaller = (Marshaller<K>) this.marshallers.getOrDefault(clazz, DEFAULT_MARSHALLER);
        String out = marshaller.marshal(this.loc, this.form, value);
        Throw.when(out.contains(this.sep) || out.contains(this.delim), RuntimeException.class,
                "String representation of value contains separator or delimiter.");
        return out;
    }

    /**
     * Marshaller interface. To generate a lambda function use {@code (locale, format, value) -> ... your code ...}.
     * <p>
     * Copyright (c) 2022-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
     * @param <T> data type.
     */
    @FunctionalInterface
    public interface Marshaller<T>
    {
        /**
         * Marshal value. A typical usage of the input is {@code String.format(locale, format, value.doubleValue())}.
         * @param locale Locale; locale.
         * @param format String; format.
         * @param value T; value, guaranteed not to be {@code null}.
         * @return String; marshalled string representation of the value.
         */
        String marshal(Locale locale, String format, T value);

        /**
         * Default marshalling.
         * @param locale Locale; locale.
         * @param format String; format.
         * @param value Object; value, guaranteed not to be {@code null}.
         * @return String; marshalled string representation of the value.
         */
        static String marshalDefault(final Locale locale, final String format, final Object value)
        {
            if (Integer.class.isAssignableFrom(value.getClass()))
            {
                // Integer is also a Number, but we don't want to print it with a number format.
                return value.toString();
            }
            if (Number.class.isAssignableFrom(value.getClass()))
            {
                return String.format(locale, format, ((Number) value).doubleValue());
            }
            return value.toString();
        }
    }

    /**
     * Compression method.
     * <p>
     * Copyright (c) 2022-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
     */
    public enum Compression
    {
        /** No compression. */
        NONE,

        /** Zip compression. */
        ZIP
    }

}
