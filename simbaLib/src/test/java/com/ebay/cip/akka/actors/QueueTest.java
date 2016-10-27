//package com.ebay.cip.akka.actors;
//
//import akka.actor.ActorRef;
//import akka.actor.ActorSystem;
//import akka.actor.Props;
//import akka.testkit.TestActorRef;
//import com.ebay.cip.framework.messages.CipMessage;
//import com.ebay.cip.framework.messages.QueueActionEnum;
//import com.ebay.cip.framework.messages.QueueMessage;
//import com.ebay.es.cipconfig.cipconfig.StorageClientConfig;
//import com.ebay.es.cipconfig.cipconfig.common.ConfigConstants;
//import com.ebay.es.cipconfig.cipconfig.core.ConfigContext;
//import com.ebay.es.cipconfig.cipconfig.core.ConfigParams;
//import com.ebay.es.cipconfig.cipconfig.core.ConfigTarget;
//import org.junit.AfterClass;
//import org.junit.BeforeClass;
//import org.junit.Test;
//
///**
// * Created by kmanekar on 4/6/2015.
// */
//public class QueueTest {
//
//    @BeforeClass
//    public static void setup() {
//        ConfigParams param = new ConfigParams();
//        param.setProject(ConfigConstants.PROJECT_CIP);
//        param.setTarget(ConfigTarget.Global);
//        param.setVersion(ConfigConstants.VERSION_1_0_0);
//
//        ConfigContext.registerConfig(param);
//        StorageClientConfig.getInstance().init();
//    }
//
//    @AfterClass
//    public static void tearDown() {}
//
//
//   //@Test
//   public void testQueue() {
//
//       final ActorSystem system = ActorSystem.create("QueueSystem");
//       final ActorRef splitterActor = system.actorOf(Props.create(SplitterActor.class), "splitterActor");
//       final ActorRef apiActor = system.actorOf(Props.create(APIActor.class),"apiActor");
//
//       // Splitter actors Job
//       final Props props = Props.create(QueueActor.class);
//       final TestActorRef<QueueActor> queueActorRef = TestActorRef.create(system, props, "queueActor");
//
//       // create Queue
//       QueueMessage msage = new QueueMessage();
//       msage.setBaseMessage(new CipMessage(null));
//       msage.setAction(QueueActionEnum.CREATE);
//       System.out.println("Create Queue...");
//       queueActorRef.tell(msage, splitterActor);
//
//       int i = 1;
//       while(i <= 10000) {
//           QueueMessage message = new QueueMessage();
//           message.setBaseMessage(new CipMessage(null));
//           message.setAction(QueueActionEnum.ENQUEUE);
//           System.out.println("Enqueuing...");
//           queueActorRef.tell(message, splitterActor);
//           i++;
//       }
//
//       // API actors Job
//       QueueMessage message = new QueueMessage();
//       message.setBaseMessage(new CipMessage(null));
//       message.setAction(QueueActionEnum.DEQUEUE);
//       System.out.println("Dequeuing...");
//       queueActorRef.tell(message, apiActor);
//
//
//       try {
//           Thread.sleep(100000000);
//       } catch (Exception e) {
//
//       }
//
//
//   }
//
//
//}
