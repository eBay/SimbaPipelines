package com.ebay.cip.framework.samples.job;

import com.ebay.cip.framework.enumeration.JobTypeEnum;
import com.ebay.cip.framework.job.Job;
import com.ebay.cip.framework.samples.FrameworkJobTypeEnum;

/**
 * Created by jagmehta on 5/11/2015.
 */
public class LineSplitterJob extends Job {

    public static String FILE_PATH = "FILE_PATH";

//    List<String> childResults = new ArrayList<>();
//    boolean failed = false;

    @Override
    public JobTypeEnum getJobType() {
        return JobTypeEnum.get(FrameworkJobTypeEnum.LineSplitterJob.getId());
    }
/*
    public List<String> getChildResults() {
        return childResults;
    }

    public void setChildResults(List<String> childResults) {
        this.childResults = childResults;
    }
    public void addChildResult(String childResult){
        this.childResults.add(childResult);
    }
    public void setFailed(boolean isFailed){
        failed = isFailed;
    }

    public boolean isFailed(){
        return failed;
    }
*/
}
