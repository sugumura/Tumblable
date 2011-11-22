package com.grsuuu.tumblable;

import oauth.signpost.OAuth;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.grsuuu.tumblable.util.MyLog;

public class Main extends Activity {

	private String CONSUMER_KEY;
	private String CONSUMER_SECRET;

	private static final String REQUEST_TOKEN_URL = "https://www.tumblr.com/oauth/request_token";
	private static final String ACCESS_TOKEN_URL = "https://www.tumblr.com/oauth/access_token";
	private static final String AUTH_URL = "https://www.tumblr.com/oauth/authorize";
	private static final String CALLBACK_URL = "tumblable://tumblable.com/ok";

	public static String APP_NAME;

	protected Context context;
	protected Activity activity;
	public String OAuthAccessKey;
	public String OAuthAccessSecret;

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		APP_NAME = getString(R.string.app_name);
		CONSUMER_KEY = getString(R.string.consumerKey);
		CONSUMER_SECRET = getString(R.string.consumerSecret);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		context = getApplicationContext();
		activity = this;

		Button authButton = (Button) findViewById(R.id.authentication);
		authButton.setOnClickListener(new TumblrOAuth());

	}

	@Override
	protected void onResume() {
		super.onResume();
		MyLog.i("onResume / start");
	}

	/*
	 * @author Suguru OAuth認証処理クラス
	 */
	class TumblrOAuth implements View.OnClickListener {

		CommonsHttpOAuthConsumer consumer = new CommonsHttpOAuthConsumer(
				CONSUMER_KEY, CONSUMER_SECRET);

		// It uses this signature by default
		// consumer.setMessageSigner(new HmacSha1MessageSigner());

		CommonsHttpOAuthProvider provider = new CommonsHttpOAuthProvider(
				REQUEST_TOKEN_URL, ACCESS_TOKEN_URL, AUTH_URL);

		public void onClick(View v) {

			MyLog.d("Main / authButton / OAuth / onClick");
			// To get the oauth token after the user has granted permissions
			Uri uri = activity.getIntent().getData();
			if (uri != null) {

				String token = uri.getQueryParameter(OAuth.OAUTH_TOKEN);
				String verifier = uri.getQueryParameter(OAuth.OAUTH_VERIFIER);
				
				MyLog.d("Token:" + token);
				MyLog.d("Verifier:" + verifier);

				try {
					MyLog.d("test");
					
					provider.retrieveAccessToken(consumer, verifier);
					
					MyLog.d("test2");
					
					OAuthAccessKey = consumer.getToken();
					OAuthAccessSecret = consumer.getTokenSecret();
					
					
					MyLog.d(OAuthAccessKey);
					MyLog.d(OAuthAccessSecret);
					
				} catch (OAuthMessageSignerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (OAuthNotAuthorizedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (OAuthExpectationFailedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (OAuthCommunicationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				

				
			} else {

				String authUrl;
				try {
					authUrl = provider.retrieveRequestToken(consumer,
							CALLBACK_URL);
					MyLog.d("Auth url:" + authUrl);

					startActivity(new Intent("android.intent.action.VIEW",
							Uri.parse(authUrl)));

				} catch (OAuthMessageSignerException e) {
					MyLog.d("Main / OAuthMessageSignerException / ", e);
					e.printStackTrace();
				} catch (OAuthNotAuthorizedException e) {
					MyLog.d("Main / OAuthNotAuthorizedException / ", e);
					e.printStackTrace();
				} catch (OAuthExpectationFailedException e) {
					MyLog.d("Main / OAuthExpectationFailedException / ", e);
					e.printStackTrace();
				} catch (OAuthCommunicationException e) {
					MyLog.d("Main / OAuthCommunicationException / ", e);
					e.printStackTrace();
				}

			}
		}
	}
}