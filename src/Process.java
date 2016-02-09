public class Process implements Runnable {
	
	public Message leftInBuffer;
	public Message rightInBuffer;

	
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
		// TODO Auto-generated method stub
		int phase = 0;
		int max_phase = (int)(Math.log(HS_Algorithm.num_process) / Math.log(2));

		while((phase <= max_phase) && !HS_Algorithm.leaderElected) { 
			try {
				HS_Algorithm.phaseStarted.acquire();
				this.leftInBuffer = null;
				this.rightInBuffer = null;
				Thread.sleep(100); // To ensure all have acquired the semaphore
				
				int hops = (int)Math.pow(2, phase);
				
				Message message = generateMessage(leaderStatus, hops-1);
				int round = 1; 
				while ((round <= 2*hops) && !HS_Algorithm.leaderElected) {
					HS_Algorithm.roundStarted.acquire();
					Thread.sleep(100);
					
					if (round == 1 && leaderStatus.equals(LeaderStatus.UNKNOWN)) {
						sendExplore(message);
					} else {
						passMessage(leaderStatus);
					}
					
					System.out.println("phase: " + phase + " round: "+round + " id: "+ this.id);
					
					HS_Algorithm.roundCompleted.acquire();
					if (HS_Algorithm.roundCompleted.availablePermits() == 0) {
						HS_Algorithm.roundCompleted.release(HS_Algorithm.num_process);
						HS_Algorithm.roundStarted.release(HS_Algorithm.num_process);
					}
					round++;
				}

				HS_Algorithm.phaseCompleted.acquire();
				if(HS_Algorithm.phaseCompleted.availablePermits() == 0) {
					HS_Algorithm.phaseCompleted.release(HS_Algorithm.num_process);
					HS_Algorithm.phaseStarted.release(HS_Algorithm.num_process);
				}
				phase++;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
	public void putMessageInBuffer(Message message, String direction) {
		if(direction.equals("l")) {
			if (leftInBuffer != null) {
				if (leftInBuffer.message < message.message) {

				} else if (leftInBuffer.message > message.message) {
					leftInBuffer = message;
				}
			} else {
				leftInBuffer = message;
			}
		} else if (direction.equals("r")) {
			if (rightInBuffer != null) {
				if (rightInBuffer.message < message.message) {
				} else if (rightInBuffer.message > message.message) {
					rightInBuffer = message;
				}
			} else {
				rightInBuffer = message;
			}
		}
	}
	
	
	public void updateStatus(LeaderStatus leaderStatus, Message message) {
		if(leaderStatus.equals(LeaderStatus.UNKNOWN)) {
			if (message.type.equals(Message.MessageType.EXPLORE)) {
				if (message.message == this.id) {
					this.leaderStatus = LeaderStatus.LEADER;
					System.out.println("LEADER: "+ this.id);
					HS_Algorithm.leaderElected = true;
				}
				else if(message.message < this.id) {
					leaderStatus = LeaderStatus.NON_LEADER;
				}
			}
		}
	}
	public void passMessage(LeaderStatus leaderStatus) {
		
		if(leftInBuffer != null) {
			Message message = leftInBuffer;
			//System.out.println(message.hops_left + " : " + message.message +" : "+ this.id+"l");
			updateStatus(leaderStatus, message);
			if(message.hops_left == 0) {
				message.hops_left = message.hops_left - 1;
				message.type = Message.MessageType.ACK;				
				HS_Algorithm.processes[this.left_index].putMessageInBuffer(message, "r");;	
			
				System.out.println(this.id +"->"
				+ HS_Algorithm.processes[this.left_index].id 
				+ ": "+message.message
				+ " ACK "
				+ " Hop "+ message.hops_left);
				
			} else {
				if (this.id == message.message) {	
					System.out.println(this.id +"=="+ this.id);

				} else {
					message.hops_left = message.hops_left - 1;
					
					HS_Algorithm.processes[this.right_index].putMessageInBuffer(message, "l");;
					
					System.out.println(this.id +"->"
							+ HS_Algorithm.processes[this.right_index].id 
							+ ": "+message.message
							+ " ACK- "
							+ " Hop "+ message.hops_left);
				}
			}
			if (message.equals(leftInBuffer)) {
				leftInBuffer = null;
			}
		}
		
		if (rightInBuffer != null) {
			Message message = rightInBuffer;
			//System.out.println(message.hops_left + " : " + message.message +" : "+ this.id+"r");
			updateStatus(leaderStatus, message);
			
			if(message.hops_left == 0) {
				message.type = Message.MessageType.ACK;
				message.hops_left = message.hops_left - 1;

				HS_Algorithm.processes[this.right_index].putMessageInBuffer(message, "l");

				System.out.println(this.id +"->"
						+ HS_Algorithm.processes[this.right_index].id 
						+ ": "+message.message
						+ " ACK-- "
						+ " Hop "+ message.hops_left);
			} else {
				if (this.id == message.message) {
					System.out.println(this.id +"=="+ this.id);

				} else {
					message.hops_left = message.hops_left - 1;
					HS_Algorithm.processes[this.left_index].putMessageInBuffer(message, "r");;

					System.out.println(this.id +"->"
							+ HS_Algorithm.processes[this.left_index].id 
							+ ": "+message.message
							+ " ACK--- "
							+ " Hop "+ message.hops_left);
				}
			}
			if (message.equals(rightInBuffer)) {
				rightInBuffer = null;
			}

		}
	}
	
	public void sendExplore(Message message) {
		HS_Algorithm.processes[this.left_index].putMessageInBuffer(message, "r");
		HS_Algorithm.processes[this.right_index].putMessageInBuffer(message, "l");
	}
	public Message generateMessage(LeaderStatus leaderStatus, int hops) {	
		Message message = new Message(Message.MessageType.EXPLORE, this.id, hops);

		return message;
	}
	
	
}
