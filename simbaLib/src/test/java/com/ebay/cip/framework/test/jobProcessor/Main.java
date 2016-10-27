package com.ebay.cip.framework.test.jobProcessor;

import com.ebay.kernel.util.Base64;

/**
 * Created by jagmehta on 5/12/2015.
 */
public class Main {

    public static void main(String...argv){
        String s = "Jagrut";
        String encoded = Base64.encode(s.getBytes());
        String decoded = new String(Base64.decode(encoded));
        System.out.println(decoded);
    }
}
