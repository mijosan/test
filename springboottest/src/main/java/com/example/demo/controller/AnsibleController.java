package com.example.demo.controller;

import java.util.Map;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.domain.ProcessDomain;
import com.example.demo.service.AnsibleService;

@RestController
public class AnsibleController {
	@Resource(name="AnsibleService")
	private AnsibleService ansibleService;
	 
	@PostMapping("/getProcess")
	public ProcessDomain getProcess(@RequestBody Map<String, Object> ansibleCmd, ProcessDomain processDomain) {
		 return ansibleService.getProcess(ansibleCmd, processDomain);
	}
}
