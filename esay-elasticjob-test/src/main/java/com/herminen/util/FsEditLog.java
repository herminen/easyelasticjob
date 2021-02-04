package com.herminen.util;


import java.util.LinkedList;

/**
 * Created on 2021/2/3.
 *
 * @author ${AUTHOR}
 */
public class FsEditLog {

    private Long txId = 0L;

    private DoubleBuffer editLogBuffer  = new DoubleBuffer();

    private volatile boolean isSyncRunning = false;

    private volatile boolean isWaitSync = false;

    private volatile Long syncMaxTxId = 0L;

    private ThreadLocal<Long> localTxId = new ThreadLocal<Long>();

    /**
     * 写日志高并发请求
     * @param content
     */
    public void logEdit(String content){
        //记录日志，加锁，保证顺序
        synchronized (this){
            txId++;
            localTxId.set(txId);
            EditLog editLog = new EditLog(txId, content);
            editLogBuffer.write(editLog);
        }
        logSync();
    }

    private void logSync() {
        synchronized (this){
            if(isSyncRunning){
                Long txId = localTxId.get();
                //表示同步工作还没处理完毕
                if(txId.compareTo(syncMaxTxId) <= 0){
                    return;
                }
                //表示是否在等待同步过程完成
                if(isWaitSync){
                    return;
                }
                isWaitSync = true;
                while (isSyncRunning){
                    try {
                        //释放锁
                        this.wait(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                isWaitSync = false;
            }

            //TODO 正常情况下需要判断下缓冲区的大小达到一定阈值，比如10M
            editLogBuffer.setReadyToSync();

            if(editLogBuffer.currentBuffer.size() > 0){
                syncMaxTxId = editLogBuffer.getSyncMaxTxId();
            }

            isSyncRunning= true;
        }//释放锁

        //耗时操作 不加锁
        editLogBuffer.flush();

        synchronized (this){
            isSyncRunning = false;
            this.notify();
        }
    }

    /**
     * 双缓冲，刷盘缓存和记录日志缓存区分开来 ，内存处理的速度和磁盘处理的速度存在数量级的差距
     */
    class DoubleBuffer{
        private LinkedList<EditLog> currentBuffer = new LinkedList<EditLog>();
        private LinkedList<EditLog> syncBuffer = new LinkedList<EditLog>();

        public void write(EditLog editLog){
            currentBuffer.add(editLog);
        }

        /**
         * 准备刷 缓存，交换缓冲区
         */
        public void setReadyToSync(){
            LinkedList<EditLog> temp = currentBuffer;
            currentBuffer = syncBuffer;
            syncBuffer = temp;
        }

        /**
         * 获得刷磁盘缓存日中最大的txId
         * @return
         */
        public Long getSyncMaxTxId(){
            return syncBuffer.getLast().txId;
        }

        /**
         * 日志 刷盘
         */
        public void flush(){
            for (EditLog editLog : syncBuffer) {
                //TODO 真实场景应该是刷盘这种耗时操作
                System.out.println(editLog);
            }
            syncBuffer.clear();
        }
    }

    /**
     * 抽象hdfs编辑日志
     */
    class EditLog{
        private Long txId;
        private String content;

        public EditLog(Long txId, String content) {
            this.txId = txId;
            this.content = content;
        }

        @Override
        public String toString() {
            return "EditLog{" +
                    "txId=" + txId +
                    ", content='" + content + '\'' +
                    '}';
        }
    }

}
