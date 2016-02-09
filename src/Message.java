
public class Message {
	public static enum MessageType {
		ACK, EXPLORE, LEADER
	}
	
	public MessageType type;
	public int message;
	public int hops_left;
	
	public Message(MessageType type, int m, int hops) {
		this.type = type;
		this.message = m;
		this.hops_left = hops;
	}
}
