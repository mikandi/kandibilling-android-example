package com.example.testapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import tools.mikandi.dev.ads.OnFullScreenAdDisplayedListener;
import tools.mikandi.dev.inapp.OnAuthorizeInAppListener;
import tools.mikandi.dev.inapp.onPurchaseHistoryListener;
import tools.mikandi.dev.inapp.onUserVerificationListener;
import tools.mikandi.dev.library.KandiLibs;
import tools.mikandi.dev.login.LibraryLoginResult;
import tools.mikandi.dev.utils.UserInfoObject;

public class TestAppMain extends ActionBarActivity implements OnClickListener
{
    
    private Button btn_forceinstaller, btn_listPurchases, btn_login, btn_logout, btn_loggedIn, btn_valid_user, btn_token_check, btn_make_purchase;
    private Button btn_buy_gold, btn_whoinstalled;
    private Button btn_ad;
    
    private UserInfoObject uio = null;
    private static Context context;
    final static String EXTRA_INSTALLER_PACKAGE_NAME = "android.intent.extra.INSTALLER_PACKAGE_NAME";
    final static String INSTALLER_PACKAGE_NAME = "com.mikandi.vending";
    public static boolean debug = true;
    public static final String tag = "Test Example App";
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
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
        btn_ad = (Button) findViewById(R.id.btn_adcheck);

//--------------------------------------------------------- install check -------------------------------------------------------------		
        // this installer reinstalls the application and sets the installer to the def Mikandi installer to allow
        // you to see and understand how our drm works.
        //	btn_forceinstaller.setOnClickListener(this);
        
        //this button checks the installer of the app. MiKandi sets the installer
        //variable to "com.mikandi.vending" while the installer variable would be
        //null if it is installed by the system. (this also applies to apks installed by the shell.)
        btn_whoinstalled.setOnClickListener(this);
        
        btn_buy_gold.setOnClickListener(this);
        btn_make_purchase.setOnClickListener(this);
        btn_listPurchases.setOnClickListener(this);
        btn_valid_user.setOnClickListener(this);
        btn_token_check.setOnClickListener(this);
        btn_logout.setOnClickListener(this);
        btn_loggedIn.setOnClickListener(this);
        btn_login.setOnClickListener(this);
        btn_ad.setOnClickListener(this);
    }
    
    @SuppressWarnings("serial")
    @Override
    public void onClick(View v)
    {
        // updated instance of for each KandiLibs library.
        uio = UserInfoObject.getInstance(context);
        if (debug && v instanceof Button) Log.i(" ", "'" + ((Button)v).getText() + "' Button Pressed");
        switch (v.getId())
        {
            case R.id.btn_login:
                logUserIn(uio);
                break;
            case R.id.btn_list_purchases:
                dumpPurchaseListPurchases(uio);
                break;
            case R.id.btn_logout:
                KandiLibs.logOutUser(uio);
                break;
            case R.id.btn_loggedIn:
                boolean loggedIn = KandiLibs.isLoggedIn(uio);
                if (debug) Log.i(tag, loggedIn == true ? "Logged in sucessfully " : "Failed to Log in ");
                break;
            case R.id.btn_valid:
                if (debug) Log.i("verifying user", "about to verify user!");
                verifyUser(uio);
                break;
            case R.id.btn_tokencheck:
                String token = "last_test_check";
                boolean testPurchase = KandiLibs.checkPurchase(uio, token);
                if (debug) Log.i("Token Check ", "token check of " + token + " returns :" + testPurchase);
                break;
            case R.id.btn_buyGold:
                buyGold();
                break;
            case R.id.btn_make_purchase:
                String token2 = "porn_package_one";
                String token_description = "this is a test token";
                String token_amount = "200";
                purchaseToken(token2, token_description, token_amount);
                break;
            case R.id.btn_installer:
                isInstallerCorrect(context);
                break;
            case R.id.btn_whoinstalled:
                //	KandiLibs.checkInstaller(context);
                break;
            case R.id.btn_adcheck:
                fullScreenAd(new OnFullScreenAdDisplayedListener()
                {
                    @Override
                    public void AdFinished()
                    {
                        Toast.makeText(uio.getContext(), "Ad finished", Toast.LENGTH_SHORT).show();
                    }
                });
                break;
        }
        ;
    }
    
    // -------------------------------------------------------------------
    protected void fullScreenAd(OnFullScreenAdDisplayedListener l)
    {
        UserInfoObject.getInstance(context).setFullScreenAdListener(l);
        KandiLibs.requestFullScreenAd(this, l);
    }
// ----------------------------------------------------------------	
    
    // -------------------------------------- Purchase Token  -----------------------------------------------------------------------
    protected void purchaseToken(final String token, String token_description,
                                 String token_amount)
    {
        
        KandiLibs.AuthInAppPuchase(uio, token, token_description, token_amount, new OnAuthorizeInAppListener()
        {
            
            @Override
            public void Sucess()
            {
                if (debug) Log.i("Purchase Success", "" + token + " purchased");
            }
            
            @Override
            public void Failed(int code)
            {
                if (debug) Log.i("Purchase failed", "" + token + " was not purchased. Code returned is " + code);
            }
        });
    }
    
    // ------------------------ Buying gold	---------------------------------------------------------------
    protected void buyGold()
    {
        KandiLibs.requestBuyGold(this);
    }
    
    // ------------------------------------------------ verifying user ----------------------------------------------
    protected void verifyUser(final UserInfoObject uio)
    {
        
        if (debug) Log.i("verifying user", "requesting library verify user ");
        KandiLibs.requestUserVerify(uio, new onUserVerificationListener()
        {
            
            @Override
            public void userVerifyFailed(int code)
            {
                Toast.makeText(getApplicationContext(), "user not verified code " + code, Toast.LENGTH_LONG).show();
                if (debug) Log.i(tag, "verify user was unsucessful - user has NOT purchased app");
            }
            
            @Override
            public void userVerifiedSuccessfully()
            {
                Toast.makeText(getApplicationContext(), "Verified user ", Toast.LENGTH_LONG).show();
                if (debug) Log.i(tag, "Verifying was sucessful - user has purchased app");
            }
        });
    }
    
    //--------------------------------------------- Check Token --------------------------------------
    protected void checktoken(final String string, final UserInfoObject uio)
    {
        String temp = "porn_package_test";
        boolean testPurchase = KandiLibs.checkPurchase(uio, temp);
        if (debug) Log.e(tag, "testing the checkPurchase(" + temp + ")" + " test was: " + testPurchase);
    }
    
    // ----------------------------------------------------Purchase History ------------------------------------------------------------ >> -
// for testing
    private void dumpPurchaseListPurchases(final UserInfoObject uio)
    {
        KandiLibs.requestPurchaseHistory(uio, new onPurchaseHistoryListener()
        {
            
            @Override
            public void onSucessfulHistoryRetrieved(List<String> lp)
            {
                LibraryLoginResult myLr = uio.getLoginResult();
                ArrayList<String> myArrayList = myLr.getArrayListTokens();
                for (String s : myArrayList)
                {
                    if (debug) Log.i(tag, "Printing out new purchases from saved lr: " + s);
                }
            }
            
            @Override
            public void onFailedHistoryRetrieved()
            {
                Toast.makeText(getBaseContext(), "failed purchase History no returned list", Toast.LENGTH_LONG).show();
            }
            
            @Override
            public void onNoPurchases()
            {
                Toast.makeText(getBaseContext(), " No Purchases found ", Toast.LENGTH_LONG).show();
            }
        });
    }
    
    // -----------------------------------------------Login ----------------------------------------------------------------------------------
    private void logUserIn(UserInfoObject uio)
    {
        // "this" doesn't reference the correct thing if the code below is
        // within the buttonlistener (this referes to the listener not the activity)
        
        uio = UserInfoObject.getInstance(context);
        KandiLibs.requestLogin(this, uio);
    }
    // ---------------------------------------------- Install intent testing ----------
    
    /**
     * this test is to check if the installer of the app. It will return true if Mikandi
     * installed the application on the device.
     *
     * @param apkUri
     *
     * @return
     */
    public static Intent createInstallIntent(final Uri apkUri)
    {
        final Intent installIntent = new Intent(Intent.ACTION_VIEW);
        if (debug) Log.i("createInstallIntent", "printing install intent after creation " + installIntent.toString());
        installIntent.setDataAndType(apkUri,
                "application/vnd.android.package-archive");
        if (debug) Log.i("createInstallIntent", "set data");
        // provide the installer package name:
        final String installerKey = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH ? Intent.EXTRA_INSTALLER_PACKAGE_NAME
                : EXTRA_INSTALLER_PACKAGE_NAME);
        if (debug) Log.i("createInstallIntent", "installerKey" + installerKey);
        installIntent.putExtra(installerKey, INSTALLER_PACKAGE_NAME);
        installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return installIntent;
    }
    
    public static boolean isInstallerCorrect(Context ctx)
    {
        boolean myb = false;
        
        Toast.makeText(ctx, "Login UserName is " + "", Toast.LENGTH_SHORT).show();
		
		/*
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
		*/
        return myb;
    }
    // -------------------------------------------------------------------------------
}
