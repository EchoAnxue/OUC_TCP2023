package com.ouc.tcp.test;

import java.util.Hashtable;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import com.ouc.tcp.client.Client;
import com.ouc.tcp.client.UDT_RetransTask;
import com.ouc.tcp.client.UDT_Timer;
import com.ouc.tcp.message.TCP_PACKET;

public class senderTahoeWindow {
//    public Client client;  //客户端
//    private volatile int ssthresh = 16;
//    public int cwnd = 1;
//    private int lastAck = -1; //上一次收到的ACK的包的seq
//    // 使用哈希表，存储窗口内的包
//    private Hashtable<Integer, TCP_PACKET> packets = new Hashtable<Integer, TCP_PACKET>();
//    // 使用哈希表，储存每个包的计时器
//    private Hashtable<Integer, Taho_Timer> timers = new Hashtable<Integer, Taho_Timer>();
//    private int ackCount = -1; //重复的Ack数
//    private int congestionCount = 0; // 进入拥塞避免状态时收到的ACK数记录

    private int timeSleep = 100;
    // 加入包到窗口
    public void addPacket(TCP_PACKET packet) {
        int number = (packet.getTcpH().getTh_seq() - 1) / 100;
        timers.put(number, new UDT_Timer());
        timers.get(number).schedule(new Taho_Timer(client, packet), timeSleep, timeSleep);
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
        if (number >lastAck) {//wrong ACK,ACK+1 都会重传 正常发送，ACK，ACK+1都超时了，
        	//ACK+1先超时的原因的是ACK原来的倒计时被取消了，遇到下面的判断条件才开始重新的倒计时
        	//所以顺序倒了
        	//这也是符合逻辑的，因为ACK已经被回应，按理说应该取消倒计时的
        	//是新的序号
//        	int i = lastAck+1
            for (int i = lastAck-1; i <= number; i++) {
                //累计确认，可以将之前的包移出窗口了
            	if(packets.containsKey(i)) {
             	try {
					temp.add(packets.get(i).clone());
					packets.remove(i);
				} catch (CloneNotSupportedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}           		
            	}

                
               
                if (timers.containsKey(i)) {
                    timers.get(i).cancel();
                    timers.remove(i);
                    System.out.println("****"+i+ "timer cancle*****");
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
        }
        // wrong packet
        else if (number+1==lastAck) {
            if(timers.get(number) == null){
            	if(temp.getPacketQueue().peek()!=null) {
            	System.err.println("****size:   "+temp.getPacketQueue().size());
            	System.err.println("*****ACK queue: "+temp.packetQueue.peek().getTcpH().getTh_ack());
            	timers.put(number, new UDT_Timer());
                timers.get(number).schedule(new Taho_Timer(client,temp.outPacket()), timeSleep, timeSleep);
                if(timers.get(number+1) != null){
                    timers.get(number+1).cancel();
                    timers.remove(number+1);
                    System.err.println("****numeber +1!=null "+(number+1)+temp.getPacketQueue().size());
                }
            	}
               
            }
            
        }

        else if (number==lastAck)
        { //旧序号
            ackCount++;
            if(ackCount>=3){ //有三次重复的ack，与fast recovery的区别，cwnd只是折半+3.
                TCP_PACKET packet = packets.get(number + 1);
                if (packet != null) {
                    System.out.println("*********fast retransmit快重传  ,num = "+number+" ********* ");
                    client.send(packet);
                    timers.get(number + 1).cancel();
                    timers.put(number + 1, new UDT_Timer());
                    timers.get(number + 1).schedule(new Taho_Timer(client, packet), timeSleep, timeSleep);
                }
              //有三次重复的ack，与fast recovery的区别，cwnd只是折半+3.
                System.out.println("*********fast recovery 快恢复  ,num = "+number+" ********* ");
                //ssthresh  折半
                System.out.println("ssthresh: " + ssthresh + " ---> " + Math.max(cwnd / 2, 2));
                ssthresh = Math.max(cwnd / 2, 2); // ssthresh 不得小于2
                System.out.println("cwnd: " + cwnd + " ---> " + ssthresh+" + 3");
                cwnd = ssthresh +3;
            
            }
        }

    }

    public Client client;  //客户端
    private volatile int ssthresh = 16;
    public int cwnd = 1;
    private int lastAck = -1; //上一次收到的ACK的包的seq
    // 使用哈希表，存储窗口内的包
    private Hashtable<Integer, TCP_PACKET> packets = new Hashtable<Integer, TCP_PACKET>();
    // 使用哈希表，储存每个包的计时器
    private Hashtable<Integer, UDT_Timer> timers = new Hashtable<Integer, UDT_Timer>();
    private int ackCount = -1; //重复的Ack数
    private int congestionCount = 0; // 进入拥塞避免状态时收到的ACK数
    private tem temp = new tem();


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
            System.err.println("###### 超时重传 ,num = "+(packet.getTcpH().getTh_seq() - 1) / 100+" ###### ");
            System.out.println("cwnd: " + cwnd + " ---> " + 1);
            System.out.println("ssthresh: " + ssthresh + " ---> " + Math.max(cwnd / 2, 2));

            ssthresh = Math.max(cwnd / 2, 2); // ssthresh 不得小于2
            cwnd = 1;
            super.run();

            int number = (packet.getTcpH().getTh_seq() - 1) / 100;
            if(timers.get(number) != null){
                timers.get(number).cancel();
                timers.remove(number);
            }
//一般来说不会再出现二次重传还错误的情况了，可以省略下方

//           timers.put(number, new UDT_Timer());
//            timers.get(number).schedule(new Taho_Timer(client, packet), timeSleep, timeSleep);


        }


    }
    //一个及时储存最新的ACK的包队列，其作用就是以防wrong，在hashtable已经被清空的情况下提供正确的packet
    
    class tem {
    	private Queue<TCP_PACKET> packetQueue = new LinkedBlockingQueue();
    	public void add(TCP_PACKET packet) {
            while(!packetQueue.isEmpty()) {
            	packetQueue.poll();
            }
    		try {
				packetQueue.add(packet.clone());
			} catch (CloneNotSupportedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	public TCP_PACKET outPacket() {
    		
    			return packetQueue.poll();

    	}
		private Queue<TCP_PACKET> getPacketQueue() {
			return packetQueue;
		}
		private void setPacketQueue(Queue<TCP_PACKET> packetQueue) {
			this.packetQueue = packetQueue;
		}
    
    	
    }
}
