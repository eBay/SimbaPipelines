package com.ebay.cip.framework;

import com.ebay.cip.service.FeedRequestData;
import com.ebay.cip.service.FeedResponseData;

/**
 * Created by jagmehta on 8/24/2015.
 */
public interface FeedContext extends Context{

    FeedRequestData getFeedRequestData();
    FeedResponseData getFeedResponseData();
    String getPipelineKey();
    String getFeedId();
}
