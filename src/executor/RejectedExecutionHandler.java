package executor;

public class RejectedExecutionHandler {
    public void rejectedExecution(Runnable r, CustomThreadPoolExecutor executor) {
        System.err.println("[Rejected] Task " + r + " was rejected due to overload!");
    }
}