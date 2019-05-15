package assignTasks.helper;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

public class GoogleSheetsHelper {
    @Autowired
    private Environment env;

    private final String pathP12File = env.getProperty("pathP12File");
    private final String sheetName = env.getProperty("sheetName");

    private static String RESPONSIBILITY = "Responsibility";
    private static String RANGE = "A1:B";

    private static JsonFactory getJsonFactory()
    {
        return JacksonFactory.getDefaultInstance();
    }

    private static HttpTransport getHttpTransport()
            throws GeneralSecurityException, IOException
    {
        return GoogleNetHttpTransport.newTrustedTransport();
    }
    public Credential getCredentials()
            throws GeneralSecurityException, IOException
    {
        File p12 = new File(pathP12File);
        System.out.println(p12.getAbsoluteFile());
        List<String> SCOPES_ARRAY =
                Arrays.asList(SheetsScopes.SPREADSHEETS_READONLY);

        Credential credential = new GoogleCredential.Builder()
                .setTransport(getHttpTransport())
                .setJsonFactory(getJsonFactory())
                .setServiceAccountId("googlesheets@modular-virtue-207013.iam.gserviceaccount.com")
                .setServiceAccountScopes(SCOPES_ARRAY)
                .setServiceAccountPrivateKeyFromP12File(p12)
                .build();

        return credential;
    }

    public List<List<Object>> getValues(String sheetName)
            throws GeneralSecurityException, IOException
    {
        Credential credential = getCredentials();
        Sheets sheets = new Sheets.Builder(getHttpTransport(),
                getJsonFactory(),
                credential)
                .setApplicationName("Google Sheets API Java Quickstart")
                .build();
        String range = RESPONSIBILITY + "!" + RANGE;

        ValueRange response = sheets.spreadsheets()
                .values()
                .get(sheetName, range)
                .execute();

        System.out.println(response.values().toString());
        return response.getValues();
    }

    public HashMap getCasesIds() throws GeneralSecurityException, IOException{
        HashMap<String,String> mapAssignedCases = new HashMap<>();
        for (List<Object> row : getValues(sheetName)) {
            System.out.printf("%s\t\t%s\n", row.get(0), row.get(1));
            mapAssignedCases.put(row.get(0).toString(), row.get(1).toString());
        }
        return mapAssignedCases;
    }

}
