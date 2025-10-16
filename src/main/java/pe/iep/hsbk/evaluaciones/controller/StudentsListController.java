package pe.iep.hsbk.evaluaciones.controller;

import javafx.beans.property.*;
import javafx.collections.*;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.shape.SVGPath;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Locale;
import java.util.function.Predicate;

public class StudentsListController {

  @FXML private TextField txtBuscar;
  @FXML private TableView<RowAlumno> tblAlumnos;
  @FXML private TableColumn<RowAlumno, Boolean> colSel;
  @FXML private TableColumn<RowAlumno, String> colApellidos;
  @FXML private TableColumn<RowAlumno, String> colNombres;
  @FXML private TableColumn<RowAlumno, String> colCodigo;
  @FXML private TableColumn<RowAlumno, Void> colAcciones;
  @FXML private ToggleGroup grpGrados;

  // estado de filtros
  private final StringProperty gradoSel = new SimpleStringProperty("1");
  private final StringProperty seccionSel = new SimpleStringProperty("A");

  private final ObservableList<RowAlumno> master = FXCollections.observableArrayList();
  private FilteredList<RowAlumno> filtered;

  @FXML
  public void initialize() {
    // Datos de ejemplo (cámbialos cuando conectes a BD)
    master.addAll(
        RowAlumno.of("ALEGRÍA MUÑOZ", "GAEL MATHIAS", "AM75148320", "1", "A"),
        RowAlumno.of("ALVARADO VALERIO", "HELLEN LUCIANA", "AV74810233", "1", "A"),
        RowAlumno.of("CABRERA GALARZA", "IKER IYALIN", "CG01548946", "1", "B"),
        RowAlumno.of("CABRERA QUISPE", "BRIAN YAEL", "CQ10247890", "1", "C"),
        RowAlumno.of("DAVILA DIONICIO", "ARIADNE MAITE", "DD04552978", "2", "A"),
        RowAlumno.of("FACHO ZEÑA", "LUANA CRISTEL", "FZ88741203", "2", "B")
    );

    // columnas
    colSel.setCellValueFactory(data -> data.getValue().selectedProperty());
    colSel.setCellFactory(CheckBoxTableCell.forTableColumn(colSel));

    colApellidos.setCellValueFactory(data -> data.getValue().apellidosProperty());
    colNombres.setCellValueFactory(data -> data.getValue().nombresProperty());
    colCodigo.setCellValueFactory(data -> data.getValue().codigoProperty());

    // Columna acciones con botón de lápiz (SVG)
    colAcciones.setCellFactory(col -> new TableCell<>() {
      private final Button btn = buildEditButton();

      {
        btn.setOnAction(e -> {
          RowAlumno row = getTableView().getItems().get(getIndex());
          // aquí abres tu formulario de edición de notas del alumno
          System.out.println("Editar: " + row.getApellidos() + ", " + row.getNombres());
        });
      }

      @Override
      protected void updateItem(Void item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) setGraphic(null);
        else setGraphic(btn);
      }
    });

    // filtros
    filtered = new FilteredList<>(master, buildPredicate());
    tblAlumnos.setItems(filtered);

    // re-filtrar cuando cambian filtros
    gradoSel.addListener((obs, a, b) -> refiltrar());
    seccionSel.addListener((obs, a, b) -> refiltrar());
  }

  private Button buildEditButton() {
    SVGPath pencil = new SVGPath();
    // ícono de lápiz simple
    pencil.setContent("M3,14 L10,7 13,10 6,17 3,17z M10,6 L12,4 15,7 13,9z");
    Button b = new Button();
    b.getStyleClass().add("icon-btn");
    b.setGraphic(new HBox(pencil));
    return b;
  }

  private Predicate<RowAlumno> buildPredicate() {
    return row -> row.getGrado().equals(gradoSel.get())
        && row.getSeccion().equalsIgnoreCase(seccionSel.get())
        && matchesSearch(row, txtBuscar == null ? "" : txtBuscar.getText());
  }

  private boolean matchesSearch(RowAlumno r, String q) {
    if (q == null || q.isBlank()) return true;
    String s = q.toLowerCase(Locale.ROOT).trim();
    return r.getApellidos().toLowerCase().contains(s)
        || r.getNombres().toLowerCase().contains(s)
        || r.getCodigo().toLowerCase().contains(s);
  }

  private void refiltrar() {
    filtered.setPredicate(buildPredicate());
  }

  // ==== handlers UI ====
  @FXML private void onBuscar() { refiltrar(); }

  @FXML private void onChangeGrado() {
    ToggleButton tb = (ToggleButton) tblAlumnos.getScene().lookup(".tab:selected");
    if (tb != null) {
      String text = tb.getText().replace("°", "");
      gradoSel.set(text);
    }
  }

  @FXML private void onSeccionA() { seccionSel.set("A"); }
  @FXML private void onSeccionB() { seccionSel.set("B"); }
  @FXML private void onSeccionC() { seccionSel.set("C"); }
  @FXML private void onSeccionD() { seccionSel.set("D"); }
  @FXML private void onSeccionE() { seccionSel.set("E"); }

  @FXML private void onDescargar() {
    try {
      File out = new File(System.getProperty("user.home"), "alumnos_seleccion.csv");
      try (BufferedWriter w = new BufferedWriter(new FileWriter(out))) {
        w.write("Apellidos,Nombres,Codigo\n");
        for (RowAlumno r : filtered) {
          if (r.isSelected()) {
            w.write(String.format("%s,%s,%s%n", r.getApellidos(), r.getNombres(), r.getCodigo()));
          }
        }
      }
      System.out.println("CSV generado: " + out.getAbsolutePath());
    } catch (Exception e) { e.printStackTrace(); }
  }

  // ==== POJO observable para la tabla ====
  public static class RowAlumno {
    private final BooleanProperty selected = new SimpleBooleanProperty(false);
    private final StringProperty apellidos = new SimpleStringProperty();
    private final StringProperty nombres = new SimpleStringProperty();
    private final StringProperty codigo = new SimpleStringProperty();
    private final String grado;
    private final String seccion;

    public RowAlumno(String apellidos, String nombres, String codigo, String grado, String seccion) {
      this.apellidos.set(apellidos);
      this.nombres.set(nombres);
      this.codigo.set(codigo);
      this.grado = grado;
      this.seccion = seccion;
    }

    public static RowAlumno of(String ap, String no, String co, String gr, String se) {
      return new RowAlumno(ap, no, co, gr, se);
    }

    public boolean isSelected() { return selected.get(); }
    public BooleanProperty selectedProperty() { return selected; }

    public String getApellidos() { return apellidos.get(); }
    public StringProperty apellidosProperty() { return apellidos; }

    public String getNombres() { return nombres.get(); }
    public StringProperty nombresProperty() { return nombres; }

    public String getCodigo() { return codigo.get(); }
    public StringProperty codigoProperty() { return codigo; }

    public String getGrado() { return grado; }
    public String getSeccion() { return seccion; }
  }
}
