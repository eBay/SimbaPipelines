package com.ebay.cip.framework.dispatcher;

import com.ebay.cip.framework.job.Job;
import com.ebay.cip.framework.messages.BaseMessage;

/**
 * Created by hachong on 4/2/2015.
 */
public interface Dispatcher {
    /**
     * Framework called method to fork next job. It should ensure to set nextJob in currentJob if any.
     * @param jobToDispatch nextJob to fork
     * @param currentJob current job. If create of pipeline then null.
     */
    void dispatchNextJob(Job jobToDispatch, Job currentJob);


    /**
     * If message is ready and just needs to be dispatched without any extra logic, then use this method.
     * @param message
     */
    void dispatchMessage(BaseMessage message);

    /**
     * Specific message for pipelineCompletion.
     * @param job
     */
    void dispatchPipelineCompletionMessage(Job job);


    /** This method is only for forking purpose. Should not be used by platform directly.
     *
     */
    void fork(Job parent,Job forkedJob);
}
