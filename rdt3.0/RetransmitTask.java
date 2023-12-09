package com.ouc.tcp.test;

import java.util.TimerTask;

import com.ouc.tcp.client.Client;
import com.ouc.tcp.client.UDT_RetransTask;
import com.ouc.tcp.message.TCP_PACKET;

public class RetransmitTask extends TimerTask {

    private Client senderClient;  // �ͻ���
    private TCP_PACKET[] packets;  // ά�������ڰ�������

	public RetransmitTask(Client client,TCP_PACKET[] packets) {
		// TODO Auto-generated constructor stub
		super();
		this.senderClient = client;
		this.packets = packets;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
        for (int i = 0; i < packets.length; i++) {
            if (packets[i] == null) {  // ���û�а���break
                break;
            } else {  // �ݽ�������
                senderClient.send(packets[i]);
            }
        }
		
	}

}
