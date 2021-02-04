package com.herminen;

import com.herminen.util.FsEditLog;
import org.junit.Test;

/**
 * Created on 2021/2/3.
 *
 * @author ${AUTHOR}
 */
public class TestEditLog {

    @Test
    public void test() throws InterruptedException {
        FsEditLog editLog = new FsEditLog();

        for (int i = 0; i < 100; i++) {
            new Thread(() ->{
                for (int j = 0; j < 500; j++) {
                    editLog.logEdit("hello i'm new log");
                }
            }).start();

        }

        Thread.sleep(Integer.MAX_VALUE);
    }
}
