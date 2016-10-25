package mpeg2ts;
import util.*;

public class TSPacket {
	private static final int MAX_PACKET_SIZE = 188;
	int sync_byte;
	int transport_error_indicator;
	int payload_unit_start_indicator;
	int transport_priority;
	int PID;
	int transport_scrambling_control;
	int adaptation_field_control;
	int continuity_counter;
	TSAdaptationField adaptation_field = new TSAdaptationField();
	
	private byte[] payload;
	
	public byte[] getPayload() {
		return payload;
	}
	public int getSync_byte() {
		return sync_byte;
	}
	public void setSync_byte(int sync_byte) {
		this.sync_byte = sync_byte;
	}
	public int getTransport_error_indicator() {
		return transport_error_indicator;
	}
	public void setTransport_error_indicator(int transport_error_indicator) {
		this.transport_error_indicator = transport_error_indicator;
	}
	public int getPayload_unit_start_indicator() {
		return payload_unit_start_indicator;
	}
	public void setPayload_unit_start_indicator(int payload_unit_start_indicator) {
		this.payload_unit_start_indicator = payload_unit_start_indicator;
	}
	public int getTransport_priority() {
		return transport_priority;
	}
	public void setTransport_priority(int transport_priority) {
		this.transport_priority = transport_priority;
	}
	public int getPID() {
		return PID;
	}
	public void setPID(int pID) {
		PID = pID;
	}
	public int getTransport_scrambling_control() {
		return transport_scrambling_control;
	}
	public void setTransport_scrambling_control(int transport_scrambling_control) {
		this.transport_scrambling_control = transport_scrambling_control;
	}
	public int getAdaptation_field_control() {
		return adaptation_field_control;
	}
	public void setAdaptation_field_control(int adaptation_field_control) {
		this.adaptation_field_control = adaptation_field_control;
	}
	public int getContinuity_counter() {
		return continuity_counter;
	}
	public void setContinuity_counter(int continuity_counter) {
		this.continuity_counter = continuity_counter;
	}
	
	public boolean parseTsPacket(byte[] buf)
	{
		int offset = 0;
		if(buf[0] != 0x47)
		{
			MyUtil.logPrint("the first byte of stream is not 0x47");
		
		}
		this.sync_byte						= (int)MyUtil.getValueFromBytes(buf, offset, 8); 	offset += 8;
		this.transport_error_indicator		= (int)MyUtil.getValueFromBytes(buf, offset, 1); 	offset += 1;
		this.payload_unit_start_indicator 	= (int)MyUtil.getValueFromBytes(buf, offset, 1); 	offset += 1;
		this.transport_priority 			= (int)MyUtil.getValueFromBytes(buf, offset, 1); 	offset += 1;
		this.PID							= (int)MyUtil.getValueFromBytes(buf, offset, 13); 	offset += 13;
		// 0x0000 : PAT, 0x0001 : CAT, 0x0002 : Transport Stream Description Table, 0x0003-0x000f:Rsvd, 0x1FFF : Null packet
		this.transport_scrambling_control	= (int)MyUtil.getValueFromBytes(buf, offset, 2); 	offset += 2;
		this.adaptation_field_control		= (int)MyUtil.getValueFromBytes(buf, offset, 2); 	offset += 2;
		this.continuity_counter				= (int)MyUtil.getValueFromBytes(buf, offset, 4); 	offset += 4;
		
		if(this.adaptation_field_control == 0b10 || this.adaptation_field_control == 0b11) 
		{
			//Adaptation Field
			this.adaptation_field.adaptation_field_length = (int)MyUtil.getValueFromBytes(buf, offset, 8); 	offset += 8;
			// Adaptation Field 는 parsing 하지 않고 offset만큼 증가
			offset += this.adaptation_field.adaptation_field_length;
		}
		else if(this.adaptation_field_control == 0b01 || this.adaptation_field_control == 0b11)
		{
			//Payload
			this.payload = new byte[MAX_PACKET_SIZE - 4 - this.adaptation_field.adaptation_field_length];
			for(int i = 0 ; i < this.payload.length ; i++)
			{
				this.payload[i] = (byte)MyUtil.getValueFromBytes(buf, offset, 8); 	offset += 8;
			}
			//System.out.println("payload : ");
			//MyUtil.hexdump(this.payload, this.payload.length);
		}
		
		return true;
	}
	public void print()
	{
		MyUtil.logPrint("transport_error_indicator     = 0x" + Integer.toHexString(transport_error_indicator));
		MyUtil.logPrint("payload_unit_start_indicator  = 0x" + Integer.toHexString(payload_unit_start_indicator));
		MyUtil.logPrint("transport_priority            = 0x" + Integer.toHexString(transport_priority));
		MyUtil.logPrint("PID                           = 0x" + Integer.toHexString(PID));
		MyUtil.logPrint("transport_scrambling_control  = 0x" + Integer.toHexString(transport_scrambling_control));
		MyUtil.logPrint("adaptation_field_control      = 0x" + Integer.toHexString(adaptation_field_control));
		MyUtil.logPrint("continuity_counter            = 0x" + Integer.toHexString(continuity_counter));
		
	}
	public static int getCurrentPID(byte[] data)
	{
		int ret = 0;
		int offset = 0;
		if(data[0] != 0x47)
		{
			MyUtil.logPrint("the first byte of stream is not 0x47");
		}
		offset = 11; // pid starts from bit #
		ret = (int)MyUtil.getValueFromBytes(data, offset, 13);
		return ret;
	}
	/**
	 * Pointer field는 없을 수도 있음. psi의 경우에만 있음.
	 * 존재 할 경우를 가정하여 값을 리턴
	 * @return
	 */
	public int getPointerField()
	{
		return (int)MyUtil.getValueFromBytes(this.payload, 0, 8);
	}
}
