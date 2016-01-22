package barqsoft.footballscores;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import barqsoft.footballscores.DatabaseContract.ScoresEntry;

/**
 * Created by yehya khaled on 2/25/2015.
 */
public class ScoresDBHelper extends SQLiteOpenHelper
{
    public static final String DATABASE_NAME = "Scores.db";
    private static final int DATABASE_VERSION = 8;
    public ScoresDBHelper(Context context)
    {
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        //Create Scores table
        final String CreateScoresTable = "CREATE TABLE " + DatabaseContract.SCORES_TABLE + " ("
                + DatabaseContract.ScoresEntry._ID + " INTEGER PRIMARY KEY,"
                + ScoresEntry.DATE_COL + " TEXT NOT NULL,"
                + DatabaseContract.ScoresEntry.TIME_COL + " INTEGER NOT NULL,"
                + DatabaseContract.ScoresEntry.HOME_COL + " TEXT NOT NULL,"
                + ScoresEntry.AWAY_COL + " TEXT NOT NULL,"
                + DatabaseContract.ScoresEntry.LEAGUE_COL + " INTEGER NOT NULL,"
                + DatabaseContract.ScoresEntry.HOME_GOALS_COL + " TEXT NOT NULL,"
                + ScoresEntry.AWAY_GOALS_COL + " TEXT NOT NULL,"
                + DatabaseContract.ScoresEntry.MATCH_ID + " INTEGER NOT NULL,"
                + ScoresEntry.MATCH_DAY + " INTEGER NOT NULL,"
                + " UNIQUE ("+ DatabaseContract.ScoresEntry.MATCH_ID+") ON CONFLICT REPLACE"
                + " );";

        //Create Teams Table
        final String CreateTeamsTable = "CREATE TABLE " + DatabaseContract.TEAMS_TABLE + "("
                + DatabaseContract.TeamsEntry._ID + " INTEGER PRIMARY KEY,"
                + DatabaseContract.TeamsEntry.COL_NAME + " TEXT,"
                + DatabaseContract.TeamsEntry.COL_CODE + " TEXT,"
                + DatabaseContract.TeamsEntry.COL_SHORT_NAME + " TEXT,"
                + DatabaseContract.TeamsEntry.COL_CREST_URL + " TEXT"
                + " );";


        db.execSQL(CreateScoresTable);
        db.execSQL(CreateTeamsTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        //Remove old values when upgrading.
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.SCORES_TABLE);
        onCreate(db);
    }
}
