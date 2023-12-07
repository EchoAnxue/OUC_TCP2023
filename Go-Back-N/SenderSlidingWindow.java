package com.ouc.tcp.test;

import com.ouc.tcp.client.Client;
import com.ouc.tcp.message.TCP_PACKET;

import java.util.Timer;

public class SenderSlidingWindow extends Window {

    private Timer timer;  // ��ʱ��
    private RetransmitTask task;  // �ش�����

    public SenderSlidingWindow(Client client) {
        super(client);
    }

    // �����������
    public void putPacket(TCP_PACKET packet) {
        packets[nextIndex] = packet;  // �ڴ��ڵ���һ������λ�ã������
        if (nextIndex == 0) {  // ���nextIndex==0�����ڴ������أ�������ʱ��
            timer = new Timer();
            task = new RetransmitTask(client, packets);
            timer.schedule(task, 1000, 1000);
        }
        nextIndex++;  // ���´��ڵ���һ������λ��
    }

    // ���յ�ACK
    public void receiveACK(int currentSequence) {
        if (base <= currentSequence && currentSequence < base + size)
        {  // �����ACK�ڴ��ڷ�Χ��

            for (int i = 0; currentSequence - base + 1 + i < size; i++)
            {  // �����ƶ��������ڣ��൱�ڽ���Ӧ��������
                packets[i] = packets[currentSequence - base + 1 + i];
                packets[currentSequence - base + 1 + i] = null;
            }

            nextIndex -= currentSequence - base + 1;  // ����nextIndex
            base = currentSequence + 1;  // ����base

            timer.cancel();  // ֹͣ��ʱ��

            if (nextIndex != 0) {  // ���������а�����Ҫ�ؿ���ʱ��
                timer = new Timer();
                task = new RetransmitTask(client, packets);
                timer.schedule(task, 1000, 1000);
            }
        }
    }


}