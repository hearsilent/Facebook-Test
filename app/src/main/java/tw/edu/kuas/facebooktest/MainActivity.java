package tw.edu.kuas.facebooktest;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

	CallbackManager callbackManager;
	public static String TAG = "HearSilent";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		printHashKey(this);
		// Initialize the SDK before executing any other operations,
		// especially, if you're using Facebook UI elements.
		FacebookSdk.sdkInitialize(this);

		setContentView(R.layout.activity_main);

		callbackManager = CallbackManager.Factory.create();
		LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
		loginButton.setReadPermissions(Arrays.asList("public_profile", "email", "user_birthday"));

		if (AccessToken.getCurrentAccessToken() != null) {
			Log.d(TAG, "Has Login");
		} else {
			Log.d(TAG, "Not Login");
		}

		// Callback registration
		loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
			@Override
			public void onSuccess(LoginResult loginResult) {

				GraphRequest request = GraphRequest
						.newMeRequest(AccessToken.getCurrentAccessToken(),
								new GraphRequest.GraphJSONObjectCallback() {

									@Override
									public void onCompleted(JSONObject object,
									                        GraphResponse response) {
										try {
											Log.d(TAG, object.optString("name"));
											Log.d(TAG, object.optString("link"));
											Log.d(TAG, object.optString("id"));
											Log.d(TAG, object.optString("gender"));
											Log.d(TAG, object.optString("email"));
											Log.d(TAG, object.optString("birthday"));
											Log.d(TAG, object.getJSONObject("picture")
													.getJSONObject("data").optString("url"));
											//https://graph.facebook.com/{user-id}/picture?width=1000&height=1000
										} catch (JSONException e) {
											e.printStackTrace();
										}
									}
								});

				Bundle parameters = new Bundle();
				parameters.putString("fields", "id,name,link,gender,birthday,picture,email");
				request.setParameters(parameters);
				request.executeAsync();
			}

			@Override
			public void onCancel() {
				Log.d(TAG, "Cancel");
			}

			@Override
			public void onError(FacebookException exception) {
				Log.d(TAG, exception.toString());
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		callbackManager.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// Logs 'install' and 'app activate' App Events.
		AppEventsLogger.activateApp(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		// Logs 'app deactivate' App Event.
		AppEventsLogger.deactivateApp(this);
	}

	public static void printHashKey(Context context) {
		try {
			PackageInfo info =
					context.getPackageManager().getPackageInfo(TAG, PackageManager.GET_SIGNATURES);
			for (Signature signature : info.signatures) {
				MessageDigest md = MessageDigest.getInstance("SHA");
				md.update(signature.toByteArray());
				String keyHash = Base64.encodeToString(md.digest(), Base64.DEFAULT);
				Log.d(TAG, "keyHash: " + keyHash);
			}
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}
}
