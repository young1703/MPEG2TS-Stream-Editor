package PsiDemuxer;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import util.MyUtil;
public class PsiTable {
	private int PID;
	public enum PSI_TABLE_TYPE_T {PSI_TABLE_TYPE_EIT, PSI_TABLE_TYPE_NIT, PSI_TABLE_TYPE_PAT, PSI_TABLE_TYPE_PMT};
	private PSI_TABLE_TYPE_T psi_table_type;
	private byte tableId;
	private static int MAX_TABLE_SIZE = 4096;
	private byte[] payload = new byte[MAX_TABLE_SIZE];
	private ArrayList<Integer> seqList = new ArrayList<Integer>();	// table이 출현한 packet number list
	private ArrayList<TableFragmentPacketNumList> FragPacketNumList = new ArrayList<TableFragmentPacketNumList>();

	private long lastInsertedPacketSeq;
	private int packetDelta;
	
	
	public byte[] getPayload() {
		return payload;
	}

	private int lastPos=0; //for next append payload
	private boolean isEmptyPayload = true;
	
	public boolean isEmptyPayload() {
		return isEmptyPayload;
	}

//	
//	protected class SeqList{
//		public ArrayList<Integer> fragSeqList = new ArrayList<Integer>();	// table의 조각이 출현한 packet number list
//		public void add(int input)
//		{
//			fragSeqList.add(input);
//		}
//		public ArrayList<Integer> getFragSeqList() {
//			return fragSeqList;
//		}
//		
//	}
//	
	
	public ArrayList<Integer> getSeqList() {
		return seqList;
	}
	
	public ArrayList<TableFragmentPacketNumList> getFragPacketNumList()
	{
		return FragPacketNumList;
	}

	public PsiTable(int PID, PSI_TABLE_TYPE_T type)
	{
		this.PID = PID;
		this.psi_table_type = type;
	}
	public PsiTable()
	{
		
	}
	
	public byte getTableId()
	{
		return (byte)MyUtil.getValueFromBytes(payload, 0, 8);
	}
	
	public void addSeqNumber(int num)
	{
		seqList.add(num);
	}
	public void addSeqNumList(TableFragmentPacketNumList fragPacketNumList)
	{
		FragPacketNumList.add(fragPacketNumList);
	}
	
	public int getPID() {
		return PID;
	}
	public void setPID(int pID) {
		PID = pID;
	}
	public PSI_TABLE_TYPE_T getPsi_table_type() {
		return psi_table_type;
	}
	public void setPsi_table_type(PSI_TABLE_TYPE_T psi_table_type) {
		this.psi_table_type = psi_table_type;
	}
	
	/**
	 * void appendPayload(byte[] data)
	 * @author jyjy.kim
	 * @param data
	 * @return true - success
	 * @return false - failed
	 * table 객체의 payload에 전달된 byte배열 데이터를 붙인다
	 */
	public boolean appendPayload(byte[] data)
	{
		if(data == null)
		{
			MyUtil.logPrint("appendPayload : null data");
			return false;
		}
		if(lastPos + data.length > MAX_TABLE_SIZE)
		{
			MyUtil.logPrint("length over max table size");
			return false;
		}
		for(int i = 0; i < data.length ; i++ )
		{
			payload[i+lastPos]=data[i];
		}
		lastPos+= data.length;
		this.isEmptyPayload = false;
		return true;
	}
	
	/**
	 * void appendPayload(byte[] data)
	 * @author jyjy.kim
	 * @param data
	 * @return true - success
	 * @return false - failed
	 * table 객체의 payload에 전달된 byte배열 데이터를 붙인다
	 */
	public boolean appendPayload(byte[] data, int startOffset, int endOffset)
	{
		int size = (endOffset - startOffset + 1);
		if(lastPos + size > MAX_TABLE_SIZE)
		{
			MyUtil.logPrint("length over max table size");
			return false;
		}
		for(int i = 0; i < size ; i++ )
		{
			payload[i+lastPos]=data[startOffset + i];
		}
		lastPos+= size;
		
		this.isEmptyPayload = false;
		return true;
	}
	
	public void hexdump()
	{
		MyUtil.hexdump(payload, lastPos);
	}
	
	/**
	 * 
	 * @param otherTable
	 * @return true - same
	 * @return false - different
	 */
	public boolean isSamePayloadWith(PsiTable otherTable)
	{
		if(this.lastPos != otherTable.lastPos)
		{
			//MyUtil.logPrint("different payload size");
			return false;
		}
		
		if(this.PID != otherTable.PID)
		{
			MyUtil.logPrint("different PID");
			return false;
		}
		
		//if(this.tableId != otherTable.tableId)
		//{
			for(int i = 0 ; i < this.lastPos ; i++)
			{
				if(this.payload[i] != otherTable.payload[i])
					return false;
				//else 					continue;
			}
		//}
		
		return true;
	}
	
	public long getStartPacketNum()
	{
		return FragPacketNumList.get(0).getFirstPacketNum();
	}
	public long getLastPacketNum()
	{
		return FragPacketNumList.get(FragPacketNumList.size() - 1).getFirstPacketNum();
	}
	public int getPacketDeltaAverage()
	{
		int deltaSum = 0;
		Iterator<TableFragmentPacketNumList> iter = FragPacketNumList.iterator();
		long current = iter.next().getFirstPacketNum();
		while(iter.hasNext())
		{
			long prev = current;
			current = iter.next().getFirstPacketNum();
			
			deltaSum += (current - prev);
		}
		int ret = 0;
		if(FragPacketNumList.size() > 1)
			ret = deltaSum / (FragPacketNumList.size() - 1);
		
		return ret;
	}
	
	
	/**
	 * 
	 * @param payloadSize
	 * @return
	 * payload를 size에 맞게 잘라서 list로 전달한다.  첫번째 payload에는 pointer field 0가 들어간다.
	 * tsPacket 4byte만큼 비워두고 뒤의 payload만 채워서 보낸다.
	 */
	public ArrayList<byte[]> getTsPacketArrayForInsert(int payloadSize)
	{
		ArrayList<byte[]> ret = new ArrayList<byte[]>();
		
		int count = (this.lastPos ) / payloadSize + 1;
		int curOffset = -1;
		
		for(int i = 0 ; i < count ; i ++)
		{
			byte[] tmp = new byte[payloadSize+4];
			//Arrays.fill(tmp, (byte)0xff);
			int tmpIdx = 4;
			
			if(curOffset == -1)
			{
				tmp[tmpIdx++] = 0x00;
				curOffset++;
			}
			while(tmpIdx < payloadSize + 4)
			{
				if(curOffset+tmpIdx > this.lastPos +4)
					tmp[tmpIdx] = (byte)0xff;
				else
					tmp[tmpIdx] = this.payload[curOffset+tmpIdx - 4 - 1];
				tmpIdx++;
			}

			curOffset += payloadSize;
			ret.add(tmp);
//			MyUtil.logPrint("count = "+count);
//			MyUtil.hexdump(tmp, tmp.length);
		}
		return ret;
	}
	public boolean isTableReadyForSeq(long seq)
	{
		boolean ret = false;
		if(seq >= lastInsertedPacketSeq && seq <= getLastPacketNum() && seq >= getStartPacketNum())
			ret = true;
		
		return ret;
	}
	
	public ArrayList<byte[]> insertTableTsPacketForSeq(long seq, int payloadSize)
	{
		if(isTableReadyForSeq(seq))
		{
			lastInsertedPacketSeq += getPacketDeltaAverage();
			return getTsPacketArrayForInsert(payloadSize);
		}
		return null;
	}
	
	public void extractBinfile()
	{
		String filename = "table_0x" + Integer.toHexString(this.PID) + "_" + getStartPacketNum() + ".bin";
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(filename);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			out.write(payload, 0,this.lastPos);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void setPayloadFromBinFile(String path)
	{
		FileInputStream in = null;
		
		try {
			in = new FileInputStream(path);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		byte buf;
		this.lastPos = 0;
		try {
			while((buf = (byte)in.read()) != -1 )
			{
				this.payload[lastPos++] = buf;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public void initInsertTable()
	{
		lastInsertedPacketSeq = getStartPacketNum();
		packetDelta = getPacketDeltaAverage();
	}

	public long getLastInsertedPacketSeq() {
		return lastInsertedPacketSeq;
	}

	public void setLastInsertedPacketSeq(long lastInsertedPacketSeq) {
		this.lastInsertedPacketSeq = lastInsertedPacketSeq;
	}
	
//	/**
//	 * 
//	 * @param payloadSize
//	 * @return
//	 */
//	public ArrayList<byte[]> getNextPayloadForInsert(int payloadSize)
//	{
//		ArrayList<byte[]> ret = new ArrayList<byte[]>();
//		
//		int count = this.lastPos / payloadSize + 1;
//		int curOffset = 0;
//		for(int i = 0 ; i < count ; i ++)
//		{
//			byte[] tmp = new byte[payloadSize];
//			int tmpIdx = 0;
//			for(int j = curOffset ; j < curOffset + payloadSize ; j++)
//			{
//				if(curOffset + tmpIdx >= this.lastPos )
//					tmp[tmpIdx] = (byte)0xff;
//				else
//					tmp[tmpIdx] = this.payload[curOffset+tmpIdx];
//				tmpIdx++;
//			}
//			
//			curOffset += payloadSize;
//			ret.add(tmp);
//			MyUtil.hexdump(tmp, payloadSize);
//		}
//		return ret;
//	}
	
	
}

