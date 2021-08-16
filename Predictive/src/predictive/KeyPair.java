package predictive;

public class KeyPair {
	public Integer variable;
	public Integer lock;
	
	KeyPair(Integer var, Integer l)
	{
		this.variable = var;
		this.lock = l;
	}

	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		if (obj == null) {
			return false;
		}
		
		
		if(!(obj instanceof KeyPair))
		{
			return false;
			
		}
		
		KeyPair kp = (KeyPair) obj;
		return (this.variable.equals(kp.variable) && this.lock.equals(kp.lock));
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		int ret = variable.hashCode() ^ lock.hashCode();
		return ret;
	}
	
	

}
