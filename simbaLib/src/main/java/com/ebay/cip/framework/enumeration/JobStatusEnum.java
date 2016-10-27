package com.ebay.cip.framework.enumeration;

import com.ebay.cip.framework.serilizer.json.BaseEnumSerilizer;
import com.ebay.cip.framework.serilizer.kryo.BaseEnumSerializer;
import com.ebay.kernel.BaseEnum;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Created by hachong on 4/3/2015.
 */
@com.esotericsoftware.kryo.DefaultSerializer(BaseEnumSerializer.BaseEnumKryoSerializer.class)
@JsonDeserialize(using = BaseEnumSerilizer.BaseEnumJsonDeSerializer.class)
@JsonSerialize(using = BaseEnumSerilizer.BaseEnumJsonSerializer.class)
public class JobStatusEnum extends BaseEnum {
    protected JobStatusEnum(int id, String name) {
        super(id, name);
    }

    //this status is set after job creation
    public static final JobStatusEnum CREATED = new JobStatusEnum(10,"CREATED");
    //this status is set before calling .execute() method
    public static final JobStatusEnum IN_PROGRESS = new JobStatusEnum(20, "IN_PROGRESS");
    //this status is set after calling.execute() method and the job is not set to "SUCCESS" or "FAILURE"
    public static final JobStatusEnum WAITING = new JobStatusEnum(30,"WAITING");
    //this status is set by business logic to indicate this job is complete as expected
    public static final JobStatusEnum SUCCESS= new JobStatusEnum(40, "SUCCESS");
    //this status is set by business logic to indicate this job is complete but not as expected
    public static final JobStatusEnum FAILURE= new JobStatusEnum(50, "FAILURE");
    //this status is set if a job have passed the deadline to complete
    public static final JobStatusEnum TIMEOUT = new JobStatusEnum(60, "TIMEOUT");

    public static JobStatusEnum get(int key) {
        JobStatusEnum action = (JobStatusEnum)getEnum(JobStatusEnum.class, key);
        return action;

    }

    /** Get the enumeration instance for a given value or null */
    public static JobStatusEnum get(String name) {
        return (JobStatusEnum)getEnum(JobStatusEnum.class, name);
    }

}
