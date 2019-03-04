package akka;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.japi.Procedure;
import akka.pattern.Patterns;
import scala.concurrent.Await;

/**
 * 状态切换
 */
public class StateSwitchDemo {

    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("state");
        ActorRef baby = system.actorOf(Props.create(BabyActor.class), "baby");
        baby.tell("sleep",ActorRef.noSender());
        baby.tell("sleep",ActorRef.noSender());

        baby.tell("play",ActorRef.noSender()); //状态转变

        baby.tell("play",ActorRef.noSender());
        baby.tell("sleep",ActorRef.noSender());
    }

    static class BabyActor extends UntypedActor {

        Procedure<Object> happy;
        Procedure<Object> angry;

        {
            angry = (message) -> {
                System.out.println("AngryApply:" + message);
                if (message.equals("sleep")) {
                    getSender().tell("I am already anger", getSelf());
                    System.out.println("I am already anger");
                } else if (message.equals("play")) {
                    getSender().tell("I like playing", getSelf());
                    getContext().become(happy);
                }
            };
            happy = (message) -> {
                System.out.println("HappyApply:" + message);
                if (message.equals("play")) {
                    getSender().tell("I am already happy", getSelf());
                    System.out.println("I am already happy");
                } else if (message.equals("sleep")) {
                    getSender().tell("I don't want to sleep", getSelf());
                    getContext().become(angry);
                }
            };
        }

        @Override
        public void onReceive(Object message) throws Throwable {
            if (message.equals("sleep")) {
                /**
                 * become用于切换状态，入参是Procedure表示一种状态，其内部方法可以封装处理消息的逻辑
                 *<p>
                 * 当状态切换完成之后，如果后续有消息进来的话，就不会由onReceive来进行消息处理了，而是
                 * 当前状态的Procedure的内部函数apply来进行处理。从而可以根据不同的状态进行不同的逻辑处理
                 * </p>
                 *
                 */
                getContext().become(angry);
            } else {
                getContext().become(happy);
            }
        }
    }

}
