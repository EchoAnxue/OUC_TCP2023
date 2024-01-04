# OUC_TCP2023
中国海洋大学计算机网络实验题
本实验参考：
https://github.com/MrTjming/OUC-Computer-Network-Experiment
https://github.dev/adventurer-w/Computer-network-TCP等，感谢提供参考，如有冒犯可以联系我删除，联系QQ：2399025768
本实验代码修改部分，**关于解决TCP Reno 版本WRONG超时重传没有实行，反而执行fast retransmit的问题。**
经过学习，得出WRONG的ACK并不是返回上一个ACK，而是正常返回WRONG Packet’s ACK。
未解决的问题：**接收端的NO ACK并不会引起发送端重传**，浏览之前学长的代码似乎也没有解决这个问题。
这位老师的代码解决了：
https://github.com/MrTjming/OUC-Computer-Network-Experiment
