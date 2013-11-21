package com.fourpool.glasstagram;

import java.io.File;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import com.google.android.glass.widget.CardScrollView;
import com.squareup.otto.Subscribe;

public class MainActivity extends Activity {
	private static final String TAG = MainActivity.class.getSimpleName();
	private static final String EXTRA_PICTURE_FILE_PATH = "picture_file_path";
	private static final int TAKE_PHOTO = 0;
	private static final int SPEECH_REQUEST = 1;

	private CardScrollView scrollView;
	private Bitmap downsampledBitmap;

	private String selectedFilePath;
	private int selectedFilterIndex;
	private String caption = "";

	// yolo
	private boolean photoTakingPhase = true;

	@Override
	protected void onCreate(Bundle b) {
		super.onCreate(b);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	@Override
	public void onResume() {
		super.onResume();

		BusFactory.getInstance().register(this);

		if (photoTakingPhase) {
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			startActivityForResult(intent, TAKE_PHOTO);
		}
	}

	@Override
	public void onPause() {
		super.onPause();

		BusFactory.getInstance().unregister(this);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent resultIntent) {
		super.onActivityResult(requestCode, resultCode, resultIntent);
		Log.d(TAG, "onActivityResult called");

		switch (requestCode) {
		case TAKE_PHOTO:
			if (resultCode == RESULT_OK) {
				setContentView(R.layout.card_loading_image);

				final String path = resultIntent
						.getStringExtra(EXTRA_PICTURE_FILE_PATH);

				Intent intent = new Intent(this, PrepareFileIntentService.class);
				intent.putExtra(PrepareFileIntentService.EXTRA_IMAGE_PATH, path);
				startService(intent);

				photoTakingPhase = false;
			}
			break;
		case SPEECH_REQUEST:
			if (resultCode == RESULT_OK) {
				List<String> results = resultIntent
						.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
				caption = results.get(0);
				Log.d(TAG, "Caption = " + caption);
				scrollView.deactivate();
				scrollView.setAdapter(new FilterCardScrollAdapter(
						downsampledBitmap, caption, this));
				scrollView.activate();

				scrollView.setSelection(selectedFilterIndex);
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_share_to_twitter:
			setContentView(R.layout.card_uploading_to_twitter);

			Intent intent = new Intent(this, TweetIntentService.class);
			intent.putExtra(TweetIntentService.EXTRA_IMAGE_FILE_PATH,
					selectedFilePath);
			intent.putExtra(TweetIntentService.EXTRA_FILTER_INDEX,
					selectedFilterIndex);
			intent.putExtra(TweetIntentService.EXTRA_CAPTION, caption);
			startService(intent);
			return true;
		case R.id.action_add_caption:
			Intent speechIntent = new Intent(
					RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
			startActivityForResult(speechIntent, SPEECH_REQUEST);
			return true;
		default:
			throw new RuntimeException();
		}
	}

	@Subscribe
	public void onImageFileReady(ImageFileReadyEvent event) {
		File imageFile = event.getImageFile();
		final String path = imageFile.getPath();

		downsampledBitmap = BitmapUtils.decodeSampledBitmapFromPath(path, 200,
				200);
		scrollView = new CardScrollView(this);
		FilterCardScrollAdapter adapter = new FilterCardScrollAdapter(
				downsampledBitmap, null, this);
		scrollView.setAdapter(adapter);
		scrollView.activate();
		scrollView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				selectedFilePath = path;
				selectedFilterIndex = position;
				openOptionsMenu();
			}
		});

		setContentView(scrollView);
	}

	@Subscribe
	public void onImageTweeted(ImageTweetedEvent event) {
		Log.d(TAG, "Received image tweeted event");
		setContentView(R.layout.card_upload_success);
	}

	@Subscribe
	public void onError(ErrorEvent event) {
		Toast.makeText(this, event.getError(), Toast.LENGTH_LONG).show();
		finish();
	}
}
