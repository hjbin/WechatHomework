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
import java.net.MulticastSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;

import com.weixin.thread.Client;
import com.weixin.ui.ImageBtn;
import com.weixin.ui.SearchDevicesView;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;



public class MainActivity extends Activity {

	private SearchDevicesView searchDevicesView;
	private String ip = "123";
	private String otherip = "";
	private String longitude = "0";
	private String latitude = "0";
	private ArrayList<String> IPlist;
	private int screenWidth = 0;
	private int screenHeight = 0;
	private int leftMargin = 0;
	private int topMargin = 0;
	private int port = 8888;
	/*��ȷ���õĶ˿ں�**/
	private int confirmPort = 8885;
	/*���ļ��Ķ˿ں�**/
	private int filePort = 7777;
	
	
    /*�ļ���**/
	private String fileName = null;
	/*���Ͷ�IP**/
	private String serverIP = null;
	/*����ļ�·��**/
	private String filePath = "/sdcard/";
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		searchDevicesView = new SearchDevicesView(this);
		searchDevicesView.setWillNotDraw(false);
		setContentView(searchDevicesView);

		ip = getIP();

		WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		screenWidth = wm.getDefaultDisplay().getWidth();
		screenHeight = wm.getDefaultDisplay().getHeight();

		new Thread(new MarginThread()).start();
		/* ���նԷ�������ͷ����Ϣ*/
		new Thread(new UDPServerThread()).start();
		/* ���ͼ�����ͷ����Ϣ*/
		new Thread(new UDPClinetThread()).start();
		/*�Ƿ���ܶԷ����������ļ�*/
		new Thread(new ComfirmReceThread()).start();

		IPlist = new ArrayList<String>();

	}

	class UDPServerThread implements Runnable {
		// UDP�������˿�
		public void run() {
			MulticastSocket s = null;
			InetAddress group;
			try {
				group = InetAddress.getByName("239.255.255.105");
				s = new MulticastSocket(9001);
				s.setTimeToLive(255);
//				System.out.println("�鲥���ͳ����ʼ����ϣ����ڼ����鲥��....");
				s.joinGroup(group);
//				System.out.println("�ɹ������鲥��,���ڽ����鲥��Ϣ....");
				while (true) {
					byte[] buf = new byte[1024];
					DatagramPacket packet = new DatagramPacket(buf, buf.length);
					s.receive(packet);
//					System.out.println("���յ� : "
//							+ new String(packet.getData(), packet.getOffset(),
//									packet.getLength()));

					otherip = new String(packet.getData(), packet.getOffset(),
							packet.getLength());

					// analyzeMsg(msg);

					if (otherip.equals(ip)) {
						continue;
					}

					int i = 0;
					for (i = 0; i < IPlist.size(); i++) {
						if (otherip.equals(IPlist.get(i))) {
							break;
						}
					}

					if (i < IPlist.size()) {
						continue;
					}

					IPlist.add(otherip);

					handler.sendEmptyMessage(0x1233);

				}

			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				s.close();
			}
		}
	}

	class UDPClinetThread implements Runnable {
		// UDP�������˿�
		public void run() {
			MulticastSocket s = null;
			InetAddress group;
			try {

				group = InetAddress.getByName("239.255.255.105");
				s = new MulticastSocket();

//				System.out.println("�鲥���ͳ����ʼ�����");

				while (true) {
					String msg = getIP();
					// String msg2="100";
					// String msg3="100";
					// String msg=msg1+" "+msg2+" "+msg3+" ";

					DatagramPacket packet = new DatagramPacket(msg.getBytes(),
							msg.length(), group, 9001);

//					System.out.println("���ڷ����鲥��Ϣ....");
					try {
						s.send(packet);
						Thread.sleep(2000);
					} catch (IOException e) {
						e.printStackTrace();
					}
//					System.out.println("���ͳɹ�");
				}

			} catch (Exception e1) {
				e1.printStackTrace();
			} finally {
				s.close();
			}

		}
	}

	class MarginThread implements Runnable {

		public void run() {
			setImageMargin();
		}

	}

	class ComfirmReceThread implements Runnable{
		public void run() {
			
			InetAddress selfHost = null;
			try {
				selfHost = InetAddress.getByName(ip);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
			// UDP�����ļ�����IP
			DatagramSocket mySocket = null;
			try {
				mySocket = new DatagramSocket(
							port, selfHost);
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			int MAX_LEN = 500;
			
			
			
			while(true){
			
			// �����ļ�����IP
			byte[] bufferName = new byte[MAX_LEN];
			DatagramPacket datagramName = new DatagramPacket(
					bufferName, MAX_LEN);
			try {
				mySocket.receive(datagramName);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// ��ȡ�ļ�����IP
			ByteArrayInputStream bais = new ByteArrayInputStream(
					bufferName);
			DataInputStream dis = new DataInputStream(bais);
			try {
				fileName = dis.readUTF();
				serverIP = dis.readUTF();
				
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println(fileName);
			
			handler.sendEmptyMessage(0x1234);
			
			}

			
			
			
		}
	}
	
	private String getIP() {
		// ��ȡwifi����

		WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

		// �ж�wifi�Ƿ���

		if (!wifiManager.isWifiEnabled()) {

			wifiManager.setWifiEnabled(true);

		}

		WifiInfo wifiInfo = wifiManager.getConnectionInfo();

		int ipAddress = wifiInfo.getIpAddress();

		String ip = intToIp(ipAddress);

		return ip;

	}

	private String intToIp(int i) {

		return (i & 0xFF) + "." +

		((i >> 8) & 0xFF) + "." +

		((i >> 16) & 0xFF) + "." +

		(i >> 24 & 0xFF);

	}

	private void analyzeMsg(String msg) {
		char[] ipchar = new char[20];
		char[] longchar = new char[20];
		char[] latchar = new char[20];
		int j = 0;
		int turn = 1;
		for (int i = 0; i < msg.length(); i++) {
			if (msg.charAt(i) == ' ') {
				switch (turn) {
				case 1:
					msg.getChars(j, i, ipchar, 0);
					ip = new String(ipchar).trim();
					j = i + 1;
					turn++;
					break;
				case 2:
					msg.getChars(j, i, longchar, 0);
					longitude = new String(longchar).trim();
					j = i + 1;
					turn++;
					break;
				case 3:
					msg.getChars(j, i, latchar, 0);
					latitude = new String(latchar).trim();
					j = i + 1;
					turn++;
					break;
				default:
					break;
				}
			}

		}
	}

	int i = 0;
	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (msg.what == 0x1233) {
				// Toast.makeText(MainActivity.this, ip + " " + i++,
				// Toast.LENGTH_LONG).show();

				ImageBtn imageBtn = new ImageBtn(MainActivity.this);
				imageBtn.setIP(ip);
				imageBtn.setOhterIP(otherip);
				imageBtn.setTextViewText(imageBtn.getOtherIP());
				imageBtn.setMyIpTextViewText(getIP());
				imageBtn.setImageResource(R.drawable.good);
				RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
						LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

				lp.setMargins(leftMargin, topMargin, 0, 0);
				searchDevicesView.addView(imageBtn, lp);

			}else if (msg.what==0x1234) {
				AlertDialog.Builder dialog=new AlertDialog.Builder(MainActivity.this);
				dialog.setTitle("��ʾ");
				dialog.setMessage("�Ƿ����"+serverIP+"��"+fileName+"?");
				dialog.setCancelable(false);
				dialog.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						try {
							new Thread() {

								final int MAX_LEN = 100;

								public void run() {
									try {
										
										//����server�˿��Է����ļ���1��ʾ���Խ��շ���
				
										InetAddress selfHost = InetAddress.getByName(ip);
										// UDP�����ļ������ļ���С
										DatagramSocket confirmSocket = new DatagramSocket(
												confirmPort, selfHost);
										
										//serverHost 
										InetAddress serverHost = InetAddress.getByName(serverIP);
										// UDP���ļ���С���ͻ���
										ByteArrayOutputStream baos = new ByteArrayOutputStream();
										DataOutputStream dos = new DataOutputStream(
												baos);
										dos.writeInt(1);

										byte[] buffer = baos.toByteArray();
										DatagramPacket datagram = new DatagramPacket(
												buffer, buffer.length,
												serverHost, confirmPort);
										// �������ݰ�
										confirmSocket.send(datagram);
										
										confirmSocket.close();
										
										Log.e("10","�����ļ�");
									
										//�����ļ�
										DatagramSocket mySocket = new DatagramSocket(
												filePort, selfHost);

										// �����ļ���С
										byte[] bufferLength = new byte[MAX_LEN];
										DatagramPacket datagramLength = new DatagramPacket(
												bufferLength, MAX_LEN);
										mySocket.receive(datagramLength);
										// ��ȡ�ļ�����
										ByteArrayInputStream bais = new ByteArrayInputStream(
												bufferLength);
										DataInputStream dis = new DataInputStream(bais);
										long fileLength = dis.readLong();
										System.out.println(fileLength);

										
										
										// �����ļ���
										byte[] bufferName = new byte[MAX_LEN];
										DatagramPacket datagramName = new DatagramPacket(
												bufferName, MAX_LEN);
										mySocket.receive(datagramName);
										// ��ȡ�ļ���
										ByteArrayInputStream bais2 = new ByteArrayInputStream(
												bufferName);
										DataInputStream dis2 = new DataInputStream(bais2);
										String fileName = dis2.readUTF();
										System.out.println(fileName);
										
										Log.e("11",fileName);

										// �ر�socket
										mySocket.close();

										// �ļ�·���������ļ�����
										String receFileStr = filePath + fileName;
										File receFile = new File(receFileStr);
										// �����ļ���С�����߳���Ŀ
										if (fileLength < 20971520) { // fileLength<20971520
											/* long startPos = 0; */

											// ����Ŀ�ʼλ��
											long initStartPos = 0;

											// TODO::�����Ӧ���Ǵ����ݿ���ȡ������
											long startPos = 0;

											// �Ѿ����͵ĳ���
											long sentLength = startPos - initStartPos;
											
											Socket dataSocket = new Socket(serverIP,
													filePort);
											Client client = new Client(dataSocket,
													receFile, startPos, fileLength
															- sentLength);

											// �����߳�
											client.start();

											client.join();

											Log.e("11","���ճɹ�");

										} else if (fileLength < 104857600) { // fileLength<104857600

											// ����Ŀ�ʼλ��
											long initStartPos1 = 0;
											long initStartPos2 = fileLength / 3;
											long initStartPos3 = 2 * fileLength / 3;

											// TODO::�⼸����Ӧ���Ǵ����ݿ���ȡ������
											long startPos1 = 0;
											long startPos2 = fileLength / 3;
											long startPos3 = 2 * fileLength / 3;

											// �Ѿ����͵ĳ���
											long sentLength1 = startPos1
													- initStartPos1;
											long sentLength2 = startPos2
													- initStartPos2;
											long sentLength3 = startPos3
													- initStartPos3;

											Socket dataSocket1 = new Socket(serverIP,
													filePort);
											Client client1 = new Client(dataSocket1,
													receFile, startPos1, fileLength / 3
															- sentLength1);
											Socket dataSocket2 = new Socket(serverIP,
													filePort);
											Client client2 = new Client(dataSocket2,
													receFile, startPos2, fileLength / 3
															- sentLength2);
											Socket dataSocket3 = new Socket(serverIP,
													filePort);
											Client client3 = new Client(dataSocket3,
													receFile, startPos3, fileLength - 2
															* fileLength / 3
															- sentLength3);

											if (!client1.isAlive())
												client1.start();
											if (!client2.isAlive())
												client2.start();
											if (!client3.isAlive())
												client3.start();

											// ��鷢���߳��Ƿ����
											while (client1.isAlive()
													|| client2.isAlive()
													|| client3.isAlive())
												;

											System.out.println("���ճɹ�");

										} else {

											/*
											 * long startPos1 = 0; long startPos2 =
											 * fileLength/5; long startPos3 =
											 * 2*fileLength/5; long startPos4 =
											 * 3*fileLength/5; long startPos5 =
											 * 4*fileLength/5;
											 */

											// ����Ŀ�ʼλ��
											long initStartPos1 = 0;
											long initStartPos2 = fileLength / 3;
											long initStartPos3 = 2 * fileLength / 3;
											long initStartPos4 = 3 * fileLength / 3;
											long initStartPos5 = 4 * fileLength / 3;

											// TODO::�⼸����Ӧ���Ǵ����ݿ���ȡ������
											long startPos1 = 0;
											long startPos2 = fileLength / 5;
											long startPos3 = 2 * fileLength / 5;
											long startPos4 = 3 * fileLength / 5;
											long startPos5 = 4 * fileLength / 5;

											// �Ѿ����͵ĳ���
											long sentLength1 = startPos1
													- initStartPos1;
											long sentLength2 = startPos2
													- initStartPos2;
											long sentLength3 = startPos3
													- initStartPos3;
											long sentLength4 = startPos4
													- initStartPos4;
											long sentLength5 = startPos5
													- initStartPos5;

											Socket dataSocket1 = new Socket(serverIP,
													filePort);
											Client client1 = new Client(dataSocket1,
													receFile, startPos1, fileLength / 5
															- sentLength1);
											Socket dataSocket2 = new Socket(serverIP,
													filePort);
											Client client2 = new Client(dataSocket2,
													receFile, startPos2, fileLength / 5
															- sentLength2);
											Socket dataSocket3 = new Socket(serverIP,
													filePort);
											Client client3 = new Client(dataSocket3,
													receFile, startPos3, fileLength / 5
															- sentLength3);
											Socket dataSocket4 = new Socket(serverIP,
													filePort);
											Client client4 = new Client(dataSocket4,
													receFile, startPos4, fileLength / 5
															- sentLength4);
											Socket dataSocket5 = new Socket(serverIP,
													filePort);
											Client client5 = new Client(dataSocket5,
													receFile, startPos5, fileLength - 4
															* fileLength / 5
															- sentLength5);

											if (!client1.isAlive())
												client1.start();
											if (!client2.isAlive())
												client2.start();
											if (!client3.isAlive())
												client3.start();
											if (!client4.isAlive())
												client4.start();
											if (!client5.isAlive())
												client5.start();

											// ��鷢���߳��Ƿ����
											while (client1.isAlive()
													|| client2.isAlive()
													|| client3.isAlive()
													|| client4.isAlive()
													|| client5.isAlive())
												;

											System.out.println("���ճɹ�");
										}
									} catch (Exception e) {
										e.printStackTrace();
									}
								}

							}.start();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
				
				dialog.setNegativeButton("ȡ��", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						
					}
				});
				
				dialog.show();
			}
		}
	};

	public void setImageMargin() {
		boolean flag = true;
		leftMargin = (int) (75 + Math.random() * (screenWidth-300 - 75 + 1));
		topMargin = (int) (75 + Math.random() * (screenHeight - 300-75 + 1));
//		while (flag) {
//			if ((leftMargin > 75 || leftMargin < screenWidth+ 75)
//					&& (topMargin > screenHeight / 2 - 75 || topMargin < screenHeight / 2 + 75)) {
//				leftMargin = (int) (0 + Math.random() * (screenWidth - 0 + 1));
//				topMargin = (int) (0 + Math.random() * (screenHeight - 0 + 1));
//			} else {
//				flag = false;
//			}
//		}
	}
}
