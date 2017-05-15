package com.pactera.emtc.http;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpClientParams;

import java.io.*;

public class HttpHandler {

    protected HttpClient client;

    public HttpHandler(String host){
        client = new HttpClient(new MultiThreadedHttpConnectionManager());
        client.getHostConfiguration().setHost(host,80,"http");
        HttpClientParams params = client.getParams();
        params.setContentCharset("gb2312");
    }

    public HttpHandler(String host,int port){
        client = new HttpClient(new MultiThreadedHttpConnectionManager());
        client.getHostConfiguration().setHost(host,port,"http");
        HttpClientParams params = client.getParams();
        params.setContentCharset("gb2312");
    }

    public String post(String url, NameValuePair[] pairs){
        PostMethod post = new PostMethod(url);
        post.setRequestBody(pairs);
        String response = "";
        try {
            int status  =  client.executeMethod(post);
            response = post.getResponseBodyAsString();
        } catch (HttpException e) {
            e.printStackTrace();
            response = "{'status':0,'err':'"+e.getMessage()+"'}";
        } catch (IOException e)  {
            e.printStackTrace();
            response = "{'status':0,'err':'"+e.getMessage()+"'}";
        }finally{
            post.releaseConnection();
        }
        return response;
    }

}
