package predictive;

public class FV {

	public EpochPair[] flag;
	
	FV()
	{
		this.flag = new EpochPair[VC.MAX_THREADS + 1];
	}
	
	
	public boolean isNull()
	{
		boolean flag = true;
		for(int i = VC.SPECIAL_ELEMENTS; i < VC.MAX_THREADS + 1; i++)
		{
			if(this.flag[i] != null)
			{
				flag = false;
				break;
			}
		}
		return flag;
	}
	
	public static boolean isNullChecking(FV currPFV, int id)
	{
		boolean flag = true;
		for(int i = VC.SPECIAL_ELEMENTS; i < VC.MAX_THREADS + 1; i++)
		{
			if(i == id) continue;
			if(currPFV.flag[i] != null)
			{
				flag = false;
				break;
			}
		}
		return flag;
	}
	
	public static void copy(FV fv1, FV fv2)
	{
		for(int i = 0; i < VC.MAX_THREADS + 1; i ++)
		{
			fv1.flag[i] = fv2.flag[i];
		}
	}
	
	
	public static void printFV(FV currentFV)
	{
		System.out.print("[");
		for(int i = VC.SPECIAL_ELEMENTS; i < VC.MAX_THREADS + 1; i ++)
		{
			System.out.print("(");
			if(currentFV.flag[i] != null)
			{
				if(currentFV.flag[i].source != null) //new type of epoch
				{
					System.out.print(currentFV.flag[i].source.clock + "@" + currentFV.flag[i].source.region + "@"+ currentFV.flag[i].source.tid + ", ");
				}
				
				if(currentFV.flag[i].sink != null)
				{
					System.out.print(currentFV.flag[i].sink.clock + "@" + currentFV.flag[i].sink.region + "@" + currentFV.flag[i].sink.tid);
				}
			}
			System.out.print("), ");
		}
		System.out.println("]");
	}
	
	
	public static boolean existPath(FV currentPFV, int currTid, Epoch target) //target.tid != currTid
	{
		if(currentPFV.flag[target.tid] == null || currentPFV.flag[target.tid].sink == null) //No path to target thread
		{
			return false;
		}
		else //exist path to target thread, check whether the path is increasing
		{
				if(Epoch.RegionLessThan(currentPFV.flag[target.tid].sink, target)
						|| Epoch.Less(currentPFV.flag[target.tid].sink, target))
				{
					int errorFlag = 0;
					Epoch source = currentPFV.flag[target.tid].source; //target.tid != currTid, so source != null
					
					while(true)
					{
						errorFlag ++;
						if(source != null && Epoch.RegionLessThan(currentPFV.flag[currTid].sink, source)) //find the target end transaction
						{
							return true;
						}
						else if(source != null && currentPFV.flag[source.tid]!= null && currentPFV.flag[source.tid].sink != null && (Epoch.RegionLessThan(currentPFV.flag[source.tid].sink, source)
						|| Epoch.Less(currentPFV.flag[source.tid].sink, source)))//checking the increasing path
						{
							source = currentPFV.flag[source.tid].source;
						}
						else
						{
							return false;
						}
						
						if(errorFlag == VC.MAX_THREADS)
						{
							//endless loop error, no path found
							System.out.println(" exist path error!!!");
							return false;
						}
					}
					
				}
				else
				{
					return false;
				}
				
			}
		} 
		
	
}
