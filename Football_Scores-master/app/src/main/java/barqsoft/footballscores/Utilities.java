package barqsoft.footballscores;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.SystemClock;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;

/**
 * Created by yehya khaled on 3/3/2015.
 */
public class Utilities
{
    public static final int SERIE_A = 357;
    public static final int PREMIER_LEGAUE = 354;
    public static final int CHAMPIONS_LEAGUE = 362;
    public static final int PRIMERA_DIVISION = 358;
    public static final int BUNDESLIGA = 351;

    private static int apiCallCount = 0;


    private static final String TAG = "Utilities";

    public static String getLeague(int league_num)
    {
        switch (league_num)
        {
            case SERIE_A : return "Seria A";
            case PREMIER_LEGAUE : return "Premier League";
            case CHAMPIONS_LEAGUE : return "UEFA Champions League";
            case PRIMERA_DIVISION : return "Primera Division";
            case BUNDESLIGA : return "Bundesliga";
            default: return "Not known League Please report";
        }
    }
    public static String getMatchDay(int match_day,int league_num)
    {
        if(league_num == CHAMPIONS_LEAGUE)
        {
            if (match_day <= 6)
            {
                return "Group Stages, Matchday : 6";
            }
            else if(match_day == 7 || match_day == 8)
            {
                return "First Knockout round";
            }
            else if(match_day == 9 || match_day == 10)
            {
                return "QuarterFinal";
            }
            else if(match_day == 11 || match_day == 12)
            {
                return "SemiFinal";
            }
            else
            {
                return "Final";
            }
        }
        else
        {
            return "Matchday : " + String.valueOf(match_day);
        }
    }

    public static String getScores(int home_goals,int awaygoals)
    {
        if(home_goals < 0 || awaygoals < 0)
        {
            return " - ";
        }
        else
        {
            return String.valueOf(home_goals) + " - " + String.valueOf(awaygoals);
        }
    }

    public static int getTeamCrestByTeamName (String teamname)
    {
        if (teamname==null){return R.drawable.no_icon;}
        switch (teamname)
        { //This is the set of icons that are currently in the app. Feel free to find and add more
            //as you go.
            case "Arsenal London FC" : return R.drawable.arsenal;
            case "Manchester United FC" : return R.drawable.manchester_united;
            case "Swansea City" : return R.drawable.swansea_city_afc;
            case "Leicester City" : return R.drawable.leicester_city_fc_hd_logo;
            case "Everton FC" : return R.drawable.everton_fc_logo1;
            case "West Ham United FC" : return R.drawable.west_ham;
            case "Tottenham Hotspur FC" : return R.drawable.tottenham_hotspur;
            case "West Bromwich Albion" : return R.drawable.west_bromwich_albion_hd_logo;
            case "Sunderland AFC" : return R.drawable.sunderland;
            case "Stoke City FC" : return R.drawable.stoke_city;
            default: return R.drawable.no_icon;
        }
    }

    public static String getTodaysDate() {
        Date today = new Date(System.currentTimeMillis());
        SimpleDateFormat mformat = new SimpleDateFormat("yyyy-MM-dd");
        return mformat.format(today);
    }

    public static String getTheTime() {
        Date now = new Date(System.currentTimeMillis());
        SimpleDateFormat tformat = new SimpleDateFormat("HH:mm");
        return tformat.format(now);
    }

    public static String getFormattedDay(String inputDate){
        if (inputDate != null){
            String[] parsedDate = inputDate.split("-");
            GregorianCalendar gCal = new GregorianCalendar(Integer.parseInt(parsedDate[0]),
                                                            Integer.parseInt(parsedDate[1])-1,
                                                            Integer.parseInt(parsedDate[2]));
            //Date theDate = gCal.getTime();
            //Construct the formatted Day
            //Formatted String
            SimpleDateFormat dformat = new SimpleDateFormat("EE, MMM d");

            return dformat.format(gCal.getTime());
        } else {
            return null;
        }
    }

    public static String getFormattedTime(String inputTime) {
        if (inputTime != null) {
            int inputHour = Integer.parseInt(inputTime.substring(0, 2));
            if (inputHour >= 12) {
                return inputTime + "PM";
            } else {
                return inputTime + "AM";
            }
        }
        else {
            return null;
        }
    }

    public static Bitmap loadBitmap(String src) {
        Bitmap crest = null;
        try {
            URL aURL = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) aURL.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream in = connection.getInputStream();
            crest = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.e(TAG, "loadBitmap: " + e.getMessage());
            e.printStackTrace();
        }
        return crest;
    }

    public static JSONObject callAPI(Context context, String queryString) {

        HttpURLConnection m_connection = null;
        BufferedReader reader = null;
        String JSON_data = null;
        JSONObject results = null;

        if (apiCallCount == 49) {
            //Sleep for a minute
            Log.d(TAG, "callAPI: Reached System limit. Waiting a minute...");
            SystemClock.sleep(60000);
            //reset the counter
            apiCallCount = 0;
        } else {
            Log.d(TAG, "callAPI: API Call Count: " + apiCallCount);
            apiCallCount++;
        }

        //Opening Connection
        try {
            URL fetch = new URL(queryString);
            Log.d(TAG, "callAPI: Set URL: " + fetch.toString());
            m_connection = (HttpURLConnection) fetch.openConnection();
            m_connection.setRequestMethod("GET");
            Log.d(TAG, "callAPI: value of api_key: " + context.getString(R.string.api_key));
            m_connection.addRequestProperty("X-Auth-Token", context.getString(R.string.api_key));
            Map props = m_connection.getRequestProperties();
            Log.d(TAG, "callAPI: request props: " + m_connection.getRequestProperty("X-Auth-Token"));
            Log.d(TAG, "callAPI: the request: " + m_connection.toString());
            m_connection.connect();
            Log.d(TAG, "callAPI: Connection made!");

            // Read the input stream into a String
            InputStream inputStream = m_connection.getInputStream();
            Log.d(TAG, "callAPI: got InputStream!");
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }
            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            JSON_data = buffer.toString();
            //Output the retrieved data from the web service
            Log.d(TAG, "JSON Data from Football API: " + JSON_data);
            if (JSON_data != null) {
                results = new JSONObject(JSON_data);
            }
        }
        catch (Exception e)
        {
            Log.e(TAG,"Exception here, message: " + e.toString());
            e.printStackTrace();
        }
        finally {
            if(m_connection != null)
            {
                m_connection.disconnect();
            }
            if (reader != null)
            {
                try {
                    reader.close();
                }
                catch (IOException e)
                {
                    Log.e(TAG,"Error Closing Stream");
                }
            }
        }
        return results;
    }

    public static String getPNGUrl(String svgURL) {
        //Strip the protocol
        //Should be http://
        StringBuilder pngURL = new StringBuilder();
        final String THUMB = "thumb";
        final String PNG_PREFIX = "200px-";
        final String PNG_SUFFIX = ".png";

        String svgStripped = null;
        if (svgURL.endsWith(".svg")) {
            if (svgURL.startsWith("http://")) {
                svgStripped = new StringBuilder(svgURL).delete(0, 7).toString();
            } else if (svgURL.startsWith("https://")) {
                svgStripped = new StringBuilder(svgURL).delete(0,8).toString();
            }
            String[] tokens = svgStripped.split("/");
            String auth = tokens[0]; //upload.wikimedia.org
            String wiki_path = tokens[1]; //wikipedia
            String language_path = tokens[2]; //lang
            String dir_path1 = tokens[3]; //dir1
            String dir_path2 = tokens[4]; //dir2
            String file_name = tokens[5]; //*.svg

            //Build the new PNG URL
            pngURL.append("https://").append(auth + "/");
            pngURL.append(wiki_path + "/");
            pngURL.append(language_path + "/");
            //Insert thumbnail dir
            pngURL.append(THUMB + "/");
            pngURL.append(dir_path1 + "/");
            pngURL.append(dir_path2 + "/");
            pngURL.append(file_name + "/");
            pngURL.append(PNG_PREFIX).append(file_name).append(PNG_SUFFIX);

        } else {
            Log.d(TAG, "getPNGUrl: svg String is not a SVG: " + svgURL);
            return changeProtocol(svgURL);
        }

        Log.d(TAG, "getPNGUrl: Converted PNG URL: " + pngURL.toString());

        return pngURL.toString();
    }

    public static String changeProtocol(String url) {
        final String HTTP = "http://";
        final String HTTPS = "https://";

        if (url.startsWith(HTTP)) {
            StringBuilder builder = new StringBuilder(url);
            builder.delete(0,7);
            builder.insert(0,HTTPS);
            return builder.toString();
        } else {
            return url;
        }
    }
}
