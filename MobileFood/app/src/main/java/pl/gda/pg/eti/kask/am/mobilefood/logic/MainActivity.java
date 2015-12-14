package pl.gda.pg.eti.kask.am.mobilefood.logic;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import java.util.UUID;

import pl.gda.pg.eti.kask.am.mobilefood.R;

public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {

    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "MainActivity";

    private String serverAddress;
    private String deviceId;
    private GoogleApiClient mGoogleApiClient;
    private String userGoogleId;
    private String userGoogleToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        findViewById(R.id.button_sign_in).setOnClickListener(this);
        findViewById(R.id.button_sign_out).setOnClickListener(this);
        findViewById(R.id.button_show_products).setOnClickListener(this);

        initializeServerAddressAndDeviceId();
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestEmail()
                .build();
        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    private void initializeServerAddressAndDeviceId() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Consts.SHARED_PREFERENCES_NAME,
                MODE_PRIVATE);
        initializeServerAddress(pref);
        initializeDeviceId(pref);
    }

    private void initializeDeviceId(SharedPreferences pref) {
        String stringDeviceId = pref.getString(Consts.SHARED_PREF_DEVICE_ID_KEY, "");
        if (stringDeviceId.isEmpty()) {
            stringDeviceId = UUID.randomUUID().toString();
            Log.d(TAG, "Generated device ID: " + stringDeviceId);
        }
        deviceId = stringDeviceId;
        Log.d(TAG, "Device id: " + deviceId);
    }

    private void initializeServerAddress(SharedPreferences pref) {
        String prefServerAddress = pref.getString(Consts.SHARED_PREF_SERVER_ADDRESS_KEY, "");
        String stringAddress = getString(R.string.server_api_path);
        if (prefServerAddress.isEmpty() || !stringAddress.equals(prefServerAddress)) {
            Log.d(TAG, "Loading server address from strings");
            serverAddress = stringAddress;
        } else {
            Log.d(TAG, "Loaded server address from preferences");
            serverAddress = prefServerAddress;
        }
        Log.d(TAG, "Server address: " + serverAddress);
    }

    private void savePreferences() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Consts.SHARED_PREFERENCES_NAME,
                MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(Consts.SHARED_PREF_SERVER_ADDRESS_KEY, serverAddress);
        editor.putString(Consts.SHARED_PREF_DEVICE_ID_KEY, deviceId);
        editor.commit();
        Log.d(TAG, "Saved to preferences server address: " + serverAddress + " and device Id: " + deviceId);
    }

    @Override
    protected void onStop() {
        super.onStop();

        savePreferences();
    }

    @Override
    public void onStart() {
        super.onStart();

        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (opr.isDone()) {
            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
            // and the GoogleSignInResult will be available instantly.
            Log.d(TAG, "Got cached sign-in");
            GoogleSignInResult result = opr.get();
            handleSignInResult(result);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_sign_in:
                signIn();
                break;
            case R.id.button_sign_out:
                signOut();
                break;
            case R.id.button_show_products:
                showProducts();
                break;
        }
    }

    private void showProducts() {
        Intent productsIntent = new Intent(this, ProductActivity.class);
        productsIntent.putExtra(Consts.GOOGLE_ID_PARAMETER, userGoogleId);
        productsIntent.putExtra(Consts.GOOGLE_ID_TOKEN, userGoogleToken);
        productsIntent.putExtra(Consts.SERVER_ADDRESS, serverAddress);
        productsIntent.putExtra(Consts.DEVICE_ID, deviceId);
        startActivity(productsIntent);
    }

    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        updateUi(false);
                        Log.d(TAG, "logged out successfully");
                    }
                });
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }

    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            GoogleSignInAccount acct = result.getSignInAccount();
            Log.d(TAG, "Logged in successfully");
            Log.d(TAG, "Signed in as: " + acct.getDisplayName());
            Log.d(TAG, "The e-mail is: " + acct.getEmail());
            Log.d(TAG, "The user's Google ID is: " + acct.getId());
            Log.d(TAG, "The ID token is: " + acct.getIdToken());

            userGoogleId = acct.getId();
            userGoogleToken = acct.getIdToken();

            updateUi(true);
        } else {
            String errorMsg = "Could not log in, please check your Internet connection";
            Log.d(TAG, errorMsg);
            Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
            updateUi(false);
        }

    }

    private void updateUi(boolean signedIn) {
        if (signedIn) {
            findViewById(R.id.button_sign_in).setVisibility(View.GONE);
            findViewById(R.id.signed_in_options).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.button_sign_in).setVisibility(View.VISIBLE);
            findViewById(R.id.signed_in_options).setVisibility(View.GONE);
        }
    }

}
