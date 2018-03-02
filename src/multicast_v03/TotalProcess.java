package multicast_v03;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class TotalProcess extends Process{
	public int idCounter;
	public InetSocketAddress sequencerAddr;
	public PriorityQueue<Message> deliverQueue = new PriorityQueue<>(100, new Comparator<Message>() {
		@Override
		public int compare(Message m1, Message m2) {
			return m1.messageId - m2.messageId;
		}
	});
	
	public TotalProcess(int id, HashMap<Integer, InetSocketAddress> addrMap, int minDelay, int maxDelay, BlockingQueue<String> messageQueue, String sequencerIp, int sequencerPort) throws IOException {
		super(id, addrMap, minDelay, maxDelay, messageQueue);
		this.idCounter = 0;
		this.sequencerAddr = new InetSocketAddress(sequencerIp, sequencerPort);
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
									total_processReceive(socketChannel.socket().getChannel());
//									while(!deliverQueue.isEmpty() && deliverQueue.peek().messageId == this.idCounter + 1) {
//										deliver(deliverQueue.poll());
//										this.idCounter++;
//									}
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
				String parsedMessage[] = messageContent.split(" ", 2);
				if(parsedMessage.length != 2) {
					System.out.println("Illegal formatted message! ");
					continue;
				}
				System.out.println("Sending '" + parsedMessage[1] + "' at time: " + new Date().toString() + " with delay " + delay + " ms.");
				Message message = new Message(this.id, parsedMessage[1]);
				new Timer().schedule(new TimerTask() {
					@Override
					public void run() {
						try {
							//System.out.print("Delay: " + delay + " ms...");
							
							if(parsedMessage[0].equals("msend")) {
								//int destId = Integer.parseInt(parsedMessage[1]);
								unicast_send(sequencerAddr, message);
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
	
	public void total_processReceive(SocketChannel receiveChannel) throws IOException {
		//while(true) {
			ByteBuffer content = ByteBuffer.allocate(100);
			receiveChannel.read(content);
			content.flip();
			byte[] message = new byte[content.remaining()];
			content.get(message);
			Message received = deserialize(message);
			//received.messageId = this.idCounter;
			//this.idCounter++;
			deliverQueue.offer(received);
			System.out.println("Received \"" + received.messageContent + "\" at time " + new Date().toString());
			while(!deliverQueue.isEmpty() && deliverQueue.peek().messageId == this.idCounter + 1) {
				deliver(deliverQueue.poll());
				this.idCounter++;
			}
		//}
	}
	
	public void deliver(Message m) {
		System.out.println("Delivering: \"" + m.messageContent + "\" from process " + m.senderId + "at system time " + new Date().toString());
	}
}
