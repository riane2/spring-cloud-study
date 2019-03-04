package akka;

import akka.actor.*;
import scala.Option;

/**
 * akka的生命周期
 */
public class AkkaLifeCycle {

    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("LifeCycle");
        ActorRef greeter = system.actorOf(Props.create(Greeter.class), "greeter");
        system.actorOf(Props.create(Watcher.class, greeter));
        greeter.tell(Greeter.Msg.GREET, ActorRef.noSender());
        greeter.tell(Greeter.Msg.WORKING, ActorRef.noSender());
        greeter.tell(Greeter.Msg.DONE, ActorRef.noSender());
        greeter.tell(Greeter.Msg.CLOSE, ActorRef.noSender());
        //greeter.tell(PoisonPill.getInstance(), ActorRef.noSender());
    }

    static class Watcher extends UntypedActor {

        Watcher(ActorRef actorRef) {
            /**
             * 监听传入的Actor
             */
            getContext().watch(actorRef);
        }

        @Override
        public void onReceive(Object message) throws Throwable {
            if (message == Greeter.Msg.WORKING) {
                System.out.println("watcher recieved msg " + Greeter.Msg.WORKING);
            }
            if (message instanceof Terminated) {
                System.out.println(((Terminated) message).getActor().path() + " is terminated,shutting down system");
            }
            getContext().system().terminate();
        }
    }

    static class Greeter extends UntypedActor {

        Greeter() {

        }

        Greeter(String name) {
            System.out.println("greeter constructor " + name);
        }

        private enum Msg {
            WORKING, GREET, DONE, CLOSE;
        }

        @Override
        public void preStart() throws Exception {
            System.out.println("preStart方法可以预初始化一些数据");
            ;
        }

        @Override
        public void postStop() throws Exception {
            System.out.println("postStop方法可以垃圾回收或者关闭一些数据");
        }

        @Override
        public void preRestart(Throwable reason, Option<Object> message) throws Exception {
            System.out.println("preRestart方法可以在重启之前初始化一些事，异常为" + reason.toString());
        }

        @Override
        public void postRestart(Throwable reason) throws Exception {
            System.out.println("postRestart方法可以在重启之后初始化一些事，异常为" + reason.toString());
        }

        @Override
        public void onReceive(Object message) throws Throwable {
            if (message == Msg.GREET) {
                System.out.println("Hello greeter...");
            } else if (message == Msg.WORKING) {
                System.out.println("I am working...");
            } else if (message == Msg.DONE) {
                System.out.println("I am done...");
            } else if (message == Msg.CLOSE) {
                System.out.println("I am closing...");
                getSender().tell(Msg.DONE, getSelf());
                getContext().stop(getSelf());
            } else {
                unhandled(message);
            }
        }
    }
}
