package ru.evendate.android.ui.eventdetail;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import ru.evendate.android.EvendateAccountManager;
import ru.evendate.android.EvendatePreferences;
import ru.evendate.android.R;
import ru.evendate.android.models.Event;
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

    @BindView(R.id.promocode_container) LinearLayout mPromoCodeContainer;
    @BindView(R.id.promocode) EditText mPromoCode;

    @BindView(R.id.load_state) LoadStateView mLoadStateView;
    private Unbinder unbinder;
    @BindView(R.id.registration_promocode_sale) TextView mPromoSale;

    Event mEvent;
    Registration mRegistration;
    OnRegistrationCallbackListener mListener;
    ArrayList<OrderTicketView> ticketViews = new ArrayList<>();
    float totalSum = 0;

    @Nullable PromoCode promoCode;

    private static final String EVENT_KEY = "event";
    private static final String REGISTRATION_KEY = "registration";
    private static final int PAYMENT_REQUEST_CODE = 1;

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
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
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

        if (mEvent.isTicketingAvailable()) {
            for (TicketType type : mEvent.getTicketTypes()) {
                if (type.isSelling()) {
                    OrderTicketView view = new OrderTicketView(getContext());
                    view.setTicketType(type);
                    view.setOnTicketTotalSumChangedListener(this);
                    ticketViews.add(view);
                    mTicketList.addView(view);
                }
            }
            totalSumChanged();
            mSubmitButton.setText(R.string.ticketing_form_order_button);
        }
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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
    public void totalSumChanged() {
        float sum = 0;
        for (OrderTicketView view : ticketViews) {
            sum += view.getTicketTotalSum();
        }
        setTotalSum(sum);
        submitPromoCode();
    }

    private void setTotalSum(float sum) {
        totalSum = sum;
        mTotalSum.setText(TicketFormatter.formatCost(getContext(), totalSum));
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.promocode_submit_button)
    void onPromoCodeEntered() {
        checkPromoCode(mPromoCode.getText().toString());
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.registration_submit_button)
    void onSubmitClick() {
        getFormController().resetValidationErrors();
        if (getFormController().isValidInput()) {

            mRegistration = new Registration();
            setupRegistration(mRegistration);
            postRegistrationInput(mEvent.getEntryId(), mRegistration);
            mLoadStateView.showProgress();
        } else {
            getFormController().showValidationErrors();
        }
    }

    private void setupRegistration(Registration registration) {
        registration.setTickets(getTickets());
        registration.setRegistrationFieldsList(getRegistrationFields());
        if (promoCode != null) {
            registration.setPromocode(promoCode.getCode());
        }
    }

    private ArrayList<RegistrationField> getRegistrationFields() {
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

    private ArrayList<Ticket> getTickets() {
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

    public void checkPromoCode(String promoCode) {
        ApiService apiService = ApiFactory.getService(getContext());
        Observable<ResponseObject<PromoCode>> checkPromoCodeObservable =
                apiService.checkPromoCode(EvendateAccountManager.peekToken(getContext()),
                        mEvent.getEntryId(), promoCode);
        checkPromoCodeObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                            if (result.isOk()) {
                                this.promoCode = result.getData();
                                submitPromoCode();
                            } else {
                                Toast.makeText(getActivity(), "Указанный промокод не существует или более не активен",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                );
    }

    private void submitPromoCode() {
        if (promoCode == null || !promoCode.isEnabled())
            return;
        float dirtSum = totalSum;

        if (promoCode.isFixed()) {
            setTotalSum(Math.max(totalSum - promoCode.getEffort(), 0));
        } else if (promoCode.isPercentage()) {
            setTotalSum(totalSum * (1 - promoCode.getEffort() / 100));
        }
        mPromoSale.setText(TicketFormatter.formatCost(getContext(), dirtSum - totalSum));
    }

    public void postRegistrationInput(int eventId, Registration input) {
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
            } else {
            getActivity().onBackPressed();
            mListener.onPaymentError();
        }
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
