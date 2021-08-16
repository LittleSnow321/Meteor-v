package predictive;

public class Epoch {
	public int clock;
	public int region;
	public int tid;
	
	
	Epoch(int clock, int region, int tid)
	{
		this.clock = clock;
		this.region = region;
		this.tid = tid;
	}
	
	
	public static boolean Equal(Epoch e1, Epoch e2)
	{
		if (e1.tid == e2.tid && e1.clock == e2.clock)
		{
			return true;
		}
		return false;
	}
	
	
	
	public static boolean LessEqual(Epoch e1, Epoch e2)
	{
		if (e1.tid == e2.tid && e1.clock <= e2.clock)
		{
			return true;
		}
		return false;
	}
	
	
	public static boolean Less(Epoch e1, Epoch e2)
	{
		if (e1.tid == e2.tid && e1.clock < e2.clock)
		{
			return true;
		}
		return false;
	}
	
	public static boolean RegionEqual(Epoch e1, Epoch e2)
	{
		if (e1.tid == e2.tid && e1.clock == e2.clock && e1.region == e2.region)
		{
			return true;
		}
		return false;
	}
	
	public static boolean RegionLessThan(Epoch e1, Epoch e2)
	{
		if (e1.tid == e2.tid && e1.clock == e2.clock && e1.region <= e2.region)
		{
			return true;
		}
		return false;
	}
	
	public static boolean RegionLess(Epoch e1, Epoch e2)
	{
		if (e1.tid == e2.tid && e1.clock == e2.clock && e1.region < e2.region)
		{
			return true;
		}
		return false;
	}
	
	
	
	
	

}
