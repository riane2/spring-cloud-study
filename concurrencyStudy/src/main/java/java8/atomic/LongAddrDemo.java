package java8.atomic;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.LongAdder;

/**
 * @author lixiongxiong
 * @date 2019/3/2
 * @description LongAdder demo
 */
public class LongAddrDemo {
    private static LongAdder nums = new LongAdder();

    private static final long MAX = 100000;

    private static CountDownLatch countDownLatch = new CountDownLatch(4);

    static class LongAddrThread implements Runnable {
        private long startTime;

        LongAddrThread(long startTime) {
            this.startTime = startTime;
        }

        @Override
        public void run() {
            long v = nums.sum();
            while (v < MAX) {
                nums.increment();
                v = nums.sum();
            }
            System.out.println("Spend Time: " + (System.currentTimeMillis() - startTime));
            System.out.println("value is " + v);
            countDownLatch.countDown();

        }
    }

    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(4);

        long startTime = System.currentTimeMillis();

        LongAddrThread thread = new LongAddrThread(startTime);

        for (int i = 0; i < 4; i++) {
            executorService.submit(thread);
        }

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        executorService.shutdown();
        System.out.println(nums.sum());

    }
}
