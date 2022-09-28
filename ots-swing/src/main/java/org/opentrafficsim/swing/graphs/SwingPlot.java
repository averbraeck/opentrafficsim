package org.opentrafficsim.swing.graphs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.opentrafficsim.draw.graphs.AbstractPlot;
import org.opentrafficsim.draw.graphs.JFileChooserWithSettings;
import org.opentrafficsim.draw.graphs.PointerHandler;

/**
 * Swing wrapper of all plots. This schedules regular updates, creates menus and deals with listeners. There are a number of
 * delegate methods for sub classes to implement.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class SwingPlot extends JFrame
{
    /**  */
    private static final long serialVersionUID = 20190823L;

    /** The JFreeChart plot. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected final AbstractPlot plot;

    /** Status label. */
    private JLabel statusLabel;

    /** Detach menu item. */
    private JMenuItem detach;

    /**
     * Construct a new Swing container for an AbstractPlot.
     * @param plot AbstractPlot; the plot to embed
     */
    public SwingPlot(final AbstractPlot plot)
    {
        this.plot = plot;
        // status label
        this.statusLabel = new JLabel(" ", SwingConstants.CENTER);
        add(this.statusLabel, BorderLayout.SOUTH);
        setChart(plot.getChart());
    }

    /**
     * Add the chart.
     * @param chart JFreeChart; the chart
     */
    protected void setChart(final JFreeChart chart)
    {
        // this.plot.setChart(chart);
        // override to gain some control over the auto bounds
        ChartPanel chartPanel = new ChartPanel(chart)
        {
            /** */
            private static final long serialVersionUID = 20181006L;

            /** {@inheritDoc} */
            @Override
            public void restoreAutoDomainBounds()
            {
                super.restoreAutoDomainBounds();
                if (chart.getPlot() instanceof XYPlot)
                {
                    SwingPlot.this.plot.setAutoBoundDomain(chart.getXYPlot());
                }
            }

            /** {@inheritDoc} */
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
        ChartMouseListener chartListener = getChartMouseListener();
        if (chartListener != null)
        {
            chartPanel.addChartMouseListener(chartListener);
        }

        // pointer handler
        final PointerHandler ph = new PointerHandler()
        {
            /** {@inheritDoc} */
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
        chartPanel.addMouseMotionListener(ph);
        chartPanel.addMouseListener(ph);
        add(chartPanel, BorderLayout.CENTER);
        chartPanel.setMouseWheelEnabled(true);

        // pop up
        JPopupMenu popupMenu = chartPanel.getPopupMenu();
        popupMenu.add(new JPopupMenu.Separator());
        this.detach = new JMenuItem("Show in detached window");
        this.detach.addActionListener(new ActionListener()
        {
            @SuppressWarnings("synthetic-access")
            @Override
            public void actionPerformed(final ActionEvent e)
            {
                SwingPlot.this.detach.setEnabled(false);
                JFrame window = new JFrame(getPlot().getCaption());
                window.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
                window.add(chartPanel, BorderLayout.CENTER);
                window.add(SwingPlot.this.statusLabel, BorderLayout.SOUTH);
                window.addWindowListener(new WindowAdapter()
                {
                    /** {@inheritDoc} */
                    @Override
                    public void windowClosing(@SuppressWarnings("hiding") final WindowEvent e)
                    {
                        add(chartPanel, BorderLayout.CENTER);
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
        addPopUpMenuItems(popupMenu);
    }

    /**
     * Manually set status label from sub class. Will be overwritten by a moving mouse pointer over the axes.
     * @param label String; label to set
     */
    protected final void setStatusLabel(final String label)
    {
        if (this.statusLabel != null)
        {
            this.statusLabel.setText(label);
        }
    }

    /**
     * Overridable method to add pop up items.
     * @param popupMenu JPopupMenu; pop up menu
     */
    protected void addPopUpMenuItems(final JPopupMenu popupMenu)
    {
        //
    }

    /**
     * Overridable; may return a chart listener for additional functions.
     * @return ChartMouseListener, {@code null} by default
     */
    protected ChartMouseListener getChartMouseListener()
    {
        return null;
    }

    /**
     * Retrieve the plot.
     * @return AbstractPlot; the plot
     */
    public AbstractPlot getPlot()
    {
        return this.plot;
    }

}
