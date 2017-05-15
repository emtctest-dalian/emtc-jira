package com.pactera.emtc.impl;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.pactera.emtc.http.HttpHandler;
import com.pactera.emtc.http.HttpsHandler;
import com.pactera.emtc.servlet.EmtcServer;
import com.pactera.emtc.tools.EncodeChange;
import org.apache.commons.httpclient.NameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple JIRA listener using the atlassian-event library and demonstrating
 * plugin lifecycle integration.
 */
@ExportAsService
@Named("eventListener")
public class IssueCreatedResolvedListener implements InitializingBean, DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(IssueCreatedResolvedListener.class);
    @ComponentImport
    private final EventPublisher eventPublisher;

    private HttpHandler https;

    protected static final String ORDERFIELD = "Order";
    protected static final String EQUIPFIELD = "Equipment";
    protected static final String SEVERITY = "Severity";
    /**
     * Constructor.
     * @param eventPublisher injected {@code EventPublisher} implementation.
     */
    @Inject
    public IssueCreatedResolvedListener(EventPublisher eventPublisher) {
        System.out.println("IssueCreatedResolvedListener");
        this.eventPublisher = eventPublisher;
    }

    /**
     * Called when the plugin has been enabled.
     * @throws Exception
     */
    public void afterPropertiesSet() throws Exception {
        // register ourselves with the EventPublisher
        eventPublisher.register(this);
    }

    /**
     * Called when the plugin is being disabled or removed.
     * @throws Exception
     */
    public void destroy() throws Exception {
        // unregister ourselves with the EventPublisher
        eventPublisher.unregister(this);
    }

    /**
     * Receives any {@code IssueEvent}s sent by JIRA.
     * @param issueEvent the IssueEvent passed to us
     */
    @EventListener
    public void onIssueEvent(IssueEvent issueEvent) {
        Long eventTypeId = issueEvent.getEventTypeId();
        System.out.println("eventTypeId:"+eventTypeId);
        https = new HttpHandler(EmtcServer.host,8882);
        Issue issue = issueEvent.getIssue();
        Comment comment = issueEvent.getComment();
        // if it's an event we're interested in, log it
        if (eventTypeId.equals(EventType.ISSUE_CREATED_ID)) {
            //新增bug
            putIssue(issue);
        } else if (eventTypeId.equals(EventType.ISSUE_UPDATED_ID)){
            //修改bug
            putIssue(issue);
            if(comment != null){
                putComment(comment);
            }
        } else if ( eventTypeId.equals( EventType.ISSUE_DELETED_ID ) ){
            //删除bug
            deleteIssue(issue);
        }else if (eventTypeId.equals(EventType.ISSUE_ASSIGNED_ID)){
            //修改指派人
            putIssue(issue);
        } else if (eventTypeId.equals(EventType.ISSUE_GENERICEVENT_ID)){
            //修改bug状态
            putIssue(issue);
        } else if (eventTypeId.equals(EventType.ISSUE_COMMENTED_ID)){
            //新增comment
            putComment(comment);
        } else if ( eventTypeId.equals( EventType.ISSUE_COMMENT_EDITED_ID ) ){
            //编辑comment
            putComment(comment);
        } else if ( eventTypeId.equals( EventType.ISSUE_COMMENT_DELETED_ID ) ){
            //删除comment
            if(comment != null){
                System.out.println("comment is not null");
                deleteComment(comment);
            }
        }
    }

    /**
     * 提交issue数据
     * @param issue
     */
    private void putIssue(Issue issue){

        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        pairs.add(new NameValuePair("jira_issue_id", issue.getId().toString()));
//        pairs.add(new NameValuePair("jira_project_id", issue.getProjectId().toString()));
        pairs.add(new NameValuePair("title",issue.getSummary()));
        pairs.add(new NameValuePair("assignee_id",issue.getAssigneeId()));
        pairs.add(new NameValuePair("created_id",issue.getReporterId()));
        pairs.add(new NameValuePair("description",issue.getDescription()));
        pairs.add(new NameValuePair("priority",issue.getPriority().getId()));
        pairs.add(new NameValuePair("status", issue.getStatus().getName()));

        List<CustomField> customfields = ComponentAccessor.getCustomFieldManager().getCustomFieldObjects(issue);
        String fieldName = "";
        for (CustomField customfield : customfields) {
            fieldName = customfield.getFieldName();
            String fieldValue = customfield.getValue(issue) == null ? "" : customfield.getValue(issue).toString();
//            System.out.println(customfield.getValue(issue).toString() );
            if( fieldName.equals(ORDERFIELD) ){
                //订单编号
                pairs.add(new NameValuePair("order_id",fieldValue));
            }else if( fieldName.equals(EQUIPFIELD) ){
                //设备编号
                pairs.add(new NameValuePair("device_id",fieldValue));
            }else if( fieldName.equals(SEVERITY) ){
                //严重程度
                pairs.add(new NameValuePair("level",fieldValue));
            }
        }

        System.out.println(https.post(EmtcServer.EmtcUrl.PUTBUG.getUrl(),pairs.toArray(new NameValuePair[0])));
    }


    /**
     * 提交comment数据
     * @param comment
     */
    private void putComment(Comment comment){
        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        pairs.add(new NameValuePair("jira_issue_id", comment.getIssue().getId().toString()));
        pairs.add(new NameValuePair("jira_comment_id",comment.getId().toString()));
        pairs.add(new NameValuePair("body",comment.getBody()));
        pairs.add(new NameValuePair("operate_name",comment.getAuthorApplicationUser().getUsername()));

        List<CustomField> customfields = ComponentAccessor.getCustomFieldManager().getCustomFieldObjects(comment.getIssue());
        String fieldName = "";
        for (CustomField customfield : customfields) {
            fieldName = customfield.getFieldName();
            if( fieldName.equals(ORDERFIELD) ){
                //订单编号
                pairs.add(new NameValuePair("order_id",customfield.getValue(comment.getIssue()).toString()));
            }
        }

        System.out.println(https.post(EmtcServer.EmtcUrl.PUTCOMMENT.getUrl(),pairs.toArray(new NameValuePair[0])));
    }

    private void deleteIssue(Issue issue){
        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        pairs.add(new NameValuePair("jira_issue_id", issue.getId().toString()));

        List<CustomField> customfields = ComponentAccessor.getCustomFieldManager().getCustomFieldObjects(issue);
        String fieldName = "";
        for (CustomField customfield : customfields) {
            fieldName = customfield.getFieldName();
            if( fieldName.equals(ORDERFIELD) ){
                //订单编号
                pairs.add(new NameValuePair("order_id",customfield.getValue(issue).toString()));
            }
        }

        System.out.println(https.post(EmtcServer.EmtcUrl.DELETEBUG.getUrl(),pairs.toArray(new NameValuePair[0])));
    }

    private void deleteComment(Comment comment){
        //TODO
    }

    public String emtcStatus(String statusName){
        try {
            String nameUTF =EncodeChange.toUtf8(statusName);
            String sign = "";
            System.out.println(statusName);
            System.out.println(nameUTF);
            if ( nameUTF.equals("新建") ){
                sign = "NEW";
            }else if ( nameUTF.equals("打开") ){
                sign = "OPEN";
            }else if ( nameUTF.equals("拒绝") ){
                sign = "REJECT";
            }else if ( nameUTF.equals("延迟") ){
                sign = "DELAY";
            }else if ( nameUTF.equals("修复") ){
                sign = "REPAIR";
            }else if ( nameUTF.equals("关闭") ){
                sign = "CLOSE";
            }else if ( nameUTF.equals("重新打开") ){
                sign = "REOPEN";
            }else {
                System.out.println("no match");
            }
            System.out.println(sign);
            return sign;
        }catch (Exception e){
            System.out.println(e.getMessage());
            return "";
        }
    }

}