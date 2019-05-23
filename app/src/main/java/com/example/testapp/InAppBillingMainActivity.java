package com.example.testapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.List;

import tools.mikandi.dev.library.KandiLibs;
import tools.mikandi.dev.listeners.OnAuthorizeInAppListener;
import tools.mikandi.dev.listeners.OnFullScreenAdDisplayedListener;
import tools.mikandi.dev.listeners.onPurchaseHistoryListener;
import tools.mikandi.dev.listeners.onUserVerificationListener;
import tools.mikandi.dev.utils.Logger;
import tools.mikandi.dev.utils.UserInfoObject;

public class InAppBillingMainActivity extends AppCompatActivity implements OnClickListener {

    private Button main_btn_forceinstaller, main_btn_listPurchases, main_btn_token_check, main_btn_make_purchase, main_btn_buy_gold, main_btn_ad;

    private ToggleButton main_btn_login_toggle;

    private UserInfoObject userInfoObject = null;
    //    private static Context context;
    // private Context context;
    final static String EXTRA_INSTALLER_PACKAGE_NAME = "android.intent.extra.INSTALLER_PACKAGE_NAME";
    final static String INSTALLER_PACKAGE_NAME = "com.mikandi.vending";
    public static boolean debug = true;
    private boolean loggedIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        Toolbar mainToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mainToolbar);
//        context = getApplicationContext();

        main_btn_login_toggle = (ToggleButton) findViewById(R.id.btn_login_toggle);
        main_btn_buy_gold = (Button) findViewById(R.id.main_btn_buyGold);

        // FIXME: 5/17/2019 are these buttons needed?
        main_btn_listPurchases = (Button) findViewById(R.id.main_btn_list_purchases);
        main_btn_token_check = (Button) findViewById(R.id.main_btn_tokencheck);
        main_btn_make_purchase = (Button) findViewById(R.id.main_btn_make_purchase);
        main_btn_forceinstaller = (Button) findViewById(R.id.main_btn_installer);
        main_btn_ad = (Button) findViewById(R.id.main_btn_adcheck);


        main_btn_login_toggle.setOnClickListener(this);
        main_btn_buy_gold.setOnClickListener(this);

        // FIXME: 5/17/2019 are these listeners needed?
        // this installer reinstalls the application and sets the installer to the def Mikandi installer to allow
        // you to see and understand how our drm works.
        main_btn_forceinstaller.setOnClickListener(this);
        //this button checks the installer of the app. MiKandi sets the installer
        //variable to "com.mikandi.vending" while the installer variable would be
        //null if it is installed by the system. (this also applies to apks installed by the shell.)
        main_btn_make_purchase.setOnClickListener(this);
        main_btn_listPurchases.setOnClickListener(this);
        main_btn_token_check.setOnClickListener(this);
        main_btn_ad.setOnClickListener(this);


        //checking login status for login toggle button
        loggedIn = userInfoObject != null;
        if (Logger.isDebug) Logger.i("user logged in: %1$s", loggedIn);
        main_btn_login_toggle.setChecked(loggedIn);

    }


    @Override
    public void onClick(View v) {
        // updated instance of for each KandiLibs library.
        final Context context = this;
        userInfoObject = UserInfoObject.reload(context);
        if (Logger.isDebug)
            Logger.i("Getting instance of userInfoObject: %1$s", userInfoObject.toString());


        //launching activities according to the button clicked
        if (debug && v instanceof Button)
            if (Logger.isDebug) Logger.i("%s button pressed", getResources().getResourceName(v.getId()));
        switch (v.getId()) {
            case R.id.btn_login_toggle:
                //handling login and logout based on login status
                // TODO: 5/17/2019 use is isChecked method or loggedIn boolean?
                if (main_btn_login_toggle.isChecked()) {

                    logUserIn(userInfoObject);
                    if (Logger.isDebug)
                        Logger.i("Logging user in with user info: %1$s", userInfoObject.toString());
                } else {
                    KandiLibs.logOutUser(context);
                }
                break;
            case R.id.main_btn_buyGold:
                buyGoldOnWeb();
                break;
            case R.id.main_btn_list_purchases:
                listPurchaseHistory(userInfoObject);
                break;
            case R.id.main_btn_make_purchase:
                String token2 = "porn_package_one";
                String token_description = "this is a test token";
                String token_amount = "200";
                purchaseToken(token2, token_description, token_amount);
                break;
            case R.id.main_btn_tokencheck:
                String token = "last_test_check";
                boolean testPurchase = KandiLibs.checkPurchase(context, token);
                if (Logger.isDebug) Logger.i("Checking token: %1$s", token);
                if (Logger.isDebug) Logger.i("Purchased: %1$s", testPurchase);
                break;
            case R.id.main_btn_adcheck:
                fullScreenAd(new OnFullScreenAdDisplayedListener() {
                    @Override
                    public void onAdFinished() {
                        Toast.makeText(context, "Ad finished", Toast.LENGTH_SHORT).show();
                    }
                });
                break;
            case R.id.main_btn_installer:
//                installMikandiFromWeb();
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    /**
     * Logs user in by calling {@link KandiLibs} requestLogin method
     *
     * @param userInfoObject
     */
    private void logUserIn(final UserInfoObject userInfoObject) {
        //        userInfoObject = UserInfoObject.getInstance(context);
        if (Logger.isDebug) Logger.i("userInfoObject instance is: %1$s", userInfoObject.toString());
        KandiLibs.requestLogin(this, userInfoObject);
        // TODO: 5/17/2019 check if login successful?
        if (KandiLibs.isLoggedIn(this)) {
            main_btn_login_toggle.setChecked(loggedIn);
        }
    }

    /**
     * Method launching buying gold on web
     */
    protected void buyGoldOnWeb() {
        KandiLibs.requestBuyGold(this);
    }

    /**
     * Returns log in state of user defined by an {@link UserInfoObject}
     *
     * @param userInfoObject
     * @return
     */
    protected boolean isLoggedIn(final UserInfoObject userInfoObject) {
        return KandiLibs.isLoggedIn(this);
    }


    /**
     * Authorizes an in-app purchase of a token
     *
     * @param token             - the in-app purchase id
     * @param token_description - description of the item
     * @param token_amount      - amount purchased
     */
    protected void purchaseToken(final String token, final String token_description,
                                 final String token_amount) {

        KandiLibs.authorizeInAppPurchase(this, token, token_description, token_amount, new OnAuthorizeInAppListener() {
            @Override
            public void onSuccess() {
                if (Logger.isDebug) Logger.i("Token %1$s purchased successfully", token);
            }

            @Override
            public void onFailure(int code) {
                if (Logger.isDebug)
                    Logger.i("Purchase of token %1$s failed with return code: %2$d", token, code);
            }
        });
    }

    /**
     * Returns the purchase history of the user
     *
     * @param userInfoObject
     */
    private void listPurchaseHistory(final UserInfoObject userInfoObject) {
        KandiLibs.requestPurchaseHistory(this, new onPurchaseHistoryListener() {
            @Override
            public void onSucessfulHistoryRetrieved(List<String> list) {

            }

//            @Override
//            public void onSuccessfulHistoryRetrieved(List<String> purchasedItems) {
//                // FIXME: 3/20/2019
////                LibraryLoginResult libraryLoginResult = userInfoObject.getLoginResult();
////                ArrayList<String> arrayListTokens = libraryLoginResult.getArrayListTokens();
////                for (String s : arrayListTokens) {
////                    if (debug) Log.i(tag, "Printing out new purchases from saved lr: " + s);
////                }
//            }

            @Override
            public void onFailedHistoryRetrieved() {
                Toast.makeText(getBaseContext(), "failed purchase History no returned list", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNoPurchases() {
                Toast.makeText(getBaseContext(), " No Purchases found ", Toast.LENGTH_LONG).show();
            }
        });
    }

    protected void fullScreenAd(OnFullScreenAdDisplayedListener l) {
        UserInfoObject.getInstance(this).setFullScreenAdListener(l);
        KandiLibs.requestFullScreenAd(this, l);
    }

//    protected void installMikandiFromWeb() {
//        KandiLibs.installMiKandi(this);
//    }

    // TODO: 5/17/2019 check the methods below and the corresponding KandiLibs methods checking legitimate users. Which one is needed/correct?

    /**
     * Method verifying if the user purchased the application from MiKandi app store.
     *
     * @param userInfoObject
     */
    protected void verifyUser(final UserInfoObject userInfoObject) {
//        if (Logger.isDebug) Logger.i("Verifying user: %1$s", userInfoObject.toString());
        KandiLibs.requestUserVerify(this, new onUserVerificationListener() {
            @Override
            public void userVerifiedSuccessfully() {
                Toast.makeText(getApplicationContext(), "Verified user ", Toast.LENGTH_LONG).show();
                if (Logger.isDebug) Logger.i("Successful verification: user has purchased the app");
            }

            @Override
            public void userVerifyFailed(int i) {
                Toast.makeText(getApplicationContext(), "user not verified code ", Toast.LENGTH_LONG).show();
                if (Logger.isDebug)
                    Logger.i("Verification unsuccessful");
            }

//            @Override
//            public void userVerifiedSuccessfully(boolean verified, int responseCode) {
//                Toast.makeText(getApplicationContext(), "Verified user ", Toast.LENGTH_LONG).show();
//                if (Logger.isDebug) Logger.i("Successful verification: user has purchased the app");
//            }
//
//            @Override
//            public void userVerifyFailed(boolean verified, int responseCode) {
//                Toast.makeText(getApplicationContext(), "user not verified code " + responseCode, Toast.LENGTH_LONG).show();
//                if (Logger.isDebug)
//                    Logger.i("Verification unsuccessful, response code:  %1$d", responseCode);
//            }
            //addressing failed verification due to server error
// FIXME: 3/20/2019 aldebaran dependency conflict
//            @Override
//            public void userVerifyFailure(com.saguarodigital.returnable.defaultimpl.JSONResponse jsonResponse) {
//            }
        });
    }


    /**
     * @param userInfoObject
     * @return
     */
//    protected boolean isLegitInstall(UserInfoObject userInfoObject) {
//        return KandiLibs.checkInstaller(userInfoObject);
//    }
//    /**
//     *
//     * @param string
//     * @param uio
//     */
//    protected void checktoken(final String string, final UserInfoObject uio) {
//        String temp = "porn_package_test";
//        boolean testPurchase = KandiLibs.checkPurchase(uio, temp);
//        if (debug)
//            Log.e(tag, "testing the checkPurchase(" + temp + ")" + " test was: " + testPurchase);
//    }


    /**
     * this test is to check if the installer of the app. It will return true if Mikandi
     * installed the application on the device.
     *
     * @param apkUri
     * @return
     */
    public static Intent createInstallIntent(final Uri apkUri) {
        final Intent installIntent = new Intent(Intent.ACTION_VIEW);
        if (Logger.isDebug) Logger.i("Create install intent: %1$s", installIntent.toString());
        installIntent.setDataAndType(apkUri,
                "application/vnd.android.package-archive");
        // provide the installer package name:
        final String installerKey = Intent.EXTRA_INSTALLER_PACKAGE_NAME;
        if (Logger.isDebug) Logger.i("Create install intent installerkey: %1$s", installerKey);
        installIntent.putExtra(installerKey, INSTALLER_PACKAGE_NAME);
        installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return installIntent;
    }

//    public static boolean isInstallerCorrect(Context ctx) {
//        boolean myb = false;
//
//        Toast.makeText(ctx, "Login UserName is " + "", Toast.LENGTH_SHORT).show();
//
//		/*
//		final PackageManager pm = ctx.getPackageManager();
//		if (debug) Log.i("isInstallerCorrect" , "retreived package manager" );
//		File f = new File(context.getPackageResourcePath());
//		if (debug) Log.i("isInstallerCorrect" , "File created : " + f.toString() );
//		Uri u = Uri.fromFile(f);
//		if (debug) Log.i("isInstallerCorrect" , "printing out uri:" + u.toString());
//		String installer_initial = pm.getInstallerPackageName(ctx.getApplicationContext().getPackageName());
//		if (debug) Log.i("isInstallerCorrect", "printing out installer_initial " + installer_initial);
//		Intent i = createInstallIntent(u);
//		if (debug) Log.i("isInstallerCorrect" , " printing out intent : " + i.toString());
//		ctx.startActivity(i);
//		String installer_final = pm.getInstallerPackageName(ctx.getApplicationContext().getPackageName());
//		if (debug) Log.i("isInstallerCorrect" , "installer_final" + installer_final );
//		if (debug) Log.i("installer test" , "Initial (should be null) + : " + installer_initial + ", after intent the install is : " + installer_final);
//		myb = InstallerCheck.checkInstaller(ctx);
//		*/
//        return myb;
//    }
}
