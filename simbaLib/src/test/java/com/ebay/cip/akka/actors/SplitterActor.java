//package com.ebay.cip.akka.actors;
//
//import akka.actor.ActorSelection;
//import akka.actor.ReceiveTimeout;
//import akka.actor.UntypedActor;
//import com.ebay.cip.framework.messages.CipMessage;
//import com.ebay.cip.framework.messages.QueueActionEnum;
//import com.ebay.cip.framework.messages.QueueMessage;
//import scala.concurrent.duration.Duration;
//
///**
// * Created by kmanekar on 4/6/2015.
// */
//public class SplitterActor extends UntypedActor{
//
//    public SplitterActor() {}
//
//    @Override
//    public void onReceive(Object message) throws Exception {
//        System.out.println("SplitterActor Actor :Got Message from "+getSender());
//    }
//}
