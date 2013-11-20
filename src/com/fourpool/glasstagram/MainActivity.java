package com.fourpool.glasstagram;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import com.google.android.glass.widget.CardScrollView;
import com.squareup.otto.Subscribe;

public class MainActivity extends Activity {
	private static final String TAG = MainActivity.class.getSimpleName();
	private static final String EXTRA_PICTURE_FILE_PATH = "picture_file_path";
	private static final int TAKE_PHOTO = 0;

	private String selectedFilePath;
	private int selectedFilterIndex;

	// yolo
	private boolean photoTakingPhase = true;

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
			Intent imageReturnedIntent) {
		super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
		Log.d(TAG, "onActivityResult called");

		switch (requestCode) {
		case TAKE_PHOTO:
			if (resultCode == RESULT_OK) {
				setContentView(R.layout.card_loading_image);

				final String path = imageReturnedIntent
						.getStringExtra(EXTRA_PICTURE_FILE_PATH);

				Intent intent = new Intent(this, PrepareFileIntentService.class);
				intent.putExtra(PrepareFileIntentService.EXTRA_IMAGE_PATH, path);
				startService(intent);

				photoTakingPhase = false;
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
			startService(intent);
			return true;
		default:
			throw new RuntimeException();
		}
	}

	@Subscribe
	public void onImageFileReady(ImageFileReadyEvent event) {
		File imageFile = event.getImageFile();
		final String path = imageFile.getPath();

		final Bitmap bitmap = BitmapUtils.decodeSampledBitmapFromPath(path,
				200, 200);
		CardScrollView scrollView = new CardScrollView(this);
		FilterCardScrollAdapter adapter = new FilterCardScrollAdapter(bitmap,
				this);
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
