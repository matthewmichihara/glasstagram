package com.fourpool.glasstagram;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.glass.widget.CardScrollAdapter;
import com.lightbox.android.photoprocessing.PhotoProcessing;

public class FilterCardScrollAdapter extends CardScrollAdapter {
	private final Bitmap bitmap;
	private final Context context;
	private final static int SIZE = 12;

	private static final int[] FILTER_RES_IDS = new int[] {
			R.string.filter_original, R.string.filter_instafix,
			R.string.filter_ansel, R.string.filter_testino,
			R.string.filter_xpro, R.string.filter_retro, R.string.filter_bw,
			R.string.filter_sepia, R.string.filter_cyano,
			R.string.filter_georgia, R.string.filter_sahara,
			R.string.filter_hdr };

	public FilterCardScrollAdapter(Bitmap bitmap, Context context) {
		this.bitmap = bitmap;
		this.context = context;
	}

	@Override
	public int findIdPosition(Object arg0) {
		return -1;
	}

	@Override
	public int findItemPosition(Object arg0) {
		return -1;
	}

	@Override
	public int getCount() {
		return 12;
	}

	@Override
	public Object getItem(int arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View v = inflater.inflate(R.layout.card_filtered_layout, arg2, false);
		ImageView filteredImage = (ImageView) v
				.findViewById(R.id.filtered_image);
		TextView filteredName = (TextView) v.findViewById(R.id.filter_name);
		
		Bitmap bm = PhotoProcessing.filterPhoto(bitmap.copy(Config.ARGB_8888, true), arg0);
		
		filteredImage.setImageBitmap(bm);
		filteredName.setText(FILTER_RES_IDS[arg0]);

		
		
		return v;
	}
}
