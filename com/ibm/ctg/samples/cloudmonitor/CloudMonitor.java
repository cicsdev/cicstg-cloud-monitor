/*
 Copyright IBM Corporation 2014

 LICENSE: Apache License
          Version 2.0, January 2004
          http://www.apache.org/licenses/

 The following code is sample code created by IBM Corporation.
 This sample code is not part of any standard IBM product and
 is provided to you solely for the purpose of assisting you in
 the development of your applications.  The code is provided
 'as is', without warranty or condition of any kind.  IBM shall
 not be liable for any damages arising out of your use of the
 sample code, even if IBM has been advised of the possibility
 of such damages.
*/
package com.ibm.ctg.samples.cloudmonitor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ibm.ctg.monitoring.*;

public class CloudMonitor implements RequestExit {

   private static final String LOG_NAME_PROPERTY = "cloudmonitor.logfile";
   private static final String DB_URL_PROPERTY = "cloudmonitor.dburl";
   
   private final String DB_URL;
   
   private Logger log;
   private ArrayBlockingQueue<JSONObject> queue = new ArrayBlockingQueue<>(100);
   
   public CloudMonitor() throws SecurityException, IOException{
      log = Logger.getLogger(this.getClass().getCanonicalName());
      String logName = System.getProperty(LOG_NAME_PROPERTY);
      if(logName != null && logName.length() > 0){
         log.addHandler(new FileHandler(logName));
      }
      DB_URL = System.getProperty(DB_URL_PROPERTY);
      Thread dbStore = new Thread(new DBStore());
      dbStore.setDaemon(true);
      dbStore.start();
   }
   
   @Override
   public void eventFired(RequestEvent event, Map<RequestData, Object> data) {
      if (!event.equals(RequestEvent.Command) && !event.equals(RequestEvent.ShutDown)) {
         // Build a JSON object
         try {
            JSONObject json = new JSONObject();
            json.put("RequestEvent", event.name());
            for(RequestData dataPoint : RequestData.values()){
               Object value = data.get(dataPoint);
               switch(dataPoint){
               case PayLoad:
                  //deal with a COMMAREA
                  if(value == null){
                     json.put(dataPoint.name(), "");
                  } else {
                     TransientPayLoad payload = (TransientPayLoad)value;
                     json.put(dataPoint.name(), Base64.encodeBase64(payload.getBytes(0, payload.getLength())));
                  }
                  break;
               case Channel:
                  //deal with a channel
                  JSONObject chanObj = new JSONObject();
                  if(value != null){
                     ChannelInfo channel = (ChannelInfo)value;
                     chanObj.put("name", channel.getName());
                     JSONArray containers = new JSONArray();
                     for(ContainerInfo container : channel.getContainers()){
                        JSONObject containerObj = new JSONObject();
                        containerObj.put("name", container.getName());
                        switch(container.getType()){
                        case CHAR:
                           containerObj.put("charcontainer", true);
                           containerObj.put("content", container.getCharData());
                           break;
                        case BIT:
                           containerObj.put("charcontainer", false);
                           containerObj.put("content", Base64.encodeBase64(container.getBitData()));
                        }
                        containers.put(containerObj);
                     }
                     chanObj.put("containers", containers);
                  }
                  json.put(dataPoint.name(), chanObj);
                  break;
               default:
                  if(value == null){
                     value = "";
                  }
                  json.put(dataPoint.name(), value);
               }
               
            }
            queue.put(json);
         } catch (JSONException e) {
            log.log(Level.SEVERE, "Failed to create JSON Object " + e.getMessage());
         } catch (InterruptedException e) {
            log.log(Level.SEVERE, "Failed to store JSON Object on Queue " + e.getMessage());
         } catch (InvalidContainerTypeException e) {
            log.log(Level.SEVERE, "Called wrong get data method on Conatiner " + e.getMessage());
         } catch (ContainerInfoContentException e) {
            log.log(Level.SEVERE, "ContainerInfo no longer valid " + e.getMessage());
         }
      }
   }
   
   private class DBStore implements Runnable {
      @Override
      public void run(){
         while(true){
            try {
               
               JSONObject json = queue.take();
               
               // Store it in the Cloudant Database
               URL dbUrl;
               dbUrl = new URL(DB_URL);
               HttpURLConnection dbConn = (HttpURLConnection) dbUrl
                     .openConnection();
               if (dbUrl.getUserInfo() != null) {
                  String basicAuth = "Basic "
                        + new String(new Base64().encode(dbUrl.getUserInfo().getBytes()));
                  dbConn.setRequestProperty("Authorization", basicAuth);
               }
               dbConn.setRequestMethod("POST");
               dbConn.setDoOutput(true);
               dbConn.setDoInput(true);
               dbConn.setRequestProperty("Content-Type", "application/json");
               dbConn.setRequestProperty("Accept-Charset", "UTF-8");
               log.log(Level.FINE, json.toString(3));
               dbConn.getOutputStream().write(json.toString().getBytes("UTF-8"));
               int responseCode = dbConn.getResponseCode();
               if(responseCode != 201 && responseCode != 200){
                  log.log(Level.SEVERE, "Failed to get successful return code rc=" + responseCode);
                  BufferedReader br = new BufferedReader(new InputStreamReader(dbConn.getErrorStream()));
                  StringBuilder sb = new StringBuilder();
                  String line = br.readLine();
                  while(line != null){
                     sb.append(line);
                     line = br.readLine();
                  }
                  log.log(Level.SEVERE, "Error response from server - " + sb.toString());
               }
            } catch (InterruptedException | IOException | JSONException e) {
               log.throwing(this.getClass().getName(), "run", e);
            }
         }
      }
   }
}
