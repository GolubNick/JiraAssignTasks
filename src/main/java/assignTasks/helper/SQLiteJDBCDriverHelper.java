package assignTasks.helper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

public class SQLiteJDBCDriverHelper {

    private final String SQLITE_URL = GetProperties.getInstance().getProperties("SQLITE_URL");

    private Connection connect() {
        String url = SQLITE_URL;
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    public void insert(String person, String id_test, String id_subtask, String id_task, String error_message) {
        String sql = "INSERT INTO reports (person,id_test,id_subtask,id_task,error_message,date_added) VALUES(?,?,?,?,?,datetime('now'))";

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, person);
            pstmt.setString(2, id_test);
            pstmt.setString(3, id_subtask);
            pstmt.setString(4, id_task);
            pstmt.setString(5, error_message);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println(person + " " + id_test + " " + id_subtask + " " + id_task + " " + error_message + " successfully insert to sqlite");
    }

    public String getPersonByTestId(String testId, String taskId){
        String sql = "select person from reports where id_test = '" + testId + "' and id_task = '" + taskId + "'";
        String person = "";
        try (Connection conn = this.connect();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)){
            while (rs.next()) {
                person = rs.getString("person");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return person;
    }

    public String getFullName(String name){
        String sql = "SELECT full_name FROM engineers where name ='" + name + "'";
        String person = "";
        try (Connection conn = this.connect();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)){
            while (rs.next()) {
                person = rs.getString("full_name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return person;
    }

    public boolean isJuniorName(String name){
        String sql = "SELECT * FROM engineers where full_name ='" + name + "' and position = 'junior'";
        boolean isJuniorExist = false;
        try (Connection conn = this.connect();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)){
            if (rs.next()) {
                isJuniorExist = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return isJuniorExist;
    }

    public String getJuniorFullName(String name){
        String sql = "SELECT full_name FROM engineers where name ='" + name + "' and position = 'junior'";
        String person = "";
        try (Connection conn = this.connect();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)){
            while (rs.next()) {
                person = rs.getString("full_name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return person;
    }

    public String getEngineerPosition(String fullName){
        String sql = "SELECT position FROM engineers where full_name = '" + fullName + "'";
        String position = "";
        try (Connection conn = this.connect();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)){
            while (rs.next()) {
                position = rs.getString("position");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return position;
    }

    public String getFullNameByPCName(String pcname){
        String sql = "SELECT full_name FROM engineers where pcname = '" + pcname + "'";
        String full_name = "";
        try (Connection conn = this.connect();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)){
            while (rs.next()) {
                full_name = rs.getString("full_name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return full_name;
    }

    public ArrayList<String> getAllEngineers(){
        String sql = "SELECT name FROM engineers";
        ArrayList<String> engineers = new ArrayList<>();
        try (Connection conn = this.connect();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)){
            while (rs.next()) {
                engineers.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return engineers;
    }

    public  void addNewEngineer(String name, String fullName, String position, String birthday, String pcname) {
        String sql = "INSERT INTO engineers (name,full_name,position,birthday,pcname) VALUES (?,?,?,?,?)";

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, fullName);
            pstmt.setString(3, position);
            pstmt.setString(4, birthday);
            pstmt.setString(5, pcname);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public  void removeEngineerByFullName(String fullName) {
        String sql = "DELETE FROM engineers WHERE full_name = ?";

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, fullName);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public  void changeEngineerPositionByFullName(String fullName, String position) {
        String sql = "UPDATE engineers set position = ? WHERE full_name = ?";

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, position);
            pstmt.setString(2, fullName);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
