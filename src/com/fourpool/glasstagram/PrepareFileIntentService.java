package com.fourpool.glasstagram;

import java.io.File;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

public class PrepareFileIntentService extends IntentService {
	private static final String TAG = PrepareFileIntentService.class
			.getSimpleName();
	public static final String EXTRA_IMAGE_PATH = "extra_image_path";

	public PrepareFileIntentService() {
		super(PrepareFileIntentService.class.getSimpleName());
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		String imagePath = intent.getStringExtra(EXTRA_IMAGE_PATH);
		final File file = new File(imagePath);

		// yolo
		while (!file.exists()) {
			Log.d(TAG, "Waiting for file to exist");
			SystemClock.sleep(500);
		}

		Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable() {
			@Override
			public void run() {
				BusFactory.getInstance().post(new ImageFileReadyEvent(file));
			}
		});
	}
}
