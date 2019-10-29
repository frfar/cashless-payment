package web;

import java.net.URLEncoder;
import java.util.HashMap;

public class QueryParameter {

    private HashMap<String,String> queryParameters;

    private StringBuilder serializeParameter;

    public QueryParameter(){
        queryParameters = new HashMap<String,String>();
        serializeParameter = new StringBuilder();
    }

    public void addParameter(String key,String value){
        queryParameters.put(key,value);

        try {
            if(!serializeParameter.toString().isEmpty()){
                serializeParameter.append("&");
            } else {
                serializeParameter.append("?");
            }
            serializeParameter.append(URLEncoder.encode(key, "UTF-8"));
            serializeParameter.append("=");
            serializeParameter.append(URLEncoder.encode(value,"UTF-8"));
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    String getQueryString(){
        return serializeParameter.toString();
    }

}