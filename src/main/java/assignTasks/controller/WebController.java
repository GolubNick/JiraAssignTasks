package assignTasks.controller;

import assignTasks.helper.AccountHelper;
import assignTasks.helper.DashboardHelper;
import assignTasks.helper.JiraRestHelper;
import assignTasks.helper.SQLiteJDBCDriverHelper;
import assignTasks.helper.GetProperties;
import assignTasks.model.JiraIssue;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import rx.schedulers.Schedulers;
import java.net.InetAddress;
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
        String positionSpecialUser = sqliteHelper.getEngineerPosition(GetProperties.getInstance().getProperties("specialUser"));
        model.addAttribute("engineers", listEngineers.toArray(new String[listEngineers.size()]));
        model.addAttribute("positionSpecialUser", positionSpecialUser);
        if (!isSessionExist) {
            return "randomAssignForm";
        } else {
            return "busy";
        }
    }

    @GetMapping("/addEngineer")
    @ResponseBody
    public String addEngineerPanel(@RequestParam(name = "") String fullName, @RequestParam(name = "") String name,
                                   @RequestParam(name = "") String position, @RequestParam(name = "") String birthday, @RequestParam(name = "") String pcname) {
        SQLiteJDBCDriverHelper sqliteHelper = new SQLiteJDBCDriverHelper();
        sqliteHelper.addNewEngineer(name, fullName, position, birthday, pcname);
        return fullName + " has been added!";
    }

    @GetMapping("/removeEngineer")
    @ResponseBody
    public String removeEngineerPanel(@RequestParam(name = "") String fullName) {
        SQLiteJDBCDriverHelper sqliteHelper = new SQLiteJDBCDriverHelper();
        sqliteHelper.removeEngineerByFullName(fullName);
        return fullName + " has been deleted!";
    }

    @GetMapping("/changePosition")
    @ResponseBody
    public String changePosition(@RequestParam(name = "") String fullName, @RequestParam(name = "") String position) {
        SQLiteJDBCDriverHelper sqliteHelper = new SQLiteJDBCDriverHelper();
        sqliteHelper.changeEngineerPositionByFullName(fullName, position);
        return fullName + " has been updated!";
    }


    @RequestMapping(value = "/myform", method = RequestMethod.POST)
    public String assignJiraTasksToPerson(JiraIssue jiraIssue, BindingResult bindingResult, HttpServletRequest request) throws Exception {
        if (isSessionExist) {
            return "busy";
        }
        isSessionExist = true;
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String PCName = InetAddress.getByName(request.getRemoteAddr()).getHostName().split(".in")[0];
        Calendar cal = Calendar.getInstance();
        System.out.println("\n\n********************************************************************************************\n" + dateFormat.format(cal.getTime()));
        System.out.println("ip address is: " + request.getRemoteAddr());
        System.out.println("pcname is: " + PCName);
        System.out.println("Login is: " + jiraIssue.getUsername());
        System.out.println("Jira task is: " + jiraIssue.getIssue());
        System.out.println("Dashboard report is: " + jiraIssue.getNumberReport());
        JiraRestHelper jiraRestHelper = new JiraRestHelper();
        SQLiteJDBCDriverHelper sqliteHelper = new SQLiteJDBCDriverHelper();
        String cookie = jiraRestHelper.loginToAccount(jiraIssue.getUsername(), jiraIssue.getPassword());
        System.out.println("Cookie is " + cookie);
        if (cookie.equals("Login failed")) {
            isSessionExist = false;
            return "loginFailed";
        }
        jiraIssue.setEngineer(sqliteHelper.getFullNameByPCName(PCName));
        System.out.println("Engineer is " + jiraIssue.getEngineer());
        jiraRestHelper.stopWatchingIssue(jiraIssue.getIssue(), jiraIssue.getEngineer(), cookie);
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
        randomAssignTasks(sqliteHelper, jiraRestHelper, jiraIssue, listJiraSubTasks, cookie);
    }

    private void randomAssignTasks(SQLiteJDBCDriverHelper sqliteHelper, JiraRestHelper jiraRestHelper, JiraIssue jiraIssue, List<String> listJiraSubTasks, String cookie) {
        int index = 0;
        String[] duties = jiraIssue.getDutyPerson();
        System.out.println("Duties are: ");
        Arrays.stream(duties).forEach(System.out::println);
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
            jiraRestHelper.stopWatchingIssue(jiraSubTask, jiraIssue.getEngineer(), cookie);
            index = index < duties.length - 1 ? ++index : 0;
        }
    }
}
