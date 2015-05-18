package com.weixin.ui;

import com.weixin.main.FileSelectActivity;
import com.weixin.main.R;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ImageBtn extends LinearLayout {

	private ImageView imageView;
	private TextView textView;
	private TextView myiptv;
	private Context mContext;
	private String ip;
	private String otherip;

	public ImageBtn(Context context) {
		super(context);
		this.mContext=context;
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.imagebtn, this);
		imageView = (ImageView) findViewById(R.id.imageView1);
		textView = (TextView) findViewById(R.id.textView1);
		myiptv=(TextView) findViewById(R.id.myip_tv);
		imageView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Bundle bundle=new Bundle();
				bundle.putString("self_ip", ip);
				bundle.putString("target_ip", otherip);
//				textView.setText("hehe");
				Intent it=new Intent(mContext,FileSelectActivity.class);
				it.putExtras(bundle);
				mContext.startActivity(it);
			}
		});
	}
	

	public ImageBtn(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.imagebtn, this);
		imageView = (ImageView) findViewById(R.id.imageView1);
		textView = (TextView) findViewById(R.id.textView1);
	}

	/**
	 * 设置图片资源
	 */
	public void setImageResource(int resId) {
		imageView.setImageResource(resId);
	}

	/**
	 * 设置显示的文字
	 */
	public void setTextViewText(String text) {
		textView.setText(text);
	}

	public void setMyIpTextViewText(String text){
		myiptv.setText(text);
	}
	
	public void setIP(String ip){
		this.ip=ip;
	}
	
	public String getIP(){
		return ip;
	}
	
	public void setOhterIP(String otherip){
		this.otherip=otherip;
	}
	
	public String getOtherIP(){
		return otherip;
	}
	
}
