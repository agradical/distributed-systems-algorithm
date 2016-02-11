import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class HS_Algorithm implements LeaderElection {
	
	public static Process[] processes;	
	public static int num_process;
	public List<Thread> threadPool;

	
	public static Semaphore roundCompleted;	
	public static Semaphore roundStarted;
	
	
	public HS_Algorithm(int n, int[] ids) {
		num_process = n;
		
		// semaphores to be used to track the round completion
		// in actual distributed environment, these will be basically the tokens
		// used to communicate.
		roundCompleted = new Semaphore(n);
		roundStarted = new Semaphore(n);
		
		processes = new Process[n];
		this.threadPool = new ArrayList<Thread>();

		//initializing all processes and set info about their neighbors
		for(int i=0; i<n; i++) {
			Process process = new Process(ids[i%n], i, (i+1)%n, (i+n-1)%n);
			processes[i] = process;
			Thread t = new Thread(process);
			threadPool.add(t);
		}
	}

	public void execute() {		
		//Starting all processes
		System.out.println("Electing Leader with Max ID");
		for(int i=0; i< num_process; i++) {
			this.threadPool.get(i).start();
		}
		
		//waiting for all threads to finish and join
		for(int i=0; i< num_process; i++) {
			try {
				this.threadPool.get(i).join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("TERMINATED....");
	}
	
}
