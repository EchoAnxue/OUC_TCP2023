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
	            // ACK号在上一个window[base - size, base - 1]
	            if (base - size <= currentSequence && currentSequence <= base - 1)
	                return currentSequence;
	            //return -1 还是currentSequence 区别不大，因为都不在范围内
	        }
	        
	        else if (base <= currentSequence &&
	                currentSequence < base + size) {//位置正确
	            //加入窗口
	            packets[currentSequence - base] = packet;
	            if (currentSequence == base) { //收到的分组好刚好为窗口左端
	                sliding();// 移动窗口并交付数据
	            }
	            return currentSequence;
	        }
	        return -1;
	    }

	    private void sliding() {
	    	//
	    	//类似于senderWindow recvACK()
	        int maxIndex = 0; //最大的收到ACK的包
	        while (maxIndex + 1 < size && packets[maxIndex + 1] != null) {
	            maxIndex++;
	        }
	        // 将已接收到的分组加入交付队列
	        for (int i = 0; i < maxIndex + 1; i++)
	            dataQueue.add(packets[i].getTcpS().getData());

	        for (int i = 0; maxIndex + 1 + i < size; i++)// 右移窗口（即左移包）
	            packets[i] = packets[maxIndex + 1 + i];

	        for (int i = size - (maxIndex + 1); i < size; i++)//清空
	            packets[i] = null;

	        base += maxIndex + 1;// 更新base

	        if (dataQueue.size() >= 20 || base == 1000)//交付数据
	            deliver_data();
	    }

	    //交付数据: 将数据写入文件
	    public void deliver_data() {
	        // 检查 dataQueue，将数据写入文件
	        try {
	            File file = new File("recvData.txt");
	            BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));

	            while (!dataQueue.isEmpty()) {
	                int[] data = dataQueue.poll();

	                // 将数据写入文件
	                for (int i = 0; i < data.length; i++) {
	                    writer.write(data[i] + "\n");
	                }

	                writer.flush();  // 清空输出缓存
	            }

	            writer.close();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }

}
