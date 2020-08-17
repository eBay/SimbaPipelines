package com.ebay.cip.framework.samples.configBean;

import com.ebay.aero.kernel.init.InitializationException;
import com.ebay.kernel.bean.configuration.BaseConfigBean;
import com.ebay.kernel.bean.configuration.BeanConfigCategoryInfo;
import com.ebay.kernel.bean.configuration.BeanPropertyInfo;
import com.ebay.kernel.context.AppBuildConfig;

/**
 * Sample config bean for LMS sample
 * Created by hachong on 6/4/2015.
 */
public class LMSConfigBean extends BaseConfigBean {
    private static LMSConfigBean s_instance = null;
    private static final String CONFIG_CATEGORY_ID = "com.ebay.cip.framework.samples.configBean.LMSConfigBean";
    private static final String ALIAS = "LMSConfigBean";
    private static final String GROUP = "com.ebay.cip.framework.samples";
    private static final String DESCRIPTION = "This config bean is for LMS sample";

    private LMSConfigBean(BeanConfigCategoryInfo category) {
        defineVariablesBasedOnEnv();
        init(category, true);
    }

    protected void defineVariablesBasedOnEnv() {

        AppBuildConfig buildConfig = AppBuildConfig.getInstance();
        //Production env
        if(buildConfig.isProduction()){

        }else if(buildConfig.isLandP()) {
            authToken = "Service Authentication token";
            tradingApiURL = "Service API endpoint";
        }else if(buildConfig.isSandbox()) {

        }else if(buildConfig.isQATE() || buildConfig.isDev()) {
            authToken = "Service Authentication token";
            //tradingApiURL = "";
        }


    }

    public static void initialize() {
        synchronized (LMSConfigBean.class) {
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
                    s_instance = new LMSConfigBean(category);
                }
                catch (Exception e) {
                    throw new InitializationException(e);
                }
            }
        }

    }

    public static LMSConfigBean getBean() {
        if(s_instance == null){ initialize(); }
        return s_instance;
    }


    public static final BeanPropertyInfo tradingApiBeanPropertyInfo =
            createBeanPropertyInfo("tradingApiURL", "testParam1", true);
    public static final BeanPropertyInfo maxRetryBeanPropertyInfo =
            createBeanPropertyInfo("maxRetry", "maxRetry", true);

    public static final BeanPropertyInfo authTokenBeanPropertyInfo =
            createBeanPropertyInfo("authToken", "authToken", true);

    public static final BeanPropertyInfo APIJobTimeoutPropertyInfo =
            createBeanPropertyInfo("APIJobTimeout", "APIJobTimeout", true);

    public static final BeanPropertyInfo splitterJobTimeoutPropertyInfo =
            createBeanPropertyInfo("splitterJobTimout", "splitterJobTimout", true);

    private String tradingApiURL = "http://eazye.qa.ebay.com/ws/api.dll";  // QA URL
    public String getTradingApiURL() {
        return tradingApiURL;
    }
    public void setTradingApiURL(String tradingApiURL) {
        this.tradingApiURL = tradingApiURL;
    }

    private String maxRetry = "3";
    public String getMaxRetry() {
        return maxRetry;
    }
    public void setMaxRetry(String tradingApiURL) {
        this.maxRetry = tradingApiURL;
    }

    private String authToken = "v^1.1#i^1#r^1#I^3#f^0#p^3#t^Ul4yX0ZCMTgyQUIxQTFFQkMyRTJDNDU0MEIwNzJDRTgxM0U2I0VeNTE2"; // for QA
    public String getAuthToken(){
        return authToken;
    }
    public void setAuthToken(String lAuthToken){
        this.authToken = lAuthToken;
    }

    private int APIJobTimeout = 60;
    public int getAPIJobTimeout() {
        return APIJobTimeout;
    }
    public void setAPIJobTimeout(int APIJobTimeout) {
        this.APIJobTimeout = APIJobTimeout;
    }

    private int splitterJobTimout = 3600;
    public int getSplitterJobTimout() {
        return splitterJobTimout;
    }
    public void setSplitterJobTimout(int splitterJobTimout) {
        this.splitterJobTimout = splitterJobTimout;
    }
}
