package com.weixin.thread;

/** 
 *  传输文件，接收线程
 *  @author lihao
 */
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
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

/**
 * 文件接收端
 */
public class Client extends Thread {

	/* 数据Socket* */
	private Socket dataSocket;

	/* 文件大小* */
	private long fileLength;

	/* 文件* */
	private File receFile;

	/* 发送文件读取位置* */
	private long startPos;

	/* md5检查结果* */
	private boolean md5Check;

	/* 文件是否断点* */
	private boolean fileInterrupt = false;

	/* 密钥* */
	private String key = "lele";

	/**
	 * 构造函数，传入参数
	 */
	public Client(Socket dataSocket, File receFile, long startPos,
			long fileLength) {

		this.dataSocket = dataSocket;
		this.receFile = receFile;
		this.startPos = startPos;
		System.out.println(startPos);
		this.fileLength = fileLength;

	}

	/**
	 * run函数，接收文件
	 */
	@Override
	public void run() {

		// OutputStream
		OutputStream outStream = null;
		try {
			outStream = dataSocket.getOutputStream();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		DataOutputStream dos = new DataOutputStream(outStream);

		// 传输给接收方文件相关信息
		try {
			sendFileInfo(dos);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// 输入流
		InputStream inStream = null;
		try {
			inStream = dataSocket.getInputStream();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		// data输出流
		DataInputStream dis = new DataInputStream(inStream);

		// 接收文件
		try {
			readFileStream(inStream, dis);
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		try {
			dataSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * 读取文件输入流，并存到文件里
	 * 
	 * @throws Exception
	 */
	private void readFileStream(InputStream inStream, DataInputStream dis) {

		// TODO::已经读取的长度 ,total的另外一个作用是显示传输进度
		long total = 0;

		try {
			// 文件读取位置设置
			RandomAccessFile fos = new RandomAccessFile(receFile, "rw");

			// 定位文件指针到 nPos 位置
			try {
				// 文件指针移动到那个位置
				fos.seek(startPos);
			} catch (IOException e) {
				e.printStackTrace();
			}

			byte[] bufFile = new byte[1024 * 2];

			MessageDigest messageDigest = MessageDigest.getInstance("MD5");

			while (total < fileLength) {
				System.out.println(321);
				/*// 读取传进来的md5值
				String md5Rece = dis.readUTF();
*/
				// 完整读取发送端一次发过来的流
				int length = dis.readInt();
				int len = 0;
				int off = 0;
				do {
					len = inStream.read(bufFile, off, length - len); // 接收数据
					off += len;
				} while (off < length);

				/*messageDigest.update(Arrays.copyOfRange(bufFile, 0, length));

				// 计算收到的字节流计算出的md5值
				String md5 = parseByte2HexStr(messageDigest.digest());

				// 检查前后md5值
				if (md5Rece.equals(md5)) {
					md5Check = true;
				} else {
					md5Check = false;
					System.out.println("md5检验结果： " + md5Check);
					break;
				}

				byte[] AES = AESUtil.decrypt(
						Arrays.copyOfRange(bufFile, 0, length), key);
*/
				
				total += length;
				if (len != -1) {
					fos.write(bufFile, 0, length); // 写入硬盘文件

				} else {
					break;
				}

				// 接收方主动断开连接
				if (fileInterrupt == true) {
					// TODO::下一次开始的位置，这个位置应该存起来
					startPos += total;
					System.out.println("接收方主动断开连接，下次开始位置： " + startPos);
					break;
				}
			}

			fos.close();
		} catch (EOFException e) {
			System.out.println("发送端暂停发送");
			// TODO::下一次开始的位置，这个位置应该存起来
			startPos += total;
			System.out.println("发送端暂停发送，下次开始位置： " + startPos);

		} catch (SocketException e) {
			System.out.println("发送端socket断了");
			// TODO::下一次开始的位置，这个位置应该存起来
			startPos += total;
			System.out.println("发送端socket断了，下次开始位置： " + startPos);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 传输文件相关信息
	 */
	private void sendFileInfo(DataOutputStream dos) throws IOException {

		// 传输文件大小
		dos.writeLong(fileLength);
		dos.flush();

		// 传输文件开始位置
		dos.writeLong(startPos);
		dos.flush();

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

}
