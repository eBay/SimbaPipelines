package com.ebay.cip.framework.job;

import com.ebay.cip.framework.enumeration.JobStatusEnum;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by hachong on 9/21/2015.
 */
public class ForkMetadata {
    private Integer count;
    private Map<String,JobStatusEnum> childrenMap;

    public ForkMetadata(){
        count =0;
        childrenMap = new ConcurrentHashMap<String,JobStatusEnum>();
    }

    public Integer getCount(){
        return this.count;
    }

    public void increaseCount(){
        count++;
    }

    public Map<String,JobStatusEnum> getChildrenMap(){
        return this.childrenMap;
    }

    public void changeChildrenStatus(String jobId,JobStatusEnum jobStatus){
        childrenMap.put(jobId,jobStatus);
    }

    public void addChildren(String jobId){
        childrenMap.put(jobId,JobStatusEnum.CREATED);
    }

    public static ForkMetadata buildForkMetaData(String commaSeperatedString){
        ForkMetadata meta = new ForkMetadata();
        Map<String,String> pipelineToFirstJobMap = new HashMap<>();
        if(commaSeperatedString != null){
            Map<String,JobStatusEnum> map = meta.getChildrenMap();
            String[] outer = commaSeperatedString.split(",");
            JobStatusEnum st;
            String jobId;
            String pipelineId;
            for(String inner: outer){
                String[] values = inner.split(":");
                if(values.length == 2) {
                    st = JobStatusEnum.get(values[1]);
                    jobId = values[0];
                    map.put(jobId, st);
                    if (JobStatusEnum.SUCCESS == st || JobStatusEnum.FAILURE == st || JobStatusEnum.TIMEOUT == st) {
                        meta.increaseCount();
                    }
                }else if(values.length == 3) {
                    pipelineId = values[0];
                    jobId = values[1];
                    st = JobStatusEnum.get(values[2]);

                    String firstJobId = pipelineToFirstJobMap.get(pipelineId);
                    if(firstJobId == null){
                        pipelineToFirstJobMap.put(pipelineId, jobId);
                        firstJobId = jobId;
                    }
                    map.put(firstJobId,st);
                    if (JobStatusEnum.SUCCESS == st || JobStatusEnum.FAILURE == st || JobStatusEnum.TIMEOUT == st) {
                        meta.increaseCount();
                    }
                }
            }
        }
        return meta;
    }

}
