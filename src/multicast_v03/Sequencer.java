package multicast_v03;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

public class Sequencer extends Process {
	public InetSocketAddress seqAddr;
	public BlockingQueue<Message> sequence;
	public int idCounter;
//	private PriorityQueue<Message> sequence = new PriorityQueue<>(100, new Comparator<Message>() {
//		@Override
//		public int compare(Message m1, Message m2) {
//			return m1.messageId - m2.messageId;
//		}
//	});
	
	public Sequencer(int id, HashMap<Integer, InetSocketAddress> addrMap, int minDelay, int maxDelay, BlockingQueue<String> messageQueue, String sequencerIp, int sequencerPort) throws IOException {
		//super(id, addrMap, minDelay, maxDelay, messageQueue);
		this.id = 0;
		//this.addr = null;
		this.addrMap = addrMap;
		this.minDelay = minDelay;
		this.maxDelay = maxDelay;
		this.messageQueue = messageQueue;
		this.seqAddr = new InetSocketAddress(sequencerIp, sequencerPort);
		this.addr = this.seqAddr;
		this.serverSocketChannel = ServerSocketChannel.open();
		this.serverSocketChannel.bind(seqAddr);
		this.sequence = new LinkedBlockingDeque<Message>(100);
		this.idCounter = 1;
	}
	
	@Override
	public void run() {
		System.out.println("Sequencer is up...");
		System.out.println("Sequencer Address: " + serverSocketChannel);
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
									//unicast_receive(socketChannel.socket().getChannel());
									total_sequencerReceive(socketChannel.socket().getChannel());
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
				//String message = (String) (messageQueue.poll(1, TimeUnit.DAYS));
				Message toSend = sequence.poll(1, TimeUnit.DAYS);
				for(Map.Entry<Integer, InetSocketAddress> entry : addrMap.entrySet()) {
					long delay = (long)(new Random().nextDouble() * (maxDelay - minDelay) + minDelay);
					System.out.println("Sending \"" + toSend.messageContent + "\" at time: " + new Date().toString() + " with delay " + delay + " ms.");
					new Timer().schedule(new TimerTask() {
						@Override
						public void run() {
							try {
								//System.out.print("Delay: " + delay + " ms...");
//								String parsedMessage[] = message.split(" ", 3);
//								if(parsedMessage.length != 3) {
//									System.out.println("Illegal formatted message! ");
//									return;
//								}
//								if(parsedMessage[0].equals("send")) {
//									int destId = Integer.parseInt(parsedMessage[1]);
//									unicast_send(destId, parsedMessage[2].getBytes());
//								} else {
//									System.out.println("Illegal Command");
//								}
								unicast_send(entry.getKey(), toSend);
							} catch(IOException ioe) {
								ioe.printStackTrace();
							}
						}
					}, delay);
				}
			} catch(InterruptedException itrpte) {
				itrpte.printStackTrace();
			}
		}
	}
	
//	public void total_sendBack(Message toSend) throws IOException {
//		//Message toSend = sequence.poll();
//		String serialized = serialize(toSend);
//		for(Map.Entry<Integer, InetSocketAddress> entry : addrMap.entrySet()) {
//			unicast_send((int)(entry.getKey()), toSend);
//		}
//	}
	
	public void total_sequencerReceive(SocketChannel receiveChannel) throws IOException {
		while(true) {
			ByteBuffer content = ByteBuffer.allocate(100);
			receiveChannel.read(content);
			content.flip();
			byte[] message = new byte[content.remaining()];
			content.get(message);
			Message received = deserialize(message);
			received.messageId = this.idCounter;
			this.idCounter++;
			sequence.offer(received);
		}
	}
	
}
