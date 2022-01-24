package Lesson5Concurrent;

import java.util.concurrent.Callable;

class CountTask implements Callable<Long> {

    private Long localCounter = (long) 0;
    private String localString = null;

    public CountTask(String s) {
        setLocalString(s);
    }

    public Long call() {
        // Count each character in localString
        char[] charArray = localString.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            // Store the value in a variable in this thread
            localCounter++;
        }
        return localCounter;
    }

    private void setLocalString(String localString) {
        this.localString = localString;
    }

}
