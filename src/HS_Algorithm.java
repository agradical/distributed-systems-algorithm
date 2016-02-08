
public class HS_Algorithm implements Runnable {
	ProcessState p_state;

	public HS_Algorithm(int pid) {
		this.p_state = new ProcessState(pid);
	}

	public ProcessState getP_state() {
		return p_state;
	}

	@Override
	public void run() {
		while (true) {
			System.out.println(p_state.getPid() + ": Waiting for the round to start");
			// wait for confirmation from master
			while (!p_state.isCanStartRound()) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			System.out.println(p_state.getPid() + ": Round started");

			// TODO: perform process's current round tasks

			System.out.println(p_state.getPid() + ": Round ended");
			// notify master that current round has ended
			p_state.setCanStartRound(false);
		}
	}

}
