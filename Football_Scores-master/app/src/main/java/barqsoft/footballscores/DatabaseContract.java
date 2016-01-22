package barqsoft.footballscores;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by yehya khaled on 2/25/2015.
 */
public class DatabaseContract
{
    //URI data
    public static final String CONTENT_AUTHORITY = "barqsoft.footballscores";
    public static final String SCORES_PATH = "score";
    public static final String TEAMS_PATH = "team";
    public static Uri BASE_CONTENT_URI = Uri.parse("content://"+CONTENT_AUTHORITY);


    public static final String SCORES_TABLE = "scores";
    public static final String TEAMS_TABLE = "teams";

    public static final class ScoresEntry implements BaseColumns
    {
        //Table data
        public static final String LEAGUE_COL = "league";
        public static final String DATE_COL = "date";
        public static final String TIME_COL = "time";
        public static final String HOME_COL = "home";
        public static final String AWAY_COL = "away";
        public static final String HOME_GOALS_COL = "home_goals";
        public static final String AWAY_GOALS_COL = "away_goals";
        public static final String MATCH_ID = "match_id";
        public static final String MATCH_DAY = "match_day";

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(SCORES_PATH).build();

        //Types
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + SCORES_PATH;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + SCORES_PATH;

        public static Uri buildScoreWithLeague()
        {
            return CONTENT_URI.buildUpon().appendPath("league").build();
        }
        public static Uri buildScoreWithId()
        {
            return CONTENT_URI.buildUpon().appendPath("id").build();
        }
        public static Uri buildScoreWithDate()
        {
            return CONTENT_URI.buildUpon().appendPath("date").build();
        }
        public static Uri buildFutureScores()
        {
            return CONTENT_URI.buildUpon().appendPath("future").build();
        }
    }


    public static final class TeamsEntry implements BaseColumns {
        //This class will specify the attributes associated with the Team table

        //Table Data
        public static final String TABLE_NAME = "teams";

        public static final String COL_NAME = "name";

        public static final String COL_CODE = "code";

        public static final String COL_SHORT_NAME = "shortName";

        public static final String COL_CREST_URL = "crestUrl";

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(TEAMS_PATH).build();

        //Content Types
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + TEAMS_PATH;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + TEAMS_PATH;

        //URIs
        public static Uri buildUriWithId(long id) {
            return ContentUris.withAppendedId(CONTENT_URI,id);
        }
    }


}
