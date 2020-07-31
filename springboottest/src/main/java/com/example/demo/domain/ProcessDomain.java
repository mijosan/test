package com.example.demo.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import lombok.Data;

@Data
public class ProcessDomain {
	private String hostIp;
	private List<HashMap<String, String>> processList = new ArrayList<HashMap<String, String>>();
}
