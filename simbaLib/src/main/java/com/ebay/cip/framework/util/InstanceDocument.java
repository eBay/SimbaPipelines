package com.ebay.cip.framework.util;

import com.ebay.es.cbdataaccess.bean.CouchBaseDocument;

/**
 * Created by hachong on 6/10/2015.
 */
public class InstanceDocument implements CouchBaseDocument {

    private String classPath;
    private Object classData;

    public InstanceDocument() {}
    public InstanceDocument(String classPath, Object classData){
        this.classPath = classPath;
        this.classData = classData;
    }

    public String getClassPath(){
        return this.classPath;
    }

    public Object getClassData(){
        return this.classData;
    }

    @Override
    public void setType(String s) {

    }

}
