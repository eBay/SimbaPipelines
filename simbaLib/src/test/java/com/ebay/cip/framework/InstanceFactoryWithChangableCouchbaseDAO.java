package com.ebay.cip.framework;

import com.ebay.cip.framework.util.InstanceFactory;
import com.ebay.es.cbdataaccess.CouchBaseWrapperDAO;

/**
 * Created by jagmehta on 11/4/2015.
 */
public class InstanceFactoryWithChangableCouchbaseDAO extends InstanceFactory {
    private CouchBaseWrapperDAO originalDAO;
    public InstanceFactoryWithChangableCouchbaseDAO(boolean isDBBacked){
        super(isDBBacked);
        this.originalDAO = super.couchBaseDAO;
    }
    public void setCouchbaseWrapperDAO(CouchBaseWrapperDAO dao){
        super.couchBaseDAO = dao;
    }
    public void resetToOriginal() { super.couchBaseDAO = originalDAO;}
    @Override
    protected void finalize() throws Exception {resetToOriginal();}
}
