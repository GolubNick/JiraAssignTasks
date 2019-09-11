package assignTasks.model;

import assignTasks.helper.GetProperties;
import lombok.Getter;
import lombok.Setter;

public class JiraIssue {
    @Getter
    private final String username = GetProperties.getInstance().getProperties("jirauser");
    @Getter
    private final String password = GetProperties.getInstance().getProperties("jirapass");
    @Getter
    @Setter
    private String issue;
    @Getter
    @Setter
    private String startNumberIssue;
    @Getter
    @Setter
    private String endNumberIssue;
    @Getter
    @Setter
    private String[] dutyPerson;
    @Getter
    @Setter
    private String engineer;
    @Getter
    @Setter
    private String numberReport;
    @Getter
    @Setter
    private String isNewJiraIssue;


    public JiraIssue(String issue, String startNumberIssue, String endNumberIssue, String dutyPerson[], String numberReport, String isNewJiraIssue) {
        this.issue = issue;
        this.startNumberIssue = startNumberIssue;
        this.endNumberIssue = endNumberIssue;
        this.dutyPerson = dutyPerson;
        this.numberReport = numberReport;
        this.isNewJiraIssue = isNewJiraIssue;
    }
}
