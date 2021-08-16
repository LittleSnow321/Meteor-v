package predictive;

import java.util.ArrayList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;


public class RVMThread {
	public static int START_TRANSACTION_ID = 1;
	
	public int threadid;
	public VC currVC;
	public VC currPC;
	public VC reverVC;
	public FV currPFV; //for predictive TAV
	public FV currFV; //for predictive TAV
	public int numOfNodes;
	public boolean currentInTransaction;
	public Transaction currentTransaction;
	public ArrayList<Integer> TransactionList;
	public Set<Integer> HeldLocks; 
	public Map<Integer, Queue<VC>> Acq;
	public Map<Integer, Queue<VC>> Rel;
	
	RVMThread(int threadid)
	{
		this.threadid = threadid;
		this.currVC = new VC(threadid);
		this.currVC.incrementClock(threadid);
		this.currPC = new VC(threadid);
		this.reverVC = new VC(threadid);
		this.currPFV = new FV();
		this.currFV = new FV();
		this.numOfNodes = START_TRANSACTION_ID;
		this.currentInTransaction = false;
		this.currentTransaction = new Transaction(this.numOfNodes, true, false, -1, threadid);
		this.currentTransaction.set_beginVC(this.currVC);
		this.currentTransaction.set_currVC(this.currVC);
		this.TransactionList = new ArrayList<Integer>();
		this.HeldLocks = new HashSet<Integer>();
		this.Acq = new HashMap<Integer, Queue<VC>>();
		this.Rel = new HashMap<Integer, Queue<VC>>();
	}
	


	public void set_currTransaction(Transaction currTrans)
	{
		this.currentTransaction = currTrans;
	}
	
	public Transaction get_currTransaction()
	{
		return this.currentTransaction;
	}
	
	public static VC generate_Ct(RVMThread currThread)
	{
		VC Ct = new VC(currThread.threadid);
		VC.copy(Ct, currThread.currPC);
		VC.setValue(Ct, currThread.currVC, currThread.threadid);
		
		return Ct;
	}

}
