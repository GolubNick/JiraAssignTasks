package assignTasks.controller;

import assignTasks.helper.AccountHelper;
import assignTasks.helper.DashboardHelper;
import assignTasks.helper.JiraRestHelper;
import assignTasks.helper.SQLiteJDBCDriverHelper;
import assignTasks.model.JiraIssue;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import rx.schedulers.Schedulers;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
public class WebController implements WebMvcConfigurer {

    public static boolean isSessionExist = false;

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/results").setViewName("results");
    }

    @GetMapping("/")
    public String showForm(Model model, JiraIssue jiraIssue) {
        SQLiteJDBCDriverHelper sqliteHelper = new SQLiteJDBCDriverHelper();
        ArrayList<String> listEngineers = sqliteHelper.getAllEngineers();
        model.addAttribute("engineers", listEngineers.toArray(new String[listEngineers.size()]));
        if (!isSessionExist){
            return "randomAssignForm";
        }
        else {
            return "busy";
        }
    }


    @RequestMapping(value="/myform", method= RequestMethod.POST)
    public String assignJiraTasksToPerson(JiraIssue jiraIssue, BindingResult bindingResult, HttpServletRequest request) {
        if (isSessionExist){
            return "busy";
        }
        isSessionExist = true;
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        System.out.println("\n\n********************************************************************************************\n" + dateFormat.format(cal.getTime()));
        System.out.println("ip address is: " + request.getRemoteAddr());
        System.out.println("Login is: " + jiraIssue.getUsername());
        System.out.println("Jira task is: " + jiraIssue.getIssue());
        System.out.println("Dashboard report is: " + jiraIssue.getNumberReport());
        JiraRestHelper jiraRestHelper = new JiraRestHelper();
        SQLiteJDBCDriverHelper sqliteHelper = new SQLiteJDBCDriverHelper();
        String cookie = jiraRestHelper.loginToAccount(jiraIssue.getUsername(), jiraIssue.getPassword());
        System.out.println("Cookie is " + cookie);
        if (cookie.equals("Login failed")){
            isSessionExist = false;
            return "loginFailed";
        }
        jiraRestHelper.stopWatchingIssue(jiraIssue.getIssue(), jiraIssue.getUsername(), cookie);
        randomAssignJiraTasksToPersonAndAssignJunior(sqliteHelper, jiraRestHelper, cookie, jiraIssue, bindingResult);
        System.out.println(dateFormat.format(cal.getTime()) + "\n--------------------------------------------------------------------------------------------\n\n");
        isSessionExist = false;
        if (jiraIssue.getIsNewJiraIssue().equals("true")) {
            DashboardHelper dashHelper = new DashboardHelper();
            HashSet<String> setOldIssues = dashHelper.getAllIssuesFromReportByCombaineRun(jiraIssue.getNumberReport());
            System.out.println("Start reopen old tasks");
            rx.Observable<String> values = rx.Observable.from(setOldIssues).subscribeOn(Schedulers.newThread());
            values.subscribe(
                    task -> jiraRestHelper.reopenTask(task, cookie),
                    error -> System.out.println("Error: " + error),
                    () -> System.out.println("Old tasks were reopened")
            );
        }
        System.out.println("isSessionExist " + isSessionExist);
        return "redirect:/results";
    }

    private void randomAssignJiraTasksToPersonAndAssignJunior(SQLiteJDBCDriverHelper sqliteHelper, JiraRestHelper jiraRestHelper, String cookie, JiraIssue jiraIssue, BindingResult bindingResult) {
        ArrayList<String> listJiraSubTasks;
        ArrayList<String> listFreeJiraSubTasks;
        System.out.println("#NewJiraIssue is " + jiraIssue.getIsNewJiraIssue());
        listJiraSubTasks = jiraRestHelper.getAllsubTasksFromTask(cookie, jiraIssue.getIssue());
        if (jiraIssue.getIsNewJiraIssue().equals("false")) {
            System.out.println("#false");
            listFreeJiraSubTasks = jiraRestHelper.getAllFreeSubTasksFromTask(cookie, listJiraSubTasks);
            listJiraSubTasks = listFreeJiraSubTasks;

        }
        List<String> listJSJiraSubTasksUnassigned =  (ArrayList<String>) listJiraSubTasks.clone();
        ArrayList<String> juniors = new ArrayList<>();
        for (String junior : jiraIssue.getDutyPerson()){
            if (AccountHelper.getInstance().isJuniorKey(junior)){
                juniors.add(AccountHelper.getInstance().getJuniorFullName(junior));
            }
        }
        for (String jiraSubTask : listJiraSubTasks){
            String[] casesIds = jiraRestHelper.getTaskInfo(cookie, jiraSubTask);
            if (casesIds.length == 1){
                String author = jiraRestHelper.getIssueAuthorBySummary(cookie, casesIds[0]);
                System.out.println("*** case id is " + casesIds[0] + " author is " + author);
                if (AccountHelper.getInstance().isJuniorName(author) && !juniors.contains(author)){
                    System.out.println("*** " + author + " is AQA junior");
                    String summary = jiraRestHelper.getIssueSummaryById(jiraSubTask, cookie);
                    jiraRestHelper.assigneTaskToName(jiraSubTask, author, cookie);
                    sqliteHelper.insert(author, casesIds[0], jiraSubTask, jiraIssue.getIssue(), summary);
                    jiraRestHelper.stopWatchingIssue(jiraSubTask, jiraIssue.getUsername(), cookie);
                    listJSJiraSubTasksUnassigned.remove(jiraSubTask);
                }
            }
        }

        randomAssignTasks(sqliteHelper, jiraRestHelper, jiraIssue, listJSJiraSubTasksUnassigned, cookie);
    }

    private void randomAssignTasks(SQLiteJDBCDriverHelper sqliteHelper, JiraRestHelper jiraRestHelper, JiraIssue jiraIssue, List<String> listJiraSubTasks, String cookie){
        int index = 0;
        String[] duties = jiraIssue.getDutyPerson();
        System.out.println("Duties are: " + duties.toString());
        Collections.shuffle(Arrays.asList(duties));
        for (String jiraSubTask : listJiraSubTasks) {
            int point = 0;
            String person = "";
            String comment = "";
            String[] casesIds = jiraRestHelper.getTaskInfo(cookie, jiraSubTask);
            String summary = jiraRestHelper.getIssueSummaryById(jiraSubTask, cookie);
            for (String caseId : casesIds) {
                person = sqliteHelper.getPersonByTestId(caseId, jiraIssue.getIssue());
                sqliteHelper.insert(person.isEmpty() ? AccountHelper.getInstance().getFullName(duties[index]) : person, caseId, jiraSubTask, jiraIssue.getIssue(), summary);
                if (!person.isEmpty()) {
                    if (casesIds.length != 1) {
                        comment = caseId + " - " + person + "\n" + comment;
                    }
                    point++;
                }
            }
            jiraRestHelper.assigneTaskToName(jiraSubTask, casesIds.length == point ? person : AccountHelper.getInstance().getFullName(duties[index]), cookie);
            if (!comment.isEmpty()) {
                System.out.println("\n******************************\n" + comment + " - " + jiraSubTask + "\n******************************\n");
                jiraRestHelper.addComment(jiraSubTask, comment, cookie);
            }
            jiraRestHelper.stopWatchingIssue(jiraSubTask, jiraIssue.getUsername(), cookie);
            index = index < duties.length - 1 ? ++index : 0;
        }
    }
}
