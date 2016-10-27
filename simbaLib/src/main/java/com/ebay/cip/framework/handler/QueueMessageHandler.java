package com.ebay.cip.framework.handler;

import com.ebay.cip.akka.configuration.ActorConfig;
import com.ebay.cip.akka.configuration.QueueConfig;
import com.ebay.cip.framework.messages.*;
import com.ebay.cip.framework.samples.configBean.FrameworkConfigBean;
import com.ebay.es.cbdataaccess.CouchBaseWrapperDAO;
import com.ebay.es.cipconfig.cipconfig.common.BucketEnum;
import com.ebay.kernel.calwrapper.CalEventHelper;
import com.ebay.kernel.logger.LogLevel;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.io.Input;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by kmanekar on 5/5/2015.
 */
public class QueueMessageHandler {

    private static final QueueMessageHandler instance = new QueueMessageHandler();

    private QueueMessageHandler(){}

    public static QueueMessageHandler getInstance() {
        return instance;
    }

    //TODO need to move it to utility method
    public String getQueueName(QueueMessage dequeueMessage){
        QueueConfig queueConfig = getQueueConfig(dequeueMessage);
        return  queueConfig.getQueueName();
    }

    /**
     * get Queue Config given a message.
     * TODO need to move this method to the util class
     * @param dequeueMessage
     * @return actorConfig
     */
    private QueueConfig getQueueConfig(QueueMessage dequeueMessage){

        String method = "QueueMessageHandler.getQueueConfig()";
        try {
            if(dequeueMessage != null) {
                String actorPath = getActorPath(dequeueMessage);

                if (actorPath != null) {
                    ActorConfig actorConfig = ActorConfig.getActorConfig(actorPath);

                    if (actorConfig != null) {
                        return actorConfig.getQueueConfig();
                    }
                }
            }
        } catch (Throwable th) {
            CalEventHelper.writeLog("CIP_FRAMEWORK", method, th.getMessage(), th, "ERROR");
        }
        return null;
    }

    /**
     * This function is use to create a queue
     * @param queueName
     */
    public void createQueue(String queueName) {
        String method = "QueueMessageHandler.createQueue()";
        try {
            if(!StringUtils.isEmpty(queueName)) {
                CouchBaseWrapperDAO wrapperDAO = CouchBaseWrapperDAO.getInstance(FrameworkConfigBean.getBean().getCouchBaseBucketName());
                wrapperDAO.createQueue(queueName);
            }
        } catch (Throwable th) {
            CalEventHelper.writeLog("CIP_FRAMEWORK", method, th.getMessage(), th, "ERROR");
        }
    }

    /**
     * This function will delete queue first and then create it again.
     * @param queueName
     */
    public void reCreateQueue(String queueName) {
        String method = "QueueMessageHandler.reCreateQueue()";
        try {
            if(!StringUtils.isEmpty(queueName)) {
                CouchBaseWrapperDAO wrapperDAO = CouchBaseWrapperDAO.getInstance(FrameworkConfigBean.getBean().getCouchBaseBucketName());
                wrapperDAO.deleteQueue(queueName);

                /** Workaround till WrapperDAO is fixed to delete head and tail synchroniously **/
                String qhead = queueName + "_Q:head";
                String qtail = queueName + "_Q:tail";
                try {
                    wrapperDAO.deleteSync(qhead);
                }catch (Exception e){ /* this means we are good */}
                try {
                    wrapperDAO.deleteSync(qtail);
                }catch (Exception e){ /* this means we are good */}
                /** End workaround **/

                wrapperDAO.createQueue(queueName);
            }
        } catch (Throwable th) {
            CalEventHelper.writeLog("CIP_FRAMEWORK", method, th.getMessage(), th, "ERROR");
        }
    }

    /**
     * This function will enqueue a encoded message to the queue
     * @param queueName the name of the queue
     * @param message the message that need to be queue
     * @return boolean true if it enqueue successfully  and false if it fail to enqueue
     */
    public boolean enqueue(String queueName, BaseMessage message) {
        String method = "QueueMessageHandler.enqueue()";
        try {
            if(!StringUtils.isEmpty(queueName)  && message != null) {
                CalEventHelper.writeLog("Framework", "Queue", "Queue message to:  "+queueName ,"0");
                CouchBaseWrapperDAO wrapperDAO = CouchBaseWrapperDAO.getInstance(FrameworkConfigBean.getBean().getCouchBaseBucketName());
                return wrapperDAO.enqueue(queueName, trancoder(message));
            } else {
                return false;
            }
        } catch (Throwable th) {
            CalEventHelper.writeLog("CIP_FRAMEWORK", method, th.getMessage(), th, "ERROR");
        }
        return false;
    }

    /**
     * This function will dequeue 1 message from the queue
     * @param queueName the name of the queue that will be dequeue
     * @return CipMessage if the message available, null if the queue is empty.
     */
    public CipMessage dequeue(String queueName) {
        try {
            if(!StringUtils.isEmpty(queueName)) {
                CouchBaseWrapperDAO wrapperDAO = CouchBaseWrapperDAO.getInstance(FrameworkConfigBean.getBean().getCouchBaseBucketName());
                CalEventHelper.writeLog("Framework", "Queue", "Dequeue message from: "+queueName, "0");
                byte [] bytes = (byte [])wrapperDAO.dequeue(queueName);
                return decoder(bytes);
            }
        } catch (Throwable th) {
            CalEventHelper.writeLog("CIP_FRAMEWORK", "QueueMessageHandler.dequeue()", th.getMessage(), th, "ERROR");
        }
        return null;
    }

    private byte[] trancoder(Object object){

        try {
            Kryo kryo = new Kryo();
            ByteArrayOutputStream bst = new ByteArrayOutputStream();
            Output out = new Output(bst);
            kryo.writeObject(out, object);
            out.close();
            return bst.toByteArray();
        }catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private CipMessage decoder(byte [] bytes){

        try {
            if(bytes != null) {
                Kryo kryo = new Kryo();
                ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
                Input input = new Input(stream);
                CipMessage message = kryo.readObject(input, CipMessage.class);
                input.close();
                return message;
            }
        }catch (Throwable exception) {
            throw new RuntimeException(exception);
        }
        return null;
    }
    /**
     * A helper class to get the actor path given a message
     * TODO need to move this to util class.
     * @param dequeueMessage message
     * @return path return the path of the actor that handle the job inside the message
     */

    private String getActorPath(QueueMessage dequeueMessage){
        return dequeueMessage.getActorPath();
    }
}

