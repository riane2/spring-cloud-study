package akka;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import scala.Option;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

/**
 * 父子监督策略：一对一和多对一
 * <p>
 * 一对一表示：父Actor检测到有一个子Actor发生异常或者错误之后只对发生错误的进行相应的操作，如重启或者不管继续执行等
 * 多对一表示：只要有一个异常，所有的子Actor都必须进行相应的操作。这种适用于子Actor耦合度较高的场合
 */
public class StrategyDemo {


    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("strategy");
        ActorRef superVisor = system.actorOf(Props.create(Supervisor.class), "superVisor");
        superVisor.tell(Props.create(RestartActor.class), ActorRef.noSender());

        ActorSelection selection = system.actorSelection("akka://strategy/user/superVisor/restartActor");

        //selection.tell(RestartActor.Msg.RESTART, ActorRef.noSender());
        //测试一分钟重启最多3次
        //看日志：postStop 方法被调用，表示Actor被关闭了
        for (int i = 0; i < 10; i++) {
            selection.tell(RestartActor.Msg.RESTART, ActorRef.noSender());
        }

    }


    static class Supervisor extends UntypedActor {

        LoggingAdapter log = Logging.getLogger(getContext().system(), this);

        /**
         * 运行的Actor发生错误后，在一分钟内重试3次，如果超过这个频率，那么直接杀死Actor
         */
        private final SupervisorStrategy strategy = new OneForOneStrategy(3, Duration.create(1, TimeUnit.MINUTES), (t) -> {
            if (t instanceof ArithmeticException) {
                System.out.println("meet ArithmeticException,just resume");
                return SupervisorStrategy.resume(); //继续执行
            } else if (t instanceof NullPointerException) {
                System.out.println("meet NullPointerException,just restart");
                return SupervisorStrategy.restart();
            } else if (t instanceof IllegalArgumentException) {
                System.out.println("meet IllegalArgumentException,just restart");
                return SupervisorStrategy.stop();
            }
            return SupervisorStrategy.escalate();//向上抛出，由顶层的Actor处理
        });

        /**
         * 使用我们自定义的策略
         *
         * @return
         */
        @Override
        public SupervisorStrategy supervisorStrategy() {
            return strategy;
        }

        @Override
        public void onReceive(Object message) throws Throwable {
            if (message instanceof Props) {
                log.info("收到Props对象，准备生成子Actor对象");
                getContext().actorOf((Props) message, "restartActor");
            } else {
                unhandled(message);
            }
        }
    }

    static class RestartActor extends UntypedActor {

        private enum Msg {
            RESTART, DONE;
        }

        @Override
        public void preStart() throws Exception {
            System.out.println("preStart hashCode " + this.hashCode());
            ;
        }

        @Override
        public void postStop() throws Exception {
            System.out.println("postStop hashCode " + this.hashCode());
            ;
        }

        @Override
        public void preRestart(Throwable reason, Option<Object> message) throws Exception {
            System.out.println("preRestart hashCode " + this.hashCode());
        }

        @Override
        public void postRestart(Throwable reason) throws Exception {
            super.postRestart(reason);
            System.out.println("postRestart hashCode " + this.hashCode());
        }

        @Override
        public void onReceive(Object message) throws Throwable {
            if (message == Msg.DONE) {
                getContext().stop(getSelf());
            } else if (message == Msg.RESTART) {
                //抛出空指针异常
                System.out.println(((Object) null).toString());
                //抛出ArithmeticException
                System.out.println(0 / 0);
            } else {
                unhandled(message);
            }
        }
    }

}
