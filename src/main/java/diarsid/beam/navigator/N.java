package diarsid.beam.navigator;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class N {
    public static void main(String[] args) {
        String hostname = "Unknown";

        try
        {
            InetAddress addr;
            addr = InetAddress.getLocalHost();
            hostname = addr.getHostName();
            System.out.println(hostname);
        }
        catch (UnknownHostException ex)
        {
            System.out.println("Hostname can not be resolved");
        }
    }
}
