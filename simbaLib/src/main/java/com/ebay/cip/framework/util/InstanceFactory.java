package com.ebay.cip.framework.util;

import com.couchbase.client.protocol.views.ComplexKey;
import com.couchbase.client.protocol.views.Query;
import com.couchbase.client.protocol.views.ViewResponse;
import com.couchbase.client.protocol.views.ViewRow;
import com.ebay.cip.framework.BaseContext;
import com.ebay.cip.framework.JobContext;
import com.ebay.cip.framework.exception.InstanceProcessingException;
import com.ebay.cip.framework.feed.Feed;
import com.ebay.cip.framework.feed.FeedStatus;
import com.ebay.cip.framework.job.ForkMetadata;
import com.ebay.cip.framework.job.ICipJob;
import com.ebay.cip.framework.job.Job;
import com.ebay.cip.framework.messages.CipPipelineCompleteMessage;
import com.ebay.cip.framework.samples.configBean.FrameworkConfigBean;
import com.ebay.es.cbdataaccess.CouchBaseWrapperDAO;
import com.ebay.es.cbdataaccess.bean.CouchBaseDocument;
import com.ebay.es.cbdataaccess.bean.QueryConfig;
import com.ebay.es.cbdataaccess.exception.LockNotAcquiredException;
import com.ebay.es.cbdataaccess.exception.ProcessingException;
import com.ebay.kernel.calwrapper.CalEventHelper;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.Map;
import java.util.concurrent.*;

/**
 * The purpose of this class is to provide job, message and jobContext instance for platform.
 * There are two different type to store and get the instance (DB and non DB)
 * the current implementation for DB backed instance it will store all objects in couch base, and for non DB backed it will store all objects in memory using weak hashmap
 *
 * Created by hachong on 6/9/2015.
 */
public class InstanceFactory {


    private final static String PLATFORM = "Platform::";
    private final static String JOB = "job::";
    private final static String CONTEXT = "context::";
    private final static String PIPELINE_COMPLETE_MESSAGE = "message-complete::";
    private final static String FEED = "feed::";
    private final static String COUNTER = "counter::";
    private final static String DATA = "data::";

    private static FrameworkConfigBean frameworkConfigBean = FrameworkConfigBean.getBean();
    protected static CouchBaseWrapperDAO couchBaseDAO;
    private Map<String,Object> map;
    private boolean isDbBacked;
    private String firstJobId;

    static{
            couchBaseDAO = CouchBaseWrapperDAO.getInstance(frameworkConfigBean.getCouchBaseBucketName());
    }

    public InstanceFactory(boolean isDbBacked) {
        this.isDbBacked = isDbBacked;
        if (!isDbBacked)
            map = new ConcurrentHashMap<>();
        if(couchBaseDAO == null){
            couchBaseDAO = CouchBaseWrapperDAO.getInstance(frameworkConfigBean.getCouchBaseBucketName());
        }
    }

    public void setFirstJobId(String firstJobId){
        this.firstJobId = firstJobId;
    }
    /**
     * This method will get the object instance stored via db/ memory
     * @param key of the object
     * @param type of the object
     * @param <T> object type
     * @return
     */
    public <T> T getObjectInstance(String key, Class type) throws InstanceProcessingException {
        if (isDbBacked) {
            return getObjectInstanceFromDB(key, type);
        } else {
            return getObjectInstanceInMemory(key, type);
        }
    }
    /**
     * This method will always use Kryo to serialize given object. Key used will be decided by {@link #buildFinalKeyFromObject(String, Object)}
     * It also uses Couchbase locks. If you dont want to use locks then use {@link #saveObjectInstanceInDB(String, Object,boolean)}
     * @param key
     * @param object
     * @param <T>
     * @param async  Should this save be asynchronious or not?
     * @throws ProcessingException
     * @throws LockNotAcquiredException
     * @throws ExecutionException
     * @throws InterruptedException
     *
     */

    public <T> void saveObjectInstance(String key, T object, boolean async) throws InstanceProcessingException {
        if(object == null){
            return;
        }
        if(isDbBacked)
            try {
                saveObjectInstanceInDB(key, object, async);
            } catch (InstanceProcessingException e) {
                StringBuffer message = new StringBuffer();
                message.append("Unable to save object ").append(object.getClass().getName()).append(" with key: ").append(key);
                CalEventHelper.writeException("DBError",e,true,message.toString());
                throw e;
            }
        else
            saveObjectInstanceInMemory(key,object);
    }

    /**
     * Calls {@link #saveObjectInstance(String, Object, boolean)} with async as false.
     * @param key
     * @param object
     * @param <T>
     * @throws ProcessingException
     * @throws LockNotAcquiredException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public <T> void saveObjectInstance(String key, T object) throws InstanceProcessingException {
        saveObjectInstance(key, object, false);
    }

    /**
     * Method to append data to existing key. Does not support in-memory append yet.
     * @param key  against which data needs to be saved. This will be wrapped with Platform specific string.
     * @param typeOfData What type of data you are saving. Needs to build (wrap) key supplied above. For example ForkMetaData.class
     * @param data actual data to be appended.
     * @throws InstanceProcessingException
     */
    public Boolean appendSync(String key, Class typeOfData, Object data,boolean create) {
        if(data == null) {
            return Boolean.FALSE;
        }
        String finalKey = buildFinalKeyFromClass(key, typeOfData);
        Boolean result = Boolean.FALSE;
        if(isDbBacked) {
            try {
                if(create) {
                    result = couchBaseDAO.updateSyncRawData(finalKey, frameworkConfigBean.getDocumentPersistPeriod(typeOfData), data);
                } else {
                    result = couchBaseDAO.appendSyncRawData(finalKey, data);
                }

            }catch(Exception e) {
                CalEventHelper.writeException("InstanceFactory", e, true, new String("Error appending/creating "+key));
                result = Boolean.FALSE;
            }
            //couchBaseDAO.appendSyncRawData()        }
        }else { // in-memory
            // not supported yet.
        }
        return result;
    }
    public Future<Boolean> appendAsync(String key, Class typeOfData, Object data, boolean create) {
        Future<Boolean> result = new Future<Boolean>() {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return false;
            }

            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public boolean isDone() {
                return true;
            }

            @Override
            public Boolean get() throws InterruptedException, ExecutionException {
                return Boolean.FALSE;
            }

            @Override
            public Boolean get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                return get();
            }
        };

        if(data == null) {
            return result;
        }
        String finalKey = buildFinalKeyFromClass(key, typeOfData);
        if(isDbBacked) {
            try {
                if(create) {
                    result = couchBaseDAO.updateAsyncRawData(finalKey, frameworkConfigBean.getDocumentPersistPeriod(typeOfData), data);
                }else {
                    result = couchBaseDAO.appendAsyncRawData(finalKey, data);
                }
            }catch(Exception e) {
                CalEventHelper.writeException("InstanceFactory", e, true, new String("Error appending/creating "+key));
            }
            //couchBaseDAO.appendSyncRawData()        }
        }else { // in-memory
            // not supported yet.
        }
        return result;
    }

    public <T> Object getRawData(String key, Class<T> typeOfData) throws InstanceProcessingException {
        String finalKey = buildFinalKeyFromClass(key, typeOfData);
        if (isDbBacked) {
            try {
                return couchBaseDAO.get(finalKey);
            } catch (Exception e) {
                throw new InstanceProcessingException(e);
            }
        }else {
            // not supported yet.
            return null;
        }
    }

    /**
     * This method save object Instance without using Kryo serializer
     * @param key the Key of the object
     * @param object The object that want to save
     * @param <T> type of the object
     */
    public <T> void saveObjectInstanceNonKyro(String key,T object) throws InstanceProcessingException{
        String finalKey = buildFinalKeyFromObject(key, object);
        CalEventHelper.writeLog("Framework", "InstanceFactory", "save Object non kyro. Key = "+finalKey, "0");
        try {
            couchBaseDAO.updateAsync(finalKey, frameworkConfigBean.getDocumentPersistPeriod(object), (CouchBaseDocument) object);
        } catch (Exception  e) {
            throw new InstanceProcessingException(e);
        }
    }

    /**
     * This method will get object instance that save without using Kyro serializer
     * @param key The key of the object
     * @param type The class name of the object
     * @param <T>  The type of object
     * @return The object that casted to type  .
     * @throws InstanceProcessingException
     */
    public <T> T getObjectInstaceNonKyro(String key,Class type) throws InstanceProcessingException{
        String finalKey = buildFinalKeyFromClass(key, type);
        try{
            return (T) couchBaseDAO.get(finalKey,type);
        } catch (Exception e) {
            throw new InstanceProcessingException(e);
        }
    }


    private <T> T getObjectInstanceFromDB(String key, Class type) throws InstanceProcessingException {
        byte[] bytes;
        String finalKey = buildFinalKeyFromClass(key, type);
        try {
            bytes = (byte[])couchBaseDAO.get(finalKey);
        } catch (Exception e) {
            CalEventHelper.writeException("InstanceFactory", e, true, finalKey+" Unable to get object "+e.getMessage());
            e.printStackTrace();
            throw new InstanceProcessingException(e);
        }

        if(bytes != null) {
            InstanceDocument doc = KryoUtil.deserialize(bytes, InstanceDocument.class);
            return (T) doc.getClassData();
        }else {
            CalEventHelper.writeLog("Framework", "InstanceFactory", finalKey+ "No object found ", "WARNING");
            return null;
        }
    }

    /**
     * get object from inMemory hashMap
     * @param key The key of the object
     * @param type The type of the object
     * @param <T> The type of the object
     * @return
     */
    private <T> T getObjectInstanceInMemory(String key, Class type) {
        String finalKey = buildFinalKeyFromClass(key, type);
        Object object = map.get(finalKey);

        if(object != null)
            return (T) object;
        else{
            String message = "key: "+key+" is not exist in map";
            System.out.println(message);
            CalEventHelper.writeLog("Framework", "InstanceFactory.getInMemory",message,"0");
            return null;
        }

    }

    /**
     * Save object to inMemory hashMap
     * @param key The key of the object
     * @param object The object that will be save
     * @param <T> The type of the object
     */
    private  <T> void saveObjectInstanceInMemory(String key, T object){
        String finalKey = buildFinalKeyFromObject(key,object);
        map.put(finalKey,object);
    }


    private <T> void saveObjectInstanceInDB(String key,T object, boolean async) throws InstanceProcessingException {
        if (object == null) {
            return;
        }
        InstanceDocument idoc = new InstanceDocument(object.getClass().getName(), object);
        byte[] doc = KryoUtil.serialize(idoc);

        String finalKey = buildFinalKeyFromObject(key, object);
        try {
            //create instanceDocument for CouchBase
            finalKey = buildFinalKeyFromObject(key,object);
            idoc = new InstanceDocument(object.getClass().getName(), object);
            doc = KryoUtil.serialize(idoc);
            if(!async) {
                couchBaseDAO.updateSyncRawData(finalKey, frameworkConfigBean.getDocumentPersistPeriod(object), doc);
            }else {
                couchBaseDAO.updateAsyncRawData(finalKey, frameworkConfigBean.getDocumentPersistPeriod(object), doc);
            }
        }catch(InterruptedException | ExecutionException| RuntimeException |ProcessingException e){
            e.printStackTrace();
            CalEventHelper.writeException("Framework",e,true);
            int counter = 0;
            Boolean success = false;
            while(counter < FrameworkConfigBean.getBean().getDbMaxRetry() && (success==false)) {
                counter++;
                try{
                    success = couchBaseDAO.updateSyncRawData(finalKey, frameworkConfigBean.getDocumentPersistPeriod(object), doc);
                }
                catch(Exception e1){
                    StringBuffer message = new StringBuffer();
                    message.append("retry to save ").append(key).append(" for ").append(counter).append(" times. ");
                    CalEventHelper.writeLog("Framework","InstanceFactory.SaveObjectNoLock",message.toString(),"0");
                }
            }

            //save in memory if the object is the first job of the pipeline.
            if(object instanceof Job){
                if(((Job) object).getJobId() == firstJobId){
                    switchToInMemory(key, object);
                }
            }

        } catch (Exception e) {
            CalEventHelper.writeException("InstanceFactory", e, true, "Failed to save data for key " + finalKey + " -- " + e.getMessage());
            e.printStackTrace();
            CalEventHelper.writeException("Framework",e,true);
            throw new InstanceProcessingException(e);
        }
    }

    /**
     * Construct final key that use to store object
     * @param originalKey the key send by application
     * @param classType the type of class
     * @return the key that use to store object
     */
    private String buildFinalKeyFromClass(String originalKey, Class classType) {

        StringBuilder finalKey = new StringBuilder(PLATFORM);
        if (originalKey == null) {
            return null;
        }else if (classType == null) {
            finalKey.append(DATA);
        } else {
            if (ICipJob.class.isAssignableFrom(classType)) {
                finalKey.append(JOB);
            } else if (JobContext.class.isAssignableFrom(classType)) {
                finalKey.append(CONTEXT);
            } else if (CipPipelineCompleteMessage.class.isAssignableFrom(classType)) {
                finalKey.append(PIPELINE_COMPLETE_MESSAGE);
            } else if (Feed.class.isAssignableFrom(classType)){
                finalKey.append(FEED);
            } else if(ForkMetadata.class.isAssignableFrom(classType)){
                finalKey.append(COUNTER);
            }
            else {
                finalKey.append(DATA);
            }
        }
        finalKey.append(originalKey);
        return finalKey.toString();
    }

    /**
     * This method understands various objects and creates keys accordingly.
     * Key format is  <pre> "platform::<objectType>::originalKey  </pre>
     * Example
     * <li>platform::job::123456</li>
     * <li>platform::jobcontext::123456</li>
     * <li>platform::message::123456</li>
     *
     * @param originalKey  objectKey. For example, jobId, messageId, pipelineKey
     * @param object
     * @return
     */
    private String buildFinalKeyFromObject(String originalKey, Object object){

        if(object != null && originalKey != null) {
            StringBuilder finalKey = new StringBuilder(PLATFORM);

            if (object instanceof ICipJob) {
                finalKey.append(JOB);
            } else if (object instanceof BaseContext) {
                finalKey.append(CONTEXT);
            } else if (object instanceof CipPipelineCompleteMessage) {
                finalKey.append(PIPELINE_COMPLETE_MESSAGE);
            } else if (object instanceof Feed) {
                finalKey.append(FEED);
            } else if(object instanceof ForkMetadata){
                finalKey.append(COUNTER);
            }
            else {
                finalKey.append(DATA);
            }
            finalKey.append(originalKey);
            return finalKey.toString();
        }
        return null;

    }


    /**
     * This method only use by simba tools in to search feed in  DB
     * @param key
     * @param object
     * @return
     */
//    private <T> void saveObjectInstanceNoLock(String key, T object) throws InstanceProcessingException {
//
//        InstanceDocument idoc;
//        Boolean ret = null;
//        idoc = new InstanceDocument(object.getClass().getName(), object);
//        byte[] doc = KryoUtil.serialize(idoc);
//        String finalKey = buildFinalKeyFromObject(key,object);
//        try {
//            ret =couchBaseDAO.updateSyncRawData(finalKey, frameworkConfigBean.getDocumentPersistPeriod(object), doc);
//            if(ret==false){
//                throw new Exception("Failed to save data for key "+ finalKey);
//            }
//        } catch (Exception  e) {
//            CalEventHelper.writeException("InstanceFactory", e, true, e.getMessage());
//            e.printStackTrace();
//            e.printStackTrace();
//        }
//    }


    /**
     * Helper class to do all configuration that needed for inMemory processing(ex: change the flag and create concurrent hashmap).
     *
     * @param key
     * @param object
     * @param <T>
     */
    private<T> void switchToInMemory(String key, T object){
        StringBuffer message = new StringBuffer().append("Key: ").append(key).append(". Object class name: ").append(object.getClass().getName());
        CalEventHelper.writeLog("Framework","Swith saving the object from Db to in Memory ",message.toString(),"0");
        isDbBacked = false;
        map = new ConcurrentHashMap<>();
        saveObjectInstanceInMemory(key, object);
    }

    public boolean isDbBacked(){
        return this.isDbBacked;
    }

    /**
     * This method only use by simba tools in to search feed in  DB
     * @param key
     * @param type
     * @return
     */
    public JSONObject getFeedJSONObject(String key, Class type) {
        try {
            String data = (String) couchBaseDAO.get(buildFinalKeyFromClass(key, type));
            if(data != null)
                return new JSONObject(data);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    public String getAllFeeds(FeedStatus status,int hour) {
        QueryConfig queryConfig = new QueryConfig();
        queryConfig.setDesignDocument("feed");

        switch (status) {
            case CREATED:
                queryConfig.setViewName("feed_created");
                break;
            case PROCESSING:
                queryConfig.setViewName("feed_processing");
                break;
            case COMPLETE:
                queryConfig.setViewName("feed_complete");
                break;
            default:
                queryConfig.setViewName("feed_allstatus");
                break;
        }

        DateTime dt = new DateTime(DateTimeZone.UTC);
        //create endKey
        ComplexKey endKey = ComplexKey.of(dt.getYear(),dt.getMonthOfYear(), dt.getDayOfMonth(),dt.getHourOfDay(),dt.getMinuteOfHour());

        if(hour == 0){

        }
        dt = dt.minusHours(hour);
        //create startKey
        ComplexKey startKey = ComplexKey.of(dt.getYear(), dt.getMonthOfYear(),dt.getDayOfMonth(),dt.getHourOfDay(),dt.getMinuteOfHour());


        Query query = new Query().setRange(startKey, endKey);
        try {
            ViewResponse result = couchBaseDAO.query(queryConfig, query);
            JSONArray jsonArray = new JSONArray();
            if(result != null){
                for(ViewRow row: result){
                    jsonArray.put(row.getValue());
                }
            }
            return jsonArray.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "Unable to query";
        }
    }

    public String getFeedDataByCorelationId(String corelationId) {
        QueryConfig queryConfig = new QueryConfig();
        queryConfig.setDesignDocument("feed");

        queryConfig.setViewName("feedData");

        Query query = new Query();//.setKey(corelationId);

        //Couchbase java client has defect where it treats numeric differrently and hence we are not getting data back from view.
        //Workaround to it is to wrap all numeric string with ""
        corelationId = StringUtils.isNumeric(corelationId)?"\""+corelationId+"\"":corelationId;

        query.setKey(corelationId);
        try {
            ViewResponse result = couchBaseDAO.query(queryConfig, query);
            JSONArray jsonArray = new JSONArray();
            if(result != null){
                for(ViewRow row: result){
                    jsonArray.put(row.getValue());
                }
            }
            return jsonArray.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "Unable to query";
        }
    }
}
