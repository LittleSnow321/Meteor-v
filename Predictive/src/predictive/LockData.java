package predictive;

import java.util.HashSet;
import java.util.Set;

public class LockData {
	public int address;
	public VC clock;
	public int Clk;
	public VC Pclock;
	public Set<Integer> readVariable;
	public Set<Integer> writeVariable;
	public CriticalSection lastCS;
	public CriticalSection currentCS;
	public boolean swap;
	
	LockData(int address)
	{
		this.address = address;
		this.clock = null;
		this.Clk = -1;
		this.Pclock = null;
		this.readVariable = new HashSet<Integer>();
		this.writeVariable = new HashSet<Integer>();
		this.lastCS = null;
		this.currentCS = null;
		this.swap = false;
	}
	
	public void updateClock(VC vc)
	{
		VC.copy(this.clock, vc);
	}
	
	public void updatePClock(VC pc)
	{
		VC.copy(this.Pclock, pc);
	}
	
	public void InitialVarSet()
	{
		this.readVariable = new HashSet<Integer>();
		this.writeVariable = new HashSet<Integer>();
	}
}
