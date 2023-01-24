package org.opentrafficsim.kpi.sampling;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Locale.Category;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.djunits.Throw;
import org.djunits.value.base.Scalar;
import org.djutils.immutablecollections.ImmutableCollection;

/**
 * Reader of table from a csv file.
 * <p>
 * Copyright (c) 2022-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public final class TableCsvReader
{

    /** Default unmarshaller. */
    private static final Unmarshaller<Object> DEFAULT_UNMARSHALLER = new Unmarshaller<>()
    {
        /** {@inheritDoc} */
        @Override
        public Object unmarshal(final Class<? extends Object> clazz, final Locale locale, final String value, final String unit)
        {
            return Unmarshaller.unmarshalDefault(clazz, locale, value, unit);
        }
    };

    /** Locale. */
    private Locale locale = Locale.getDefault(Category.FORMAT);

    /** Separator. */
    private String separator;

    /** Delimiter. */
    private String delim = System.lineSeparator();

    /** Registered unmarshallers. */
    private final LinkedHashMap<Class<?>, Unmarshaller<?>> unmarshallers = new LinkedHashMap<>();

    /**
     * Private.
     */
    private TableCsvReader()
    {
        //
    }

    /**
     * Creates a reader with default delimiter {@code System.lineSeparator()}.<br>
     * <br>
     * For data types for which no unmarshaller is registered, a default unmarshaller is used. This will do the following:
     * <ul>
     * <li>For {@code String}, it is returned.</li>
     * <li>Otherwise for {@code Character}, {@code Character.valueOf(value.charAt(0))}.</li>
     * <li>Otherwise for other primitive wrappers, {@code WrapperClass.valueOf(String value)}.</li>
     * <li>Otherwise for {@code Scalar} sub-classes, {@code ScalarClass.valueOf(value + unit)}.</li>
     * </ul>
     * where {@code value} is a {@code String} of the value to unmarshal, and {@code unit} is a {@code String} of the unit of
     * the relevant column.
     * @return TableCsvReader; reader.
     */
    public static TableCsvReader create()
    {
        return new TableCsvReader();
    }

    /**
     * Set delimiter (delimits a line in the file).
     * @param delimiter String; delimiter.
     * @return TableCsvReader; for method chaining.
     */
    public TableCsvReader setDelimiter(final String delimiter)
    {
        Throw.whenNull(delimiter, "delimiter may not be null.");
        this.delim = delimiter;
        return this;
    }

    /**
     * Register an unmarshaller for a data type.
     * @param <T> data type.
     * @param clazz Class&lt;T&gt;; class of the data type.
     * @param unmarshaller Unmarshaller&lt;? extends T&gt;; unmarshaller.
     * @return TableCsvWriter; for method chaining.
     */
    public <T> TableCsvReader registerUnmarshaller(final Class<T> clazz, final Unmarshaller<? extends T> unmarshaller)
    {
        Throw.whenNull(clazz, "clazz may not be null.");
        Throw.whenNull(unmarshaller, "unmarshaller may not be null.");
        this.unmarshallers.put(clazz, unmarshaller);
        return this;
    }

    /**
     * Reads table from a file. A header file with the same name appended with ".header" is required.
     * @param file String; file.
     * @return Table; table from the file.
     */
    public Table read(final String file)
    {
        Throw.when(!file.regionMatches(true, file.length() - 4, ".csv", 0, 4), IllegalArgumentException.class,
                "File should end with '.csv'");
        ListTable table = readHeaderFile(file);
        try (Scanner scanner = new Scanner(new File(file)).useDelimiter(this.delim))
        {
            Throw.when(!scanner.hasNext(), IOException.class, "Empty file.");
            int[] colIndices = columnIndices(scanner.next().split(this.separator), table.getColumns());
            while (scanner.hasNext())
            {
                String[] rowStrings = scanner.next().split(this.separator, -1);
                Throw.when(rowStrings.length != table.getNumberOfColumns(), RuntimeException.class,
                        "Line has more columns than table.");
                Object[] data = new Object[table.getNumberOfColumns()];
                IntStream.range(0, table.getNumberOfColumns())
                        .forEach((i) -> data[colIndices[i]] = unmarshal(table.getColumn(colIndices[i]), rowStrings[i]));
                table.addRow(data);
            }
        }
        catch (IOException exception)
        {
            throw new RuntimeException("Could not read from file " + file, exception);
        }
        return table;
    }

    /**
     * Reads the header file and derives the columns.
     * @param file String; file.
     * @return ListTable; empty table with the columns.
     */
    private ListTable readHeaderFile(final String file)
    {
        Collection<Column<?>> columns = new LinkedHashSet<>();
        String[] metaFields = new String[4];
        try (Scanner scanner = new Scanner(new File(file + ".header")).useDelimiter(this.delim))
        {
            // id, description, locale and separator
            int strIndex = 0;
            while (scanner.hasNext() && metaFields[3] == null)
            {
                metaFields[strIndex++] = scanner.next();
            }
            Throw.when(strIndex != 4, RuntimeException.class,
                    "Header file of %s is incomplete or a wrong delimiter is specified.", file);
            // column info
            String[] data = new String[4];
            int dataIndex = 0;
            while (scanner.hasNext())
            {
                String line = scanner.next();
                if (!line.isBlank())
                {
                    data[dataIndex++] = line;
                }
                else if (dataIndex > 0)
                {
                    columns.add(createColumn(data, dataIndex - 1));
                    dataIndex = 0;
                }
            }
            // create last column if file did not end with a blank line
            if (dataIndex > 0)
            {
                columns.add(createColumn(data, dataIndex - 1));
            }
        }
        catch (IOException exception)
        {
            throw new RuntimeException("Could not read from file " + file + ".header", exception);
        }
        catch (ClassNotFoundException exception)
        {
            throw new RuntimeException("Could not find class of column. Make sure that the right delimiter is set.", exception);
        }
        this.locale = new Locale(metaFields[2]);
        this.separator = metaFields[3];
        return new ListTable(metaFields[0], metaFields[1], columns);
    }

    /**
     * Create a column based on the lines in the header file describing a column.
     * @param data String[]; array of 4 Strings with the lines in the header file describing a column.
     * @param numberOfLines int; number of lines that were read in to the array (starting at 0).
     * @return Column&lt;?^gt;; columns.
     * @throws IOException if the number of lines read is not 4 or 3 (i.e. without a line describing the unit).
     * @throws ClassNotFoundException if the data type class could not be found.
     */
    private Column<?> createColumn(final String[] data, final int numberOfLines) throws IOException, ClassNotFoundException
    {
        Throw.when(numberOfLines < 2 || numberOfLines > 3, IOException.class,
                "Table header file is invalid. It must have 4 lines for id, description, locale and separator followed"
                        + " by a blank line, and then 3 or 4 lines per described column, separated by a blank line.");
        Class<?> clazz = Class.forName(data[2]);
        String unit = null;
        if (numberOfLines == 3)
        {
            unit = data[3];
            if (unit.startsWith("["))
            {
                unit = unit.substring(1);
            }
            if (unit.endsWith("]"))
            {
                unit = unit.substring(0, unit.length() - 1);
            }
        }
        return new Column<>(data[0], data[1], clazz, unit);
    }

    /**
     * As the column order between table file and header file may be different, this method returns a mapping from index in the
     * table file, to column index. The n'th column as read in the table file, should be stored in the colIndices[n]'th column,
     * where colIndices is the result from this method.
     * @param tableFileIds String[]; column ids in the order as read from the header line in the table file.
     * @param columnsFromHeaderFile ImmutableCollection&lt;Column&lt;?&gt;&gt;; columns as derived from the header file.
     * @return int[]; column indices.
     * @throws IOException if the columns in both files do not match.
     */
    private int[] columnIndices(final String[] tableFileIds, final ImmutableCollection<Column<?>> columnsFromHeaderFile)
            throws IOException
    {
        Throw.when(tableFileIds.length != columnsFromHeaderFile.size(), IOException.class,
                "Table file and header file have a different number of columns.");
        int[] colIndices = new int[tableFileIds.length];
        Set<Integer> colNumbersDuplicateCheck = new LinkedHashSet<>();
        List<String> columnIdsFromHeaderFile =
                columnsFromHeaderFile.stream().map((col) -> col.getId()).collect(Collectors.toList());
        for (int i = 0; i < tableFileIds.length; i++)
        {
            int colIndex = columnIdsFromHeaderFile.indexOf(tableFileIds[i]);
            Throw.when(colIndex < 0 || colNumbersDuplicateCheck.contains(colIndex), IOException.class,
                    "Columns in table file and header file do not match.");
            colIndices[i] = colIndex;
            colNumbersDuplicateCheck.add(colIndex);
        }
        return colIndices;
    }

    /**
     * Unmarshal a value.
     * @param <T> value type.
     * @param column Column&lt;? extends T&gt;; columns.
     * @param value String; String representation of the value.
     * @return T; unmarshalled value.
     */
    @SuppressWarnings("unchecked")
    private <T> T unmarshal(final Column<? extends T> column, final String value)
    {
        if (value.isBlank())
        {
            return null;
        }
        Class<? extends T> clazz = (Class<? extends T>) column.getValueType();
        Unmarshaller<T> unmarshaller = (Unmarshaller<T>) this.unmarshallers.getOrDefault(clazz, DEFAULT_UNMARSHALLER);
        return unmarshaller.unmarshal(clazz, this.locale, value, column.getUnit());
    }

    /**
     * Unmarshaller interface. To generate a lambda function use {@code (clazz, locale, value, unit) -> ... your code ...}.
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
    public interface Unmarshaller<T>
    {
        /**
         * Unmarshal value.
         * @param clazz Class&lt;? extends T&gt;; class of data type.
         * @param locale Locale; locale of the value formatting.
         * @param value String; value, guaranteed not to be blank according to {@code String.isBlank()}.
         * @param unit String; unit of the value formatting.
         * @return T; unmarshalled representation of the value.
         */
        T unmarshal(Class<? extends T> clazz, Locale locale, String value, String unit);

        /**
         * Default unmarshalling.
         * @param clazz Class&lt;?&gt;; class of data type.
         * @param locale Locale; locale of the value formatting.
         * @param value String; value, guaranteed not to be blank according to {@code String.isBlank()}.
         * @param unit String; unit of the value formatting.
         * @return Object; unmarshalled representation of the value.
         */
        static Object unmarshalDefault(final Class<?> clazz, final Locale locale, final String value, final String unit)
        {
            if (String.class.equals(clazz))
            {
                return value;
            }
            if (Character.class.equals(clazz))
            {
                return Character.valueOf(value.charAt(0));
            }
            if (Float.class.equals(clazz))
            {
                return Float.valueOf(value);
            }
            if (Double.class.equals(clazz))
            {
                return Double.valueOf(value);
            }
            if (Boolean.class.equals(clazz))
            {
                return Boolean.valueOf(value);
            }
            if (Short.class.equals(clazz))
            {
                return Short.valueOf(value);
            }
            if (Integer.class.equals(clazz))
            {
                return Integer.valueOf(value);
            }
            if (Long.class.equals(clazz))
            {
                return Long.valueOf(value);
            }
            if (Byte.class.equals(clazz))
            {
                return Byte.valueOf(value);
            }
            if (Scalar.class.isAssignableFrom(clazz))
            {
                try
                {
                    return clazz.getDeclaredMethod("valueOf", String.class).invoke(clazz, value + unit);
                }
                catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
                        | SecurityException exception)
                {
                    throw new RuntimeException(
                            "Unable to create " + clazz.getName() + " with argument '" + value + "' and unit '" + unit + "'.",
                            exception);
                }
            }
            throw new UnsupportedOperationException("Unable to process value " + value + " for type " + clazz.getName());
        }
    }

}
