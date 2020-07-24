package com.example.demo.service;

import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.common.SHHService;
import com.example.demo.domain.ProcessDomain;

@Service("AnsibleService")
public class AnsibleServiceImpl implements AnsibleService{
	
	@Autowired
	SHHService ssh;
	
	@Override
	public ProcessDomain getProcess(Map<String, Object> ansibleCmd, ProcessDomain processDomain){
		
		String userName = ansibleCmd.get("userName").toString();
		String host = ansibleCmd.get("host").toString();
		int port = Integer.parseInt(ansibleCmd.get("port").toString());
		String password = ansibleCmd.get("password").toString();
		String group = ansibleCmd.get("group").toString();
		String playbook = ansibleCmd.get("playbook").toString();
		String var = ansibleCmd.get("var").toString();
		
		String result = null;
		System.out.println(ansibleCmd);
		try {
			ssh.init(host, port, userName, password);
			
			result = ssh.executeCommand("ansible-playbook /root/playbook/getProcess.yml --extra-vars \"NAME=java\"");
			System.out.println(result);
		} catch (IOException e) {
			e.printStackTrace();
		}

		processDomain.setCommand(result);
		
		
		return processDomain;
	}
}
