package com.fourpool.glasstagram;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;
import android.app.Application;

public class GlasstagramApplication extends Application {

	private Twitter twitter;

	@Override
	public void onCreate() {
		super.onCreate();

		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true).setOAuthConsumerKey(SecretKeys.CONSUMER_KEY)
				.setOAuthConsumerSecret(SecretKeys.CONSUMER_SECRET)
				.setOAuthAccessToken(SecretKeys.ACCESS_TOKEN)
				.setOAuthAccessTokenSecret(SecretKeys.ACCESS_TOKEN_SECRET);
		TwitterFactory twitterFactory = new TwitterFactory(cb.build());
		twitter = twitterFactory.getInstance();
	}

	public Twitter getTwitter() {
		return twitter;
	}
}
