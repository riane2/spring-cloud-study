package akka;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 消息收件箱：InBox
 * <p>
 * 所有Actor之间的通信都是通过消息进行的：这是否意味着我们必须构建一个Actor才能控制整个系统呢？？
 * 答案是否定的，我们并不一定要这么做，Akka框架已经为我们准备了一个叫“收件箱”的组件，使用收件箱，可以很方便的
 * 对Actor进行消息的发送和接受
 */
public class InBoxDemo {

    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("inBox");
        ActorRef worker = system.actorOf(Props.create(MyWorker.class), "myWorker");
        /**
         * 根据system创建与之绑定的邮箱
         */
        Inbox inbox = Inbox.create(system);
        /**
         * 使用邮箱监测worker
         */
        inbox.watch(worker);


        inbox.send(worker, MyWorker.Msg.WORKING);
        inbox.send(worker, MyWorker.Msg.DONE);

        /**
         * 消息回执接受
         */
        while (true) {
            try {
                Object msg = inbox.receive(Duration.create(10, TimeUnit.MILLISECONDS));
                if (msg == MyWorker.Msg.DONE) {
                    System.out.println("myworker is closed");
                } else if (msg instanceof Terminated) {
                    System.out.println("MyWorker is dead");
                    system.terminate();
                    break;
                } else {
                    System.out.println(String.valueOf(msg));
                }
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
        }

    }

    static class MyWorker extends UntypedActor {

        LoggingAdapter log = Logging.getLogger(getContext().system(), this);

        private enum Msg {
            WORKING, DONE;
        }

        @Override
        public void onReceive(Object message) throws Throwable {
            if (message == Msg.WORKING) {
                log.info("I am working...");
                getSender().tell("Hello Working", getSelf());
            } else if (message == Msg.DONE) {
                log.info("I am done...");
                getSender().tell(Msg.DONE, getSelf());
                getContext().stop(getSelf());
            } else {
                unhandled(message);
            }
        }
    }

}
