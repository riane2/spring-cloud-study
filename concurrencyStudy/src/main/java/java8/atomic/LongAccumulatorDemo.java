package java8.atomic;

import java.util.concurrent.atomic.LongAccumulator;

/**
 * @author lixiongxiong
 * @date 2019/3/3
 * @description 改进的LongAddr
 */
public class LongAccumulatorDemo {

    static LongAccumulator accumulator = new LongAccumulator((x, y) -> x * y, 1);

    public static void main(String[] args) {
        while (accumulator.get() < 10) {
            accumulator.accumulate(2);
        }
        System.out.println(accumulator.get());
        System.out.println(accumulator.doubleValue());
        System.out.println(accumulator.floatValue());
        System.out.println(accumulator.longValue());
    }


}
