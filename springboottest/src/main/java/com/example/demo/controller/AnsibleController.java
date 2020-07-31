package com.example.demo.controller;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.domain.ProcessDomain;
import com.example.demo.service.AnsibleService;
import com.example.demo.service.AnsibleServiceImpl2;



@RestController
public class AnsibleController {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Resource(name="AnsibleService")
	private AnsibleService ansibleService;
	
	@Resource(name="AnsibleService2")
	private AnsibleServiceImpl2 ansibleService2;
	
	@PostMapping("/getProcess")
	public List<ProcessDomain> getProcess(@RequestBody Map<String, String> ansibleCmd) {

		return ansibleService.getProcess(ansibleCmd);
	}
	
	@PostMapping("/getProcess2")
	public String getProcess2(@RequestBody Map<String, Object> ansibleCmd) {

		return ansibleService2.getProcess2(ansibleCmd);
	}
}
