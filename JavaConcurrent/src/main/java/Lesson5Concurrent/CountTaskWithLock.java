package Lesson5Concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.locks.ReentrantLock;

public class CountTaskWithLock implements Callable<Long> {

    private static ReentrantLock re;
    private static Long localCounter = (long) 0;
    private String localString = null;

    public CountTaskWithLock(String s) {
        re = new ReentrantLock();
        setLocalString(s);
    }

    public Long call() throws InterruptedException {

        // Count each character in localString
        char[] charArray = localString.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            boolean done = false;
            while (!done) {
                // Check if this thread has the lock
                if (re.tryLock()) {
                    // Increment the shared counter
                    localCounter++;
                    done = true;
                    re.unlock();
                } else {
                    Thread.sleep(7);
                }
            }
        }
        return localCounter;
    }

    private void setLocalString(String localString) {
        this.localString = localString;
    }

}
