package com.example.testapp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import tools.mikandi.dev.inapp.OnAuthorizeInAppListener;
import tools.mikandi.dev.inapp.onPurchaseHistoryListener;
import tools.mikandi.dev.inapp.onUserVerificationListener;
import tools.mikandi.dev.login.LoginResult;
import tools.mikandi.dev.login.LoginStorageUtils;
import tools.mikandi.dev.login.OnLoginResultListener;
import tools.mikandi.dev.utils.UserInfoObject;
import tools.mikandi.tools.KandiLibs;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class TestAppMain extends ActionBarActivity {

	private Button btn_listPurchases, btn_login, btn_logout, btn_loggedIn, btn_valid_user , btn_token_check,  btn_make_purchase;
	private Button btn_buy_gold;
	private UserInfoObject uio = null; 
	private Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test_app_mainlayout);
		
		context = getApplicationContext();

		// Setting buttons properly 
		btn_login = (Button) findViewById(R.id.btn_login);
		btn_listPurchases = (Button) findViewById(R.id.btn_list_purchases);
		btn_logout = (Button) findViewById(R.id.btn_logout);
		btn_loggedIn = (Button) findViewById(R.id.btn_loggedIn);
		btn_valid_user = (Button) findViewById(R.id.btn_valid);
		btn_token_check = (Button) findViewById(R.id.btn_tokencheck);
		btn_buy_gold = (Button) findViewById(R.id.btn_buyGold);
		btn_make_purchase = (Button) findViewById(R.id.btn_make_purchase);
		
		// creation of UserInfoObject! 
		uio = new UserInfoObject();
		// updated instance of for each KandiLibs library. 
		uio = UserInfoObject.getInstance(context);

/* 
 * WHY NOT CALL THE KANDILIBS METHODS FROM THESE BUTTON CLICK LISTENERS? 
 * 	- You can! Unless the Activity variable is required, if it is, then by passing in the "this" it references 
 * the inside of the button click listener as opposed to referencing the activity. (this causes an issue!) 
 * - You don't have to make any of these calls to KandiLibs at all work from button presses but its up to you!
 * - This is not the only way to make these calls, just the easiest to understand and demonstrate. 
 */
		
// -------------------buy gold button ------------------------------------------------------------------------------
		
	btn_buy_gold.setOnClickListener(new Button.OnClickListener() {

		@Override
		public void onClick(View v) {
			buyGold();
			
			// Or you can do 
			// KandiLibs.requestBuyGold(TestAppMain.this);
		} 
	
	});
	//-----------------------------------Make purchase -------------------------------------------------------------	
  btn_make_purchase.setOnClickListener(new Button.OnClickListener() {

	@Override
	public void onClick(View v) {
		String token = "token_test_app"; 
		String token_description = "this is a test token";
		String token_amount = "100";
		purchaseToken(token , token_description, token_amount);
	}
	  
  });		
// ---------------------------------- list purchases ----------------------------------------------------
	btn_listPurchases.setOnClickListener(new Button.OnClickListener() {

		@Override
		public void onClick(View v) {
			getPreviousPurchases();
		} 	
	});
// -------------------------------------------------------- valid user check --------------------	
	btn_valid_user.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.i("verifying user" , "about to verify user!");
				verifyUser();
			}
		}); 
// -------------------------------------------------------- token check -----------------------------------
	btn_token_check.setOnClickListener(new Button.OnClickListener() {
		@Override
		public void onClick(View v) {
			String token= "porn_package_test"; 
			UserInfoObject uio = UserInfoObject.getInstance(getApplicationContext());
			boolean testPurchase = KandiLibs.checkPurchase(uio, token);
			Log.i("Token Check " , "token check of " + token + " returns :" + testPurchase);
		}
	});
//------------------------------Logout ---------------------------------------------------------------------
	 btn_logout.setOnClickListener(new Button.OnClickListener(){         
			@Override
			public void onClick(View arg0) {
				uio = UserInfoObject.getInstance(context);
				KandiLibs.logOutUser(uio);
			}
		});
// ------------------------------ Logged in?  -------------------------------------------------------------
	 btn_loggedIn.setOnClickListener(new Button.OnClickListener() {

			public void onClick(View arg0) {
				UserInfoObject uio = UserInfoObject.getInstance(getApplicationContext());
				boolean loggedIn = KandiLibs.isLoggedIn(uio);
				Log.i("User is logged in ? " , "" + loggedIn);
			}
		});
// ----------------------------Log in ------------------------------------------------ 
	 btn_login.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View arg0) {
				logUserIn();
			}
		});
	}
// -------------------------------------- Purchase Token  -----------------------------------------------------------------------
	protected void purchaseToken(final String token, String token_description,
			String token_amount) {
		
		KandiLibs.AuthInAppPuchase(uio, token, token_description, token_amount, new OnAuthorizeInAppListener() {

			@Override
			public void Sucess() {
				Log.i("Purchase Success" , "" + token + " purchased");
				
			}

			@Override
			public void Failed(int code) {
				Log.i("Purchase failed" , "" + token + " was not purchased ");
			}
			} );	
	
 
	}
// ------------------------ Buying gold	---------------------------------------------------------------
	protected void buyGold() {
			KandiLibs.requestBuyGold(this);
	}
// ------------------------------------------------ verifying user ----------------------------------------------
	protected void verifyUser() {
		
		uio = UserInfoObject.getInstance(context);
		Log.i("verifying user" , "requesting library verify user ");
		KandiLibs.requestUserVerify(uio, new onUserVerificationListener() {

		@Override
		public void userVerifyFailed(int code) {
			Toast.makeText(getApplicationContext(), "user not verified code " + code,Toast.LENGTH_LONG).show();
			Log.e("Verify user in callback " , "verify user was unsucessful - user has NOT purchased app");
		}

		@Override
		public void userVerifiedSuccessfully() {
			Toast.makeText(getApplicationContext(), "Verified user ", Toast.LENGTH_LONG).show();
			Log.e("Verifying user in callback" , "Verifying was sucessful - user has purchased app");
		}
		} );
	}
//--------------------------------------------- Check Token --------------------------------------
	protected void checktoken(String string) {
		String temp = "porn_package_test";
		UserInfoObject uio = UserInfoObject.getInstance(getApplicationContext());
		boolean testPurchase = KandiLibs.checkPurchase(uio, temp);	
		Log.e("testing the checkPurchase(" + temp + ")" , " test was: " + testPurchase);
	}

// ----------------------------------------------------Purchase History ------------------------------------------------------------ >> -

	private void getPreviousPurchases() {
		uio = UserInfoObject.getInstance(getApplicationContext());
		KandiLibs.requestPurchaseHistory(uio, new onPurchaseHistoryListener() {
			
			@Override
			public void onSucessfulHistoryRetrieved(List<String> lp) {
				LoginResult myLr = uio.getLoginResult(); 	
				ArrayList<String> myArrayList = myLr.getArrayListTokens();
				for (String s : myArrayList){
					Log.i("Printing out new purchases from saved lr", "" + s);
					}
				}
			
			@Override
			public void onFailedHistoryRetrieved() {
				Log.i("failed purchase History", "no returned list");
			}
		});
	}
	
	// -----------------------------------------------Login ----------------------------------------------------------------------------------
	private void logUserIn() {
		// "this" doesn't reference the correct thing if the code below is 
		// within the buttonlistener (this referes to the listener not the activity) 
		
		uio = UserInfoObject.getInstance(context);
		KandiLibs.requestLogin(this, uio);
	}	
}
