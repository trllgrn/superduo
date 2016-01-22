package barqsoft.footballscores;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

/**
 * Created by yehya khaled on 2/25/2015.
 */
public class ScoresProvider extends ContentProvider
{
    private static final String TAG = "ScoresProvider";
    private static ScoresDBHelper mOpenHelper;


    private static final int MATCHES = 100;
    private static final int MATCHES_WITH_LEAGUE = 101;
    private static final int MATCHES_WITH_ID = 102;
    private static final int MATCHES_WITH_DATE = 103;
    private static final int FUTURE_MATCHES = 104;
    private static final int TEAMS = 300;
    private static final int TEAMS_WITH_ID = 301;

    private UriMatcher muriMatcher = buildUriMatcher();
    private static final SQLiteQueryBuilder ScoreQuery =
            new SQLiteQueryBuilder();
    private static final String SCORES_BY_LEAGUE = DatabaseContract.ScoresEntry.LEAGUE_COL + " = ?";
    private static final String SCORES_BY_DATE =
            DatabaseContract.ScoresEntry.DATE_COL + " LIKE ?";
    private static final String SCORES_BY_ID =
            DatabaseContract.ScoresEntry.MATCH_ID + " = ?";
    //for retrieving matches yet to be played
    private static final String PENDING_SCORES = DatabaseContract.ScoresEntry.DATE_COL + ">= ?";

    private static final String TEAMS_BY_ID = DatabaseContract.TeamsEntry._ID + " = ?";


    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String baseAuthority = DatabaseContract.CONTENT_AUTHORITY.toString();

        matcher.addURI(baseAuthority, null , MATCHES);
        matcher.addURI(baseAuthority, "score/league" , MATCHES_WITH_LEAGUE);
        matcher.addURI(baseAuthority, "score/id" , MATCHES_WITH_ID);
        matcher.addURI(baseAuthority, "score/date" , MATCHES_WITH_DATE);
        matcher.addURI(baseAuthority, "score/future", FUTURE_MATCHES);
        matcher.addURI(baseAuthority, DatabaseContract.TEAMS_PATH, TEAMS);
        matcher.addURI(baseAuthority, DatabaseContract.TEAMS_PATH + "/#", TEAMS_WITH_ID);
        return matcher;
    }

    /*private int match_uri(Uri uri)
    {
        String link = uri.toString();
        {
           if(link.contentEquals(DatabaseContract.BASE_CONTENT_URI.toString()))
           {
               return MATCHES;
           }
           else if(link.contentEquals(DatabaseContract.ScoresEntry.buildScoreWithDate().toString()))
           {
               return MATCHES_WITH_DATE;
           }
           else if(link.contentEquals(DatabaseContract.ScoresEntry.buildScoreWithId().toString()))
           {
               return MATCHES_WITH_ID;
           }
           else if(link.contentEquals(DatabaseContract.ScoresEntry.buildScoreWithLeague().toString()))
           {
               return MATCHES_WITH_LEAGUE;
           }
           else if (link.contentEquals(DatabaseContract.ScoresEntry.buildFutureScores().toString())) {
               return FUTURE_MATCHES;
           }
        }
        return -1;
    }*/

    @Override
    public boolean onCreate()
    {
        mOpenHelper = new ScoresDBHelper(getContext());
        return false;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs)
    {
        return 0;
    }

    @Override
    public String getType(Uri uri)
    {
        final int match = muriMatcher.match(uri);
        switch (match) {
            //multiple items returned
            case MATCHES:
            case MATCHES_WITH_LEAGUE:
            case MATCHES_WITH_DATE:
            case FUTURE_MATCHES:
                return DatabaseContract.ScoresEntry.CONTENT_TYPE;
            //single item returned
            case MATCHES_WITH_ID:
                return DatabaseContract.ScoresEntry.CONTENT_ITEM_TYPE;
            case TEAMS:
                return DatabaseContract.TeamsEntry.CONTENT_TYPE;
            case TEAMS_WITH_ID:
                return DatabaseContract.TeamsEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri :" + uri );
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
    {
        Cursor retCursor;
        //Log.v(FetchScoreTask.LOG_TAG,uri.getPathSegments().toString());
        int match = muriMatcher.match(uri);
        //Log.v(FetchScoreTask.LOG_TAG,SCORES_BY_LEAGUE);
        //Log.v(FetchScoreTask.LOG_TAG,selectionArgs[0]);
        //Log.v(FetchScoreTask.LOG_TAG,String.valueOf(match));
        switch (match)
        {
            case MATCHES: retCursor = mOpenHelper.getReadableDatabase().query(
                    DatabaseContract.SCORES_TABLE,
                    projection,null,null,null,null,sortOrder);
                 break;
            case MATCHES_WITH_DATE:
                    //Log.v(FetchScoreTask.LOG_TAG,selectionArgs[1]);
                    //Log.v(FetchScoreTask.LOG_TAG,selectionArgs[2]);
                    retCursor = mOpenHelper.getReadableDatabase().query(
                    DatabaseContract.SCORES_TABLE,
                    projection,SCORES_BY_DATE,selectionArgs,null,null,sortOrder);
                break;
            case MATCHES_WITH_ID: retCursor = mOpenHelper.getReadableDatabase().query(
                    DatabaseContract.SCORES_TABLE,
                    projection,SCORES_BY_ID,selectionArgs,null,null,sortOrder);
                break;
            case MATCHES_WITH_LEAGUE: retCursor = mOpenHelper.getReadableDatabase().query(
                    DatabaseContract.SCORES_TABLE,
                    projection,SCORES_BY_LEAGUE,selectionArgs,null,null,sortOrder);
                break;
            case FUTURE_MATCHES:
                retCursor = mOpenHelper.getReadableDatabase().query(DatabaseContract.SCORES_TABLE,
                                                                    projection,PENDING_SCORES,selectionArgs,null,null,sortOrder);
                break;
            case TEAMS:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        DatabaseContract.TEAMS_TABLE,
                        projection,selection,selectionArgs,null,null,sortOrder);
                break;
            case TEAMS_WITH_ID:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        DatabaseContract.TEAMS_TABLE,
                        projection,TEAMS_BY_ID,null,null,null,null,sortOrder);
                break;
            default: throw new UnsupportedOperationException("Unknown Uri " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(),uri);
        return retCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        Log.d(TAG, "insert: Attempting to insert into TEAMS: Database version - " + db.getVersion());
        Uri returnUri = null;

        switch (muriMatcher.match(uri)) {
            case TEAMS: {
                long _id = db.insertWithOnConflict(DatabaseContract.TEAMS_TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                if (_id > 0) {
                    returnUri = DatabaseContract.TeamsEntry.buildUriWithId(_id);
                } else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri + "; Values: " + values.toString());
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }


    @Override
    public int bulkInsert(Uri uri, ContentValues[] values)
    {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        //db.delete(DatabaseContract.SCORES_TABLE,null,null);
        //Log.v(FetchScoreTask.LOG_TAG,String.valueOf(muriMatcher.match(uri)));
        switch (muriMatcher.match(uri))
        {
            case MATCHES:
                db.beginTransaction();
                int returncount = 0;
                try
                {
                    for(ContentValues value : values)
                    {
                        long _id = db.insertWithOnConflict(DatabaseContract.SCORES_TABLE, null, value,
                                SQLiteDatabase.CONFLICT_REPLACE);
                        if (_id != -1)
                        {
                            returncount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri,null);
                return returncount;
            default:
                return super.bulkInsert(uri,values);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }
}
