public class Process implements Runnable {
	
	// buffers to place message for transfer to neighbor
	public Message leftInBuffer;
	public Message rightInBuffer;
	public Message leftOutBuffer;
	public Message rightOutBuffer;

	//to keep track of leader announcement
	public int leader_id = 0;
	int announcement_sent_or_recieved = 0;

	//
	public boolean left_ack;
	public boolean right_ack;

	//attributes
	public int id;
	public int index;
	public int left_index;
	public int right_index;
	
	public enum LeaderStatus {
		UNKNOWN, LEADER, NON_LEADER 
	}
	
	public LeaderStatus leaderStatus;
	
	public Process(int id, int index, int left_index, int right_index) {
		this.id = id;
		
		this.index = index;
		this.left_index = left_index;
		this.right_index = right_index;
		
		this.leaderStatus = LeaderStatus.UNKNOWN;
		
	}

	@Override
	public void run() {
		int phase = 0;
		while(true) { 
			try {			
				//clearing left and right buffers
				this.leftOutBuffer = null;
				this.rightOutBuffer = null;
				
				this.left_ack = false;
				this.right_ack = false;
				
				int hops = (int)Math.pow(2, phase);
				
				System.out.println("Process id: "+ this.id +" starts phase: "+phase);
				
				int round = 1;
				while ((round <= 2*hops)) {
					// Semaphore to ensure all threads start round at same time
					HS_Algorithm.roundStarted.acquire();
					Thread.sleep(10);// To ensure all have acquired the semaphore

					//send announcement
					if (round == 1 && this.leaderStatus.equals(LeaderStatus.LEADER)) {
						sendAnnouncement();
						System.out.println("Sending Announcement.....");
						this.announcement_sent_or_recieved = 1;
					}
					//send explore message
					else if (round == 1 && this.leaderStatus.equals(LeaderStatus.UNKNOWN)) {
						sendExplore(hops);
					}
					else if (round == 1 && this.leaderStatus.equals(LeaderStatus.NON_LEADER)) {
						//do nothing
					} 
					
					// if process is not a leader than all it has to do is
					// pass the messages around
					else  {
						
						HS_Algorithm.processes[this.left_index].getMessages(this, "l");
						HS_Algorithm.processes[this.right_index].getMessages(this, "r");
						
						processMessage(leaderStatus);	
					}
					
					// Semaphore to ensure all threads start round at same time
					HS_Algorithm.roundCompleted.acquire();
					
					//Login to ensure that thread doesn't start next phase until all 
					// threads completed the round
					if (HS_Algorithm.roundCompleted.availablePermits() == 0) {
						HS_Algorithm.roundCompleted.release(HS_Algorithm.num_process);
						HS_Algorithm.roundStarted.release(HS_Algorithm.num_process);					
					}
									
					round++;				
					
					// if both ack received current thread is done with the phase
					if(left_ack && right_ack) {
						break;
					}
				}
				// if leader announcement has been received, exit the loop and terminate
				if (this.leader_id != 0 && this.announcement_sent_or_recieved != 0) {
					break;
				}
				phase++;				

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	//Updating status
	public void updateStatus(LeaderStatus leaderStatus, Message message) {
		if(this.leaderStatus.equals(LeaderStatus.UNKNOWN)) {
			if (message.type.equals(Message.MessageType.EXPLORE)) {
				if (message.message == this.id) {
					this.leaderStatus = LeaderStatus.LEADER;
					System.out.println("LEADER------------------------------------------------------: "+ this.id);
					this.leader_id = this.id;
				}
			}
		}
	}
	
	// Getting messages from the neighboring nodes
	public void getMessages(Process p, String direction) {
		if(direction.equals("l")) {
			p.leftInBuffer = this.rightOutBuffer;
			this.rightOutBuffer = null;
		}
		if(direction.equals("r")) {
			p.rightInBuffer = this.leftOutBuffer;
			this.leftOutBuffer = null;
		}
	}
	
	// Process messages fetched from neighbors
	public void processMessage(LeaderStatus leaderStatus) {
		
		if(this.leftInBuffer != null) {
			Message message = this.leftInBuffer;
			// if message received equals to the pid of process
			// set flag to track ack received from left
			if (message.message == this.id) {
				this.leftOutBuffer = null;
				this.rightOutBuffer = null;
				this.left_ack = true;
				
			}
			
			else if (message.message > this.id) {
				this.leaderStatus = LeaderStatus.NON_LEADER;

				// if message id received is Announcement
				// set the leader id  and pass it to neighbor
				if (message.type.equals(Message.MessageType.LEADER)) {
					this.leader_id = message.message;
					this.announcement_sent_or_recieved = 1;
					this.rightOutBuffer = message;
					
				}
				// if the hops left equals 0 return back to the message
				else if(message.hops_left == 0) {
					message.hops_left = message.hops_left - 1;
					message.type = Message.MessageType.ACK;
					this.leftOutBuffer = message;

				} else {
					message.hops_left = message.hops_left - 1;	
					this.rightOutBuffer = message;				
				}
				
			// if message received is greater ignore the message
			} else {
				this.rightOutBuffer = null;
			}
			
			updateStatus(this.leaderStatus, message);
			this.leftInBuffer = null;			
		}

		
		if (this.rightInBuffer != null) {
			Message message = this.rightInBuffer;

			// if message received equals to the pid of process
			// set flag to track ack received from left
			if (message.message == this.id) {
				this.leftOutBuffer = null;
				this.rightOutBuffer = null;
				this.right_ack = true;

			}
			
			else if (message.message > this.id){
				this.leaderStatus = LeaderStatus.NON_LEADER;
				
				// if message id received is Announcement
				// set the leader id  and pass it to neighbor
				if (message.type.equals(Message.MessageType.LEADER)) {
					this.leader_id = message.message;
					this.announcement_sent_or_recieved = 1;
					this.leftOutBuffer = message;
					
				// if the hops left equals 0 return back to the message	
				} else if(message.hops_left == 0) {
					message.type = Message.MessageType.ACK;
					message.hops_left = message.hops_left - 1;
					this.rightOutBuffer = message;
					
				} else {
					message.hops_left = message.hops_left - 1;
					this.leftOutBuffer = message;
				}
			// if message received is greater ignore the message
			} else {
				this.leftOutBuffer = null;
			}
			updateStatus(this.leaderStatus, message);
			this.rightInBuffer = null;
		}
		
	}
	
	public void sendExplore(int hops) {

		//generate message and send to both direction
		Message msgleft = generateMessage(LeaderStatus.UNKNOWN, hops-1);
		Message msgright = generateMessage(LeaderStatus.UNKNOWN, hops-1);

		this.leftOutBuffer = msgleft;
		this.rightOutBuffer = msgright;
	}
	
	public void sendAnnouncement() {
		//generate message and send to only one direction
		Message msgleft = generateMessage(LeaderStatus.LEADER, -1);
		this.leftOutBuffer = msgleft;
	}
	
	public Message generateMessage(LeaderStatus leaderStatus, int hops) {	
		if (leaderStatus.equals(LeaderStatus.UNKNOWN)) {
			Message message = new Message(Message.MessageType.EXPLORE, this.id, hops);
			return message;
		} else {
			Message message = new Message(Message.MessageType.LEADER, this.id, hops);
			return message;
		}
	}
	
}
