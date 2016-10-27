package com.ebay.cip.framework.samples;


/**
 * Created by jagmehta on 5/21/2015.
 */
public class Module {

    public static void init() {
        FrameworkJobTypeEnum.get(FrameworkJobTypeEnum.LineSplitterJob.getName());
    }
}
