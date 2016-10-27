//package com.ebay.cip.framework.dispatcher;
//
//import akka.actor.ActorRef;
//import akka.actor.ActorSelection;
//import com.ebay.cip.framework.configuration.CipActorSystem;
//import com.ebay.cip.framework.enumeration.JobTypeEnum;
//import com.ebay.cip.framework.handler.JobExecutionHandler;
//import com.ebay.cip.framework.handler.PipelineCompleteMessageHandler;
//import com.ebay.cip.framework.job.Job;
//import com.ebay.cip.framework.messages.BaseMessage;
//import com.ebay.cip.framework.samples.configBean.FrameworkConfigBean;
//import com.ebay.cip.framework.util.InstanceFactory;
//
///**
// * Created by jagmehta on 3/17/2015.
// * Use this dispatcher if you want to run all your jobs in single thread synchronously.
// * Recommended for unit tests, tools and production debugging.
// */
//public class SingleThreadedSynchronousDispatcher extends BaseDispatcher implements Dispatcher {
//
//    @Override
//    public void sendCipMessage(BaseMessage message) {
//        JobExecutionHandler handler = new JobExecutionHandler();
//        try {
//            handler.onReceive(message);
//        }catch(Exception e){
//            throw new RuntimeException(e);
//        }
//
//    }
//
//    /**
//     * TODO This method should be replaced with more elegent one or even with dispatchMessage/dispatchCompleteMessage
//     * @param completeMessage
//     */
//    @Override
//    public void sendPipelineCompletionMessage(BaseMessage completeMessage) {
///*
//        ActorSelection orch = actorSystem.actorSelection("/user/framework/HttpParentActor/orchestratorRouter");
//        orch.tell(completeMessage, ActorRef.noSender());
//*/
//        Job job = completeMessage.getJob();
//        Job parentJob = null;
//        parentJob = InstanceFactory.getObjectInstance(job.getJobMetaData().getParentJobId(), Job.class, FrameworkConfigBean.getBean().isDbBacked());
//        ActorSelection actor;
//
//        if(parentJob.getJobType() == JobTypeEnum.ORCHESTRATOR_JOB){
//            actor = CipActorSystem.getInstance().getActorSystem().actorSelection("/user/framework/HttpParentActor/orchestratorRouter");
//            actor.tell(completeMessage, ActorRef.noSender());
//        }
//        else {
//           PipelineCompleteMessageHandler handler = new PipelineCompleteMessageHandler();
//            try {
//                handler.onReceive(completeMessage);
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
//        }
//
//    }
//
//}package com.ebay.cip.framework.dispatcher;
//
//import akka.actor.ActorRef;
//import akka.actor.ActorSelection;
//import com.ebay.cip.framework.configuration.CipActorSystem;
//import com.ebay.cip.framework.enumeration.JobTypeEnum;
//import com.ebay.cip.framework.handler.JobExecutionHandler;
//import com.ebay.cip.framework.handler.PipelineCompleteMessageHandler;
//import com.ebay.cip.framework.job.Job;
//import com.ebay.cip.framework.messages.BaseMessage;
//import com.ebay.cip.framework.samples.configBean.FrameworkConfigBean;
//import com.ebay.cip.framework.util.InstanceFactory;
//
///**
// * Created by jagmehta on 3/17/2015.
// * Use this dispatcher if you want to run all your jobs in single thread synchronously.
// * Recommended for unit tests, tools and production debugging.
// */
//public class SingleThreadedSynchronousDispatcher extends BaseDispatcher implements Dispatcher {
//
//    @Override
//    public void sendCipMessage(BaseMessage message) {
//        JobExecutionHandler handler = new JobExecutionHandler();
//        try {
//            handler.onReceive(message);
//        }catch(Exception e){
//            throw new RuntimeException(e);
//        }
//
//    }
//
//    /**
//     * TODO This method should be replaced with more elegent one or even with dispatchMessage/dispatchCompleteMessage
//     * @param completeMessage
//     */
//    @Override
//    public void sendPipelineCompletionMessage(BaseMessage completeMessage) {
///*
//        ActorSelection orch = actorSystem.actorSelection("/user/framework/HttpParentActor/orchestratorRouter");
//        orch.tell(completeMessage, ActorRef.noSender());
//*/
//        Job job = completeMessage.getJob();
//        Job parentJob = null;
//        parentJob = InstanceFactory.getObjectInstance(job.getJobMetaData().getParentJobId(), Job.class, FrameworkConfigBean.getBean().isDbBacked());
//        ActorSelection actor;
//
//        if(parentJob.getJobType() == JobTypeEnum.ORCHESTRATOR_JOB){
//            actor = CipActorSystem.getInstance().getActorSystem().actorSelection("/user/framework/HttpParentActor/orchestratorRouter");
//            actor.tell(completeMessage, ActorRef.noSender());
//        }
//        else {
//           PipelineCompleteMessageHandler handler = new PipelineCompleteMessageHandler();
//            try {
//                handler.onReceive(completeMessage);
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
//        }
//
//    }
//
//}