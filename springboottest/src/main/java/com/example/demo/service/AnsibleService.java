package com.example.demo.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.RequestBody;

import com.example.demo.domain.ProcessDomain;

public interface AnsibleService {
	List<ProcessDomain> getProcess(@RequestBody Map<String, String> ansibleCmd);
}
