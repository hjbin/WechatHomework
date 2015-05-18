package com.weixin.adapter;

import java.util.List;
import java.util.Map;

import com.weixin.main.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class FilelistAdatper extends BaseAdapter{
	
	private Context context;
	private List<Map<String,Object>> items;
	LayoutInflater mInflater;
	
	public FilelistAdatper(Context context,List<Map<String,Object>> items){
		this.context=context;
		this.mInflater=LayoutInflater.from(context);
		this.items=items;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return items.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		
		int picResource=(Integer) items.get(position).get("img");
		String filename=(String) items.get(position).get("name");
		String filepath=(String)items.get(position).get("path");
		if(convertView==null){
			convertView=this.mInflater.inflate(R.layout.item_row,null);
		}
		
		ImageView iv_img=(ImageView)convertView.findViewById(R.id.imageView1);
		TextView tv_filename=(TextView)convertView.findViewById(R.id.tv_filename);
		TextView tv_filepath=(TextView)convertView.findViewById(R.id.tv_filepath);
		iv_img.setImageDrawable(context.getResources().getDrawable(picResource));
		
		tv_filename.setMaxEms(20);
		tv_filename.setSingleLine();
		tv_filename.setText(filename);
		tv_filepath.setText(filepath);
		return convertView;
		
		
	}

}

