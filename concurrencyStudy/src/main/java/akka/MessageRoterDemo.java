package akka;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.routing.ActorRefRoutee;
import akka.routing.RoundRobinRoutingLogic;
import akka.routing.Routee;
import akka.routing.Router;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 消息路由：
 * <p>
 * 通过消息路由，从一组提供相同服务的对等Actor中找到最合适的Actor来进行消息处理
 * <p>
 * 轮询策略，随机策略，最空闲策略,组内广播等
 */
public class MessageRoterDemo {

    static AtomicBoolean flag = new AtomicBoolean(true);

    public static void main(String[] args) throws InterruptedException {
        ActorSystem system = ActorSystem.create("router");
        ActorRef watcher = system.actorOf(Props.create(Watcher.class), "myWatcher");
        int i = 1;
        while (flag.get()) {
            watcher.tell(MyWorker.Msg.WORKING, ActorRef.noSender());
            if (i % 2 == 0) {
                watcher.tell(MyWorker.Msg.DONE, ActorRef.noSender());
            }
            i++;
            Thread.sleep(100);
        }

    }

    static class MyWorker extends UntypedActor {

        LoggingAdapter log = Logging.getLogger(getContext().system(), this);

        private enum Msg {
            WORKING, DONE;
        }

        @Override
        public void onReceive(Object message) throws Throwable {
            if (message == MyWorker.Msg.WORKING) {
                log.info("I am working...");
                getSender().tell("Hello Working", getSelf());
            } else if (message == MyWorker.Msg.DONE) {
                log.info("I am done...");
                getSender().tell(MyWorker.Msg.DONE, getSelf());
                getContext().stop(getSelf());
            } else {
                unhandled(message);
            }
        }
    }


    static class Watcher extends UntypedActor {

        LoggingAdapter log = Logging.getLogger(getContext().system(), this);

        private Router router;

        {
            List<Routee> routees = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                ActorRef worker = getContext().actorOf(Props.create(MyWorker.class), "myWorker_" + i);
                getContext().watch(worker);
                routees.add(new ActorRefRoutee(worker));
            }
            router = new Router(new RoundRobinRoutingLogic(), routees);
        }

        @Override
        public void onReceive(Object message) throws Throwable {
            if (message instanceof MyWorker.Msg) {
                router.route(message, getSender());
            } else if (message instanceof Terminated) {
                router = router.removeRoutee(((Terminated) message).actor());
                log.info(((Terminated) message).actor().path() + " is closed,routees= " + router.routees().size());
                if (router.routees().size() == 0) {
                    log.info("Close System");
                    MessageRoterDemo.flag.compareAndSet(true, false);
                    getContext().system().terminate();
                }
            } else {
                unhandled(message);
            }
        }
    }


}
