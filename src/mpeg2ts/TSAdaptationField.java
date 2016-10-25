package mpeg2ts;

public class TSAdaptationField {
	int adaptation_field_length = 0;
	int discontinuity_indicator;
	int random_access_indicator;
	int elementary_stream_priority_indicator;
	int PCR_flag;
	int OPCR_flag;
	int splicing_point_flag;
	int transport_private_data_flag;
	int adaptation_field_extension_flag;
	int program_clock_reference_base;
	int program_clock_reference_extension;
	int original_program_clock_reference_base;
	int original_program_clock_reference_extension;
	
	int splice_countdown;
	int transport_private_data_length;
	int private_data_byte;
	
	int adaptation_field_extension_length;
	int ltw_flag;
	int piecewise_rate_flag;
	int seamless_splice_flag;
	
	int ltw_valid_flag;
	int ltw_offset;
	
	int piecewise_rate;
	int splice_type;
	int DTS_next_AU;
	
	int stuffing_byte;
}
