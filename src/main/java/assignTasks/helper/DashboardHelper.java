package assignTasks.helper;

import org.json.JSONArray;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

public class  DashboardHelper {

    @Autowired
    private Environment env;
    private final String DASH_LINK = env.getProperty("DASH_LINK");

    public HashSet<String> getAllIssuesFromReportByCombaineRun(String run){
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        HashSet<String> setTasks = new HashSet<>();
        System.out.println(dateFormat.format(cal.getTime()) + " ---- Start collect ald issues");
        JSONArray issues;
        HttpResponse response;
        StringBuffer result;
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet httpGet = new HttpGet(DASH_LINK + run);
        try {
            response = client.execute(httpGet);
            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

            result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            issues = (new JSONObject(result.toString()).getJSONArray("linkedTests"));
            issues.forEach((Object x) -> {
                if (((JSONObject) x).has("issue")) {
                    if (String.valueOf(((JSONObject) x).getJSONObject("issue").get("name")).toUpperCase().contains("CDR")) {
                        setTasks.add(String.valueOf(((JSONObject) x).getJSONObject("issue").get("name")));
                    }
                }
            });

        } catch (IOException e) {
            System.out.println("Old issues are " + setTasks);
            e.printStackTrace();
        }
        System.out.println(dateFormat.format(cal.getTime()) + " ---- Old issues have been collected successfully. There are " + setTasks.size());
        return setTasks;
    }
}
