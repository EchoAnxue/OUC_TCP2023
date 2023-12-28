package com.ouc.tcp.test;

import java.util.Hashtable;

import com.ouc.tcp.client.Client;
import com.ouc.tcp.client.UDT_RetransTask;
import com.ouc.tcp.client.UDT_Timer;
import com.ouc.tcp.message.TCP_PACKET;

public class senderTahoeWindow {
    public Client client;  //客户端
    private volatile int ssthresh = 16;
    public int cwnd = 1;
    private int lastAck = -1; //上一次收到的ACK的包的seq
    // 使用哈希表，存储窗口内的包
    private Hashtable<Integer, TCP_PACKET> packets = new Hashtable<Integer, TCP_PACKET>();
    // 使用哈希表，储存每个包的计时器
    private Hashtable<Integer, UDT_Timer> timers = new Hashtable<Integer, UDT_Timer>();
    private int ackCount = -1; //重复的Ack数
    private int congestionCount = 0; // 进入拥塞避免状态时收到的ACK数记录

    
    // 加入包到窗口
    public void addPacket(TCP_PACKET packet) {
        int number = (packet.getTcpH().getTh_seq() - 1) / 100;
        timers.put(number, new UDT_Timer());
        timers.get(number).schedule(new Taho_Timer(client, packet), 1000, 1000);
        packets.put(number, packet);
    }
    public senderTahoeWindow(Client client) {
        this.client = client;
    }

    /*判断窗口是否已满*/
    public boolean isFull() {
        return cwnd <= packets.size();
    }

    
    
    // 接收到ACK
    public void receiveACK(int number) {
        if (number != lastAck) { //是新的序号
            for (int i = lastAck + 1; i <= number; i++) {
                //累计确认，可以将之前的包移出窗口了
                packets.remove(i);
                if (timers.containsKey(i)) {
                    timers.get(i).cancel();
                    timers.remove(i);
                }
            }
            lastAck = number;
            ackCount = 0;  //第一次收到这个包

            if (cwnd < ssthresh) { //慢启动
                // 每收到一个ACK，cwnd+1，即每经过RTT翻倍，指数级增长
                System.out.println("********* SLOW START *********");
                System.out.println("cwnd: " + cwnd + " ---> " + (cwnd + 1));
                cwnd ++;
            }else { //拥塞避免
                congestionCount ++;
                System.out.println("********* CONGESTION AVOIDANCE *********");
                System.out.println("cwnd: " + cwnd + "  congestionCount: " + congestionCount);
                if (congestionCount >= cwnd) {  // 收到ACK数量超过 cwnd，线性增长
                    congestionCount -= cwnd;  // 重置计数器
                    System.out.println("cwnd: " + cwnd + " ---> " + (cwnd + 1));
                    cwnd ++;
                }
            }
        }else{ //旧序号
            ackCount++;
            if(ackCount>=3){ //有三次重复的ack，与fast recovery的区别，cwnd只是折半，不会+3.
                TCP_PACKET packet = packets.get(number + 1);
                if (packet != null) {
                    System.out.println("*********fast recovery  ,num = "+number+" ********* ");
                    client.send(packet);
                    timers.get(number + 1).cancel();
                    timers.put(number + 1, new UDT_Timer());
                    timers.get(number + 1).schedule(new Taho_Timer(client, packet), 1000, 1000);
                }


                //ssthresh  折半
                System.out.println("ssthresh: " + ssthresh + " ---> " + Math.max(cwnd / 2, 2));
                ssthresh = Math.max(cwnd / 2, 2); // ssthresh 不得小于2
                System.out.println("cwnd: " + cwnd + " ---> " + ssthresh+" + 3");
                cwnd = ssthresh +3;
            
            }
        }
    }





    class Taho_Timer extends UDT_RetransTask {
        int number;
        private TCP_PACKET packet;

        public Taho_Timer(Client client, TCP_PACKET packet) {
            super(client, packet);
            number = packet.getTcpH().getTh_seq();
            this.packet = packet;
        }

        @Override
        public void run() {
        	// reply delay 
            System.out.println("********* overtime delay ,num = "+(packet.getTcpH().getTh_seq() - 1) / 100+" ********* ");
            System.out.println("cwnd: " + cwnd + " ---> " + 1);
            System.out.println("ssthresh: " + ssthresh + " ---> " + Math.max(cwnd / 2, 2));

            ssthresh = Math.max(cwnd / 2, 2); // ssthresh 不得小于2
            cwnd = 1;
            super.run();


        }
    }
}
