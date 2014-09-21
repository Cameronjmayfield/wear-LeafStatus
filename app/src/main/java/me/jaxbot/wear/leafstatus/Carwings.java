package me.jaxbot.wear.leafstatus;

import android.content.SharedPreferences;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by jonathan on 9/21/14.
 */
public class Carwings {
    private String username;
    private String password;

    public int currentBattery;
    public String chargeTime;

    public Carwings(String username, String password)
    {
        this.username = username;
        this.password = password;
    }
    private CookieStore login() {
        DefaultHttpClient httpclient = new DefaultHttpClient();

        HttpPost httppost = new HttpPost("https://www.nissanusa.com/owners/j_spring_security_check");

        try {
            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("j_username", username));
            nameValuePairs.add(new BasicNameValuePair("j_passwordHolder", "Password"));
            nameValuePairs.add(new BasicNameValuePair("j_password", password));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(httppost);
            return httpclient.getCookieStore();
        } catch (ClientProtocolException e) {
            System.out.println(e.toString());
        } catch (IOException e) {
            System.out.println(e.toString());
        }

        return null;
    }

    private String getCarId(CookieStore jar) {
        // This is a particularly bad and non-future-safe operation,
        // but so is the entire application, since Nissan's API is internal
        String vehicleHTML = getHTTPString("https://www.nissanusa.com/owners/vehicles", jar);
        Pattern pattern = Pattern.compile("(.*)div class=\"vehicleHeader\" id=\"(\\d+)\"(.*)");
        Matcher m = pattern.matcher(vehicleHTML);
        if (m.matches()) {
            return m.group(2);
        } else {
            Log.e("Leaf", "Failed to find vehicle id");
            return "";
        }
    }

    public void update() {
        try {
            CookieStore jar = this.login();
            String carid = this.getCarId(jar);

            DefaultHttpClient httpclient = new DefaultHttpClient();
            HttpGet httpget = new HttpGet("https://www.nissanusa.com/owners/vehicles/statusRefresh?id=" + carid);
            httpclient.setCookieStore(jar);
            httpclient.execute(httpget);

            String result = getHTTPString("https://www.nissanusa.com/owners/vehicles/pollStatusRefresh?id=" + carid, jar);

            JSONObject jObject = new JSONObject(result);
            this.currentBattery = jObject.getInt("currentBattery");
            this.chargeTime = jObject.getString("chargeTime");
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private String getHTTPString(String url, CookieStore jar) {
        try {
            DefaultHttpClient httpclient = new DefaultHttpClient();
            HttpGet httpget = new HttpGet(url);

            httpclient.setCookieStore(jar);
            HttpResponse response = httpclient.execute(httpget);

            InputStream inputStream = response.getEntity().getContent();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            String result = "";
            while ((line = bufferedReader.readLine()) != null)
                result += line;

            inputStream.close();

            return result;
        } catch (Exception e) {
            System.out.println(e);
        }

        return "";
    }

}