package PsiDemuxer;

import java.util.ArrayList;
import java.util.Iterator;

public class TableFragmentPacketNumList  {
	private ArrayList<Long> packetNumList = new ArrayList<Long>();
	
	public void addPacketNum(long num)
	{
		packetNumList.add(num);
	}
	
	public ArrayList<Long> getPacketNumList()
	{
		return this.packetNumList;
	}
	
	public String getNumListString()
	{
		String ret = "[";
		Iterator<Long> iter = packetNumList.iterator();
		while(iter.hasNext())
		{
			ret += iter.next();
			ret += "/";
		}
		ret += "]";
		return ret;
	}
	public Long getFirstPacketNum()
	{
		return packetNumList.get(0);
	}

	

}
