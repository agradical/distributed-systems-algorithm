import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class HS_Algorithm implements LeaderElection {
	
	public static Process[] processes;	
	public static int num_process;
	public List<Thread> threadPool;
	public static boolean leaderElected =  false;

	public static Semaphore phaseCompleted;
	public static Semaphore roundCompleted;

	public HS_Algorithm(int n, int[] ids) {
		num_process = n;
		phaseCompleted = new Semaphore(n);
		roundCompleted = new Semaphore(n);
		
		processes = new Process[n];
		this.threadPool = new ArrayList<Thread>();

		for(int i=0; i<n; i++) {
			Process process = new Process(ids[i%n], i, (i+1)%n, (i+n-1)%n);
			processes[i] = process;
			Thread t = new Thread(process);
			threadPool.add(t);
		}
	}

	public void execute() {		
		for(int i=0; i< num_process; i++) {
			this.threadPool.get(i).start();
		}
	}
	
}
