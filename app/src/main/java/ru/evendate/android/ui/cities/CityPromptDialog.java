package ru.evendate.android.ui.cities;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import ru.evendate.android.R;

/**
 * Created by Aedirn on 11.03.17.
 */

public class CityPromptDialog extends Fragment {
    PromptInteractionListener mListener;
    private Unbinder unbinder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_city_prompt, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof PromptInteractionListener) {
            mListener = (PromptInteractionListener)context;
        } else {
            throw new RuntimeException("Activity should implement PromptInteractionListener" +
                    "interface");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick(R.id.button2)
    public void onClick() {
        mListener.onPlaceButtonClick();
    }

    interface PromptInteractionListener {
        void onPlaceButtonClick();
    }
}