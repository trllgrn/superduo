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

/**
 * Implementation of App Widget functionality.
 */
public class FutbolWidget extends AppWidgetProvider {

    private static final String TAG = "FutbolWidget";
    public static final int COL_HOME = 3;
    public static final int COL_AWAY = 4;
    public static final int COL_HOME_GOALS = 6;
    public static final int COL_AWAY_GOALS = 7;
    public static final int COL_DATE = 1;
    public static final int COL_LEAGUE = 5;
    public static final int COL_MATCHDAY = 9;
    public static final int COL_ID = 8;
    public static final int COL_MATCHTIME = 2;
    public double detail_match_id = 0;

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        //Create an Intent to launch the Football Scores app when the widget is tapped
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,0,intent,0);

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.futbol_widget);
        views.setOnClickPendingIntent(R.id.wdgt_layout_wrapper,pendingIntent);

        //Get the closest match to the current time from the database
        //Query the database for matches with Date >= to today and time >= right now
        String today = Utilities.getTodaysDate();

        Log.d(TAG, "updateAppWidget: formatted Day: " + Utilities.getFormattedDay(today));

        String currentTime = Utilities.getTheTime();

        Log.d(TAG,"Today's Date: " + today);

        //Launch query in background thread

        //Get at DBHelper instance
        ScoresDBHelper dbHelper = new ScoresDBHelper(context);
        //Get access to the app's content resolver
        ContentResolver resolver = context.getContentResolver();

        //Build the Uri
        Uri allScoresUri = DatabaseContract.BASE_CONTENT_URI;

        Uri futureScoresUri = DatabaseContract.ScoresEntry.buildFutureScores();


        Cursor data = resolver.query(futureScoresUri,null,null,new String[] {today},null);

        //Get the first record
        if (data == null) {
            Log.d(TAG, "updateAppWidget: Scores Cursor is empty");
        } else {
            data.moveToFirst();
            Log.d(TAG, "updateAppWidget: We have data!");
            int count = data.getCount();
            Log.d(TAG, "updateAppWidget: Got " + count + " rows!");
            //Get the row with a game nearest to now.
            //Find out the date of the first row.
            do {
                String fetchedDate = data.getString(COL_DATE);
                //if the Date of the first row is not today, then the nearest game is at least tomorrow
                //we can extract and use this data for our widget
                if (fetchedDate.equals(today)) {
                    Log.d(TAG, "updateAppWidget: First date in result is a future date. Awesome!");
                    String fetchedTime = data.getString(COL_MATCHTIME);
                    Log.d(TAG, "updateAppWidget: currentTime: " + currentTime);
                    Log.d(TAG, "updateAppWidget: fetchedTime: " + fetchedTime);
                    int fetchedTimeHour = Integer.parseInt(fetchedTime.substring(0,2));
                    int currentTimeHour = Integer.parseInt(currentTime.substring(0,2));
                    if (fetchedTimeHour >= currentTimeHour) {
                        break;
                    }
                } else {
                    Log.d(TAG, "updateAppWidget: Future date found.");
                    break;
                }
            } while(data.moveToNext());




            //This record should be the next match; Collect the data
            views.setTextViewText(R.id.wdgt_date_textview, data.getString(COL_DATE));

            views.setTextViewText(R.id.wdgt_time_textview, data.getString(COL_MATCHTIME));

            views.setTextViewText(R.id.wdgt_home_name, data.getString(COL_HOME));

            views.setTextViewText(R.id.wdgt_away_name, data.getString(COL_AWAY));

            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }

    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
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

