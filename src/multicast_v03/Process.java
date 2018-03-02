package multicast_v03;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.net.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class Process implements Runnable{
	public int id;
	public InetSocketAddress addr;
	public HashMap<Integer, InetSocketAddress> addrMap;
	public int minDelay;
	public int maxDelay;
	public ServerSocketChannel serverSocketChannel;
	public BlockingQueue<String> messageQueue;
	
	public Process(int id, HashMap<Integer, InetSocketAddress> addrMap, int minDelay, int maxDelay, BlockingQueue<String> messageQueue) throws IOException {
		this.id = id;
		this.addrMap = addrMap;
		this.minDelay = minDelay;
		this.maxDelay = maxDelay;
		this.addr = this.addrMap.get(id);
		this.serverSocketChannel = ServerSocketChannel.open();
		this.serverSocketChannel.bind(this.addr);
		this.messageQueue = messageQueue;
	}
	
	public Process() {
		this.id = -1;
		this.messageQueue = new LinkedBlockingDeque<String>();
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
						System.out.println("Accepting from: " + socketChannel.socket().getRemoteSocketAddress());
						new Thread(new Runnable() {
							@Override
							public void run() {
								try { 
									unicast_receive(socketChannel.socket().getChannel());
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
				long delay = (long)(new Random().nextDouble() * (maxDelay - minDelay) + minDelay);
				String parsedMessage[] = messageContent.split(" ", 3);
				if(parsedMessage.length != 3) {
					System.out.println("Illegal formatted message! ");
					continue;
				}
				System.out.println("Sending \"" + parsedMessage[2] + "\" at time: " + new Date().toString() + " with delay " + delay + " ms.");
				Message message = new Message(this.id, parsedMessage[2]);
				new Timer().schedule(new TimerTask() {
					@Override
					public void run() {
						try {
							//System.out.print("Delay: " + delay + " ms...");
							
							if(parsedMessage[0].equals("send")) {
								int destId = Integer.parseInt(parsedMessage[1]);
								unicast_send(destId, message);
							} else {
								System.out.println("Illegal Command");
							}
						} catch(IOException ioe) {
							ioe.printStackTrace();
						}
					}
				}, delay);
			} catch(InterruptedException itrpte) {
				itrpte.printStackTrace();
			}
		}
	}
	
	public void unicast_send(int destId, Message message) throws IOException {
		SocketChannel sendChannel = SocketChannel.open();
		sendChannel.bind(new InetSocketAddress(this.addr.getHostName(), 0));
		if(sendChannel.connect(addrMap.get(destId))) {
			//System.out.println("Connected to: " + sendChannel.getRemoteAddress());
			String serialized = serialize(message);
			ByteBuffer size = ByteBuffer.allocate(4);
			size.putInt(serialized.length());
			sendChannel.write(size);
			ByteBuffer content = ByteBuffer.allocate(serialized.length());
			sendChannel.write(content.wrap(serialized.getBytes()));
			sendChannel.finishConnect();
			//System.out.println("Message: \"" + message.messageContent + "\", TimeStamp: " + timeStampSerialize(message.timeStamp) + ", system time: " + new Date().toString());
			//sendChannel.close();
			//System.out.println("Message Sent at time: " + new Date().toString());
		} else {
			sendChannel.finishConnect();
		}
		//sendChannel.connect(addrMap.get(destId));
		
	}
	
	public void unicast_send(InetSocketAddress destAddr, Message message) throws IOException {
		SocketChannel sendChannel = SocketChannel.open();
		sendChannel.bind(new InetSocketAddress(this.addr.getHostName(), 0));
		if(sendChannel.connect(destAddr)){
			//System.out.println("Connected to: " + sendChannel.getRemoteAddress());
			String serialized = serialize(message);
			ByteBuffer size = ByteBuffer.allocate(4);
			size.putInt(serialized.length());
			sendChannel.write(size);
			ByteBuffer content = ByteBuffer.allocate(serialized.length());
			sendChannel.write(content.wrap(serialized.getBytes()));
			sendChannel.finishConnect();
			//System.out.println("Message: \"" + message.messageContent + "\", TimeStamp: " + timeStampSerialize(message.timeStamp) + ", system time: " + new Date().toString());
			//System.out.println("Message Sent at time: " + new Date().toString());
		} else {
			sendChannel.finishConnect();
		}
	}
	
//	public void unicast_send(int destId, byte[] message) throws IOException {
//		SocketChannel sendChannel = SocketChannel.open();
//		sendChannel.bind(new InetSocketAddress(this.addr.getHostName(), 0));
//		sendChannel.connect(addrMap.get(destId));
//		System.out.println("Connected to: " + sendChannel.getRemoteAddress());
//		int length = message.length;
//		ByteBuffer size = ByteBuffer.allocate(4);
//		size.putInt(length);
//		sendChannel.write(size);
//		//byteBuffer.clear();
//		ByteBuffer content = ByteBuffer.allocate(length);
//		sendChannel.write(content.wrap(message));
//		//byteBuffer.clear();
//		sendChannel.finishConnect();
//		System.out.println("Message Sent at time: " + new Date().toString());
//	}
	
	public void unicast_receive(SocketChannel receiveChannel) throws IOException {
		//while(true) {
//			ByteBuffer size = ByteBuffer.allocate(4);
//			receiveChannel.read(size);
//			size.flip();
//			int length = Integer.parseInt(size.toString());
//			System.out.println("Receiving " + length + " bytes of message");
//			ByteBuffer content = ByteBuffer.allocate(length);
//			System.out.println("Buffer Allocated");
//			receiveChannel.read(content);
//			System.out.println("Message Read");
//			content.flip();
//			byte[] messageReceived = new byte[content.remaining()];
//			content.get(messageReceived);
//			System.out.println("Received message: ' " + new String(messageReceived) + " ' at time: " + new Date().toString());
			ByteBuffer content = ByteBuffer.allocate(100);
			receiveChannel.read(content);
			content.flip();
			byte[] message = new byte[content.remaining()];
			content.get(message);
			//System.out.println(new String(message));
			Message received = deserialize(message);
			//System.out.println("Received message: ' " + received + " ' at time: " + new Date().toString());
			System.out.println("Received \"" + received.messageContent + "\" from process " + received.senderId + ", system time is " + new Date().toString());
		//}
	}
	
	public String serialize(Message m) {
		String str = Integer.toString(m.senderId);
		str += ',';
		str += Integer.toString(m.messageId);
		str += ',';
		str += timeStampSerialize(m.timeStamp);
		str += ',';
		str += m.messageContent;
		//System.out.println("Serialized: " + str);
		return str;
	}
	
	public Message deserialize(byte[] m) {
		String str = new String(m);
		String[] parsed = str.split(",", 4);
		int[] timeStamp = timeStampDeserialize(parsed[2]);
		return new Message(Integer.parseInt(parsed[0]), Integer.parseInt(parsed[1]), timeStamp, parsed[3]);
	}
	
	public String timeStampSerialize(int[] t) {
		String str = new String();
		for(int i=0; i<t.length; i++) {
			str += Integer.toString(t[i]);
			str += " ";
		}
		return str;
	}
	
	public int[] timeStampDeserialize(String str) {
		String[] nums = str.split(" ");
		int[] t = new int[nums.length];
		for(int i=0; i<t.length; i++) {
			t[i] = Integer.parseInt(nums[i]);
		}
		return t;
	}
}
