package PsiTableSyntax;

import util.*;

import java.util.ArrayList;
import java.util.Iterator;

import PsiDescriptors.*;
public class PsiNit {
	private int table_id;
	private int section_syntax_indicator;
	private int reserved_future_use;
	private int reserved;
	private int section_length;
	private int network_id;
	private int reserved2;
	private int version_number;
	private int current_next_indicator;
	private int section_number;
	private int last_section_number;
	private int reserved_future_use2;
	private int network_descriptors_length;
	
	private ArrayList<PsiDescriptor> networkDescArrayList = new ArrayList<PsiDescriptor>();
	
	private int reserved_future_use3;
	private int transport_stream_loop_length;
	private ArrayList<PsiDescriptor> TransportDescArrayList = new ArrayList<PsiDescriptor>();

	private int CRC_32;

	public boolean parseNit(byte[] buf)
	{
		int offset = 0;
		
		this.table_id 					= (int)MyUtil.getValueFromBytes(buf, offset, 8);  offset += 8;
		this.section_syntax_indicator 	= (int)MyUtil.getValueFromBytes(buf, offset, 1);  offset += 1;
		this.reserved_future_use 		= (int)MyUtil.getValueFromBytes(buf, offset, 1);  offset += 1;
		this.reserved 					= (int)MyUtil.getValueFromBytes(buf, offset, 2);  offset += 2;
		this.section_length 			= (int)MyUtil.getValueFromBytes(buf, offset, 12);  offset += 12;
		this.network_id 				= (int)MyUtil.getValueFromBytes(buf, offset, 16);  offset += 16;
		this.reserved 					= (int)MyUtil.getValueFromBytes(buf, offset, 2);  offset += 2;
		this.version_number 			= (int)MyUtil.getValueFromBytes(buf, offset, 5);  offset += 5;
		this.current_next_indicator 	= (int)MyUtil.getValueFromBytes(buf, offset, 1);  offset += 1;
		this.section_number 			= (int)MyUtil.getValueFromBytes(buf, offset, 8);  offset += 8;
		this.last_section_number 		= (int)MyUtil.getValueFromBytes(buf, offset, 8);  offset += 8;
		this.reserved_future_use 		= (int)MyUtil.getValueFromBytes(buf, offset, 4);  offset += 4;
		this.network_descriptors_length = (int)MyUtil.getValueFromBytes(buf, offset, 12);  offset += 12;
		
		
		//desc loop
		int remain = this.network_descriptors_length;
		int startByte = 11;
		do
		{
			PsiDescriptor temp = new PsiDescriptor();
			int parseLen = temp.parsePsiDescriptor(buf, startByte);
			if(parseLen < 0)
				networkDescArrayList.add(temp);
			startByte += parseLen;
			remain -= parseLen;
		} while (remain > 0);
		
		this.reserved_future_use = (int)MyUtil.getValueFromBytes(buf, offset, 4);  offset += 4;
		this.transport_stream_loop_length = (int)MyUtil.getValueFromBytes(buf, offset, 12);  offset += 12;
		
		offset += (this.transport_stream_loop_length * 8);
/*	skip!!
		for(i=0;i<N;i++){
			transport_stream_id 16 uimsbf
			original_network_id 16 uimsbf
			reserved_future_use 4 bslbf
			transport_descriptors_length 12 uimsbf
			for(j=0;j<N;j++){
				descriptor()
			}
		}
*/
		this.CRC_32 = (int)MyUtil.getValueFromBytes(buf, offset, 32);  offset += 32;

		return true;
	}
	
	public void printNit()
	{
		MyUtil.logPrint("table_id     = 0x" + Integer.toHexString(table_id));
		MyUtil.logPrint("section_syntax_indicator     = 0x" + Integer.toHexString(section_syntax_indicator));
		MyUtil.logPrint("reserved_future_use     = 0x" + Integer.toHexString(reserved_future_use));
		MyUtil.logPrint("reserved     = 0x" + Integer.toHexString(reserved));
		MyUtil.logPrint("section_length     = 0x" + Integer.toHexString(section_length));
		MyUtil.logPrint("network_id     = 0x" + Integer.toHexString(network_id));
		MyUtil.logPrint("reserved     = 0x" + Integer.toHexString(reserved));
		MyUtil.logPrint("version_number     = 0x" + Integer.toHexString(version_number));
		MyUtil.logPrint("current_next_indicator     = 0x" + Integer.toHexString(current_next_indicator));
		MyUtil.logPrint("section_number     = 0x" + Integer.toHexString(section_number));
		MyUtil.logPrint("last_section_number     = 0x" + Integer.toHexString(last_section_number));
		MyUtil.logPrint("reserved_future_use     = 0x" + Integer.toHexString(reserved_future_use));
		MyUtil.logPrint("network_descriptors_length     = 0x" + Integer.toHexString(network_descriptors_length));

		Iterator<PsiDescriptor> iter = networkDescArrayList.iterator();
		while(iter.hasNext())
		{
			iter.next().printDescriptor();
		}

		MyUtil.logPrint("reserved_future_use     = 0x" + Integer.toHexString(reserved_future_use));
		MyUtil.logPrint("transport_stream_loop_length     = 0x" + Integer.toHexString(transport_stream_loop_length));


		MyUtil.logPrint("CRC_32     = 0x" + Integer.toHexString(CRC_32));

	}
	
	/*
	table_id 8 uimsbf
	section_syntax_indicator 1 bslbf
	reserved_future_use 1 bslbf
	reserved 2 bslbf
	section_length 12 uimsbf
	network_id 16 uimsbf
	reserved 2 bslbf
	version_number 5 uimsbf
	current_next_indicator 1 bslbf
	section_number 8 uimsbf
	last_section_number 8 uimsbf
	reserved_future_use 4 bslbf
	network_descriptors_length 12 uimsbf
	for(i=0;i<N;i++){
	descriptor()
	}
	reserved_future_use 4 bslbf
	transport_stream_loop_length 12 uimsbf
	for(i=0;i<N;i++){
		transport_stream_id 16 uimsbf
		original_network_id 16 uimsbf
		reserved_future_use 4 bslbf
		transport_descriptors_length 12 uimsbf
		for(j=0;j<N;j++){
			descriptor()
		}
	}
	CRC_32 32 rpchof
	*/
	
	
}
