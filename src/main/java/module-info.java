module pe.iep.hsbk.evaluaciones {
  requires javafx.controls;
  requires javafx.fxml;
  requires java.desktop;
  requires java.sql;
  requires jbcrypt;
  requires mysql.connector.j;
  requires javafx.web;

  opens pe.iep.hsbk.evaluaciones.controller to javafx.fxml;
  opens pe.iep.hsbk.evaluaciones.model;
  exports pe.iep.hsbk.evaluaciones;
  opens pe.iep.hsbk.evaluaciones.dao;
  opens pe.iep.hsbk.evaluaciones.service;
  opens pe.iep.hsbk.evaluaciones.util;
}
