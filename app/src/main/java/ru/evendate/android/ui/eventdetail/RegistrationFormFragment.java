package ru.evendate.android.ui.eventdetail;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.github.dkharrat.nexusdialog.FormController;
import com.github.dkharrat.nexusdialog.FormElementController;
import com.github.dkharrat.nexusdialog.FormInitializer;
import com.github.dkharrat.nexusdialog.FormManager;
import com.github.dkharrat.nexusdialog.FormModel;
import com.github.dkharrat.nexusdialog.controllers.CheckBoxController;
import com.github.dkharrat.nexusdialog.controllers.EditTextController;
import com.github.dkharrat.nexusdialog.controllers.FormSectionController;
import com.github.dkharrat.nexusdialog.controllers.RadioButtonController;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import ru.evendate.android.EvendateAccountManager;
import ru.evendate.android.R;
import ru.evendate.android.models.Event;
import ru.evendate.android.models.Registration;
import ru.evendate.android.models.RegistrationField;
import ru.evendate.android.models.Ticket;
import ru.evendate.android.models.TicketOrder;
import ru.evendate.android.network.ApiFactory;
import ru.evendate.android.network.ApiService;
import ru.evendate.android.network.ResponseObject;
import ru.evendate.android.views.LoadStateView;

public class RegistrationFormFragment extends DialogFragment implements FormInitializer,
        LoadStateView.OnReloadListener {
    private static String LOG_TAG = RegistrationFormFragment.class.getSimpleName();
    @BindView(R.id.toolbar_registration) Toolbar mToolbar;
    @BindView(R.id.scroll_view) ScrollView mScrollView;
    @BindView(R.id.container) LinearLayout mContainer;

    private FormManager formManager;
    @BindView(R.id.load_state) LoadStateView mLoadStateView;
    private Unbinder unbinder;

    Event mEvent;
    Registration mRegistration;
    OnRegistrationCallbackListener mListener;

    private static final String EVENT_KEY = "event";
    private static final String REGISTRATION_KEY = "registration";

    interface OnRegistrationCallbackListener {
        void onRegistered();
    }

    public static RegistrationFormFragment newInstance(Event event) {
        RegistrationFormFragment fragment = new RegistrationFormFragment();
        fragment.mEvent = event;
        return fragment;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_registration, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        //todo
        mToolbar.setTitle(R.string.event_registration_title);

        mToolbar.setNavigationIcon(R.drawable.ic_clear_white);
        mToolbar.setNavigationOnClickListener((View v) -> getActivity().onBackPressed());

        mLoadStateView.setOnReloadListener(this);
        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        formManager = new FormManager(this, this, R.id.form_elements_container);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(EVENT_KEY, Parcels.wrap(mEvent));
        outState.putParcelable(REGISTRATION_KEY, Parcels.wrap(mRegistration));

    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            mEvent = Parcels.unwrap(savedInstanceState.getParcelable(EVENT_KEY));
            mRegistration = Parcels.unwrap(savedInstanceState.getParcelable(REGISTRATION_KEY));
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnRegistrationCallbackListener) {
            mListener = (OnRegistrationCallbackListener)context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnRegistrationCallbackListener");
        }
    }

    public FormController getFormController() {
        return formManager.getFormController();
    }

    public FormModel getModel() {
        return getFormController().getModel();
    }

    protected void recreateViews() {
        formManager.recreateViews();
    }

    @Override
    public void initForm(FormController controller) {
        Context context = getContext();

        FormSectionController section = new FormSectionController(context, mEvent.getTitle());
        for (RegistrationField field : mEvent.getRegistrationFields()) {
            if (field.getType().equals("select") || field.getType().equals("select_multi")) {
                List<String> items = new ArrayList<>();
                List<RegistrationField> values = new ArrayList<>();
                for (RegistrationField selectValue : field.getValues()) {
                    items.add(selectValue.getValue());
                    values.add(selectValue);
                }
                if (field.getType().equals("select")) {
                    section.addElement(new RadioButtonController(context, field.getUuid(), field.getLabel(),
                            field.isRequired(), items, values));
                } else {
                    section.addElement(new CheckBoxController(context, field.getUuid(), field.getLabel(),
                            field.isRequired(), items, values));
                }
            } else {
                section.addElement(new EditTextController(context, field.getUuid(), field.getLabel(),
                        "", field.isRequired()));
            }
        }
        controller.addSection(section);
        ViewGroup containerView = (ViewGroup)getActivity().findViewById(R.id.form_elements_container);
        controller.recreateViews(containerView);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            dismiss();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.registration_submit_button)
    void onSubmitClick() {
        getFormController().resetValidationErrors();
        if (getFormController().isValidInput()) {
            List<RegistrationField> input = new ArrayList<>();
            for (RegistrationField field : mEvent.getRegistrationFields()) {
                if (field.getType().equals("select") || field.getType().equals("select_multi")) {

                    HashSet<RegistrationField> set = (HashSet<RegistrationField>)getModel().getValue(field.getUuid());
                    ArrayList<RegistrationField> values = new ArrayList<>(set);
                    input.add(new RegistrationField(field.getUuid(), values));

                } else {
                    input.add(new RegistrationField(field.getUuid(), (String)getModel().getValue(field.getUuid())));
                }
            }
            mRegistration = new Registration();
            mRegistration.setRegistrationFieldsList(new ArrayList<>(input));
            ArrayList<Ticket> ticketOrderList = new ArrayList<>();
            // registration without uuid
            ticketOrderList.add(new TicketOrder("", 1));
            mRegistration.setTickets(ticketOrderList);
            postRegistrationInput(mEvent.getEntryId(), mRegistration);
            mLoadStateView.showProgress();
        } else {
            getFormController().showValidationErrors();
        }
    }

    public void postRegistrationInput(int eventId, Registration input) {
        ApiService apiService = ApiFactory.getService(getContext());
        Observable<ResponseObject<Registration>> registrationObservable =
                apiService.postRegistration(EvendateAccountManager.peekToken(getContext()), eventId, input);
        registrationObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                            if (result.isOk()) {
                                getActivity().onBackPressed();
                                mListener.onRegistered();
                            } else {
                                updateFields(result.getData());
                            }
                        }, this::onError,
                        mLoadStateView::hideProgress
                );
    }

    @Override
    public void onReload() {
        postRegistrationInput(mEvent.getEntryId(), mRegistration);
        mScrollView.setVisibility(View.VISIBLE);
    }

    public void onError(Throwable error) {
        Log.e(LOG_TAG, "" + error.getMessage());
        mLoadStateView.showErrorHint();
        mScrollView.setVisibility(View.INVISIBLE);
    }

    public void updateFields(Registration registration) {
        getFormController().resetValidationErrors();
        for (RegistrationField field : registration.getRegistrationFieldsList()) {
            FormElementController controller = getFormController().getElement(field.getUuid());
            controller.setError(field.getError());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
