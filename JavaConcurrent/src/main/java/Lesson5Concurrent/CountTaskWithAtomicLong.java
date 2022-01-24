package Lesson5Concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;

class CountTaskWithAtomicLong implements Callable<Long> {

    private static AtomicLong localCounter = new AtomicLong(0);
    private String localString = null;

    public CountTaskWithAtomicLong(String s) {
        setLocalString(s);
    }

    public Long call() {
        // Count each character in localString
        char[] charArray = localString.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            // Store the value in a shared AtomicLong
            localCounter.incrementAndGet();
        }
        return localCounter.get();
    }

    private void setLocalString(String localString) {
        this.localString = localString;
    }

}
