package com.weixin.thread;

/** 
 *  传输文件，发送线程
 *  @author lihao
 */
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.net.SocketException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;

import com.weixin.globals.GlobalValue;

import android.R.integer;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings.Global;

/**
 * 文件发送端
 */
public class Server extends Thread {

	/* 数据socket* */
	private Socket dataSocket;
	
	private Handler mHandler;

	/* 文件* */
	private File file;

	/* 文件长度* */
	private long fileLength;

	/* 文件读取开始位置* */
	private long startPos;

	/* 文件是否断点* */
	private boolean fileInterrupt = false;
	
	/* totalNum* */
	private int totalNum;

	/* 密钥* */
	private String key = "lele";
	
	

	/**
	 * 构造函数，传入参数
	 */
	public Server(Socket dataSocket, File file, int totalNum) {

		this.dataSocket = dataSocket;
		this.file = file;
		this.totalNum = totalNum;

	}

	/**
	 * run函数，发送文件
	 */
	@Override
	public void run() {

		// 输入流
		InputStream inStream = null;
		try {
			inStream = dataSocket.getInputStream();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		// data输出流
		DataInputStream dis = new DataInputStream(inStream);

		// 发送文件相关信息
		try {
			readFileInfo(dis);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// 传文件
		OutputStream outStream = null;
		try {
			outStream = dataSocket.getOutputStream();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		DataOutputStream dos = new DataOutputStream(outStream);
		// 发送文件
		try {
			sendFile(outStream, dos);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 发送文件
	 * 
	 * @throws Exception
	 */
	private void sendFile(OutputStream outStream, DataOutputStream dos) {

		try {
			// 可以从多少字节开始写的文件类
			RandomAccessFile fis = null;
			fis = new RandomAccessFile(file, "rw");
			fis.seek(startPos);

			byte[] bufFile = new byte[1024];
			int len = 0;

			// TODO::已经传输的字节数 ,total的另外一个作用是显示传输进度
			long total = 0;

			// MD5对象
			MessageDigest messageDigest = MessageDigest.getInstance("MD5");

			while (total < fileLength) {

				len = fis.read(bufFile);

				// 已经读取的字节数
				total += len;

				// 读取的字节数超过范围
				if (total > fileLength) {
					// total回到之前的位置
					total -= len;

					fis.seek(startPos + total);

					int remainingLength = (int) (fileLength - total);

					// 完整读取最后的一段流
					int readLen = 0;
					int off = 0;
					do {
						readLen = fis.read(bufFile, off, remainingLength
								- readLen); // 接收数据
						off += readLen;
					} while (off < remainingLength);

					len = remainingLength;
					total = fileLength;
				}
				
				
				switch(totalNum){
				case 0:
					GlobalValue.globalTotal0 = total;
					break;
				case 1:
					GlobalValue.globalTotal1 = total;
					break;
				case 2:
					GlobalValue.globalTotal2 = total;
					break;
				case 3:
					GlobalValue.globalTotal3 = total;
					break;
				case 4:
					GlobalValue.globalTotal4 = total;
					break;	
				}
				
				
				
				
				

				if (len != -1) {
					/*byte[] AesBufFile = AESUtil.encrypt(
							Arrays.copyOfRange(bufFile, 0, len), key);

					messageDigest.update(AesBufFile);
					String md5 = parseByte2HexStr(messageDigest.digest());
					dos.writeUTF(md5);

					int length = AesBufFile.length;

					dos.writeInt(length);

					// 将从硬盘上读取的字节数据写入socket输出流
					outStream.write(AesBufFile, 0, length);*/
					
					dos.writeInt(len);
					
					outStream.write(bufFile, 0, len);
					
				} else {
					break;
				}

				// 发送方主动断开连接
				if (fileInterrupt) {
					// 下一次开始的位置
					// startPos += total;
					// System.out.println("发送方主动断开连接,下一次开始位置： "+startPos);

					break;
				}

			}

			fis.close();
			dataSocket.close();

		} catch (SocketException e) {
			System.out.println("接收端暂停接收");

		} catch (Exception e) {
			e.printStackTrace();
		}

	}	

	/**
	 * 字节数组转化成16进制的字符串
	 */
	public static String parseByte2HexStr(byte buf[]) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < buf.length; i++) {
			String hex = Integer.toHexString(buf[i] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			sb.append(hex.toUpperCase());
		}
		return sb.toString();
	}

	/**
	 * 读取文件相关信息
	 */
	private void readFileInfo(DataInputStream dis) throws IOException {

		fileLength = dis.readLong();
		startPos = dis.readLong();

	}

}
