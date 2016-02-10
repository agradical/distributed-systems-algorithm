public class Process implements Runnable {
	
	public Message leftInBuffer;
	public Message rightInBuffer;
	public Message leftOutBuffer;
	public Message rightOutBuffer;

	
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
		int max_phase = (int)(Math.log(HS_Algorithm.num_process) / Math.log(2));

		while((phase <= max_phase+1) && !HS_Algorithm.leaderElected) { 
			try {
				HS_Algorithm.phaseStarted.acquire();

				this.leftInBuffer = null;
				this.rightInBuffer = null;
				this.leftOutBuffer = null;
				this.rightOutBuffer = null;
				
				int max_possible_hops = (int)Math.pow(2, phase);
				
				int hops =  (max_possible_hops > HS_Algorithm.num_process)
						  ? HS_Algorithm.num_process : max_possible_hops ;
				
				int round = 1;
				while ((round <= 2*hops) && !HS_Algorithm.leaderElected) {
					HS_Algorithm.roundStarted.acquire();
					Thread.sleep(100);// To ensure all have acquired the semaphore

//					System.out.println("phase: " + phase + " round: "+round + " id: "+ this.id);

					if (round == 1 && this.leaderStatus.equals(LeaderStatus.UNKNOWN)) {
						sendExplore(hops);
					} else if (round == 1 && this.leaderStatus.equals(LeaderStatus.NON_LEADER)) {
						//do nothing
					} else  {
						
						HS_Algorithm.processes[this.left_index].getMessages(this, "l");
						HS_Algorithm.processes[this.right_index].getMessages(this, "r");
												
						processMessage(leaderStatus);					
					}
					
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
		
	public void updateStatus(LeaderStatus leaderStatus, Message message) {
		if(this.leaderStatus.equals(LeaderStatus.UNKNOWN)) {
			if (message.type.equals(Message.MessageType.EXPLORE)) {
				if (message.message == this.id) {
					this.leaderStatus = LeaderStatus.LEADER;
					System.out.println("LEADER------------------------------------------------------: "+ this.id);
					HS_Algorithm.leaderElected = true;
				}
			}
		}
	}
	
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
	
	public void processMessage(LeaderStatus leaderStatus) {
		
		if(this.leftInBuffer != null) {
			Message message = this.leftInBuffer;
			if (message.message == this.id) {
				this.leftOutBuffer = null;
				this.rightOutBuffer = null;
			} else if (message.message < this.id) {
				this.leaderStatus = LeaderStatus.NON_LEADER;
//				System.out.println(this.id + "---->"+this.leaderStatus.toString());
				if(message.hops_left == 0) {
					message.hops_left = message.hops_left - 1;
					message.type = Message.MessageType.ACK;
					this.leftOutBuffer = message;

//					System.out.println("message:" + message.message + "--------------> ACK");
//					System.out.println(this.id +"->"
//							+ HS_Algorithm.processes[this.left_index].id 
//							+ ": "+message.message
//							+ " Hop "+ message.hops_left
//							+ "  "+ message.type.toString());
//					
				} else {
					message.hops_left = message.hops_left - 1;	
					this.rightOutBuffer = message;

//					System.out.println(this.id +"->"
//							+ HS_Algorithm.processes[this.right_index].id 
//							+ ": "+message.message
//							+ " Hop "+ message.hops_left
//							+ "  "+ message.type.toString());
//					
				}
			} else {
				this.rightOutBuffer = null;
			}
			updateStatus(this.leaderStatus, message);
			this.leftInBuffer = null;			
		}

		if (this.rightInBuffer != null) {
			Message message = this.rightInBuffer;
			if (message.message == this.id) {
				this.leftOutBuffer = null;
				this.rightOutBuffer = null;
			} else if (message.message < this.id){
				this.leaderStatus = LeaderStatus.NON_LEADER;
//				System.out.println(this.id + "---->"+this.leaderStatus.toString());

				if(message.hops_left == 0) {
					message.type = Message.MessageType.ACK;
					message.hops_left = message.hops_left - 1;
					this.rightOutBuffer = message;

//					System.out.println("message:" + message.message + "--------------> ACK");				
//					System.out.println(this.id +"->"
//							+ HS_Algorithm.processes[this.right_index].id 
//							+ ": "+message.message
//							+ " Hop "+ message.hops_left
//							+ "  "+ message.type.toString());
					
				} else {
					message.hops_left = message.hops_left - 1;
					this.leftOutBuffer = message;

//					System.out.println(this.id +"->"
//							+ HS_Algorithm.processes[this.left_index].id 
//							+ ": "+message.message
//							+ " Hop "+ message.hops_left
//							+ "  "+ message.type.toString());
					
				}
				
			} else {
				this.leftOutBuffer = null;
			}
			updateStatus(this.leaderStatus, message);
			this.rightInBuffer = null;
		}
		
	}
	
	public void sendExplore(int hops) {

		Message msgleft = generateMessage(LeaderStatus.UNKNOWN, hops-1);
		Message msgright = generateMessage(LeaderStatus.UNKNOWN, hops-1);

		this.leftOutBuffer = msgleft;
		this.rightOutBuffer = msgright;
		
//		System.out.println(this.id +"->"
//				+ HS_Algorithm.processes[this.left_index].id 
//				+ ": "+msgleft.message
//				+ " Hop "+ msgleft.hops_left
//				+ "  "+ msgleft.type.toString());
//		System.out.println(this.id +"->"
//				+ HS_Algorithm.processes[this.right_index].id 
//				+ ": "+msgright.message
//				+ " Hop "+ msgright.hops_left
//				+ "  "+ msgright.type.toString());
		
	}
	public Message generateMessage(LeaderStatus leaderStatus, int hops) {	
		Message message = new Message(Message.MessageType.EXPLORE, this.id, hops);

		return message;
	}
	
}
