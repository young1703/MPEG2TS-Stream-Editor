package PsiDescriptors;


import util.MyUtil;

public class PsiDescriptor {
	int descriptor_tag;
	int descriptor_length;
	byte[] payload;
	
	public int parsePsiDescriptor(byte[] buf, int startByte)
	{
		int offset = startByte * 8;
		this.descriptor_tag = (int)MyUtil.getValueFromBytes(buf, offset, 8);  offset += 8;
		this.descriptor_length = (int)MyUtil.getValueFromBytes(buf, offset, 8);  offset += 8;
//		System.out.println("descriptor_tag = "+ Integer.toHexString(descriptor_tag));
//		System.out.println("descriptor_length = "+ Integer.toHexString(descriptor_length));
/*
		if(this.descriptor_length != buf.length - startByte - 2)
		{
			return 0;
		}
*/
		this.payload = new byte[descriptor_length];
		for(int i = 0 ; i < this.descriptor_length ; i++)
		{
			this.payload[i] = buf[i+2+startByte];
		}
		return this.descriptor_length + 2;
	}
	
	public void printDescriptor()
	{
		MyUtil.logPrint("## descriptor print start ##");
		MyUtil.logPrint("## descriptor tag = " + String.format("0x%x", this.descriptor_tag) );
		MyUtil.logPrint("## descriptor len = " + String.format("0x%x", this.descriptor_length) );
		MyUtil.hexdump(this.payload, this.descriptor_length);
		
	}
}
