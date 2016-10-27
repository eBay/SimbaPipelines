package com.ebay.cip.framework.samples;

import com.ebay.cip.framework.enumeration.JobTypeEnum;

/**
 * Created by jagmehta on 5/11/2015.
 */
public class FrameworkJobTypeEnum extends JobTypeEnum {


    public static final JobTypeEnum ComputeMaxNumberJob = JobTypeEnum.add( "ComputeMaxNumberJob");
    public static final JobTypeEnum LineSplitterJob = JobTypeEnum.add( "LineSplitterJob");
    public static final JobTypeEnum DecodeJob = JobTypeEnum.add("DecodeJob");
    public static final JobTypeEnum ResultCompareJob = JobTypeEnum.add( "ResultCompareJob");
    public static final JobTypeEnum DirectComputeMaxNumberJob = JobTypeEnum.add( "DirectComputeMaxNumberJob");
    public static final JobTypeEnum UnitTestParentJob = JobTypeEnum.add( "UnitTestParentJob");

    protected FrameworkJobTypeEnum(int id, String name) {
        super(id, name);
    }
}
