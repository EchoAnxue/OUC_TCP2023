package com.ouc.tcp.test;

import java.util.Hashtable;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import com.ouc.tcp.client.Client;
import com.ouc.tcp.client.UDT_RetransTask;
import com.ouc.tcp.client.UDT_Timer;
import com.ouc.tcp.message.TCP_PACKET;

public class senderTahoeWindow {
//    public Client client;  //�ͻ���
//    private volatile int ssthresh = 16;
//    public int cwnd = 1;
//    private int lastAck = -1; //��һ���յ���ACK�İ���seq
//    // ʹ�ù�ϣ���洢�����ڵİ�
//    private Hashtable<Integer, TCP_PACKET> packets = new Hashtable<Integer, TCP_PACKET>();
//    // ʹ�ù�ϣ������ÿ�����ļ�ʱ��
//    private Hashtable<Integer, Taho_Timer> timers = new Hashtable<Integer, Taho_Timer>();
//    private int ackCount = -1; //�ظ���Ack��
//    private int congestionCount = 0; // ����ӵ������״̬ʱ�յ���ACK����¼

    private int timeSleep = 100;
    // �����������
    public void addPacket(TCP_PACKET packet) {
        int number = (packet.getTcpH().getTh_seq() - 1) / 100;
        timers.put(number, new UDT_Timer());
        timers.get(number).schedule(new Taho_Timer(client, packet), timeSleep, timeSleep);
        packets.put(number, packet);
    }
    public senderTahoeWindow(Client client) {
        this.client = client;
    }

    /*�жϴ����Ƿ�����*/
    public boolean isFull() {
        return cwnd <= packets.size();
    }

    
    
    // ���յ�ACK
    public void receiveACK(int number) {
        if (number >lastAck) {//wrong ACK,ACK+1 �����ش� �������ͣ�ACK��ACK+1����ʱ�ˣ�
        	//ACK+1�ȳ�ʱ��ԭ�����ACKԭ���ĵ���ʱ��ȡ���ˣ�����������ж������ſ�ʼ���µĵ���ʱ
        	//����˳����
        	//��Ҳ�Ƿ����߼��ģ���ΪACK�Ѿ�����Ӧ������˵Ӧ��ȡ������ʱ��
        	//���µ����
//        	int i = lastAck+1
            for (int i = lastAck-1; i <= number; i++) {
                //�ۼ�ȷ�ϣ����Խ�֮ǰ�İ��Ƴ�������
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
            ackCount = 0;  //��һ���յ������

            if (cwnd < ssthresh) { //������
                // ÿ�յ�һ��ACK��cwnd+1����ÿ����RTT������ָ��������
                System.out.println("********* SLOW START *********");
                System.out.println("cwnd: " + cwnd + " ---> " + (cwnd + 1));
                cwnd ++;
            }else { //ӵ������
                congestionCount ++;
                System.out.println("********* CONGESTION AVOIDANCE *********");
                System.out.println("cwnd: " + cwnd + "  congestionCount: " + congestionCount);
                if (congestionCount >= cwnd) {  // �յ�ACK�������� cwnd����������
                    congestionCount -= cwnd;  // ���ü�����
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
        { //�����
            ackCount++;
            if(ackCount>=3){ //�������ظ���ack����fast recovery������cwndֻ���۰�+3.
                TCP_PACKET packet = packets.get(number + 1);
                if (packet != null) {
                    System.out.println("*********fast retransmit���ش�  ,num = "+number+" ********* ");
                    client.send(packet);
                    timers.get(number + 1).cancel();
                    timers.put(number + 1, new UDT_Timer());
                    timers.get(number + 1).schedule(new Taho_Timer(client, packet), timeSleep, timeSleep);
                }
              //�������ظ���ack����fast recovery������cwndֻ���۰�+3.
                System.out.println("*********fast recovery ��ָ�  ,num = "+number+" ********* ");
                //ssthresh  �۰�
                System.out.println("ssthresh: " + ssthresh + " ---> " + Math.max(cwnd / 2, 2));
                ssthresh = Math.max(cwnd / 2, 2); // ssthresh ����С��2
                System.out.println("cwnd: " + cwnd + " ---> " + ssthresh+" + 3");
                cwnd = ssthresh +3;
            
            }
        }

    }

    public Client client;  //�ͻ���
    private volatile int ssthresh = 16;
    public int cwnd = 1;
    private int lastAck = -1; //��һ���յ���ACK�İ���seq
    // ʹ�ù�ϣ���洢�����ڵİ�
    private Hashtable<Integer, TCP_PACKET> packets = new Hashtable<Integer, TCP_PACKET>();
    // ʹ�ù�ϣ������ÿ�����ļ�ʱ��
    private Hashtable<Integer, UDT_Timer> timers = new Hashtable<Integer, UDT_Timer>();
    private int ackCount = -1; //�ظ���Ack��
    private int congestionCount = 0; // ����ӵ������״̬ʱ�յ���ACK��
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
            System.err.println("###### ��ʱ�ش� ,num = "+(packet.getTcpH().getTh_seq() - 1) / 100+" ###### ");
            System.out.println("cwnd: " + cwnd + " ---> " + 1);
            System.out.println("ssthresh: " + ssthresh + " ---> " + Math.max(cwnd / 2, 2));

            ssthresh = Math.max(cwnd / 2, 2); // ssthresh ����С��2
            cwnd = 1;
            super.run();

            int number = (packet.getTcpH().getTh_seq() - 1) / 100;
            if(timers.get(number) != null){
                timers.get(number).cancel();
                timers.remove(number);
            }
//һ����˵�����ٳ��ֶ����ش������������ˣ�����ʡ���·�

//           timers.put(number, new UDT_Timer());
//            timers.get(number).schedule(new Taho_Timer(client, packet), timeSleep, timeSleep);


        }


    }
    //һ����ʱ�������µ�ACK�İ����У������þ����Է�wrong����hashtable�Ѿ�����յ�������ṩ��ȷ��packet
    
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
