package multicast_v03;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class CausalProcess extends Process {
	int[] timeStamp;
	ArrayList<Message> sequence;
	
	public CausalProcess(int id, HashMap<Integer, InetSocketAddress> addrMap, int minDelay, int maxDelay, BlockingQueue<String> messageQueue, int numProcess) throws IOException {
		super(id, addrMap, minDelay, maxDelay, messageQueue);
		this.timeStamp = new int[numProcess];
		this.sequence = new ArrayList<Message>();
	}
	
	@Override
	public void run() {
		System.out.println("Server is up...");
		System.out.println("Local Address: " + serverSocketChannel);
		new Thread(new Runnable() {
			@Override
			public void run() {
				while(true) {
					try {
						SocketChannel socketChannel = serverSocketChannel.accept();
						//System.out.println("Accepting from: " + socketChannel.socket().getRemoteSocketAddress());
						new Thread(new Runnable() {
							@Override
							public void run() {
								try { 
									causal_receive(socketChannel.socket().getChannel());
								} catch (IOException ioe) {
									ioe.printStackTrace();
								}
							}
						}).start();
					} catch (IOException ioe) {
						ioe.printStackTrace();
					}
				}
			}
		}).start();
		while(true) {
			try {
				String messageContent = (String) (messageQueue.poll(1, TimeUnit.DAYS));
				this.timeStamp[this.id-1]++;
				int[] ts = new int[this.timeStamp.length];
				for(int i=0; i<ts.length; i++) {
					ts[i] = this.timeStamp[i];
				}
				String parsedMessage[] = messageContent.split(" ", 2);
				Message toSend = new Message(this.id, parsedMessage[1], ts);
				for(Map.Entry<Integer, InetSocketAddress> entry : addrMap.entrySet()) {
					if(entry.getKey() == this.id) {
						deliver(toSend);
						afterDeliver();
						continue;
					}
					long delay = (long)(new Random().nextDouble() * (maxDelay - minDelay) + minDelay);
					if(parsedMessage.length != 2) {
						System.out.println("Illegal formatted message! ");
						System.out.println(messageContent);
						continue;
					}
					//System.out.println("Sending \"" + parsedMessage[1] + "\"" + "to Process " + entry.getKey() + " at time: " + new Date().toString() + " with delay " + delay + " ms.");
					if(parsedMessage[0].equals("msend")) {
						new Timer().schedule(new TimerTask() {
							@Override
							public void run() {
								try {
									//System.out.print("Delay: " + delay + " ms...");
									
//									if(parsedMessage[0].equals("send")) {
//										int destId = Integer.parseInt(parsedMessage[1]);
//										unicast_send(destId, message);
//									} else {
//										System.out.println("Illegal Command");
//									}
//									if(this.id == entry.getKey())
//										deliver(toSend);
									unicast_send(entry.getKey(), toSend);
								} catch(IOException ioe) {
									ioe.printStackTrace();
								}
							}
						}, delay);
					} else {
						System.out.println("Illegal Command");
					}
				}
//				long delay = (long)(new Random().nextDouble() * (maxDelay - minDelay) + minDelay);
//				String parsedMessage[] = messageContent.split(" ", 3);
//				if(parsedMessage.length != 3) {
//					System.out.println("Illegal formatted message! ");
//					continue;
//				}
			} catch(InterruptedException itrpte) {
				itrpte.printStackTrace();
			}
		}
	}
	
	public void causal_receive(SocketChannel receiveChannel) throws IOException {
		ByteBuffer content = ByteBuffer.allocate(100);
		receiveChannel.read(content);
		content.flip();
		byte[] message = new byte[content.remaining()];
		content.get(message);
		//System.out.println(new String(message));
		Message received = deserialize(message);
		if(received.timeStamp[received.senderId-1] <= this.timeStamp[received.senderId-1]) {
			System.out.println("Reject Message: " + new String(message));
			return;
		}
		//System.out.println("Received message: ' " + received + " ' at time: " + new Date().toString());
		System.out.println("Received \"" + received.messageContent + "\" from process " + received.senderId + ", system time is " + new Date().toString());
		//System.out.println("Serialized Message: " + new String(message));
		if(acceptable(received)) {
			deliver(received);
			//this.timeStamp[received.senderId-1]++;
			afterDeliver();
		} else {
			sequence.add(received);
		}
	}
	
	public void afterDeliver() {
		boolean search = true;
		while(search) {
			search = false;
			List<Integer> delivered = new ArrayList<>();
			for(int i=0; i<this.sequence.size(); i++) {
				if(acceptable(sequence.get(i))) {
					Message toDeliver = sequence.get(i);
					deliver(toDeliver);
					//this.timeStamp[toDeliver.senderId-1]++;
					delivered.add(i);
				}
			}
			if(delivered.size() != 0) {
				search = true;
			}
			for(int i=0; i<delivered.size(); i++) {
				sequence.remove(delivered.get(i));
			}
		}
	}
	
	public boolean acceptable(Message message) {
		if((message.timeStamp[message.senderId-1] != this.timeStamp[message.senderId-1] + 1)) {
			return false;
		} else {
			for(int i=0; i<this.timeStamp.length; i++) {
				if(i != message.senderId-1 && message.timeStamp[i] > this.timeStamp[i])
					return false;
			}
		}
		return true;
	}
	 
	public void deliver(Message message) {
		System.out.println("Delivering: \"" + message.messageContent + "\" at system time " + new Date().toString());
		if(message.senderId != this.id)
			this.timeStamp[message.senderId-1]++;
		System.out.println("TimeStamp: " + timeStampSerialize(this.timeStamp));
	}
	
}
