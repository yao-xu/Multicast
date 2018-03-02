package multicast_v03;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class UnicastLauncher {
	public static Config parseConfig(String filename) throws IOException {
		BufferedReader file = new BufferedReader(new FileReader(filename));
		String firstLine = file.readLine();
		if(firstLine == null)
			throw new IOException("Empty Configuration File");
		String[] delays = firstLine.split(" ");
		if(delays.length != 2)
			throw new IOException("Wrong Formatted Configuration File");
		Integer minDelay = Integer.parseInt(delays[0]);
		Integer maxDelay = Integer.parseInt(delays[1]);
		if(minDelay == null || maxDelay == null) 
			throw new IOException("Wrong Formatted Configuration File");
		
		HashMap<Integer, InetSocketAddress> addrMap = new HashMap<>();
		HashMap<InetSocketAddress, Integer> idMap = new HashMap<>();
		String line = file.readLine();
		while(line != null) {
			String[] content = line.split("\\s+");
			if(content.length != 3)
				throw new IOException("Wrong Formatted Configuration File");
			Integer id = Integer.parseInt(content[0]);
			Integer port = Integer.parseInt(content[2]);
			if(id == null || port == null) 
				throw new IOException("Wrong Formatted Configuration File");
			System.out.println("id: " + id + ", ip: " + content[1] + ", port: " + port);
			addrMap.put(id, new InetSocketAddress(content[1], port));
			idMap.put(new InetSocketAddress(content[1], port), id);
			line = file.readLine();
		}
		return new Config(addrMap, idMap, minDelay, maxDelay);
	}
	
	public static void main(String[] args) throws Exception {
		BlockingQueue<String> messageQueue = new LinkedBlockingDeque<String>(100);
		int id = 2;
		String filename = "D:\\javaProj\\multicast_v03\\src\\multicast_v03\\config.txt";
		Config config = parseConfig(filename);
		new Thread(new Process(id, config.addrMap, config.minDelay, config.maxDelay, messageQueue)).start();
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in)); 
		while(true) {
			String message = br.readLine();
			messageQueue.offer(message);
		}
	}
}
