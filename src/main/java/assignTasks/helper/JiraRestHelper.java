package assignTasks.helper;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import assignTasks.controller.WebController;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import static assignTasks.controller.WebController.isSessionExist;

public class JiraRestHelper {
    @Autowired
    private Environment env;
    private final String JIRALINK = env.getProperty("JIRALINK");

    public String loginToAccount(String username, String password) {
        String cookie = "";
        HttpResponse response;
        StringBuffer result;
        JSONObject json = new JSONObject();
        json.put("username", username);
        json.put("password", password);
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost httpPost = new HttpPost(JIRALINK + "/rest/auth/1/session");
        httpPost.addHeader("content-type", "application/json");

        try {
            httpPost.setEntity(new StringEntity(json.toString()));
            response = client.execute(httpPost);
            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

            result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            if (new JSONObject(result.toString()).has("session")) {
                cookie = new JSONObject(result.toString()).getJSONObject("session").get("value").toString();
            }
            else{
                cookie = "Login failed";
            }
        } catch (Exception e) {
            System.out.println("cookie is " + cookie);
            isSessionExist = false;
            e.printStackTrace();
        }

        return cookie;
    }

    public ArrayList<String> getAllsubTasksFromTask(String JSESSIONID, String issue) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        System.out.println(dateFormat.format(cal.getTime()) + " ---- Start collect subtasks");
        ArrayList<String> listSubTasks = new ArrayList<>();
        JSONArray subtasks;
        HttpResponse response;
        StringBuffer result;
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet httpGet = new HttpGet(JIRALINK + "/rest/api/2/issue/" + issue);
        httpGet.addHeader("Cookie", "JSESSIONID=" + JSESSIONID);
        try {
            response = client.execute(httpGet);
            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

            result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            subtasks = new JSONObject(result.toString()).getJSONObject("fields").getJSONArray("subtasks");
            subtasks.forEach((Object x) -> listSubTasks.add(String.valueOf(((JSONObject) x).get("key"))));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(dateFormat.format(cal.getTime()) + " ---- Subtasks have been collected successfully");
        return listSubTasks;
    }

    public String[] getTaskInfo(String JSESSIONID, String issue) {
        String[] casesIds = new String[0];
        HttpResponse response;
        StringBuffer result;
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet httpGet = new HttpGet(JIRALINK + "/rest/api/2/issue/" + issue);
        httpGet.addHeader("Cookie", "JSESSIONID=" + JSESSIONID);
        try {
            response = client.execute(httpGet);
            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

            result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            casesIds = new JSONObject(result.toString()).getJSONObject("fields").get("description").toString().split("\n")[0].replaceAll("\\s+", "").split(",");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return casesIds;
    }

    public void assigneTaskToName(String task, String name, String JSESSIONID) {
        Calendar cal = Calendar.getInstance();
        JSONObject json = new JSONObject();
        json.put("name", name);
        HttpClient client = HttpClientBuilder.create().build();
        HttpPut httpPut = new HttpPut(JIRALINK + "/rest/api/2/issue/" + task + "/assignee");
        httpPut.addHeader("Cookie", "JSESSIONID=" + JSESSIONID);
        httpPut.addHeader("content-type", "application/json");

        try {
            httpPut.setEntity(new StringEntity(json.toString()));
            client.execute(httpPut);
            System.out.println(cal.getTime() + " --- " + task + " has been assigned to " + name);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getIssueAuthorBySummary(String JSESSIONID, String summary) {
        String author = "";
        HttpResponse response;
        StringBuffer result;
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet httpGet = new HttpGet(JIRALINK + "/rest/api/2/search?jql=summary~'" + summary + "'");
        httpGet.addHeader("Cookie", "JSESSIONID=" + JSESSIONID);
        try {
            response = client.execute(httpGet);
            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

            result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            JSONArray array = new JSONObject(result.toString()).getJSONArray("issues");
            author = array.length() == 0 ? "" :
                    (array.getJSONObject(0).getJSONObject("fields").isNull("assignee") ? "" :
                            array.getJSONObject(0).getJSONObject("fields").getJSONObject("assignee").get("name").toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return author.toLowerCase();
    }

    public ArrayList<String> getAllFreeSubTasksFromTask(String JSESSIONID, ArrayList<String> issues) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        System.out.println(dateFormat.format(cal.getTime()) + " ---- Start collect subtasks");
        ArrayList<String> listSubTasks = new ArrayList<>();
        HttpResponse response;
        StringBuffer result;
        for (String issue : issues) {
            HttpClient client = HttpClientBuilder.create().build();
            System.out.println("@ " + issue);
            HttpGet httpGet = new HttpGet(JIRALINK + "/rest/api/2/issue/" + issue);
            httpGet.addHeader("Cookie", "JSESSIONID=" + JSESSIONID);
            try {
                response = client.execute(httpGet);
                BufferedReader rd = new BufferedReader(
                        new InputStreamReader(response.getEntity().getContent()));

                result = new StringBuffer();
                String line = "";
                while ((line = rd.readLine()) != null) {
                    result.append(line);
                }
                if (new JSONObject(result.toString()).getJSONObject("fields").isNull("assignee"))
                    listSubTasks.add(issue);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println(dateFormat.format(cal.getTime()) + " ---- Subtasks have been collected successfully");
        return listSubTasks;
    }

    public void reopenTask(String task, String JSESSIONID) {
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost httpPost = new HttpPost(JIRALINK + "/rest/api/2/issue/" + task + "/transitions");
        httpPost.addHeader("Cookie", "JSESSIONID=" + JSESSIONID);
        httpPost.addHeader("content-type", "application/json");
        String json = "{\"transition\":{\"id\":\"3\"}}";
        try {
            httpPost.setEntity(new StringEntity(json));
            client.execute(httpPost);
            System.out.println(task + " has been reopened successfully");
        } catch (Exception e) {
            WebController.isSessionExist = false;
            System.out.println(task + " is failed");
            e.printStackTrace();
        }

    }

    public void stopWatchingIssue(String task, String username, String JSESSIONID){
        HttpClient client = HttpClientBuilder.create().build();
        HttpDelete httpDelete = new HttpDelete(JIRALINK + "/rest/api/2/issue/" + task + "/watchers?username=" + username);
        httpDelete.addHeader("Cookie", "JSESSIONID=" + JSESSIONID);
        try {
            client.execute(httpDelete);
            System.out.println(task + " Stop watching this issue ");
        } catch (Exception e) {
            System.out.println(task + " Stop watching this issue is failed");
            e.printStackTrace();
        }
    }

    public String getIssueSummaryById(String task, String JSESSIONID){
        String summary = "";
        HttpResponse response;
        StringBuffer result;
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet httpGet = new HttpGet(JIRALINK + "/rest/api/2/issue/" + task + "?fields=summary");
        httpGet.addHeader("Cookie", "JSESSIONID=" + JSESSIONID);
        try {
            response = client.execute(httpGet);
            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

            result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            summary = new JSONObject(result.toString()).getJSONObject("fields").get("summary").toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return summary;
    }

    public void addComment(String task, String comment, String JSESSIONID){
        HttpResponse response;
        StringBuffer result;
        JSONObject json = new JSONObject();
        json.put("body", comment);
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost httpPost = new HttpPost(JIRALINK + "/rest/api/2/issue/" + task + "/comment");
        httpPost.addHeader("Cookie", "JSESSIONID=" + JSESSIONID);
        httpPost.addHeader("content-type", "application/json");

        try {
            httpPost.setEntity(new StringEntity(json.toString()));
            response = client.execute(httpPost);
            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

            result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
