import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Main {
	public static ProcessState[] processStates;
	
	public static void main(String[] args) {
		
		String filename = "input.dat";
		if(args.length != 0) {
			filename = args[0];
		}
		String n_str = "0";
		String ids_str = "0";
		
		try {
			File file = new File(filename);		
			BufferedReader br = new BufferedReader(new FileReader(file));
			n_str = br.readLine();
			ids_str = br.readLine();
			br.close();
		} catch(FileNotFoundException f) {
			System.out.print(f.getMessage());
			return;
		} catch(IOException i) {
			System.out.print(i.getMessage());
			return;
		}
		
		//parse read strings to integers 
		Integer n = Integer.parseInt(n_str);
		String[] id_array_str = ids_str.split(" ");
		int[] ids = new int[n];
		for (int i=0; i< n; i++) {
			ids[i] = Integer.parseInt(id_array_str[i]);
		}

		// start n threads
		Runnable[] processes = new HS_Algorithm[n];
		for (int i = 0; i < processes.length; i++) {
			processes[i] = new HS_Algorithm(ids[i]);
		}
		Thread[] threads = new Thread[n];
		for (int i = 0; i < threads.length; i++) {
			threads[i] = new Thread(processes[i]);
			threads[i].start();
		}

		while (true) {
			// Perform termination checks
			boolean isAnyThreadRunning = false;
			for (Thread thread : threads) {
				if (thread.isAlive()) {
					isAnyThreadRunning = true;
					break;
				}
			}
			// finish computation if all threads have terminated
			if (!isAnyThreadRunning)
				break;
			
			// Ask processes to start their rounds
			System.out.println("Starting round");
			for (Runnable p : processes) {
				HS_Algorithm proc = (HS_Algorithm)p;
				proc.getP_state().setCanStartRound(true);
			}
			
			// Wait for all processes to complete the round
			boolean isAnyRoundActive;
			while (true) {
				isAnyRoundActive = false;
				for (Runnable p : processes) {
					HS_Algorithm proc = (HS_Algorithm)p;
					if (proc.getP_state().isCanStartRound()) {
						isAnyRoundActive = true;
						break;
					}
				}
				// marks the end of corresponding round in all processes
				if (!isAnyRoundActive)
					break;
			}

			System.out.println("Round completed");
		}
	}
}
