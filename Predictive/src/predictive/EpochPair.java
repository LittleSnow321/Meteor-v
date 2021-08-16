package predictive;

public class EpochPair {
	public Epoch source;
	public Epoch sink;
	
	EpochPair()
	{
		this.source = null;
		this.sink = null;
	}
	
	EpochPair(Epoch sinkE)
	{
		this.source = null;
		this.sink = sinkE;
	}
	
	EpochPair(Epoch sourceE, Epoch sinkE)
	{
		this.source = sourceE;
		this.sink = sinkE;
	}
	
	
	public static void printEP(EpochPair ep)
	{
		System.out.println(ep.source.clock + "@" + ep.source.region + "@" + ep.source.tid + "--->>" + ep.sink.clock + "@" + ep.sink.region + "@" + ep.sink.tid);
	}

}
