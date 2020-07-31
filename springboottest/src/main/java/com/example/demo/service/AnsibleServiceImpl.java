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
	
	List<ProcessDomain> pdList = new ArrayList<ProcessDomain>();
	
	@Autowired
	SHHService ssh;
	
	@Override
	public List<ProcessDomain> getProcess(Map<String, String> ansibleCmd){

		String userName = ansibleCmd.get("userName").toString();
		String host = ansibleCmd.get("host").toString();
		int port = Integer.parseInt(ansibleCmd.get("port").toString());
		String password = ansibleCmd.get("password").toString();
		String playbook = ansibleCmd.get("playbook").toString();
		String var = ansibleCmd.get("var").toString();
		
		String result = null;
		
		pdList = new ArrayList<ProcessDomain>();
		
		try {
			ssh.init(host, port, userName, password);
			
			result = ssh.executeCommand("ansible-playbook /root/playbook/" + playbook + " --extra-vars \"NAME=" + var + "\"");
			
			parsing3(result);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return pdList;
	}
	
	private void parsing3(String result) {
		BufferedReader reader = new BufferedReader(new StringReader(result)); 
		
		String line = null;
		
		boolean flag = false; 
		boolean flag2 = false;
		
		StringBuilder sb = new StringBuilder();
		
		//장비의 개수를 받아와 배열크기 지정
		ProcessDomain[] pd = new ProcessDomain[100];
		
		//프로세스가 100개 넘어갈수도 있으니 동적으로 확장하는 List 형식으로 변경
		HashMap[] map = new HashMap[100];
		
		int pdCount = 0;
		int psCount = 0;
		int mapCount = 0;
		try {
			while((line = reader.readLine()) != null) {
				line = line.trim();
				
				if(line.contains("Get Process")) { //Gathering Facts에서 성공한 ip를 땀
					flag = true;
					
					continue;
				}
				
				if(flag == true) { //호스트 값 뽑기
					StringTokenizer st = new StringTokenizer(line, " ");

					String preText = ""; //전에 ok:, changed 구분하기 위해 사용
					while(st.hasMoreTokens()) {
						String text = st.nextToken();
								
						if(preText.equals("changed:") && text.contains("]")) {
							text = text.replaceAll("[\\[\\]]", "");
							
							pd[pdCount] = new ProcessDomain();
							pd[pdCount].setHostIp(text);
							pdCount++;
						}
						
						if(text.contains("\"ps.stdout_lines\":")) {
							flag = false;
							flag2 = true;

							break;
						}	
						preText = text;
					}
				}else {
					if(flag2 == true){
						///////////////replace////////////////
						String temp = line.toString().trim();
						temp = temp.replaceAll("\"", "");
						temp = temp.replaceAll(",", "");
						
						if(temp.equals("]") || temp.equals("}") || temp.contains("ok:")) continue;
						
						if(temp.contains("ps.stdout_lines:")) {
							psCount++;
							continue;
						}
						
						if(temp.contains("PLAY RECAP")) {
							break;
						}
						
						StringTokenizer st = new StringTokenizer(temp, " ");				
						
						String cmd = "";
						String mem = "";
						String cpu = "";
						
						String temp2;
						
						if(st.hasMoreTokens()) {

							while(true) {
								temp2 = st.nextToken();
								
								if(temp2.matches("^[0-9]+(\\.?[0-9]*)$")) {
									mem = temp2;
									break;
								}
								
								cmd = cmd + temp2 + " ";
							}
							
							cpu = st.nextToken();
										
							map[mapCount] = new HashMap<String, String>();
							map[mapCount].put("cmd", cmd.trim());
							map[mapCount].put("mem", mem);
							map[mapCount].put("cpu", cpu);
							
							pd[psCount].getProcessList().add(map[mapCount]); //pd에 먼저 넣으면안됨
							
							mapCount++;	
						}
					}
				}		
			}
			
			for(int i = 0; i < pdCount; i++) {
				pdList.add(pd[i]);
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
	}		
}
