package com.example.demo.common;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.springframework.stereotype.Component;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;

@Component
public class SHHService{
  
	private Connection connection;
	
	public void init(String host, int port, String userName, String password){
		try {
			//서버 연결 객체 생성
			connection = new Connection(host);
			connection.connect();
			connection.authenticateWithPassword(userName, password);
		} catch (IOException e) {
            e.printStackTrace();
        }
    }
	public String executeCommand(String command) throws IOException{
	
    	//세션 생성
    	Session session = connection.openSession();
    	InputStream stdout = new StreamGobbler(session.getStdout());
    	BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
    	try {
    		//command 실행
    		session.execCommand(command);
    		//결과확인	
    		StringBuilder sb = new StringBuilder();	
    		String line = br.readLine();
    		while(line != null) {
    			sb.append(line + "\n");
    			line = br.readLine();
    		}
    		System.out.println(sb.toString());
    
    		return sb.toString();
    		
    		}catch(IOException e) {
    			e.printStackTrace();
    			return "";
    		}finally {
    			br.close();
    			//세션 종료
    			session.close();
    		}
	}
	
	public void saveFile(String processInfo, String filePath) throws IOException {
		FileWriter fileWriter = new FileWriter(filePath);
		try {
			fileWriter.write(processInfo);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			fileWriter.close();
		}
		
		
	}
	
	//서버에서 로그아웃
	public void disconnection(){
		connection.close();	
	}
}
