package com.ebay.cip.framework.util;

import com.ebay.cip.framework.JobContext;
import com.ebay.cip.framework.configuration.JobConfiguration;
import com.ebay.cip.framework.configuration.PipelineConfiguration;
import com.ebay.cip.framework.configuration.Pipelines;
import com.ebay.cip.framework.job.ICipJob;
import com.ebay.cip.framework.job.Job;
import com.ebay.cip.framework.job.JobMetadata;
import com.ebay.cip.framework.messages.BaseMessage;
import com.ebay.kernel.calwrapper.CalEventHelper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by hachong on 4/3/2015.
 */
public class CipClassUtil {

    static Cache<String, Class> classCache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .build();
    /**
     * Adding this method so that later on we can classCache name to Class object and also add default package behaviour.
     * @param className
     * @return
     * @throws ClassNotFoundException
     */
    public static Class getClass(String className) throws ClassNotFoundException {
        Class c = classCache.getIfPresent(className);
        if(c == null){
            c = Class.forName(className);
            classCache.put(className, c);
        }
        return c;
    }

    public static Job getJobObject(String jobName,String pipelineId,String feedId){
        try {
            Constructor ctr = CipClassUtil.getClass(jobName).getConstructor();
            Job job = (Job)ctr.newInstance();
            job.setJobMetadata(new JobMetadata(feedId));
            job.setPipelineId(pipelineId);
            return job;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] getSHA1(Object o) throws NoSuchAlgorithmException, IOException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        return md.digest(KryoUtil.serialize(o));
    }
}
