package com.example.demo.service;

import java.util.Map;

import com.example.demo.domain.ProcessDomain;

public interface AnsibleService {
	ProcessDomain getProcess(Map<String, Object> ansibleCmd, ProcessDomain processDomain);
}
