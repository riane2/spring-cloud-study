package java8.lock;

import java.util.concurrent.locks.StampedLock;

/**
 * 乐观模式的读写锁：乐观模式的读策略，这种策略类似于无锁操作，使得其不会阻塞写线程
 *
 * @author lixiongxiong
 * @date 2019/3/2
 * @description Stamped Lock Demo
 */
public class StampedLockDemo {
    public static void main(String[] args) {
        Long a  = 1L;
        int i = 1 << 7;
        System.out.println(Integer.toBinaryString(i));
        System.out.println(Integer.toBinaryString(~i));
        System.out.println(~i);

        System.out.println(a << 7);
    }

    static class Point {
        private double x, y;
        private final StampedLock stampedLock = new StampedLock();

        /**
         * 排他锁
         *
         * @param deltax
         * @param deltay
         */
        private void move(double deltax, double deltay) {
            long stamp = stampedLock.writeLock();
            try {
                x += deltax;
                y += deltay;
            } finally {
                stampedLock.unlockWrite(stamp);
            }

        }

        private double disFromOrigin() {
            long stamp = stampedLock.tryOptimisticRead(); //试图尝试一次乐观锁，它返回类似时间戳的邮戳整数，他就是一次锁获取的凭证
            double conX = x, conY = y;
            //判断stamp是否被其他线程修改过
            if (!stampedLock.validate(stamp)) {
                //如果当前对象正在被修改，则获取悲观锁的请求可能导致线程挂起
                stamp = stampedLock.readLock();//获取悲观锁
                try {
                    conX = x;
                    conY = y;
                } finally {
                    stampedLock.unlockRead(stamp);
                }
            }
            return Math.sqrt(conX * conX + conY * conY);
        }

    }
}
