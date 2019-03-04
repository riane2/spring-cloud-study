package akka;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.pattern.Patterns;
import com.typesafe.config.ConfigFactory;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

/**
 * 问询模式：Future
 */
public class FutureInActorDemo {


    public static void main(String[] args) throws Exception {
        ActorSystem system = ActorSystem.create("strategy", ConfigFactory.load("akka.config"));
        ActorRef printActor = system.actorOf(Props.create(PrintActor.class), "PrintActor");
        ActorRef workerActor = system.actorOf(Props.create(WorkerActor.class), "WorkerActor");


        //使用ask方法给worker发送消息，内容是5
        Future<Object> future = Patterns.ask(workerActor, 5, 1000);

        //等等future返回
        int result = (int) Await.result(future, Duration.create(3, TimeUnit.SECONDS));
        System.out.println("result:" + result);

        //不等待返回值，直接重定向到其他actor，有返回值来的时候将会重定向到printActor
        Future<Object> future1 = Patterns.ask(workerActor, 8, 1000);

        //pipe不会阻塞程序
        Patterns.pipe(future1, system.dispatcher()).to(printActor);

        workerActor.tell(PoisonPill.getInstance(), ActorRef.noSender());
    }


    static class PrintActor extends UntypedActor {
        private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

        @Override
        public void onReceive(Object o) throws Throwable {
            log.info("akka.future.PrintActor.onReceive:" + o);
            if (o instanceof Integer) {
                System.out.println(getSender().path());
                log.info("print:" + o);
            } else {
                unhandled(o);
            }
        }
    }

    static class WorkerActor extends UntypedActor {
        private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

        @Override
        public void onReceive(Object o) throws Throwable {
            log.info("akka.future.WorkerActor.onReceive:" + o);
            System.out.println(getSender().path());

            if (o instanceof Integer) {
                Thread.sleep(1000);
                int i = Integer.parseInt(o.toString());
                getSender().tell(i * i, getSelf());
            } else if (o instanceof Terminated) {
                System.out.println("worker is Terminated");
                getContext().system().terminate();
            } else {
                unhandled(o);
            }
        }
    }

}
