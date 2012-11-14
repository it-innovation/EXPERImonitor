/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.wegov.coordinator.web;

/**
 *
 * @author Steve Taylor
 */
public class ConfigParameter {
  
  int ID;
  String name;
  String value;
  String description;
  int run;

  public ConfigParameter(int ID, String name, String value, String description, int run) {
    this.ID = ID;
    this.name = name;
    this.value = value;
    this.description = description;
    this.run = run;
  }

  public ConfigParameter(int ID, String name, String value) {
    this.ID = ID;
    this.name = name;
    this.value = value;
  }

  public int getID() {
    return ID;
  }

  public void setID(int ID) {
    this.ID = ID;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public int getRun() {
    return run;
  }

  public void setRun(int run) {
    this.run = run;
  }

  
}
