package com.ebay.cip.framework.messages;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import com.ebay.cip.framework.job.ICipJob;

/**
 * Created by hachong on 6/2/2015.
 */
public class GateKeeperMessage {
    private BaseMessage originMessage;
    private String key;
    private ActorRef destination;
    private String queueName;

    public GateKeeperMessage(String key,BaseMessage originMessage, ActorRef destination){
        this.key = key;
        this.originMessage = originMessage;
        this.destination = destination;
    }
    public String getKey(){
        return this.key;
    }

    public BaseMessage getOriginMessage(){
        return this.originMessage;
    }

    public ActorRef getDestination(){
        return destination;
    }

    public void setQueueName(String queueName){
        this.queueName = queueName;
    }

    public String getQueueName(){
        return this.queueName;
    }


}
