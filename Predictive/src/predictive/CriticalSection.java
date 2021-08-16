package predictive;

public class CriticalSection {
	public int tid;
	public int AcqClock;
	public int AcqSubRegion;
	public int RelClock;
	public int RelSubRegion;
	public VC AcqPC;
	
	CriticalSection(int tid)
	{
		this.tid = tid;
		this.AcqClock = -1;
		this.AcqSubRegion = -1;
		this.RelClock = -1;
		this.RelSubRegion = -1;
		this.AcqPC = null;
	}
	
	public static void copy(CriticalSection cs1, CriticalSection cs2)
	{
		cs1.tid = cs2.tid;
		cs1.AcqClock = cs2.AcqClock;
		cs1.AcqSubRegion = cs2.AcqSubRegion;
		cs1.RelClock = cs2.RelClock;
		cs1.RelSubRegion = cs2.RelSubRegion;
		//cs1.AcqPC = new VC(cs2.tid);
		VC.copy(cs1.AcqPC, cs2.AcqPC);
	}

}
