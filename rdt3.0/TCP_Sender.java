/***************************2.1: ACK/NACK
**************************** Feng Hong; 2015-12-09*/

package com.ouc.tcp.test;

import com.ouc.tcp.client.TCP_Sender_ADT;
import com.ouc.tcp.client.UDT_RetransTask;
import com.ouc.tcp.client.UDT_Timer;
import com.ouc.tcp.message.*;
import com.ouc.tcp.tool.TCP_TOOL;

public class TCP_Sender extends TCP_Sender_ADT {
	private UDT_Timer timer;
	private TCP_PACKET tcpPack;	//待发送的TCP数据报
	private UDT_RetransTask retransTask;
	private volatile int flag = 1;
	
	/*鏋勯�犲嚱鏁�*/
	public TCP_Sender() {
		super();	//调用超类构造函数
		super.initTCP_Sender(this);		//初始化TCP发送端
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
	
	
		timer = new UDT_Timer();
		retransTask = new UDT_RetransTask(client, tcpPack); //设置重传任务
		timer.schedule(retransTask, 3000, 3000);
		udt_send(tcpPack);
		flag = 1;
		 // 等待ACK报文
	    // 这是个提示！！！
	    // waitACK(); 属于是recv()的内容
//		while(循环条件)后带有分号“；”时：
//		while(循环条件)与其后面的分号“；”成为了一个整体。
//		当循环条件为真(非0)时， 不会执行循环体语句。直到循环条件为假(0)时，才会执行循环体语句（执行一遍），然后继续执行后面的语句。
		while (flag==1);
//		{
//			waitACK();
//		}
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
		tcpH.setTh_eflag((byte)7);		// 此处 1 可以替换为其他错误类型
		//System.out.println("to send: "+stcpPack.getTcpH().getTh_seq());				
	    // 发送数据报
		client.send(stcpPack);
	}
	
	@Override
	//闇�瑕佷慨鏀�
	public void waitACK() {
		//循环检查ackQueue
		//循环检查确认号对列中是否有新收到的ACK		
		if(!ackQueue.isEmpty()){
			int currentAck=ackQueue.poll();
			// System.out.println("CurrentAck: "+currentAck);
			if (currentAck == tcpPack.getTcpH().getTh_seq()){
				System.out.println("Clear: "+tcpPack.getTcpH().getTh_seq());
				timer.cancel();
				flag = 0;
				//break;
			}
			

		}
		//else{}
	}

	@Override
	//接收到ACK报文：检查校验和，将确认号插入ack队列;NACK的确认号为－1；不需要修改
	public void recv(TCP_PACKET recvPack) {
		if(recvPack.getTcpH().getTh_sum()==CheckSum.computeChkSum(recvPack)) {
		System.out.println("Receive ACK Number is："+ recvPack.getTcpH().getTh_ack());
		ackQueue.add(recvPack.getTcpH().getTh_ack());
	    System.out.println();				
		}
		else {
		//发生比特翻转，在ack队列加入-1
		System.out.println("Receive corrupt ACK: " + recvPack.getTcpH().getTh_ack());
		ackQueue.add(-1);
		System.out.println();
		}

		waitACK();

	   
	}
	
}