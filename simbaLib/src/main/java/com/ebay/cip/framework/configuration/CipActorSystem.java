package com.ebay.cip.framework.configuration;

import akka.actor.ActorContext;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import com.ebay.cip.akka.actors.ParentActor;

/**
 * Created by hachong on 4/2/2015.
 */
public class CipActorSystem{
    private ActorSystem actorSystem;
    private ActorRef queueActor;
    private ActorRef jobMonitorActor;
    private ActorRef feedFailActor;
    private ParentActor parentActor;

    private static CipActorSystem _instance = null;
    private CipActorSystem() { }

    public static CipActorSystem getInstance() {
        return _instance;
    }

    /**
     * This method should be called during bootstrap/jvm startup.
     * It is responsible for creating full actor tree.
     * @param context
     */
    synchronized public static void  init(ActorContext context){
        if(_instance == null) {
            _instance = new CipActorSystem();
            _instance.actorSystem = context.system();
        }
    }

    public ActorSystem getActorSystem() {
        return actorSystem;
    }

    public ActorRef getJobMonitorActor() { return jobMonitorActor;}
    public void setJobMonitorActor(ActorRef jobMonitorActor){
        this.jobMonitorActor = jobMonitorActor;
    }

    public ActorRef getQueueActor() { return queueActor;}
    public void setQueueActor(ActorRef queueActor){
        this.queueActor = queueActor;
    }

    public ParentActor getParentActorInstance() {
        return parentActor;
    }
    
    public void setParentActorInstance(ParentActor parentActor) {
        this.parentActor = parentActor;
    }


    public ActorSelection getActor(String actorName){
        return actorSystem.actorSelection("user/parentActor/"+actorName);
    }

    public ActorRef getFeedFailActor() {
        return feedFailActor;
    }

    public void setFeedFailActor(ActorRef feedFailActor) {
        this.feedFailActor = feedFailActor;
    }
}