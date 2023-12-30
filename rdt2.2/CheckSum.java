package com.ouc.tcp.test;

import java.util.Arrays;
import java.util.zip.CRC32;

import com.ouc.tcp.message.TCP_HEADER;
import com.ouc.tcp.message.TCP_PACKET;

public class CheckSum {
	
	/*璁＄畻TCP鎶ユ枃娈垫牎楠屽拰锛氬彧闇�鏍￠獙TCP棣栭儴涓殑seq銆乤ck鍜宻um锛屼互鍙奣CP鏁版嵁瀛楁*/
	public static short computeChkSum(TCP_PACKET tcpPack) {
		int checkSum = 0;

		//使用import的CRC32包，提供了一种校验方式
		CRC32 crc32 = new CRC32();
		crc32.update(tcpPack.getTcpH().getTh_ack());
		crc32.update(tcpPack.getTcpH().getTh_seq());
		int data[] = tcpPack.getTcpS().getData();
		for(int ele:data) {
			crc32.update(ele);	
		}
//		crc32.update(Arrays.toString(tcpPack.getTcpS().getData()).getBytes());
		
		return (short)crc32.getValue() ;

	}
	
}
