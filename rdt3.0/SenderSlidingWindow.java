package com.ouc.tcp.test;

import com.ouc.tcp.client.Client;
import com.ouc.tcp.client.UDT_RetransTask;
import com.ouc.tcp.client.UDT_Timer;
import com.ouc.tcp.message.TCP_PACKET;

import java.util.Timer;

public class SenderSlidingWindow extends Window {


    private RetransmitTask task;  // 重传任务
    
    private UDT_Timer[] timers = new UDT_Timer[size];//窗口中每个包都需要一个计时器

    public SenderSlidingWindow(Client client) {
        super(client);
    }

    // 加入包到窗口
    public void putPacket(TCP_PACKET packet) {
        packets[nextIndex] = packet;  // 在窗口的下一个插入位置，放入包
        //timers 和 packets大小相同，对应每个packet并计时
        timers[nextIndex] = new UDT_Timer();
        timers[nextIndex].schedule(new UDT_RetransTask(client, packet), 1000, 1000);
        nextIndex++;  // 更新窗口的下一个插入位置
      
    }

    // 接收到ACK
    public void receiveACK(int currentSequence) {
        if (base <= currentSequence && currentSequence < base + size)
        {  // 如果该ACK在窗口范围内
            if (timers[currentSequence - base] == null)  return;// 重复ACK，直接return
            //else{}收到ACK， timer暂停 
            timers[currentSequence - base].cancel();// 取消并删除该包的计时器
            timers[currentSequence - base] = null;

            if (currentSequence == base) { //收到的ack是窗口最左边的
                int maxACKedIndex = 0; //最大的收到ACK的分组
                while (maxACKedIndex + 1 < nextIndex
                        && timers[maxACKedIndex + 1] == null) {
                    maxACKedIndex++;
                }
                for (int i = 0; maxACKedIndex + 1 + i < size; i++) {// 右移窗口
                    packets[i] = packets[maxACKedIndex + 1 + i];
                    timers[i] = timers[maxACKedIndex + 1 + i];
                }
                // 清空原位的包和计时器
                for (int i = size - (maxACKedIndex + 1); i < size; i++) {
                    packets[i] = null;
                    timers[i] = null;
                }
                base += maxACKedIndex + 1; //将base移动到，最小未ACK的分组
                nextIndex -= maxACKedIndex + 1; // 更新下一个插入包的在packets中的index，
                //caution：nextIndex：[0,size]
            }
        }
    }


}