package ru.evendate.android.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import ru.evendate.android.R;

/**
 * Created by Dmitry on 16.09.2016.
 */
public class LoadStateView extends FrameLayout {

    private Button reloadButton;
    private TextView headerView;
    private TextView descriptionView;
    private ProgressBar progressBar;
    OnReloadListener listener;

    public LoadStateView(Context context) {
        super(context, null);
    }

    public LoadStateView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);

        LayoutInflater inflater = (LayoutInflater)context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_load_state, this, true);
        ViewGroup viewGroup = (ViewGroup)getChildAt(0);
        reloadButton = (Button)viewGroup.getChildAt(0);
        headerView = (TextView)viewGroup.getChildAt(1);
        descriptionView = (TextView)viewGroup.getChildAt(2);
        progressBar = (ProgressBar)viewGroup.getChildAt(3);
        reloadButton.setOnClickListener((View v) -> {
            showProgress();
            hideHint();
            hideReloadButton();
            if (listener != null)
                listener.onReload();
        });
    }

    public interface OnReloadListener {
        void onReload();
    }

    public void setOnReloadListener(OnReloadListener listener) {
        this.listener = listener;
    }

    public void setHeader(String header) {
        headerView.setText(header);
    }

    public void setDescription(String description) {
        descriptionView.setText(description);
    }

    public void setText(String header, String description) {
        headerView.setText(header);
        descriptionView.setText(description);
    }

    public void setErrorHint() {
        headerView.setText(getContext().getString(R.string.state_error));
        descriptionView.setText(getContext().getString(R.string.state_error_description));
    }

    public void setNoInternethint() {
        headerView.setText(getContext().getString(R.string.state_no_connection));
        descriptionView.setText(getContext().getString(R.string.state_no_connection_description));
    }

    public void hide(){
        progressBar.setVisibility(GONE);
        reloadButton.setVisibility(GONE);
        headerView.setVisibility(GONE);
        descriptionView.setVisibility(GONE);
    }

    private void showReloadButton() {
        reloadButton.setVisibility(VISIBLE);
    }

    private void hideReloadButton() {
        reloadButton.setVisibility(GONE);
    }

    private void hideHint() {
        headerView.setVisibility(GONE);
        descriptionView.setVisibility(GONE);
    }

    public void hideProgress() {
        progressBar.setVisibility(GONE);
    }

    public void showProgress() {
        progressBar.setVisibility(VISIBLE);
        hideHint();
        hideReloadButton();
    }

    public void showHint() {
        hideProgress();
        hideReloadButton();
        headerView.setVisibility(VISIBLE);
        descriptionView.setVisibility(VISIBLE);
    }

    public void showErrorHint() {
        hideProgress();
        showReloadButton();
        headerView.setVisibility(VISIBLE);
        descriptionView.setVisibility(VISIBLE);
    }

}
