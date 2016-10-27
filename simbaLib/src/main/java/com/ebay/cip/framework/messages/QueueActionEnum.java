package com.ebay.cip.framework.messages;

import com.ebay.kernel.BaseEnum;

/**
 * Created by kmanekar on 5/6/2015.
 */
public class QueueActionEnum extends BaseEnum {


    protected QueueActionEnum(int id, String name) {
        super(id, name);
    }

    public static final QueueActionEnum CREATE = new QueueActionEnum(10,"CREATE");
    public static final QueueActionEnum ENQUEUE = new QueueActionEnum(20,"ENQUEUE");
    public static final QueueActionEnum DEQUEUE = new QueueActionEnum(30, "DEQUEUE");

    public static QueueActionEnum get(int key) {
        QueueActionEnum action = (QueueActionEnum)getEnum(QueueActionEnum.class, key);
        return action;

    }

    /** Get the enumeration instance for a given value or null */
    public static QueueActionEnum get(String name) {
        return (QueueActionEnum)getEnum(QueueActionEnum.class, name);
    }


}