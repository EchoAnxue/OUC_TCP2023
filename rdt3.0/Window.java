package com.ouc.tcp.test;

import com.ouc.tcp.client.Client;
import com.ouc.tcp.message.TCP_PACKET;

public class Window {
    public Client client;  //�ͻ���
    public int size = 16; //���ڴ�С
    //origin 32->16
    //window too large
    public TCP_PACKET[] packets = new TCP_PACKET[size];  // �洢�����ڵİ�
    public int base = 0;  // ������ָ��
    public int nextIndex = 0;  // ��һ������ָ��

    public Window(Client client) {
        this.client = client;
    }

    /*�жϴ����Ƿ�����*/
    public boolean isFull() {
        return size <= nextIndex;
    }

}
