package predictive;
import java.util.ArrayList;

import predictive.VC;

public class MemoryData {
	
	public int address;
	public VC writeVC;
	public int writeClk;
	public VC writePC;
	public boolean setRead;
	public ArrayList<VC> readVCMap;
	public int[] readClkMap;
	public ArrayList<VC> readPCMap;
	
	MemoryData(int address)
	{
		this.address = address;
		this.writeVC = null;
		this.writeClk = -1;
		this.writePC = null;
		this.setRead = false;
		this.readVCMap = new ArrayList<VC>();
		this.readClkMap = new int[VC.MAX_THREADS];
		this.readPCMap = new ArrayList<VC>();
		for(int id = VC.SPECIAL_ELEMENTS; id < VC.MAX_THREADS + 1; id ++)
		{
			readVCMap.add(null);
			readPCMap.add(null);
			readClkMap[id-1] = -1;
		}
	}

}
