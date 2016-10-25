package PsiTableSyntax;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import PsiDescriptors.PsiDescriptor;
import util.MyUtil;
import java.util.Iterator;

public class PsiEit {
	private int table_id;
	private int section_syntax_indicator;
	private int reserved_future_use;
	private int reserved;
	private int section_length;
	private int service_id;
	private int reserved2;
	private int version_number;
	private int current_next_indicator;
	private int section_number;
	private int last_section_number;
	private int transport_stream_id;
	private int original_network_id;
	private int segment_last_section_number;
	private int last_table_id;

	//inner loop list
	ArrayList<Loop> loopList = new ArrayList<Loop>();
	
	private int CRC_32;

	public void parseEit(byte[] buf, int startByte)
	{
		int offset					 		= startByte * 8;
		
		this.table_id					 	= (int)MyUtil.getValueFromBytes(buf, offset, 8);  offset += 8;
		this.section_syntax_indicator 		= (int)MyUtil.getValueFromBytes(buf, offset, 1);  offset += 1;
		this.reserved_future_use 			= (int)MyUtil.getValueFromBytes(buf, offset, 1);  offset += 1;
		this.reserved 						= (int)MyUtil.getValueFromBytes(buf, offset, 2);  offset += 2;
		this.section_length 				= (int)MyUtil.getValueFromBytes(buf, offset, 12);  offset += 12;
		this.service_id 					= (int)MyUtil.getValueFromBytes(buf, offset, 16);  offset += 16;
		this.reserved2 						= (int)MyUtil.getValueFromBytes(buf, offset, 2);  offset += 2;
		this.version_number 				= (int)MyUtil.getValueFromBytes(buf, offset, 5);  offset += 5;
		this.current_next_indicator 		= (int)MyUtil.getValueFromBytes(buf, offset, 1);  offset += 1;
		this.section_number 				= (int)MyUtil.getValueFromBytes(buf, offset, 8);  offset += 8;
		this.last_section_number 			= (int)MyUtil.getValueFromBytes(buf, offset, 8);  offset += 8;
		this.transport_stream_id 			= (int)MyUtil.getValueFromBytes(buf, offset, 16);  offset += 16;
		this.original_network_id 			= (int)MyUtil.getValueFromBytes(buf, offset, 16);  offset += 16;
		this.segment_last_section_number 	= (int)MyUtil.getValueFromBytes(buf, offset, 8);  offset += 8;
		this.last_table_id					= (int)MyUtil.getValueFromBytes(buf, offset, 8);  offset += 8;
		

		//handle loop : section_length 소모하면서 parsing 해야 함.
		int remain = this.section_length - 11 /*위에까지의 사용한 bit*/ - 4 /*CRC*/;
		int byteLen = 14; //다음 시작 byte
		while(remain > 0)
		{
			int parseLen = 0;
			Loop tempLoop = new Loop();
			parseLen = tempLoop.parseLoop(buf, byteLen);
			if(parseLen > 0)
			{
				loopList.add(tempLoop);
			}
			remain -= parseLen;
			byteLen += parseLen;

		}
		offset = byteLen *8;
		
		
		this.CRC_32 = (int)MyUtil.getValueFromBytes(buf, offset, 32);  offset += 32;
		
	}
	
	public void printEit()
	{
		
		MyUtil.logPrint("=== start print EIT ===");
		MyUtil.logPrint("table_id     = 0x" + Integer.toHexString(table_id));
		MyUtil.logPrint("section_syntax_indicator     = 0x" + Integer.toHexString(section_syntax_indicator));
		MyUtil.logPrint("reserved_future_use     = 0x" + Integer.toHexString(reserved_future_use));
		MyUtil.logPrint("reserved     = 0x" + Integer.toHexString(reserved));
		MyUtil.logPrint("section_length     = 0x" + Integer.toHexString(section_length));
		MyUtil.logPrint("service_id     = 0x" + Integer.toHexString(service_id));
		MyUtil.logPrint("reserved2     = 0x" + Integer.toHexString(reserved2));
		MyUtil.logPrint("version_number     = 0x" + Integer.toHexString(version_number));
		MyUtil.logPrint("current_next_indicator     = 0x" + Integer.toHexString(current_next_indicator));
		MyUtil.logPrint("section_number     = 0x" + Integer.toHexString(section_number));
		MyUtil.logPrint("last_section_number     = 0x" + Integer.toHexString(last_section_number));
		MyUtil.logPrint("transport_stream_id     = 0x" + Integer.toHexString(transport_stream_id));
		MyUtil.logPrint("original_network_id     = 0x" + Integer.toHexString(original_network_id));
		MyUtil.logPrint("segment_last_section_number     = 0x" + Integer.toHexString(segment_last_section_number));
		MyUtil.logPrint("last_table_id     = 0x" + Integer.toHexString(last_table_id));

		Iterator<Loop> iter = loopList.iterator();
		while(iter.hasNext())
		{
			iter.next().printLoop();
		}
		
		MyUtil.logPrint("CRC_32     = 0x" + Integer.toHexString(CRC_32));

	}
	
	public void parseFromBinFile(String path)
	{
		FileInputStream in = null;
		byte[] tableBuf = new byte[4096];
		
		try {
			in = new FileInputStream(path);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		byte buf;
		int lastPos = 0;
		try {
			while((buf = (byte)in.read()) != -1 )
			{
				tableBuf[lastPos++] = buf;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		parseEit(tableBuf, 0);
		printEit();
		
	}
	
	private class Loop {
		private int original_network_id;
		private double start_time;
		private int duration;
		private int running_status;
		private int free_CA_mode;
		private int descriptors_loop_length;
		
		//descriptor list
		ArrayList<PsiDescriptor> descList = new ArrayList<PsiDescriptor>();
		
		public int parseLoop(byte[] buf, int startByte)
		{
			int offset = startByte * 8;
			int totalLen = 0;
			
			this.original_network_id 			= (int)MyUtil.getValueFromBytes(buf, offset, 16);  offset += 16;
			this.start_time 					= (double)MyUtil.getValueFromBytes(buf, offset, 40);  offset += 40;
			this.duration 						= (int)MyUtil.getValueFromBytes(buf, offset, 24);  offset += 24;
			this.running_status 				= (int)MyUtil.getValueFromBytes(buf, offset, 3);  offset += 3;
			this.free_CA_mode 					= (int)MyUtil.getValueFromBytes(buf, offset, 1);  offset += 1;
			this.descriptors_loop_length 		= (int)MyUtil.getValueFromBytes(buf, offset, 12);  offset += 12;
			
//			System.out.println("this.original_network_id 			=" + Integer.toHexString(this.original_network_id) 	 );
//            System.out.println("this.start_time 			        =" + Double.toHexString(this.start_time 			 ));
//            System.out.println("this.duration 				        =" + Integer.toHexString(this.duration 				 ));
//            System.out.println("this.running_status 		        =" + Integer.toHexString(this.running_status 		 ));
//            System.out.println("this.free_CA_mode 			        =" + Integer.toHexString(this.free_CA_mode 			 ));
//            System.out.println("this.descriptors_loop_length       =" + Integer.toHexString(this.descriptors_loop_length));
            
			totalLen += 12;
			
			int remain = this.descriptors_loop_length;
			int descStartByte = startByte + 12 ;
			totalLen += this.descriptors_loop_length;
			
			while(remain > 0)
			{
				PsiDescriptor temp = new PsiDescriptor();
				int parseLen = temp.parsePsiDescriptor(buf, descStartByte);
//				System.out.println("parseLen = " + parseLen);
				if(parseLen > 0)
					descList.add(temp);
				descStartByte += parseLen;
				remain -= parseLen;
			}
			
			return totalLen;

		}
		
		public void printLoop()
		{
			MyUtil.logPrint("original_network_id     = 0x" + Integer.toHexString(original_network_id));
			MyUtil.logPrint("start_time     = 0x" + Double.toHexString(start_time));
			MyUtil.logPrint("duration     = 0x" + Integer.toHexString(duration));
			MyUtil.logPrint("running_status     = 0x" + Integer.toHexString(running_status));
			MyUtil.logPrint("free_CA_mode     = 0x" + Integer.toHexString(free_CA_mode));
			MyUtil.logPrint("descriptors_loop_length     = 0x" + Integer.toHexString(descriptors_loop_length));
			
			//descriptor loop print
			Iterator<PsiDescriptor> iter = descList.iterator();
			while(iter.hasNext())
			{
				iter.next().printDescriptor();
			}

		}

	}
	
	
	
	
}
