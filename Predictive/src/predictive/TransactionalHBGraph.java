package predictive;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;


import predictive.RVMThread;
import predictive.LockData;
import predictive.MemoryData;
import predictive.VC;

public class TransactionalHBGraph {
	public static int OCTET_FINALIZER_THREAD_ID = 0 + VC.SPECIAL_ELEMENTS;
	public static int DACAPO_DRIVER_THREAD_OCTET_ID = 1 + VC.SPECIAL_ELEMENTS;
	public static int TRANSCNT = 0;
	
	public ArrayList<RVMThread> ThreadMap;
	public Map<Integer, MemoryData> MemoryMap;
	public Map<Integer, LockData> LockMap;
	public ArrayList<Integer> AtomicList;
	public ArrayList<Integer> DetectList;
	public long readOp;
	public long writeOp;
	public long acqOp;
	public long relOp;
	public long transOp;
	
	public Map<KeyPair, VC> LockVarReadVCMap; //LR(m,x), the join of all HB time of all rel(m) events (seen so far) whose critical sections contain a r(x) event
	public Map<KeyPair, VC> LockVarWriteVCMap; //LW(m,x), the join of all HB time of all rel(m) events (seen so far) whose critical sections contain a w(x) event
	
	public Map<Integer, Integer> targetList;
	
	TransactionalHBGraph()
	{
		this.ThreadMap = new ArrayList<RVMThread>();
		for(int id = VC.SPECIAL_ELEMENTS; id < VC.MAX_THREADS + 1; id++) //initialize the threadMap and create a RVMThread object for each thread
		{
			ThreadMap.add(new RVMThread(id));
		}
		this.MemoryMap = new HashMap<Integer, MemoryData>();
		this.LockMap = new HashMap<Integer, LockData>();
		this.AtomicList = new ArrayList<Integer>();
		this.DetectList = new ArrayList<Integer>();
		this.LockVarReadVCMap = new HashMap<KeyPair, VC>();
		this.LockVarWriteVCMap = new HashMap<KeyPair, VC>();
		this.readOp = 0;
		this.writeOp = 0;
		this.acqOp = 0;
		this.relOp = 0;
		this.transOp = 0;
		
		this.targetList = new HashMap<Integer, Integer>();
	}
	
	public void read_AtomicList(String readFile) throws NumberFormatException, IOException {
		// TODO Auto-generated method stub
		File f = new File(readFile);
		FileInputStream fileInput = new FileInputStream(f);
		BufferedReader in = new BufferedReader(new InputStreamReader(fileInput));
		
		String str = null;
		int site = 0;
		while((str = in.readLine()) != null)
		{
			str = str.trim();
			site = Integer.parseInt(str);
			this.AtomicList.add(site);
		}
	
		fileInput.close();
		in.close();
		
		
	}
	
	
	public void read_target(String readFile) throws NumberFormatException, IOException {
		// TODO Auto-generated method stub
		File f = new File(readFile);
		FileInputStream fileInput = new FileInputStream(f);
		BufferedReader in = new BufferedReader(new InputStreamReader(fileInput));
		
		String str = null;
		int site = 0;
		while((str = in.readLine()) != null)
		{
			str = str.trim();
			site = Integer.parseInt(str);
			this.AtomicList.add(site);
			
			if(!this.targetList.containsKey(site))
			{
				this.targetList.put(site, 0);
			}
		}
	
		fileInput.close();
		in.close();
		
		
	}
	
	public void update_AtomicList() {
		// TODO Auto-generated method stub
		for(int i = 0; i < this.DetectList.size(); i++)
		{
			if (!this.AtomicList.contains(this.DetectList.get(i)))
			{
				this.AtomicList.add(this.DetectList.get(i));
			}
		}
		
	}

	public int get_atomic() {
		// TODO Auto-generated method stub
		return this.AtomicList.size();
	}

	public void initData() {
		// TODO Auto-generated method stub
		this.ThreadMap = new ArrayList<RVMThread>();
		for(int id = VC.SPECIAL_ELEMENTS; id < VC.MAX_THREADS + 1; id++)
		{
			ThreadMap.add(new RVMThread(id));
		}
		this.MemoryMap = new HashMap<Integer, MemoryData>();
		this.LockMap = new HashMap<Integer, LockData>();
		
	}

	public ArrayList<Integer> get_atomicList() {
		// TODO Auto-generated method stub
		return this.AtomicList;
	}
	

	public void startTransaction(int threadid, int site) {
		// TODO Auto-generated method stub
		if (threadid == DACAPO_DRIVER_THREAD_OCTET_ID || threadid == OCTET_FINALIZER_THREAD_ID)
		{
			return;
		}
		
		RVMThread threadState = this.ThreadMap.get(threadid - 1);
		
		
		if(threadState.currentInTransaction == false && threadState.currentTransaction.isUnary && !this.AtomicList.contains(site))
		{
			threadState.currVC.incrementClock(threadid);
			threadState.numOfNodes++;
			
			threadState.set_currTransaction(new Transaction(threadState.numOfNodes, false, false, site, threadid));
			
			/******set HB clock for current node*******/
			threadState.currentTransaction.set_beginVC(threadState.currVC);
			threadState.currentTransaction.set_currVC(threadState.currVC);
			
			/******set the WCP predecessor time of the current thread*******/
			threadState.currentTransaction.set_currPC(threadState.currPC);
			threadState.currentInTransaction = true;
			
			/******set current sink transaction*******/
			//create new CSEV for each transaction
			threadState.currPFV = new FV();
			Epoch current = new Epoch(threadState.currVC.clock[threadid], threadState.currentTransaction.currClk, threadid);
			EpochPair currentRun = new EpochPair(current);
			threadState.currPFV.flag[threadid] = currentRun;
			
			threadState.currFV = new FV();
			threadState.currFV.flag[threadid] = currentRun;
			

		}
		
		
	}

	public void endTransaction(int threadid, int site) {
		// TODO Auto-generated method stub
		if (threadid == DACAPO_DRIVER_THREAD_OCTET_ID || threadid == OCTET_FINALIZER_THREAD_ID)
		{
			return;
		}
		
		RVMThread threadState = this.ThreadMap.get(threadid - 1);
		if(threadState.currentInTransaction == true && threadState.currentTransaction.siteID == site)
		{
			
			threadState.currentInTransaction = false;
//			//create an unary transaction to merge outside operations
			threadState.currVC.incrementClock(threadid);
			threadState.numOfNodes++;
			threadState.set_currTransaction(new Transaction(threadState.numOfNodes, true, false, site, threadid)); //automatically set region = 1
			
			/******set HB clock for current node*******/
			threadState.currentTransaction.set_beginVC(threadState.currVC); //Actually, in theory, we do not need to set begin.VC for merged unary transactions.
			threadState.currentTransaction.set_currVC(threadState.currVC);
			
			/******set the WCP predecessor time of the current thread*******/
			threadState.currentTransaction.set_currPC(threadState.currPC);
			
		}
		
	}

	public void processRead(int threadid, int address) {
		// TODO Auto-generated method stub
		if (threadid == DACAPO_DRIVER_THREAD_OCTET_ID || threadid == OCTET_FINALIZER_THREAD_ID)
		{
			return;
		}

		
		RVMThread threadState = this.ThreadMap.get(threadid - 1);
		Transaction currRead = threadState.currentTransaction;
		
		/**********************************update VC&PC**********************************************/
		for(Integer lock : threadState.HeldLocks) //memory address is fixed
		{
			KeyPair curr = new KeyPair(address, lock.intValue());
			if(LockVarWriteVCMap.containsKey(curr))
			{
				VC LW = LockVarWriteVCMap.get(curr);
				if (LW != null)
				{
					VC.join(threadState.currPC, LW);
				}
				
			}
			
			if(LockMap.containsKey(lock))//Rm = Rm U {address}
			{
				LockData ld = LockMap.get(lock);
				if(!ld.readVariable.contains(address))
				{
					ld.readVariable.add(address);
				}
			}
			
			
			
		}
		/**********************************update VC&PC**********************************************/
		
		if(this.MemoryMap.containsKey(address)) //not the first access
		{
			MemoryData MemVar = this.MemoryMap.get(address);
			
			if(MemVar.writePC != null) //last write PC exists
			{
				if(MemVar.writePC.getTid() != threadid && MemVar.readPCMap.get(threadid - 1) == null) //optimize the capture
				{
					VC.join(threadState.currPC, MemVar.writePC); //modified WCP relation, last write -> current read, WCP-ordered
				}
				
			}
			
			if(MemVar.writeVC != null) //last write exists, captures the HB relation
			{
				VC lastWrite = MemVar.writeVC;
				int lastWRegion = MemVar.writeClk;
				
				int lwThread = lastWrite.getTid();
				
				if (lwThread != threadid && MemVar.readVCMap.get(threadid - 1) == null) //this is the first read of this thread since last write
				{
					if (buildHB(lastWrite, lastWRegion, currRead))  //create write-read edge
					{
						VC.copy(currRead.currVC, threadState.currVC); //update current clock of current transaction as the current clock of this thread
					}												  //thread clock has been updated, create subregion
				}
				
			}
			VC read = new VC(threadid);
			VC.copy(read, currRead.currVC);  //update last read
			
			if(!VC.Equal(threadState.currPC, currRead.currPC))
			{
				VC.copy(currRead.currPC, threadState.currPC); 
			}
			
			VC readPC = new VC(threadid);
			VC.copy(readPC, currRead.currPC); //not sure whether this is correct
			
			MemVar.readVCMap.set(threadid - 1, read);
			
			MemVar.readClkMap[threadid - 1] = currRead.currClk; //set current subregion
			
			MemVar.readPCMap.set(threadid - 1, readPC);
	
			if(!MemVar.setRead)
			{
				MemVar.setRead = true;
			}
		}
		else   //this is the first access to this variable
		{
			MemoryData MemVar = new MemoryData(address);
			VC read = new VC(threadid);
			VC.copy(read, currRead.currVC);  //update last read
			
			
			VC readPC = new VC(threadid);
			VC.copy(readPC, currRead.currPC); //not sure whether this is correct
		
			MemVar.readVCMap.set(threadid - 1, read);
			
			MemVar.readClkMap[threadid - 1] = currRead.currClk; //set current subregion
			
			MemVar.readPCMap.set(threadid - 1, readPC);
			
			MemVar.setRead = true;
			this.MemoryMap.put(address, MemVar);
		}
		
	}

	public void processWrite(int threadid, int address) {
		// TODO Auto-generated method stub
		
		if (threadid == DACAPO_DRIVER_THREAD_OCTET_ID || threadid == OCTET_FINALIZER_THREAD_ID)
		{
			return;
		}
		
		RVMThread threadState = this.ThreadMap.get(threadid - 1);
		Transaction currWrite = threadState.currentTransaction;
		
		/**********************************update VC&PC**********************************************/
		for(Integer lock : threadState.HeldLocks) //memory address is fixed
		{
			KeyPair curr = new KeyPair(address, lock.intValue());
			if(LockVarWriteVCMap.containsKey(curr))
			{
				VC LW = LockVarWriteVCMap.get(curr);
				if (LW != null)
				{
					VC.join(threadState.currPC, LW);
				}
			}
			
			if(LockVarReadVCMap.containsKey(curr))
			{
				VC LR = LockVarReadVCMap.get(curr);
				if (LR != null)
				{
					VC.join(threadState.currPC, LR);
				}
			}
			
			
			
			if(LockMap.containsKey(lock))//Wm = Wm U {address}
			{
				LockData ld = LockMap.get(lock);
				if(!ld.writeVariable.contains(address))
				{
					ld.writeVariable.add(address);
				}
			}
				
		}
		/**********************************update VC&PC**********************************************/
		
		if(this.MemoryMap.containsKey(address)) //not the first access
		{
			MemoryData MemVar = this.MemoryMap.get(address);
			
			
				
			if(MemVar.setRead) //this variable has been read after last write
			{
				for(int id = VC.SPECIAL_ELEMENTS; id < VC.MAX_THREADS + 1; id++)
				{
					if(MemVar.readPCMap.get(id - 1) != null && id != threadid) //thread id has read this variable and id != threadid, we can build read->write dependence
					{
						VC.join(threadState.currPC, MemVar.readPCMap.get(id - 1));
					}
					
					if(MemVar.readVCMap.get(id - 1) != null && id != threadid) //thread id has read this variable and id != threadid, we can build read->write dependence
					{
						if(buildHB(MemVar.readVCMap.get(id - 1), MemVar.readClkMap[id - 1], currWrite))  //create read-write edges
						{
							VC.copy(currWrite.currVC, threadState.currVC);//thread clock has been updated, create subregion
						}
					}
					
				}
				MemVar.readVCMap = new ArrayList<VC>(); //clrear all reads 
				MemVar.readPCMap = new ArrayList<VC>(); //clear all reads, as previous relations have all been captured
				for(int id = VC.SPECIAL_ELEMENTS; id < VC.MAX_THREADS + 1; id ++)
				{
					MemVar.readVCMap.add(null);
					MemVar.readPCMap.add(null);
					MemVar.readClkMap[id-1] = -1;
				}
			}
			else //there is no read between two writes
			{
				if(MemVar.writePC != null) //last write PC exists
				{
					if(MemVar.writePC.getTid() != threadid)
					{
						VC.join(threadState.currPC, MemVar.writePC);
					}
					
				}
				
				if(MemVar.writeVC != null) //if last write exists
				{
					VC lastWrite = MemVar.writeVC;
					int lastWRegion = MemVar.writeClk;
					int lwThread = lastWrite.getTid();
					
					if(lwThread != threadid)   //create write-write dependence
					{
						if(buildHB(lastWrite, lastWRegion, currWrite)) 
						{
							VC.copy(currWrite.currVC, threadState.currVC);//thread clock has been updated, create subregion
						}
					}
				}
				
			}
			MemVar.writeVC = new VC(threadid);
			VC.copy(MemVar.writeVC, currWrite.currVC);  //update last write
			
			MemVar.writeClk = currWrite.currClk;
			
			if(!VC.Equal(threadState.currPC, currWrite.currPC))
			{
				VC.copy(currWrite.currPC, threadState.currPC); 
			}
			
			MemVar.writePC = new VC(threadid);
			VC.copy(MemVar.writePC, currWrite.currPC);  //update last write
			
		}
		else  //this is the first access to this variable
		{
			MemoryData MemVar = new MemoryData(address);
			MemVar.writeVC = new VC(threadid);
			VC.copy(MemVar.writeVC, currWrite.currVC);
			
			MemVar.writeClk = currWrite.currClk;
			
			MemVar.writePC = new VC(threadid);
			VC.copy(MemVar.writePC, currWrite.currPC);  //update last write
			
			this.MemoryMap.put(address, MemVar);
		}
		
		
	}

	public void processAcquire(int threadid, int address) {
		// TODO Auto-generated method stub
		if (threadid == DACAPO_DRIVER_THREAD_OCTET_ID || threadid == OCTET_FINALIZER_THREAD_ID)
		{
			return;
		}
		
		RVMThread threadState = this.ThreadMap.get(threadid - 1);
		Transaction currentAcq = threadState.currentTransaction;
		currentAcq.currClk = currentAcq.currClk + 1; //increment the clk to split subregion, before acquire execution
		boolean EnqueFlag = true;
		
		
		if(!threadState.HeldLocks.contains(address)) //update current held locks
		{
			threadState.HeldLocks.add(address);
		}
		else
		{
			EnqueFlag = false;
		}
		
		if(this.LockMap.containsKey(address))  //create rel-acq dependence
		{
			LockData LockVar = this.LockMap.get(address);
			if(LockVar.Pclock != null)
			{
				if(LockVar.Pclock.getTid() != threadid)
				{
					VC.join(threadState.currPC, LockVar.Pclock);
				}
				
			}
			
			if(LockVar.clock != null) //last release exists
			{
				VC lastRel = LockVar.clock;
				int lastRRegion = LockVar.Clk;
				int lastThread = lastRel.getTid();
				
				if(lastThread != threadid)
				{
					if(buildHB(lastRel, lastRRegion, currentAcq)) 
					{
						VC.copy(currentAcq.currVC, threadState.currVC); //thread clock has been updated, create subregion
					}
				}
			}
			
			LockVar.currentCS.tid = threadid;
			LockVar.currentCS.AcqClock = currentAcq.currVC.clock[threadid]; //store the Acq information of the current critical section
			LockVar.currentCS.AcqSubRegion = currentAcq.currClk;
			
			if(!VC.Equal(threadState.currPC, currentAcq.currPC))
			{
				VC.copy(currentAcq.currPC, threadState.currPC); 
			}
			
			VC.copy(LockVar.currentCS.AcqPC, RVMThread.generate_Ct(threadState));
			
		}
		else  //this is the first access to this lock
		{
			LockData LockVar = new LockData(address); 

			CriticalSection currL = new CriticalSection(threadid); //store the Acq information of the current critical section
			currL.AcqClock = currentAcq.currVC.clock[threadid];
			currL.AcqSubRegion = currentAcq.currClk;
			currL.AcqPC = new VC(threadid);
			VC.copy(currL.AcqPC, RVMThread.generate_Ct(threadState));
			LockVar.currentCS = currL;
			this.LockMap.put(address, LockVar);
		}
		
		if(EnqueFlag)
		{
			for(int id = VC.SPECIAL_ELEMENTS; id < VC.MAX_THREADS + 1; id ++)
			{
				if(id != threadid)
				{
					RVMThread idState = this.ThreadMap.get(id - 1);
					if(idState.Acq.containsKey(address))
					{
						idState.Acq.get(address).offer(RVMThread.generate_Ct(threadState)); //should generate Ct as P(t)[t:=H(t)[t]]
					}
					else
					{
						Queue<VC> tem = new LinkedList<VC>();
						tem.offer(RVMThread.generate_Ct(threadState));
						idState.Acq.put(address, tem);
					}
				}
			}
		}
		
		
	}

	public void processRelease(int threadid, int address) {
		// TODO Auto-generated method stub

		if (threadid == DACAPO_DRIVER_THREAD_OCTET_ID || threadid == OCTET_FINALIZER_THREAD_ID)
		{
			return;
		}
			
		RVMThread threadState = this.ThreadMap.get(threadid - 1);
		Transaction currentRel = threadState.currentTransaction;
		
		if(this.LockMap.containsKey(address))
		{
			LockData LockVar = this.LockMap.get(address);
			
			LockVar.currentCS.RelClock = currentRel.currVC.clock[threadid];
			LockVar.currentCS.RelSubRegion = currentRel.currClk;
			
			/*******************Mark Swappable Critical Sections on the same lock*****************/
			if(threadState.Acq.containsKey(address))
			{
				//point to swappable HB  
				if(!currentRel.swap && !LockVar.swap && LockVar.lastCS!= null && LockVar.lastCS.tid != threadid && !VC.HappensBefore(LockVar.lastCS.AcqPC, RVMThread.generate_Ct(threadState))) // && threadState.HeldLocks.size() == 1
				{
					LockVar.swap = true;
					//forward and backward propagate the swappable relation // current rel has not been updated yet
			
					System.out.print("SHB  " + LockVar.currentCS.RelClock + "@" + LockVar.currentCS.RelSubRegion + "@" + LockVar.currentCS.tid);
				
					System.out.print("-->");
					
					System.out.println(LockVar.lastCS.AcqClock + "@" + LockVar.lastCS.AcqSubRegion + "@" + LockVar.lastCS.tid);
					
					currentRel.swap = true;
					Epoch swapSource = new Epoch(LockVar.currentCS.RelClock, LockVar.currentCS.RelSubRegion, LockVar.currentCS.tid);
					Epoch swapSink = new Epoch(LockVar.lastCS.AcqClock, LockVar.lastCS.AcqSubRegion, LockVar.lastCS.tid);
					
					updatePSEV(swapSource, swapSink); 
					
					
					
				}
				
				/****************Tracking WCP relation rule(b)******************/
				while(threadState.Acq.containsKey(address) && threadState.Acq.get(address).peek() != null && threadState.Rel.containsKey(address) && threadState.Rel.get(address).peek() != null && VC.HappensBefore(threadState.Acq.get(address).peek(), RVMThread.generate_Ct(threadState))) //should check whether peek() = null?
				{
					threadState.Acq.get(address).poll();
					
					VC.join(threadState.currPC, threadState.Rel.get(address).poll());
				}
			
			}
			
			/****************update recorded LR(m,x)***********************/
			
			KeyPair temp;
			for(Integer readVar : LockVar.readVariable)
			{
				temp = new KeyPair(readVar.intValue(), address);
				if(LockVarReadVCMap.containsKey(temp))
				{
					VC rd = LockVarReadVCMap.get(temp);
					VC.join(rd, threadState.currVC);
					LockVarReadVCMap.put(temp, rd);
				}
				else
				{
					VC rd = new VC(threadid);
					VC.join(rd, threadState.currVC);
					LockVarReadVCMap.put(temp, rd);
				}
			}
			
			/****************update recorded LW(m,x)***********************/
			for(Integer writeVar : LockVar.writeVariable)
			{
				temp = new KeyPair(writeVar.intValue(), address);
				if(LockVarWriteVCMap.containsKey(temp))
				{
					VC wr = LockVarWriteVCMap.get(temp);
					VC.join(wr, threadState.currVC);
					LockVarWriteVCMap.put(temp, wr);
				}
				else
				{
					VC wr = new VC(threadid);
					VC.join(wr, threadState.currVC);
					LockVarWriteVCMap.put(temp, wr);
				}
				
				
			}
			
			/*****************************************************************/
			
			LockVar.clock = new VC(threadid);	
			VC.copy(LockVar.clock, currentRel.currVC);  //update the clock of this lock
			
			LockVar.Clk = currentRel.currClk;
			
			if(!VC.Equal(threadState.currPC, currentRel.currPC))
			{
				VC.copy(currentRel.currPC, threadState.currPC); 
			}
			
			LockVar.Pclock = new VC(threadid);
			VC.copy(LockVar.Pclock, currentRel.currPC);
			
			/****************update critical section information********************/
			
		
			
			if(LockVar.lastCS != null)
			{
				
				CriticalSection.copy(LockVar.lastCS, LockVar.currentCS);
			}
			else
			{
				LockVar.lastCS = new CriticalSection(threadid);
				LockVar.lastCS.AcqPC = new VC(threadid);
				CriticalSection.copy(LockVar.lastCS, LockVar.currentCS);
			}
			
			currentRel.currClk = currentRel.currClk + 1; //increment the clk to split subregion
			
			
			LockVar.InitialVarSet(); // Rm <- Wm <- empty set
		}
		
		
		/******************update the Rel queue of other threads*********************/
		for(int id = VC.SPECIAL_ELEMENTS; id < VC.MAX_THREADS + 1; id ++)
		{
			if(id != threadid)
			{
				RVMThread otherState = this.ThreadMap.get(id - 1);
				if(otherState.Rel.containsKey(address))
				{
					Queue<VC> re = otherState.Rel.get(address);
					re.offer(threadState.currVC);
				}
				else
				{
					Queue<VC> re = new LinkedList<VC>();
					re.offer(threadState.currVC);
					otherState.Rel.put(address, re);
				}
			}
		}
		
		/********************remove current lock from the set*************************/
		if(threadState.HeldLocks.contains(address))
		{
			threadState.HeldLocks.remove(address);
		}
		
	}
	
	public void printStatics()
	{
		System.out.println("Trans: " + this.transOp);
		System.out.println("Read: " + this.readOp);
		System.out.println("Write: " + this.writeOp);
		System.out.println("Acq: " + this.acqOp);
		System.out.println("Rel: " + this.relOp);
		System.out.println("**********************************");
	}
	
	public void printTarget()
	{
		for(Entry<Integer, Integer> entry : this.targetList.entrySet())
		{
			System.out.println(entry.getKey() + "  " + entry.getValue());
		}
		
	}
	
	public boolean buildHB(VC source, int sourceRegion, Transaction dest)
	{
		int sourceID = source.getTid();
		int destID = dest.threadid;
		if(!needHB(sourceID, destID))
		    return false;
		
	
		RVMThread threadState = this.ThreadMap.get(destID - 1);
		boolean updated = VC.join(threadState.currVC, source);  //update the clock by join operation
		updateSEV(source, sourceRegion, dest);
		
		if(checkHB(source, dest))
		{
			if(!this.DetectList.contains(dest.siteID))
			{
				this.DetectList.add(dest.siteID);
			}
			
			if(!threadState.TransactionList.contains(dest.transactionID)) //for each transaction, only report once
			{
				
				threadState.TransactionList.add(dest.transactionID);

				System.out.println("*********RT-V1************"); //report the cycles
				locateCycleSequence(source, sourceRegion, dest);
				System.out.println(dest.siteID);
				System.out.println("**************************");
			}
		}
		else 
		{
			if(precheckHB(source, sourceRegion, dest)) {
				if(!this.DetectList.contains(dest.siteID))
				{
					this.DetectList.add(dest.siteID);
				}
				
				if(!threadState.TransactionList.contains(dest.transactionID)) //for each transaction, only report once
				{
					
					threadState.TransactionList.add(dest.transactionID);

					
					
					System.out.println("*********predicitve TAV************"); //report the cycles
					System.out.println(dest.threadid + "--" + dest.transactionID + "--"+ dest.siteID);
					locatePredictiveCycleSequence(source, sourceRegion, dest);
					//EpochPair.printEP(threadState.currPFV.flag[sourceID]);
					System.out.println("**************************");
				}
			}
			
			if(checkCycle(source, dest)) //disable for non-increasing cycles
			{
				System.out.println("*********RT-V2************");
				System.out.println("**************************");
			}
		}
	
		//update TVC
		updateTVC(source, dest, sourceID, destID);
		
		
		return updated; //clock has been updated
	}
	
	private void updateTVC(VC source, Transaction dest, int sourceID, int destID)
	{
		RVMThread destState = this.ThreadMap.get(destID - 1);
		RVMThread sourceState = this.ThreadMap.get(sourceID - 1);
		int sourceC = source.clock[sourceID];
		int sinkC = destState.currVC.clock[destID];
		ArrayList<Integer> Tids = new ArrayList<Integer>();
		if(sourceState.reverVC.clock[sourceID] == sourceC) //same source transaction
		{
			if(sourceState.reverVC.clock[destID] == 0 || sourceState.reverVC.clock[destID] > sinkC)
			{
				sourceState.reverVC.clock[destID] = sinkC;
			}
			forwardPropagate(Tids, sourceID, destID);
			backPropagate(Tids, sourceID, destID, sourceC, sinkC);
		}
		else if(sourceState.reverVC.clock[sourceID] < sourceC)
		{
			sourceState.reverVC.initialVC();
			sourceState.reverVC.clock[sourceID] = sourceC;
			sourceState.reverVC.clock[destID] = sinkC;
			forwardPropagate(Tids, sourceID, destID);
			backPropagate(Tids, sourceID, destID, sourceC, sinkC);
		}
		
	}
	
	private ArrayList<Integer> forwardPropagate(ArrayList<Integer> Tids, int sourceID, int destID)
	{
		ArrayList<Integer> Tid2 = Tids;
		Tid2.add(sourceID);
		Tid2.add(destID);
		RVMThread destState = this.ThreadMap.get(destID - 1);
		RVMThread sourceState = this.ThreadMap.get(sourceID - 1);
		if(sourceState.reverVC.clock[destID] > 0 && destState.reverVC.clock[destID] > 0 && sourceState.reverVC.clock[destID] <= destState.reverVC.clock[destID] ) 
		{
			for(int id = VC.SPECIAL_ELEMENTS; id < VC.MAX_THREADS; id ++)
			{
				if(id == sourceID)
				{
					continue;
				}
				
				if((destState.reverVC.clock[id] != 0 && sourceState.reverVC.clock[id] > destState.reverVC.clock[id]) || (sourceState.reverVC.clock[id] == 0 && destState.reverVC.clock[id] > 0))
				{
					sourceState.reverVC.clock[id] = destState.reverVC.clock[id];
					
					if(!Tid2.contains(id))
					{
						Tid2 = forwardPropagate(Tid2, sourceID, id);
					}
				}
			}
		}
		return Tid2;
	}
	
	private ArrayList<Integer> backPropagate(ArrayList<Integer> Tids, int sourceID, int destID, int source, int sink)
	{
		ArrayList<Integer> Tid1 = Tids;
		Tid1.add(sourceID);
		Tid1.add(destID);
		for(int id = VC.SPECIAL_ELEMENTS; id < VC.MAX_THREADS; id ++)
		{
			if(Tid1.contains(id))
			{
				continue;
			}
			
			RVMThread threadState1 = this.ThreadMap.get(id - 1);
			if(threadState1.reverVC.clock[id] > RVMThread.START_TRANSACTION_ID) //running transaction has outgoing edge
			{
				if(threadState1.reverVC.clock[sourceID] <= source)
				{
					if(threadState1.reverVC.clock[destID] == 0 || (sink != 0 && threadState1.reverVC.clock[destID] > sink))
					{
						threadState1.reverVC.clock[destID] = sink;
					}
					forwardPropagate(Tids, id, destID); //update TVC of thread (id)
					Tid1 = backPropagate(Tid1, id, destID, threadState1.reverVC.clock[id], sink); //back propagate to other threads
				}
			}
		}
		return Tid1;
	}
	
	private boolean checkCycle(VC source, Transaction dest) {
		// TODO Auto-generated method stub
		if(dest.isUnary || dest.transactionID == RVMThread.START_TRANSACTION_ID)
			return false;
		int sourceID = source.getTid();
		int destID = dest.threadid;
		
		RVMThread destState = this.ThreadMap.get(destID - 1);
		if(dest.currVC.clock[destID] == destState.reverVC.clock[destID])  //current ongoing transaction dest has outgoing edges
		{
			if(destState.reverVC.clock[sourceID] > 0 && VC.lessThan(destState.reverVC, source, sourceID)) //current ongoing transaction dest has direct or indirect outgoing edge
			{																				//to source or predecessor transaction in source thread
				return true;
			}
		}
		return false;
	}
	
	private ArrayList<Integer> forwardPropaPSEV(ArrayList<Integer> Tids, Epoch swapSource, Epoch swapSink, int sourceID, int sinkID)
	{
		ArrayList<Integer> Tid2 = Tids;
		Tid2.add(sourceID);
		Tid2.add(sinkID);
		RVMThread destState = this.ThreadMap.get(sinkID - 1);
		RVMThread sourceState = this.ThreadMap.get(sourceID - 1);
		
		if(sourceState.currPFV.flag[sinkID] != null && destState.currPFV.flag[sinkID] != null && FV.existPath(sourceState.currPFV, sourceID, destState.currPFV.flag[sinkID].sink))// check whether increasing path exists
			//, i.e., destState.currPFV.flag[sinkID].sink is the currently running of thread sinkID, whether sourceID has increasing path
		{
			for(int id = VC.SPECIAL_ELEMENTS; id < VC.MAX_THREADS + 1; id ++)
			{
				if(Tid2.contains(id)) //the updated EP should be protected, not to be modified
				{
					continue;
				}
				
				//currently only update "null" component, smaller one should store???
				if(sourceState.currPFV.flag[id] == null && destState.currPFV.flag[id] != null && FV.existPath(sourceState.currPFV, sourceID, destState.currPFV.flag[id].source))
				{
					
					sourceState.currPFV.flag[id] = destState.currPFV.flag[id];
					
					Tid2 = forwardPropaPSEV(Tid2, swapSource, swapSink, sourceID, id);
					
				}
				
				//currently only update "null" component, smaller one should store???
				if(sourceState.currPFV.flag[id] == null && destState.currFV.flag[id] != null && FV.existPath(sourceState.currPFV, sourceID, destState.currFV.flag[id].source))
				{
					sourceState.currPFV.flag[id] = destState.currFV.flag[id];
					
					Tid2 = forwardPropaPSEV(Tid2, swapSource, swapSink, sourceID, id);
				}
			}
		}
		return Tid2;
	}
	
	
	private ArrayList<Integer> backPropaPSEV(ArrayList<Integer> Tids, Epoch swapSource, Epoch swapSink, int sourceID, int sinkID)
	{
		ArrayList<Integer> Tid1 = Tids;
		Tid1.add(sourceID);
		Tid1.add(sinkID);
		EpochPair swapss = new EpochPair(swapSource, swapSink);
		for(int id = VC.SPECIAL_ELEMENTS; id < VC.MAX_THREADS + 1; id ++)
		{
			if(Tid1.contains(id))
			{
				continue;
			}
			
			RVMThread otherState = this.ThreadMap.get(id - 1);
			
			if(otherState.currPFV.flag[sourceID] != null && otherState.currPFV.flag[sinkID] == null 
					&& FV.existPath(otherState.currPFV, id, swapSource)) //make sure the checking is correct
			{
				otherState.currPFV.flag[sinkID] = swapss;
				forwardPropaPSEV(new ArrayList<Integer>(), swapSource, swapSink, id, sinkID); //how to determine??
			}
			else if(FV.isNullChecking(otherState.currPFV, id) && otherState.currFV.flag[sourceID] != null && otherState.currFV.flag[sinkID] == null 
					&& FV.existPath(otherState.currFV, id, swapSource))
			{
				FV.copy(otherState.currPFV, otherState.currFV);
				otherState.currPFV.flag[sinkID] = swapss;
				forwardPropaPSEV(new ArrayList<Integer>(), swapSource, swapSink, id, sinkID); //how to determine??
				
			}
		}
		return Tid1;
	}
	
	
	private void updatePSEV(Epoch swapSource, Epoch swapSink)
	{
		ArrayList<Integer> Tids11 = new ArrayList<Integer>();
		ArrayList<Integer> Tids12 = new ArrayList<Integer>();
		RVMThread sourceState = ThreadMap.get(swapSource.tid - 1); //update the PSEV of source thread
		EpochPair swapss = new EpochPair(swapSource, swapSink);
		
		if(sourceState.currPFV.flag[swapSource.tid] != null && Epoch.RegionLessThan(sourceState.currPFV.flag[swapSource.tid].sink, swapSource) && sourceState.currPFV.flag[swapSink.tid] == null) //currently running transaction
		{
				sourceState.currPFV.flag[swapSink.tid] = swapss; //update the PSEV of source thread at corresponding position
				
				/************************forward propagate its PSEV***********************/
				forwardPropaPSEV(Tids11, swapSource, swapSink, swapSource.tid, swapSink.tid); //update the PSEV of sourceState
		}
		
		backPropaPSEV(Tids12, swapSource, swapSink, swapSource.tid, swapSink.tid); //update the PSEV of other threads
		
	}
	
	private void locatePredictiveCycleSequence(VC source, int sourceRegion, Transaction dest)
	{
		//the last and the first transaction in this cycle should be dest
		LinkedList<Epoch> sequence = new LinkedList<Epoch>(); 
		int destTid = dest.threadid;
		int sourceTid = source.getTid();
		int errorFlag = 0;
		RVMThread threadState = this.ThreadMap.get(destTid - 1);
		

		//search from the last to the beginning
		Epoch last = new Epoch(dest.currVC.clock[destTid], dest.currClk, destTid); //current incoming edge, source --> dest
		sequence.add(last);
		Epoch beforeLast = new Epoch(source.clock[sourceTid], sourceRegion, sourceTid);
		sequence.add(beforeLast);
		
		EpochPair previous = threadState.currPFV.flag[sourceTid];
		if(previous != null && (previous.sink.clock < beforeLast.clock || (previous.sink.clock == beforeLast.clock && previous.sink.region <= beforeLast.region)))
		{
			sequence.add(previous.sink);
		}
		
		Epoch target = previous.source;
		while(true)
		{
			if(target != null && Epoch.RegionLessThan(target, last))
			{
				sequence.add(target); // find the dest transaction --> rebuild the path from beforeLast to dest, a cycle exists
				break;
			}
			
			sequence.add(target);
			previous = threadState.currPFV.flag[target.tid];
			if(previous != null && (previous.sink.clock < target.clock || (previous.sink.clock == target.clock && previous.sink.region <= target.region)))
			{
				sequence.add(previous.sink);
			}
			target = previous.source;
			
			errorFlag ++;
			
			if(errorFlag == VC.MAX_THREADS)
			{
				//endless loop error, no path found
				//print the error threadState.currFV
				System.out.println("locate predictive cycle error!!!");
				FV.printFV(threadState.currPFV);
				break;
			}
			
		}	
		if(errorFlag <= VC.MAX_THREADS)
		{
			for(int i = sequence.size() - 1; i > 0; i--)
			{
				System.out.print(sequence.get(i).clock + "@" + sequence.get(i).region + "@" + sequence.get(i).tid + "-->");
			}
			System.out.println(sequence.get(0).clock + "@" + sequence.get(0).region + "@" + sequence.get(0).tid);
		}
		
	}
	
	private void locateCycleSequence(VC source, int sourceRegion, Transaction dest)
	{
		//the last and the first transaction in this cycle should be dest
		LinkedList<Epoch> sequence = new LinkedList<Epoch>(); 
		int destTid = dest.threadid;
		int sourceTid = source.getTid();
		int errorFlag = 0;
		RVMThread threadState = this.ThreadMap.get(destTid - 1);
		

		//search from the last to the beginning
		Epoch last = new Epoch(source.clock[destTid], dest.currClk, destTid); //source-->dest
		sequence.add(last);
		Epoch beforeLast = new Epoch(source.clock[sourceTid], sourceRegion, sourceTid);
		sequence.add(beforeLast);
		
		EpochPair previous = threadState.currFV.flag[sourceTid];
		if(previous != null && previous.sink.clock < beforeLast.clock)
		{
			sequence.add(previous.sink);
		}
		Epoch target = previous.source;
		while(true)
		{
			if(Epoch.Equal(target, last))
			{
				sequence.add(target); // find the dest transaction --> rebuild the path from beforeLast to dest, a cycle exists
				break;
			}
			
			sequence.add(target);
			previous = threadState.currFV.flag[target.tid];
			if(previous != null && previous.sink.clock < target.clock)
			{
				sequence.add(previous.sink);
			}
			target = previous.source;
			
			errorFlag ++;
			
			if(errorFlag == VC.MAX_THREADS)
			{
				//endless loop error, no path found
				//print the error threadState.currFV
				System.out.println("locate cycle error!!!");
				FV.printFV(threadState.currFV);
				break;
			}
			
		}	
		if(errorFlag <= VC.MAX_THREADS)
		{
			for(int i = sequence.size() - 1; i > 0; i--)
			{
				System.out.print(sequence.get(i).clock + "@" + sequence.get(i).region + "@" + sequence.get(i).tid + "-->");
			}
			System.out.println(sequence.get(0).clock + "@" + sequence.get(0).region + "@" + sequence.get(0).tid);
		}
		
	}
	
	private void updateSEV(VC source, int sourceRegion, Transaction dest)
	{
		int destTid = dest.threadid;
		int sourceTid = source.getTid();
		RVMThread sourceState = this.ThreadMap.get(sourceTid - 1);
		Epoch sourceE = new Epoch(source.clock[sourceTid], sourceRegion, sourceTid);
		Epoch sinkE = new Epoch(dest.currVC.clock[destTid], dest.currClk, destTid);
		
		//just check whether need to update the SEV of the source thread, only when currSourceTrans is the currently running transaction of source thread
		if(sourceState.currFV.flag[sourceTid] != null && Epoch.Equal(sourceState.currFV.flag[sourceTid].sink, sourceE) && sourceState.currFV.flag[destTid] == null) 
		{
			EpochPair ss = new EpochPair(sourceE, sinkE);
			sourceState.currFV.flag[destTid] = ss;
		}
		
		backPropagateSEVandPSEV(sourceTid, destTid, source, dest, sourceE, sinkE);
	}
	
	private void backPropagateSEVandPSEV(int sourceID, int destID, VC source, Transaction dest, Epoch sourceE, Epoch sinkE)
	{
		for(int id = VC.SPECIAL_ELEMENTS; id < VC.MAX_THREADS + 1; id++)
		{
			if(id != sourceID && id != destID)
			{
				//check whether the sinkE need to propagate to other threads through sourceE
				RVMThread threadState = this.ThreadMap.get(id - 1);
				EpochPair ss = new EpochPair(sourceE, sinkE);
				//(1)other thread must have a currently running transaction->has begin event, (2)must have an increasing THB relation from that trans to current dest, (3) has path to source transaction, (4) no path to sink thread before 
				if(threadState.currentInTransaction && threadState.currentTransaction.beginVC.clock[id] <= source.clock[id] && (threadState.currFV.flag[sourceID] != null && Epoch.LessEqual(threadState.currFV.flag[sourceID].sink, sourceE)) && threadState.currFV.flag[destID] == null)
				{
					
					threadState.currFV.flag[destID] = ss;
				}
				//(1)other thread must have a currently running transaction, (2)the running transaction has an increasing path from itself to sourceE, 
				//(3)no path to sink thread before (as the sink event is the current event of sink thread, it should has the largest transID and regionID, 
				//if other thread exists path to sink thread, then the transID and regionID should smaller than or equal to current sink event), hence no need to consider path exist case??
				if(threadState.currentInTransaction && FV.existPath(threadState.currPFV, id, sourceE) && threadState.currPFV.flag[destID] == null)//
				{
					threadState.currPFV.flag[destID] = ss;
				}
				
				
			}
		}
	}
	
	
	private boolean checkHB(VC source, Transaction dest) {
		// TODO Auto-generated method stub
		if(dest.isUnary || dest.transactionID == RVMThread.START_TRANSACTION_ID)
			return false;
		if(VC.lessThan(dest.beginVC, source, dest.threadid)) //dest.beginVC happens-before source ==> source can see dest.begin event
		{
			return true;
		}
		return false;
	}
	
	private boolean precheckHB(VC source, int region, Transaction dest) {
		// TODO Auto-generated method stub
		if(dest.isUnary || dest.transactionID == RVMThread.START_TRANSACTION_ID)
			return false;
		EpochPair target = ThreadMap.get(dest.threadid - 1).currPFV.flag[source.getTid()];
		
		if(target != null && target.source != null && target.sink != null)
		{
			if(target.sink.tid == source.getTid() 
					&& (target.sink.clock < source.clock[source.getTid()] 
							|| (target.sink.clock == source.clock[source.getTid()] && target.sink.region <= region))) //dest.beginVC happens-before source ==> source can see dest.begin event
			{
				return true;
			}
		}
		
		return false;
	}

	private static boolean needHB(int sourceID, int destID) {
		   if (sourceID == destID) { //|| isCrossThreadEdgeAlreadyPresent(source, dest)) {
		     return false; // No cross-thread dependence
		   }   
		   // This is especially problematic for avrora9
		   if (sourceID == OCTET_FINALIZER_THREAD_ID || destID == OCTET_FINALIZER_THREAD_ID) {
		     return false;
		   }    
//		   // We avoid cycle detection from the driver thread in DaCapo, which is currently Thread 1. Sync changes with AVD.
		   if (sourceID == DACAPO_DRIVER_THREAD_OCTET_ID || destID == DACAPO_DRIVER_THREAD_OCTET_ID) {
		     return false;
		   }

		   return true;
		 }

	
	
}
