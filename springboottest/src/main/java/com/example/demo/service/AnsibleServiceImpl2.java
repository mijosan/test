package com.example.demo.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.common.SHHService;
import com.example.demo.domain.ProcessDomain;

@Service("AnsibleService2")
public class AnsibleServiceImpl2 {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	SHHService ssh;

	public String getProcess2(Map<String, Object> ansibleCmd){
		String userName = ansibleCmd.get("userName").toString();
		String host = ansibleCmd.get("host").toString();
		int port = Integer.parseInt(ansibleCmd.get("port").toString());
		String password = ansibleCmd.get("password").toString();
		String playbook = ansibleCmd.get("playbook").toString();
		String var = ansibleCmd.get("var").toString();
		
		String result = null;
		
		//리턴할 자료구조 정의
		List<ProcessDomain> responseVO = new ArrayList<ProcessDomain>();
		
		try {
			ssh.init(host, port, userName, password);
			
			result = ssh.executeCommand("ansible-playbook /root/playbook/" + playbook + " --extra-vars \"NAME=" + var + "\"");
			
			result = parsing(result);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return result;
	}
	//접속에 성공한 호스트에 한해서 (Debug 문 읽기)
	private List<HashMap<String, String>> parsing2(String result){
		
		//최종 리턴 List
		List<HashMap<String,String>> list = new ArrayList<HashMap<String, String>>();
		
		//List에 넣을 Map
		HashMap<String, String> map = new HashMap<String, String>();
		
		//ansible 결과 한줄씩 읽기 위해
		BufferedReader reader = new BufferedReader(new StringReader(result)); 
		
		String line = null;
		
		StringBuilder sb = new StringBuilder();
		
		boolean flag = false; 
		boolean flag2 = false;
		
		int count = 0;
		
		try {
			while((line = reader.readLine()) != null) {
				
				if(line.contains("debug")) { //debug문을 만나면 그때부터 파싱 시작	
					flag = true;
					
					continue;
				}
				
				if(flag == true) { //호스트 값 뽑기
					StringTokenizer st = new StringTokenizer(line, " ");
					
					while(st.hasMoreTokens()) {
						String str = st.nextToken();
						
						if(str.equals("ok:")) { //호스트 뽑기위해
							String temp = st.nextToken();
							
							temp = temp.replaceAll("[\\[\\]]", "");
							map.put("host" + count, temp);
							break;
						}
						
						if(str.contains("ps.stdout_lines")) { //ps목록 전 문장
							flag = false;
							flag2 = true;
							break;
						}
					}
				}else { //ps목록 뽑기
					if(line.equals("}")) {				
						map.put("ps" + count, new String(sb.toString()));
						
						sb.delete(0, sb.length());
						flag = true;
						count++;
					}else if(flag2 == true) {
						line = line.replaceAll("(^\\p{Z}+|\\p{Z}+$)", "").trim();
						
						if(line.equals("]")) {
							
						}else {
							sb.append(line.replaceAll("\"", ""));
						}
					}
				}
			}
			
			list.add(map);
			
		} catch(Exception e) {
			e.printStackTrace();
		}
			
		return list;
	}
		
	private String parsing(String result) {
		
		BufferedReader reader = new BufferedReader(new StringReader(result)); 
		
		String line = null;
		
		boolean flag = false; 
		boolean flag2 = false;
		
		StringBuilder sb = new StringBuilder();
		
		try {
			while((line = reader.readLine()) != null) {
				line = line.trim();
				
				if(line.contains("Gathering Facts")) { //Gathering Facts에서 성공한 ip를 땀
					flag = true;
					
					continue;
				}
				
				if(flag == true) { //호스트 값 뽑기
					StringTokenizer st = new StringTokenizer(line, " ");
					
					String preText = ""; //전에 ok:, changed 구분하기 위해 사용
					while(st.hasMoreTokens()) {
						String text = st.nextToken();
								
						if(preText.equals("ok:") && text.contains("[")) {
							text = text.replaceAll("[\\[\\]]", "");
							
							sb.append("\"host\":" + "\"" + text + "\",");
						}
						
						if(text.contains("ps.stdout_lines:")) {
							flag = false;
							flag2 = true;
							break;
						}
						
						preText = text;
					}
				}else {
					if(line.contains("ps.stdout_lines:")) {
						flag = true;
					}else if(flag2 == true){
						String temp = line.toString();
						temp = temp.replaceAll("-", "");
						temp = temp.replaceAll("'", "");
						
						sb.append(temp);
					}
				}		
			}
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return sb.toString();
	}
}
