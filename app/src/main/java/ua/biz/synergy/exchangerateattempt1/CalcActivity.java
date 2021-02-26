package ua.biz.synergy.exchangerateattempt1;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

/**
 * CalcActivity class
 */
public class CalcActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener {
    
    private static final String TAG_CALC = "CalcActivityTAG";
    public static final String EXTRA_MESSAGE = "ExchangeActivity_MESSAGE_String";
    public static final String EXTRA_MESSAGE_RATE_ME_BUY_OR_SALE_CURRENCY = "ExchangeActivity_MESSAGE_TITLE_RATE_ME_BUY_OR_SALE_CURRENCY";
    public static final String EXTRA_MESSAGE_ME_AMOUNT_OF_CURRENCY = "ExchangeActivity_MESSAGE_TITLE_CURRENCY_AMOUNT";
    public static final String EXTRA_MESSAGE_ME_SUM = "ExchangeActivity_MESSAGE_TITLE_MONEY_SUM";
    public static final String EXTRA_DATA_ME_BUY_OR_SALE_CURRENCY_RATE_DOUBLE = "ExchangeActivity_DATA_FOR_BUY_OR_SALE_CURRENCY_RATE_double";
    public static final String EXTRA_DATA_CURRENCY_NAME_STRING = "ExchangeActivity_DATA_CURRENCY_NAME_String";
    public static final String EXTRA_DATA_MONEY_NAME_STRING = "ExchangeActivity_DATA_MONEY_NAME_String";
    public static final String EXTRA_DATA_RESULT_FROM_CALCACTIVITY_DOUBLE = "CalcActivity_Data_Result_double";
    public static final String EXTRA_DATA_RESULT_FROM_CALCACTIVITY_STRING = "CalcActivity_Data_Result_String";
    public static final int CODE_REQUEST_FOR_START_CALCACTIVITY = 1;
    String currensySign;
    String moneySign;
    
    private TextView mTextMessageForAction;
    private TextView mTextViewTitleRow1Col1;
    private TextView mTextViewTitleRow2Col1;
    private TextView mTextViewTitleRow3Col1;
    private EditText mEditTextDataValueRow1Col2;
    private EditText mEditTextDataValueRow2Col2;
    private EditText mEditTextDataValueRow3Col2;
    
    private ImageButton imageButtonCalcOk;
    private ImageButton imageButtonClearingRow1Col2;
    private ImageButton imageButtonClearingRow2Col2;
    private ImageButton imageButtonClearingRow3Col2;
    
    private View layoutCustomToastCalc;
    private TextView textViewCustomToastCalc;
    private ImageView imageViewCustomToastCalc;
    
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    
                    //mTextMessageForAction.setText(R.string.title_home);
                    return true;
                case R.id.navigation_dashboard:
                    //mTextMessageForAction.setText(R.string.title_dashboard);
                    return true;
                case R.id.navigation_notifications:
                    //mTextMessageForAction.setText(R.string.title_notifications);
                    return true;
            }
            return false;
        }
        
    };
    
    /**
     * OR this method for hide the input soft keyboard
     */
    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calc);
        
        mTextMessageForAction = (TextView) findViewById(R.id.message_for_action);
        mTextViewTitleRow1Col1 = (TextView) findViewById(R.id.textViewCalculateRow1Col1);
        mTextViewTitleRow2Col1 = (TextView) findViewById(R.id.textViewCalculateRow2Col1);
        mTextViewTitleRow3Col1 = (TextView) findViewById(R.id.textViewCalculateRow3Col1);
        mEditTextDataValueRow1Col2 = (EditText) findViewById(R.id.editTextCalculateRow1Col2);
        mEditTextDataValueRow2Col2 = (EditText) findViewById(R.id.editTextCalculateRow2Col2);
        mEditTextDataValueRow3Col2 = (EditText) findViewById(R.id.editTextCalculateRow3Col2);
        
        imageButtonClearingRow1Col2 = (ImageButton) findViewById(R.id.imageButtonClearRow1Col2);
        imageButtonClearingRow2Col2 = (ImageButton) findViewById(R.id.imageButtonClearRow2Col2);
        imageButtonClearingRow3Col2 = (ImageButton) findViewById(R.id.imageButtonClearRow3Col2);
        imageButtonCalcOk = (ImageButton) findViewById(R.id.imageButtonCalculateOk);
        imageButtonCalcOk.setOnClickListener(this);
        imageButtonCalcOk.setOnLongClickListener(this);
        imageButtonClearingRow1Col2.setOnClickListener(this);
        imageButtonClearingRow1Col2.setOnLongClickListener(this);
        imageButtonClearingRow2Col2.setOnClickListener(this);
        imageButtonClearingRow2Col2.setOnLongClickListener(this);
        imageButtonClearingRow3Col2.setOnClickListener(this);
        imageButtonClearingRow3Col2.setOnLongClickListener(this);
        
        // For the custom layout of Toast:
        LayoutInflater inflater = getLayoutInflater();
        layoutCustomToastCalc = inflater.inflate(R.layout.custom_refresh_toast, (ViewGroup) findViewById(R.id.custom_refresh_toast_container));
        textViewCustomToastCalc = (TextView) layoutCustomToastCalc.findViewById(R.id.textView_custom_Refresh_Toast);
        imageViewCustomToastCalc = (ImageView) layoutCustomToastCalc.findViewById(R.id.imageView_custom_Refresh_Toast);
        
        // For BottomNavigation Bar. Now no using
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        
        String messageTitleGeneral = "";
        String titleBuyOrSaleCurrency = "";
        String titleCurrencyAmount = "";
        String titleSum = "";
        double rateValueMeBuyOrSale = 0.0;
        
        //get an Intent which was created in ExchangeActivity for start of this CalcActivity
        Intent intentCalcActivity = getIntent();
        if (intentCalcActivity != null && intentCalcActivity.hasExtra(EXTRA_MESSAGE)
                && intentCalcActivity.hasExtra(EXTRA_MESSAGE_RATE_ME_BUY_OR_SALE_CURRENCY)
                && intentCalcActivity.hasExtra(EXTRA_MESSAGE_ME_AMOUNT_OF_CURRENCY)
                && intentCalcActivity.hasExtra(EXTRA_MESSAGE_ME_SUM)
                && intentCalcActivity.hasExtra(EXTRA_DATA_CURRENCY_NAME_STRING)
                && intentCalcActivity.hasExtra(EXTRA_DATA_MONEY_NAME_STRING)) {
            // setting the general title for this screen
            messageTitleGeneral = intentCalcActivity.getStringExtra(EXTRA_MESSAGE);
            mTextMessageForAction.setText(messageTitleGeneral);
            //retrieve from Intent String value for the title of a currency rate
            titleBuyOrSaleCurrency = intentCalcActivity.getStringExtra(EXTRA_MESSAGE_RATE_ME_BUY_OR_SALE_CURRENCY);
            //retrieve from Intent String value for the title of a currency amount
            titleCurrencyAmount = intentCalcActivity.getStringExtra(EXTRA_MESSAGE_ME_AMOUNT_OF_CURRENCY);
            //retrieve from Intent String value for the title of a sum
            titleSum = intentCalcActivity.getStringExtra(EXTRA_MESSAGE_ME_SUM);
            //retrieve from Intent double value for the value of a currency rate
            rateValueMeBuyOrSale = intentCalcActivity.getDoubleExtra(EXTRA_DATA_ME_BUY_OR_SALE_CURRENCY_RATE_DOUBLE, 0.0);
            //retrieve from intent string value of a currency name
            currensySign = intentCalcActivity.getStringExtra(EXTRA_DATA_CURRENCY_NAME_STRING);
            //retrieve from intent string value of a my money name
            moneySign = intentCalcActivity.getStringExtra(EXTRA_DATA_MONEY_NAME_STRING);
            //Setting the title of a field:
            // 1)a currency rate;
            mTextViewTitleRow1Col1.setText(titleBuyOrSaleCurrency.concat(currensySign));
            // 2)an amount of a currency;
            mTextViewTitleRow2Col1.setText(titleCurrencyAmount.concat(" ").concat(currensySign));
            // 3)a sum(a result of the multiple of a currency rate and an amount of the currency)
            mTextViewTitleRow3Col1.setText(titleSum.concat(" ").concat(moneySign));
            // set the value of the currency rare
            mEditTextDataValueRow1Col2.setText(String.format(Locale.getDefault(), "%f", rateValueMeBuyOrSale));
        }
        //Setting the focus on the field following the currency rate field
        mEditTextDataValueRow2Col2.requestFocus();
        showSoftKeyboard(mEditTextDataValueRow2Col2); // Misunderstanding: don't make a input Method visible
        
        Log.i(TAG_CALC, " mEditTextDataAmountRow1Col2.setText(String.format(\"%f\", buyDollarMe)):" + String.format("%f", rateValueMeBuyOrSale));
    }
    
    /**
     * This will force the keyboard to be hidden in all situations. In some cases you will want to pass
     * in InputMethodManager.HIDE_IMPLICIT_ONLY as the second parameter to ensure you only hide
     * the keyboard when the user didn't explicitly force it to appear (by holding down menu).
     */
    public void hideSoftKeyboard() {
        // Hide input soft keyboard for current view
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
    
    // based on https://stackoverflow.com/a/21574135/115145
    // (Obtained from MainActivity todo_app Commonsware)
    
    private void hideSoftInput() {
        if (getCurrentFocus()!=null && getCurrentFocus().getWindowToken()!=null) {
            ((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }
    
    
    /**
     *
     */
    public void hideSoftKeyboardToggle() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }
    
    /**
     * @param view
     */
    public void showSoftKeyboard(View view) {
        Log.i(TAG_CALC, "showSoftKeyboard(View view) is running");
        if (view.requestFocus()) {
            Log.i(TAG_CALC, "view.requestFocus() is true");
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT); //flag may be 0 or SHOW_IMPLICIT or SHOW_FORCED
        }
    }
    
    /**
     * onCalc handle of a pressing of buttons
     * I)If pressed button for Calculate and obtaining a result
     * result is:
     * 1) a value of a calculate of the field "sum"  if before a calculate only this field is empty
     * OR all fields are filled
     * 2) a value of calculate of the field "amount", if before a calculate only this field is empty
     * 3) a value of calculate of the field "currency rate", if before a calculate only this field is empty
     * 4) Error message.
     * II)if pressed any button for clear of the value field
     * result is: clear field according button that be pressed
     *
     * @param v - View of the pressed button
     */
    public void onClick(View v) {
        Log.i(TAG_CALC, "onClick run");
        
        switch (v.getId()) {
            case (R.id.imageButtonCalculateOk): {
                //Pressed button for Calculate and obtaining a result
                // result is:
                // 1) a value of a calculate of the field "sum"  if before a calculate only this field is empty
                //    OR all fields are filled
                // 2) a value of calculate of the field "amount", if before a calculate only this field is empty
                // 3) a value of calculate of the field "currency rate", if before a calculate only this field is empty
                boolean isResult; //do is a correct result?
                double rateBuyOrSaleCurrency = 0.0;     // a value of the field "currency rate"
                double amountMeBuyOrSaleCurrency = 0.0; // a value of the field "amount"
                double sumMeBuyOrSaleCurrency = 0.0;    // a void of the field "sum"
                double resultOfOperation = 0.0;         // a result for send to activity that called this activity
                String resultCurrencyName = "";         // a currency name for a result - a result for send to activity that called this activity
                Log.i(TAG_CALC, "mEditTextDataValueRow1Col2.getText().toString() = " + mEditTextDataValueRow1Col2.getText().toString());
                Log.i(TAG_CALC, "mEditTextDataValueRow2Col2.getText().toString() = " + mEditTextDataValueRow2Col2.getText().toString());
                String strFieldValueRow1 = mEditTextDataValueRow1Col2.getText().toString(); //currency rate
                String strFieldValueRow2 = mEditTextDataValueRow2Col2.getText().toString(); //amount
                String strFieldValueRow3 = mEditTextDataValueRow3Col2.getText().toString(); //sum
                int flagOperation = 0; //a flag to handle the values of the input fields if == 0 - Error
                
                if ((!strFieldValueRow1.isEmpty() && !strFieldValueRow2.isEmpty() && strFieldValueRow3.isEmpty())
                        || (!strFieldValueRow1.isEmpty() && !strFieldValueRow2.isEmpty() && !strFieldValueRow3.isEmpty())) {
                    flagOperation = 1; //1)
                } else if (!strFieldValueRow1.isEmpty() && strFieldValueRow2.isEmpty() && !strFieldValueRow3.isEmpty()) {
                    flagOperation = 2;  //2)
                } else if (strFieldValueRow1.isEmpty() && !strFieldValueRow2.isEmpty() && !strFieldValueRow3.isEmpty()) {
                    flagOperation = 3;  //3)
                }
                
                Log.i(TAG_CALC, "flagOperation = " + flagOperation);
                
                switch (flagOperation) {
                    case (1): {// calculating a sum =(currency rate) * amount
                        rateBuyOrSaleCurrency = convertStringToDouble(mEditTextDataValueRow1Col2.getText().toString());
                        amountMeBuyOrSaleCurrency = convertStringToDouble(mEditTextDataValueRow2Col2.getText().toString());
                        sumMeBuyOrSaleCurrency = rateBuyOrSaleCurrency * amountMeBuyOrSaleCurrency;
                        mEditTextDataValueRow3Col2.setText(String.format(Locale.getDefault(), "%.2f", sumMeBuyOrSaleCurrency));
                        resultOfOperation = sumMeBuyOrSaleCurrency; // result - a value of the sum
                        resultCurrencyName = moneySign;
                        isResult = true;
                        break;
                    }
                    case (2): { // calculating a currency amount = sum / (currency rate)
                        rateBuyOrSaleCurrency = convertStringToDouble(mEditTextDataValueRow1Col2.getText().toString());
                        if (0 == Double.compare(rateBuyOrSaleCurrency, 0.0)) {
                            Toast.makeText(this, R.string.error_filling_input_fields, Toast.LENGTH_SHORT).show();
                            isResult = false;
                            break;
                        }
                        sumMeBuyOrSaleCurrency = convertStringToDouble(mEditTextDataValueRow3Col2.getText().toString());
                        amountMeBuyOrSaleCurrency = sumMeBuyOrSaleCurrency / rateBuyOrSaleCurrency;
                        // setting the value of the amount (a result of the operation divide: sum / currency rate)
                        mEditTextDataValueRow2Col2.setText(String.format(Locale.getDefault(), "%.2f", amountMeBuyOrSaleCurrency));
                        resultOfOperation = amountMeBuyOrSaleCurrency; // result - a value of the amount of the currency
                        resultCurrencyName = currensySign;
                        isResult = true;
                        break;
                    }
                    case (3): { // calculating a currency rate = sum / (currency amount)
                        amountMeBuyOrSaleCurrency = convertStringToDouble(mEditTextDataValueRow2Col2.getText().toString());
                        if (0 == Double.compare(amountMeBuyOrSaleCurrency, 0.0)) {
                            Toast.makeText(this, R.string.error_filling_input_fields, Toast.LENGTH_SHORT).show();
                            isResult = false;
                            break;
                        }
                        sumMeBuyOrSaleCurrency = convertStringToDouble(mEditTextDataValueRow3Col2.getText().toString());
                        rateBuyOrSaleCurrency = sumMeBuyOrSaleCurrency / amountMeBuyOrSaleCurrency;
                        // setting the value of the currency rate (a result of the operation divide: sum / amount of currency)
                        mEditTextDataValueRow1Col2.setText(String.format(Locale.getDefault(), "%.4f", rateBuyOrSaleCurrency));
                        resultOfOperation = rateBuyOrSaleCurrency; // result - a value of the currency rate
                        resultCurrencyName = moneySign.concat("/").concat(currensySign);
                        isResult = true;
                        break;
                    }
                    default: {
                        Toast.makeText(this, R.string.error_filling_input_fields, Toast.LENGTH_LONG).show();
                        isResult = false;
                        break;
                    }
                }
                if (isResult) {
                    Intent intentResult = new Intent(); //Intent for send result into activity that called this activity
                    intentResult.putExtra(EXTRA_DATA_RESULT_FROM_CALCACTIVITY_DOUBLE, resultOfOperation);// result for send to activity that called this activity
                    intentResult.putExtra(EXTRA_DATA_RESULT_FROM_CALCACTIVITY_STRING, resultCurrencyName);
                    setResult(RESULT_OK, intentResult);
                    // Hide input soft keyboard for current view who is in focus
                    //hideSoftKeyboard();
                    hideSoftKeyboardToggle();
                    //hideKeyboard(this);
                }
                break;
            }
            case (R.id.imageButtonClearRow1Col2): {
                mEditTextDataValueRow1Col2.setText("");
                break;
            }
            
            case (R.id.imageButtonClearRow2Col2): {
                mEditTextDataValueRow2Col2.setText("");
                break;
            }
            
            case (R.id.imageButtonClearRow3Col2): {
                mEditTextDataValueRow3Col2.setText("");
                break;
            }
            default: {
                Toast.makeText(this, R.string.error_Handling_Pressing_Button, Toast.LENGTH_SHORT).show();
                break;
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
            case R.id.imageButtonCalculateOk: {
                ExchangeActivity.showCustomToastOneImageOneText(this, R.string.hint_imageButtonCalculateOk, R.drawable.ic_info_outline_48px, Toast.LENGTH_SHORT,
                        Gravity.CENTER_VERTICAL, 0, 0, layoutCustomToastCalc, imageViewCustomToastCalc, textViewCustomToastCalc);
                break;
            }
            case R.id.imageButtonClearRow1Col2: {
                ExchangeActivity.showCustomToastOneImageOneText(this, R.string.hint_imageButtonClearRow1Col2, R.drawable.ic_info_outline_48px, Toast.LENGTH_SHORT,
                        Gravity.CENTER_VERTICAL, 0, 0, layoutCustomToastCalc, imageViewCustomToastCalc, textViewCustomToastCalc);
                break;
            }
            case R.id.imageButtonClearRow2Col2: {
                ExchangeActivity.showCustomToastOneImageOneText(this, R.string.hint_imageButtonClearRow2Col2, R.drawable.ic_info_outline_48px, Toast.LENGTH_SHORT,
                        Gravity.CENTER_VERTICAL, 0, 0, layoutCustomToastCalc, imageViewCustomToastCalc, textViewCustomToastCalc);
                break;
            }
            case R.id.imageButtonClearRow3Col2: {
                ExchangeActivity.showCustomToastOneImageOneText(this, R.string.hint_imageButtonClearRow3Col2, R.drawable.ic_info_outline_48px, Toast.LENGTH_SHORT,
                        Gravity.CENTER_VERTICAL, 0, 0, layoutCustomToastCalc, imageViewCustomToastCalc, textViewCustomToastCalc);
                //Snackbar.make(v, "Snackbar for Row3Col2", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                break;
            }
            default: {
                Toast.makeText(this, R.string.hint_No_hint_for_this_button, Toast.LENGTH_SHORT).show();
            }
        }
        return true;
    }
    
    private double convertStringToDouble(@Nullable String sd) {
        double doubleValueOf = 0.0;
        try {
            Log.i(TAG_CALC, "convertStringToDouble(String sd) :  sd = " + sd);
            Log.i(TAG_CALC, "convertStringToDouble(String sd) :  sd.matches(\"[^0-9.]\") = " + sd.matches("[^0-9.,]"));
        } catch (NullPointerException ex) {
            Log.i(TAG_CALC, "convertStringToDouble(String sd) : NullPointerException :" + ex);
        }
        if (null == sd || sd.isEmpty() || sd.matches("[^0-9.,]")) {
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
    public boolean isDoubleZero(double d) {
        //if(d >= 0.0 - Double.MIN_VALUE && d <= 0.0 + Double.MIN_VALUE)
        if (0 == Double.compare(d, 0.0)) {
            return true;
        }
        return false;
    }
    
    @Override
    public void finish() {
        Log.i(TAG_CALC, "CalcActivity is FINISHED");
        super.finish();
    }
    
    /**
     * method to call when user hits the back button.
     */
    @Override
    public void onBackPressed() {
        //The result code to propagate back to the originating activity is set in the code above
        // in our case, it's normal when user come back to the originating activity through back button
        Log.i(TAG_CALC, "At the CalcActivity were pressed back button and called onBackPressed()");
        super.onBackPressed();
    }
}
