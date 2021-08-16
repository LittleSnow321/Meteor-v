package predictive;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;

public class ReadFile {
	
	public static String trace_path = "/home/xiaoxue/velodrome-output/";
	public static String loc = "/home/xiaoxue/Desktop/";
	public static ArrayList<String> bench1 = new ArrayList<String>(Arrays.asList("eclipse6", "sunflow9"));
	//public static ArrayList<String> bench2 = new ArrayList<String>(Arrays.asList("micro2", "micro3", "micro4", "micro5"));
//	public static ArrayList<String> bench2 = new ArrayList<String>(Arrays.asList("hsqldb6", "xalan6", "lusearch9-fixed", "xalan9"));
	public static ArrayList<String> bench2 = new ArrayList<String>(Arrays.asList("hsqldb6", "lusearch6", "xalan6", "jython9", "luindex9", "lusearch9-fixed", "pmd9", "xalan9"));

	public static final int BUFFER_SIZE = 19999992; 
	public static boolean flag = false;
	
	public static void main(String[] args) throws IOException
	{

		String trace = args[0];
		System.out.println("detection started");
		
		
		
		
		for(int i = 0; i < bench2.size(); i++) {
			String bench_trace = loc + "trace_" + trace + trace_path + bench2.get(i) + "1.log";
			//String bench_trace = trace_path +"/" +  bench2.get(i) + "/micro1.log";
			File f1 = new File(bench_trace);
			FileInputStream fileInput1 = new FileInputStream(f1);
			
			String atomic_path = loc + "atomic_specification/PT_iteration/" + bench2.get(i) + "_" + trace;

			
			TransactionalHBGraph transGraph = new TransactionalHBGraph();
			transGraph.read_AtomicList(atomic_path);	
			
			
			PrintStream ps = new PrintStream(new FileOutputStream("PT_iteration_"+bench2.get(i)+trace)); 
			System.setOut(ps);
			byte[] readBytes1 = new byte[BUFFER_SIZE];
			int offset = 0;
			int[] test = new int[3];
		

			while((offset = fileInput1.read(readBytes1)) != -1)
			{
				int index = 0;
				for(int read = 0; read < offset; read += 4)
				{
					test[index] = ((readBytes1[read] & 0xff) << 24) |
							((readBytes1[read+1] & 0xff) << 16) |
							((readBytes1[read+2] & 0xff) << 8) |
							(readBytes1[read +3] & 0xff);
					index++;

					if (index == 3)
					{


						if (test[1] == 20)
						{
							transGraph.startTransaction(test[0], test[2]);
						}
						else if(test[1] == 21)
						{
							transGraph.endTransaction(test[0], test[2]);
						}
						else if(test[1] == 22)
						{
							transGraph.processRead(test[0], test[2]);
						}
						else if(test[1] == 23)
						{
							transGraph.processWrite(test[0], test[2]);
						}
						else if(test[1] == 24)
						{
							transGraph.processAcquire(test[0], test[2]);
						}
						else if(test[1] == 25)
						{
							transGraph.processRelease(test[0], test[2]);
						}

						index = 0;
					}
				}
			}
			
			fileInput1.close();


		}
		
		
		for(int i = 0; i < bench1.size(); i++) {
			String bench_trace1 = loc + "trace_" + trace + trace_path + bench1.get(i) + "1.log";
			String bench_trace2 = loc + "trace_" + trace + trace_path + bench1.get(i) + "2.log";
			
			String atomic_path = loc + "atomic_specification/PT_iteration/" + bench1.get(i) + "_" + trace;
			
			File f1 = new File(bench_trace1);
			FileInputStream fileInput1 = new FileInputStream(f1);
			File f2 = new File(bench_trace2);
			FileInputStream fileInput2 = new FileInputStream(f2);
			TransactionalHBGraph transGraph = new TransactionalHBGraph();
			transGraph.read_AtomicList(atomic_path);	
			
			
			PrintStream ps = new PrintStream(new FileOutputStream("PT_iteration_"+bench1.get(i)+trace)); //PT_time_memory_
			System.setOut(ps);
			byte[] readBytes1 = new byte[BUFFER_SIZE];
			byte[] readBytes2 = new byte[BUFFER_SIZE];
			int offset = 0;
			int[] test = new int[3];
			
				
			while((offset = fileInput1.read(readBytes1)) != -1)
			{
				int index = 0;
				for(int read = 0; read < offset; read += 4)
				{
					test[index] = ((readBytes1[read] & 0xff) << 24) |
							((readBytes1[read+1] & 0xff) << 16) |
							((readBytes1[read+2] & 0xff) << 8) |
							(readBytes1[read +3] & 0xff);
					index++;

					if (index == 3)
					{

						if (test[1] == 20)
						{
							transGraph.startTransaction(test[0], test[2]);
						}
						else if(test[1] == 21)
						{
							transGraph.endTransaction(test[0], test[2]);
						}
						else if(test[1] == 22)
						{
							transGraph.processRead(test[0], test[2]);
						}
						else if(test[1] == 23)
						{
							transGraph.processWrite(test[0], test[2]);
						}
						else if(test[1] == 24)
						{
							transGraph.processAcquire(test[0], test[2]);
						}
						else if(test[1] == 25)
						{
							transGraph.processRelease(test[0], test[2]);
						}

						index = 0;
					}
				}
			}

			while((offset = fileInput2.read(readBytes2)) != -1)
			{
				int index = 0;
				for(int read = 0; read < offset; read += 4)
				{
					test[index] = ((readBytes2[read] & 0xff) << 24) |
							((readBytes2[read+1] & 0xff) << 16) |
							((readBytes2[read+2] & 0xff) << 8) |
							(readBytes2[read +3] & 0xff);
					index++;

					if (index == 3)
					{

						if (test[1] == 20)
						{
							transGraph.startTransaction(test[0], test[2]);
						}
						else if(test[1] == 21)
						{
							transGraph.endTransaction(test[0], test[2]);
						}
						else if(test[1] == 22)
						{
							transGraph.processRead(test[0], test[2]);
						}
						else if(test[1] == 23)
						{
							transGraph.processWrite(test[0], test[2]);
						}
						else if(test[1] == 24)
						{
							transGraph.processAcquire(test[0], test[2]);
						}
						else if(test[1] == 25)
						{
							transGraph.processRelease(test[0], test[2]);
						}

						index = 0;
					}
				}
			}
			
			fileInput1.close();
			fileInput2.close();
			
		}


		
	}
	
	
	

}
