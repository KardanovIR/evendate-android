package ru.evendate.android.ui.eventdetail;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.transition.TransitionManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.dkharrat.nexusdialog.FormController;
import com.github.dkharrat.nexusdialog.FormDialogFragment;
import com.github.dkharrat.nexusdialog.FormElementController;
import com.github.dkharrat.nexusdialog.controllers.CheckBoxController;
import com.github.dkharrat.nexusdialog.controllers.EditTextController;
import com.github.dkharrat.nexusdialog.controllers.FormSectionController;
import com.github.dkharrat.nexusdialog.controllers.RadioButtonController;
import com.yandex.money.api.methods.payment.params.PaymentParams;
import com.yandex.money.api.methods.payment.params.ShopParams;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import ru.evendate.android.EvendateAccountManager;
import ru.evendate.android.EvendatePreferences;
import ru.evendate.android.R;
import ru.evendate.android.models.Event;
import ru.evendate.android.models.Price;
import ru.evendate.android.models.PromoCode;
import ru.evendate.android.models.Registration;
import ru.evendate.android.models.RegistrationField;
import ru.evendate.android.models.Ticket;
import ru.evendate.android.models.TicketOrder;
import ru.evendate.android.models.TicketType;
import ru.evendate.android.models.User;
import ru.evendate.android.network.ApiFactory;
import ru.evendate.android.network.ApiService;
import ru.evendate.android.network.ResponseObject;
import ru.evendate.android.statistics.Statistics;
import ru.evendate.android.ui.utils.TicketFormatter;
import ru.evendate.android.views.LoadStateView;
import ru.evendate.android.views.OrderTicketView;
import ru.yandex.money.android.PaymentActivity;

import static android.app.Activity.RESULT_OK;

public class RegistrationFormFragment extends FormDialogFragment
        implements LoadStateView.OnReloadListener, OrderTicketView.OnTotalSumChangedListener {
    private static String LOG_TAG = RegistrationFormFragment.class.getSimpleName();
    @BindView(R.id.toolbar_registration) Toolbar mToolbar;
    @BindView(R.id.scroll_view) ScrollView mScrollView;
    @BindView(R.id.container) LinearLayout mContainer;
    @BindView(R.id.ticket_list) LinearLayout mTicketList;
    @BindView(R.id.registration_total_sum) TextView mTotalSum;
    @BindView(R.id.registration_submit_button) Button mSubmitButton;

    @BindView(R.id.promocode) EditText mPromoCodeEditView;

    @BindView(R.id.load_state) LoadStateView mLoadStateView;
    private Unbinder unbinder;
    @BindView(R.id.registration_final_cost) TextView mFinalCost;
    @BindView(R.id.registration_crossed_out_cost) TextView mCrossedCost;
    @BindView(R.id.registration_final_cost_container) ViewGroup mCostContainer;
    @BindViews({R.id.registration_ticket_section,
            R.id.ticket_list,
            R.id.registration_total_sum,
            R.id.promocode_container,
            R.id.promocode_description,
            R.id.registration_final_cost_container})
    List<View> mTicketViews;
    private ArrayList<OrderTicketView> ticketViews = new ArrayList<>();

    private Event mEvent;
    private Registration mRegistration;
    private OnRegistrationCallbackListener mListener;
    private float totalSum = 0;
    private float finalSum = 0;
    @Nullable private PromoCode mPromoCode;
    private Disposable mPromoCodeDisposable;
    private Disposable mPreOrderDisposable;

    private static final String EVENT_KEY = "event";
    private static final String REGISTRATION_KEY = "registration";
    private static final int PAYMENT_REQUEST_CODE = 1;

    private static final ButterKnife.Action<View> VISIBLE =
            (View view, int index) -> view.setVisibility(View.VISIBLE);
    private static final ButterKnife.Action<View> GONE =
            (View view, int index) -> view.setVisibility(View.GONE);

    interface OnRegistrationCallbackListener {
        void onRegistered();

        void onPaymentCompleted();

        void onPaymentError();
    }

    public static RegistrationFormFragment newInstance(Event event) {
        RegistrationFormFragment fragment = new RegistrationFormFragment();
        fragment.mEvent = event;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_registration, container, false);
        unbinder = ButterKnife.bind(this, rootView);

        mToolbar.setTitle(mEvent.getTitle());
        mToolbar.setNavigationIcon(R.drawable.ic_clear_white);
        mToolbar.setNavigationOnClickListener((View v) -> getActivity().onBackPressed());

        mLoadStateView.setOnReloadListener(this);
        setHasOptionsMenu(true);

        // check it's registration or ticketing
        if (mEvent.isTicketingLocally()) {
            ButterKnife.apply(mTicketViews, VISIBLE);
            constructTicketForm();
            totalSumChanged();
            Statistics.getInstance(getContext()).sendTicketingStarted(mEvent.getEntryId());
        } else {
            ButterKnife.apply(mTicketViews, GONE);
            Statistics.getInstance(getContext()).sendRegistrationStarted(mEvent.getEntryId());
        }
        mCrossedCost.setPaintFlags(mFinalCost.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        return rootView;
    }

    private void constructTicketForm() {
        for (TicketType type : mEvent.getTicketTypes()) {
            if (type.isSelling()) {
                OrderTicketView view = new OrderTicketView(getContext());
                view.setFormatter((float cost) -> TicketFormatter.formatCost(getContext(), cost));
                view.setTicketType(type);
                view.setOnTicketTotalSumChangedListener(this);
                ticketViews.add(view);
                mTicketList.addView(view);
            }
        }
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

    @Override
    public void initForm(FormController controller) {
        Context context = getContext();

        FormSectionController section = new FormSectionController(context,
                getString(R.string.event_registration_enter_fields));
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

    @Override
    public void onStart() {
        super.onStart();
        if (mEvent.isTicketingLocally()) {
            preOrder();
        }
    }


    @Override
    public void totalSumChanged() {
        float sum = 0;
        for (OrderTicketView view : ticketViews) {
            sum += view.getTicketTotalSum();
        }
        setInitSum(sum);
        preOrder();
        setupButton();
    }

    private void setInitSum(float sum) {
        if (!isAdded())
            return;
        totalSum = sum;
        mTotalSum.setText(" " + TicketFormatter.formatTotalCost(getContext(), totalSum));
        mFinalCost.setText(" " + TicketFormatter.formatCost(getContext(), totalSum));
        mCrossedCost.setText(" " + TicketFormatter.formatCost(getContext(), totalSum));
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.promocode_submit_button)
    void onPromoCodeEntered() {
        checkPromoCode(mPromoCodeEditView.getText().toString());
    }

    private void checkPromoCode(String promoCode) {
        hideKeyboard();

        if (promoCode.isEmpty()) {
            Toast.makeText(getActivity(), R.string.ticketing_form_toast_promo_code,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (this.mPromoCode != null && this.mPromoCode.getCode().equals(promoCode)) {
            return;
        }
        ApiService apiService = ApiFactory.getService(getContext());
        Observable<ResponseObject<PromoCode>> checkPromoCodeObservable =
                apiService.checkPromoCode(EvendateAccountManager.peekToken(getContext()),
                        mEvent.getEntryId(), promoCode);
        if (mPromoCodeDisposable != null && !mPromoCodeDisposable.isDisposed()) {
            mPromoCodeDisposable.dispose();
        }
        mPromoCodeDisposable = checkPromoCodeObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                            if (result.isOk()) {
                                this.mPromoCode = result.getData();
                            } else {
                                this.mPromoCode = null;
                                Toast.makeText(getActivity(), R.string.ticketing_form_toast_promo_code_error,
                                        Toast.LENGTH_SHORT).show();
                            }
                            preOrder();
                        }
                );
    }

    private void hideKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            view.clearFocus();
            InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void preOrder() {
        if (mPreOrderDisposable != null && !mPreOrderDisposable.isDisposed()) {
            mPreOrderDisposable.dispose();
        }
        mSubmitButton.setEnabled(false);
        Registration input = setupRegistrationWithoutFields();
        ApiService apiService = ApiFactory.getService(getContext());
        Observable<ResponseObject<Registration>> preOrderObservable =
                apiService.preorder(EvendateAccountManager.peekToken(getContext()), mEvent.getEntryId(), input);
        mPreOrderDisposable = preOrderObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                            if (result.isOk()) {
                                onPreOrderResult(result.getData());
                            }
                            mSubmitButton.setEnabled(true);
                        }, this::onError,
                        mLoadStateView::hideProgress
                );
    }

    private void onPreOrderResult(Registration registration) {
        setupDiscountedCost(registration.getPrice());
        setupButton();
    }

    private void setupDiscountedCost(Price price) {
        TransitionManager.beginDelayedTransition(mCostContainer);
        finalSum = totalSum;
        if (price.getDynamicDiscount() == 0 || price.getPromoCodeDiscount() == 0) {
            mCrossedCost.setVisibility(View.GONE);
        }
        finalSum = price.getFinalSum();
        mFinalCost.setText(" " + TicketFormatter.formatCost(getContext(), finalSum));

        if (totalSum != 0f && totalSum != finalSum) {
            mCrossedCost.setVisibility(View.VISIBLE);
        } else {
            mCrossedCost.setVisibility(View.GONE);
        }
    }

    private void setupButton() {
        if (finalSum == 0f) {
            mSubmitButton.setText(R.string.ticketing_form_register_button);
        } else {
            mSubmitButton.setText(R.string.ticketing_form_order_button);
        }
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.registration_submit_button)
    void onSubmitClick() {
        getFormController().resetValidationErrors();
        if (getFormController().isValidInput()) {

            mRegistration = setupRegistration();
            if (checkTicketsEmpty(mRegistration)) {
                Toast.makeText(getActivity(), R.string.ticketing_form_choose_tickets,
                        Toast.LENGTH_SHORT).show();
                return;
            }
            postRegistrationInput(mEvent.getEntryId(), mRegistration);
            mLoadStateView.showProgress();
        } else {
            getFormController().showValidationErrors();
        }
    }

    private Registration setupRegistrationWithoutFields() {
        Registration registration = new Registration();
        registration.setTickets(prepareTickets());
        if (mPromoCode != null) {
            registration.setPromocode(mPromoCode.getCode());
        }
        return registration;
    }

    private Registration setupRegistration() {
        Registration registration = new Registration();
        registration.setTickets(prepareTickets());
        registration.setRegistrationFieldsList(prepareRegistrationFields());
        if (mPromoCode != null) {
            registration.setPromocode(mPromoCode.getCode());
        }
        return registration;
    }

    private boolean checkTicketsEmpty(Registration registration) {
        for (Ticket ticket : registration.getTickets()) {
            if (((TicketOrder)ticket).getCount() != 0) {
                return false;
            }
        }
        return true;
    }

    private ArrayList<Ticket> prepareTickets() {
        ArrayList<Ticket> list = new ArrayList<>();
        if (mEvent.isTicketingAvailable()) {
            for (OrderTicketView view : ticketViews) {
                list.add(new TicketOrder(view.getTicket().getUuid(), view.getNumber()));
            }
        } else if (mEvent.isRegistrationAvailable()) {
            // registration without uuid
            list.add(new TicketOrder("", 1));
        }
        return list;
    }

    private ArrayList<RegistrationField> prepareRegistrationFields() {
        ArrayList<RegistrationField> input = new ArrayList<>();
        for (RegistrationField field : mEvent.getRegistrationFields()) {
            if (field.getType().equals("select") || field.getType().equals("select_multi")) {

                HashSet<RegistrationField> set = (HashSet<RegistrationField>)getModel().getValue(field.getUuid());
                ArrayList<RegistrationField> values = new ArrayList<>(set);
                input.add(new RegistrationField(field.getUuid(), values));

            } else {
                input.add(new RegistrationField(field.getUuid(), (String)getModel().getValue(field.getUuid())));
            }
        }
        return input;
    }

    private void postRegistrationInput(int eventId, Registration input) {
        ApiService apiService = ApiFactory.getService(getContext());
        Observable<ResponseObject<Registration>> registrationObservable =
                apiService.postRegistration(EvendateAccountManager.peekToken(getContext()), eventId, input);
        registrationObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                            if (result.isOk()) {
                                onResult(result.getData());
                            } else {
                                updateFields(result.getData());
                            }
                        }, this::onError,
                        mLoadStateView::hideProgress
                );
    }

    private void onResult(Registration registration) {
        if (mEvent.isTicketingAvailable()) {
            // order cost equals 0 => it's registration
            if (registration.getOrder().getFinalSum() == 0) {
                getActivity().onBackPressed();
                mListener.onRegistered();
            } else {
                String enteredEmail = getEmailFromFields(registration);
                if (enteredEmail == null || enteredEmail.isEmpty()) {
                    enteredEmail = EvendateAccountManager.getActiveAccountName(getContext());
                }
                User user = EvendatePreferences.newInstance(getContext()).getUser();
                String userName = user.getFirstName() + " " + user.getLastName();
                payByYandex(registration.getOrder().getFinalSum(), userName,
                        registration.getOrder().getUuid(), enteredEmail);
            }
        } else if (mEvent.isRegistrationAvailable()) {
            getActivity().onBackPressed();
            mListener.onRegistered();
            new Statistics(getContext()).sendRegistrationCompleted(mEvent.getEntryId());
        }
    }

    private String getEmailFromFields(Registration registration) {
        for (RegistrationField field : registration.getRegistrationFieldsList()) {
            if (field.getType().equals("email")) {
                return field.getValue();
            }
        }
        return null;
    }

    private void payByYandex(float sum, String userId, String orderId, String email) {
        Map<String, String> params = new HashMap<>();
        params.put("shopId", getString(R.string.yandex_money_shop_id));
        params.put("scid", getString(R.string.yandex_money_sc_id));
        params.put("sum", "" + sum);
        params.put("customerNumber", userId);
        params.put("paymentType", "");
        // todo need yandex docs
        //params.put("cps_email", email);
        params.put("evendate_payment_id", "order-" + orderId);

        PaymentParams shopParams = new ShopParams(getString(R.string.yandex_money_sc_id), params);
        Intent intent = PaymentActivity.getBuilder(getContext())
                .setPaymentParams(shopParams)
                .setClientId(getString(R.string.yandex_money_client_id))
                .setHost(getString(R.string.yandex_money_host))
                .build();
        startActivityForResult(intent, PAYMENT_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PAYMENT_REQUEST_CODE && resultCode == RESULT_OK) {
            getActivity().onBackPressed();
            mListener.onPaymentCompleted();
            Statistics.getInstance(getContext()).sendTicketingCompleted(mEvent.getEntryId());
        } else {
            getActivity().onBackPressed();
            mListener.onPaymentError();
            Statistics.getInstance(getContext()).sendTicketingAborted(mEvent.getEntryId());
        }
    }

    public void onBackPressed() {
        if (mEvent.isTicketingLocally()) {
            Statistics.getInstance(getContext()).sendTicketingCanceled(mEvent.getEntryId());
        } else {
            Statistics.getInstance(getContext()).sendRegistrationCanceled(mEvent.getEntryId());
        }
    }

    @Override
    public void onReload() {
        postRegistrationInput(mEvent.getEntryId(), mRegistration);
        mScrollView.setVisibility(View.VISIBLE);
    }

    private void onError(Throwable error) {
        Log.e(LOG_TAG, "" + error.getMessage());
        mLoadStateView.showErrorHint();
        mScrollView.setVisibility(View.INVISIBLE);
    }

    private void updateFields(Registration registration) {
        getFormController().resetValidationErrors();
        for (RegistrationField field : registration.getRegistrationFieldsList()) {
            FormElementController controller = getFormController().getElement(field.getUuid());
            controller.setError(field.getError());
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mPromoCodeDisposable != null && !mPromoCodeDisposable.isDisposed()) {
            mPromoCodeDisposable.dispose();
        }
        if (mPreOrderDisposable != null && !mPreOrderDisposable.isDisposed()) {
            mPreOrderDisposable.dispose();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
