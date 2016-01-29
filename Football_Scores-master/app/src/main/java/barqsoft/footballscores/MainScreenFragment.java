package barqsoft.footballscores;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import barqsoft.footballscores.service.myFetchService;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainScreenFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>
{
    public scoresAdapter mAdapter;
    public static final int SCORES_LOADER = 0;
    private String[] fragmentdate = new String[1];
    private int last_selected_item = -1;

    public MainScreenFragment()
    {
    }

    private void update_scores()
    {
        Intent service_start = new Intent(getActivity(), myFetchService.class);
        getActivity().startService(service_start);
    }
    public void setFragmentDate(String date)
    {
        fragmentdate[0] = date;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        update_scores();
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        final ListView score_list = (ListView) rootView.findViewById(R.id.scores_list);
        mAdapter = new scoresAdapter(getActivity(),null,0);
        score_list.setAdapter(mAdapter);
        getLoaderManager().initLoader(SCORES_LOADER,null,this);
        mAdapter.detail_match_id = MainActivity.selected_match_id;
        score_list.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                ViewHolder selected = (ViewHolder) view.getTag();
                mAdapter.detail_match_id = selected.match_id;
                MainActivity.selected_match_id = (int) selected.match_id;
                mAdapter.notifyDataSetChanged();
            }
        });
        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle)
    {

        final String COL_HOME_SHORT = "homeTeamShort";
        final String COL_HOME_CREST_URL = "homeTeamCrest";
        final String COL_AWAY_NAME = "awayTeamName";
        final String COL_AWAY_CODE = "awayTeamCode";
        final String COL_AWAY_SHORT = "awayTeamShort";
        final String COL_AWAY_CREST_URL = "awayTeamCrest";

        String[] requestedColumns = new String[] {
                DatabaseContract.SCORES_TABLE + "." + DatabaseContract.ScoresEntry._ID,
                DatabaseContract.ScoresEntry.DATE_COL,
                DatabaseContract.ScoresEntry.TIME_COL,
                DatabaseContract.ScoresEntry.LEAGUE_COL,
                DatabaseContract.ScoresEntry.HOME_GOALS_COL,
                DatabaseContract.ScoresEntry.AWAY_GOALS_COL,
                DatabaseContract.ScoresEntry.MATCH_DAY,
                DatabaseContract.ScoresEntry.MATCH_ID,
                "h.shortName as " + COL_HOME_SHORT,
                "h.crestUrl as " + COL_HOME_CREST_URL,
                "a.name as " + COL_AWAY_NAME,
                "a.code as " + COL_AWAY_CODE,
                "a.shortName as " + COL_AWAY_SHORT,
                "a.crestUrl as " + COL_AWAY_CREST_URL
        };

        String dateRequest = DatabaseContract.ScoresEntry.DATE_COL + " LIKE ?";

        return new CursorLoader(getActivity(), DatabaseContract.BASE_CONTENT_URI,
                requestedColumns,dateRequest,fragmentdate,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor)
    {
        //Log.v(FetchScoreTask.LOG_TAG,"loader finished");
        //cursor.moveToFirst();
        /*
        while (!cursor.isAfterLast())
        {
            Log.v(FetchScoreTask.LOG_TAG,cursor.getString(1));
            cursor.moveToNext();
        }
        */

        int i = 0;
        cursor.moveToFirst();
        while (!cursor.isAfterLast())
        {
            i++;
            cursor.moveToNext();
        }
        //Log.v(FetchScoreTask.LOG_TAG,"Loader query: " + String.valueOf(i));
        mAdapter.swapCursor(cursor);
        //mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader)
    {
        mAdapter.swapCursor(null);
    }


}
