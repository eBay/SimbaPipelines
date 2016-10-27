//package com.ebay.cip.akka.actors;
//
//import akka.actor.UntypedActor;
//import com.ebay.cip.framework.messages.CipMessage;
//
///**
// * This class is use for testing purpose
// * Created by hachong on 6/3/2015.
// */
//public class TestActor extends UntypedActor {
//    int count = 0;
//
//    @Override
//    public void onReceive(Object message) throws Exception {
//        if(message instanceof CipMessage) {
//            count++;
//            System.out.println("You receive message");
//            System.out.println("total we received "+ count);
//        }
//        else
//            unhandled(message);
//
//    }
//}
