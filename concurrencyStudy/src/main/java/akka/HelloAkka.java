package akka;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import com.typesafe.config.ConfigFactory;

/**
 * @author lixiongxiong
 * @date 2019/3/3
 * @description akka first demo
 */
public class HelloAkka {

    public static void main(String[] args) {
        //管理和维护Actor的系统，一般来说一个应用程序只需要一个即可
        //第一个参数表示系统名称，第二个参数为配置文件
        ActorSystem system = ActorSystem.create("Hello",ConfigFactory.load("samplehello.conf"));
        ActorRef ref = system.actorOf(Props.create(HelloWorld.class, "helloworld"));
        System.out.println("Hello World Actor Path:" + ref.path());
        system.stop(ref);
    }

    static class Greeter extends UntypedActor {

        Greeter(){

        }

        Greeter(String name) {
            System.out.println("greeter constructor " + name);
        }

        private static enum Msg {
            GREET, DONE;
        }

        @Override
        public void onReceive(Object message) throws Throwable {
            if (message == Msg.GREET) {
                System.out.println("Hello world");
                getSender().tell(Msg.DONE, getSelf());
            } else {
                unhandled(message);
            }
        }
    }

    static class HelloWorld extends UntypedActor {

        HelloWorld(String name) {
            System.out.println("hello world constructor " + name);
        }

        ActorRef greeter;

        @Override
        public void preStart() throws Exception {
            greeter = getContext().actorOf(Props.create(Greeter.class), "greeter");
            System.out.println("Greeter Actor Path: " + greeter.path());
            greeter.tell(Greeter.Msg.GREET, getSelf());
        }

        @Override
        public void onReceive(Object message) throws Throwable {
            if (message == Greeter.Msg.DONE) {
                greeter.tell(Greeter.Msg.GREET, getSelf());
                getContext().stop(getSelf());
            } else {
                unhandled(message);
            }
        }
    }


}
