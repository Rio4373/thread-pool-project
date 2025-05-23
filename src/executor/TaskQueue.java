package executor;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TaskQueue extends LinkedBlockingQueue<Runnable> {
    public TaskQueue(int capacity) {
        super(capacity);
    }
}