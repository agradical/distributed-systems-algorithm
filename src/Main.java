import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Main {

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
		
		LeaderElection algo = new HS_Algorithm(n, ids);
		algo.execute();
		
	}
}
