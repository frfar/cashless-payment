package web;

import com.google.gson.Gson;
import config.Config;
import transaction.AuthUser;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AuthenticationService {

    public static final String EMAIL = "email";
    public static final String PASSWORD = "password";

    private static final String EMAIL_REGEX = "^[\\w-\\+]+(\\.[\\w]+)*@[\\w-]+(\\.[\\w]+)*(\\.[a-z]{2,})$";
    private static Pattern pattern = Pattern.compile(EMAIL_REGEX, Pattern.CASE_INSENSITIVE);

    private static AuthUser authUser;
    private static String userToken;
    private static boolean isLoggedIn;

    public static AuthUser getAuthUser() {
        return authUser;
    }

    public static String getUserToken() {
        return userToken;
    }

    public static boolean isLoggedIn() {
        return isLoggedIn;
    }

    public static String userLogin(String email, String password) {
        String postData = getPostData(email, password);
        if (postData == null) {
            return null;
        }
        QueryParameter query = new QueryParameter();
        query.addParameter("email", email);
        query.addParameter("password", password);

        String baseURL = "";
        try {
            baseURL = Config.getInstance().getServer();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        URL obj;
        try {
            HttpURLConnection con = postRequest(baseURL + "/login", query);
            int responseCode = con.getResponseCode();
            if (responseCode == 200) {
                return postLogin(con);
            }

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return null;
    }

    public static boolean isValidEmail(String email) {
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private static String getPostData(String email, String password) {
        if (!isValidEmail(email)) {
            return null;
        }
        Map<String, String> data = new HashMap<>();
        data.put(EMAIL, email);
        data.put(PASSWORD, password);
        return new Gson().toJson(data);
    }

    private static String postLogin(HttpURLConnection con) throws IOException {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder res = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                res.append(responseLine.trim());
            }
            AuthUser user = new Gson().fromJson(res.toString(), AuthUser.class);
            Map<String, List<String>> map = con.getHeaderFields();
            List<String> userToken = map.getOrDefault("Auth-Token", null);
            if (userToken == null) {
                return null;
            } else {
                AuthenticationService.authUser = user;
                AuthenticationService.userToken = userToken.get(0);
                AuthenticationService.isLoggedIn = true;
                return userToken.get(0);
            }
        }
    }

    private static HttpURLConnection postRequest(String baseURL, String postData) throws IOException {
        URL obj;
        obj = new URL(baseURL);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json; utf-8");
        con.setRequestProperty("Accept", "application/json");
        con.setConnectTimeout(5000);
        con.setReadTimeout(5000);
        con.setDoOutput(true);

        con.setRequestProperty("Content-Length", "" + Integer.toString(postData.getBytes().length));
        con.setRequestProperty("Content-Language", "en-US");

        con.setUseCaches(false);
        con.setDoInput(true);
        con.setDoOutput(true);


        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(postData);
        wr.flush();
        wr.close();
        return con;
    }

    private static HttpURLConnection postRequest(String baseURL, QueryParameter parameters) throws IOException {
        URL obj = new URL(baseURL);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Accept-Language","en-US,en;q=0.5");

        con.setConnectTimeout(5000);
        con.setReadTimeout(5000);

        con.setDoOutput(true);

        DataOutputStream wr = new DataOutputStream(con.getOutputStream());

        wr.writeBytes(parameters.getQueryString(QueryParameter.CallType.POST));
        wr.flush();
        wr.close();
        return con;
    }
}
