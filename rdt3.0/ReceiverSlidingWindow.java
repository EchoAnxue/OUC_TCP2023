package com.ouc.tcp.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import com.ouc.tcp.client.Client;
import com.ouc.tcp.message.TCP_PACKET;

public class ReceiverSlidingWindow extends Window{

    
    Queue<int[]> dataQueue = new LinkedBlockingQueue();
    

	public ReceiverSlidingWindow(Client client) {
		// TODO Auto-generated constructor stub
		super(client);
	}
	
	//
	 public int receivePacket(TCP_PACKET packet) {
	        int currentSequence = (packet.getTcpH().getTh_seq() - 1) / 100;

	        if (currentSequence < base) {
	            // ACK������һ��window[base - size, base - 1]
	            if (base - size <= currentSequence && currentSequence <= base - 1)
	                return currentSequence;
	            //return -1 ����currentSequence ���𲻴���Ϊ�����ڷ�Χ��
	        }
	        
	        else if (base <= currentSequence &&
	                currentSequence < base + size) {//λ����ȷ
	            //���봰��
	            packets[currentSequence - base] = packet;
	            if (currentSequence == base) { //�յ��ķ���øպ�Ϊ�������
	                sliding();// �ƶ����ڲ���������
	            }
	            return currentSequence;
	        }
	        return -1;
	    }

	    private void sliding() {
	    	//
	    	//������senderWindow recvACK()
	        int maxIndex = 0; //�����յ�ACK�İ�
	        while (maxIndex + 1 < size && packets[maxIndex + 1] != null) {
	            maxIndex++;
	        }
	        // ���ѽ��յ��ķ�����뽻������
	        for (int i = 0; i < maxIndex + 1; i++)
	            dataQueue.add(packets[i].getTcpS().getData());

	        for (int i = 0; maxIndex + 1 + i < size; i++)// ���ƴ��ڣ������ư���
	            packets[i] = packets[maxIndex + 1 + i];

	        for (int i = size - (maxIndex + 1); i < size; i++)//���
	            packets[i] = null;

	        base += maxIndex + 1;// ����base

	        if (dataQueue.size() >= 20 || base == 1000)//��������
	            deliver_data();
	    }

	    //��������: ������д���ļ�
	    public void deliver_data() {
	        // ��� dataQueue��������д���ļ�
	        try {
	            File file = new File("recvData.txt");
	            BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));

	            while (!dataQueue.isEmpty()) {
	                int[] data = dataQueue.poll();

	                // ������д���ļ�
	                for (int i = 0; i < data.length; i++) {
	                    writer.write(data[i] + "\n");
	                }

	                writer.flush();  // ����������
	            }

	            writer.close();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }

}
