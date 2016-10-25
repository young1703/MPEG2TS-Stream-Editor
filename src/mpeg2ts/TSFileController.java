package mpeg2ts;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;

import util.MyUtil;

public class TSFileController {
	
	BufferedInputStream bis;
	int aPacketSize = 188;
	int curFilePos = 0;
	
	public boolean openTsFile(String path)
	{
		try {
			bis = new BufferedInputStream(new FileInputStream(path));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}
	public boolean closeTsFile() 
	{
		try {
			System.out.flush();
			bis.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public byte[] getNextPacket()
	{
		byte[] buf = new byte[aPacketSize];
		try {
			if(-1 == bis.read(buf, 0, aPacketSize))
			{
				MyUtil.logPrint("EOF!");
				return null;
			}
			if((int)MyUtil.getValueFromBytes(buf, 0, 8) != 0x47)
			{
				
				System.out.println("0x47 error");
				MyUtil.hexdump(buf, aPacketSize);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

		return buf;
	}
	public byte[] getNextPacketWithoutPids(ArrayList<Integer> pidList)
	{
		byte[] buf = new byte[aPacketSize];
		boolean isSkip = false;
		while(true)
		{
			isSkip = false;
			try {
				if(-1 == bis.read(buf, 0, aPacketSize))
				{
					MyUtil.logPrint("EOF!");
					return null;
				}
				if((int)MyUtil.getValueFromBytes(buf, 0, 8) != 0x47)
				{
					
					System.out.println("0x47 error");
					MyUtil.hexdump(buf, aPacketSize);
				}
				int curPid = (int)MyUtil.getValueFromBytes(buf, 11, 13);
				//System.out.println("curPid = "+ curPid);
				Iterator<Integer> iter = pidList.iterator();
				while(iter.hasNext())
				{
					if(curPid == iter.next())
					{
						//System.out.println("skip curPid = " + curPid);
						isSkip = true;
					}
				}
				if(isSkip) continue;
				else return buf;
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		}

		//return buf;
	}
}
