package controller;
import PsiDemuxer.PsiTable.*;
import PsiTableSyntax.PsiEit;
import mpeg2ts.TSFileController;
import mpeg2ts.TSFileMaker;
import mpeg2ts.TSPacket;
import util.MyUtil;
import util.SimpleTableMaker;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Queue;
import java.util.LinkedList;


import PsiDemuxer.*;

//import PsiDemuxer.PsiTable.PSI_TABLE_TYPE_T;

public class Main {
	public static void main(String[] args)
	{
		
		TSFileController tsFile = new TSFileController();
		//String inputFilename = "v:\\mux1-cp.ts";
		String inputFilename = "v:\\CRID_1_7.00.ts";
		//tsFile.openTsFile("v:\\CRID_1_7.00.ts");
		//tsFile.openTsFile("v:\\[NL_62_64_Ziggo]DVBC_120719_TS18_Freq369_NIT9003_Ziggo_live_network.ts");
		tsFile.openTsFile(inputFilename);
		
		TSFileMaker output = new TSFileMaker();
		output.openTsFile("v:\\result.ts");
//		
//		PsiTable aTable = new PsiTable(0x12, PSI_TABLE_TYPE_T.PSI_TABLE_TYPE_EIT);
//		PsiTable bTable = new PsiTable(0x12, PSI_TABLE_TYPE_T.PSI_TABLE_TYPE_EIT);
//		aTable.appendPayload(tsFile.getNextPacket());
//		aTable.appendPayload(tsFile.getNextPacket());
//		bTable.appendPayload(tsFile.getNextPacket());
//		aTable.hexdump();
//		bTable.hexdump();
//		
//		PsiTableList tableList = new PsiTableList();
//		tableList.inputPacket(tsFile.getNextPacket(), 0);
//		
//		System.out.println(aTable.isSamePayloadWith(aTable));

if(true){		
		PsiTableList tableList = new PsiTableList();
		byte[] packet;
		long i = 0;
		while((packet = tsFile.getNextPacket()) != null)
		{
			tableList.inputPacket(packet, i);
			//MyUtil.logPrint("packet #"+i);
			i++;
//			System.out.println("packet #"+i);
		}
		//tableList.printTableList();
		tableList.testChangeTable();
		
		
		long j = 0;
		ArrayList<Integer> skipPidList = new ArrayList<Integer>();
		skipPidList.add(0x12);
		
		
		tableList.initInsertTableList();
//		while((packet = tsFile.getNextPacket()) != null)
		Queue<byte[]> tableTsPacketQueue = new LinkedList<byte[]>();
		ArrayList<byte[]> tempTableFragList;
		
		
		tsFile.openTsFile(inputFilename);
		while((packet = tsFile.getNextPacketWithoutPids(skipPidList)) != null)
		{
			//MyUtil.logPrint("packet #"+i);
			
			//System.out.println("packet #"+j);
			j++;
			if(!tableTsPacketQueue.isEmpty())
			{
				//table queue에서 꺼내서 처리
				//System.out.println("packet #"+j);
				j++;
				output.writeNextPacket(tableTsPacketQueue.remove());
			}
			
			if( (tempTableFragList = tableList.getInsertTableTsPacketForSeq(j, 184)) != null)
			{
				Iterator<byte[]> iter = tempTableFragList.iterator();
				while(iter.hasNext())
				{
					tableTsPacketQueue.add(iter.next());
				}
				
			}
			output.writeNextPacket(packet);
			
		}
		
		
		
		//tableList.printTableList();
		
		
		tsFile.closeTsFile();
		output.closeTsFile();
}
/*		
		byte[] test ={	0x4f, 	(byte) 0xf1, 	0x06, 	0x32, 	0x42, 	(byte) 0xf3, 	0x01, 	0x01, 	0x30, 	0x02, 	0x23, 	0x3a, 	0x01, 	0x4f, 	0x5f, 	(byte) 0xff, 
				(byte) 0xd5, 	(byte) 0x82, 	0x16, 	0x30, 	0x00, 	0x00, 	0x30, 	0x00, 	0x20, 	(byte) 0xeb, 	0x54, 	0x02, 	(byte) 0xf0, 	0x00, 	0x50, 	0x06, 
				(byte) 0xf1, 	0x04, 	0x04, 	0x65, 	0x6e, 	0x67, 	0x50, 	0x06, 	(byte) 0xf2, 	0x03, 	(byte) 0x8e, 	0x65, 	0x6e, 	0x67, 	0x4d, 	(byte) 0xc2, 
				0x65, 	0x6e, 	0x67, 	0x0a, 	0x4e, 	0x65, 	0x69, 	0x67, 	0x68, 	0x62, 	0x6f, 	0x75, 	0x72, 	0x73, 	(byte) 0xb3, 	0x46, 
				0x72, 	0x61, 	0x7a, 	0x65, 	0x72, 	0x20, 	0x69, 	0x73, 	0x20, 	0x64, 	0x65, 	0x76, 	0x61, 	0x73, 	0x74, 	0x61, 
				0x74, 	0x65, 	0x64, 	0x20, 	0x74, 	0x6f, 	0x20, 	0x6c, 	0x65, 	0x61, 	0x72, 	0x6e, 	0x20, 	0x74, 	0x68, 	0x61, 
				0x74, 	0x20, 	0x50, 	0x72, 	0x75, 	0x65, 	0x20, 	0x68, 	0x61, 	0x73, 	0x20, 	0x6e, 	0x6f, 	0x74, 	0x20, 	0x66, 
				0x6f, 	0x72, 	0x67, 	0x69, 	0x76, 	0x65, 	0x6e, 	0x20, 	0x68, 	0x69, 	0x6d, 	0x20, 	0x66, 	0x6f, 	0x72, 	0x20, 
				0x74, 	0x68, 	0x65, 	0x20, 	0x70, 	0x61, 	0x73, 	0x74, 	0x2e, 	0x20, 	0x45, 	0x6c, 	0x6c, 	0x65, 	0x20, 	0x73, 
				0x74, 	0x72, 	0x75, 	0x67, 	0x67, 	0x6c, 	0x65, 	0x73, 	0x20, 	0x74, 	0x6f, 	0x20, 	0x6b, 	0x65, 	0x65, 	0x70, 
				0x20, 	0x50, 	0x61, 	0x75, 	0x6c, 	0x27, 	0x73, 	0x20, 	0x69, 	0x6e, 	0x66, 	0x69, 	0x64, 	0x65, 	0x6c, 	0x69, 
				0x74, 	0x79, 	0x20, 	0x66, 	0x72, 	0x6f, 	0x6d, 	0x20, 	0x52, 	0x65, 	0x62, 	0x65, 	0x63, 	0x63, 	0x61, 	0x2e, 
				0x20, 	0x53, 	0x74, 	0x65, 	0x70, 	0x68, 	0x20, 	0x64, 	0x69, 	0x73, 	0x63, 	0x6f, 	0x76, 	0x65, 	0x72, 	0x73, 
				0x20, 	0x61, 	0x20, 	0x73, 	0x68, 	0x6f, 	0x63, 	0x6b, 	0x69, 	0x6e, 	0x67, 	0x20, 	0x74, 	0x72, 	0x75, 	0x74, 
				0x68, 	0x20, 	0x61, 	0x62, 	0x6f, 	0x75, 	0x74, 	0x20, 	0x54, 	0x79, 	0x2e, 	0x20, 	0x5b, 	0x41, 	0x44, 	0x2c, 
				0x53, 	0x5d, 	0x76, 	0x08, 	(byte) 0xc4, 	0x06, 	0x2f, 	0x56, 	0x34, 	0x4a, 	0x45, 	0x31, 	0x76, 	0x07, 	(byte) 0xc8, 	0x05, 
				0x2f, 	0x52, 	0x39, 	0x4a, 	0x4f, 	0x32, 	0x15, 	0x01, 	(byte) 0xa2 	};						

		PsiEit temp = new PsiEit();
		temp.parseEit(test, 0);
		temp.printEit();
*/
//		System.out.println("0x"+Integer.toHexString(MyUtil.getFileMpeg2Crc32WithOriginal("table_0x12_7968865.bin")));
		
		System.out.println("end");
		
	}

	public static void main1(String[] args)
	{
		PsiTableList test = new PsiTableList();
		
		test.getTableTsPacketHeader(0x12, 1, 10);
		
	}
	public static void main6(String[] args)
	{
		System.out.println("0x"+Integer.toHexString(MyUtil.getFileMpeg2Crc32WithOriginal("table_0x12_65587_crid_add.bin")));
		System.out.println("0x"+Integer.toHexString(MyUtil.getFileMpeg2Crc32WithOriginal("table_0x12_7968865_crid_add.bin")));
		System.out.println("0x"+Integer.toHexString(MyUtil.getFileMpeg2Crc32WithOriginal("table_0x12_9416323_crid_add.bin")));

		
	}
	public static void main5(String[] args)
	{
		TSFileController tsFile = new TSFileController();
		//tsFile.openTsFile("v:\\CRID_1_7.00.ts");
		//tsFile.openTsFile("v:\\[NL_62_64_Ziggo]DVBC_120719_TS18_Freq369_NIT9003_Ziggo_live_network.ts");
		tsFile.openTsFile("v:\\mux1-cp.ts");
		PsiTableList tableList = new PsiTableList();
		byte[] packet;
		long i = 0;
		while((packet = tsFile.getNextPacket()) != null)
		{
			tableList.inputPacket(packet, i);
			i++;
		}
		
		tableList.printTableList();
		tableList.initInsertTableList();
		
		int j = 0;
		while(j <= i)
		{
			ArrayList<byte[]> temp;
			
			if((temp = tableList.getInsertTableTsPacketForSeq(j, 184)) != null)
			{
				MyUtil.logPrint("hit table :" + j);
				System.out.println("hit table :" + j);
				int p;
				for(p = 0 ; p < temp.size(); p++)
				{
					MyUtil.logPrint("fragment :" + p);
					MyUtil.hexdump(temp.get(p), temp.get(p).length);
				}
				MyUtil.logPrint("\n");
			}
			j++;
		}
	}
	public static void main10(String[] args)
	{
		PsiEit eit = new PsiEit();
		
		eit.parseFromBinFile("table_0x12_65587.bin");
		eit.parseFromBinFile("table_0x12_7968865.bin");
		eit.parseFromBinFile("table_0x12_9416323.bin");
		
		
	}
}
