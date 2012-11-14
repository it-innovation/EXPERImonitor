package eu.wegov.coordinator.dao.data;

import java.sql.Timestamp;


import java.util.ArrayList;
import java.util.HashMap;

public class HeadsUpPost {


  int id;
  Timestamp dateTs;
  String date;

  String thread;
  String user;
  String subject;
  String message;

  public HeadsUpPost(int id, Timestamp dateTs, String thread, String user, String subject, String message) {
    this.id = id;
    this.dateTs = dateTs;
    this.date = dateTs.toString();
    this.thread = thread;
    this.user = user;
    this.subject = subject;
    this.message = message;
  }



  /*
  HashMap id;
  HashMap date;
  HashMap thread;
  HashMap user;
  HashMap subject;
  HashMap message;


  public HeadsUpPost(HashMap id, HashMap date, HashMap thread, HashMap user, HashMap subject, HashMap message) {
    this.id = id;
    this.date = date;
    this.thread = thread;
    this.user = user;
    this.subject = subject;
    this.message = message;
  }

  public HashMap getDate() {
    return date;
  }

  public void setDate(HashMap date) {
    this.date = date;
  }

  public HashMap getId() {
    return id;
  }

  public void setId(HashMap id) {
    this.id = id;
  }

  public HashMap getMessage() {
    return message;
  }

  public void setMessage(HashMap message) {
    this.message = message;
  }

  public HashMap getSubject() {
    return subject;
  }

  public void setSubject(HashMap subject) {
    this.subject = subject;
  }

  public HashMap getThread() {
    return thread;
  }

  public void setThread(HashMap thread) {
    this.thread = thread;
  }

  public HashMap getUser() {
    return user;
  }

  public void setUser(HashMap user) {
    this.user = user;
  }
*/

  public String getDate() {
    return date;
  }

  public void setDate(Timestamp dateTs) {
    this.dateTs = dateTs;
    this.date = dateTs.toString();
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getSubject() {
    return subject;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public String getThread() {
    return thread;
  }

  public void setThread(String thread) {
    this.thread = thread;
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }


}