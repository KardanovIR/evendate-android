package ru.evendate.android.ui;

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

import com.github.dkharrat.nexusdialog.FormController;
import com.github.dkharrat.nexusdialog.FormElementController;
import com.github.dkharrat.nexusdialog.FormInitializer;
import com.github.dkharrat.nexusdialog.FormManager;
import com.github.dkharrat.nexusdialog.FormModel;
import com.github.dkharrat.nexusdialog.controllers.EditTextController;
import com.github.dkharrat.nexusdialog.controllers.FormSectionController;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ru.evendate.android.EvendateAccountManager;
import ru.evendate.android.R;
import ru.evendate.android.models.EventFull;
import ru.evendate.android.models.Registration;
import ru.evendate.android.models.RegistrationField;
import ru.evendate.android.network.ApiFactory;
import ru.evendate.android.network.ApiService;
import ru.evendate.android.network.ResponseArray;
import ru.evendate.android.views.LoadStateView;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

//todo saveinstantstate
public class RegistrationFormFragment extends DialogFragment implements FormInitializer,
        LoadStateView.OnReloadListener {
    private static String LOG_TAG = RegistrationFormFragment.class.getSimpleName();

    private FormManager formManager;
    @Bind(R.id.load_state) LoadStateView mLoadStateView;

    EventFull mEvent;
    Registration mRegistration;


    public static RegistrationFormFragment newInstance(EventFull event) {
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
        ButterKnife.bind(this, rootView);

        Toolbar toolbar = (Toolbar)rootView.findViewById(R.id.toolbar_registration);
        //todo
        toolbar.setTitle("Регистрация на событие");

        toolbar.setNavigationIcon(R.drawable.ic_clear_white);
        toolbar.setNavigationOnClickListener((View v) -> getActivity().onBackPressed());

        mLoadStateView.setOnReloadListener(this);
        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        formManager = new FormManager(this, this, R.id.form_elements_container);
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
        Context ctxt = getContext();
        FormSectionController section = new FormSectionController(ctxt, mEvent.getTitle());
        for (RegistrationField field : mEvent.getRegistrationFieldsList()) {
            section.addElement(new EditTextController(ctxt, field.getUuid(), field.getLabel(), "", field.isRequired()));
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
            for (RegistrationField field : mEvent.getRegistrationFieldsList())
                input.add(new RegistrationField(field.getUuid(), (String)getModel().getValue(field.getUuid())));
            mRegistration = new Registration();
            mRegistration.setRegistrationFieldsList(new ArrayList<>(input));
            postRegistrationInput(mEvent.getEntryId(), mRegistration);
            mLoadStateView.showProgress();
        } else {
            getFormController().showValidationErrors();
        }
    }


    public void postRegistrationInput(int eventId, Registration input) {
        ApiService apiService = ApiFactory.getService(getContext());
        Observable<ResponseArray<Registration>> registrationObservable =
                apiService.postRegistration(EvendateAccountManager.peekToken(getContext()), eventId, input);
        registrationObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                            if (result.isOk()) {
                                //todo ok snack!
                                dismiss();
                            } else {
                                updateFields(result.getData().get(0));
                            }
                        }, this::onError,
                        mLoadStateView::hideProgress
                );
    }

    @Override
    public void onReload() {
        postRegistrationInput(mEvent.getEntryId(), mRegistration);
    }

    public void onError(Throwable error) {
        Log.e(LOG_TAG, "" + error.getMessage());
        mLoadStateView.showErrorHint();
    }

    public void updateFields(Registration registration) {
        getFormController().resetValidationErrors();
        for (RegistrationField field : registration.getRegistrationFieldsList()) {
            FormElementController controller = getFormController().getElement(field.getUuid());
            controller.setError(field.getError());
        }
    }
}
