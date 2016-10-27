package com.ebay.cip.util;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This is a generic class which calls REST services.
 *
 * @author jagmehta
 *
 */
public class BasicRestServiceClient {
    public static String post(String wsURL,String payload,Map<String,String> reqProps) throws MalformedURLException, IOException {

        // Code to make a webservice HTTP request
        String responseString = "";
        String outputString = "";
        //String wsURL = "http://www.deeptraining.com/webservices/weather.asmx";
        URL url = new URL(wsURL);
        URLConnection connection = url.openConnection();
        HttpURLConnection httpConn = (HttpURLConnection) connection;
        ByteArrayOutputStream bout = new ByteArrayOutputStream();

        byte[] buffer = new byte[payload.length()];
        buffer = payload.getBytes();
        bout.write(buffer);
        byte[] b = bout.toByteArray();
/*		String SOAPAction = "http://litwinconsulting.com/webservices/GetWeather";
		// Set the appropriate HTTP parameters.
		httpConn.setRequestProperty("Content-Length", String.valueOf(b.length));
		httpConn.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
		httpConn.setRequestProperty("SOAPAction", SOAPAction);
*/
        addRequestProps(httpConn, reqProps);
        httpConn.setRequestMethod("POST");
        httpConn.setDoOutput(true);
        httpConn.setDoInput(true);
        OutputStream out = httpConn.getOutputStream();
        // Write the content of the request to the outputstream of the HTTP
        // Connection.
        out.write(b);
        out.close();
        // Ready with sending the request.

        //Read status

		Map<String, List<String>> map = httpConn.getHeaderFields();
		for (Map.Entry<String, List<String>> entry : map.entrySet()) {
			System.out.println("Key : " + entry.getKey() +" ,Value : " + entry.getValue());
		}

        // Read the response.
        InputStreamReader isr = new InputStreamReader(httpConn.getInputStream());
        BufferedReader in = new BufferedReader(isr);

        // Write the SOAP message response to a String.
        while ((responseString = in.readLine()) != null) {
            outputString = outputString + responseString;
        }
         return outputString;

    }
    static void addRequestProps(HttpURLConnection c,Map<String,String> p){
        Iterator it = p.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            c.addRequestProperty((String)pairs.getKey(), (String)pairs.getValue());
        }
    }

}
