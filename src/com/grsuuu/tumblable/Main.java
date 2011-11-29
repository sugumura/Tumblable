package com.grsuuu.tumblable;

import java.io.IOException;

import oauth.signpost.OAuth;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.grsuuu.tumblable.util.MyLog;

public class Main extends Activity {

	public static String APP_NAME;

	public static final String REQUEST_URL = "http://www.tumblr.com/oauth/request_token";
	public static final String ACCESS_URL = "http://www.tumblr.com/oauth/access_token";
	public static final String AUTHORIZE_URL = "http://www.tumblr.com/oauth/authorize";
	public static final String OAUTH_CALLBACK_URL = "tumblable://callback";

	private static SharedPreferences pref = null;

	private static String token, secret, authURL, uripath;

	private static CommonsHttpOAuthConsumer consumer =
			new CommonsHttpOAuthConsumer(Keys.TUMBLR_CONSUMER_KEY, Keys.TUMBLR_CONSUMER_SECRET);
	private static CommonsHttpOAuthProvider provider =
			new CommonsHttpOAuthProvider(REQUEST_URL, ACCESS_URL, AUTHORIZE_URL);

	private static boolean auth = false, browser = false;
	
	private TextView textView;
	private Button button;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		APP_NAME = getString(R.string.app_name);

		MyLog.d("Main / onCreate");

		super.onCreate(savedInstanceState);
		
		pref = PreferenceManager.getDefaultSharedPreferences(this);

		token = pref.getString("TUMBLR_OAUTH_TOKEN", "");
		secret = pref.getString("TUMBLR_OAUTH_TOKEN_SECRET", "");

		if (token != null && token != "" && secret != null && secret != "") {
			MyLog.d("認証OK");
			auth = true; // すでにOAuthで認証しているかのフラグ
		} else {
			setAuthURL(); // OAuth認証のための情報をセットする
		}
		
		setContentView(R.layout.main);

	}

	@Override
	protected void onResume() {
		super.onResume();

		if (auth == false) {

			// 認証画面へ
			if (browser == false) {
				browser = true;
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(authURL)));
			} else {
				Uri uri = getIntent().getData();
				uripath = uri.toString();

				if (uri != null && uripath.startsWith(OAUTH_CALLBACK_URL)) {
					String verifier = uri
							.getQueryParameter(OAuth.OAUTH_VERIFIER);
					try {

						provider.retrieveAccessToken(consumer, verifier);

						token = consumer.getToken();
						secret = consumer.getTokenSecret();
						
						MyLog.d("token:" + token);
						MyLog.d("secret:" + secret);
						
						final Editor editor = pref.edit();
						editor.putString("TUMBLR_OAUTH_TOKEN", token);
						editor.putString("TUMBLR_OAUTH_TOKEN_SECRET", secret);
						editor.commit();

						auth = true;
						
						
					} catch (OAuthMessageSignerException e) {
						e.printStackTrace();
					} catch (OAuthNotAuthorizedException e) {
						e.printStackTrace();
					} catch (OAuthExpectationFailedException e) {
						e.printStackTrace();
					} catch (OAuthCommunicationException e) {
						e.printStackTrace();
					}
				}
			}
		} else {
			MyLog.d("画面描画");
			

			textView = (TextView) findViewById(R.id.textView1);
			button = (Button) findViewById(R.id.button1);
			
			button.setOnClickListener(new View.OnClickListener() {
				
				public void onClick(View v) {
					HttpPost hpost = new HttpPost("http://api.tumblr.com/v2/user/info");
					
					consumer = new CommonsHttpOAuthConsumer(Keys.TUMBLR_CONSUMER_KEY, Keys.TUMBLR_CONSUMER_SECRET);
					consumer.setTokenWithSecret(token, secret);
					
					try {
						consumer.sign(hpost);
					} catch (OAuthMessageSignerException e) {
						e.printStackTrace();
					} catch (OAuthExpectationFailedException e) {
						e.printStackTrace();
					} catch (OAuthCommunicationException e) {
						e.printStackTrace();
					}
					DefaultHttpClient client = new DefaultHttpClient();
					HttpResponse resp = null;
					
					try {
						resp = client.execute(hpost);
					} catch (ClientProtocolException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					
					String s = null;
					try {
						s = EntityUtils.toString(resp.getEntity(), "UTF-8");
						MyLog.d(s);
					} catch (ParseException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					
					try {
						
						JSONArray jo = new JSONObject(s).getJSONObject("response").getJSONObject("user").getJSONArray("blogs");
						MyLog.d(jo.toString());
						MyLog.d(jo.getJSONObject(0).getString("url"));
//						
//						for (int i = 0; i < jo.length(); i++) {
//							MyLog.d(jo.getJSONObject(i).toString());
//						}
						
//						textView.setText(jo.getJSONObject(jo.length() - 1).toString());
//						JSONArray jsons= new JSONObject(s).getJSONObject("response").getJSONObject("user").getJSONArray("blogs");
//						String name = jsons.getJSONObject(0).getJSONObject("name").toString();
//						MyLog.d("name = " + name);
						
						
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			});
		}

	}

	private void setAuthURL() {
		MyLog.d("setAuthURL");
		
		try {
			if ((token == null || token == "")
					&& (secret == null || secret == "") && auth == false
					&& browser == false)
				authURL = provider.retrieveRequestToken(consumer,
						OAUTH_CALLBACK_URL);

		} catch (OAuthMessageSignerException e) {
			e.printStackTrace();
		} catch (OAuthNotAuthorizedException e) {
			e.printStackTrace();
		} catch (OAuthExpectationFailedException e) {
			e.printStackTrace();
		} catch (OAuthCommunicationException e) {
			e.printStackTrace();
		}
	}
}