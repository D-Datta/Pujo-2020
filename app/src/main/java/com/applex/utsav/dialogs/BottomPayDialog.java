package com.applex.utsav.dialogs;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.applex.utsav.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.shreyaspatil.easyupipayment.EasyUpiPayment;
import com.shreyaspatil.easyupipayment.exception.AppNotFoundException;
import com.shreyaspatil.easyupipayment.listener.PaymentStatusListener;
import com.shreyaspatil.easyupipayment.model.TransactionDetails;

import java.util.Objects;

public class BottomPayDialog extends BottomSheetDialogFragment implements PaymentStatusListener {

    String payeeVpa;
    private TextView statusView;
    Button pay;
//    PaymentApp paymentApp;
    private EasyUpiPayment easyUpiPayment;
    ImageView imageView;
    private EditText amountEt;

    public BottomPayDialog(String upiid ) {
        this.payeeVpa = upiid;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v= inflater.inflate(R.layout.bottom_pay_dialog, container, false);
        Objects.requireNonNull(Objects.requireNonNull(getDialog()).getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        ImageView dismiss = v.findViewById(R.id.dismissflame);
//        radioAppChoice = v.findViewById(R.id.radioAppChoice);
        statusView = v.findViewById(R.id.textView_status);
        pay = v.findViewById(R.id.button_pay);
        imageView = v.findViewById(R.id.imageView);
        amountEt = v.findViewById(R.id.amount);
//        paymentAppChoice = v.findViewById(radioAppChoice.getCheckedRadioButtonId());

        dismiss.setOnClickListener(v1 -> super.onDestroyView());
        pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                payAmount();
            }
        });
        return v;
    }

    void payAmount()
    {
        try {
            if(amountEt.getText().toString().trim()!=null && !amountEt.getText().toString().trim().isEmpty()
                && amountEt.getText().toString().contains(".")){
                easyUpiPayment = new EasyUpiPayment.Builder(getActivity())
                        .with()
                        .setPayeeVpa(payeeVpa)
                        .setPayeeName("Utsav")
                        .setTransactionId("TID" + System.currentTimeMillis())
                        .setTransactionRefId("TID" + System.currentTimeMillis())
                        .setDescription("e-Pronami")
                        .setAmount(amountEt.getText().toString())
                        .build();
            }
            else
            {
                toast("Please enter amount in the given format");
            }

        } catch (AppNotFoundException e) {
            e.printStackTrace();
        }

            // Register Listener for Events
            easyUpiPayment.setPaymentStatusListener(this);

            // Start payment / transaction
            easyUpiPayment.startPayment();


    }

    @Override
    public void onTransactionCompleted(TransactionDetails transactionDetails) {
        // Transaction Completed
        Log.d("TransactionDetails", transactionDetails.toString());
        statusView.setText(transactionDetails.toString());

        switch (transactionDetails.getTransactionStatus()) {
            case SUCCESS:
                onTransactionSuccess();
                break;
            case FAILURE:
                onTransactionFailed();
                break;
            case SUBMITTED:
                onTransactionSubmitted();
                break;
        }
    }
    @Override
    public void onTransactionCancelled() {
        // Payment Cancelled by User
        Toast.makeText(requireActivity(), "Cancelled", Toast.LENGTH_SHORT).show();
    }

    private void onTransactionSuccess() {
        // Payment Success
        toast("Success");
        imageView.setImageResource(R.drawable.ic_success);
    }

    private void onTransactionSubmitted() {
        // Payment Pending
        toast("Pending | Submitted");
        imageView.setImageResource(R.drawable.ic_success);
    }

    private void onTransactionFailed() {
        // Payment Failed
        toast("Failed");
        imageView.setImageResource(R.drawable.ic_failed);
    }

    private void toast(String message) {
        Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show();
    }


}
