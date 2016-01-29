package barqsoft.footballscores;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * Created by yehya khaled on 2/26/2015.
 */
public class scoresAdapter extends CursorAdapter
{

    public double detail_match_id = 0;
    private String FOOTBALL_SCORES_HASHTAG = "#Football_Scores";

    final String COL_HOME_SHORT = "homeTeamShort";
    final String COL_HOME_CREST_URL = "homeTeamCrest";
    final String COL_AWAY_SHORT = "awayTeamShort";
    final String COL_AWAY_CREST_URL = "awayTeamCrest";

    public scoresAdapter(Context context,Cursor cursor,int flags)
    {
        super(context,cursor,flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent)
    {
        View mItem = LayoutInflater.from(context).inflate(R.layout.scores_list_item, parent, false);
        ViewHolder mHolder = new ViewHolder(mItem);
        mItem.setTag(mHolder);
        //Log.v(FetchScoreTask.LOG_TAG,"new View inflated");
        return mItem;
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor)
    {
        final ViewHolder mHolder = (ViewHolder) view.getTag();

        mHolder.home_name.setText(cursor.getString(cursor.getColumnIndex(COL_HOME_SHORT)));
        mHolder.away_name.setText(cursor.getString(cursor.getColumnIndex(COL_AWAY_SHORT)));
        mHolder.date.setText(Utilities.
                getFormattedTime(cursor.getString(cursor.getColumnIndex(DatabaseContract.ScoresEntry.TIME_COL))));
        mHolder.score.setText(Utilities.getScores(cursor.getInt(cursor.getColumnIndex(DatabaseContract.ScoresEntry.HOME_GOALS_COL)),
                cursor.getInt(cursor.getColumnIndex(DatabaseContract.ScoresEntry.AWAY_GOALS_COL))));
        mHolder.match_id = cursor.getDouble(cursor.getColumnIndex(DatabaseContract.ScoresEntry.MATCH_ID));
        Picasso.with(context)
                .load(Utilities.getPNGUrl(cursor.getString(cursor.getColumnIndex(COL_HOME_CREST_URL))))
                .error(R.drawable.ic_launcher)
                .into(mHolder.home_crest);
        Picasso.with(context)
                .load(Utilities.getPNGUrl(cursor.getString(cursor.getColumnIndex(COL_AWAY_CREST_URL))))
                .error(R.drawable.ic_launcher)
                .into(mHolder.away_crest);

        //Log.v(FetchScoreTask.LOG_TAG,mHolder.home_name.getText() + " Vs. " + mHolder.away_name.getText() +" id " + String.valueOf(mHolder.match_id));
        //Log.v(FetchScoreTask.LOG_TAG,String.valueOf(detail_match_id));
        LayoutInflater vi = (LayoutInflater) context.getApplicationContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = vi.inflate(R.layout.detail_fragment, null);
        ViewGroup container = (ViewGroup) view.findViewById(R.id.details_fragment_container);
        if(mHolder.match_id == detail_match_id)
        {
            //Log.v(FetchScoreTask.LOG_TAG,"will insert extraView");

            container.addView(v, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                    , ViewGroup.LayoutParams.MATCH_PARENT));
            TextView match_day = (TextView) v.findViewById(R.id.matchday_textview);
            match_day.setText(Utilities.getMatchDay(cursor.getInt(cursor.getColumnIndex(DatabaseContract.ScoresEntry.MATCH_DAY)),
                                                    cursor.getInt(cursor.getColumnIndex(DatabaseContract.ScoresEntry.LEAGUE_COL))));
            TextView league = (TextView) v.findViewById(R.id.league_textview);
            league.setText(Utilities.getLeague(cursor.getInt(cursor.getColumnIndex(DatabaseContract.ScoresEntry.LEAGUE_COL))));
            Button share_button = (Button) v.findViewById(R.id.share_button);
            share_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    //add Share Action
                    context.startActivity(createShareScoreIntent(mHolder.home_name.getText() + " "
                            + mHolder.score.getText() + " " + mHolder.away_name.getText() + " "));
                }
            });
        }
        else
        {
            container.removeAllViews();
        }

    }
    public Intent createShareScoreIntent(String ShareText) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, ShareText + FOOTBALL_SCORES_HASHTAG);
        return shareIntent;
    }

}
