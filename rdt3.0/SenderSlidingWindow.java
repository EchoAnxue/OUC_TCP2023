package com.ouc.tcp.test;

import com.ouc.tcp.client.Client;
import com.ouc.tcp.client.UDT_RetransTask;
import com.ouc.tcp.client.UDT_Timer;
import com.ouc.tcp.message.TCP_PACKET;

import java.util.Timer;

public class SenderSlidingWindow extends Window {


    private RetransmitTask task;  // �ش�����
    
    private UDT_Timer[] timers = new UDT_Timer[size];//������ÿ��������Ҫһ����ʱ��

    public SenderSlidingWindow(Client client) {
        super(client);
    }

    // �����������
    public void putPacket(TCP_PACKET packet) {
        packets[nextIndex] = packet;  // �ڴ��ڵ���һ������λ�ã������
        //timers �� packets��С��ͬ����Ӧÿ��packet����ʱ
        timers[nextIndex] = new UDT_Timer();
        timers[nextIndex].schedule(new UDT_RetransTask(client, packet), 1000, 1000);
        nextIndex++;  // ���´��ڵ���һ������λ��
      
    }

    // ���յ�ACK
    public void receiveACK(int currentSequence) {
        if (base <= currentSequence && currentSequence < base + size)
        {  // �����ACK�ڴ��ڷ�Χ��
            if (timers[currentSequence - base] == null)  return;// �ظ�ACK��ֱ��return
            //else{}�յ�ACK�� timer��ͣ 
            timers[currentSequence - base].cancel();// ȡ����ɾ���ð��ļ�ʱ��
            timers[currentSequence - base] = null;

            if (currentSequence == base) { //�յ���ack�Ǵ�������ߵ�
                int maxACKedIndex = 0; //�����յ�ACK�ķ���
                while (maxACKedIndex + 1 < nextIndex
                        && timers[maxACKedIndex + 1] == null) {
                    maxACKedIndex++;
                }
                for (int i = 0; maxACKedIndex + 1 + i < size; i++) {// ���ƴ���
                    packets[i] = packets[maxACKedIndex + 1 + i];
                    timers[i] = timers[maxACKedIndex + 1 + i];
                }
                // ���ԭλ�İ��ͼ�ʱ��
                for (int i = size - (maxACKedIndex + 1); i < size; i++) {
                    packets[i] = null;
                    timers[i] = null;
                }
                base += maxACKedIndex + 1; //��base�ƶ�������СδACK�ķ���
                nextIndex -= maxACKedIndex + 1; // ������һ�����������packets�е�index��
                //caution��nextIndex��[0,size]
            }
        }
    }


}