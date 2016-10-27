package com.ebay.cip.framework.samples.job;

import com.ebay.cip.framework.enumeration.JobTypeEnum;
import com.ebay.cip.framework.job.Job;
import com.ebay.cip.framework.samples.FrameworkJobTypeEnum;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jagmehta on 5/11/2015.
 */
public class ComputeMaxNumberJob extends Job{

    public static final String DATA_TO_PROCESS = "DATA_TO_PROCESS";
    public static final String RESULT = "RESULT";
    public static final String CHILD_RESULT = "CHILD_RESULT";

    List<String> numbers = new ArrayList<>();
    //List<String> childResults = new ArrayList<>();
    Long max;
    //boolean failed = false;

    @Override
    public JobTypeEnum getJobType() {
        return JobTypeEnum.get(FrameworkJobTypeEnum.ComputeMaxNumberJob.getId());
    }

    public List<String> getNumbers() {
        return numbers;
    }

    public void setNumbers(List<String> numbers) {
        this.numbers = numbers;
    }

    public Long getMax() {
        return max;
    }

    public void setMax(Long max) {
        this.max = max;
    }

   /* public List<String> getChildResults() {
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
