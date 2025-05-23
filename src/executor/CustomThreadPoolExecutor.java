package executor;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class CustomThreadPoolExecutor implements CustomExecutor {

    protected final int corePoolSize;
    protected final int maxPoolSize;
    protected final long keepAliveTime;
    protected final TimeUnit timeUnit;
    protected final int queueSize;
    protected final int minSpareThreads;

    protected volatile boolean isShutdown = false;
    protected final Set<Worker> workers = Collections.synchronizedSet(new HashSet<>());
    protected final List<TaskQueue> queues = new ArrayList<>();
    private final AtomicInteger threadCounter = new AtomicInteger(1);

    private final Object workerLock = new Object();
    private final ThreadFactory threadFactory;
    private final RejectedExecutionHandler rejectedExecutionHandler;

    private int currentQueueIndex = 0;

    public CustomThreadPoolExecutor(int corePoolSize, int maxPoolSize, long keepAliveTime,
                                    TimeUnit timeUnit, int queueSize, int minSpareThreads) {
        this.corePoolSize = corePoolSize;
        this.maxPoolSize = maxPoolSize;
        this.keepAliveTime = keepAliveTime;
        this.timeUnit = timeUnit;
        this.queueSize = queueSize;
        this.minSpareThreads = minSpareThreads;

        this.threadFactory = new ThreadFactory();
        this.rejectedExecutionHandler = new RejectedExecutionHandler();

        for (int i = 0; i < corePoolSize; i++) {
            addWorker();
        }
    }

    private void addWorker() {
        TaskQueue queue = new TaskQueue(queueSize);
        queues.add(queue);
        Worker worker = new Worker(queue, "MyPool-worker-" + threadCounter.getAndIncrement(), this);
        workers.add(worker);
        System.out.println("[ThreadFactory] Creating new thread: " + worker.getName());
        worker.start();
    }

    @Override
    public void execute(Runnable command) {
        if (isShutdown) {
            System.err.println("[Rejected] Task was rejected after shutdown.");
            return;
        }

        synchronized (workerLock) {
            boolean taskAdded = false;
            for (int i = 0; i < queues.size(); i++) {
                TaskQueue queue = queues.get((currentQueueIndex + i) % queues.size());
                if (queue.remainingCapacity() > 0) {
                    try {
                        queue.put(command);
                        System.out.println("[Pool] Task accepted into queue #" + queues.indexOf(queue) + ": " + command);
                        currentQueueIndex = (currentQueueIndex + 1) % queues.size();
                        taskAdded = true;
                        break;
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }

            if (!taskAdded) {
                if (workers.size() < maxPoolSize) {
                    addWorker();
                    execute(command);
                } else {
                    rejectedExecutionHandler.rejectedExecution(command, this);
                }
            }
        }
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        if (isShutdown) return null;

        FutureTask<T> futureTask = new FutureTask<>(task);
        execute(futureTask);
        return futureTask;
    }

    @Override
    public void shutdown() {
        isShutdown = true;
        for (Worker worker : workers) {
            worker.interrupt();
        }
    }

    @Override
    public void shutdownNow() {
        isShutdown = true;
        List<Worker> workerList;
        synchronized (workers) {
            workerList = new ArrayList<>(workers);
            workers.clear();
        }
        for (Worker worker : workerList) {
            worker.interrupt();
        }
    }

    public void waitForTermination() throws InterruptedException {
        List<Worker> workerList;
        synchronized (workers) {
            workerList = new ArrayList<>(workers);
        }
        for (Worker worker : workerList) {
            worker.join();
        }
    }
}