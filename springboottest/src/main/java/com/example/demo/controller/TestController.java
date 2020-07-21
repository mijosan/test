package com.example.demo.controller;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.domain.Article;
import com.example.demo.service.ArticleService;


@RestController
public class TestController {
	  @Resource(name="ArticleService")
	  private ArticleService articleService;
	  
	  @GetMapping("/list")
	  public List<Article> test(){
	    
	    return articleService.test();
	    
	  }
}
