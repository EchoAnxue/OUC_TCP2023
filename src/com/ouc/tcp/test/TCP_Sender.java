/***************************2.1: ACK/NACK
**************************** Feng Hong; 2015-12-09*/

package com.ouc.tcp.test;

import com.ouc.tcp.client.TCP_Sender_ADT;
import com.ouc.tcp.client.UDT_RetransTask;
import com.ouc.tcp.client.UDT_Timer;
import com.ouc.tcp.message.*;
import com.ouc.tcp.tool.TCP_TOOL;

public class TCP_Sender extends TCP_Sender_ADT {
	
	private TCP_PACKET tcpPack;	//寰呭彂閫佺殑TCP鏁版嵁鎶�
	private volatile int flag = 0;
	
	/*鏋勯�犲嚱鏁�*/
	public TCP_Sender() {
		super();	//璋冪敤瓒呯被鏋勯�犲嚱鏁�
		super.initTCP_Sender(this);		//鍒濆鍖朤CP鍙戦�佺
	}
	
	@Override
	// 可靠发送（应用层调用）：封装应用层数据，产生TCP数据报；需要修改
	public void rdt_send(int dataIndex, int[] appData) {
		
	    // 生成TCP数据报（设置序号和数据字段/校验和),注意打包的顺序
		tcpH.setTh_seq(dataIndex * appData.length + 1);// 包序号设置为字节流号：
		tcpS.setData(appData);
		tcpPack = new TCP_PACKET(tcpH, tcpS, destinAddr);		
				
		tcpH.setTh_sum(CheckSum.computeChkSum(tcpPack));
		tcpPack.setTcpH(tcpH);
		
		// 发送TCP数据报
		udt_send(tcpPack);
		flag = 0;
		
		 // 等待ACK报文
	    // 这是个提示！！！
	    // waitACK();
		while (flag==0);
	}
	
	@Override
	// 不可靠发送：将打包好的TCP数据报通过不可靠传输信道发送；仅需修改错误标志
	public void udt_send(TCP_PACKET stcpPack) {
	    // 设置错误控制标志
	    // 1.只出错
	    // 2.只丢包
	    // 3.只延迟
	    // 4.出错 / 丢包
	    // 5.出错 / 延迟
	    // 6.丢包 / 延迟
	    // 7.出错 / 丢包 / 延迟
		tcpH.setTh_eflag((byte)1);		// 此处 1 可以替换为其他错误类型
		//System.out.println("to send: "+stcpPack.getTcpH().getTh_seq());				
	    // 发送数据报
		client.send(stcpPack);
	}
	
	@Override
	//闇�瑕佷慨鏀�
	public void waitACK() {
		//寰幆妫�鏌ckQueue
		//寰幆妫�鏌ョ‘璁ゅ彿瀵瑰垪涓槸鍚︽湁鏂版敹鍒扮殑ACK		
		if(!ackQueue.isEmpty()){
			int currentAck=ackQueue.poll();
			// System.out.println("CurrentAck: "+currentAck);
			if (currentAck == tcpPack.getTcpH().getTh_seq()){
				System.out.println("Clear: "+tcpPack.getTcpH().getTh_seq());
				flag = 1;
				//break;
			}else{
				System.out.println("Retransmit: "+tcpPack.getTcpH().getTh_seq());
				udt_send(tcpPack);
				flag = 0;
			}
		}
	}

	@Override
	//鎺ユ敹鍒癆CK鎶ユ枃锛氭鏌ユ牎楠屽拰锛屽皢纭鍙锋彃鍏ck闃熷垪;NACK鐨勭‘璁ゅ彿涓猴紞1锛涗笉闇�瑕佷慨鏀�
	public void recv(TCP_PACKET recvPack) {
		System.out.println("Receive ACK Number is："+ recvPack.getTcpH().getTh_ack());
		ackQueue.add(recvPack.getTcpH().getTh_ack());
	    System.out.println();	
	   
	    //澶勭悊ACK鎶ユ枃
	    waitACK();
	   
	}
	
}
