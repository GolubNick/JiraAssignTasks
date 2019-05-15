package assignTasks.model;

import assignTasks.helper.GetProperties;

public class JiraIssue {
    private final String username = GetProperties.getInstance().getProperties("jirauser");
    private final String password = GetProperties.getInstance().getProperties("jirapass");
    private String issue;
    private String startNumberIssue;
    private String endNumberIssue;
    private String[] dutyPerson;
    private String numberReport;
    private String isNewJiraIssue;


    public JiraIssue(String issue, String startNumberIssue, String endNumberIssue, String dutyPerson[], String numberReport, String isNewJiraIssue) {

        this.issue = issue;
        this.startNumberIssue = startNumberIssue;
        this.endNumberIssue = endNumberIssue;
        this.dutyPerson = dutyPerson;
        this.numberReport = numberReport;
        this.isNewJiraIssue = isNewJiraIssue;
    }

    public String getStartNumberIssue() {
        return startNumberIssue;
    }

    public void setStartNumberIssue(String startNumberIssue) {
        this.startNumberIssue = startNumberIssue;
    }

    public String getEndNumberIssue() {
        return endNumberIssue;
    }

    public void setEndNumberIssue(String endNumberIssue) {
        this.endNumberIssue = endNumberIssue;
    }

    public String[] getDutyPerson() {
        return dutyPerson;
    }

    public void setDutyPerson(String[] dutyPerson) {
        this.dutyPerson = dutyPerson;
    }

    public String getUsername() {
        return username;
    }

    public String getIssue() {
        return issue;
    }

    public void setIssue(String issue) {
        this.issue = issue;
    }

    public String getPassword() {
        return password;
    }

    public String getNumberReport() {
        return numberReport;
    }

    public void setNumberReport(String numberReport) {
        this.numberReport = numberReport;
    }

    public String getIsNewJiraIssue() {
        return isNewJiraIssue;
    }

    public void setIsNewJiraIssue(String isNewJiraIssue) {
        this.isNewJiraIssue = isNewJiraIssue;
    }

}
