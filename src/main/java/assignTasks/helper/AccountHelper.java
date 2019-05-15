package assignTasks.helper;

public class AccountHelper {

    private static AccountHelper instance;
    private SQLiteJDBCDriverHelper sqliteHelper;

    public static AccountHelper getInstance(){
        if (instance == null)
            instance = new AccountHelper().init();
        return instance;
    }

    protected AccountHelper init(){
        sqliteHelper = new SQLiteJDBCDriverHelper();
        return this;
    }

    public boolean isJuniorName(String name){
        return sqliteHelper.isJuniorName(name);
    }

    public boolean isJuniorKey(String key){
        return !sqliteHelper.getJuniorFullName(key).isEmpty();
    }

    public String getFullName(String lastName){
        return sqliteHelper.getFullName(lastName);
    }

    public String getJuniorFullName(String lastName){
        return sqliteHelper.getJuniorFullName(lastName);
    }
}