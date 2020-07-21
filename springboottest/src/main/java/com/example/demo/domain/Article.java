package com.example.demo.domain;

import lombok.Data;

@Data
public class Article {

  private long seq;
  private String title;
  private String text;
  private String writer;
  
}
