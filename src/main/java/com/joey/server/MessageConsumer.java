package com.joey.server;

import com.joey.common.JobQueue;
import com.joey.common.Pair;
import com.joey.handler.MsgConsumerExceptionHandler;
import com.joey.worker.QueryWorker;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by joey on 2014-7-2.
 */
public class MessageConsumer extends Thread {

    private JobQueue<Pair> jobQueue;
    private ExecutorService executorService;
    private MsgConsumerExceptionHandler msgExceptionHandler;

    public MessageConsumer(JobQueue<Pair> jobQueue) {
        this.jobQueue = jobQueue;
        this.msgExceptionHandler = new MsgConsumerExceptionHandler();
        this.executorService = Executors.newCachedThreadPool();
        this.setUncaughtExceptionHandler(this.msgExceptionHandler);
    }

    @Override
    public void run() {
        try {
            while (true) {
                if (jobQueue.isEmpty()) {
                    Thread.currentThread().sleep(10);
                    continue;
                }

                Pair pair = jobQueue.poll();
                QueryWorker queryWorker = new QueryWorker(pair);
                executorService.submit(queryWorker);
            }
        } catch (InterruptedException e) {
            // TODO need define exception handler to process exception
            e.printStackTrace();
        }
    }

    public void setJobQueue(JobQueue<Pair> jobQueue) {
        this.jobQueue = jobQueue;
    }
}
