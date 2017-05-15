package com.pactera.emtc.servlet;

import com.pactera.emtc.http.HttpHandler;
import com.pactera.emtc.http.HttpsHandler;
import org.apache.commons.httpclient.NameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class EmtcServer extends HttpServlet{
    private static final Logger log = LoggerFactory.getLogger(EmtcServer.class);

    public static final String host = "58.215.221.218";

    private HttpHandler https;
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        String response = "";
        https = new HttpHandler(host,8882);

        String method = req.getParameter("method");
        if(method.equals("equipmentList")){
            response = getEquipmentList(req);
        }else if ( method.equals("orderList") ){
            response = getEmtcOrder(req);
        }else if ( method.equals("orderName") ){
            response = getOrderName(req);
        }else if ( method.equals("equipmentName")){
            response = getEquipmentName(req);
        }else {
            response = "{\"status\"=>0,\"err\"=>\"unknow method\"}";
        }
        resp.getWriter().write(response);
    }

    /**
     * 获取订单列表
     * @param req
     * @return
     */
    protected String getEmtcOrder(HttpServletRequest req){
        String url = EmtcUrl.ORDERLIST.getUrl();

        String projectName = req.getParameter("projectName");

        if ( projectName.indexOf('/') >= 0 ){
            projectName = projectName.substring(projectName.lastIndexOf('/')+1);
        }
        String domain = req.getServerPort()==80
                        ?   req.getScheme()+"://"+req.getServerName()
                        :   req.getScheme()+"://"+req.getServerName()+":"+req.getServerPort();
        //post param
        NameValuePair domainPair = new NameValuePair("domain",domain );
        NameValuePair projectNamePair = new NameValuePair("projectName",projectName );
        NameValuePair pidPair = new NameValuePair("pid",req.getParameter("pid") );
        NameValuePair search = new NameValuePair("search", req.getParameter("search"));
        NameValuePair[] pairs = {domainPair,projectNamePair,pidPair,search};

        return https.post(url,pairs);
    }

    /**
     * 获取设备列表
     * @param req
     * @return
     */
    protected String getEquipmentList(HttpServletRequest req){
        String url = EmtcUrl.EQUIPMENTLIST.getUrl();
        //post param
        NameValuePair orderid = new NameValuePair("orderid", req.getParameter("orderid"));
        NameValuePair search = new NameValuePair("search", req.getParameter("search"));
        NameValuePair[] pairs = {orderid,search};

        return https.post(url,pairs);
    }

    /**
     * 获取订单名称
     * @param req
     * @return
     */
    protected String getOrderName(HttpServletRequest req){
        String url = EmtcUrl.ORDERNAME.getUrl();
        //post param
        NameValuePair orderid = new NameValuePair("orderid", req.getParameter("orderid"));
        NameValuePair[] pairs = {orderid};

        return https.post(url,pairs);
    }

    /**
     * 获取设备名称
     * @param req
     * @return
     */
    protected String getEquipmentName(HttpServletRequest req){
        String url = EmtcUrl.EQUIPMENTNAME.getUrl();
        //post param
        NameValuePair equipmentid = new NameValuePair("device_id", req.getParameter("equipmentid"));
        NameValuePair[] pairs = {equipmentid};

        return https.post(url,pairs);
    }

    public enum EmtcUrl{
        ORDERLIST("/webservice/jira/orderformsselect2"),
        EQUIPMENTLIST("/webservice/jira/devicesselect2"),
        ORDERNAME("/webservice/jira/orderformname"),
        EQUIPMENTNAME("/webservice/jira/devicename"),
        PUTBUG("/webservice/jira/putbug"),
        DELETEBUG("/webservice/jira/delbug"),
        PUTCOMMENT("/webservice/jira/putnote");
        private String url;

        EmtcUrl(String url){
            this.url = url;
        }
        public String getUrl(){
            return url;
        }
    }
}