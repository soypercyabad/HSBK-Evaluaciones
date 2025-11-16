module pe.iep.hsbk.evaluaciones {
  requires javafx.controls;
  requires javafx.fxml;
  requires javafx.web;
  requires java.desktop;
  requires java.sql;
  requires jbcrypt;
  requires mysql.connector.j;
  requires openhtmltopdf.pdfbox;

  opens pe.iep.hsbk.evaluaciones.controller to javafx.fxml;
  opens pe.iep.hsbk.evaluaciones.model;
  exports pe.iep.hsbk.evaluaciones;
  opens pe.iep.hsbk.evaluaciones.dao;
  opens pe.iep.hsbk.evaluaciones.service;
  opens pe.iep.hsbk.evaluaciones.util;
  opens pe.iep.hsbk.evaluaciones.dao.impl;
}
