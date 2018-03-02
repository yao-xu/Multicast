package multicast_v03;

import java.io.Serializable;

public class Message implements Serializable {
	int messageId;
	int senderId;
	int[] timeStamp;
	String messageContent;
	
	public Message(int senderId, int messageId, int[] timeStamp, String messageContent) {
		this.messageId = messageId;
		this.senderId = senderId;
		this.messageContent = messageContent;
		this.timeStamp = timeStamp;
	}
	
	public Message(int senderId, int messageId, String messageContent) {
		this.messageId = messageId;
		this.senderId = senderId;
		this.messageContent = messageContent;
		this.timeStamp = new int[1];
	}
	
	public Message(int senderId, String messageContent) {
		this.senderId = senderId;
		this.messageContent = messageContent;
		this.messageId = -1;
		this.timeStamp = new int[1];
	}
	
	public Message(int senderId, String messageContent, int numProcess) {
		this.senderId = senderId;
		this.messageContent = messageContent;
		this.messageId = -1;
		this.timeStamp = new int[numProcess];
	}
	
	public Message(int senderId, String messageContent, int[] timeStamp) {
		this.senderId = senderId;
		this.messageContent = messageContent;
		this.messageId = -1;
		this.timeStamp = timeStamp;
	}
}
