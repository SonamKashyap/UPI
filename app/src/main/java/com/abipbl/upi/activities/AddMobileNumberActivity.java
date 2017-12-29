package com.abipbl.upi.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import com.abipbl.upi.util.Logger;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;

import com.abipbl.upi.R;
import com.abipbl.upi.adapter.AdapterForOperator;
import com.abipbl.upi.api.MErrorResponse;
import com.abipbl.upi.api.MJsonObjectRequest;
import com.abipbl.upi.api.MResponse;
import com.abipbl.upi.api.MySingleton;
import com.abipbl.upi.api.PreparePlatware;
import com.abipbl.upi.database.DataConnection;
import com.abipbl.upi.pojo.BillerUserMasterBean;
import com.abipbl.upi.pojo.ContactsBean;
import com.abipbl.upi.pojo.OperatorBean;
import com.abipbl.upi.util.AppConstant;
import com.abipbl.upi.util.AppUtil;
import com.abipbl.upi.util.BaseActivity;
import com.abipbl.upi.util.MUtil;
import com.abipbl.upi.widget.DotProgressBar;
import com.abipbl.upi.widget.MyEditText;
import com.abipbl.upi.widget.MyTextView;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class AddMobileNumberActivity extends BaseActivity implements TextWatcher {

    private static String category;
    private AdapterForOperator adapter;
    private DotProgressBar progress;
    private ArrayList<String> circleList;
    private View vW_circle;
    private MyTextView tv_circle;
    private Spinner sp_circle;
    private HashMap<String, String> hashBillerCircle;
    private String circleName;
    private String handleId;
    private String regId;
    private MyTextView tvErrorMsg;
    private AppBarLayout appbar;
    private Toolbar toolbar;
    private ImageView ivBackButton;
    private MyTextView tvContactName;
    private MyEditText etMobileNo;
    private ImageView ivPhoneBook;
    private MyTextView etCurrentOperator;
    private CardView cvOperatorList;
    private RecyclerView rvOperatorList;
    private MyTextView etCircle;
    private MyTextView btProceed;

    public static void goTOPullPushActivity(Context context, String billerUserId, String category) {
        try {


            ArrayList<BillerUserMasterBean> billerIdList = DataConnection.getInstance(context).getAdderBillersListFromTable(category);
            int position = 0;
            try {
                if (billerUserId != null && !billerUserId.isEmpty()) {

                    for (int i = 0; i < billerIdList.size(); i++) {

                        if (billerIdList.get(i).getUSER_BILLER_ID().equalsIgnoreCase(billerUserId)) {
                            position = i;
                        }

                    }

                } else {
                    position = 0;
                }
            } catch (NumberFormatException e) {
                Logger.i("getIntentData position", "" + position);
                position = 0;
            } catch (Exception e) {
                e.printStackTrace();
            }


            Intent intent = new Intent(context, PushAndPullActivityBiller.class);
            intent.putExtra(context.getString(R.string.category), category);
            intent.putExtra(AppConstant.POSITION, String.valueOf(position));
            ((Activity) context).startActivityForResult(intent, AppConstant.INTENT_BILL_PAGE);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_mobile_number);
        try {
            category = getIntent().getStringExtra(getString(R.string.category));
            handleId = DataConnection.getInstance(AddMobileNumberActivity.this).getDataFromUserTable(AppUtil.KEY_HANDLER);
            regId = DataConnection.getInstance(AddMobileNumberActivity.this).getDataFromUserTable(AppUtil.KEY_REG_ID);
            findViews();
            setOnClick();
            ArrayList<OperatorBean> operatorBeanArrayList = DataConnection.getInstance(AddMobileNumberActivity.this).getOperatorAll(AddMobileNumberActivity.this);
            setAdapter(operatorBeanArrayList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setOnClick() {
        etCurrentOperator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MUtil.hideKeyboard(AddMobileNumberActivity.this, v);
                if (cvOperatorList.getVisibility() == View.VISIBLE) {
                    cvOperatorList.setVisibility(View.GONE);
                } else {
                    cvOperatorList.setVisibility(View.VISIBLE);
                }
            }
        });
        ivBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddMobileNumberActivity.super.onBackPressed();
            }
        });

        ivPhoneBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddMobileNumberActivity.this, ContactList.class);
                intent.putExtra(AppConstant.FROM, AppConstant.ADD_MOBILE);
                AddMobileNumberActivity.this.startActivityForResult(intent, AppConstant.INTENT_ADD_MOBILE);
            }
        });
        etMobileNo.addTextChangedListener(this);
        btProceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<String> authenticator = new ArrayList<String>();
                ArrayList<String> authenticatorLabel = new ArrayList<String>();
                authenticator.add(etMobileNo.getText().toString().trim());
                authenticatorLabel.add("Mobile No");
                authenticatorLabel.add("Current Operator");
                authenticatorLabel.add("Select Circle");

                if (hashBillerCircle.containsKey(etCurrentOperator.getText().toString())) {
                    authenticator.add(hashBillerCircle.get(etCurrentOperator.getText().toString()));
                } else {
                    authenticator.add(DataConnection.getInstance(AddMobileNumberActivity.this).getCode(AddMobileNumberActivity.this, etCurrentOperator.getText().toString()));
                }

                if (null == sp_circle.getSelectedItem()) {
                    authenticator.add(hashBillerCircle.get(circleName));
                } else {
                    if (hashBillerCircle.containsKey(sp_circle.getSelectedItem().toString())) {
                        authenticator.add(hashBillerCircle.get(sp_circle.getSelectedItem().toString()));
                    } else {
                        authenticator.add(DataConnection.getInstance(AddMobileNumberActivity.this).getCode(AddMobileNumberActivity.this, sp_circle.getSelectedItem().toString()));
                    }
                }
                registerBiller(authenticatorLabel, authenticator);
            }
        });
    }

    private void setAdapter(ArrayList<OperatorBean> arrayList) {

        rvOperatorList.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 2);
        rvOperatorList.setLayoutManager(layoutManager);
        adapter = new AdapterForOperator(this, arrayList);
        rvOperatorList.setItemViewCacheSize(adapter.getItemCount());
        rvOperatorList.setAdapter(adapter);
    }

    /**
     * Find the Views in the layout<br />
     * <br />
     */
    private void findViews() {
        appbar = (AppBarLayout) findViewById(R.id.appbar);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        ivBackButton = (ImageView) findViewById(R.id.iv_backButton);
        tvContactName = (MyTextView) findViewById(R.id.tv_contact_name);
        etMobileNo = (MyEditText) findViewById(R.id.et_mobile_no);
        ivPhoneBook = (ImageView) findViewById(R.id.iv_phone_book);
        etCurrentOperator = (MyTextView) findViewById(R.id.et_current_operator);
        cvOperatorList = (CardView) findViewById(R.id.cv_operator_list);
        rvOperatorList = (RecyclerView) findViewById(R.id.rv_operatorList);
        sp_circle = (Spinner) findViewById(R.id.sp_circle);
        btProceed = (MyTextView) findViewById(R.id.bt_proceed);
        progress = (DotProgressBar) findViewById(R.id.progress);
        vW_circle = (View) findViewById(R.id.vW_circle);
        tv_circle = (MyTextView) findViewById(R.id.tv_circle);
        hashBillerCircle = new HashMap<String, String>();
        tvErrorMsg = (MyTextView) findViewById(R.id.tv_error_msg);
    }

    @Override
    public void onBackPressed() {
        if (cvOperatorList.getVisibility() == View.VISIBLE) {
            cvOperatorList.setVisibility(View.GONE);
            return;
        }

        super.onBackPressed();
    }

    public void onOperatorSelect(String operator, int adapterPosition) {
        etCurrentOperator.setText(operator);
        if (cvOperatorList.getVisibility() == View.VISIBLE) {
            cvOperatorList.setVisibility(View.GONE);
        }
        vW_circle.setVisibility(View.VISIBLE);
        tv_circle.setVisibility(View.VISIBLE);
        sp_circle.setVisibility(View.VISIBLE);

        circleList = DataConnection.getInstance(AddMobileNumberActivity.this).getCircle(AddMobileNumberActivity.this);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, R.layout.list_item_spinner, circleList); //selected item will look like a spinner set from XML
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_circle.setAdapter(spinnerArrayAdapter);
        sp_circle.performClick();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AppConstant.INTENT_ADD_MOBILE && resultCode == RESULT_OK) {

            ContactsBean contactsBean = (ContactsBean) data.getSerializableExtra(AppConstant.DATA);

            if (null != contactsBean && null != contactsBean.getPhoneNo()) {
                etMobileNo.setText(contactsBean.getPhoneNo());
            }

        }
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
        // Toast.makeText(AddMobileNumberActivity.this,"charSequence"+charSequence+"  start"+start+"  count"+count+"  after"+after,Toast.LENGTH_LONG).show();
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
        if (start < 5 && charSequence.length() >= 5) {
            getOperator();
        }
        //Toast.makeText(AddMobileNumberActivity.this,"charSequence"+charSequence+"  start"+start+"  count"+count+"  before"+before,Toast.LENGTH_LONG).show();
       /* if (charSequence.length() == 10) {
            getOperator();
        }*/

    }

    @Override
    public void afterTextChanged(Editable editable) {


    }

    private void getOperator() {
        /*progress.setVisibility(View.VISIBLE);*/
        final String processId = "GETMOBILEOPERATOR";
        JSONArray dataArr = new JSONArray();
        try {
            dataArr.put(new JSONObject().put("mobile_no", etMobileNo.getText().toString().trim()));
        } catch (JSONException e) {
            e.printStackTrace();
        }


        JSONObject jsonObject = PreparePlatware.preparePlatware(AddMobileNumberActivity.this, processId, dataArr, true);
        JsonObjectRequest jsonObjectRequest = new MJsonObjectRequest(
                jsonObject, new MResponse(AddMobileNumberActivity.this, processId, MUtil.getRandomTransactionId(AddMobileNumberActivity.this)) {


            @Override
            public void onMResponse(String response, String processId) {
                /*progress.setVisibility(View.GONE);*/
                String data = PreparePlatware.parseForDataList(AddMobileNumberActivity.this, response, processId);
                try {
                    JSONArray jsonArray = new JSONArray(data);
                    JSONObject jsonObject1 = jsonArray.getJSONObject(0);


                    String billerName = jsonObject1.getString("BILLER_NAME");
                    String circleName = jsonObject1.getString("CIRCLE_NAME");
                    String billerId = jsonObject1.getString("BILLER_ID");
                    String circleCode = jsonObject1.getString("CIRCLE_CODE");

                    hashBillerCircle.put(billerName, billerId);
                    hashBillerCircle.put(circleName, circleCode);

                    etCurrentOperator.setText(billerName);
                    vW_circle.setVisibility(View.GONE);
                    tv_circle.setVisibility(View.GONE);
                    sp_circle.setVisibility(View.GONE);

                    circleList = DataConnection.getInstance(AddMobileNumberActivity.this).getCircle(AddMobileNumberActivity.this);
                    ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(AddMobileNumberActivity.this, R.layout.list_item_spinner, circleList); //selected item will look like a spinner set from XML
                    spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    sp_circle.setAdapter(spinnerArrayAdapter);


                    if (circleList.contains(circleName)) {
                        sp_circle.setSelection(circleList.indexOf(circleName));
                    } else {
                        AddMobileNumberActivity.this.circleName = circleName;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }

            @Override
            public void onPWErrorResponse(String response) {
               /* progress.setVisibility(View.GONE);*/

            }
        }, new MErrorResponse(AddMobileNumberActivity.this) {

            @Override
            public void onMErrorResponse(VolleyError error) {
                /*progress.setVisibility(View.GONE);*/
            }
        },AddMobileNumberActivity.this) {

        };

        MySingleton.getInstance(AddMobileNumberActivity.this).addToRequestQueue(jsonObjectRequest);

    }

    public void registerBiller(ArrayList<String> authenticatorLabel, final ArrayList<String> authenticators) {

        tvErrorMsg.setVisibility(View.GONE);

        final String processId = "REGISTERBILLERV2";

        String authenticatorLebel = android.text.TextUtils.join("~", authenticatorLabel);
        String autheticatorValue = android.text.TextUtils.join("!", authenticators);

        final BillerUserMasterBean billerUserMasterBean = new BillerUserMasterBean();

        billerUserMasterBean.setHANDLE_ID(handleId);
        billerUserMasterBean.setREGISTERATION_ID(regId);
        billerUserMasterBean.setBILLER_ID(authenticators.get(1));
        billerUserMasterBean.setAUTHENTICATOR_LABEL(authenticatorLebel);
        billerUserMasterBean.setAUTHENTICATORS_VALUE(autheticatorValue);
        billerUserMasterBean.setMERCHANT_HANDLER(AppUtil.getBillerMetaData(this).get(category).get(0).getMerchantHandler());
        billerUserMasterBean.setCATEGORY(category);

        //TODO change autheticator value to index 1
        billerUserMasterBean.setFIRST_AUTHENTIC_VALUE(authenticators.get(0));

        billerUserMasterBean.setPAYMENT_ALLOWED_POST_DUE_DATE("");
        billerUserMasterBean.setPARTIAL_PAYMENT_ALLOWED("");
        billerUserMasterBean.setINSTAA_PAY("");

        billerUserMasterBean.setPARAM1("Y");


        try {
            billerUserMasterBean.setCIRCLE_ID(authenticators.get(2));
            billerUserMasterBean.setOPERATER_ID(authenticators.get(1));
        } catch (Exception e) {
            e.printStackTrace();
        }

        JSONArray dataArr = new JSONArray();
        MUtil.showProgressDialog(AddMobileNumberActivity.this, AddMobileNumberActivity.this.getSupportFragmentManager().beginTransaction());
        try {
            JSONObject dataObj = new JSONObject();

            JSONObject inputJson = new JSONObject();

            inputJson.put("handleId", billerUserMasterBean.getHANDLE_ID());
            inputJson.put("RegisterationId", billerUserMasterBean.getREGISTERATION_ID());   //   registeration_id
            inputJson.put("billerId", billerUserMasterBean.getBILLER_ID());
            inputJson.put("authenticatorLabel", billerUserMasterBean.getAUTHENTICATOR_LABEL());
            inputJson.put("authenticator", billerUserMasterBean.getAUTHENTICATORS_VALUE());
            inputJson.put("merchantHandler", billerUserMasterBean.getMERCHANT_HANDLER());
            inputJson.put("merchantCategory", billerUserMasterBean.getCATEGORY());
            inputJson.put("payAfterDueDate", billerUserMasterBean.getPAYMENT_ALLOWED_POST_DUE_DATE());

            inputJson.put("partialPayAllowed", billerUserMasterBean.getPARTIAL_PAYMENT_ALLOWED());
            inputJson.put("circleId", billerUserMasterBean.getCIRCLE_ID());
            inputJson.put("OperatorId", billerUserMasterBean.getOPERATER_ID());
            inputJson.put("instaaPay", billerUserMasterBean.getINSTAA_PAY());
            inputJson.put("firstAuthenticatorValue", billerUserMasterBean.getFIRST_AUTHENTIC_VALUE());

            inputJson.put("amountCaptureOnFrontend", billerUserMasterBean.getPARAM1());

            inputJson.put("isBillFetchRequired", "N");

            dataObj.put("input_json", inputJson.toString());

            dataArr.put(dataObj);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        JSONObject jsonObject = PreparePlatware.preparePlatware(AddMobileNumberActivity.this, processId, dataArr, true);

        JsonObjectRequest jsonObjectRequest = new MJsonObjectRequest(
                jsonObject, new MResponse(AddMobileNumberActivity.this, processId, MUtil.getRandomTransactionId(AddMobileNumberActivity.this)) {

            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onMResponse(String response, String processId) {
                MUtil.dismissProgressDialog();
                String data = PreparePlatware.parseForData(AddMobileNumberActivity.this, response, processId);
                try {
                    JSONObject jsonObject = new JSONObject(data);
                    if (AppUtil.getStatus(jsonObject).equalsIgnoreCase(AppUtil.SUCCESS)) {

                        JSONObject infoJson = new JSONObject(AppUtil.getInfo(jsonObject));

                        String shouldSyncBill = infoJson.optString("IS_BILL_SYNC_REQUIRED", "");
                        String accountId = infoJson.optString("BILLER_ACCOUNT_ID", "");
                        String billerUserId = infoJson.optString("BILLER_USER_RECORD_ID", "");

                        billerUserMasterBean.setBILLER_ACCOUNT_ID(accountId);
                        billerUserMasterBean.setUSER_BILLER_ID(billerUserId);

                        DataConnection.getInstance(AddMobileNumberActivity.this).insertDataToBillerUserMaster(billerUserMasterBean);
                        openChatPage(billerUserMasterBean);
                    } else {
                        String info = jsonObject.getString("info");
                        tvErrorMsg.setText(info);
                        tvErrorMsg.setVisibility(View.VISIBLE);
                        AppUtil.eventsTrackUtilityReg(AddMobileNumberActivity.this, category, authenticators.get(1), false);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    tvErrorMsg.setText(getString(R.string.genric_error_msg));
                    tvErrorMsg.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPWErrorResponse(String response) {
                MUtil.dismissProgressDialog();
                tvErrorMsg.setText(response);
                tvErrorMsg.setVisibility(View.VISIBLE);


            }
        }, new MErrorResponse(AddMobileNumberActivity.this) {

            @Override
            public void onMErrorResponse(VolleyError error) {
                MUtil.dismissProgressDialog();
                tvErrorMsg.setText(error.getMessage());
                tvErrorMsg.setVisibility(View.VISIBLE);
            }
        }, this) {

        };

        MySingleton.getInstance(AddMobileNumberActivity.this).addToRequestQueue(jsonObjectRequest);

    }

    private void openChatPage(BillerUserMasterBean billerUserMasterBean) {
        AppUtil.eventsTrackUtilityReg(this, category, billerUserMasterBean.getBILLER_ID(), true);
        goTOPullPushActivity(AddMobileNumberActivity.this, billerUserMasterBean.getUSER_BILLER_ID(), category);
        setResult(RESULT_OK);
        finish();
    }

}



 /**/