package cn.gdut.android.everyday.ui.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import cn.gdut.android.everyday.R;


/**
 * Created by denghewen on 2018/5/4 0004.
 */

public class ConfirmCreateFolderDialog implements View.OnClickListener {
    private Context mContext;
    private Dialog mDialog;
    private View mContentView;

    private TextView mTitleView;
    private EditText mMessageView;
    private Button mConfirmButton;

    private String mTitle;
    private String mMessage;
    private String mInputMessage;

    private OnConfirmListener mOnConfirmListener;

    public ConfirmCreateFolderDialog(Context context) {
        mContext = context;
        initDialog();
        initContentView();
        mDialog.setContentView(mContentView);
    }

    @SuppressLint("InflateParams")
    private void initContentView() {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        mContentView = inflater.inflate(R.layout.dialog_confirm, null);

        mTitleView = (TextView) mContentView.findViewById(R.id.confirm_title);
        mMessageView = (EditText) mContentView.findViewById(R.id.etInputText);
        mConfirmButton = mContentView.findViewById(R.id.confirm_ok);
        mMessageView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mInputMessage = mMessageView.getText().toString();
                if (TextUtils.isEmpty(mInputMessage)) {
                    mConfirmButton.setTextColor(mContext.getResources().getColor(R.color.colorAccentTint));
                    mConfirmButton.setClickable(false);
                } else {
                    mConfirmButton.setTextColor(mContext.getResources().getColor(R.color.colorAccent));
                    mConfirmButton.setClickable(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mContentView.findViewById(R.id.confirm_ok).setOnClickListener(this);
        mContentView.findViewById(R.id.confirm_cancel).setOnClickListener(this);
    }

    private void initDialog() {
        mDialog = new Dialog(mContext, R.style.WrapContentDialog);
        mDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION);
    }

    public ConfirmCreateFolderDialog show() {
        if (mDialog != null && !mDialog.isShowing()) {
            mDialog.show();
        }
        return this;
    }

    public ConfirmCreateFolderDialog setTitle(String title) {
        mTitle = title;
        updateTitle();
        return this;
    }

    public ConfirmCreateFolderDialog setMessage(String message) {
        mMessage = message;
        updateMessage();
        return this;
    }

    private void updateUI() {
        updateTitle();
        updateMessage();
    }

    private void updateMessage() {
        if (mMessage != null) {
            mMessageView.setText(mMessage);
        }
    }

    private void updateTitle() {
        if (mTitle != null) {
            mTitleView.setText(mTitle);
        }
    }

    public ConfirmCreateFolderDialog setOnConfirmListener(OnConfirmListener listener) {
        mOnConfirmListener = listener;
        return this;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.confirm_ok:
                onOk();
                break;
            case R.id.confirm_cancel:
                onCancel();
                break;
            default:
                break;
        }
    }

    private void onOk() {
        if (mOnConfirmListener != null) {
            mOnConfirmListener.onOk(mInputMessage);
        }
        dismiss();
    }

    private void onCancel() {
        if (mOnConfirmListener != null) {
            mOnConfirmListener.onCancel();
        }
        dismiss();
    }

    public interface OnConfirmListener {
        void onCancel();

        void onOk(String inputMessage);
    }

    public void dismiss() {
        if (mDialog != null) {
            mDialog.dismiss();
        }
        mOnConfirmListener = null;
    }
}

