package java8.complete;

import java.util.concurrent.CompletableFuture;

public class TestCompletableFuture {
    public static void main(String[] args) {
        CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "hello";
        }).thenApplyAsync((s) -> s + " world")
                .thenApplyAsync((ss) -> ss + " !")
                .thenAcceptAsync(System.out::println).join();

        /**
         * thenComposeAsync(fn):上面的执行结果传递下来给下一个CompletionStage进行执行,fn接受另一个CompletionStage对象
         */
        CompletableFuture.supplyAsync(() -> calc(50)).
                thenComposeAsync((i) -> CompletableFuture.supplyAsync(() -> calc(i))).
                thenAcceptAsync(System.out::println).join();

        //test1();
        //test2();
        //test3();
        //processHandle();
    }

    private static void test1() {

        /**
         * 以Async结尾的方法都是可以异步执行的，如果指定了线程池，会在指定的线程池中执行，
         * 如果没有指定，默认会在ForkJoinPool.commonPool()中执行
         *
         * 方法表示进行值的变换。
         * thenApply:
         * thenApplyAsync:它们的入参是上一个阶段计算后的结果，返回值是经过转化后结果
         *
         * 方法表示进行值的消耗。
         * thenAccept:
         * thenAcceptAsync:针对结果进行消耗，因为他的入参是Consumer，有入参无返回值
         *
         */
        CompletableFuture.supplyAsync(() -> "hello").thenApplyAsync((s) -> s + " World").
                thenAcceptAsync(System.out::println);

        /**
         * thenRun:
         * thenRunAsync:表示对上一步的计算结果不关心，执行下一个操作
         * 它的入参是一个Runnable的实例，表示当得到上一步的结果时的操作，即当上一步执行完了才能执行入参函数
         */
        CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "Hello";
        }).thenRunAsync(() -> System.out.println("Hello riane"));
        while (true) {

        }
    }

    private static int calc(int i) {
        return i / 2;
    }

    private static void test2() {


        /**
         * thenCombine:
         * thenCombineAsync(other,fn): 结合两个CompletionStage的结果，进行转化后返回
         * 它需要前面结果的处理返回值，并且other代表的CompletionStage也要返回值之后，
         * 利用这两个返回值，fn函数进行转换后返回指定类型的值。
         */
        String result = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "hello";
        }).thenCombine(CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "world";
        }), (s1, s2) -> s1 + " " + s2).join();
        System.out.println(result);

        /**
         * thenAcceptBoth：结合两个CompletionStage的结果，进行消耗
         * 它需要原来的处理返回值，并且other代表的CompletionStage也要返回值之后，利用这两个返回值，进行消耗。
         */
        CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "hello";
        }).thenAcceptBoth(CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "world";
        }), (s1, s2) -> System.out.println(s1 + " " + s2));
        /*
        //必须加，否则因为异步执行主程序将立马退出
        while (true) {
        }*/


        /**
         * runAfterBoth:
         * runAfterBothAsync: 在两个CompletionStage都运行完之后执行
         * 不关心这两个CompletionStage的执行结果，只关心这两个CompletionStage是否执行完毕，执行完毕之后再进行操作（Runnable）。
         */
        CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "s1";
        }).runAfterBothAsync(CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "s2";
        }), () -> System.out.println("Hello riane"));


        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void test3() {
        /**
         * applyToEither:
         * applyToEitherAsync: 两个CompletionStage，谁计算的快，我就用那个CompletionStage的结果进行下一步的转化操作:
         * 我们现实开发场景中，总会碰到有两种渠道完成同一个事情，所以就可以调用这个方法，找一个最快的结果进行处理
         */
        String result = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "s1";
        }).applyToEitherAsync(CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "s2";
        }), (s) -> s).join();
        System.out.println(result);


        /**
         * acceptEither:
         * acceptEitherAsync:两个CompletionStage，谁计算的快，我就用那个CompletionStage的结果进行下一步的消耗操作
         */
        CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "s1";
        }).acceptEither(CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "s2";
        }), (s) -> System.out.println(s + " Hello world"));
        //while (true){}

        /**
         * runAfterEither:
         * runAfterEitherAsync:两个CompletionStage，任何一个完成了都会执行下一步的操作（Runnable）
         */
        CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "s1";
        }).runAfterEither(CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(2200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "s2";
        }), () -> System.out.println("hello world..."));
        while (true) {
        }
    }

    private static void processHandle() {
        /**
         * exceptionally:当运行时出现了异常，可以通过exceptionally进行补偿。
         */
        String result = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (1 == 1) {
                throw new RuntimeException("测试一下异常情况");
            }
            return "s1";
        }).exceptionally(e -> {
            System.out.println(e.getMessage());
            return "hello world";
        }).join();
        System.out.println(result);

        /**
         * whenComplete:
         * whenCompleteAsync: 当运行完成时，对结果的记录。这里的完成时有两种情况，一种是正常执行，返回值。
         * 另外一种是遇到异常抛出造成程序的中断。这里为什么要说成记录，因为这几个方法都会返回CompletableFuture，
         * 当Action执行完毕后它的结果返回原始的CompletableFuture的计算结果或者返回异常。所以不会对结果产生任何的作用
         */
        String result2 = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (1 == 1) {
                throw new RuntimeException("测试一下异常情况");
            }
            return "s2";
        }).thenApplyAsync(
                (s) -> s + " s2-2"
        ).whenComplete((s, t) -> {
            System.out.println(s);
            if (t != null) {
                System.out.println(t.getMessage());
            }
        }).exceptionally(e -> {
            System.out.println(e.getMessage());
            return "hello world2";
        }).join();
        /**
         * 观察输出结果：发现如果使用了exceptionall，并且前面的确有异常发生，那就会对最终的结果产生影响,即不会执行whenComplete里面的内容，
         * 因为它没有方法或者方式来返回如果没有异常时的正确的值，这也就引出下面我们要介绍的handle。
         * 但是如果没有异常产生，result2它就会返回正确的值
         */
        if (result2 == null || result2.isEmpty()) {
            System.out.println("返回值为空");
        } else {
            System.out.println(result2);
        }

        /**
         * handle:
         * handleAsync:运行完成时，对结果的处理。这里的完成时有两种情况，一种是正常执行，返回值。另外一种是遇到异常抛出造成程序的中断
         */
        //有异常发生时
        String result3 = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //出现异常
            if (1 == 1) {
                throw new RuntimeException("测试一下异常情况");
            }
            return "s3";
        }).handle((s, t) -> {
            if (t != null) {
                System.err.println(t.getMessage());
                //如果出现异常，s的结果将为null
                return s + " hello world 3 error";
            }
            return s;
        }).join();
        System.out.println(result3);

        //没有异常发生时
        String result4 = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "s4";
        }).handle((s, t) -> {
            if (t != null) {
                return "hello world 4";
            }
            return s;
        }).join();
        System.out.println(result4);
    }

}

