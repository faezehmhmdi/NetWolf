import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    static ArrayList<Peer> createdPeers = new ArrayList<>();

    public static void main(String[] args) throws UnknownHostException, SocketException, FileNotFoundException, InterruptedException {
        int numberOfPeers = 3;
        // create peers
        for (int i = 0; i < numberOfPeers; i++) {
            Peer peer = new Peer("N" + (i + 1), InetAddress.getByName("127.0.0.1"), 2000 + i + 1, "./N" + (i + 1));
            createdPeers.add(peer);
        }
        // load neighbors
        for (int i = 0; i < numberOfPeers; i++) {
            File file = new File("./n" + (i + 1) + ".txt");
            if (!file.exists())
                continue;
            FileReader fileReader = new FileReader(file);
            Scanner scanner = new Scanner(fileReader);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String name = line.substring(0, line.indexOf(" "));
                String host = line.substring(line.indexOf(" "));
                createdPeers.get(i).addNeighbour(name, host);
            }
        }
        for (int i = 0; i < createdPeers.size(); i++) {
            System.out.println("N" + (i + 1) + " neighbours:");
            System.out.println(createdPeers.get(i).discoveredPeers);
        }
        //starting discovery thread
        for (int i = 0; i < numberOfPeers; i++) {
            createdPeers.get(i).startReceiver();
            createdPeers.get(i).startDiscovery();
        }
        Scanner sc = new Scanner(System.in);
        String input = sc.nextLine();
        if (input.contains("Get")) {
            String sub = input.replace("Get ", "");
            String index = sub.substring(0, sub.indexOf(" "));
            String filename = sub.substring(sub.indexOf(" ") + 1);
            //wait for discovery
            Thread.sleep(4500L);
            createdPeers.get(Integer.parseInt(index) - 1).startShare(filename);
            System.out.println("Now " + createdPeers.get(Integer.parseInt(index) - 1).getName() + " is requesting " + filename);

        }
        if (input.contains("Ls")) {
            String index = input.substring(input.indexOf(" ") + 1);
            System.out.println(createdPeers.get(Integer.parseInt(index) - 1).discoveredPeers.keySet());

        }
    }

}

