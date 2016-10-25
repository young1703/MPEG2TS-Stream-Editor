package PsiDemuxer;
import mpeg2ts.*;
import util.MyUtil;
import util.SimpleTableMaker;

import java.util.ArrayList;
import java.util.Iterator;

//import PsiTableSyntax.PsiNit;
import PsiTableSyntax.PsiEit;

import java.util.HashMap;

public class PsiTableList {
	private int packet_size = 188;
	ArrayList<Integer> pidList = new ArrayList<Integer>(10);
	//PsiTable curTable;// = new PsiTable();
	HashMap<Integer, PsiTable> curTableMap							= new HashMap<Integer, PsiTable>();
	HashMap<Integer, TableFragmentPacketNumList> curPacketNumMap 	= new HashMap<Integer, TableFragmentPacketNumList>();
	HashMap<Integer, Integer> nextContinuityCounterMap 				= new HashMap<Integer, Integer>();
	
	ArrayList<PsiTable> arrayPsiTable = new ArrayList<PsiTable>();
	
	
	
	/**
	 * constructor
	 * 기본으로 사용할 pid list를 입력
	 */
	public PsiTableList()
	{
		//curTableMap =  new HashMap<Integer, PsiTable>();
		// 걸러낼 pid들 등록
		//addPidFilter(0x0);
		addPidFilter(0x12);
		//addPidFilter(0x1);
		//addPidFilter(0x2);
		
		//pidList.add(0x0);
	}
	
	public void addPidFilter(int pid)
	{
		pidList.add(pid);
		curTableMap.put(pid, new PsiTable());
		curPacketNumMap.put(pid, new TableFragmentPacketNumList());
		nextContinuityCounterMap.put(pid, 0);
		
	}
	
	public boolean inputPacket(byte[] packet, long packetSeq)
	{
		// TODO : packet 하나 들어왔을 때 동작
		TSPacket tsPacket = new TSPacket();
		tsPacket.parseTsPacket(packet);
		//tsPacket.print();
		Iterator<Integer> iter = pidList.iterator();
		while(iter.hasNext())
		{
			if (tsPacket.getPID() == iter.next()) // find filtered PID
			{
				int curPid = tsPacket.getPID();
//				MyUtil.logPrint("pid 0x" + Integer.toHexString(curPid) + " matched!");
//				MyUtil.hexdump(packet, packet.length);
				if(tsPacket.getPayload_unit_start_indicator() == 1) // pointer_field가 존재한다. 이전 payload가 마무리 되었음을 의미
				{
					// 이전 payload 버퍼를 처리해야 한다.
					if(tsPacket.getPointerField() == 0) // 단독 payload 
					{
						if(!curTableMap.get(curPid).isEmptyPayload()) //완성 된 payload를 전달
						{
							//MyUtil.logPrint("handle last table payload");
							
							// table을 처리하고 나서 list에 새로운 table object를 추가해 줘야 한다.
							curTableMap.get(curPid).setPID(curPid);
							inputTable(curTableMap.get(curPid), curPacketNumMap.get(curPid));
							
							curTableMap.put(curPid, new PsiTable());
							curPacketNumMap.put(curPid, new TableFragmentPacketNumList());
							

						}
						//MyUtil.logPrint("payload only");
						curTableMap.get(curPid).appendPayload(tsPacket.getPayload(), 1, tsPacket.getPayload().length-1);
						curPacketNumMap.get(curPid).addPacketNum(packetSeq);
					}
					else if(tsPacket.getPointerField() > 0 ) //이전 payload의 마지막, 다음 payload의 시작
					{
						//MyUtil.logPrint("last payload + new payload");
//						curTableMap.get(curPid).appendPayload(tsPacket.getPayload(), 1, tsPacket.getPointerField() - 1);
						curTableMap.get(curPid).appendPayload(tsPacket.getPayload(), 1, tsPacket.getPointerField() );
						curPacketNumMap.get(curPid).addPacketNum(packetSeq);
						
						if(!curTableMap.get(curPid).isEmptyPayload()) //완성 된 payload를 전달
						{
							//MyUtil.logPrint("handle last table payload");
							
							// table을 처리하고 나서 list에 새로운 table object를 추가해 줘야 한다.
							curTableMap.get(curPid).setPID(curPid);

							
							inputTable(curTableMap.get(curPid), curPacketNumMap.get(curPid));
							
							curTableMap.put(curPid, new PsiTable());
							curPacketNumMap.put(curPid, new TableFragmentPacketNumList());
						}

//						curTableMap.get(curPid).appendPayload(tsPacket.getPayload(), tsPacket.getPointerField(), tsPacket.getPayload().length-1);
						curTableMap.get(curPid).appendPayload(tsPacket.getPayload(), tsPacket.getPointerField() + 1, tsPacket.getPayload().length-1);
						curPacketNumMap.get(curPid).addPacketNum(packetSeq);
					
					}
				}
				else if(tsPacket.getPayload_unit_start_indicator() == 0) // pointer field가 존재하지 않는다. 이전 payload의 연속
				{
					//MyUtil.logPrint("continued payload only");
					curTableMap.get(curPid).appendPayload(tsPacket.getPayload());
					curPacketNumMap.get(curPid).addPacketNum(packetSeq);
				}
			}
			else
			{
				//MyUtil.logPrint("skip : filter is not set for pid ");
			}
		}
		
		return true;
	}

	/**
	 * 조합 완료 된 table을 pid, payload, table_id 별로 구분하여 sequence number 정보를 기록한다.
	 * @param table
	 * @return
	 *//*
	public boolean inputTable(PsiTable table, int seqNum)
	{
		//MyUtil.logPrint("###table complete!!###");
		//table.hexdump();
		//MyUtil.logPrint("######################\n");
		boolean found = false;
		//중복 테이블 탐색
		for(int i = 0 ; i < arrayPsiTable.size(); i++) 
		{
			//중복이 있을 경우 seq number만 기록
			if(arrayPsiTable.get(i).isSamePayloadWith(table))
			{
				arrayPsiTable.get(i).addSeqNumber(seqNum);
				found = true;
			}
		}
		if(!found) //중복이 없을 경우 새로운 list 생성
		{
			table.addSeqNumber(seqNum);
			arrayPsiTable.add(table);
		}

		return true;
	}
	*/
	public boolean inputTable(PsiTable table, TableFragmentPacketNumList fragPacketNumList)
	{
		boolean found = false;
		//중복 테이블 탐색
		for(int i = 0 ; i < arrayPsiTable.size(); i++) 
		{
			//중복이 있을 경우 seq number만 기록
			if(arrayPsiTable.get(i).isSamePayloadWith(table))
			{
//				arrayPsiTable.get(i).addSeqNumber(seqNum);
				arrayPsiTable.get(i).addSeqNumList(fragPacketNumList);
				found = true;
			}
		}
		if(!found) //중복이 없을 경우 새로운 list 생성
		{
//			table.addSeqNumber(seqNum);
			table.addSeqNumList(fragPacketNumList);
			arrayPsiTable.add(table);
		}
		
		return true;
		
	}
	
	public void printTableList()
	{
		
		MyUtil.logPrint("### start print table list ###");
		MyUtil.logPrint("# of all table : = " + arrayPsiTable.size() );
		
		for(int i = 0 ; i < arrayPsiTable.size(); i++) 
		{
			String result = "";
			MyUtil.logPrint(i + " table print start====="); 
			arrayPsiTable.get(i).hexdump();
			for(int j = 0 ; j < arrayPsiTable.get(i).getFragPacketNumList().size() ; j++)
			{
				//result +=  arrayPsiTable.get(i).getSeqList().get(j);
				result +=  arrayPsiTable.get(i).getFragPacketNumList().get(j).getNumListString();
				result += '\t';
			}
			MyUtil.logPrint(result);
			MyUtil.logPrint("Start Packet Number 	: " + arrayPsiTable.get(i).getStartPacketNum());
			MyUtil.logPrint("Last Packet Number 	: " + arrayPsiTable.get(i).getLastPacketNum());
			MyUtil.logPrint("Packet delta 			: " + arrayPsiTable.get(i).getPacketDeltaAverage());
			
			// TODO : 테스트 code 바이너리 추출 삭제할 것
			//arrayPsiTable.get(i).extractBinfile();
			
			
			//test code for eit
			if(false)
			//if(arrayPsiTable.get(i).getPID() == 0x12)
			{
				PsiEit temp = new PsiEit();
				temp.parseEit(arrayPsiTable.get(i).getPayload(), 0);
				temp.printEit();
			}
			//arrayPsiTable.get(i).getTsPacketArrayForInsert(184);
			MyUtil.logPrint(i + " table print end=====\n\n\n"); 
			
		}
		
		//arrayPsiTable.get(0).setPayloadFromBinFile("table_0x12_850.bin");
		//arrayPsiTable.get(0).hexdump();
	}
	
	public void testChangeTable()
	{
		for(int i = 0 ; i < arrayPsiTable.size(); i++) 
		{
			if(arrayPsiTable.get(i).getStartPacketNum() == 65587)
			{
				arrayPsiTable.get(i).setPayloadFromBinFile("table_0x12_65587_crid_add.bin");
				System.out.println("table_0x12_65587 changed!\n");
			}
			if(arrayPsiTable.get(i).getStartPacketNum() == 7968865)
			{
				arrayPsiTable.get(i).setPayloadFromBinFile("table_0x12_7968865_crid_add.bin");
				System.out.println("table_0x12_7968865 changed!\n");
			}
			if(arrayPsiTable.get(i).getStartPacketNum() == 9416323)
			{
				arrayPsiTable.get(i).setPayloadFromBinFile("table_0x12_9416323_crid_add.bin");
				System.out.println("table_0x12_9416323 changed!\n");
			}

		
		}
	}
	
	public ArrayList<byte[]> getInsertTableTsPacketForSeq(long seq, int payloadSize)
	{
		
		//ArrayList<byte[]> ret = null;
		
		Iterator<PsiTable> PsiTableIter = arrayPsiTable.iterator();
		PsiTable tempTable;
		
		while(PsiTableIter.hasNext())
		{
			ArrayList<byte[]> tempPayloadArrayList = null;
			tempTable = PsiTableIter.next();
			if(( tempPayloadArrayList = tempTable.insertTableTsPacketForSeq(seq, payloadSize) ) != null)
			{
				Iterator<byte[]> tsPacketListIter = tempPayloadArrayList.iterator();
				boolean first = true;
				while(tsPacketListIter.hasNext())
				{
					byte[] tsPacketHeader = new byte[4];
					tsPacketHeader = getTableTsPacketHeader(tempTable.getPID(), first?1:0 ,nextContinuityCounterMap.get(tempTable.getPID()));
					
					System.arraycopy(tsPacketHeader, 0, tsPacketListIter.next(), 0, 4);

					first=false;
					int nextContinuityCounter = nextContinuityCounterMap.get(tempTable.getPID()) + 1;
					if(nextContinuityCounter == 16) nextContinuityCounter = 0; //continuity counter 순환
					nextContinuityCounterMap.put(tempTable.getPID(), nextContinuityCounter );

				}
				return tempPayloadArrayList;
			}
		}
		return null;
	}
	
	public byte[] getTableTsPacketHeader(int pid, int startUnitIndicator, int count )
	{
		byte[] ret = new byte[4];
		
		SimpleTableMaker simpleTableMkr = new SimpleTableMaker();
		
		simpleTableMkr.writeValueWithBitSize(8, 0x47);
		simpleTableMkr.writeValueWithBitSize(1, 0);
		simpleTableMkr.writeValueWithBitSize(1, startUnitIndicator);
		simpleTableMkr.writeValueWithBitSize(1, 0);
		simpleTableMkr.writeValueWithBitSize(13, pid);
		simpleTableMkr.writeValueWithBitSize(2, 0);
		simpleTableMkr.writeValueWithBitSize(2, 0x1);
		simpleTableMkr.writeValueWithBitSize(4, count);
		
		//simpleTableMkr.hexdump();
		
		ret[0] = simpleTableMkr.getByteAt(0);
		ret[1] = simpleTableMkr.getByteAt(1);
		ret[2] = simpleTableMkr.getByteAt(2);
		ret[3] = simpleTableMkr.getByteAt(3);
		
//		this.sync_byte						= (int)MyUtil.getValueFromBytes(buf, offset, 8); 	offset += 8;
//		this.transport_error_indicator		= (int)MyUtil.getValueFromBytes(buf, offset, 1); 	offset += 1;
//		this.payload_unit_start_indicator 	= (int)MyUtil.getValueFromBytes(buf, offset, 1); 	offset += 1;
//		this.transport_priority 			= (int)MyUtil.getValueFromBytes(buf, offset, 1); 	offset += 1;
//		this.PID							= (int)MyUtil.getValueFromBytes(buf, offset, 13); 	offset += 13;
//		// 0x0000 : PAT, 0x0001 : CAT, 0x0002 : Transport Stream Description Table, 0x0003-0x000f:Rsvd, 0x1FFF : Null packet
//		this.transport_scrambling_control	= (int)MyUtil.getValueFromBytes(buf, offset, 2); 	offset += 2;
//		this.adaptation_field_control		= (int)MyUtil.getValueFromBytes(buf, offset, 2); 	offset += 2;
//		this.continuity_counter				= (int)MyUtil.getValueFromBytes(buf, offset, 4); 	offset += 4;
		return ret;
	}
	
	public void initInsertTableList()
	{
		Iterator<PsiTable> iter = arrayPsiTable.iterator();
		while(iter.hasNext())
		{
			iter.next().initInsertTable();
		}
	}
	
	/**
	 * ts packet size를 설정한다 (보통 188 byte)
	 * @return
	 */
	public int getPacket_size() {
		return packet_size;
	}



	/**
	 * ts packet size를 설정한다 (보통 188 byte)
	 * @return
	 */
	public void setPacket_size(int packet_size) {
		this.packet_size = packet_size;
	}
	
	
	
}
