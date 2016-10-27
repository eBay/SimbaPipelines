package com.ebay.cip.framework.messages;

import java.io.Serializable;

/**
 * Created by kmanekar on 5/6/2015.
 */
public class QueueMessage implements Serializable {
    private String actorPath;

    public QueueMessage(String actorPath) {
        this.actorPath = actorPath;
    }

    public String getActorPath() {
        return this.actorPath;
    }
}

