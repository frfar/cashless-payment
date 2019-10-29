package web;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
public class WebRequest {
    /**
     * Timeout for the web request. Timeout is measured in milliseconds.
     * It will throw an exception if it didn't get respond during this time limit.
     */
    private static final int TIME_OUT = 5000;

    /**
     * This function will send post request to the specific url and specific parameters.
     *
     * @param url it represent the url of webserver you are calling.
     * @param parameters QueryParameter object
     * @return response from the server. It can be empty string. For 404 error it returns null.
     */
    public static String sendPost(String url, QueryParameter parameters)  {
        try {
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Accept-Language","en-US,en;q=0.5");

            con.setConnectTimeout(TIME_OUT);
            con.setReadTimeout(TIME_OUT);

            con.setDoOutput(true);

            DataOutputStream wr = new DataOutputStream(con.getOutputStream());

            wr.writeBytes(parameters.getQueryString());
            wr.flush();
            wr.close();

            int responseCode = con.getResponseCode();

            if(responseCode == 404)
                return null;

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return response.toString();
        }
        catch(Exception ex) {
            return null;
        }
    }

    /**
     * This function will send post request to the specific url. Include all the query parameters inside url only.
     *
     * @param url it represent the url of webserver you are calling.
     * @param queryParameter QueryParameter object
     * @return response from the server. It can be empty string. For 404 error it returns null.
     */

    public static String sendGet(String url,QueryParameter queryParameter)  {
        try {
            URL obj = new URL(url + queryParameter.getQueryString());
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            con.setRequestMethod("GET");
            con.setRequestProperty("Accept-Language","en-US,en;q=0.5");

            con.setConnectTimeout(TIME_OUT);
            con.setReadTimeout(TIME_OUT);

            int responseCode = con.getResponseCode();

            if(responseCode == 404)
                return null;

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            return response.toString();
        }
        catch(Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
