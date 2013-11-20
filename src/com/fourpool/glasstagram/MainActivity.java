package com.fourpool.glasstagram;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;

import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.google.android.glass.widget.CardScrollView;
import com.lightbox.android.photoprocessing.PhotoProcessing;

public class MainActivity extends Activity {
	private static final int TAKE_PHOTO = 0;
	private Twitter twitter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true).setOAuthConsumerKey(SecretKeys.CONSUMER_KEY)
				.setOAuthConsumerSecret(SecretKeys.CONSUMER_SECRET)
				.setOAuthAccessToken(SecretKeys.ACCESS_TOKEN)
				.setOAuthAccessTokenSecret(SecretKeys.ACCESS_TOKEN_SECRET);
		TwitterFactory tf = new TwitterFactory(cb.build());
		twitter = tf.getInstance();

		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		startActivityForResult(intent, TAKE_PHOTO);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent imageReturnedIntent) {
		super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

		switch (requestCode) {
		case TAKE_PHOTO:
			if (resultCode == RESULT_OK) {
				final String path = imageReturnedIntent
						.getStringExtra("picture_file_path");
				File file = new File(path);

				while (!file.exists()) {
					SystemClock.sleep(500);
					Log.e("ASDF", "About to check again");
				}

				final Bitmap bitmap = BitmapUtils
						.decodeSampledBitmapFromPath(imageReturnedIntent
								.getStringExtra("picture_file_path"), 100, 100);
				CardScrollView scrollView = new CardScrollView(this);
				FilterCardScrollAdapter adapter = new FilterCardScrollAdapter(
						bitmap, this);
				scrollView.setAdapter(adapter);
				scrollView.activate();
				scrollView.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						Bitmap originalBitmap = BitmapFactory.decodeFile(path);
						Bitmap bm = PhotoProcessing.filterPhoto(originalBitmap,
								arg2);
						postTweet(bm);
					}

				});
				setContentView(scrollView);
			}
		}
	}

	private void postTweet(final Bitmap bitmap) {
		new Thread() {
			@Override
			public void run() {
				try {
					StatusUpdate update = new StatusUpdate("#glasstagram");

					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					bitmap.compress(CompressFormat.PNG, 0 /* ignored for PNG */,
							bos);
					byte[] bitmapdata = bos.toByteArray();
					ByteArrayInputStream bs = new ByteArrayInputStream(
							bitmapdata);

					update.setMedia("asdf", bs);
					Status status = twitter.updateStatus(update);
					Log.e("MainActivity", status.getText());
				} catch (TwitterException e) {
					Log.e("MainActivity", "Fail", e);
				}
			}
		}.start();
	}
}
