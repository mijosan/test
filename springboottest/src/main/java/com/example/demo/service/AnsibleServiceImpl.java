package com.example.demo.service;

import java.io.IOException;
import java.util.Map;

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
		String group = ansibleCmd.get("group").toString();
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

		processDomain.setCommand(result);
		
		
		return processDomain;
	}
}
