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
	
	private TCP_PACKET ackPack;	//鍥炲鐨凙CK鎶ユ枃娈�
	int sequence=1;//鐢ㄤ簬璁板綍褰撳墠寰呮帴鏀剁殑鍖呭簭鍙凤紝娉ㄦ剰鍖呭簭鍙蜂笉瀹屽叏鏄�
		
	/*鏋勯�犲嚱鏁�*/
	public TCP_Receiver() {
		super();	//璋冪敤瓒呯被鏋勯�犲嚱鏁�
		super.initTCP_Receiver(this);	//鍒濆鍖朤CP鎺ユ敹绔�
	}

	@Override
	//鎺ユ敹鍒版暟鎹姤锛氭鏌ユ牎楠屽拰锛岃缃洖澶嶇殑ACK鎶ユ枃娈�
	public void rdt_recv(TCP_PACKET recvPack) {
		//核对checksum
		if(CheckSum.computeChkSum(recvPack) == recvPack.getTcpH().getTh_sum()) {
			//设置返回的TCP packet
			//ack = seq#
			tcpH.setTh_ack(recvPack.getTcpH().getTh_seq());
			ackPack = new TCP_PACKET(tcpH, tcpS, recvPack.getSourceAddr());
			tcpH.setTh_sum(CheckSum.computeChkSum(ackPack));
			//发送 AckPack
			reply(ackPack);			
			
			//收到的data缓冲区的data++
			dataQueue.add(recvPack.getTcpS().getData());				
			sequence++;
		}else{
			System.out.println("Recieve Computed: "+CheckSum.computeChkSum(recvPack));
			System.out.println("Recieved Packet"+recvPack.getTcpH().getTh_sum());
			System.out.println("Problem: Packet Number: "+recvPack.getTcpH().getTh_seq()+" + InnerSeq:  "+sequence);
			//返回 Ack =-1
			tcpH.setTh_ack(-1);
			ackPack = new TCP_PACKET(tcpH, tcpS, recvPack.getSourceAddr());
			tcpH.setTh_sum(CheckSum.computeChkSum(ackPack));
			//鍥炲ACK鎶ユ枃娈�
			reply(ackPack);
		}
		
		System.out.println();
		
		
		//queue 队伍达到size之后再传递
		if(dataQueue.size() == 20) 
			deliver_data();	
	}

	@Override
	//交付数据（将数据写入文件）；不需要修改
	public void deliver_data() {
		//检查dataQueue，将数据写入文件
		File fw = new File("recvData.txt");
		BufferedWriter writer;
		
		try {
			writer = new BufferedWriter(new FileWriter(fw, true));
			
			//循环检查data队列中是否有新交付数据
			while(!dataQueue.isEmpty()) {
				int[] data = dataQueue.poll();
				
				//将数据写入文件
				for(int i = 0; i < data.length; i++) {
					writer.write(data[i] + "\n");
				}
				
				writer.flush();		//清空输出缓存
			}
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	//回复ACK报文段
	public void reply(TCP_PACKET replyPack) {
		//设置错误控制标志
		tcpH.setTh_eflag((byte)0);	//eFlag=0，信道无错误
				
		//发送数据报
		client.send(replyPack);
	}
	
}
