package com.kristof.gameengine.util;

public class VertexConstants {
	public static final int POS_ELEMENT_NUM = 3;
	public static final int NOR_ELEMENT_NUM = 3;
	public static final int TEX_ELEMENT_NUM = 2;
	public static final int TAN_ELEMENT_NUM = 3;
	public static final int STRIDE_IN_ELEMENTS = POS_ELEMENT_NUM + NOR_ELEMENT_NUM + TAN_ELEMENT_NUM + TEX_ELEMENT_NUM;
	public static final int STRIDE_IN_ELEMENTS_PN = POS_ELEMENT_NUM + NOR_ELEMENT_NUM;
	public static final int STRIDE_IN_ELEMENTS_PNTT = POS_ELEMENT_NUM + NOR_ELEMENT_NUM + TEX_ELEMENT_NUM + TAN_ELEMENT_NUM;
	
	public static final int BYTES_PER_FLOAT = 4;
	public static final int BYTES_PER_SHORT = 2;
	
	public static final int STRIDE_IN_BYTES = STRIDE_IN_ELEMENTS * BYTES_PER_FLOAT;
	public static final int STRIDE_IN_BYTES_PN = STRIDE_IN_ELEMENTS_PN * BYTES_PER_FLOAT;
	public static final int STRIDE_IN_BYTES_PNTT = STRIDE_IN_ELEMENTS_PNTT * BYTES_PER_FLOAT;
}
