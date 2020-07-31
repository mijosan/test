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
	
	//최종 리턴 값
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
			
			//파싱 메서드
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
		
		//장비의 개수를 받아와 배열크기 지정해야함 [fix]
		ProcessDomain[] pd = new ProcessDomain[100];
		
		//프로세스가 100개 넘어갈수도 있으니 동적으로 확장하는 List 형식으로 변경
		HashMap[] map = new HashMap[200];
		
		//장비 개수 만큼 ProcessDomain 배열에 객체를 생성 하기 위해
		int pdCount = 0;
		
		//ProcessDomain 배열에 생성된 객체의 processList에 Map 객체를 넣기 위해
		int psCount = 0;
		
		//중복되지 않는 HashMap을 만들어 processList에 add 하기 위해
		int mapCount = 0;
		
		try {
			//한줄씩 읽어옴
			while((line = reader.readLine()) != null) {
				line = line.trim();
				
				if(line.contains("Get Process")) { //Gathering Facts에서 성공한 장비들에 대해서 프로세스를 가져옴
					flag = true;
					
					continue;
				}
				
				if(flag == true) { //Gathering Facts 구문에서 호스트를 따는 부분
					StringTokenizer st = new StringTokenizer(line, " ");

					String preText = ""; //전에 ok:, changed 구분하기 위해 사용 (changed 가 성공한 장비이기 때문에 여기서 호스트 ip를 땀)
					while(st.hasMoreTokens()) {
						String text = st.nextToken();
								
						if(preText.equals("changed:") && text.contains("]")) { //그전의 문자가 changed 가 포함되고 지금 문자에 "]"가 포함된다면
							text = text.replaceAll("[\\[\\]]", "");
							
							pd[pdCount] = new ProcessDomain();
							pd[pdCount].setHostIp(text);
							pdCount++;
						}
						
						if(text.contains("\"ps.stdout_lines\":")) { //프로세스 리스트가 나오기 시작하기 시작하기 전
							flag = false;
							flag2 = true;

							break;
						}	
						preText = text;
					}
				}else {
					if(flag2 == true){
						//특수 문자 제거
						String temp = line.toString().trim();
						temp = temp.replaceAll("\"", "");
						temp = temp.replaceAll(",", "");
						
						if(temp.equals("]") || temp.equals("}") || temp.contains("ok:")) continue;
						
						if(temp.contains("ps.stdout_lines:")) { //다음 장비의 프로세스의 리스트가 나타나기 시작한다는 말 따라서 psCount를 증가시켜준다.
							psCount++;
							continue;
						}
						
						if(temp.contains("PLAY RECAP")) { //마지막에 나오는 문장이니 끝내준다.
							break;
						}
						
						StringTokenizer st = new StringTokenizer(temp, " ");				
						
						String cmd = "";
						String mem = "";
						String cpu = "";
						
						String temp2;
						
						if(st.hasMoreTokens()) {

							while(true) { //while문을 돌리는 이유는 StringTokenizer 가 3개만 나와야 하는데 4개 이상 나올경우 이상한곳에 대입됨
								temp2 = st.nextToken();
								
								if(temp2.matches("^[0-9]+(\\.?[0-9]*)$")) { //cpu가 실수이기 때문에 cpu가 나올때까지 cmd를 합쳐줌
									mem = temp2;
									break;
								}
								
								cmd = cmd + temp2 + " ";
							}
							
							cpu = st.nextToken();
							
							//map 객체를 만들어 ProcessDomain의 getProcessList에 add시켜줌
							map[mapCount] = new HashMap<String, String>();
							map[mapCount].put("cmd", cmd.trim());
							map[mapCount].put("mem", mem);
							map[mapCount].put("cpu", cpu);
							
							pd[psCount].getProcessList().add(map[mapCount]); //pd에 먼저 넣으면안됨
							
							//다음 주소에 객체 생성을 위해 사용
							mapCount++;	
						}
					}
				}		
			}
			
			//마지막에 리턴하는 pdList에 ProcessDomain 배열에 있는 값들을 pdList에 add시켜준다.
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
