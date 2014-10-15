package org.apache.maven.plugin.allura;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.maven.plugin.issues.Issue;
import org.apache.maven.plugins.changes.model.Release;

public class AlluraRestAccess {

    /**
     * The Allura client.
     */
    private HttpClient client;

    private String alluraURL;

    private JsonObject root;

    public AlluraRestAccess(String alluraURL) throws IOException {
        client = new HttpClient();
        this.alluraURL = alluraURL;
        GetMethod method = new GetMethod(this.alluraURL);
        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));
        client.executeMethod(method);
        ByteArrayInputStream in = new ByteArrayInputStream(method.getResponseBody());
        JsonReader jsonReader = Json.createReader(in);
        root = jsonReader.readObject();
    }

    public List<Issue> getIssues() throws HttpException, IOException {
        List<Issue> result = new ArrayList<Issue>();
        for (JsonValue ticket : root.getJsonArray("tickets")) {
            if (ticket instanceof JsonObject) {
                result.add(getIssue(((JsonObject) ticket).getInt("ticket_num")));
            }
        }
        return result;
    }

    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss.SSS");

    private Issue getIssue(int number) throws HttpException, IOException {
        Issue result = new Issue();
        GetMethod method = new GetMethod(this.alluraURL + "/" + number);
        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));
        client.executeMethod(method);
        ByteArrayInputStream in = new ByteArrayInputStream(method.getResponseBody());
        JsonReader jsonReader = Json.createReader(in);
        JsonObject issue = jsonReader.readObject().getJsonObject("ticket");
        if (!issue.isNull("assigned_to")) {
            result.setAssignee(issue.getString("assigned_to"));
        }
        try {
            result.setCreated(format.parse(issue.getString("created_date")));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        result.addFixVersion(issue.getJsonObject("custom_fields").getString("_milestone"));
        result.setId(Integer.toString(issue.getInt("ticket_num")));
        result.setKey(result.getId());
        result.setLink(alluraURL + "/" + result.getKey());
        result.setLink(alluraURL + "/" + result.getKey());
        result.setReporter(issue.getString("reported_by"));
        result.setStatus(issue.getString("status"));
        result.setSummary(issue.getString("description"));
        result.setTitle(issue.getString("summary"));
        result.setType("ISSUE");
        try {
            result.setUpdated(format.parse(issue.getString("mod_date")));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void getMilesoneDescription(String version, Release release) {
        for (JsonValue milestoneObject : root.getJsonArray("milestones")) {
            if (milestoneObject instanceof JsonObject) {
                JsonObject milestone = (JsonObject) milestoneObject;
                if (version.equals(milestone.getString("name"))) {
                    if (!milestone.isNull("due_date")) {
                        release.setDateRelease(milestone.getString("due_date"));
                    }
                    if (!milestone.isNull("description")) {
                        release.setDescription(milestone.getString("description"));
                    }
                }
            }
        }
    }
}
