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

	// Ŀ¼·������ʼΪsdcard,�������ֱ��������file������
	private String path = "/sdcard";
	private ListView fileview;
	private List<Map<String, Object>> items;
	private FilelistAdatper mAdapter;
	private String self_ip;
	private String target_ip;
	private File selectedFile = null;
	private int port = 8888;
	private Handler progressHandler;
	/* ��ȷ���õĶ˿ں�* */
	private int confirmPort = 8885;
	/* ���ļ��Ķ˿ں�* */
	private int filePort = 7777;
	/* ���ļ�����* */
	private double transferPercernt = 0;

	private AlertDialog.Builder progressDialog;
	private ProgressDialog pd2;

	/* ���շ��Ƿ�ȷ�Ͻ���* */
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

	// ������дһ���Ի������û��Ƿ��͸��ļ���Ȼ�����ת�������͵�activity����path��������ȥ
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
			dialog.setTitle("��ʾ");
			dialog.setMessage("�Ƿ�����û�" + target_ip + "���͸��ļ�?");
			dialog.setCancelable(false);
			dialog.setPositiveButton("ȷ��",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							try {
								System.out.println("��ο��0");
								new Thread() {
									public void run() {
										System.out.println("��ο��1");
										try {

											System.out.println("��ο��2");
											// selfIP
											InetAddress serverHost = InetAddress
													.getByName(self_ip);

											// ���ݰ�socket��UDP����
											DatagramSocket mySocket = null;
											mySocket = new DatagramSocket(
													confirmPort, serverHost);
											System.out.println("��ο��3");

											// ��ȡ�ļ���
											String fileName = selectedFile
													.getName();

											System.out.println(fileName);

											// ���շ�IP
											InetAddress receiverHost = null;
											receiverHost = InetAddress
													.getByName(target_ip);
											System.out.println("��ο��4");
											System.out.println(receiverHost);

											// UDP���ļ�����IP���ͻ���
											ByteArrayOutputStream bao = new ByteArrayOutputStream();
											DataOutputStream dataos = new DataOutputStream(
													bao);

											dataos.writeUTF(fileName);

											dataos.writeUTF(self_ip);
											System.out.println("��ο��5");
											byte[] bufferFileName = bao
													.toByteArray();
											DatagramPacket datagramFileName = new DatagramPacket(
													bufferFileName,
													bufferFileName.length,
													receiverHost, port);
											// �������ݰ�
											mySocket.send(datagramFileName);
											System.out.println("��ο��6");

											// ���� ���շ��Ƿ���Խ����ļ��Ľ��
											byte[] buffer = bao.toByteArray();
											DatagramPacket datagram = new DatagramPacket(
													buffer, buffer.length,
													serverHost, port);
											mySocket.receive(datagram);

											// ��ȡ���
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

											// �ر�Socket
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
				dialog.setTitle("��ʾ");
				dialog.setMessage("�Է�ͬ������ļ����Ƿ�ʼ�����ļ���");
				dialog.setCancelable(false);
				dialog.setPositiveButton("��",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								try {

									Log.e("aaa", "sssss");

									/*
									 * ����ProgressDialog��ʾ�ļ��������
									 */
									pd2 = new ProgressDialog(
											FileSelectActivity.this);
									pd2.setMax(100);
									pd2.setTitle("�ļ����ڴ���");
									pd2.setMessage("�ļ�����İٷֱ�");
									pd2.setCancelable(false);
									pd2.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
									pd2.setIndeterminate(false);
									pd2.show();

									new Thread() {

										// ���շ�IP
										InetAddress receiverHost = InetAddress
												.getByName(target_ip);

										public void run() {

											try {

												Log.e("1", "��ʼ����");
												// ���ݰ�socket��UDP����
												DatagramSocket mySocket = new DatagramSocket();

												// ��ȡ�ļ���С
												long fileLength = selectedFile
														.length();
												// UDP���ļ���С���ͻ���
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
												// �������ݰ�
												mySocket.send(datagramLength);

												// ��ȡ�ļ���
												String fileName = selectedFile
														.getName();
												// UDP���ļ������ͻ���
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
												// �������ݰ�
												mySocket.send(datagramFileName);

												// �ر�socket
												mySocket.close();

												// �����̵߳���Ŀ
												int threadNum = 0;

												if (fileLength < 20971520) {
													threadNum = 1;
												} else if (fileLength < 104857600) {
													threadNum = 3;
												} else {
													threadNum = 5;
												}

												Log.e("2", "�½�����socket");

												// �½�����Socket
												ServerSocket connectionSocket = new ServerSocket(
														filePort);

												// Socket��list
												List<Server> serverList = new ArrayList<Server>();

												// Message startmsg=new
												// Message();
												// startmsg.what=0x1334;
												// //startmsg.obj=transferPercernt;
												// handler.sendMessage(startmsg);

												// �½����߳���Ŀ
												int existingThreadNum = 0;

												// �������ӣ��½��߳�
												while (existingThreadNum < threadNum) {

													System.out.println("�½��߳�");

													// �½�����socket
													Socket dataSocket = connectionSocket
															.accept();
													System.out
															.println("���շ���������");

													// �½������߳�
													Server server = new Server(
															dataSocket,
															selectedFile,
															existingThreadNum);

													existingThreadNum++;

													// �����߳�
													if (!server.isAlive())
														server.start();

													// ��Server���뵽list����ȥ
													serverList.add(server);

												}

												Log.e("3", "��ⷢ���Ƿ�ɹ�");
												// ����ļ������Ƿ����
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

												Log.e("4", "�������");

											} catch (Exception e) {

											}
										};

										boolean checkServerState(
												List<Server> serverList) {
											// �߳�״̬
											boolean complete = false;

											// ���
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

				dialog.setNegativeButton("��",
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
				dialog.setTitle("��ʾ");
				dialog.setMessage("�Է��ܾ������ļ���");
				dialog.setCancelable(false);
				dialog.setPositiveButton("ȷ��",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface arg0, int arg1) {

							}
						});
				dialog.show();
			} else if (msg.what == 0x1333) {
				// ���ý������Ľ���ֵ
				pd2.setProgress((int) (transferPercernt * 100));
				/*
				 * ����������ﵽ0.97����ʱ���رս������Ի���ͬʱ�����Ի�����ʾ�ļ��������
				 */
				if (transferPercernt >= 0.97) {
					pd2.dismiss();
					// handler2.sendEmptyMessage(0x1334);
					Toast.makeText(FileSelectActivity.this, "�ļ�����ɹ�",
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
				dialog.setTitle("��ʾ");
				dialog.setMessage("�ļ����ͳɹ�");
				dialog.setCancelable(false);
				dialog.setPositiveButton("ȷ��",
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
