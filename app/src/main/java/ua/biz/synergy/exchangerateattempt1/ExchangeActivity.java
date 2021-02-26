package ua.biz.synergy.exchangerateattempt1;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.ArrayMap;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static android.content.Intent.FLAG_ACTIVITY_NEW_DOCUMENT;
import static android.content.pm.PackageManager.GET_ACTIVITIES;

/**
 * class ExchangeActivity
 * Downloading data from a bank's website in JSON format that contains currency rate.
 * Parsing of a Json string which contain an exchange rate for the currency.
 * A user can do a calculation with currency rate value.
 * A user can invoke an external web browser and may be other outside resources
 */

public class ExchangeActivity extends AppCompatActivity implements View.OnClickListener,
        View.OnLongClickListener {
    
    private static final String TAG = ExchangeActivity.class.getCanonicalName();

    
    private final Context contextExchangeActivity = ExchangeActivity.this;
    
    // Flag for avoiding trigger overlapping downloads that causing when pressing the
    // "refresh currency rate" button frequently
    // set permission for start first downloading at the netOperationThread
    boolean isDownloading = true;
    private ProgressDialog progressDialog = null;
    private ProgressBar progressBarCircle = null;
    // Url for download a Json file
    private String httpExchangeJson;
    //handler for main activity looper
    private Handler handler;
    
    private TextView textViewTitle$Sale;
    private TextView textViewTitle$Buy;
    private TextView textViewTitleEuroSale;
    private TextView textViewTitleEuroBuy;
    private TextView textView$SaleExchngeRate;
    private TextView textView$BuyExchngeRate;
    private TextView textViewEuroSaleExchngeRate;
    private TextView textViewEuroBuyExchngeRate;
    private TextView textViewResult;
    private Button buttonGetExchangeRate;
    private View layoutCustomToast;
    private TextView textViewCustomToast;
    private ImageView imageViewCustomToast;
    private SwipeRefreshLayout swipeRefreshLayout;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exchange);
        
        initView();
        initSwipeRefresh();
        initCustomToast();
        //Setting the Url for download a data of a currency exchange rate in format JSON string
        setCurrencyRateUrl(this.getString(R.string.http_PvBank_exchange_JSON_file));
        
        handler = new Handler();

        Log.i(TAG, "onCreate is running");

        //Launching net-operation in the local thread for getting the currency rate
        netOperationThread();
    }
    
    /**
     * Initialization of the View for MainActivity's UI
     */
    private void initView() {
        ImageButton imageActionButton;
        ImageButton imageCalcButton;
        ImageButton imageButtonSaleDollarCalc;
        ImageButton imageButtonBuyDollarCalc;
        ImageButton imageButtonSaleEuroCalc;
        ImageButton imageButtonBuyEuroCalc;
        progressBarCircle = (ProgressBar) findViewById(R.id.progressBarCircle);
        textViewTitle$Sale = (TextView) findViewById(R.id.textViewTitleExchRateSaleDollar);
        textViewTitle$Buy = (TextView) findViewById(R.id.textViewTitleExchBuyDollar);
        textViewTitleEuroSale = (TextView) findViewById(R.id.textViewTitleExchSaleEuro);
        textViewTitleEuroBuy = (TextView) findViewById(R.id.textViewTitleExchBuyEuro);
        textView$SaleExchngeRate = (TextView) findViewById(R.id.textViewExchangeRateSaleDollar);
        textView$BuyExchngeRate = (TextView) findViewById(R.id.textViewExchangeBuyDollar);
        textViewEuroSaleExchngeRate = (TextView) findViewById(R.id.textViewExchangeSaleEuro);
        textViewEuroBuyExchngeRate = (TextView) findViewById(R.id.textViewExchangeBuyEuro);
        textViewResult = (TextView) findViewById(R.id.textViewResultValue);
        buttonGetExchangeRate = (Button) findViewById(R.id.buttonExchangeRate);
        imageActionButton = (ImageButton) findViewById(R.id.imageActionButton);
        imageCalcButton = (ImageButton) findViewById(R.id.imageCalcButton);
        imageButtonSaleDollarCalc = (ImageButton) findViewById(R.id.imageButtonSaleDollarCalculate);
        imageButtonBuyDollarCalc = (ImageButton) findViewById(R.id.imageButtonBuyDollarCalculate);
        imageButtonSaleEuroCalc = (ImageButton) findViewById(R.id.imageButtonSaleEuroCalculate);
        imageButtonBuyEuroCalc = (ImageButton) findViewById(R.id.imageButtonBuyEuroCalculate);
        buttonGetExchangeRate.setOnClickListener(this);
        buttonGetExchangeRate.setOnLongClickListener(this);
        imageActionButton.setOnClickListener(this);
        imageActionButton.setOnLongClickListener(this);
        imageCalcButton.setOnClickListener(this);
        imageCalcButton.setOnLongClickListener(this);
        imageButtonSaleDollarCalc.setOnClickListener(this);
        imageButtonSaleDollarCalc.setOnLongClickListener(this);
        imageButtonBuyDollarCalc.setOnClickListener(this);
        imageButtonBuyDollarCalc.setOnLongClickListener(this);
        imageButtonSaleEuroCalc.setOnClickListener(this);
        imageButtonSaleEuroCalc.setOnLongClickListener(this);
        imageButtonBuyEuroCalc.setOnClickListener(this);
        imageButtonBuyEuroCalc.setOnLongClickListener(this);
        // set text at the title fields of UI
        textViewTitle$Sale.setText(getString(R.string.title_Selling).concat(getString(R.string.sign_DOLLAR)));
        textViewTitle$Buy.setText(getString(R.string.title_Purchase).concat(getString(R.string.sign_DOLLAR)));
        textViewTitleEuroSale.setText(getString(R.string.title_Selling).concat(getString(R.string.sign_EURO)));
        textViewTitleEuroBuy.setText(getString(R.string.title_Purchase).concat(getString(R.string.sign_EURO)));
    }
    
    /**
     * initializing of the custom Toast
     */
    private void initCustomToast() {
        // For the custom layout of Toast:
        LayoutInflater inflater = getLayoutInflater();
        layoutCustomToast = inflater.inflate(R.layout.custom_refresh_toast, (ViewGroup) findViewById(R.id.custom_refresh_toast_container));
        textViewCustomToast = (TextView) layoutCustomToast.findViewById(R.id.textView_custom_Refresh_Toast);
        imageViewCustomToast = (ImageView) layoutCustomToast.findViewById(R.id.imageView_custom_Refresh_Toast);
    }
    
    /**
     *Initialise of the SwipeRefreshLayout for refresh data
     */
    private void initSwipeRefresh() {
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        Log.i(TAG, "swipeRefreshLayout = " + swipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            /**
             * implemented callback method from SwipeRefreshLayout.OnRefreshListener
             * Called when a swipe gesture triggers a refresh.
             *
             * */
            public void onRefresh() {
                //Launching net-operation in the local thread for getting the currency rate
                netOperationThread();
                // Indicating that refresh is completed
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }
    
    /**
     *Setting the Url for download a data of a currency exchange rate in format JSON string
     * @param url Url of a web resource that contains a data of a currency exchange rate
     */
    private void setCurrencyRateUrl(String url){

        httpExchangeJson = this.getString(R.string.http_PvBank_exchange_JSON_file);
    }
    
    /**
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    /**
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    /**
     *
     * @param v type View
     */
    public void onClick(View v) {
        int flagCurrencyType = 0;
        switch (v.getId()) {
            case R.id.buttonExchangeRate: {
                Log.i(TAG, "onClick run");
                buttonGetExchangeRate.setVisibility(View.INVISIBLE);
                if (isDownloading) {
                    if (progressBarCircle != null) {                 //invoke Progress Bar Circle
                        progressBarCircle.setVisibility(View.VISIBLE);
                    }
                    //showProgressDialog(getString(R.string.progress_dialog_working_title), getString(R.string.progress_dialog_working_message));
                    netOperationThread();
                    // Indicating that downloading is completed
                    isDownloading = false;
                }
                break;
            }
            case R.id.imageActionButton: {
                // Call EXTERNAL Web Browser implicitly
                startOutsideAction(1);
                // Test PackageManager, PackageInfo, ActivityInfo
                //startOutsideAction(2);
                break;
            }
            case R.id.imageCalcButton: {
                //!!! NOT WORKING !!! Call an outside calculator implicitly through setting an Action and Category
                //startCalculateAction(0);
                
                // Call an outside calculator implicitly through calling an Intent.makeMainSelectorActivity()
                // for search a pending activity for intent and startActivity()
                startCalculateAction(1);
                // Call outside calculator explicitly through use the package name and the qualified name of the Activity of GOOGLE Calculator
                //startCalculateAction(2);
                break;
            }
            case R.id.imageButtonSaleDollarCalculate: { // Call INTERNAL CalcActivity explicitly for calculating buy of a dollar
                String currencyName = getString(R.string.sign_DOLLAR);
                Intent intentCalcActivity = new Intent(this, CalcActivity.class);
                // sending a label text for a calculator
                intentCalcActivity.putExtra(CalcActivity.EXTRA_MESSAGE, getString(R.string.message_About_Action_that_you_must_do));
                // sending a title text for a currency rate field
                intentCalcActivity.putExtra(CalcActivity.EXTRA_MESSAGE_RATE_ME_BUY_OR_SALE_CURRENCY, getString(R.string.title_Selling));
                // sending a title text for a currency amount field
                intentCalcActivity.putExtra(CalcActivity.EXTRA_MESSAGE_ME_AMOUNT_OF_CURRENCY, getString(R.string.hint_field_title_amount_currency));
                // sending a title text for a sum fild
                intentCalcActivity.putExtra(CalcActivity.EXTRA_MESSAGE_ME_SUM, getString(R.string.hint_field_title_sum));
                // sending a value of a currency rate
                intentCalcActivity.putExtra(CalcActivity.EXTRA_DATA_ME_BUY_OR_SALE_CURRENCY_RATE_DOUBLE, convertStringToDouble(this.textView$SaleExchngeRate.getText().toString()));
                // setting a currency name sign
                intentCalcActivity.putExtra(CalcActivity.EXTRA_DATA_CURRENCY_NAME_STRING, getString(R.string.sign_DOLLAR));
                // setting my money sign
                intentCalcActivity.putExtra(CalcActivity.EXTRA_DATA_MONEY_NAME_STRING, getString(R.string.sign_My_MONEY));
                //intentCalcActivity.addFlags(FLAG_ACTIVITY_NEW_DOCUMENT);
                startActivityForResult(intentCalcActivity, CalcActivity.CODE_REQUEST_FOR_START_CALCACTIVITY);
                break;
            }
            case R.id.imageButtonBuyDollarCalculate: { // Call INTERNAL CalcActivity explicitly for calculating sale of a dollar
                Intent intentCalcActivity = new Intent(this, CalcActivity.class);
                intentCalcActivity.putExtra(CalcActivity.EXTRA_MESSAGE, getString(R.string.message_About_Action_that_you_must_do));
                intentCalcActivity.putExtra(CalcActivity.EXTRA_MESSAGE_RATE_ME_BUY_OR_SALE_CURRENCY, getString(R.string.title_Purchase));
                intentCalcActivity.putExtra(CalcActivity.EXTRA_MESSAGE_ME_AMOUNT_OF_CURRENCY, getString(R.string.hint_field_title_amount_currency));
                intentCalcActivity.putExtra(CalcActivity.EXTRA_MESSAGE_ME_SUM, getString(R.string.hint_field_title_sum));
                // sending a value of a currency rate
                intentCalcActivity.putExtra(CalcActivity.EXTRA_DATA_ME_BUY_OR_SALE_CURRENCY_RATE_DOUBLE, convertStringToDouble(this.textView$BuyExchngeRate.getText().toString()));
                intentCalcActivity.putExtra(CalcActivity.EXTRA_DATA_CURRENCY_NAME_STRING, getString(R.string.sign_DOLLAR));
                intentCalcActivity.putExtra(CalcActivity.EXTRA_DATA_MONEY_NAME_STRING, getString(R.string.sign_My_MONEY));
                //intentCalcActivity.addFlags(FLAG_ACTIVITY_NEW_DOCUMENT);
                startActivityForResult(intentCalcActivity, CalcActivity.CODE_REQUEST_FOR_START_CALCACTIVITY);
                break;
            }
            case R.id.imageButtonSaleEuroCalculate: { // Call INTERNAL CalcActivity explicitly for calculating buy of a EURO
                Intent intentCalcActivity = new Intent(this, CalcActivity.class);
                intentCalcActivity.putExtra(CalcActivity.EXTRA_MESSAGE, getString(R.string.message_About_Action_that_you_must_do));
                intentCalcActivity.putExtra(CalcActivity.EXTRA_MESSAGE_RATE_ME_BUY_OR_SALE_CURRENCY, getString(R.string.title_Selling));
                intentCalcActivity.putExtra(CalcActivity.EXTRA_MESSAGE_ME_AMOUNT_OF_CURRENCY, getString(R.string.hint_field_title_amount_currency));
                intentCalcActivity.putExtra(CalcActivity.EXTRA_MESSAGE_ME_SUM, getString(R.string.hint_field_title_sum));
                // sending a value of a currency rate
                intentCalcActivity.putExtra(CalcActivity.EXTRA_DATA_ME_BUY_OR_SALE_CURRENCY_RATE_DOUBLE, convertStringToDouble(this.textViewEuroSaleExchngeRate.getText().toString()));
                intentCalcActivity.putExtra(CalcActivity.EXTRA_DATA_CURRENCY_NAME_STRING, getString(R.string.sign_EURO));
                intentCalcActivity.putExtra(CalcActivity.EXTRA_DATA_MONEY_NAME_STRING, getString(R.string.sign_My_MONEY));
                //intentCalcActivity.addFlags(FLAG_ACTIVITY_NEW_DOCUMENT);
                startActivityForResult(intentCalcActivity, CalcActivity.CODE_REQUEST_FOR_START_CALCACTIVITY);
                break;
            }
            case R.id.imageButtonBuyEuroCalculate: { // Call INTERNAL CalcActivity explicitly for calculating sale of a EURO
                Intent intentCalcActivity = new Intent(this, CalcActivity.class);
                intentCalcActivity.putExtra(CalcActivity.EXTRA_MESSAGE, getString(R.string.message_About_Action_that_you_must_do));
                intentCalcActivity.putExtra(CalcActivity.EXTRA_MESSAGE_RATE_ME_BUY_OR_SALE_CURRENCY, getString(R.string.title_Purchase));
                intentCalcActivity.putExtra(CalcActivity.EXTRA_MESSAGE_ME_AMOUNT_OF_CURRENCY, getString(R.string.hint_field_title_amount_currency));
                intentCalcActivity.putExtra(CalcActivity.EXTRA_MESSAGE_ME_SUM, getString(R.string.hint_field_title_sum));
                // sending a value of a currency rate
                intentCalcActivity.putExtra(CalcActivity.EXTRA_DATA_ME_BUY_OR_SALE_CURRENCY_RATE_DOUBLE, convertStringToDouble(this.textViewEuroBuyExchngeRate.getText().toString()));
                intentCalcActivity.putExtra(CalcActivity.EXTRA_DATA_CURRENCY_NAME_STRING, getString(R.string.sign_EURO));
                intentCalcActivity.putExtra(CalcActivity.EXTRA_DATA_MONEY_NAME_STRING, getString(R.string.sign_My_MONEY));
                //intentCalcActivity.addFlags(FLAG_ACTIVITY_NEW_DOCUMENT);
                startActivityForResult(intentCalcActivity, CalcActivity.CODE_REQUEST_FOR_START_CALCACTIVITY);
                break;
            }
            default: {
                Toast.makeText(contextExchangeActivity, R.string.error_No_Action_for_this_Button, Toast.LENGTH_SHORT).show();
            }
        }
        
    }
    
    /**
     * Called when a view has been clicked and held.
     *
     * @param v The view that was clicked and held.
     * @return true if the callback consumed the long click, false otherwise.
     */
    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.buttonExchangeRate: {
                showCustomToastOneImageOneText(contextExchangeActivity, R.string.hint_buttonExchangeRate, R.drawable.ic_info_outline_48px, Toast.LENGTH_SHORT,
                        Gravity.CENTER_VERTICAL, 0, 0, layoutCustomToast, imageViewCustomToast, textViewCustomToast);
                //Toast.makeText(contextExchangeActivity, getString(R.string.hint_buttonExchangeRate), Toast.LENGTH_LONG).show();
                Log.i(TAG, getString(R.string.hint_buttonExchangeRate));
                break;
            }
            case R.id.imageActionButton: {
                showCustomToastOneImageOneText(contextExchangeActivity, R.string.hint_imageActionButton, R.drawable.ic_info_outline_48px, Toast.LENGTH_SHORT,
                        Gravity.CENTER_VERTICAL, 0, 0, layoutCustomToast, imageViewCustomToast, textViewCustomToast);
                //Toast.makeText(contextExchangeActivity, getString(R.string.hint_imageActionButton), Toast.LENGTH_LONG).show();
                Snackbar.make(v, R.string.hint_imageActionButton, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                Log.i(TAG, getString(R.string.hint_imageActionButton));
                break;
            }
            case R.id.imageCalcButton: {
                showCustomToastOneImageOneText(contextExchangeActivity, R.string.hint_imageCalcButton, R.drawable.ic_info_outline_48px, Toast.LENGTH_SHORT,
                        Gravity.CENTER_VERTICAL, 0, 0, layoutCustomToast, imageViewCustomToast, textViewCustomToast);
                //Toast.makeText(contextExchangeActivity, getString(R.string.hint_imageCalcButton), Toast.LENGTH_LONG).show();
                Log.i(TAG, getString(R.string.hint_imageCalcButton));
                break;
            }
            case R.id.imageButtonSaleDollarCalculate: {
                showCustomToastOneImageOneText(contextExchangeActivity, R.string.hint_imageButtonSaleDollarCalculate, R.drawable.ic_info_outline_48px, Toast.LENGTH_SHORT,
                        Gravity.CENTER_VERTICAL, 0, 0, layoutCustomToast, imageViewCustomToast, textViewCustomToast);
                //Toast.makeText(contextExchangeActivity, getString(R.string.hint_imageButtonSaleDollarCalculate), Toast.LENGTH_LONG).show();
                break;
            }
            case R.id.imageButtonBuyDollarCalculate: {
                showCustomToastOneImageOneText(contextExchangeActivity, R.string.hint_imageButtonBuyDollarCalculate, R.drawable.ic_info_outline_48px, Toast.LENGTH_SHORT,
                        Gravity.CENTER_VERTICAL, 0, 0, layoutCustomToast, imageViewCustomToast, textViewCustomToast);
                //Toast.makeText(contextExchangeActivity, getString(R.string.hint_imageButtonBuyDollarCalculate), Toast.LENGTH_LONG).show();
                break;
            }
            case R.id.imageButtonSaleEuroCalculate: {
                showCustomToastOneImageOneText(contextExchangeActivity, R.string.hint_imageButtonSaleEuroCalculate, R.drawable.ic_info_outline_48px, Toast.LENGTH_SHORT,
                        Gravity.CENTER_VERTICAL, 0, 0, layoutCustomToast, imageViewCustomToast, textViewCustomToast);
                //Toast.makeText(contextExchangeActivity, getString(R.string.hint_imageButtonSaleEuroCalculate), Toast.LENGTH_LONG).show();
                break;
            }
            case R.id.imageButtonBuyEuroCalculate: {
                showCustomToastOneImageOneText(contextExchangeActivity, R.string.hint_imageButtonBuyEuroCalculate, R.drawable.ic_info_outline_48px, Toast.LENGTH_SHORT,
                        Gravity.CENTER_VERTICAL, 0, 0, layoutCustomToast, imageViewCustomToast, textViewCustomToast);
                //Toast.makeText(contextExchangeActivity, getString(R.string.hint_imageButtonBuyEuroCalculate), Toast.LENGTH_LONG).show();
                break;
            }
            default: {
                Toast.makeText(contextExchangeActivity, R.string.hint_No_hint_for_this_button, Toast.LENGTH_SHORT).show();
            }
        }
        return true;
    }
    
    /**
     * Showing a Toast message that including a image and a text
     *
     * @param contextActivity
     * @param textResource    text for show
     * @param imageResource   image for show
     * @param toastDuration
     * @param gravity
     * @param xOffset
     * @param yOffset
     * @param customView      View that contain ImageView and TextView
     * @param customImageView
     * @param customTextView
     */
    public static void showCustomToastOneImageOneText(final Context contextActivity, final int textResource, final int imageResource, final int toastDuration,
                                                      final int gravity, final int xOffset, final int yOffset, final View customView,
                                                      final ImageView customImageView, final TextView customTextView) {
        
        Toast toastCustom = new Toast(contextActivity);
        toastCustom.setGravity(gravity, xOffset, yOffset);
        toastCustom.setDuration(toastDuration);
        toastCustom.setView(customView);
        customTextView.setText(textResource);
        customImageView.setImageResource(imageResource);
        toastCustom.show();
    }
    
    /**
     * @param strTitle   title for progress dialog
     * @param strMessage a message for the progress dialog
     */
    private void showProgressDialog(String strTitle, String strMessage) {
        /*
            ProgressDialog pd = new ProgressDialog(this);
            pd.setCancelable(true);
            pd.show(this, "Please wait", "working...");
             Like the first example, this will show a ProgressDialog which cannot be dismissed with the back button.
              Why? Because the show() method used here is a static method which returns a new ProgressDialog.
              The instance named pd is never shown. The compiler will show a warning that a static method is being
              invoked in a non-static way so you might get a clue that something is wrong;
              still, this is a really confusing API with no documentation. If you want to use the ProgressDialog
              constructor directly (as you must in order to subclass it), you must do this instead:
            !!! Note that the no-arg show() method used here is an instance method inherited from the Dialog class.
                     The resulting ProgressDialog may indeed be canceled.
                     Otherwise, if we use show(Context, String, String) it's static method ProgressDialog class
                       and it may be not cancelable.
        */
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(strTitle);
        progressDialog.setMessage(strMessage);
        progressDialog.setCancelable(true);
        progressDialog.show();
    }
    
    /**
     * @param variant
     */
    private void startOutsideAction(int variant) {
        switch (variant) {
            case 1: {// Call the Web Browser
                Uri expression = Uri.parse(this.getString(R.string.http_call_site_for_Pressed_Action_Button));
                Log.i(TAG, variant + ") expression.getEncodedAuthority() = " + expression.getEncodedAuthority());
                Intent intentWebSite = new Intent(Intent.ACTION_VIEW, expression);
                PackageManager packageManager = getPackageManager();
                List<ResolveInfo> activities = packageManager.queryIntentActivities(intentWebSite, 0);
                boolean isIntentSafe = activities.size() > 0;
                Log.i(TAG, "Is Resolved Activity? " + isIntentSafe);
                if (isIntentSafe) {
                    for (ResolveInfo act : activities) {
                        Log.i(TAG, variant + ") activities size = " + activities.size() + " List<ResolveInfo> = " +
                                " Class = " + act.activityInfo.name + "; Package = " + act.activityInfo.packageName);
                    }
                    
                    startActivity(intentWebSite);
                }
                break;
            }
            case 2: {// Test of PackageManager, PackageInfo, ActivityInfo
                Uri expression = Uri.parse("26.50");
                //Intent intentCalculator = new Intent(Intent.ACTION_MAIN,expression);
                
                Intent intentCalculator = Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_CALCULATOR);
                //?? intentCalculator.setDataAndType(expression, "float");
                PackageManager packageManager = getPackageManager();
                
                List<PackageInfo> packageInstalled = packageManager.getInstalledPackages(GET_ACTIVITIES);
                if (packageInstalled != null) {
                    Log.i(TAG, variant + ") List<PackageInfo> packageInstalled is not null");
                    Log.i(TAG, variant + ") packageInstalled.size() =" + packageInstalled.size());
                    Log.i(TAG, variant + ") ActivityInfo[0].toString = " + packageInstalled.get(0).activities[0].toString());
                    Log.i(TAG, variant + ") ActivityInfo[0].name = " + packageInstalled.get(0).activities[0].name);
                    Log.i(TAG, variant + ") ActivityInfo[0].packageName = " + packageInstalled.get(0).activities[0].packageName);
                } else {
                    Log.i(TAG, variant + ") ERROR: PackageInfo is null");
                }
                List<ResolveInfo> activities = packageManager.queryIntentActivities(intentCalculator, 0);
                boolean isIntentSafe = activities.size() > 0;
                Log.i(TAG, variant + ") Is Resolved Activity? " + isIntentSafe);
                if (isIntentSafe) {
                    for (ResolveInfo act : activities) { //list of the Activites that best handle this intent
                        Log.i(TAG, variant + ") activities size = " + activities.size() + " List<ResolveInfo> = " +
                                " Class = " + act.activityInfo.name + "; Package = " + act.activityInfo.packageName);
                    }
                    ComponentName componentName = new ComponentName(activities.get(0).activityInfo.packageName, activities.get(0).activityInfo.name);
                    Log.i(TAG, variant + ") ComponentName : Name = " + componentName.getClassName() + "; Package = " + componentName.getPackageName());
                    intentCalculator.setComponent(componentName);
                    
                    IntentFilter filterIntent = activities.get(0).filter;
                    if (filterIntent != null) {
                        Log.i(TAG, variant + ") IntentFilter: countCategories = " + filterIntent.countCategories() + "; countActions = " + filterIntent.countActions() +
                                "; countActions = " + filterIntent.countDataTypes());
                    } else {
                        Log.i(TAG, variant + ") IntentFilter: filter = null");
                    }
                    
                    startActivityForResult(intentCalculator, 1);
                }
                break;
            }
            
        }
    }
    
    private void startCalculateAction(int variant) {
        switch (variant) {
            case 0: {// !!! NOT WORKING !!! Call the Calculator through set directly Action and Category
                
                Intent intentCalculator = new Intent();
                intentCalculator.setAction(Intent.ACTION_MAIN);
                intentCalculator.addCategory(Intent.CATEGORY_APP_CALCULATOR);
                intentCalculator.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //Log.i(TAG, variant + ") intentCalculator.toString(): Action: " + intentCalculator.getAction() + "; Category: " + intentCalculator.getCategories());
                startActivity(intentCalculator);
                
                break;
            }
            
            case 1: {//  Call an outside calculator implicitly through calling an Intent.makeMainSelectorActivity() for search a pending activity for intent
                Uri expression = Uri.parse("data:26.0");
                Intent intentCalculator = null;
                intentCalculator = Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_CALCULATOR);
                //?? intentCalculator.setDataAndType(expression, "float");
                PackageManager packageManager = getPackageManager();
                
                List<ResolveInfo> activities = packageManager.queryIntentActivities(intentCalculator, 0);
                boolean isIntentSafe = activities.size() > 0;
                Log.i(TAG, variant + ") Is Resolved Activity? " + isIntentSafe);
                if (isIntentSafe) {
                    for (ResolveInfo act : activities) { //list of the Activities that best handle this intent
                        Log.i(TAG, variant + ") activities size = " + activities.size() + " List<ResolveInfo> = " +
                                " Class = " + act.activityInfo.name + "; Package = " + act.activityInfo.packageName);
                    }
                    ComponentName componentName = new ComponentName(activities.get(0).activityInfo.packageName, activities.get(0).activityInfo.name);
                    intentCalculator.setComponent(componentName);
                    
                    Log.i(TAG, variant + ") ComponentName : Name = " + componentName.getClassName() +
                            "; Package = " + componentName.getPackageName());
                    
                    IntentFilter filterIntent = activities.get(0).filter;
                    if (filterIntent != null) {
                        Log.i(TAG, variant + ") IntentFilter: countCategories = " + filterIntent.countCategories() +
                                "; countActions = " + filterIntent.countActions() +
                                "; countDataTypes = " + filterIntent.countDataTypes());
                    } else {
                        Log.i(TAG, variant + ") IntentFilter: filter = null");
                    }
                    
                    startActivity(intentCalculator);
                }
                break;
            }
            
            case 2: {//Call EXTERNAL calculator explicitly through use the package name and
                // the qualified name of the Activity of GOOGLE Calculator
                final String CALCULATOR_PACKAGE = "com.android.calculator2";
                final String CALCULATOR_CLASS = "com.android.calculator2.Calculator";
                Uri expression = Uri.parse("data:26");
                //Log.i(TAG, variant + ") expression.getEncodedAuthority() = " + expression.getEncodedAuthority());
                
                Intent intentCalculator = new Intent();
                intentCalculator.setAction(Intent.ACTION_MAIN);
                intentCalculator.addCategory(Intent.CATEGORY_LAUNCHER);
                ComponentName componentName = new ComponentName(CALCULATOR_PACKAGE, CALCULATOR_CLASS);
                intentCalculator.setComponent(componentName);
                Log.i(TAG, variant + ") ComponentName : Name = " + componentName.getClassName() + "; Package = " + componentName.getPackageName());
                //intentCalculator.setDataAndType(expression, "data/float");
                //intentCalculator.setData(expression);
                if (intentCalculator.resolveActivity(getPackageManager()) != null) {
                    //
                    PackageManager packageManager = getPackageManager();
                    List<ResolveInfo> activities = packageManager.queryIntentActivities(intentCalculator, 0);
                    boolean isIntentSafe = activities.size() > 0;
                    if (isIntentSafe) {
                        Log.i(TAG, variant + ") activities size = " + activities.size() + " List<ResolveInfo> = " + activities.get(0).toString());
                    }
                    IntentFilter filterIntent = activities.get(0).filter;
                    if (filterIntent != null) {
                        Log.i(TAG, variant + ") IntentFilter: countCategories = " + filterIntent.countCategories() + "; countActions = " + filterIntent.countActions() +
                                "; countActions = " + filterIntent.countDataTypes());
                    } else {
                        Log.i(TAG, variant + ") IntentFilter: filter = null");
                    }
                    //
                    Log.i(TAG, "Intent " + intentCalculator + " is resolve? " + true);
                    
                    startActivity(intentCalculator);
                } else {
                    Log.i(TAG, "Intent " + intentCalculator + " is resolve? " + false);
                }
                break;
            }
            
            default: {
                Toast.makeText(contextExchangeActivity, "No correct parameter for startActivityTest()", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    /**
     * called after returning from other app or inside Activity that was called with intent
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent dataIntent) {
        Log.i(TAG, "onActivityResult: requestCode= " + requestCode + "; resultCode= " + resultCode);
        if (CalcActivity.CODE_REQUEST_FOR_START_CALCACTIVITY == requestCode && RESULT_OK == resultCode) {
            double resData = dataIntent.getDoubleExtra(CalcActivity.EXTRA_DATA_RESULT_FROM_CALCACTIVITY_DOUBLE, 0.0);
            String resString = dataIntent.getStringExtra(CalcActivity.EXTRA_DATA_RESULT_FROM_CALCACTIVITY_STRING);
            textViewResult.setText(String.format(Locale.getDefault(), "%.2f %s", resData, resString));
            Log.i(TAG, "resData = " + resData);
            Bundle dataBundle = dataIntent.getExtras();
            
        }
    }
    
    private double convertStringToDouble(@Nullable String sd) {
        double doubleValueOf = 0.0;
        try {
            Log.i(TAG, "convertStringToDouble(String sd) :  sd = " + sd);
            Log.i(TAG, "convertStringToDouble(String sd) :  sd.matches(\"[^0-9.]\") = " + sd.matches("[^0-9.]"));
        } catch (NullPointerException ex) {
            Log.i(TAG, "convertStringToDouble(String sd) : NullPointerException :" + ex);
        }
        if (null == sd || sd.isEmpty() || sd.matches("[^0-9\\.,]")) {
            return 0.0;
        }
        //for avoid java.lang.NumberFormatException: For input string: with decimal separator as comma
        // after used String.format("%f", double arg)
        sd = sd.replace(',', '.');
        try {
            doubleValueOf = Double.valueOf(sd).doubleValue();
        } catch (NumberFormatException e) {
            Toast.makeText(this, R.string.exception_convert_String_to_double, Toast.LENGTH_LONG).show();
            doubleValueOf = 0;
        }
        return doubleValueOf;
    }
    
    /**
     *
     */
    private void netOperationThread() {
        
        Thread netThread = new Thread(new NetTask(httpExchangeJson));
        netThread.setPriority(Thread.NORM_PRIORITY);
        netThread.start();
    }
    
    
    /**
     *
     */
    public class NetTask implements Runnable {
        
        CountDownTimer countDownTimer = null;
        InputStream inStream = null;
        
        String exchangeRateSale_$ = "";
        String exchangeRateBuy_$ = "";
        String exchangeRateSale_EUR = "";
        String exchangeRateBuy_EUR = "";
        String httpExchangeJson;
        
        final String TAG_USD = "USD";
        final String TAG_EUR = "EUR";
        final String TAG_UAH = "UAH";
        final String TAG_BUY = "buy";
        final String TAG_SALE = "sale";
        
        long time = 0;
        
        //Map entry: key - String("USDbuy", "USDsale", "EURbuy", "EURsale", etc) , value - Double variable (currency rate)
        Map<String, Double> jsonStringToMap = null; //new HashMap<>();
        
        /**
         * Constructor without parameters
         */
        /*
        public NetTask(){
            super();
        }
        */
        
        /**
         * Constructor with a parameter
         *
         * @param httpExchangeJson
         */
        public NetTask(String httpExchangeJson) {
            super();
            this.httpExchangeJson = httpExchangeJson;
        }
        
        /**
         *
         */
        @Override
        public void run() {
            boolean isIOException = false;
            String jsonStringResult = null;
            
            time = System.currentTimeMillis();
            Log.i(TAG, "The net operation is run in the new thread");
            // Creating a thread for launching the CountDownTimer (with Handler - Looper) for control a response time for a network operation
            Thread threadCountDownTimer = new Thread() {
                @Override
                public void run() {
                    Log.i(TAG, "======== Looper Thread for CountDownTimer is running");
                    Looper.prepare();
                    
                    countDownTimer = new CountDownTimer(contextExchangeActivity.getResources().getInteger(R.integer.time_out_Net_Operation_millis),
                            contextExchangeActivity.getResources().getInteger(R.integer.interval_Tick_around_time_out_Net_Operation_millis)) {
                        boolean isFirstInterval = true;
                        
                        @Override
                        public void onTick(long millisUntilFinished) {
                            if (!isFirstInterval) {
                                Log.i(TAG, getString(R.string.pending_Response_Network) + (millisUntilFinished / 1000 + 1) + " " + TimeUnit.SECONDS);
                                handlerPostToast(contextExchangeActivity,
                                        getString(R.string.pending_Response_Network) + (millisUntilFinished / 1000 + 1) + " " + TimeUnit.SECONDS,
                                        Toast.LENGTH_SHORT);
                            } else {
                                isFirstInterval = false;
                            }
                        }
                        
                        @Override
                        public void onFinish() {
                            handlerPostToast(contextExchangeActivity, R.string.error_Timed_Out_Response_Network, Toast.LENGTH_LONG);
                            Log.i(TAG, getString(R.string.error_Timed_Out_Response_Network));
                            cancel(); // to avoid a leakage a memory
                            // set permission for start a later downloading at the netOperationThread
                            try {
                                if (inStream != null) {
                                    inStream.close(); // close a HTTP net operation InputStream
                                }
                            } catch (IOException e) {
                                Log.i(TAG, getString(R.string.exception_closing_Input_Stream) + e);
                            }
                            isDownloading = true;
                            Looper.myLooper().quitSafely();
                        }
                    }.start(); // CountDownTimer start()
                    Looper.loop();
                }
            };
            threadCountDownTimer.setPriority(3);
            threadCountDownTimer.start();
            
            
            try {
                jsonStringResult = httpOperationInputStream(httpExchangeJson);// get JSON string from Http resource
                Log.i(TAG, ("String returned = '" + jsonStringResult + "'"));
            } catch (IOException e) {
                handlerPostToast(contextExchangeActivity, R.string.exception_while_creating_Input_Stream, Toast.LENGTH_LONG);
                Log.i(TAG, "I/O Exception while calling the httpOperationInputStream(httpExchangeJson): " + e);
                isIOException = true;
            }
            if (jsonStringResult != null) {  // reading the Json string from the Http resource is  failed
                jsonStringToMap = parseJsonToMapPrv(jsonStringResult);//retrieve a rate currency data from JSON to Map
            }
            if (jsonStringToMap != null) {
                Log.i(TAG, jsonStringToMap.toString());
                exchangeRateSale_$ = jsonStringToMap.get(TAG_USD + TAG_SALE).toString();
                exchangeRateBuy_$ = jsonStringToMap.get(TAG_USD + TAG_BUY).toString();
                exchangeRateSale_EUR = jsonStringToMap.get(TAG_EUR + TAG_SALE).toString();
                exchangeRateBuy_EUR = jsonStringToMap.get(TAG_EUR + TAG_BUY).toString();
                
                Log.i(TAG, "Before HANDLER.POST() will put the result at UI");
                /*
                try {
                    Thread.sleep(2000);
                }
                catch (InterruptedException e) {
                     e.printStackTrace();
                }
                 */
                handler.post(new Runnable() { //writing the values of a currency rate in accordance UI  fields
                    @Override
                    public void run() {
                        //cancel of the invoked ProgressBar Circle if it was showing
                        if (progressBarCircle != null && progressBarCircle.isShown()) {
                            progressBarCircle.setVisibility(View.GONE); // or (View.Invisible)
                        }
                        //cancel of the invoked ProgressDialog if it was invoked and showing
                        if (progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.cancel();
                        }
                        buttonGetExchangeRate.setVisibility(View.VISIBLE);
                        
                        textView$SaleExchngeRate.setText(exchangeRateSale_$);
                        textView$BuyExchngeRate.setText(exchangeRateBuy_$);
                        textViewEuroSaleExchngeRate.setText(exchangeRateSale_EUR);
                        textViewEuroBuyExchngeRate.setText(exchangeRateBuy_EUR);
                        // set permission for start a later downloading at the netOperationThread
                        isDownloading = true;
                    }
                });
                countDownTimer.cancel();
                handlerShowCustomToast(contextExchangeActivity, R.string.rate_was_refreshed, R.drawable.ic_thumb_up_black_48px, Toast.LENGTH_SHORT,
                        Gravity.CENTER_VERTICAL, 0, 0, layoutCustomToast, imageViewCustomToast, textViewCustomToast);
                //handlerPostToast(contextExchangeActivity, R.string.rate_was_refreshed, Toast.LENGTH_SHORT);
                Log.i(TAG, "After HANDLER.POST()were send the result at UI");
                Log.i(TAG, "Time of the net operatin is " + (System.currentTimeMillis() - time) + " millisec");
            } else {
                handler.post(new Runnable() { // The operation reading a Json string and convert its in the currency rate value has not a correct result
                    @Override
                    public void run() {
                        //cancel of the invoked ProgressBar Circle if it was showing
                        if (progressBarCircle != null && progressBarCircle.isShown()) {
                            progressBarCircle.setVisibility(View.GONE); // or (View.Invisible)
                        }
                        //cancel of the invoked ProgressDialog if it was invoked and showing
                        if (progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.cancel();
                        }
                        buttonGetExchangeRate.setVisibility(View.VISIBLE);
                        
                        // set permission for start a later downloading at the netOperationThread
                        isDownloading = true;
                    }
                });
                countDownTimer.cancel();
                if (!isIOException) {
                    handlerPostToast(contextExchangeActivity, R.string.error_result_JSON_string_to_Map_is_null, Toast.LENGTH_LONG);
                }
            }
        }
        
        /**
         * Input a string resource directly from URL
         *
         * @param httpUrl
         * @return String String The string of JSON that retrieved from URL-address.
         * @throws IOException
         */
        String httpOperationBufferReader(final String httpUrl) throws IOException {
            String strResult = null;
            return (strResult);
        }
        
       
        
        /**
         * Parsing JsonString which contain a exchange rate for the currency.
         * name parseJsonToMapPrv
         *
         * @param stringJson type final String
         * @return HashMap<String   ,       Double> The result of a parse JsonString, which contain a pair Key-Value, where
         * Key - a name of currency with a suffix "buy" or "sale" and
         * Value - the value of a currency rate.
         */ /**
         * Input a string resource directly from URL.
         *
         * @param httpUrl
         * @return String The string of JSON that retrieved from URL-address.
         * @throws IOException
         */
        String httpOperationInputStream(final String httpUrl) throws IOException {
        
            final int LENGTH_INPUT_BYTE_BUFFER = contextExchangeActivity.getResources().getInteger(R.integer.length_input_stream_byte_buffer_default);
            String strResult = null;
            URL urlHttp = null;
            HttpURLConnection httpsUrlConnection = null;
        
            Log.i(TAG, "The method httpOperationInputStream is running - BEGIN");
            try {
                urlHttp = new URL(httpUrl);
            } catch (MalformedURLException e) {
                Log.i(TAG, "URL is not created. Exception thrown" + e);
                handlerPostToast(contextExchangeActivity, R.string.exception_create_URL, Toast.LENGTH_LONG);
                e.printStackTrace();
            }
            if (urlHttp != null) {
                Log.i(TAG, "The method httpOperationInputStream - URL correct = " + urlHttp.toString());
                Log.i(TAG, "The method httpOperationInputStream - Before inStream created");
                try {
                    inStream = urlHttp.openStream();
                } catch (IOException e) {
                    //handlerPostToast(contextExchangeActivity, R.string.exception_while_creating_Input_Stream, Toast.LENGTH_LONG);
                    Log.i(TAG, "ERROR: I/O Exception: Input Stream is not created");
                    e.printStackTrace();
                    throw e;
                }
            }
            if (inStream != null) {       //Reading from InputStream into the byte array
                strResult = "";          //Initialise of the result's string
                int countByteBuff = 0;   //the counter of a number of the bytes, or -1, if  an end of reading
                int len = 0;             //number bytes for the creation of a byte array
                Log.i(TAG, "The method httpOperationInputStream - After inStream created");
                while (countByteBuff != -1) {
                    try {
                        len = inStream.available(); //an estimate of a number bytes in a buffer before reading
                    } catch (IOException e) {
                        //handlerPostToast(contextExchangeActivity, R.string.error_while_using_the_Input_Stream, Toast.LENGTH_LONG);
                        Log.i(TAG, "ERROR: I/O Exception: Input Stream is not available");
                        e.printStackTrace();
                        throw e;
                    }
                    if (len < 0) {
                        //handlerPostToast(contextExchangeActivity, R.string.error_while_using_the_Input_Stream, Toast.LENGTH_LONG);
                        len = LENGTH_INPUT_BYTE_BUFFER;
                    }
                    byte[] inputByteBuffer = new byte[len];
                    countByteBuff = inStream.read(inputByteBuffer);
                    Log.i(TAG, "estimate len = " + len + "; countByteBuff = " + countByteBuff + "; inputByteBuffer.length = " + inputByteBuffer.length);
                    if (countByteBuff != -1) {
                        String strResultTemp = new String(inputByteBuffer, 0, countByteBuff);
                        strResult += strResultTemp;
                    }
                }
                inStream.close();
            } else {
                handlerPostToast(contextExchangeActivity, R.string.error_while_creating_Input_Stream, Toast.LENGTH_LONG);
            }
            /*
            try { // The temporary block for testing of a reaction on rotating the device
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            */
            return strResult;
        }
        Map<String, Double> parseJsonToMapPrv(final String stringJson) {
            parseGsonFromJson(stringJson);
            Map<String, Double> map = null; //The result of the parse of the JSON string
            JSONArray arrayOfJsonObject;
            JSONObject jsonObject;
            final String nameCurrency = "ccy";
            final String nameSale = "sale";
            final String nameBuy = "buy";
            
            if (stringJson != null) {
                try {
                    map = new ArrayMap<>();
                    arrayOfJsonObject = new JSONArray(stringJson);
                    Log.i(TAG, "JSONArray(length = " + arrayOfJsonObject.length() + ") =  " + arrayOfJsonObject.toString());
                    for (int i = 0; i < arrayOfJsonObject.length(); i++) {
                        jsonObject = arrayOfJsonObject.getJSONObject(i);
                        Log.i(TAG, jsonObject.getString("ccy"));
                        String valueCurrency = jsonObject.getString(nameCurrency);
                        Double valueSale = jsonObject.getDouble(nameSale);
                        Double valueBuy = jsonObject.getDouble(nameBuy);
                        switch (valueCurrency) {
                            case TAG_USD: {
                                Log.i(TAG, "TAG_USD :  " + valueCurrency);
                                map.put(valueCurrency + TAG_SALE, valueSale);
                                map.put(valueCurrency + TAG_BUY, valueBuy);
                                break;
                            }
                            case TAG_EUR: {
                                Log.i(TAG, "TAG_EUR : " + valueCurrency);
                                map.put(valueCurrency + TAG_SALE, valueSale);
                                map.put(valueCurrency + TAG_BUY, valueBuy);
                                break;
                            }
                            default:
                                Log.i(TAG, "default : " + valueCurrency);
                        }
                        
                    }
                } catch (JSONException e) {
                    map = null;   // Indicate that result is failed
                    Log.i(TAG, "Create the object of JSONArray(stringJson). Occured Exception JSONJSONException " + e);
                    handlerPostToast(contextExchangeActivity, R.string.exception_Create_JSONObject, Toast.LENGTH_LONG);
                    e.printStackTrace();
                }
            } else {
                handlerPostToast(contextExchangeActivity, R.string.error_while_parsed_JSON_string_and_string_is_null, Toast.LENGTH_LONG);
            }
            
            
            return map;
        }
    
        /**
         * Deserialization from Json
         * @param stringJson
         * @return Object An object that is have deserialized from a JSON string
         */
        Object parseGsonFromJson(String stringJson){
            if(null == stringJson){
                return null;
            }
            Log.i(TAG,"parseGsonFromJson(String stringJson): " );
            Gson gson = new Gson();
            //String testStringJson = "["abc
            String objFromJsonString = gson.fromJson("abc", String.class);

            Log.i(TAG,"stringJson = " + "abc");

            Log.i(TAG,"stringFromObject = " + objFromJsonString);
            
            return objFromJsonString;
        }
        
        
        /**
         * Showing the message on Toast() through invoke Handler.post
         *
         * @param contextActivity type Context : context of Activity where will be showing message
         * @param postString      type String : string for showing mesage through Toast();
         * @param toastDuration   type int (Toast.LENGTH_SHORT or Toast.LENGTH_LONG)
         * @name handlerPostToast(final Context, final String, final int)
         */
        void handlerPostToast(final Context contextActivity, final String postString, final int toastDuration) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(contextActivity, postString, toastDuration).show();
                }
            });
        }
        
        /**
         * Showing the message on Toast() through invoke Handler.post
         *
         * @param contextActivity type Context
         * @param postRstring     type int (R.string)
         * @param toastDuration   type int (Toast.LENGTH_SHORT or Toast.LENGTH_LONG)
         * @name handlerPostToast(final Context, final int, final int)
         */
        void handlerPostToast(final Context contextActivity, final int postRstring, final int toastDuration) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(contextActivity, postRstring, toastDuration).show();
                }
            });
        }
        
        void handlerShowCustomToast(final Context contextActivity, final int textResource, final int imageResource, final int toastDuration,
                                    final int gravity, final int xOffset, final int yOffset, final View customView,
                                    final ImageView customImageView, final TextView customTextView) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    
                    showCustomToastOneImageOneText(contextActivity, textResource, imageResource, toastDuration, gravity,
                            xOffset, yOffset, customView, customImageView, customTextView);
                }
            });
        }
    }
}
