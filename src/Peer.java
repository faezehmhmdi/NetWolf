import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Thread.sleep;

public class Peer {
    DatagramSocket socket;
    final HashMap<String, String> discoveredPeers = new HashMap<>(); // map from name -> ip:port
    ArrayList<File> files;
    String name;
    int port;
    InetAddress inetAddress;
    boolean running;


    Peer(String name, InetAddress inetAddress, int port, String directory) throws SocketException, UnknownHostException {
        //todo read directory file names and keep in files
        this.files = new ArrayList<>();
        this.socket = new DatagramSocket(port, inetAddress);
        this.name = name;
        this.running = true;
        this.port = port;
        System.out.print(name + " ");
        System.out.print(port + " ");
        System.out.println(inetAddress);
        System.out.println("*****");
        this.inetAddress = inetAddress;
    }

    public void startReceiver() {
        Thread thread = new Thread(() -> {
            // bind socket
//            try {
//                socket.bind(new InetSocketAddress(this.inetAddress, this.port));
//            } catch (SocketException e) {
//                e.printStackTrace();
//            }
            byte[] buf = new byte[4096];
            DatagramPacket packet;
            String message, content, command;
            while (this.running) {
                packet = new DatagramPacket(buf, buf.length);
                try {
                    this.socket.receive(packet);
                    message = new String(packet.getData(), 0, packet.getLength());
//                    System.out.println(message);
                    String[] command_content = message.split(" ", 2);
                    command = command_content[0];
                    content = command_content[1];
                    switch (command) {
                        case "Discovery":
                            // merge with our cluster
                            String[] nodes = content.split(" ");
                            for (int i = 0; i < nodes.length; i += 2) {
                                if (!nodes[i].equals(this.name)) { // if this is not us
                                    synchronized (this.discoveredPeers) {
                                        if (!this.discoveredPeers.containsKey(nodes[i])) {
                                            System.out.println(this.name + " Found " + nodes[i] + " " + nodes[i + 1]);
                                            addNeighbour(nodes[i], nodes[i + 1]);
                                        }
                                    }
                                }
                            }
                            break;
                        default:
                            System.err.println(message);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        });
        thread.start();
    }

    public void startDiscovery() {
        Thread thread = new Thread(() -> {
            // Discovery N1 IP1:Port1 N2 IP2:Port2 ...
            String myName = "Discovery " + this.name + " " + this.inetAddress.getHostAddress() + ":" + this.port;
            StringBuilder message;
            byte[] buff;
            while (this.running) {
                message = new StringBuilder(myName + " ");
                synchronized (this.discoveredPeers) {
                    // get current neighbours
                    for (Map.Entry<String, String> neighbor_name_address : this.discoveredPeers.entrySet()) {
                        message.append(neighbor_name_address.getKey()).append(" ").append(neighbor_name_address.getValue()).append(" ");
                    }
                    // send discovery message to all
                    buff = message.toString().getBytes();
                    for (String neighbour_address : discoveredPeers.values()) {
                        String[] address_port = neighbour_address.split(":");
                        try {
                            DatagramPacket req = new DatagramPacket(buff, buff.length, InetAddress.getByName(address_port[0]), Integer.parseInt(address_port[1]));
                            this.socket.send(req);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                try {
                    sleep(2000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            this.socket.close();
        });
        thread.start();
    }


    public ArrayList<File> getFiles() {
        return files;
    }

    public DatagramSocket getSocket() {
        return socket;
    }

    public HashMap<String, String> getDiscoveredPeers() {
        return discoveredPeers;
    }

    public String getName() {
        return name;
    }

    public InetAddress getInetAddress() {
        return inetAddress;
    }

    public void addNeighbour(String name, String host) {
        discoveredPeers.put(name.trim(), host.trim());
    }


}





