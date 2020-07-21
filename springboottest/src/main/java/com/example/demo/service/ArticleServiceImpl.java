package com.example.demo.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.domain.Article;
 
@Service("ArticleService")
public class ArticleServiceImpl implements ArticleService{
  
  @Override
  public List<Article> test() {
    
    List<Article> list = new ArrayList<Article>();
    
    for (int i = 1; i <= 10; i++) {
      Article article = new Article();
      article.setSeq(i);
      article.setText("This is text");
      article.setTitle("This is title");
      article.setWriter("I am writer");
      
      list.add(article);
    }
    return list;
  }
  
  @Override
  public Article detail(int seq) {
    
    Article article = new Article();
    article.setSeq(seq);
    article.setText("This is text");
    article.setTitle("This is title");
    article.setWriter("I am writer");
    
    return article;
  }
}
