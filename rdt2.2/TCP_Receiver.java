/***************************2.2: *****************/
/***** Feng Hong; 2015-12-09******************************/
package com.ouc.tcp.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Timer;

import com.ouc.tcp.client.TCP_Receiver_ADT;
import com.ouc.tcp.client.UDT_Timer;
import com.ouc.tcp.message.*;
import com.ouc.tcp.tool.TCP_TOOL;

public class TCP_Receiver extends TCP_Receiver_ADT {
	
	private TCP_PACKET ackPack;	//回复的ACK报文段

    private Hashtable<Integer, TCP_PACKET> packets = new Hashtable<Integer, TCP_PACKET>();
    private Hashtable<Integer, Timer> timers = new Hashtable<Integer, Timer>();
//    // 使用哈希表，储存每个包的计时器
//    
//    
//    private ReceiverSlidingWindow window = new ReceiverSlidingWindow(this.client);

	private int sequence = 0;
	
	private int preSequence = -1;
		

	public TCP_Receiver() {
		super();	//调用超类构造函数
		super.initTCP_Receiver(this);	//初始化TCP接收端
	}

    @Override
    //接收到数据报：检查校验和，设置回复的ACK报文段
    public void rdt_recv(TCP_PACKET recvPack) {
    	//检查校验码，生成ACK
    			if(CheckSum.computeChkSum(recvPack) == recvPack.getTcpH().getTh_sum()) {
    				//检验通过，生成ACK报文段（设置确认号）
    				tcpH.setTh_ack(recvPack.getTcpH().getTh_seq());//设置ack为收到分组的seq
    				ackPack = new TCP_PACKET(tcpH, tcpS, recvPack.getSourceAddr());//新建TCP分组
    				tcpH.setTh_sum(CheckSum.computeChkSum(ackPack));//设置校验和
    				//回复ACK报文段
    				reply(ackPack);
    				int nowSequence = (recvPack.getTcpH().getTh_seq() - 1) / 100;  // 当前这个包的seq
    				if (nowSequence != this.preSequence) {  // 收到的包的seq是新值
    					this.preSequence  = nowSequence;  // 更新上一次接受的seq为本次接受到的包的seq

    					 //将接收到的正确有序的数据插入 data 队列，准备交付
    					this.dataQueue.add(recvPack.getTcpS().getData());
    					
    				}
    				
    				//else receive past packet, ignore the duplicate packet
    				else {
    					System.out.println("------Ignore: received Packet Number: "+recvPack.getTcpH().getTh_seq());
    				}
    			
    			}
    			//wrong
    			else{
    				//RDT2.2及以前，检验未通过
    				System.out.println("Recieve Computed: "+CheckSum.computeChkSum(recvPack));//打印校验和的计算结果
    				System.out.println("Recieved Packet"+recvPack.getTcpH().getTh_sum());//打印收到的校验和
    				System.out.println("Problem: Packet Number: "+recvPack.getTcpH().getTh_seq());
    				//  tcpH.setTh_ack(-1); //不再使用NACK ，直接使用序号ACK#
    				tcpH.setTh_ack(preSequence*100+1); //RDT2.2,将ack设置为上一个接收到的包的seq
    				ackPack = new TCP_PACKET(tcpH, tcpS, recvPack.getSourceAddr());//新建TCP分组
    				tcpH.setTh_sum(CheckSum.computeChkSum(ackPack));//计算校验和
    				//回复ACK报文段
    				reply(ackPack);
    			}

    			System.out.println();


    			//交付数据（每20组数据交付一次）
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
		tcpH.setTh_eflag((byte)1);	//eFlag=0，信道无错误
				
		//发送数据报
		client.send(replyPack);
	}
	
}