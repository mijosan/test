package com.example.demo.service;

import java.util.List;

import com.example.demo.domain.Article;

public interface ArticleService {
  List<Article> test();
  
  Article detail(int seq);
}
