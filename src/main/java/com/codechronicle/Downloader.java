/*
 * Created on Jun 2, 2006
 *
 */

package com.codechronicle;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebResponse;

/**
 * Flickr REST API Endpoint : 
 * 
 * http://www.flickr.com/services/rest/?method=flickr.test.echo&name=value
 * 
 */
public class Downloader {

    private WebConversation wc = new WebConversation();
    private SAXBuilder sb = new SAXBuilder();
    private String apikey = null;
    
    /**
     * @param apikey
     */
    public Downloader(String apikey) {
        super();
        this.apikey = apikey;
    }

    private Document execute(String method, String params) throws Exception {
        String url = "http://www.flickr.com/services/rest/?method=" + method + "&api_key=" + this.apikey;
        if (params != null) {
            url += "&" + params;
        }
        
        URL u = new URL(url);
        Document doc = sb.build(u);
        
        String status = doc.getRootElement().getAttributeValue("stat");
        if ("fail".equalsIgnoreCase(status)) {
            String errorMessage = doc.getRootElement().getChild("err").getAttributeValue("msg");
            throw new Exception("Flickr returned an error : " + errorMessage);
        }

        return doc;
    }
    
    private void downloadImage(String serverId, 
                               String pictureId, 
                               String secret,
                               String farm,
                               String imageName,
                               String size,
                               File outputDirectory) throws Exception {
        BufferedInputStream is = null;
        BufferedOutputStream os = null;
        
        try {
            StringBuffer url = new StringBuffer("http://farm" + farm + ".static.flickr.com");
            url.append("/");
            url.append(serverId);
            url.append("/");
            url.append(pictureId);
            url.append("_");
            url.append(secret);
            url.append("_");
            url.append(getSizeCode(size));
            url.append(".jpg");
            
            GetMethodWebRequest req = new GetMethodWebRequest(url.toString());
            WebResponse res = wc.getResponse(req);
            
            is = new BufferedInputStream(res.getInputStream());
            String fileName = imageName+".jpg";
            File outputFile = new File(outputDirectory,imageName+".jpg");
                        
            System.out.println("Source : " + url);            
            System.out.println("Target : " + outputFile.getAbsolutePath());
            
            os = new BufferedOutputStream(new FileOutputStream(outputFile));
            int i=-1;
            int bytes=0;
            while ( (i = is.read()) != -1) {
                os.write(i);
                bytes++;
            }
            
            System.out.println("Download complete [" + bytes + " bytes] \n");
            
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            is.close();
            os.close();
        }
    }
    
    private static String getSizeCode(String sizeString) throws Exception {
        if ("tiny".equalsIgnoreCase(sizeString)) {
            return "s";
        } else if ("thumb".equalsIgnoreCase(sizeString)) {
            return "t";
        } else if ("small".equalsIgnoreCase(sizeString)) {
            return "m";
        } else if ("large".equalsIgnoreCase(sizeString)) {
            return "b";
        } else if ("original".equalsIgnoreCase(sizeString)) {
            return "o";                        
        } else {
            System.out.println("Unrecognized image size code : " + sizeString);
            System.out.println("Should be tiny | thumb | small | large | original");
            System.out.println("Defaulting to medium");
            throw new Exception("Invalid image size code");
        }
    }

    private static boolean isBlank(String str) {
    	return (str == null) || (str.length() == 0);
    }
    
    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {

        String targetDownloadDirectory = System.getProperty("targetDir");
        String photoSetId = System.getProperty("photoSetId");
        String sizeCodeString = System.getProperty("sizeCode");
        String apikey = System.getProperty("apikey");
        
        System.out.println("targetDownloadDirectory = " + targetDownloadDirectory);
        System.out.println("photoSetId = " + photoSetId);
        System.out.println("sizeCodeString = " + sizeCodeString);
        System.out.println("apikey = " + apikey);        
        
        if (isBlank(targetDownloadDirectory) ||
        		isBlank(photoSetId) ||
        		isBlank(sizeCodeString) ||
        		isBlank(apikey)) {
        	System.out.println("Usage : mvn exec:java -DtargetDir=<target dir> -DphotoSetId=<photoset id> -DsizeCode=<size code> -Dapikey=<apikey>");
        	System.exit(1);
        }
        
        
        File dlDir = new File(targetDownloadDirectory);
        dlDir.mkdirs();
        
        Downloader downloader = new Downloader(apikey);
        
        Document photosetInfo = downloader.execute("flickr.photosets.getInfo", "photoset_id=" + photoSetId);
        String numPhotos = photosetInfo.getRootElement().getChild("photoset").getAttributeValue("photos");
        
        System.out.println("******************************************");
        System.out.println("Downloading a total of " + numPhotos + " photos.");
        System.out.println("******************************************");
        
        Document photoset = downloader.execute("flickr.photosets.getPhotos", "photoset_id=" + photoSetId);
        List photos = photoset.getRootElement().getChild("photoset").getChildren();
        
        int i=1;
        for (Iterator iter = photos.iterator(); iter.hasNext();) {
            Element photo = (Element) iter.next();
            System.out.println("Photo #" + i++);
            
            String secret = photo.getAttributeValue("secret");
            
            // If asking for original size, need to handle it a bit differently.
            // http://www.flickr.com/services/api/misc.urls.html
            if (sizeCodeString.equalsIgnoreCase("original")) {
                secret = getOriginalSecret(apikey, photo.getAttributeValue("id"));
            }
            
            downloader.downloadImage(photo.getAttributeValue("server"),
                                     photo.getAttributeValue("id"),
                                     secret,
                                     photo.getAttributeValue("farm"),
                                     photo.getAttributeValue("title"),
                                     sizeCodeString,
                                     dlDir);
        }
    }

    private static String getOriginalSecret(String apiKey, String photoId) throws Exception {
        Downloader downloader = new Downloader(apiKey);
        Document doc = downloader.execute("flickr.photos.getInfo", "photo_id=" + photoId);
        
        String os = doc.getRootElement().getChild("photo").getAttribute("originalsecret").getValue();
        return os;
    }

}