package com.ebay.cip.framework.gatekeeper;

import com.ebay.cip.framework.samples.configBean.FrameworkConfigBean;
import com.ebay.es.cbdataaccess.CouchBaseWrapperDAO;
import com.ebay.kernel.calwrapper.CalEventHelper;

/**
 * Created by hachong on 6/2/2015.
 */
public class DBBackedGateKeeper extends BaseGateKeeper{

    String bucketName = FrameworkConfigBean.getBean().getCouchBaseBucketName();
    final int factor = 1000000;
    int ttl = FrameworkConfigBean.getBean().getCouchBaseDocPersistPeriod();
    CouchBaseWrapperDAO couchBaseClient = CouchBaseWrapperDAO.getInstance(bucketName);


    public boolean isAllowed(String key, BaseThrottleDataSource dataSource){
        boolean allowed = true;
        try {
            long counter = couchBaseClient.incrementCounter(key, 0, (dataSource.getThrottlePeriod())) - 1;
            if (counter > dataSource.getJobAllowed()) {
                //System.out.println("not allowed");
                couchBaseClient.decrementCounter(key);
                return false;
            }
        }catch(Exception e){  //do nothing.
            CalEventHelper.writeWarning("Throttling", e, true, "Exception while throttling. Returning allowed. ");
            allowed = true;
        }
        return allowed;
    }

    @Override
    public Counter getCounterDataAndValidate(String key, BaseThrottleDataSource dataSource) {
        return null;
    }

//    @Override
//    public boolean isAllowed(String key, BaseThrottleDataSource dataSource) {
//        boolean allowed = false;
//        try {
//            Counter counterData = getCounterDataAndValidate(key, dataSource);
//            if (counterData.getCount() < dataSource.getJobAllowed()) {
//                try {
//                    couchBaseClient.incrementCounter(key);
//                } catch (CounterNotFoundException e) {
//                    try {
//                        e.printStackTrace();
//                        long currentTime = System.currentTimeMillis();
//                        long counter = currentTime * factor;
//                        couchBaseClient.createCounter(key, counter, ttl);
//                    } catch (InterruptedException e1) {
//                        e1.printStackTrace();
//                    } catch (ExecutionException e1) {
//                        e1.printStackTrace();
//                    } catch (CounterNotFoundException e1) {
//                        e1.printStackTrace();
//                    }
//                }
//                allowed = true;
//            }
//        }catch(Exception e){
//            allowed = true;
//        }
//        return allowed;
//    }
//
//    /**
//     * This function will get a counterData from DB. the counterData contains information such as count and the timestamp.
//     * to get the timestamp we divide the CounterData with factor constant.
//     * to get the count we modulo the CounterData with factor constant.
//     * after we get the timestamp and count we validate the counter with the dataSource. if the counter is not valid we recreate the counter.
//     * @param key to get the Counter;
//     * @param dataSource use to validate the counter.
//     * @return Counter object
//     */
//    @Override
//    public Counter getCounterDataAndValidate(String key, BaseThrottleDataSource dataSource) {
//
//        try {
//            Object object = couchBaseClient.get(key);
//
//            long currentTime = System.currentTimeMillis();
//            //counter not exist
//            if (object == null) {
//                long counterData = currentTime * factor;
//                couchBaseClient.createCounter(key, counterData, ttl);
//                return new Counter(0, currentTime);
//            }
//
//            //counter exist
//            long obj = Long.valueOf((String) object).longValue();
//            long timeStamp = obj / factor;
//
//            //validate counter
//            if(currentTime - timeStamp > dataSource.getThrottlePeriod()){
//
//                CalEventHelper.writeLog("Framework", "DBBackedGateKeeper.getCounterDataAndValidate()", "counter expired,recreate new counter. Key = "+key, "0");
//                //counter is expired. Recreate the counter
//                long counterData = currentTime * factor;
//                couchBaseClient.deleteSync(key);
//                couchBaseClient.createCounter(key, counterData, ttl);
//                 return new Counter(0, currentTime);
//            }
//            int count = (int) (obj % factor);
//            return new Counter(count, timeStamp);
//
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        } catch (CounterNotFoundException e) {
//            e.printStackTrace();
//        } catch (ProcessingException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }

}
