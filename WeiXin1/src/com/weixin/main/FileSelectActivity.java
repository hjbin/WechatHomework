package com.weixin.main;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.weixin.adapter.FilelistAdatper;
import com.weixin.globals.GlobalValue;
import com.weixin.thread.Server;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class FileSelectActivity extends Activity implements
		OnItemClickListener, OnItemLongClickListener {

	// 目录路径，初始为sdcard,后面可以直接用来打开file并发送
	private String path = "/sdcard";
	private ListView fileview;
	private List<Map<String, Object>> items;
	private FilelistAdatper mAdapter;
	private String self_ip;
	private String target_ip;
	private File selectedFile = null;
	private int port = 8888;
	private Handler progressHandler;
	/* 发确认用的端口号* */
	private int confirmPort = 8885;
	/* 发文件的端口号* */
	private int filePort = 7777;
	/* 发文件进度* */
	private double transferPercernt = 0;

	private AlertDialog.Builder progressDialog;
	private ProgressDialog pd2;

	/* 接收方是否确认接收* */
	private int clientChectAccept = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		fileview = (ListView) findViewById(R.id.lv_file);
		progressDialog = new AlertDialog.Builder(this);
		listDir(path);
		Intent it = getIntent();
		Bundle bundle = it.getExtras();
		self_ip = bundle.getString("self_ip");
		target_ip = bundle.getString("target_ip");

	}

	private void listDir(String path) {
		// TODO Auto-generated method stub
		items = bindList(path);
		mAdapter = new FilelistAdatper(this, items);
		fileview.setAdapter(mAdapter);
		fileview.setOnItemClickListener(this);
		fileview.setOnItemLongClickListener(this);
	}

	private List<Map<String, Object>> bindList(String path) {
		// TODO Auto-generated method stub
		File[] files = new File(path).listFiles();
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>(
				files.length);
		Map<String, Object> root = new HashMap<String, Object>();
		root.put("name", "/sdcard");
		root.put("img", R.drawable.rootfolder);
		root.put("path", "root directory");
		list.add(root);
		Map<String, Object> pmap = new HashMap<String, Object>();
		pmap.put("name", "Back");
		pmap.put("img", R.drawable.folder);
		pmap.put("path", "parent Directory");
		list.add(pmap);
		for (File file : files) {
			Map<String, Object> map = new HashMap<String, Object>();
			if (file.isDirectory()) {
				map.put("img", R.drawable.folder);
			} else {
				map.put("img", R.drawable.document);
			}

			map.put("name", file.getName());
			map.put("path", file.getPath());
			list.add(map);
		}
		return list;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long arg3) {
		// TODO Auto-generated method stub
		if (position == 0) {
			path = "/sdcard";
			listDir(path);
		} else if (position == 1) {
			toParent();
		} else {
			if (items != null) {
				path = (String) items.get(position).get("path");
				File file = new File(path);
				if (file.isDirectory()) {
					listDir(path);
				} else {
					Toast.makeText(this, "cannot open", Toast.LENGTH_SHORT)
							.show();
				}
			}
		}
	}

	private void toParent() {
		// TODO Auto-generated method stub
		File file = new File(path);
		File parent = file.getParentFile();
		if (parent == null) {
			listDir(path);
		} else {
			path = parent.getAbsolutePath();
			listDir(path);
		}
	}

	// 在这里写一个对话框问用户是否发送该文件，然后可以转跳到发送的activity并把path变量传过去
	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View view,
			int position, long arg3) {
		// TODO Auto-generated method stub
		// Toast.makeText(FileSelectActivity.this, "long",
		// Toast.LENGTH_SHORT).show();
		String fpath = (String) items.get(position).get("path");

		selectedFile = new File(fpath);

		if (!selectedFile.isDirectory()) {
			AlertDialog.Builder dialog = new AlertDialog.Builder(
					FileSelectActivity.this);
			dialog.setTitle("提示");
			dialog.setMessage("是否给想用户" + target_ip + "发送该文件?");
			dialog.setCancelable(false);
			dialog.setPositiveButton("确定",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							try {
								System.out.println("梁慰乐0");
								new Thread() {
									public void run() {
										System.out.println("梁慰乐1");
										try {

											System.out.println("梁慰乐2");
											// selfIP
											InetAddress serverHost = InetAddress
													.getByName(self_ip);

											// 数据包socket，UDP发送
											DatagramSocket mySocket = null;
											mySocket = new DatagramSocket(
													confirmPort, serverHost);
											System.out.println("梁慰乐3");

											// 获取文件名
											String fileName = selectedFile
													.getName();

											System.out.println(fileName);

											// 接收方IP
											InetAddress receiverHost = null;
											receiverHost = InetAddress
													.getByName(target_ip);
											System.out.println("梁慰乐4");
											System.out.println(receiverHost);

											// UDP传文件名和IP到客户端
											ByteArrayOutputStream bao = new ByteArrayOutputStream();
											DataOutputStream dataos = new DataOutputStream(
													bao);

											dataos.writeUTF(fileName);

											dataos.writeUTF(self_ip);
											System.out.println("梁慰乐5");
											byte[] bufferFileName = bao
													.toByteArray();
											DatagramPacket datagramFileName = new DatagramPacket(
													bufferFileName,
													bufferFileName.length,
													receiverHost, port);
											// 发送数据包
											mySocket.send(datagramFileName);
											System.out.println("梁慰乐6");

											// 接收 接收方是否可以接收文件的结果
											byte[] buffer = bao.toByteArray();
											DatagramPacket datagram = new DatagramPacket(
													buffer, buffer.length,
													serverHost, port);
											mySocket.receive(datagram);

											// 读取结果
											ByteArrayInputStream bais = new ByteArrayInputStream(
													buffer);
											DataInputStream dis = new DataInputStream(
													bais);
											clientChectAccept = dis.readInt();
											System.out
													.println(clientChectAccept);
											if (clientChectAccept == 1) {
												handler.sendEmptyMessage(0x1234);
											} else {
												handler.sendEmptyMessage(0x1235);
											}

											// 关闭Socket
											mySocket.close();
										} catch (Exception e) {
											e.printStackTrace();
										}
									}
								}.start();
							} catch (Exception e) {
								e.printStackTrace();
							}

						}
					});
			dialog.show();
		} else {
			Toast.makeText(FileSelectActivity.this, "can not send folder",
					Toast.LENGTH_SHORT).show();
		}
		return true;
	}

	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == 0x1234) {
				AlertDialog.Builder dialog = new AlertDialog.Builder(
						FileSelectActivity.this);
				dialog.setTitle("提示");
				dialog.setMessage("对方同意接收文件，是否开始传输文件？");
				dialog.setCancelable(false);
				dialog.setPositiveButton("是",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								try {

									Log.e("aaa", "sssss");

									/*
									 * 创建ProgressDialog显示文件传输进度
									 */
									pd2 = new ProgressDialog(
											FileSelectActivity.this);
									pd2.setMax(100);
									pd2.setTitle("文件正在传输");
									pd2.setMessage("文件传输的百分比");
									pd2.setCancelable(false);
									pd2.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
									pd2.setIndeterminate(false);
									pd2.show();

									new Thread() {

										// 接收方IP
										InetAddress receiverHost = InetAddress
												.getByName(target_ip);

										public void run() {

											try {

												Log.e("1", "开始传输");
												// 数据包socket，UDP发送
												DatagramSocket mySocket = new DatagramSocket();

												// 获取文件大小
												long fileLength = selectedFile
														.length();
												// UDP传文件大小到客户端
												ByteArrayOutputStream baos = new ByteArrayOutputStream();
												DataOutputStream dos = new DataOutputStream(
														baos);
												dos.writeLong(fileLength);

												byte[] bufferLength = baos
														.toByteArray();
												DatagramPacket datagramLength = new DatagramPacket(
														bufferLength,
														bufferLength.length,
														receiverHost, filePort);
												// 发送数据包
												mySocket.send(datagramLength);

												// 获取文件名
												String fileName = selectedFile
														.getName();
												// UDP传文件名到客户端
												ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
												DataOutputStream dos2 = new DataOutputStream(
														baos2);
												dos2.writeUTF(fileName);

												byte[] bufferFileName = baos2
														.toByteArray();
												DatagramPacket datagramFileName = new DatagramPacket(
														bufferFileName,
														bufferFileName.length,
														receiverHost, filePort);
												// 发送数据包
												mySocket.send(datagramFileName);

												// 关闭socket
												mySocket.close();

												// 接收线程的数目
												int threadNum = 0;

												if (fileLength < 20971520) {
													threadNum = 1;
												} else if (fileLength < 104857600) {
													threadNum = 3;
												} else {
													threadNum = 5;
												}

												Log.e("2", "新建连接socket");

												// 新建连接Socket
												ServerSocket connectionSocket = new ServerSocket(
														filePort);

												// Socket的list
												List<Server> serverList = new ArrayList<Server>();

												// Message startmsg=new
												// Message();
												// startmsg.what=0x1334;
												// //startmsg.obj=transferPercernt;
												// handler.sendMessage(startmsg);

												// 新建的线程数目
												int existingThreadNum = 0;

												// 接收连接，新建线程
												while (existingThreadNum < threadNum) {

													System.out.println("新建线程");

													// 新建数据socket
													Socket dataSocket = connectionSocket
															.accept();
													System.out
															.println("接收方发来请求");

													// 新建发送线程
													Server server = new Server(
															dataSocket,
															selectedFile,
															existingThreadNum);

													existingThreadNum++;

													// 启动线程
													if (!server.isAlive())
														server.start();

													// 将Server加入到list里面去
													serverList.add(server);

												}

												Log.e("3", "检测发送是否成功");
												// 检查文件传输是否完成
												while (!checkServerState(serverList)) {

													switch (threadNum) {
													case 1:
														transferPercernt = (double) GlobalValue.globalTotal0
																/ fileLength;
													case 3:
														transferPercernt = (double) (GlobalValue.globalTotal0
																+ GlobalValue.globalTotal1 + GlobalValue.globalTotal2)
																/ fileLength;
													case 5:
														transferPercernt = (double) (GlobalValue.globalTotal0
																+ GlobalValue.globalTotal1
																+ GlobalValue.globalTotal2
																+ GlobalValue.globalTotal3 + GlobalValue.globalTotal4)
																/ fileLength;

													}

													Log.e("9", transferPercernt
															+ "");
													// Message msg=new
													// Message();
													// msg.what=0x1333;
													// msg.obj=transferPercernt;
													// handler.sendMessage(msg);

													handler.sendEmptyMessage(0x1333);

												}

												connectionSocket.close();

												Log.e("4", "传输完成");

											} catch (Exception e) {

											}
										};

										boolean checkServerState(
												List<Server> serverList) {
											// 线程状态
											boolean complete = false;

											// 检查
											for (int i = 0; i < serverList
													.size(); i++) {
												if (serverList.get(i).isAlive()) {
													complete = false;
													break;
												} else {
													complete = true;
												}

											}

											return complete;
										}
									}.start();
								} catch (UnknownHostException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}

							}
						});

				dialog.setNegativeButton("否",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								Toast.makeText(FileSelectActivity.this,
										"cancel", Toast.LENGTH_SHORT).show();
							}
						});
				dialog.show();

			} else if (msg.what == 0x1235) {
				AlertDialog.Builder dialog = new AlertDialog.Builder(
						FileSelectActivity.this);
				dialog.setTitle("提示");
				dialog.setMessage("对方拒绝接收文件！");
				dialog.setCancelable(false);
				dialog.setPositiveButton("确定",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface arg0, int arg1) {

							}
						});
				dialog.show();
			} else if (msg.what == 0x1333) {
				// 设置进度条的进度值
				pd2.setProgress((int) (transferPercernt * 100));
				/*
				 * 当传输比例达到0.97以上时，关闭进度条对话框同时弹出对话框提示文件传输完成
				 */
				if (transferPercernt >= 0.97) {
					pd2.dismiss();
					// handler2.sendEmptyMessage(0x1334);
					Toast.makeText(FileSelectActivity.this, "文件传输成功",
							Toast.LENGTH_SHORT).show();
				}

			}

		};
	};

	Handler handler2 = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == 0x1334) {
				AlertDialog.Builder dialog = new AlertDialog.Builder(
						FileSelectActivity.this);
				dialog.setTitle("提示");
				dialog.setMessage("文件发送成功");
				dialog.setCancelable(false);
				dialog.setPositiveButton("确定",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface arg0, int arg1) {

							}
						});
				dialog.show();
			}
		};
	};

}
