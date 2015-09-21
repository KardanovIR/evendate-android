package ru.getlect.evendate.evendate;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ru.getlect.evendate.evendate.data.EvendateContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {
    private DetailActivity mDetailActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDetailActivity = (DetailActivity)getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        TextView textView = (TextView)rootView.findViewById(R.id.text_view);
        final String[] PROJECTION = new String[] {
                EvendateContract.EventEntry._ID,
                EvendateContract.EventEntry.COLUMN_TITLE,
                EvendateContract.EventEntry.COLUMN_DESCRIPTION,
        };

        final int COLUMN_ID = 0;
        final int COLUMN_TITLE = 1;
        final int COLUMN_DESCRIPTION = 2;
        final Uri uri = mDetailActivity.mUri;
        Cursor c = getActivity().getContentResolver().query(uri, PROJECTION, null, null, null);
        c.moveToFirst();
        textView.setText(c.getString(COLUMN_TITLE));
        c.close();
        return rootView;
    }

    public DetailActivityFragment() {
        super();
    }
}
