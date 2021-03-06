package com.fourpool.glasstagram;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.lightbox.android.photoprocessing.PhotoProcessing;

public class TweetIntentService extends IntentService {
	private static final String TAG = TweetIntentService.class.getSimpleName();

	public static final String EXTRA_IMAGE_FILE_PATH = "extra_image_file_path";
	public static final String EXTRA_FILTER_INDEX = "extra_filter_index";
	public static final String EXTRA_CAPTION = "extra_caption";

	private final Handler handler = new Handler(Looper.getMainLooper());

	public TweetIntentService() {
		super(TweetIntentService.class.getSimpleName());
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(TAG, "Started TweetIntentService");

		String imagePath = intent.getStringExtra(EXTRA_IMAGE_FILE_PATH);
		int filterIndex = intent.getIntExtra(EXTRA_FILTER_INDEX, -1);
		String caption = intent.getStringExtra(EXTRA_CAPTION);

		GlasstagramApplication app = (GlasstagramApplication) getApplication();
		Twitter twitter = app.getTwitter();

		try {
			final Bitmap originalBitmap = BitmapUtils
					.decodeSampledBitmapFromPath(imagePath, 300, 300);
			Bitmap bm = PhotoProcessing
					.filterPhoto(originalBitmap, filterIndex);

			StatusUpdate update;
			if (TextUtils.isEmpty(caption)) {
				update = new StatusUpdate("#glasstagram");
			} else {
				update = new StatusUpdate(caption + " #glasstagram");
			}

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			bm.compress(CompressFormat.PNG, 0 /* ignored for PNG */, bos);
			byte[] bitmapdata = bos.toByteArray();
			ByteArrayInputStream bs = new ByteArrayInputStream(bitmapdata);

			update.setMedia("glasstagram", bs);
			twitter.updateStatus(update);

			handler.post(new Runnable() {
				@Override
				public void run() {
					BusFactory.getInstance().post(new ImageTweetedEvent());
				}
			});
		} catch (TwitterException e) {
			Log.e(TAG, "Something went wrong", e);

			handler.post(new Runnable() {
				@Override
				public void run() {
					BusFactory.getInstance().post(
							new ErrorEvent("Unable to upload to Twitter"));
				}
			});
		}
	}

}
