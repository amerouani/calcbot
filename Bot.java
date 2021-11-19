import javax.net.ssl.SSLSocket;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.net.InetAddress;

public abstract class Bot {

    protected InetAddress address;
    protected Integer port;
    protected BufferedWriter output = null;
    protected BufferedReader input = null;
    SSLSocket fd = null;

    public Bot(String ip, Integer port) {
        try {
            this.address = InetAddress.getByName(ip);
            this.port = port;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public abstract void connect();
}
