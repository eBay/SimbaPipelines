package com.ebay.cip.framework.samples.configBean;

import com.ebay.aero.kernel.init.InitializationException;
import com.ebay.cip.akka.configuration.ActorConfig;
import com.ebay.cip.framework.util.FrameworkConfiguration;
import com.ebay.kernel.bean.configuration.BaseConfigBean;
import com.ebay.kernel.bean.configuration.BeanConfigCategoryInfo;
import com.ebay.kernel.bean.configuration.BeanPropertyInfo;
import com.ebay.kernel.logger.LogLevel;
import com.ebay.kernel.logger.Logger;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hachong on 7/8/2015.
 */
public class FrameworkConfigBean extends BaseConfigBean {

    Logger logger = Logger.getInstance(FrameworkConfigBean.class);

    private static FrameworkConfigBean s_instance = null;
    private static final String CONFIG_CATEGORY_ID = "com.ebay.cip.framework.samples.configBean.FrameworkConfigBean";
    private static final String ALIAS = "SimbaPlatformConfigBean";
    private static final String GROUP = "com.ebay.cip.framework";
    private static final String DESCRIPTION = "This config bean is for framework configuration";

    private static Map<String,Integer> customDocPersistTime;

    /// Property definitions.

    private static String actorConfigSource;
    private static String couchBaseBucketName;
    private static int couchBaseDocLockPeriod;
    private static int couchBaseDefaultDocPersistPeriod;
    private static String couchBaseCustomDocPersistPeriodJson;
    private static int throttlingRateLimitAllowed;
    private static int throttlingRateLimitPeriod;
    private static boolean dbBacked;
    private static int delayPeriod;
    private static int retryCount;
    private static int dbMaxRetry;
    private static int timeoutPeriod;


    private static boolean createRaptorConfigBackup = true;

    ////////////////////////////////

    protected void loadConfiguration (FrameworkConfiguration conf){
        try {
            actorConfigSource = ActorConfig.getActorConfigSource();
            couchBaseBucketName = conf.getProperty("bucketName");
            couchBaseDocLockPeriod = Integer.parseInt(conf.getProperty("docLockPeriod"));
            couchBaseDefaultDocPersistPeriod = Integer.parseInt(conf.getProperty("defaultDocPersistPeriod"));
            couchBaseCustomDocPersistPeriodJson = conf.getJsonString("customDocPersistPeriod");
            throttlingRateLimitAllowed = Integer.parseInt(conf.getProperty("rateLimitAllowed"));
            throttlingRateLimitPeriod = Integer.parseInt(conf.getProperty("rateLimitPeriod"));
            dbBacked = Boolean.valueOf(conf.getProperty("dbBacked"));
            delayPeriod = Integer.parseInt(conf.getProperty("delayPeriod"));
            retryCount = Integer.parseInt(conf.getProperty("retryCount"));
            dbMaxRetry = Integer.parseInt(conf.getProperty("dbMaxRetry"));
            timeoutPeriod = Integer.parseInt(conf.getProperty("timeoutPeriod"));
            createRaptorConfigBackup = Boolean.parseBoolean(conf.getProperty("createRaptorConfigBackup"));

        } catch (Exception e) {
            couchBaseBucketName = "cip_framework";
            couchBaseDocLockPeriod = 10;
            couchBaseDefaultDocPersistPeriod = 604800;
            couchBaseCustomDocPersistPeriodJson = null;
            throttlingRateLimitAllowed= 10000;
            throttlingRateLimitPeriod = 1;
            dbBacked = true;
            delayPeriod = 1;
            retryCount = 3;
            dbMaxRetry = 3;
            timeoutPeriod = 10;
            e.printStackTrace();
        }
        customDocPersistTime = pasreCustomDocPersistPeriod(couchBaseCustomDocPersistPeriodJson);
        if(customDocPersistTime == null ){ customDocPersistTime = new HashMap<>(); }
    }

    private FrameworkConfigBean(BeanConfigCategoryInfo category, FrameworkConfiguration conf) {
        loadConfiguration(conf);
        init(category, true);
        addListners();
    }

    public static void initialize(FrameworkConfiguration conf) {
        synchronized (FrameworkConfigBean.class) {
            if (s_instance == null) {
                try {
                    BeanConfigCategoryInfo category =
                            BeanConfigCategoryInfo.createBeanConfigCategoryInfo
                                    (CONFIG_CATEGORY_ID,
                                            ALIAS,
                                            GROUP,
                                            true,
                                            true,
                                            null,
                                            DESCRIPTION);
                    s_instance = new FrameworkConfigBean(category, conf);
                }
                catch (Exception e) {
                    throw new InitializationException(e);
                }
            }
        }

    }

    public static FrameworkConfigBean getBean() {
        if(s_instance == null) {
            getBean(new FrameworkConfiguration());
        }
        return s_instance;
    }
    public static FrameworkConfigBean getBean(FrameworkConfiguration conf) {
        if(s_instance == null){ initialize(conf); }
        return s_instance;
    }




    //ConfigBean for CouchBase Configuration
    public static final BeanPropertyInfo couchBaseBucketNameBeanPropertyInfo =
            createBeanPropertyInfo("couchBaseBucketName", "couchBaseBucketName", true);
    public static final BeanPropertyInfo couchBaseDocLockPeriodBeanPropertyInfo =
            createBeanPropertyInfo("couchBaseDocLockPeriod", "couchBaseDocLockPeriod", true);
    public static final BeanPropertyInfo couchBaseDefaultDocPersistPeriodBeanPropertyInfo =
            createBeanPropertyInfo("couchBaseDefaultDocPersistPeriod", "couchBaseDefaultDocPersistPeriod", true);
    public static final BeanPropertyInfo couchBaseCustomDocPersistPeriodBeanPropertyInfo =
            createBeanPropertyInfo("couchBaseCustomDocPersistPeriodJson", "couchBaseCustomDocPersistPeriodJson", true);

    //ConfigBean for throttling Configuration
    public static final BeanPropertyInfo throttlingRateLimitAllowedBeanPropertyInfo =
            createBeanPropertyInfo("throttlingRateLimitAllowed", "throttlingRateLimitAllowed", true);
    public static final BeanPropertyInfo throttlingRateLimitPeriodBeanPropertyInfo =
            createBeanPropertyInfo("throttlingRateLimitPeriod", "throttlingRateLimitPeriod", true);

    //ConfigBean for pipeline Configuration
    public static final BeanPropertyInfo dbBackedBeanPropertyInfo =
            createBeanPropertyInfo("dbBacked", "dbBacked", true);

    ///Other configbeans
    public static final BeanPropertyInfo createRaptorConfigBackupPropertyInfo =
            createBeanPropertyInfo("createRaptorConfigBackup", "createRaptorConfigBackup", true);
    public static final BeanPropertyInfo actorConfigSourcePropertyInfo =
            createBeanPropertyInfo("actorConfigSource", "actorConfigSource", true);


    public String getCouchBaseBucketName() {
        return couchBaseBucketName;
    }
    public void setCouchBaseBucketName(String couchBaseBucketName) {
        this.couchBaseBucketName = couchBaseBucketName;
    }


    public int getCouchBaseDocLockPeriod() {
        return couchBaseDocLockPeriod;
    }
    public void setCouchBaseDocLockPeriod(int couchBaseDocLockPeriod) {
        this.couchBaseDocLockPeriod = couchBaseDocLockPeriod;
    }

    public int getCouchBaseDocPersistPeriod() {
        return couchBaseDefaultDocPersistPeriod;
    }
    public void setCouchBaseDocPersistPeriod(int couchBaseDocPersistPeriod) {
        this.couchBaseDefaultDocPersistPeriod = couchBaseDocPersistPeriod;
    }

    public int getThrottlingRateLimitAllowed() {
        return throttlingRateLimitAllowed;
    }
    public void setThrottlingRateLimitAllowed(int throttlingRateLimitAllowed) {
        this.throttlingRateLimitAllowed = throttlingRateLimitAllowed;
    }

    public int getThrottlingRateLimitPeriod() {
        return throttlingRateLimitPeriod;
    }
    public void setThrottlingRateLimitPeriod(int throttlingRateLimitPeriod) {
        this.throttlingRateLimitPeriod = throttlingRateLimitPeriod;
    }

    public String getCouchBaseCustomDocPersistPeriodJson() {
        return couchBaseCustomDocPersistPeriodJson;
    }

    public void setCouchBaseCustomDocPersistPeriodJson(String newCouchBaseCustomDocPersistPeriodJson) {
        super.changeProperty(couchBaseCustomDocPersistPeriodBeanPropertyInfo, FrameworkConfigBean.couchBaseCustomDocPersistPeriodJson, newCouchBaseCustomDocPersistPeriodJson);
    }

    public static String getActorConfigSource() {
        return actorConfigSource;
    }

    public static void setActorConfigSource(String actorConfigSource) {
        FrameworkConfigBean.actorConfigSource = actorConfigSource;
    }


    //configbean for framework Configuration

    public boolean isDbBacked(){
        return this.dbBacked;
    }
    public void setDbBacked(boolean dbBacked){
        this.dbBacked = dbBacked;
    }
    public int getDelayPeriod(){
        return delayPeriod;
    }
    public void setDelayPeriod(int delayPeriod){
        this.delayPeriod = delayPeriod;
    }
    public int getRetryCount() {
        return retryCount;
    }
    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }
    public int getDbMaxRetry() {
        return dbMaxRetry;
    }
    public void setDbMaxRetry(int dbMaxRetry) {
        this.dbMaxRetry = dbMaxRetry;
    }
    public int getTimeoutPeriod() {
        return timeoutPeriod;
    }
    public void setTimeoutPeriod(int timeoutPeriod) {
        FrameworkConfigBean.timeoutPeriod = timeoutPeriod;
    }
    public boolean isCreateRaptorConfigBackup() {
        return createRaptorConfigBackup;
    }
    public void setCreateRaptorConfigBackup(boolean createRaptorConfigBackup) {
        FrameworkConfigBean.createRaptorConfigBackup = createRaptorConfigBackup;
    }



    /**
     * Returns defined custom document persist time.
     * @param clazz
     * @return Integer. Null if not present.
     */
    protected Integer getCouchbaseCustomDocPersistPeriod(String clazz){
        return customDocPersistTime.get(clazz);
    }

    public int getDocumentPersistPeriod(Object obj){
        Integer  period = getCouchBaseDocPersistPeriod();
        if(obj != null) {
            Class cl = (obj instanceof Class)? (Class) obj :obj.getClass();
            Integer cPeriod = getCouchbaseCustomDocPersistPeriod(cl.getName());
            period = cPeriod!=null?cPeriod:period;
        }
        return period;
    }

    protected Map<String,Integer> pasreCustomDocPersistPeriod(String period){
        Map<String,Integer> map = null;
        if(period != null){
            ObjectMapper mapper = new ObjectMapper();
            TypeReference<HashMap<String,Integer>> typeRef = new TypeReference<HashMap<String,Integer>>() {};
            try {
                map = mapper.readValue(period,typeRef);
            } catch (Exception e) {
                logger.log(LogLevel.ERROR,e.getMessage(),e);
            }
        }
        return map;
    }

    /***********  Property change listeners ***************************************************/

    protected void addListners() {
        addPropertyChangeListener(couchBaseCustomDocPersistPeriodBeanPropertyInfo, new CouchBaseCustomDocPersistPeriodChangeListner());
    }

    class CouchBaseCustomDocPersistPeriodChangeListner implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            Map<String,Integer> newMap = pasreCustomDocPersistPeriod((String)evt.getNewValue());
            if(newMap != null){
                customDocPersistTime = newMap;
            }else {
                couchBaseCustomDocPersistPeriodJson = (String)evt.getOldValue();
            }
        }
    }


}