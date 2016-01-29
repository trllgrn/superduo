package barqsoft.footballscores;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

import com.squareup.picasso.Picasso;

/**
 * Implementation of App Widget functionality.
 */
public class FutbolWidget extends AppWidgetProvider {

    private static final String TAG = "FutbolWidget";
    public static final String COL_HOME = "home"; //home team number
    public static final String COL_AWAY = "away"; //away team number
    public static final String COL_DATE = "date"; //match date
    public static final String COL_MATCHTIME = "time"; //match time
    public static final String COL_HOME_NAME = "homeTeamName";
    public static final String COL_HOME_CODE = "homeTeamCode";
    public static final String COL_HOME_SHORT = "homeTeamShort";
    public static final String COL_HOME_CREST_URL = "homeTeamCrest";
    public static final String COL_AWAY_NAME = "awayTeamName";
    public static final String COL_AWAY_CODE = "awayTeamCode";
    public static final String COL_AWAY_SHORT = "awayTeamShort";
    public static final String COL_AWAY_CREST_URL = "awayTeamCrest";
    public double detail_match_id = 0;

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId, int[] appWidgetIds) {
        //Create an Intent to launch the Football Scores app when the widget is tapped
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,0,intent,0);

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.futbol_widget);
        views.setOnClickPendingIntent(R.id.wdgt_layout_wrapper,pendingIntent);

        //Get the closest match to the current time from the database
        //Query the database for matches with Date >= to today and time >= right now
        String today = Utilities.getTodaysDate();

        String currentTime = Utilities.getTheTime();

        //Launch query in background thread

        //Get at DBHelper instance
        ScoresDBHelper dbHelper = new ScoresDBHelper(context);
        //Get access to the app's content resolver
        ContentResolver resolver = context.getContentResolver();

        //Build the Uri
        Uri allScoresUri = DatabaseContract.BASE_CONTENT_URI;

        String[] requestedColumns = new String[] {
                COL_DATE,
                COL_MATCHTIME,
                COL_HOME,
                COL_AWAY,
                "h.name as " + COL_HOME_NAME,
                "h.code as " + COL_HOME_CODE,
                "h.shortName as " + COL_HOME_SHORT,
                "h.crestUrl as " + COL_HOME_CREST_URL,
                "a.name as " + COL_AWAY_NAME,
                "a.code as " + COL_AWAY_CODE,
                "a.shortName as " + COL_AWAY_SHORT,
                "a.crestUrl as " + COL_AWAY_CREST_URL
        };

        //Specify date column + a parameter
        String DATE_REQUEST = COL_DATE + ">=?";
        //Merge date selection with today arg
        Cursor data = resolver.query(allScoresUri,requestedColumns,DATE_REQUEST,new String[]{today},null);

        String[] colNames = data.getColumnNames();



        //Get the first record
        if (data == null || !data.moveToFirst()) {
            Log.d(TAG, "updateAppWidget: Scores Cursor is empty");
        } else {
            int count = data.getCount();

            //Get the row with a game nearest to now.
            //Find out the date of the first row.
            do {
                String fetchedDate = data.getString(data.getColumnIndex(COL_DATE));
                //if the Date of the first row is not today, then the nearest game is at least tomorrow
                //we can extract and use this data for our widget
                if (fetchedDate.equals(today)) {
                    String fetchedTime = data.getString(data.getColumnIndex(COL_MATCHTIME));
                    int fetchedTimeHour = Integer.parseInt(fetchedTime.substring(0,2));
                    int currentTimeHour = Integer.parseInt(currentTime.substring(0,2));
                    if (fetchedTimeHour >= currentTimeHour) {
                        break;
                    }
                } else {
                    break;
                }
            } while(data.moveToNext());


            //This record should be the next match; Collect the data
            views.setTextViewText(R.id.wdgt_date_textview, Utilities.getFormattedDay(data.getString(data.getColumnIndex(COL_DATE))));

            views.setTextViewText(R.id.wdgt_time_textview, Utilities.getFormattedTime(data.getString(data.getColumnIndex(COL_MATCHTIME))));

            //If both teams have a code, use it.  Otherwise, go with the short name
            String homeNameToUse = null;
            String awayNameToUse = null;

            homeNameToUse = data.getString(data.getColumnIndex(COL_HOME_CODE));
            awayNameToUse = data.getString(data.getColumnIndex(COL_AWAY_CODE));

            if (homeNameToUse.equals("null") || awayNameToUse.equals("null")){
                homeNameToUse = data.getString(data.getColumnIndex(COL_HOME_SHORT));
                awayNameToUse = data.getString(data.getColumnIndex(COL_AWAY_SHORT));
            }

            views.setTextViewText(R.id.wdgt_home_name, homeNameToUse);

            views.setTextViewText(R.id.wdgt_away_name, awayNameToUse);

            String homeCrestURL = data.getString(data.getColumnIndex(COL_HOME_CREST_URL));

            String awayCrestURL = data.getString(data.getColumnIndex(COL_AWAY_CREST_URL));


            //Test loading the ImageViews
            //views.setImageViewBitmap(R.id.wdgt_home_crest, Utilities.loadBitmap(crestURL));
            //DownloadBitmapTask getBitmap = new DownloadBitmapTask(views,R.id.wdgt_home_crest);
            //getBitmap.execute(crestURL);
            Picasso.Builder builder = new Picasso.Builder(context);
            builder.listener(new Picasso.Listener() {
                @Override
                public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                    Log.d(TAG, "onImageLoadFailed: Tried to fetch URL: " + uri.toString());
                    exception.printStackTrace();
                }
            });

            builder.build()
                    .load(Utilities.getPNGUrl(homeCrestURL))
                    .error(R.drawable.ic_launcher)
                    .into(views, R.id.wdgt_home_crest, appWidgetIds);

            //views.setImageViewBitmap(R.id.wdgt_away_crest, Utilities.loadBitmap(crestURL));
            //DownloadBitmapTask getAwayBitmap = new DownloadBitmapTask(views,R.id.wdgt_away_crest);
            //getAwayBitmap.execute(crestURL);
            Picasso.with(context)
                    .load(Utilities.getPNGUrl(awayCrestURL))
                    .error(R.drawable.ic_launcher)
                    .into(views, R.id.wdgt_away_crest, appWidgetIds);

            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views);

            data.close();
        }

    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, appWidgetIds);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.

    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created

    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

