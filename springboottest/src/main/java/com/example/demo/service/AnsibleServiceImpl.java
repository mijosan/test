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

@Service("AnsibleService")
public class AnsibleServiceImpl implements AnsibleService{
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	SHHService ssh;
	
	@Override
	public ProcessDomain getProcess(Map<String, Object> ansibleCmd, ProcessDomain processDomain){

		String userName = ansibleCmd.get("userName").toString();
		String host = ansibleCmd.get("host").toString();
		int port = Integer.parseInt(ansibleCmd.get("port").toString());
		String password = ansibleCmd.get("password").toString();
		String playbook = ansibleCmd.get("playbook").toString();
		String var = ansibleCmd.get("var").toString();
		
		String result = null;

		try {
			ssh.init(host, port, userName, password);
			
			result = ssh.executeCommand("ansible-playbook /root/playbook/" + playbook + " --extra-vars \"NAME=" + var + "\"");
			logger.info(result);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		processDomain.setPsList(parsing(result));
		
		
		return processDomain;
	}
	
	private List<HashMap<String, String>> parsing(String result){
		
		List<HashMap<String,String>> list = new ArrayList<HashMap<String, String>>();
		
		HashMap<String, String> map = new HashMap<String, String>();

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
						
						if(str.equals("ok:")) {
							String temp = st.nextToken();
							
							temp = temp.replaceAll("[\\[\\]]", "");
							map.put("host" + count, temp);
							break;
						}
						
						if(str.contains("ps.stdout_lines")) { //ps목록 전의 문장
							flag = false;
							flag2 = true;
							break;
						}
					}
				}else { //ps목록 뽑기
					if(line.equals("}")) {
						flag = true;
						map.put("ps" + count, new String(sb.toString()));
						sb.delete(0, sb.length());
						count++;
					}else if(flag2 == true){
						sb.append(line);			
					}
				}
			}
			
			list.add(map);
			
		} catch(Exception e) {
			e.printStackTrace();
		}
			
		return list;
	}
}
