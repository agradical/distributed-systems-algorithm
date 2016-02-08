
public class ProcessState {
	private boolean canStartRound;
	private Status status;
	private int pid;
	private int phase;

	public ProcessState(int pid) {
		this.canStartRound = false;
		this.status = Status.UNKNOWN;
		this.pid = pid;
		this.phase = 0;
	}
	
	public int getPhase() {
		return phase;
	}

	public void setPhase(int phase) {
		this.phase = phase;
	}

	public int getPid() {
		return pid;
	}

	public boolean isCanStartRound() {
		return (status == Status.UNKNOWN) && canStartRound;
	}

	public void setCanStartRound(boolean canStartRound) {
		this.canStartRound = canStartRound;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}
	
}
