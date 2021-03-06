package com.weixin.ui;

import com.weixin.main.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

public class SearchDevicesView extends RelativeLayout {

	private float offsetArgs = 0;
	private boolean isSearching = true;
	private Bitmap bitmap;
	private Bitmap bitmap1;
	private Bitmap bitmap2;
	private Context context;

	public boolean isSearching() {
		return isSearching;
	}

	public void setSearching(boolean isSearching) {
		this.isSearching = isSearching;
		offsetArgs = 0;
		invalidate();
	}

	public SearchDevicesView(Context context) {
		super(context);
		this.context = context;
		initBitmap();
	}

	public SearchDevicesView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		initBitmap();
	}

	public SearchDevicesView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.context = context;
		initBitmap();
	}

	private void initBitmap() {
		if (bitmap == null) {
			bitmap = Bitmap.createBitmap(BitmapFactory.decodeResource(
					context.getResources(), R.drawable.gplus_search_bg));
		}
		if (bitmap1 == null) {
			bitmap1 = Bitmap.createBitmap(BitmapFactory.decodeResource(
					context.getResources(), R.drawable.good));
		}
		if (bitmap2 == null) {
			bitmap2 = Bitmap.createBitmap(BitmapFactory.decodeResource(
					context.getResources(), R.drawable.gplus_search_args));
		}
	}

	// @SuppressLint("DrawAllocation")
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawBitmap(bitmap, getWidth() / 2 - bitmap.getWidth() / 2,
				getHeight() / 2 - bitmap.getHeight() / 2, null);
		
		if (isSearching) {

			Rect rMoon = new Rect(getWidth() / 2 - bitmap2.getWidth(),
					getHeight() / 2, getWidth() / 2, getHeight() / 2
							+ bitmap2.getHeight());
			canvas.rotate(offsetArgs, getWidth() / 2, getHeight() / 2);
			canvas.drawBitmap(bitmap2, null, rMoon, null);
			offsetArgs = offsetArgs + 3;
		} else {

			canvas.drawBitmap(bitmap2, getWidth() / 2 - bitmap2.getWidth(),
					getHeight() / 2, null);
		}

		if (isSearching) {
			canvas.rotate(-offsetArgs, getWidth() / 2, getHeight() / 2);
			canvas.drawBitmap(bitmap1, getWidth() / 2 - bitmap1.getWidth() / 2,
					getHeight() / 2 - bitmap1.getHeight() / 2, null);
			invalidate();
		}
	}

}
