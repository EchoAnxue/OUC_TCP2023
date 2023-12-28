package com.ouc.tcp.test;

import java.util.Hashtable;

import com.ouc.tcp.client.Client;
import com.ouc.tcp.client.UDT_RetransTask;
import com.ouc.tcp.client.UDT_Timer;
import com.ouc.tcp.message.TCP_PACKET;

public class senderTahoeWindow {
    public Client client;  //�ͻ���
    private volatile int ssthresh = 16;
    public int cwnd = 1;
    private int lastAck = -1; //��һ���յ���ACK�İ���seq
    // ʹ�ù�ϣ���洢�����ڵİ�
    private Hashtable<Integer, TCP_PACKET> packets = new Hashtable<Integer, TCP_PACKET>();
    // ʹ�ù�ϣ������ÿ�����ļ�ʱ��
    private Hashtable<Integer, UDT_Timer> timers = new Hashtable<Integer, UDT_Timer>();
    private int ackCount = -1; //�ظ���Ack��
    private int congestionCount = 0; // ����ӵ������״̬ʱ�յ���ACK����¼

    
    // �����������
    public void addPacket(TCP_PACKET packet) {
        int number = (packet.getTcpH().getTh_seq() - 1) / 100;
        timers.put(number, new UDT_Timer());
        timers.get(number).schedule(new Taho_Timer(client, packet), 1000, 1000);
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
        if (number != lastAck) { //���µ����
            for (int i = lastAck + 1; i <= number; i++) {
                //�ۼ�ȷ�ϣ����Խ�֮ǰ�İ��Ƴ�������
                packets.remove(i);
                if (timers.containsKey(i)) {
                    timers.get(i).cancel();
                    timers.remove(i);
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
        }else{ //�����
            ackCount++;
            if(ackCount>=3){ //�������ظ���ack����fast recovery������cwndֻ���۰룬����+3.
                TCP_PACKET packet = packets.get(number + 1);
                if (packet != null) {
                    System.out.println("*********fast recovery  ,num = "+number+" ********* ");
                    client.send(packet);
                    timers.get(number + 1).cancel();
                    timers.put(number + 1, new UDT_Timer());
                    timers.get(number + 1).schedule(new Taho_Timer(client, packet), 1000, 1000);
                }


                //ssthresh  �۰�
                System.out.println("ssthresh: " + ssthresh + " ---> " + Math.max(cwnd / 2, 2));
                ssthresh = Math.max(cwnd / 2, 2); // ssthresh ����С��2
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

            ssthresh = Math.max(cwnd / 2, 2); // ssthresh ����С��2
            cwnd = 1;
            super.run();


        }
    }
}
