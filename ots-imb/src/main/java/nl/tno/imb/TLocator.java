package nl.tno.imb;

//import java.io.InputStream;
//import java.io.OutputStream;
//import java.util.Arrays;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
//import java.net.Socket;
import java.net.SocketException;

public class TLocator
{
    private static final int MaxUDPCommandBufferSize = 512 - 62;

    private static final String ProtocolSep = "://";

    public static String DecodeServerURIServer(String aServerURI)
    {
        if (!aServerURI.isEmpty())
        {
            int i = aServerURI.indexOf(ProtocolSep);
            if (i >= 0)
            {
                String server = aServerURI.substring(i + ProtocolSep.length());
                i = server.indexOf('/');
                if (i >= 0)
                    server = server.substring(0, i);
                i = server.indexOf(':');
                if (i >= 0)
                    return server.substring(0, i);
                else
                    return server;
            }
            else
                return "";
        }
        else
            return "";
    }

    public static int DecodeServerURIPort(String aServerURI)
    {
        if (!aServerURI.isEmpty())
        {
            int i = aServerURI.indexOf(ProtocolSep);
            if (i >= 0)
            {
                String server = aServerURI.substring(i + ProtocolSep.length());
                i = server.indexOf('/');
                if (i >= 0)
                    server = server.substring(0, i);
                i = server.indexOf(':');
                if (i >= 0)
                    return Integer.parseInt(server.substring(i + 1));
                else
                    return 4000;
            }
            else
                return 4000;
        }
        else
            return 4000;
    }

    public static String LocateServerURI(boolean aIPv4, int aPort, int aTimeout)
    {
        try
        {
            TByteBuffer Buffer = new TByteBuffer();
            Buffer.prepare(TConnection.MAGIC_BYTES);
            Buffer.prepare(TEventEntry.IC_HUB_LOCATE);
            Buffer.prepare((int) 0);
            Buffer.prepareApply();
            Buffer.qWrite(TConnection.MAGIC_BYTES);
            Buffer.qWrite(TEventEntry.IC_HUB_LOCATE);
            Buffer.qWrite((int) 0);

            DatagramSocket fSocket = new DatagramSocket();
            try
            {
                if (aIPv4)
                {
                    // IPv4 locator request
                    fSocket.setSoTimeout(aTimeout);
                    fSocket.setBroadcast(true);
                    try
                    {
                        fSocket.send(new DatagramPacket(Buffer.getBuffer(), Buffer.getLength(),
                                InetAddress.getByName("255.255.255.255"), aPort));
                        // byte[] buffer = new byte[MaxUDPCommandBufferSize];
                        Buffer.clear(MaxUDPCommandBufferSize);
                        DatagramPacket receivedData = new DatagramPacket(Buffer.getBuffer(), MaxUDPCommandBufferSize);
                        fSocket.receive(receivedData);
                        // decode received data
                        if (Buffer.compare(TConnection.MAGIC_BYTES, 0))
                        {
                            Buffer.skipReading(TConnection.MAGIC_BYTES.length);
                            int command = Buffer.readInt32();
                            if (command == TEventEntry.IC_HUB_FOUND)
                            {
                                String server = Buffer.readString();
                                if (Buffer.compare(TConnection.MAGIC_STRING_CHECK, 0))
                                {
                                    return server;
                                }
                            }
                        }
                        return "server"; // TODO:
                    }
                    catch (IOException ex)
                    {
                        return "";
                    }
                }
                else
                {
                    // IPv6 locator request
                }
            }
            finally
            {
                fSocket.close();
            }
            return "";
        }
        catch (SocketException ex)
        {
            return "";
        }
    }

}
