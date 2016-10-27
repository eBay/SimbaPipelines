//package com.ebay.cip.akka.actors;
//
//import akka.actor.ActorRef;
//import akka.actor.ActorSelection;
//import akka.actor.ActorSystem;
//import akka.actor.Props;
//import com.ebay.cip.framework.messages.CipMessage;
//import com.ebay.cip.framework.messages.GateKeeperMessage;
//import com.ebay.cip.framework.test.job.TestJob;
//import org.junit.AfterClass;
//import org.junit.BeforeClass;
//import org.junit.Test;
//
///**
// * Created by hachong on 6/3/2015.
// */
//public class CipGateKeeperActorTest {
//    static ActorSystem  system;
//    static ActorRef gateKeeperActor,testActor;
//
//
//    @BeforeClass
//    public static void setup() {
//        System.out.println("Setup!");
//        system = ActorSystem.create("QueueSystem");
//        gateKeeperActor = system.actorOf(Props.create(GateKeeperActor.class), "gateKeeperActor");
//        testActor = system.actorOf(Props.create(TestActor.class),"testActor");
//    }
//
//
//    @AfterClass
//    public static void tearDown() {
//        System.out.println("Test Complete, Tear Down!");
//    }
//
//   @Test
//   public void testGateKeeper() {
//
//       ActorRef destination = testActor;
//       CipMessage originMessage = new CipMessage(new TestJob());
//       GateKeeperMessage message = new GateKeeperMessage("TEST",originMessage,destination);
//       for(int i = 0 ; i < 100; i++) {
//           gateKeeperActor.tell(message, null);
//       }
//       try {
//           Thread.sleep(200000);
//       } catch (Exception e) {
//
//      }
//   }
//}
