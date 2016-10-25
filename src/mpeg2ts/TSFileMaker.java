package mpeg2ts;


import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import util.*;

public class TSFileMaker {
	BufferedOutputStream bos;
	int aPacketSize = 188;
	
	public boolean openTsFile(String path)
	{
		try {
			bos = new BufferedOutputStream(new FileOutputStream(path));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}
	
	public boolean writeNextPacket(byte[] packet)
	{
		if(packet.length != aPacketSize)
		{
			MyUtil.logPrint("writeNextPacket Fail : length != aPacketSize");
		}
		
		try {
			bos.write(packet, 0, aPacketSize);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}
	
	public boolean closeTsFile() 
	{
		try {
			bos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
