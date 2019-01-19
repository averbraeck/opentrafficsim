package org.opentrafficsim.imb.demo;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Calendar;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import nl.tno.imb.TByteBuffer;
import nl.tno.imb.TConnection;
import nl.tno.imb.TEventEntry;

/**
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Aug 19, 2016 <br>
 * @author TNO
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author Walter Lohman
 */
public class IMBChat extends JFrame
{

    /** */
    private static final long serialVersionUID = 1L;

    /** Name of this chat user. */
    private JTextField editName;

    /** IMB Federation. */
    private JTextField editFederation;

    /** Message the the user is typing. */
    private JTextField editMessage;

    /** Messages that have been exchanged. */
    private JTextArea messages;

    /** Initiate connection to the IMB hub. */
    JButton btnConnect;

    /** Connection to the IMB hub. */
    private static TConnection connection = null;

    /** Message channel. */
    private static TEventEntry chatMessageEvent = null;

    /**
     * Convert a time.
     * @param comTime double; the time in days since windows epoch
     * @return Date
     */
    static public Date convertWindowsTimeToDate(double comTime)
    {
        return new Date(convertWindowsTimeToMilliseconds(comTime));
    }

    /**
     * Convert a time.
     * @param comTime double; the time in days since windows epoch
     * @return double; java time in milliseconds
     */
    static public long convertWindowsTimeToMilliseconds(double comTime)
    {
        long result = 0;

        comTime = comTime - 25569D;
        Calendar cal = Calendar.getInstance();
        result = Math.round(86400000L * comTime) - cal.get(Calendar.ZONE_OFFSET);
        cal.setTime(new Date(result));
        result -= cal.get(Calendar.DST_OFFSET);

        return result;
    }

    /**
     * Convert a Date to windows time.
     * @param javaDate Date; the date to convert
     * @return double; time in milliseconds
     */
    static public double convertDateToWindowsTime(Date javaDate)
    {
        if (javaDate == null)
        {
            throw new IllegalArgumentException("cannot convert null to windows time");
        }
        return convertMillisecondsToWindowsTime(javaDate.getTime());
    }

    /**
     * Convert a java time in milliseconds to windows time
     * @param milliseconds long; the java time in milliseconds
     * @return double; time in days since windows epoch
     */
    static public double convertMillisecondsToWindowsTime(long milliseconds)
    {
        double result = 0.0;

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(milliseconds);
        milliseconds += (cal.get(Calendar.ZONE_OFFSET) + cal.get(Calendar.DST_OFFSET)); // add GMT offset
        result = (milliseconds / 86400000D) + 25569D;

        return result;
    }

    /**
     * Add received message to the message received box
     * @param aDateTime Date; when was the message sent
     * @param aName String; name of sender
     * @param aMessage String; the message that was sent
     */
    void AddMessage(final Date aDateTime, final String aName, final String aMessage)
    {
        final JTextArea ta = this.messages;
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                ta.setText(aDateTime.toString() + " " + aName + ": " + aMessage);
            }

        });
    }

    /**
     * Are we currently connected to an IMB server?
     * @return boolean
     */
    private boolean getConnected()
    {
        if (connection != null)
            return connection.isConnected();
        else
            return false;
    }

    /**
     * Connect to the hub.
     */
    void Connect()
    {
        String server = "localhost";
        int port = 4000;
        connection = new TConnection(server, port, this.editName.getText(), 1, this.editFederation.getText());
        // TODO: connection.onDisconnect
        /*-
        connection.onDisconnect = new TConnection.TOnDisconnect()
        {
            @Override
            public void dispatch(TConnection aConnection)
            {
                // dispatch on display thread
                Display.getDefault().syncExec(new Runnable()
                {
                    public void run()
                    {
                        chatMainForm.this.btnConnect.setText("Connect");
                    }
                });
            }
        };
        */
        chatMessageEvent = connection.subscribe("Chat.Message");
        chatMessageEvent.onNormalEvent = new TEventEntry.TOnNormalEvent()
        {
            @Override
            public void dispatch(TEventEntry aEvent, TByteBuffer aPayload)
            {
                // decode message
                double datetime = aPayload.readDouble();
                final String name = aPayload.readString();
                final String message = aPayload.readString();
                final Date javaDateTime = convertWindowsTimeToDate(datetime);
                // dispatch on display thread

                // show received message
                AddMessage(javaDateTime, name, message);
            }
        };
        // TODO: chatMessageEvent.OnNormalEvent
        if (getConnected())
            this.btnConnect.setText("Disconnect");
        else
            // ShowMessage("## Could not connect to hub " + aServerURI);
            // java.awt.Dialog.
            JOptionPane.showMessageDialog(null, "## Could not connect to hub ");
    }

    /**
     * Close the connection to the IMB hub.
     */
    void Disconnect()
    {
        if (getConnected())
        {
            connection.close();
            this.btnConnect.setText("Connect");
        }
    }

    /**
     * Send the message in the editMessage buffer to the IMB hub and clear the editMessage buffer.
     */
    void Send()
    {
        Date javaDateTime;
        double datetime;
        String name;
        String message;
        TByteBuffer payload;

        // send message
        javaDateTime = new Date();
        datetime = convertDateToWindowsTime(javaDateTime);
        name = this.editName.getText();
        message = this.editMessage.getText();
        // encode message
        payload = new TByteBuffer();
        payload.prepare(datetime);
        payload.prepare(name);
        payload.prepare(message);
        payload.prepareApply();
        payload.qWrite(datetime);
        payload.qWrite(name);
        payload.qWrite(message);
        chatMessageEvent.signalEvent(TEventEntry.EK_NORMAL_EVENT, payload.getBuffer());
        // show our own message
        AddMessage(javaDateTime, "me", message);
        // clear previous message text
        this.editMessage.setText("");
    }

    /**
     * Start up the application.
     * @param args String[]; String[] args for main - should be empty
     */
    public static void main(String[] args)
    {
        try
        {
            EventQueue.invokeAndWait(new Runnable()
            {

                public void run()
                {
                    try
                    {
                        new IMBChat();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }

            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Create the window.
     */
    public IMBChat()
    {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter()
        {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent)
            {
                Disconnect();
            }
        });
        setPreferredSize(new Dimension(693, 443));
        setTitle("Chat - Java");
        JPanel mainPanel = new JPanel(new BorderLayout());
        getContentPane().add(mainPanel);

        JPanel topPanel = new JPanel(new BorderLayout());
        mainPanel.add(topPanel, BorderLayout.NORTH);
        JPanel namePanel = new JPanel(new BorderLayout());
        topPanel.add(namePanel, BorderLayout.LINE_START);
        JLabel lblName = new JLabel("Name");
        namePanel.add(lblName, BorderLayout.LINE_START);

        this.editName = new JFormattedTextField(System.getProperty("user.name"));
        this.editName.setPreferredSize(new Dimension(100, 20));
        namePanel.add(this.editName, BorderLayout.LINE_END);

        JPanel federationPanel = new JPanel(new BorderLayout());
        topPanel.add(federationPanel, BorderLayout.CENTER);
        JLabel lblFederation = new JLabel("Federation");
        federationPanel.add(lblFederation, BorderLayout.LINE_START);

        this.editFederation = new JFormattedTextField(TConnection.DEFAULT_FEDERATION);
        federationPanel.add(this.editFederation, BorderLayout.LINE_END);

        this.btnConnect = new JButton("Connect");
        this.btnConnect.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (IMBChat.this.btnConnect.getText().compareTo("Connect") == 0)
                    Connect();
                else
                    Disconnect();
            }
        });

        topPanel.add(this.btnConnect, BorderLayout.LINE_END);

        this.messages = new JTextArea("hier komen de messages");
        mainPanel.add(this.messages, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        this.editMessage = new JTextField("");
        this.editMessage.addKeyListener(new KeyListener()
        {
            @Override
            public void keyTyped(KeyEvent e)
            {
                if (e.getKeyChar() == 13)
                {
                    Send();
                }
            }

            @Override
            public void keyPressed(KeyEvent e)
            {
                // ignore
            }

            @Override
            public void keyReleased(KeyEvent e)
            {
                // ignore
            }

        });
        bottomPanel.add(this.editMessage, BorderLayout.CENTER);

        JButton btnSend = new JButton("Send");
        btnSend.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                Send();
            }
        });
        bottomPanel.add(btnSend, BorderLayout.LINE_END);
        this.pack();
        this.setVisible(true);
    }

}
