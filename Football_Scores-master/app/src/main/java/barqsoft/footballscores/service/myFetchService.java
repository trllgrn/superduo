package barqsoft.footballscores.service;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.TimeZone;
import java.util.Vector;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.Utilities;

/**
 * Created by yehya khaled on 3/2/2015.
 */
public class myFetchService extends IntentService
{
    public static final String LOG_TAG = "myFetchService";
    private static HashSet<Integer> teamsInDB = new HashSet<>();

    public myFetchService()
    {
        super("myFetchService");
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        fetchTeamIdsFromDB();

        getData("n2");
        getData("p2");

        return;
    }

    private void fetchTeamIdsFromDB() {
        ContentResolver resolver = getContentResolver();
        Uri teamsUri = DatabaseContract.TeamsEntry.CONTENT_URI;
        Cursor c = resolver.query(teamsUri, new String[]{DatabaseContract.TeamsEntry._ID}, null, null, null);
        if (c.moveToFirst()) {
            do {
                Integer teamId = c.getInt(0);
                if (teamId == null) {
                    Log.d(LOG_TAG, "fetchTeamIdsFromDB: Coudldn't fetch the column from query result.");
                }
                else {
                    //add the number to the list of team numbers
                    teamsInDB.add(teamId);
                }
            } while(c.moveToNext());
        } else {
            Log.d(LOG_TAG, "fetchTeamIdsFromDB: Teams query returned no data!");
        }
    }

    private void getData (String timeFrame)
    {
        //Creating fetch URL
        final String FIXTURES_URL = "http://api.football-data.org/v1/fixtures"; //Base URL
        final String TEAMS_URL = "http://api.football-data.org/v1/teams";
        final String QUERY_TIME_FRAME = "timeFrame"; //Time Frame parameter to determine days
        //final String QUERY_MATCH_DAY = "matchday";

        Uri fetch_build = Uri.parse(FIXTURES_URL).buildUpon().
                appendQueryParameter(QUERY_TIME_FRAME, timeFrame).build();
        JSONObject JSON_data = Utilities.callAPI(getApplicationContext(), fetch_build.toString());
        try {
            if (JSON_data != null) {
                //This bit is to check if the data contains any matches. If not, we call processJson on the dummy data
                JSONArray matches = JSON_data.getJSONArray("fixtures");

                if (matches.length() == 0) {
                    //if there is no data, call the function on dummy data
                    //this is expected behavior during the off season.
                    Log.i(LOG_TAG, "getData: No Data available from Server");
                    return;
                } else {
                    processJSONdata(JSON_data, getApplicationContext(), true);
                }

            } else {
                //Could not Connect
                Log.d(LOG_TAG, "Could not connect to server.");
            }
        }
        catch(Exception e)
        {
            Log.e(LOG_TAG,e.getMessage());
        }
    }

    private void processTeamData (JSONObject theData, String teamNumber) {
        final String TEAM_NAME_TAG = "name";
        final String TEAM_CODE_TAG = "code";
        final String TEAM_SHORT_NAME_TAG = "shortName";
        final String TEAM_CREST_URL_TAG = "crestUrl";

        String fetchedName = null;
        String fetchedCode = null;
        String fetchedShortName = null;
        String fetchedCrestUrl = null;

        try {
            fetchedName = theData.getString(TEAM_NAME_TAG);
            fetchedCode = theData.getString(TEAM_CODE_TAG);
            fetchedShortName = theData.getString(TEAM_SHORT_NAME_TAG);
            fetchedCrestUrl = theData.getString(TEAM_CREST_URL_TAG);

            //Get a writable database
            ContentResolver resolver = getContentResolver();

            //Collect Content Values to insert into the database
            ContentValues teamValues = new ContentValues();

            teamValues.put(DatabaseContract.TeamsEntry._ID, teamNumber);
            teamValues.put(DatabaseContract.TeamsEntry.COL_NAME,fetchedName);
            teamValues.put(DatabaseContract.TeamsEntry.COL_CODE,fetchedCode);
            teamValues.put(DatabaseContract.TeamsEntry.COL_SHORT_NAME, fetchedShortName);
            //Convert SVG URL to point to PNG version

            teamValues.put(DatabaseContract.TeamsEntry.COL_CREST_URL,fetchedCrestUrl);

            resolver.insert(DatabaseContract.TeamsEntry.CONTENT_URI,teamValues);

        }catch (JSONException e)
        {
            Log.e(LOG_TAG,e.getMessage());
        }
    }

    private void processJSONdata (JSONObject JSONdata,Context mContext, boolean isReal)
    {
        //JSON data
        // This set of league codes is for the 2015/2016 season. In fall of 2016, they will need to
        // be updated. Feel free to use the codes
        final String BUNDESLIGA1 = "394";
        final String BUNDESLIGA2 = "395";
        final String LIGUE1 = "396";
        final String LIGUE2 = "397";
        final String PREMIER_LEAGUE = "398";
        final String PRIMERA_DIVISION = "399";
        final String SEGUNDA_DIVISION = "400";
        final String SERIE_A = "401";
        final String PRIMERA_LIGA = "402";
        final String BUNDESLIGA3 = "403";
        final String EREDIVISIE = "404";

        HashSet<String> mMyLeagues = new HashSet<>();
        mMyLeagues.add(BUNDESLIGA1);
        mMyLeagues.add(BUNDESLIGA2);
        mMyLeagues.add(LIGUE1);
        mMyLeagues.add(LIGUE2);
        mMyLeagues.add(PREMIER_LEAGUE);
        mMyLeagues.add(PRIMERA_DIVISION);
        mMyLeagues.add(SEGUNDA_DIVISION);
        mMyLeagues.add(SERIE_A);
        mMyLeagues.add(PRIMERA_LIGA);
        mMyLeagues.add(BUNDESLIGA3);
        mMyLeagues.add(EREDIVISIE);

        final String SEASON_LINK = "http://api.football-data.org/v1/soccerseasons/";
        final String MATCH_LINK = "http://api.football-data.org/v1/fixtures/";
        final String TEAM_LINK = "http://api.football-data.org/v1/teams/";
        final String FIXTURES = "fixtures";
        final String LINKS = "_links";
        final String SOCCER_SEASON = "soccerseason";
        final String SELF = "self";
        final String MATCH_DATE = "date";
        final String HOME_TEAM = "homeTeamName";
        final String AWAY_TEAM = "awayTeamName";
        final String RESULT = "result";
        final String HOME_GOALS = "goalsHomeTeam";
        final String AWAY_GOALS = "goalsAwayTeam";
        final String MATCH_DAY = "matchday";
        final String HREF = "href";

        //Match data
        String League = null;
        String mDate = null;
        String mTime = null;
        String Home = null;
        String Away = null;
        String Home_goals = null;
        String Away_goals = null;
        String match_id = null;
        String match_day = null;
        String homeTeamUrl = null;
        String awayTeamUrl = null;




        try {
            JSONArray matches = JSONdata.getJSONArray(FIXTURES);

            //ContentValues to be inserted
            Vector<ContentValues> values = new Vector <ContentValues> (matches.length());
            for(int i = 0; i < matches.length(); i++)
            {

                JSONObject match_data = matches.getJSONObject(i);
                League = match_data.getJSONObject(LINKS).getJSONObject(SOCCER_SEASON).
                        getString(HREF);
                League = League.replace(SEASON_LINK, "");
                //This if statement controls which leagues we're interested in the data from.
                //add leagues here in order to have them be added to the DB.
                // If you are finding no data in the app, check that this contains all the leagues.
                // If it doesn't, that can cause an empty DB, bypassing the dummy data routine.
                if(mMyLeagues.contains(League))
                {
                    //Get the team URLs to fetch make team data call.
                    homeTeamUrl = match_data.getJSONObject(LINKS).getJSONObject("homeTeam").getString(HREF);
                    awayTeamUrl = match_data.getJSONObject(LINKS).getJSONObject("awayTeam").getString(HREF);

                    if (homeTeamUrl != null) {
                        String homeTeamNumber = homeTeamUrl.replace(TEAM_LINK, "");
                        //Is this team in the database?
                        if (!teamsInDB.contains(Integer.parseInt(homeTeamNumber))) {
                            JSONObject homeTeamResults = Utilities.callAPI(mContext, homeTeamUrl);
                            processTeamData(homeTeamResults, homeTeamNumber);
                        }
                    }

                    if (awayTeamUrl != null) {
                        //Is this team in the database?
                        String awayTeamNumber = awayTeamUrl.replace(TEAM_LINK, "");
                        if (!teamsInDB.contains(Integer.parseInt(awayTeamNumber))) {
                            JSONObject awayTeamResults = Utilities.callAPI(mContext, awayTeamUrl);
                            processTeamData(awayTeamResults, awayTeamNumber);
                        }
                    }

                    match_id = match_data.getJSONObject(LINKS).getJSONObject(SELF).
                            getString(HREF);
                    match_id = match_id.replace(MATCH_LINK, "");
                    if(!isReal){
                        //This if statement changes the match ID of the dummy data so that it all goes into the database
                        match_id=match_id+Integer.toString(i);
                    }

                    mDate = match_data.getString(MATCH_DATE);
                    mTime = mDate.substring(mDate.indexOf("T") + 1, mDate.indexOf("Z"));
                    mDate = mDate.substring(0,mDate.indexOf("T"));
                    SimpleDateFormat match_date = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss");
                    match_date.setTimeZone(TimeZone.getTimeZone("UTC"));
                    try {
                        Date parseddate = match_date.parse(mDate+mTime);
                        SimpleDateFormat new_date = new SimpleDateFormat("yyyy-MM-dd:HH:mm");
                        new_date.setTimeZone(TimeZone.getDefault());
                        mDate = new_date.format(parseddate);
                        mTime = mDate.substring(mDate.indexOf(":") + 1);
                        mDate = mDate.substring(0,mDate.indexOf(":"));

                        if(!isReal){
                            //This if statement changes the dummy data's date to match our current date range.
                            Date fragmentdate = new Date(System.currentTimeMillis()+((i-2)*86400000));
                            SimpleDateFormat mformat = new SimpleDateFormat("yyyy-MM-dd");
                            mDate=mformat.format(fragmentdate);
                        }
                    }
                    catch (Exception e)
                    {
                        Log.d(LOG_TAG, "error here!");
                        Log.e(LOG_TAG,e.getMessage());
                    }
                    //Home = match_data.getString(HOME_TEAM);
                    //Away = match_data.getString(AWAY_TEAM);
                    int homeTeamNum = Integer.parseInt(homeTeamUrl.replace(TEAM_LINK, ""));
                    int awayTeamNum = Integer.parseInt(awayTeamUrl.replace(TEAM_LINK,""));
                    Home_goals = match_data.getJSONObject(RESULT).getString(HOME_GOALS);
                    Away_goals = match_data.getJSONObject(RESULT).getString(AWAY_GOALS);
                    match_day = match_data.getString(MATCH_DAY);
                    ContentValues match_values = new ContentValues();
                    match_values.put(DatabaseContract.ScoresEntry.MATCH_ID,match_id);
                    match_values.put(DatabaseContract.ScoresEntry.DATE_COL,mDate);
                    match_values.put(DatabaseContract.ScoresEntry.TIME_COL,mTime);
                    match_values.put(DatabaseContract.ScoresEntry.HOME_COL,homeTeamNum);
                    match_values.put(DatabaseContract.ScoresEntry.AWAY_COL,awayTeamNum);
                    match_values.put(DatabaseContract.ScoresEntry.HOME_GOALS_COL,Home_goals);
                    match_values.put(DatabaseContract.ScoresEntry.AWAY_GOALS_COL,Away_goals);
                    match_values.put(DatabaseContract.ScoresEntry.LEAGUE_COL,League);
                    match_values.put(DatabaseContract.ScoresEntry.MATCH_DAY,match_day);
                    //log spam

                    //Log.v(LOG_TAG,match_id);
                    //Log.v(LOG_TAG,mDate);
                    //Log.v(LOG_TAG,mTime);
                    //Log.v(LOG_TAG,Home);
                    //Log.v(LOG_TAG,Away);
                    //Log.v(LOG_TAG,Home_goals);
                    //Log.v(LOG_TAG,Away_goals);

                    values.add(match_values);
                }
            }
            int inserted_data = 0;
            ContentValues[] insert_data = new ContentValues[values.size()];
            values.toArray(insert_data);
            inserted_data = mContext.getContentResolver().bulkInsert(
                    DatabaseContract.BASE_CONTENT_URI,insert_data);

        }
        catch (JSONException e)
        {
            Log.e(LOG_TAG,e.getMessage());
        }

    }
}

