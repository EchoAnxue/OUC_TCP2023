/***************************2.1: ACK/NACK*****************/
/***** Feng Hong; 2015-12-09******************************/
package com.ouc.tcp.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.ouc.tcp.client.TCP_Receiver_ADT;
import com.ouc.tcp.message.*;
import com.ouc.tcp.tool.TCP_TOOL;

public class TCP_Receiver extends TCP_Receiver_ADT {
	
	private TCP_PACKET ackPack;	//回复的ACK报文段

    int expSequence = -1;  //记录期望收到的序号
    
    private ReceiverSlidingWindow window = new ReceiverSlidingWindow(this.client);
		
	/*鏋勯�犲嚱鏁�*/
	public TCP_Receiver() {
		super();	//调用超类构造函数
		super.initTCP_Receiver(this);	//初始化TCP接收端
	}

	@Override
	//接收到数据报：检查校验和，设置回复的ACK报文段
	public void rdt_recv(TCP_PACKET recvPack) {
        //检查校验码，生成ACK
        if (CheckSum.computeChkSum(recvPack) == recvPack.getTcpH().getTh_sum()) {

        	try {
        		 expSequence = window.receivePacket(recvPack);
        	}
        	catch(Exception e) {
        		 e.printStackTrace();
        	}
        	if(expSequence!=-1) {
              //生成ACK报文段，ack为收到的TCP分组的seq
        	  //expSequence = currentSequence 但不在范围发出去后会被拦截
              tcpH.setTh_ack(recvPack.getTcpH().getTh_seq());
              ackPack = new TCP_PACKET(tcpH, tcpS, recvPack.getSourceAddr());
              tcpH.setTh_sum(CheckSum.computeChkSum(ackPack));

              reply(ackPack);
        		
        	}

        }
		System.out.println();

	}

	@Override
	//交付数据（将数据写入文件）；不需要修改
	public void deliver_data() {
// 已经被receiverWindow里面的deliver_data（）取代
	}

	@Override
	//回复ACK报文段
	public void reply(TCP_PACKET replyPack) {
		//设置错误控制标志
		tcpH.setTh_eflag((byte)7);	//eFlag=0，信道无错误
				
		//发送数据报
		client.send(replyPack);
	}
	
}
