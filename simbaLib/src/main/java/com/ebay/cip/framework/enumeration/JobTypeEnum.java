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
@JsonDeserialize (using = BaseEnumSerilizer.BaseEnumJsonDeSerializer.class)
@JsonSerialize (using = BaseEnumSerilizer.BaseEnumJsonSerializer.class)
public class JobTypeEnum extends BaseEnum {
    private static volatile Integer dynamicEnumId = 1000000;
    protected JobTypeEnum(int id, String name) {
        super(id, name);
    }

    public static final JobTypeEnum ORCHESTRATOR_JOB = new JobTypeEnum(0,"ORCHESTRATOR_JOB");
    public static final JobTypeEnum SplitterJob = new JobTypeEnum(1, "SplitterJob");
    public static final JobTypeEnum APIJob= new JobTypeEnum(2, "APIJob");
    public static final JobTypeEnum DUMMY_JOB = new JobTypeEnum(3,"DummyJob");

    public static JobTypeEnum get(int key) {
        JobTypeEnum action = (JobTypeEnum)getEnum(JobTypeEnum.class, key);
        return action;

    }

    /** Get the enumeration instance for a given value or null */
    public static JobTypeEnum get(String name) {
        return (JobTypeEnum)getEnum(JobTypeEnum.class, name);
    }

    /**
     * Add Enum dynamically. Useful for testing.
     */
    public static JobTypeEnum add(String name) {
        JobTypeEnum dynamicEnum;
        synchronized (dynamicEnumId){
            dynamicEnum = new JobTypeEnum(dynamicEnumId,name);
            dynamicEnumId++;
        }
        return dynamicEnum;
    }
}

