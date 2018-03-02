package multicast_v03;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;

public class Config {
	HashMap<Integer, InetSocketAddress> addrMap;
	HashMap<InetSocketAddress, Integer> idMap;
	int minDelay;
	int maxDelay;
	public Config(HashMap<Integer, InetSocketAddress> addrMap, HashMap<InetSocketAddress, Integer> idMap, int minDelay, int maxDelay) {
		this.addrMap = addrMap;
		this.idMap = idMap;
		this.minDelay = minDelay;
		this.maxDelay = maxDelay;
	} 
}
