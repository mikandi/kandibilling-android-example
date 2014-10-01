package com.example.testapp;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import tools.mikandi.dev.inapp.OnAuthorizeInAppListener;
import tools.mikandi.dev.inapp.onPurchaseHistoryListener;
import tools.mikandi.dev.inapp.onUserVerificationListener;
import tools.mikandi.dev.library.KandiLibs;
import tools.mikandi.dev.login.LoginResult;
import tools.mikandi.dev.utils.InstallerCheck;
import tools.mikandi.dev.utils.UserInfoObject;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class TestAppMain extends ActionBarActivity  {

	private Button btn_forceinstaller , btn_listPurchases, btn_login, btn_logout, btn_loggedIn, btn_valid_user , btn_token_check,  btn_make_purchase;
	private Button btn_buy_gold, btn_whoinstalled;
	private UserInfoObject uio = null; 
	private static Context context;
	final static String EXTRA_INSTALLER_PACKAGE_NAME = "android.intent.extra.INSTALLER_PACKAGE_NAME";
	final static String INSTALLER_PACKAGE_NAME = "com.mikandi.vending";
	public static boolean debug = true;
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
		btn_forceinstaller = (Button) findViewById(R.id.btn_installer);
		btn_whoinstalled = (Button) findViewById(R.id.btn_whoinstalled);
		
		// creation of UserInfoObject! 
		uio = new UserInfoObject();
		
		// updated instance of for each KandiLibs library. 
		uio = UserInfoObject.getInstance(context);

		//this button checks the installer of the app. MiKandi sets the installer 
		//variable to "com.mikandi.vending" while the installer variable would be null if it is installed by the system. (this also applies to apks 
		//installed by the shell.) 
		//btn_whoinstalled.setEnabled(false);
		
		// this installer reinstalls the application and sets the installer to the def Mikandi installer to allow 
		// you to see and understand how our drm works. 
		btn_forceinstaller.setEnabled(false);
/* 
 * WHY NOT CALL THE KANDILIBS METHODS FROM THESE BUTTON CLICK LISTENERS? 
 * 	- You can! Unless the Activity variable is required, if it is, then by passing in the "this" it references 
 * the inside of the button click listener as opposed to referencing the activity. (this causes an issue!) 
 * - You don't have to make any of these calls to KandiLibs at all work from button presses but its up to you!
 * - This is not the only way to make these calls, just the easiest to understand and demonstrate. 
 */
//--------------------------------------------------------- install check -------------------------------------------------------------		
	btn_forceinstaller.setOnClickListener(new Button.OnClickListener() {

		@Override
		public void onClick(View v) {
			isInstallerCorrect(context);
		}
	});	
	
	
	btn_whoinstalled.setOnClickListener(new OnClickListener() {

		@Override
		public void onClick(View v) {
			KandiLibs.checkInstaller(context);	
		}} );
	
	
// ---------------------------------------------------------install check end ------------------------------------------------		
// -------------------buy gold button ------------------------------------------------------------------------------
		
	btn_buy_gold.setOnClickListener(new Button.OnClickListener() {

		@Override
		public void onClick(View v) {
			buyGold();
		} 
	
	});
	//-----------------------------------Make purchase -------------------------------------------------------------	
  btn_make_purchase.setOnClickListener(new Button.OnClickListener() {

	@Override
	public void onClick(View v) {
		String token = "last_test_check"; 
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
			String token= "last_test_check"; 
			UserInfoObject uio = UserInfoObject.getInstance(getApplicationContext());
			boolean testPurchase = KandiLibs.checkPurchase(uio, token);
			if (debug) Log.i("Token Check " , "token check of " + token + " returns :" + testPurchase);
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
				if (debug) Log.i("User is logged in ? " , "" + loggedIn);
			}
		});
// ----------------------------Log in ------------------------------------------------ 
	 btn_login.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View arg0) {
				
				if (debug) Log.i("DEBUGGING XML ERROR: " , "Button Pressed");
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
				if (debug)	Log.i("Purchase Success" , "" + token + " purchased");
				
			}

			@Override
			public void Failed(int code) {
				if (debug) Log.i("Purchase failed" , "" + token + " was not purchased ");
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
		if (debug) Log.i("verifying user" , "requesting library verify user ");
		KandiLibs.requestUserVerify(uio, new onUserVerificationListener() {

		@Override
		public void userVerifyFailed(int code) {
			Toast.makeText(getApplicationContext(), "user not verified code " + code,Toast.LENGTH_LONG).show();
			if (debug) Log.e("Verify user in callback " , "verify user was unsucessful - user has NOT purchased app");
		}

		@Override
		public void userVerifiedSuccessfully() {
			Toast.makeText(getApplicationContext(), "Verified user ", Toast.LENGTH_LONG).show();
			if (debug) Log.e("Verifying user in callback" , "Verifying was sucessful - user has purchased app");
		}
		} );
	}
//--------------------------------------------- Check Token --------------------------------------
	protected void checktoken(String string) {
		String temp = "porn_package_test";
		UserInfoObject uio = UserInfoObject.getInstance(getApplicationContext());
		boolean testPurchase = KandiLibs.checkPurchase(uio, temp);	
		if (debug) Log.e("testing the checkPurchase(" + temp + ")" , " test was: " + testPurchase);
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
					if (debug) Log.i("Printing out new purchases from saved lr", "" + s);
					}
				}
			
			@Override
			public void onFailedHistoryRetrieved() {
				if (debug) Log.i("failed purchase History", "no returned list");
			}
		});
	}
	
	// -----------------------------------------------Login ----------------------------------------------------------------------------------
	private void logUserIn() {
		// "this" doesn't reference the correct thing if the code below is 
		// within the buttonlistener (this referes to the listener not the activity) 
		
		uio = UserInfoObject.getInstance(context);
		if (debug) Log.i("DEBUGGING XML ERROR: " , "uio instance retreived");
		KandiLibs.requestLogin(this, uio);
	}
	// ---------------------------------------------- Install intent testing ----------
	
	/**
	 * this test is to check if the installer of the app. It will return true if Mikandi 
	 * installed the application on the device.
	 * @param apkUri
	 * @return
	 */
	public static Intent createInstallIntent(final Uri apkUri) {
		final Intent installIntent = new Intent(Intent.ACTION_VIEW);
		if (debug) Log.i("createInstallIntent" , "printing install intent after creation " + installIntent.toString());
		installIntent.setDataAndType(apkUri,
				"application/vnd.android.package-archive");
		if (debug) Log.i("createInstallIntent" , "set data");
		// provide the installer package name:
		final String installerKey = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH ? Intent.EXTRA_INSTALLER_PACKAGE_NAME
				: EXTRA_INSTALLER_PACKAGE_NAME);
		if (debug) Log.i("createInstallIntent" , "installerKey" + installerKey ); 
		installIntent.putExtra(installerKey, INSTALLER_PACKAGE_NAME);
		installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		return installIntent;
	}
	
	public static boolean isInstallerCorrect(Context ctx) { 
		boolean myb = false; 
		final PackageManager pm = ctx.getPackageManager();
		if (debug) Log.i("isInstallerCorrect" , "retreived package manager" );
		File f = new File(context.getPackageResourcePath());
		if (debug) Log.i("isInstallerCorrect" , "File created : " + f.toString() );
		Uri u = Uri.fromFile(f);
		if (debug) Log.i("isInstallerCorrect" , "printing out uri:" + u.toString());
		String installer_initial = pm.getInstallerPackageName(ctx.getApplicationContext().getPackageName());
		if (debug) Log.i("isInstallerCorrect", "printing out installer_initial " + installer_initial);
		Intent i = createInstallIntent(u);
		if (debug) Log.i("isInstallerCorrect" , " printing out intent : " + i.toString());
		ctx.startActivity(i);
		String installer_final = pm.getInstallerPackageName(ctx.getApplicationContext().getPackageName());
		if (debug) Log.i("isInstallerCorrect" , "installer_final" + installer_final );
		if (debug) Log.i("installer test" , "Initial (should be null) + : " + installer_initial + ", after intent the install is : " + installer_final); 
		myb = InstallerCheck.checkInstaller(ctx);
		
		return myb;
	}
	// -------------------------------------------------------------------------------
}
