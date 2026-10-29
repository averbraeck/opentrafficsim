package org.opentrafficsim.swing.graphs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Optional;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.stream.XMLStreamException;

import org.djutils.data.Table;
import org.djutils.data.csv.CsvData;
import org.djutils.data.json.JsonData;
import org.djutils.data.serialization.TextSerializationException;
import org.djutils.data.xml.XmlData;
import org.djutils.io.CompressedFileWriter;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.opentrafficsim.animation.graphs.AbstractPlot;
import org.opentrafficsim.animation.graphs.JFileChooserWithSettings;
import org.opentrafficsim.animation.graphs.PointerHandler;
import org.opentrafficsim.animation.graphs.TabularDataset;

/**
 * Swing wrapper of all plots. This schedules regular updates, creates menus and deals with listeners. There are a number of
 * delegate methods for sub classes to implement.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Alexander Verbraeck
 * @author Peter Knoppers
 * @author Wouter Schakel
 */
public class SwingPlot extends JFrame
{
    /** ID. */
    private static final long serialVersionUID = 20190823L;

    /** The JFreeChart plot. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected final AbstractPlot<?> plot;

    /** Status label. */
    private JLabel statusLabel;

    /** Detach menu item. */
    private JMenuItem detach;

    /** Chart panel. */
    private ChartPanel chartPanel;

    /**
     * Construct a new Swing container for an AbstractPlot.
     * @param plot the plot to embed
     */
    public SwingPlot(final AbstractPlot<?> plot)
    {
        this.plot = plot;
        // status label
        this.statusLabel = new JLabel(" ", SwingConstants.CENTER);
        add(this.statusLabel, BorderLayout.SOUTH);
        setChart(plot.getChart());
    }

    /**
     * Add the chart.
     * @param chart the chart
     */
    protected void setChart(final JFreeChart chart)
    {
        // this.plot.setChart(chart);
        // override to gain some control over the auto bounds
        this.chartPanel = createPanel(chart);
        Optional<ChartMouseListener> chartListener = getChartMouseListener();
        if (chartListener.isPresent())
        {
            this.chartPanel.addChartMouseListener(chartListener.get());
        }

        // pointer handler
        final PointerHandler ph = new PointerHandler()
        {
            @Override
            public void updateHint(final double domainValue, final double rangeValue)
            {
                if (Double.isNaN(domainValue))
                {
                    setStatusLabel(" ");
                }
                else
                {
                    setStatusLabel(SwingPlot.this.plot.getStatusLabel(domainValue, rangeValue));
                }
            }
        };
        this.chartPanel.addMouseMotionListener(ph);
        this.chartPanel.addMouseListener(ph);
        add(this.chartPanel, BorderLayout.CENTER);
        this.chartPanel.setMouseWheelEnabled(true);

        // pop up
        JPopupMenu popupMenu = this.chartPanel.getPopupMenu();
        addSaveAsDataItems(chart, popupMenu);
        addDetachedWindowItem(popupMenu);
        addPopUpMenuItems(popupMenu);
    }

    /**
     * Adds data export options to save-as menu.
     * @param chart chart
     * @param popupMenu pop-up menu
     */
    private void addSaveAsDataItems(final JFreeChart chart, final JPopupMenu popupMenu)
    {
        if (chart.getPlot() instanceof XYPlot xyPlot && xyPlot.getDataset() instanceof TabularDataset)
        {
            for (Component c : popupMenu.getComponents())
            {
                if (c instanceof JMenu menu && menu.getText().equals("Save as"))
                {
                    menu.add(new JPopupMenu.Separator());
                    JMenuItem saveCsv = new JMenuItem("CSV...");
                    saveCsv.addActionListener(
                            new SaveDataActionListener(chart, new FileNameExtensionFilter("CSV (.csv)", "csv")));
                    menu.add(saveCsv);
                    JMenuItem saveZip = new JMenuItem("Zipped CSV...");
                    saveZip.addActionListener(
                            new SaveDataActionListener(chart, new FileNameExtensionFilter("Zipped CSV (.zip)", "zip")));
                    menu.add(saveZip);
                    JMenuItem saveJson = new JMenuItem("JSON...");
                    saveJson.addActionListener(
                            new SaveDataActionListener(chart, new FileNameExtensionFilter("JSON (.json)", "json")));
                    menu.add(saveJson);
                    JMenuItem saveXml = new JMenuItem("XML...");
                    saveXml.addActionListener(
                            new SaveDataActionListener(chart, new FileNameExtensionFilter("XML (.xml)", "xml")));
                    menu.add(saveXml);
                    return;
                }
            }
            throw new IllegalStateException("Menu item \"Save as\" could not be found to add save options to.");
        }
    }

    /**
     * Adds detached window item.
     * @param popupMenu pop-up menu
     */
    private void addDetachedWindowItem(final JPopupMenu popupMenu)
    {
        popupMenu.add(new JPopupMenu.Separator());
        this.detach = new JMenuItem("Show in detached window");
        this.detach.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(final ActionEvent e)
            {
                SwingPlot.this.detach.setEnabled(false);
                JFrame window = new JFrame(getPlot().getCaption());
                window.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
                window.add(SwingPlot.this.chartPanel, BorderLayout.CENTER);
                window.add(SwingPlot.this.statusLabel, BorderLayout.SOUTH);
                window.addWindowListener(new WindowAdapter()
                {
                    @Override
                    public void windowClosing(final WindowEvent e)
                    {
                        add(SwingPlot.this.chartPanel, BorderLayout.CENTER);
                        add(SwingPlot.this.statusLabel, BorderLayout.SOUTH);
                        SwingPlot.this.detach.setEnabled(true);
                        SwingPlot.this.getContentPane().validate();
                        SwingPlot.this.getContentPane().repaint();
                    }
                });
                window.pack();
                window.setVisible(true);
                SwingPlot.this.getContentPane().repaint();
            }
        });
        popupMenu.add(this.detach);
    }

    /**
     * Create panel.
     * @param chart chart
     * @return panel
     */
    private ChartPanel createPanel(final JFreeChart chart)
    {
        return new ChartPanel(chart)
        {
            /** Serialization version UID. */
            private static final long serialVersionUID = 20181006L;

            @Override
            public void restoreAutoDomainBounds()
            {
                super.restoreAutoDomainBounds();
                if (chart.getPlot() instanceof XYPlot)
                {
                    SwingPlot.this.plot.setAutoBoundDomain(chart.getXYPlot());
                }
            }

            @Override
            public void restoreAutoRangeBounds()
            {
                super.restoreAutoRangeBounds();
                if (chart.getPlot() instanceof XYPlot)
                {
                    SwingPlot.this.plot.setAutoBoundRange(chart.getXYPlot());
                }
            }

            /** {@inheritDoc} This implementation adds control over the PNG image size and font size. */
            @Override
            public void doSaveAs() throws IOException
            {
                // the code in this method is based on the code in the super implementation

                // create setting components
                JLabel fontSizeLabel = new JLabel("font size");
                JTextField fontSize = new JTextField("32"); // by default, give more space for labels in a png export
                fontSize.setToolTipText("Font size of title (other fonts are scaled)");
                fontSize.setPreferredSize(new Dimension(40, 20));
                JTextField width = new JTextField("960");
                width.setToolTipText("Width [pixels]");
                width.setPreferredSize(new Dimension(40, 20));
                JLabel x = new JLabel("x");
                JTextField height = new JTextField("540");
                height.setToolTipText("Height [pixels]");
                height.setPreferredSize(new Dimension(40, 20));

                // create file chooser with these components
                JFileChooser fileChooser = new JFileChooserWithSettings(fontSizeLabel, fontSize, width, x, height);
                fileChooser.setCurrentDirectory(getDefaultDirectoryForSaveAs());
                FileNameExtensionFilter filter =
                        new FileNameExtensionFilter(localizationResources.getString("PNG_Image_Files"), "png");
                fileChooser.addChoosableFileFilter(filter);
                fileChooser.setFileFilter(filter);

                int option = fileChooser.showSaveDialog(this);
                if (option == JFileChooser.APPROVE_OPTION)
                {
                    String filename = fileChooser.getSelectedFile().getPath();
                    if (isEnforceFileExtensions())
                    {
                        if (!filename.endsWith(".png"))
                        {
                            filename = filename + ".png";
                        }
                    }

                    // get settings from setting components
                    double fs; // relative scale
                    try
                    {
                        fs = Double.parseDouble(fontSize.getText());
                    }
                    catch (NumberFormatException exception)
                    {
                        fs = 16.0;
                    }
                    int w;
                    try
                    {
                        w = Integer.parseInt(width.getText());
                    }
                    catch (NumberFormatException exception)
                    {
                        w = getWidth();
                    }
                    int h;
                    try
                    {
                        h = Integer.parseInt(height.getText());
                    }
                    catch (NumberFormatException exception)
                    {
                        h = getHeight();
                    }
                    OutputStream out = new BufferedOutputStream(new FileOutputStream(new File(filename)));
                    out.write(SwingPlot.this.plot.encodeAsPng(w, h, fs));
                    out.close();
                }
            }
        };
    }

    /**
     * Save data action.
     */
    private class SaveDataActionListener implements ActionListener
    {
        /** Chart. */
        private final JFreeChart chart;

        /** File filter. */
        private final FileNameExtensionFilter filter;

        /**
         * Constructor.
         * @param chart chart
         * @param filter file filter
         */
        SaveDataActionListener(final JFreeChart chart, final FileNameExtensionFilter filter)
        {
            this.chart = chart;
            this.filter = filter;
        }

        @Override
        public void actionPerformed(final ActionEvent e)
        {
            String extension = this.filter.getExtensions()[0];
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(this.filter);
            String name = this.chart.getTitle().getText();
            name = name == null ? "" // illegal chars, collapse whitespace, no trailing dot/space
                    : name.replaceAll("[\\\\/:*?\"<>|]", "_").replaceAll("\\s+", " ").trim().replaceAll("[. ]+$", "");
            name = name.length() > 64 ? name.substring(0, 64) : name;
            fileChooser.setSelectedFile(new File(name + "." + extension));
            Window window = SwingUtilities.getWindowAncestor(SwingPlot.this.chartPanel);
            int option = fileChooser.showSaveDialog(window);
            if (option == JFileChooser.APPROVE_OPTION)
            {
                File file = fileChooser.getSelectedFile();
                String fileName = file.getAbsolutePath();
                if (file.exists())
                {
                    int result =
                            JOptionPane.showConfirmDialog(window, "The file already exists:\n" + fileName + "\n\nOverwrite it?",
                                    "Confirm overwrite", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

                    if (result != JOptionPane.YES_OPTION)
                    {
                        return; // User cancelled
                    }
                }
                if (!fileName.toLowerCase().endsWith("." + extension))
                {
                    fileName = fileName + "." + extension;
                }
                Table table = ((TabularDataset) this.chart.getXYPlot().getDataset()).asTable();
                try
                {
                    switch (extension)
                    {
                        case "csv" -> CsvData.writeData(fileName, fileName + ".header", table);
                        case "zip" ->
                        {
                            String csvName = Path.of(fileName).getFileName().toString();
                            int dot = csvName.lastIndexOf('.');
                            csvName = (dot >= 0 ? csvName.substring(0, dot) : csvName) + ".csv";
                            CsvData.writeZippedData(new CompressedFileWriter(fileName), csvName, csvName + ".header", table);
                        }
                        case "json" -> JsonData.writeData(fileName, table);
                        case "xml" -> XmlData.writeData(fileName, table);
                        default -> JOptionPane.showMessageDialog(window, "Data could not be saved.", "Data could not be saved",
                                JOptionPane.WARNING_MESSAGE);
                    }
                }
                catch (TextSerializationException | XMLStreamException | IOException ex)
                {
                    JOptionPane.showMessageDialog(window, "Data could not be saved.", "Data could not be saved",
                            JOptionPane.WARNING_MESSAGE);
                }
                JOptionPane.showMessageDialog(window, "File saved.", "File saved", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    /**
     * Manually set status label from sub class. Will be overwritten by a moving mouse pointer over the axes.
     * @param label label to set
     */
    protected void setStatusLabel(final String label)
    {
        if (this.statusLabel != null)
        {
            this.statusLabel.setText(label);
        }
    }

    /**
     * Overridable method to add pop up items.
     * @param popupMenu pop up menu
     */
    protected void addPopUpMenuItems(final JPopupMenu popupMenu)
    {
        //
    }

    /**
     * Overridable; may return a chart listener for additional functions.
     * @return ChartMouseListener, empty by default
     */
    protected Optional<ChartMouseListener> getChartMouseListener()
    {
        return Optional.empty();
    }

    /**
     * Retrieve the plot.
     * @return the plot
     */
    public AbstractPlot<?> getPlot()
    {
        return this.plot;
    }

    /**
     * Returns the chart panel.
     * @return chart panel.
     */
    public ChartPanel getChartPanel()
    {
        return this.chartPanel;
    }

}
