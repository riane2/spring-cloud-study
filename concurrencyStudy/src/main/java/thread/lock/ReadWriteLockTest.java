package thread.lock;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ReadWriteLockTest {

    private static ReentrantLock reentrantLock = new ReentrantLock();

    private static ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    private static Lock readLock = readWriteLock.readLock();

    private static Lock writeLock = readWriteLock.writeLock();

    private AtomicInteger value = new AtomicInteger(100);

    public void handleRead(Lock lock) {
        try {
            lock.lock();
            Thread.sleep(1000);
            System.out.println("读数据：" + value.get());
            //System.out.println(value.get());
        } catch (Exception ex) {

        } finally {
            lock.unlock();
        }
    }

    public void handleWrite(Lock lock, int value) {
        try {
            lock.lock();
            Thread.sleep(1000);
            System.err.println("写数据：" + value);
            this.value.set(value);
            ;
        } catch (Exception ex) {

        } finally {
            lock.unlock();
        }
    }

    public static void main(String[] args) {
        final ReadWriteLockTest readWriteLockTest = new ReadWriteLockTest();
        Runnable readRunnable = new Runnable() {
            public void run() {
                //readWriteLockTest.handleRead(reentrantLock);
                readWriteLockTest.handleRead(readLock);
            }
        };

        Runnable writeRunnable = new Runnable() {
            public void run() {
                //readWriteLockTest.handleWrite(reentrantLock, new Random().nextInt());
                readWriteLockTest.handleWrite(writeLock, new Random().nextInt());
            }
        };
        for (int i = 0; i < 5; i++) {
            new Thread(writeRunnable).start();
            //new Thread(readRunnable).start();
        }

        for (int i = 0; i < 10; i++) {
            new Thread(readRunnable).start();
        }
        /*for (int i = 0; i < 5; i++) {
            new Thread(writeRunnable).start();
            //new Thread(readRunnable).start();
        }*/


    }
}
