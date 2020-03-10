package org.opentrafficsim.remotecontrol;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import org.djunits.unit.DurationUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.cli.Checkable;
import org.djutils.cli.CliUtil;
import org.djutils.decoderdumper.HexDumper;
import org.djutils.io.URLResource;
import org.djutils.logger.CategoryLogger;
import org.djutils.logger.LogCategory;
import org.djutils.serialization.SerializationException;
import org.pmw.tinylog.Level;
import org.sim0mq.Sim0MQException;
import org.sim0mq.message.Sim0MQMessage;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQException;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * Remotely control OTS using Sim0MQ messages.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Mar 4, 2020 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class Sim0MQRemoteController extends JFrame implements WindowListener, Runnable, ActionListener
{
    /** ... */
    private static final long serialVersionUID = 20200304L;

    /**
     * The command line options.
     */
    @Command(description = "Test program for Remote Control OTS", name = "Remote Control OTS", mixinStandardHelpOptions = true,
            version = "1.0")
    public static class Options implements Checkable
    {
        /** The IP port. */
        @Option(names = { "-p", "--port" }, description = "Internet port to use", defaultValue = "8888")
        private int port;

        /**
         * Retrieve the port.
         * @return int; the port
         */
        public final int getPort()
        {
            return this.port;
        }

        /** The host name. */
        @Option(names = { "-H", "--host" }, description = "Internet host to use", defaultValue = "localhost")
        private String host;

        /**
         * Retrieve the host name.
         * @return String; the host name
         */
        public final String getHost()
        {
            return this.host;
        }

        @Override
        public final void check() throws Exception
        {
            if (this.port <= 0 || this.port > 65535)
            {
                throw new Exception("Port should be between 1 and 65535");
            }
            // TODO verify that host looks valid.
        }
    }

    /** The instance of the RemoteControl. */
    private static Sim0MQRemoteController gui = null;

    /**
     * Start the OTS remote control program.
     * @param args String[]; the command line arguments
     */
    public static void main(final String[] args)
    {
        CategoryLogger.setAllLogLevel(Level.WARNING);
        CategoryLogger.setLogCategories(LogCategory.ALL);
        // Instantiate the RemoteControl GUI
        try
        {
            EventQueue.invokeAndWait(new Runnable()
            {
                /** {@inheritDoc} */
                @Override
                public void run()
                {
                    try
                    {
                        gui = new Sim0MQRemoteController();
                        gui.setVisible(true);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        System.exit(ERROR);
                    }
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(ERROR);
        }
        // We don't get here until the GUI is fully running.
        Options options = new Options();
        CliUtil.execute(options, args); // register Unit converters, parse the command line, etc..
        gui.processArguments(options.getHost(), options.getPort());
    }

    /** ... */
    private ZContext zContext = new ZContext(1);

    /** Communication channel to OTS server. */
    private ZMQ.Socket requester = zContext.createSocket(SocketType.DEALER);

    /** Receive thread. */
    private Thread reader = new Thread(this);

    /**
     * Open connection, then enter the command loop.
     * @param host String; host to connect to (listening OTS server should already be running)
     * @param port int; port to connect to (listening OTS server should be listening on that port)
     */
    public void processArguments(final String host, final int port)
    {
        this.output.println("host is " + host + ", port is " + port);
        // Socket to talk to server
        this.output.println("Connecting to server...");
        requester.connect("tcp://" + host + ":" + port);
        this.output.println("Connected");
        this.reader.start();
        // Gotcha: Sim0MQ does not permit starting the receiver before something has been sent
        // Send something
        try
        {
            byte[] message = Sim0MQMessage.encodeUTF8(true, 0, "master", "slave", 0, 0, "HELLO");
            output.println("Sending HELLO message:");
            output.println(HexDumper.hexDumper(message));
            this.requester.send(message, 0);
        }
        catch (Sim0MQException e)
        {
            e.printStackTrace();
        }
        catch (SerializationException e)
        {
            e.printStackTrace();
        }
        // Now start the receiver thread
    }

    /**
     * Write something to the remote OTS.
     * @param command String; the command to write
     * @throws IOException when communication fails
     */
    public void write(final String command) throws IOException
    {
        this.requester.send(command);
        // output.println("Wrote " + command.getBytes().length + " bytes");
        output.println("Sent string \"" + command + "\"");
    }

    /**
     * Write something to the remote OTS.
     * @param bytes byte[]; the bytes to write
     * @throws IOException when communication fails
     */
    public void write(final byte[] bytes) throws IOException
    {
        this.requester.send(bytes);
        // output.println("Wrote " + command.getBytes().length + " bytes");
        // output.println(HexDumper.hexDumper(bytes));
    }

    /**
     * Construct the GUI.
     */
    Sim0MQRemoteController()
    {
        // Construct the GUI
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 1000, 800);
        JPanel panelAll = new JPanel();
        panelAll.setBorder(new EmptyBorder(5, 5, 5, 5));
        panelAll.setLayout(new BorderLayout(0, 0));
        setContentPane(panelAll);
        JPanel panelControls = new JPanel();
        panelAll.add(panelControls, BorderLayout.PAGE_START);
        JTextArea textArea = new JTextArea();
        textArea.setFont(new Font("monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(800, 400));
        panelAll.add(scrollPane, BorderLayout.PAGE_END);
        this.output = new PrintStream(new TextAreaOutputStream(textArea), true);
        JPanel controls = new JPanel();
        controls.setLayout(new FlowLayout());
        JButton sendNetwork = new JButton("Send network");
        sendNetwork.setActionCommand("SendNetwork");
        sendNetwork.addActionListener(this);
        controls.add(sendNetwork);
        JButton stepTo = new JButton("Step to 10 s");
        stepTo.setActionCommand("StepTo");
        stepTo.addActionListener(this);
        controls.add(stepTo);
        JButton getGTUPositions = new JButton("Get all GTU positions");
        getGTUPositions.setActionCommand("GetAllGTUPositions");
        getGTUPositions.addActionListener(this);
        controls.add(getGTUPositions);
        panelAll.add(controls, BorderLayout.CENTER);
    }

    /** Debugging and other output goes here. */
    private PrintStream output = null;

    /**
     * Shut down this application.
     */
    public void shutDown()
    {
        if (null != this.requester)
        {
            this.requester.close();
            this.requester = null;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void windowOpened(final WindowEvent e)
    {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public final void windowClosing(final WindowEvent e)
    {
        shutDown();
    }

    /** {@inheritDoc} */
    @Override
    public void windowClosed(final WindowEvent e)
    {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void windowIconified(final WindowEvent e)
    {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void windowDeiconified(final WindowEvent e)
    {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void windowActivated(final WindowEvent e)
    {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void windowDeactivated(final WindowEvent e)
    {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void run()
    {
        do
        {
            try
            {
                // Read from remotely controlled OTS
                byte[] bytes = this.requester.recv(0);
                Object[] message = Sim0MQMessage.decode(bytes).createObjectArray();
                if (message.length > 8 && message[5] instanceof String)
                {
                    String command = (String) message[5];
                    switch (command)
                    {
                        case "GTUPOSITION":
                            output.println(String.format("%10.10s: %s x=%8.3f y=%8.3f z=%8.3f heading=%6.1f, a=%s", message[8],
                                    message[9], message[10], message[11], message[12], Math.toDegrees((Double) message[13]),
                                    message[14]));
                            break;

                        case "READY":
                            output.println("Slave is ready for the next command");
                            break;

                        default:
                            output.println("Unhandled reply: " + command);
                            output.println(HexDumper.hexDumper(bytes));
                            output.println("Received " + Sim0MQMessage.print(message));
                            break;

                    }
                }
                else
                {
                    output.println(HexDumper.hexDumper(bytes));
                }
            }
            catch (ZMQException | Sim0MQException | SerializationException e)
            {
                e.printStackTrace();
                return;
            }
        }
        while (true);
    }

    /**
     * Open an URL, read it and store the contents in a string. Adapted from
     * https://stackoverflow.com/questions/4328711/read-url-to-string-in-few-lines-of-java-code
     * @param url URL; the URL
     * @return String
     * @throws IOException when reading the file fails
     */
    public static String readStringFromURL(final URL url) throws IOException
    {
        try (Scanner scanner = new Scanner(url.openStream(), StandardCharsets.UTF_8.toString()))
        {
            scanner.useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        }
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(final ActionEvent e)
    {
        switch (e.getActionCommand())
        {
            case "SendNetwork":
            {
                String networkFile = "/TrafCODDemo2/TrafCODDemo2.xml";
                Duration warmupDuration = Duration.ZERO;
                Duration runDuration = new Duration(3600, DurationUnit.SECOND);
                Long seed = 123456L;
                URL url = URLResource.getResource(networkFile);
                System.out.println("url is " + url);
                try
                {
                    String xml = readStringFromURL(url);
                    try
                    {
                        write(Sim0MQMessage.encodeUTF8(true, 0, "RemoteControl", "OTS", "LOADNETWORK", 0, xml,
                                warmupDuration, runDuration, seed));
                    }
                    catch (IOException e1)
                    {
                        output.println("Write failed; Caught IOException");
                        ((JComponent) e.getSource()).setEnabled(false);
                    }
                    catch (Sim0MQException e1)
                    {
                        e1.printStackTrace();
                    }
                    catch (SerializationException e1)
                    {
                        e1.printStackTrace();
                    }
                }
                catch (IOException e2)
                {
                    System.err.println("Could not load file " + networkFile);
                    e2.printStackTrace();
                }
                break;
            }

            case "StepTo":
            {
                JButton button = (JButton) e.getSource();
                String caption = button.getText();
                int position;
                for (position = 0; position < caption.length(); position++)
                {
                    if (Character.isDigit(caption.charAt(position)))
                    {
                        break;
                    }
                }
                Time toTime = Time.valueOf(caption.substring(position));
                try
                {
                    write(Sim0MQMessage.encodeUTF8(true, 0, "RemoteControl", "OTS", "SIMULATEUNTIL", 0, toTime));
                    toTime = toTime.plus(new Duration(10, DurationUnit.SECOND));
                    button.setText(caption.substring(0, position)
                            + String.format("%.0f %s", toTime.getInUnit(), toTime.getDisplayUnit()));
                }
                catch (IOException | Sim0MQException | SerializationException e1)
                {
                    e1.printStackTrace();
                }
                break;
            }

            case "GetAllGTUPositions":
            {
                try
                {
                    write(Sim0MQMessage.encodeUTF8(true, 0, "RemoteControl", "OTS", "SENDALLGTUPOSITIONS", 0));
                }
                catch (IOException | Sim0MQException | SerializationException e1)
                {
                    e1.printStackTrace();
                }
                break;
            }

            default:
                output.println("Oops: unhandled action command");
        }

    }
}

/**
 * Output stream that writes to a Swing component. Derived from
 * https://www.codejava.net/java-se/swing/redirect-standard-output-streams-to-jtextarea
 */
class TextAreaOutputStream extends OutputStream
{
    /** Swing output object to append all output to. */
    private final JTextArea textArea;

    /**
     * Construct a new TextAreaOutputStream object.
     * @param textArea JTextArea; the text area to append the output onto
     */
    TextAreaOutputStream(final JTextArea textArea)
    {
        this.textArea = textArea;
    }

    /** {@inheritDoc} */
    @Override
    public void write(final int b)
    {
        synchronized (this.textArea)
        {
            // redirects data to the text area
            textArea.append(String.valueOf((char) b));
            // scrolls the text area to the end of data
            textArea.setCaretPosition(textArea.getDocument().getLength());
            // FIXME: PK wonders if it is really safe to call this method from another thread?
        }
    }

}
