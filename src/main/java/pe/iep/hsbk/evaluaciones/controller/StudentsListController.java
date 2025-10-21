package pe.iep.hsbk.evaluaciones.controller;

import javafx.beans.property.*;
import javafx.collections.*;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import pe.iep.hsbk.evaluaciones.util.Dialogs;

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

  @FXML private ToggleGroup grpGrados;     // grados 1..6
  @FXML private ToggleGroup grpSecciones;  // secciones A..E

  // estado de filtros
  private final StringProperty gradoSel = new SimpleStringProperty("1");
  private final StringProperty seccionSel = new SimpleStringProperty("A");

  private final ObservableList<RowAlumno> master = FXCollections.observableArrayList();
  private FilteredList<RowAlumno> filtered;
  Stage stage = null;

  @FXML
  public void initialize() {
    // === DATOS DE EJEMPLO ===
    master.addAll(
        // ===== GRADO 1 =====
        // 1° A (8)
        RowAlumno.of("ALEGRÍA MUÑOZ","GAEL MATHIAS","AM75148320","1","A"),
        RowAlumno.of("ALVARADO VALERIO","HELLEN LUCIANA","AV74810233","1","A"),
        RowAlumno.of("GARCÍA ROSALES","DIEGO ANDRÉ","GR77854012","1","A"),
        RowAlumno.of("LOZANO CHÁVEZ","EMILIA FIORELLA","LC90784531","1","A"),
        RowAlumno.of("SIFUENTES POMA","IAN RODRIGO","SP11845022","1","A"),
        RowAlumno.of("POMA SANDOVAL","MÍA VALENTINA","PS11733409","1","A"),
        RowAlumno.of("CÓRDOVA VILLANUEVA","BRUNO ALESSIO","CV11678433","1","A"),
        RowAlumno.of("VILCHEZ TORRES","SAMUEL JADIEL","VT11590872","1","A"),

        // 1° B (3)
        RowAlumno.of("CABRERA GALARZA","IKER IYALIN","CG01548946","1","B"),
        RowAlumno.of("QUISPE TINEO","MATEO JOAQUÍN","QT12845011","1","B"),
        RowAlumno.of("RIVERA ÑAUPARI","DANNA NICOLE","RN11457820","1","B"),

        // 1° C (6)
        RowAlumno.of("CABRERA QUISPE","BRIAN YAEL","CQ10247890","1","C"),
        RowAlumno.of("MURRUGARA RIVERA","SOFÍA CAMILA","MR78451209","1","C"),
        RowAlumno.of("MENDOZA RÍOS","GAEL ENZO","MR11320987","1","C"),
        RowAlumno.of("PALACIOS VERA","LUCIANA EMILIA","PV11254780","1","C"),
        RowAlumno.of("CRUZ SÁNCHEZ","LUIS ALFONSO","CS11145702","1","C"),
        RowAlumno.of("SALAZAR TORO","VALERIA FERNANDA","ST11090275","1","C"),

        // 1° D (2)
        RowAlumno.of("DELGADO CÓRDOVA","VALERIA NICOLE","DC66541203","1","D"),
        RowAlumno.of("SÁNCHEZ LÓPEZ","JOSÉ MANUEL","SL10987451","1","D"),

        // 1° E (10)
        RowAlumno.of("SORIANO PAREDES","LUCAS ALESSIO","SP88541026","1","E"),
        RowAlumno.of("RAMÍREZ CHERO","DANIELA SOFÍA","RC10895623","1","E"),
        RowAlumno.of("LLONTOP MERINO","EMMA PAOLA","LM10874126","1","E"),
        RowAlumno.of("CARRASCO IDROGO","SANTIAGO IVÁN","CI10842095","1","E"),
        RowAlumno.of("YUPANQUI CASTRO","ISABELA ANAHÍ","YC10811234","1","E"),
        RowAlumno.of("MORI FARFÁN","FRANCISCO XAVIER","MF10799031","1","E"),
        RowAlumno.of("TERRONES PAREDES","MICAELA ARIADNE","TP10767412","1","E"),
        RowAlumno.of("VALDIVIA ZELA","RENATO GABRIEL","VZ10734581","1","E"),
        RowAlumno.of("GUEVARA CAMPOS","CÉSAR ANDRÉS","GC10710982","1","E"),
        RowAlumno.of("DÁVILA SAAVEDRA","BRISA NATALIA","DS10687451","1","E"),

        // ===== GRADO 2 =====
        // 2° A (4)
        RowAlumno.of("DÁVILA DIONICIO","ARIADNE MAITE","DD04552978","2","A"),
        RowAlumno.of("FLORES CARRIÓN","RENATO GABRIEL","FC21457803","2","A"),
        RowAlumno.of("ALFARO QUISPE","VALENTINA SOFÍA","AQ20678433","2","A"),
        RowAlumno.of("VEGA MENDOZA","BRUNO JOSUÉ","VM20590124","2","A"),

        // 2° B (9)
        RowAlumno.of("FACHO ZEÑA","LUANA CRISTEL","FZ88741203","2","B"),
        RowAlumno.of("RIVERA MENDOZA","VALENTINA SOFÍA","RM70124589","2","B"),
        RowAlumno.of("ROMERO SOTO","SEBASTIÁN PABLO","RS20457893","2","B"),
        RowAlumno.of("GONZÁLES RÍOS","CHIARA GABRIELA","GR20411256","2","B"),
        RowAlumno.of("PAREDES TORO","ADRIANO JESÚS","PT20377411","2","B"),
        RowAlumno.of("CHERO BAZÁN","MARÍA CAMILA","CB20355109","2","B"),
        RowAlumno.of("BENAVIDES LEÓN","FABRICIO JOEL","BL20314780","2","B"),
        RowAlumno.of("SALDAÑA REYES","EMILIA VICTORIA","SR20290471","2","B"),
        RowAlumno.of("OJEDA AGUILAR","IAN FERNANDO","OA20266731","2","B"),

        // 2° C (2)
        RowAlumno.of("TORRES GUEVARA","ALONSO DANIEL","TG63214785","2","C"),
        RowAlumno.of("PAREDES VÍLCHEZ","CAMILA NATALIA","PV54871230","2","C"),

        // 2° D (15)
        RowAlumno.of("SOTO SANDOVAL","JULIÁN ESTEBAN","SS41527896","2","D"),
        RowAlumno.of("CHÁVEZ IZAGA","MÍA ALEJANDRA","CI30987456","2","D"),
        RowAlumno.of("CARRIÓN SOLANO","GABRIEL JOSÉ","CS20174568","2","D"),
        RowAlumno.of("BOLAÑOS TORRES","ANA LUCÍA","BT20159873","2","D"),
        RowAlumno.of("CALDERÓN GUEVARA","MATEO ENZO","CG20142357","2","D"),
        RowAlumno.of("DURÁN RIVERO","VALERY ROCÍO","DR20120258","2","D"),
        RowAlumno.of("ESPINOZA ZAVALETA","SEBASTIÁN LUCAS","EZ20098741","2","D"),
        RowAlumno.of("FERNÁNDEZ SÁNCHEZ","ISABELLA MAITE","FS20077894","2","D"),
        RowAlumno.of("GUTIÉRREZ QUINOÑES","FRANCISCO JAVIER","GQ20057138","2","D"),
        RowAlumno.of("HIDALGO BENITES","XIMENA ALEJANDRA","HB20034971","2","D"),
        RowAlumno.of("IGLESIAS CÉSPEDES","MATHÍAS ENRIQUE","IC20011984","2","D"),
        RowAlumno.of("JARA TUME","ALISSON DAYANA","JT19997851","2","D"),
        RowAlumno.of("LEÓN REQUEJO","BRUNO FABRICIO","LR19976218","2","D"),
        RowAlumno.of("MALCA VÁSQUEZ","NATALIA ELENA","MV19953480","2","D"),
        RowAlumno.of("NIEVES OLAECHEA","GABRIEL JOSUÉ","NO19931267","2","D"),

        // 2° E (5)
        RowAlumno.of("HERRERA PEÑA","FABRICIO JOEL","HP19874560","2","E"),
        RowAlumno.of("RUIZ ARÉVALO","ABIGAIL NICOLE","RA28754106","2","E"),
        RowAlumno.of("PINEDO CAMPOS","SANTIAGO TOMÁS","PC19821057","2","E"),
        RowAlumno.of("ZEA MURRUGARA","SOFÍA LUCÍA","ZM19798410","2","E"),
        RowAlumno.of("TUME LLONTOP","RENZO ADRIÁN","TL19771093","2","E"),

        // ===== GRADO 3 =====
        // 3° A (10)
        RowAlumno.of("GUERRERO SILVA","SAMUEL IVÁN","GS76521094","3","A"),
        RowAlumno.of("PEÑA SALAZAR","CAMILA SOFÍA","PS75210946","3","A"),
        RowAlumno.of("IDROGO VÁSQUEZ","RENZO JOEL","IV19687451","3","A"),
        RowAlumno.of("CABALLERO IGLESIAS","MÍA FERNANDA","CI19654108","3","A"),
        RowAlumno.of("TAPIA CÁRDENAS","EMILIO RAFAEL","TC19621470","3","A"),
        RowAlumno.of("ZAPATA IGLESIAS","MARÍA GRACIA","ZI19598763","3","A"),
        RowAlumno.of("BENITES CAMPOS","ARIANA DANIELA","BC69547012","3","A"),
        RowAlumno.of("SALVADOR RIVERA","JOSÉ MIGUEL","SR19544102","3","A"),
        RowAlumno.of("PAIMA CHERO","ALISON NICOLE","PC19510984","3","A"),
        RowAlumno.of("REYES OJEDA","PABLO ANDRÉS","RO19487450","3","A"),

        // 3° B (4)
        RowAlumno.of("MENDOZA RÍOS","NICOLÁS ADRIÁN","MR74120568","3","B"),
        RowAlumno.of("SALAZAR QUISPE","EMMA VALERIA","SQ73985120","3","B"),
        RowAlumno.of("RÍOS NEIRA","VALERIA NOEMÍ","RN19450133","3","B"),
        RowAlumno.of("LEANDRO QUISPE","DIEGO ENZO","LQ19427901","3","B"),

        // 3° C (7)
        RowAlumno.of("RODRÍGUEZ ZAVALA","LUIS ANTONIO","RZ72851640","3","C"),
        RowAlumno.of("CRUZ SÁNCHEZ","ISABELLA MILAGROS","CS71845290","3","C"),
        RowAlumno.of("CAMACHO TORRES","MATEO ARIEL","CT19398745","3","C"),
        RowAlumno.of("MESTANZA ROJAS","MÍA ISABELLA","MR19375106","3","C"),
        RowAlumno.of("NEIRA SOLANO","SEBASTIÁN IVÁN","NS19350428","3","C"),
        RowAlumno.of("TAMANI PAREDES","ARIEL JOSUÉ","TP19329176","3","C"),
        RowAlumno.of("YUPANQUI DÍAZ","EMILIA ROCÍO","YD19307964","3","C"),

        // 3° D (3)
        RowAlumno.of("VÁSQUEZ TORO","JOSÉ MANUEL","VT70651283","3","D"),
        RowAlumno.of("BENITES CAMPOS","ARIANA DANIELA","BC69547012","3","D"),
        RowAlumno.of("GAVIDIA SOTO","BRUNO LEONARDO","GS19287410","3","D"),

        // 3° E (5)
        RowAlumno.of("ORELLANA LEÓN","SEBASTIÁN PABLO","OL68974510","3","E"),
        RowAlumno.of("LINARES REYES","CHIARA GABRIELA","LR68125749","3","E"),
        RowAlumno.of("OLIVERA CUBA","MÍA ALESSANDRA","OC19247135","3","E"),
        RowAlumno.of("PUGA SANDOVAL","IAN JOAQUÍN","PS19220987","3","E"),
        RowAlumno.of("SANDOVAL CÓRDOVA","NAOMI MICHELLE","SC19197456","3","E"),

        // ===== GRADO 4 =====
        // 4° A (12)
        RowAlumno.of("ARROYO TAFUR","MARTÍN ALEJANDRO","AT67259014","4","A"),
        RowAlumno.of("DÍAZ CARRASCO","ALONDRA NICOL","DC66874102","4","A"),
        RowAlumno.of("RIVERA ROJAS","SANTIAGO EMILIO","RR19165079","4","A"),
        RowAlumno.of("CASTRO GUEVARA","BRISA SOFÍA","CG19140237","4","A"),
        RowAlumno.of("MORALES ZEA","MATHÍAS ARTURO","MZ19116024","4","A"),
        RowAlumno.of("ESPINOZA LAGOS","ARIANA LUCÍA","EL19091472","4","A"),
        RowAlumno.of("TUESTA REYES","DIEGO MANUEL","TR19067104","4","A"),
        RowAlumno.of("SÁNCHEZ TORRES","ALISSON NATALY","ST19041083","4","A"),
        RowAlumno.of("CIEZA OLAECHEA","JAVIER ALFONSO","CO19017645","4","A"),
        RowAlumno.of("MIRANDA VERA","VALERIA LUCÍA","MV65129870","4","A"),
        RowAlumno.of("PAREDES LEÓN","GABRIEL TOMÁS","PL18998763","4","A"),
        RowAlumno.of("ZAVALETA CAMPOS","EMILIA AURORA","ZC18976120","4","A"),

        // 4° B (5)
        RowAlumno.of("CASTILLO ÑIQUE","CRISTHIAN JADIEL","CN65987410","4","B"),
        RowAlumno.of("GUEVARA REYES","MATEO RENZO","GR18955071","4","B"),
        RowAlumno.of("SALINAS MENDOZA","DANNA VALENTINA","SM18931240","4","B"),
        RowAlumno.of("ORÉ CHERO","SEBASTIÁN MATÍAS","OC18910572","4","B"),
        RowAlumno.of("VIGO TUME","MÍA FIORELLA","VT18887452","4","B"),

        // 4° C (7)
        RowAlumno.of("PALACIOS RIVERO","ADRIÁN JESÚS","PR64571028","4","C"),
        RowAlumno.of("SANDOVAL DÍAZ","MÍA FERNANDA","SD63987012","4","C"),
        RowAlumno.of("MURRUGARA SIFUENTES","BRUNO ALESSANDRO","MS18864107","4","C"),
        RowAlumno.of("BENAVIDES OLAYA","SOFÍA EMMA","BO18839574","4","C"),
        RowAlumno.of("LEÓN TERRONES","JOSÉ ÁNGEL","LT18816049","4","C"),
        RowAlumno.of("VALENCIA CAMPOS","VALENTINA NURIA","VC18792516","4","C"),
        RowAlumno.of("OJEDA PAREDES","IAN FERNANDO","OP18770159","4","C"),

        // 4° D (4)
        RowAlumno.of("TINEO RUIZ","GABRIEL EMILIO","TR63124578","4","D"),
        RowAlumno.of("YUPANQUI LÓPEZ","ANA PAULA","YL62987501","4","D"),
        RowAlumno.of("ALZAMORA GARCÍA","SEBASTIÁN GABRIEL","AG18749152","4","D"),
        RowAlumno.of("ROSAS ZAPATA","VALERIA ROCÍO","RZ18726031","4","D"),

        // 4° E (9)
        RowAlumno.of("CÓRDOVA VILLANUEVA","FRANCO ANDRÉ","CV61874502","4","E"),
        RowAlumno.of("ZAPATA IGLESIAS","DANIELA SOFÍA","ZI61320987","4","E"),
        RowAlumno.of("CARHUAPOMA TORO","JULIÁN ADRIÁN","CT18694071","4","E"),
        RowAlumno.of("POZO SANDOVAL","MARÍA BELÉN","PS18670124","4","E"),
        RowAlumno.of("TERRONES LÓPEZ","PABLO EMILIO","TL18651290","4","E"),
        RowAlumno.of("MERINO CAMPOS","ALMA FERNANDA","MC18627941","4","E"),
        RowAlumno.of("CHERO CARRIÓN","RENZO ALESSIO","CC18604077","4","E"),
        RowAlumno.of("QUINTANA RUIZ","ISIDORA NICOLE","QR18584106","4","E"),
        RowAlumno.of("ROJAS SOLANO","MATEO AARÓN","RS18560198","4","E"),

        // ===== GRADO 5 =====
        // 5° A (3)
        RowAlumno.of("PAREDES RUIZ","DIEGO ALESSANDRO","PR60217845","5","A"),
        RowAlumno.of("VILLANUEVA CASTRO","ALONDRA MÓNICA","VC59784120","5","A"),
        RowAlumno.of("SÁNCHEZ YUPANQUI","JAVIER ANDRÉS","SY18537021","5","A"),

        // 5° B (10)
        RowAlumno.of("SIFUENTES POMA","ALEXANDER ENZO","SP58974120","5","B"),
        RowAlumno.of("RIVAS OJEDA","CECILIA MARÍA","RO58641279","5","B"),
        RowAlumno.of("PEÑA RÍOS","FRANCESCO GAEL","PR18510290","5","B"),
        RowAlumno.of("MARTÍNEZ ZEA","VALERIA EMILIA","MZ18485741","5","B"),
        RowAlumno.of("QUISPE TAFUR","MIGUEL ÁNGEL","QT18460177","5","B"),
        RowAlumno.of("IDROGO SOLANO","ARIEL TOMÁS","IS18437066","5","B"),
        RowAlumno.of("SANDOVAL LÓPEZ","NICOLE ISABELLA","SL18411025","5","B"),
        RowAlumno.of("CAMPOS VIGO","VALENTINA NURIA","CV57120986","5","B"),
        RowAlumno.of("CHONG MERCADO","MATÍAS JOAQUÍN","CM18387459","5","B"),
        RowAlumno.of("PATIÑO ROMERO","CAMILA AZUL","PR18362104","5","B"),

        // 5° C (5)
        RowAlumno.of("AGUILAR BAZÁN","JOSÉ ALFONSO","AB57961024","5","C"),
        RowAlumno.of("CAMPOS VIGO","VALENTINA NURIA","CV57120986","5","C"),
        RowAlumno.of("DIAZ BENAVIDES","NAOMI JULIETA","DB18340197","5","C"),
        RowAlumno.of("VÁSQUEZ REYES","SANTIAGO LEÓN","VR18317906","5","C"),
        RowAlumno.of("ORTIZ GUERRA","MIA CAMILA","OG18294075","5","C"),

        // 5° D (8)
        RowAlumno.of("REYNA GUEVARA","SEBASTIÁN LUCAS","RG56981032","5","D"),
        RowAlumno.of("VALLEJOS SOLÍS","MARÍA CAMILA","VS56471980","5","D"),
        RowAlumno.of("SOTO GUEVARA","GABRIEL ALONSO","SG18270156","5","D"),
        RowAlumno.of("LÓPEZ MENDOZA","XIMENA VICTORIA","LM18246950","5","D"),
        RowAlumno.of("SALAZAR RAMÍREZ","IAN MAURICIO","SR18221097","5","D"),
        RowAlumno.of("TUME SIFUENTES","ARON JESÚS","TS18197420","5","D"),
        RowAlumno.of("RIVERO DÁVILA","EMILIA ROCÍO","RD18174033","5","D"),
        RowAlumno.of("CASTILLO LEÓN","DIEGO LEANDRO","CL18151268","5","D"),

        // 5° E (6)
        RowAlumno.of("TERRONES PAREDES","IAN FERNANDO","TP55987120","5","E"),
        RowAlumno.of("MORI CARRIÓN","SOFÍA EMILIA","MC55749016","5","E"),
        RowAlumno.of("ZEA QUINOÑES","GABRIELA AURORA","ZQ18129106","5","E"),
        RowAlumno.of("LAVADO RUIZ","RENZO GAEL","LR18107024","5","E"),
        RowAlumno.of("COAGUILA VÁSQUEZ","ARIEL SANTIAGO","CV18084792","5","E"),
        RowAlumno.of("GARCÍA LLONTOP","VALERY NURIA","GL18060251","5","E"),

        // ===== GRADO 6 =====
        // 6° A (5)
        RowAlumno.of("ZUÑIGA TORRES","MATHÍAS ENRIQUE","ZT54871960","6","A"),
        RowAlumno.of("CARHUAPOMA DÍAZ","ALISSON DAYANA","CD54678120","6","A"),
        RowAlumno.of("ALBURQUEQUE MERINO","RENATO ANDRÉS","AM18037122","6","A"),
        RowAlumno.of("BUSTAMANTE QUISPE","CAMILA VICTORIA","BQ18014953","6","A"),
        RowAlumno.of("CUYA RÍOS","PABLO JOSÉ","CR17998042","6","A"),

        // 6° B (12)
        RowAlumno.of("SOLANO CUEVA","BRUNO FABRICIO","SC53987164","6","B"),
        RowAlumno.of("GUEVARA CORTEZ","NATALIA ELENA","GC53649012","6","B"),
        RowAlumno.of("HERRERA ZELA","JOSÉ IGNACIO","HZ17976104","6","B"),
        RowAlumno.of("IGOR LÓPEZ","SOFÍA ISABEL","IL17952086","6","B"),
        RowAlumno.of("JIMÉNEZ OLAYA","GAEL DAVID","JO17930864","6","B"),
        RowAlumno.of("KOGA MATSUMOTO","EMILIA SARAHI","KM17907133","6","B"),
        RowAlumno.of("LAGOS ARÉVALO","MARIO ESTEBAN","LA17885020","6","B"),
        RowAlumno.of("MERINO VIGO","LUCÍA ALEJANDRA","MV17861294","6","B"),
        RowAlumno.of("NÚÑEZ CAMPOS","FRANCISCO EZEQUIEL","NC17838015","6","B"),
        RowAlumno.of("ORTEGA REÁTEGUI","JAVIER ANDRÉS","OR17817061","6","B"),
        RowAlumno.of("PATIÑO TAFUR","DANNA SOFÍA","PT17796025","6","B"),
        RowAlumno.of("QUISPE CHOQUE","EDUARDO PIERRE","QC17774109","6","B"),

        // 6° C (4)
        RowAlumno.of("ROSAS LLONTOP","GABRIEL JOSUÉ","RL52976810","6","C"),
        RowAlumno.of("TOVAR SANDOVAL","VALERY ROCÍO","TS52741986","6","C"),
        RowAlumno.of("URBINA ROJAS","ALDO FERNANDO","UR17754120","6","C"),
        RowAlumno.of("VALERA CÓRDOVA","EMMA SOFÍA","VC17731984","6","C"),

        // 6° D (9)
        RowAlumno.of("ESPINOZA MERINO","JULIO CÉSAR","EM51978406","6","D"),
        RowAlumno.of("LÓPEZ BENAVIDES","ISABELLA MAITE","LB51673409","6","D"),
        RowAlumno.of("MENDOZA PAREDES","SANTIAGO ADRIÁN","MP17709046","6","D"),
        RowAlumno.of("NAVARRO CAMPOS","MARTÍN JESÚS","NC17686031","6","D"),
        RowAlumno.of("OLIVERA GUTIÉRREZ","VALENTINA MAYTE","OG17662107","6","D"),
        RowAlumno.of("PAREDES CORDERO","JOSÉ MIGUEL","PC17641059","6","D"),
        RowAlumno.of("QUINTANILLA VERA","DIEGO TOMÁS","QV17617022","6","D"),
        RowAlumno.of("RODRÍGUEZ SIFUENTES","XIMENA VICTORIA","RS17594013","6","D"),
        RowAlumno.of("SALAZAR TUME","IAN MATTEO","ST17570980","6","D"),

        // 6° E (3)
        RowAlumno.of("QUINOÑES FARFÁN","FRANCISCO JAVIER","QF50987126","6","E"),
        RowAlumno.of("MALPARTIDA ZEA","XIMENA ALEJANDRA","MZ50761289","6","E"),
        RowAlumno.of("TURPO VÁSQUEZ","GAEL ALONSO","TV17547106","6","E")
    );

    // === CONFIG TABLA / COLUMNAS ===
    tblAlumnos.setEditable(true);
    colSel.setEditable(true);

    // Selección: vincular propiedad y centrar celda
    colSel.setCellValueFactory(data -> data.getValue().selectedProperty());

    // CellFactory por índice: el checkbox controla directamente la propiedad de la fila
    colSel.setCellFactory(tc -> {
      CheckBoxTableCell<RowAlumno, Boolean> cell =
          new CheckBoxTableCell<>(index -> {
            if (index >= 0 && index < tblAlumnos.getItems().size()) {
              return tblAlumnos.getItems().get(index).selectedProperty();
            }
            return new SimpleBooleanProperty(false);
          });
      cell.setAlignment(Pos.CENTER);
      return cell;
    });
    colSel.setSortable(false);
    colSel.getStyleClass().add("centered");       // aplica CSS utilitario

    // Checkbox en header para seleccionar visibles
    CheckBox chkAll = new CheckBox();
    chkAll.setOnAction(e -> {
      boolean v = chkAll.isSelected();
      for (RowAlumno r : tblAlumnos.getItems()) {
        r.selectedProperty().set(v);
      }
    });
    colSel.setGraphic(chkAll);
    colSel.setPrefWidth(50);

    // Texto: alineación (vertical ya por CSS)
    colApellidos.setCellValueFactory(data -> data.getValue().apellidosProperty());
    colNombres.setCellValueFactory(data -> data.getValue().nombresProperty());
    colCodigo.setCellValueFactory(data -> data.getValue().codigoProperty());
    colApellidos.setStyle("-fx-alignment: CENTER-LEFT;");
    colNombres.setStyle("-fx-alignment: CENTER-LEFT;");
    colCodigo.setStyle("-fx-alignment: CENTER-LEFT;");

    // Acciones: botón centrado vertical + horizontal
    colAcciones.setCellFactory(col -> new TableCell<RowAlumno, Void>() {
      private final Button btn = buildEditButton();
      {
        btn.setOnAction(e -> {
          RowAlumno row = getTableView().getItems().get(getIndex());
          System.out.println("Editar: " + row.getApellidos() + ", " + row.getNombres());
        });
      }
      @Override
      protected void updateItem(Void item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
          setGraphic(null);
        } else {
          setGraphic(btn);
          setAlignment(Pos.CENTER);           // centro ambos ejes
        }
      }
    });
    colAcciones.getStyleClass().add("centered");

    // === FILTRO ===
    filtered = new FilteredList<>(master, buildPredicate());
    tblAlumnos.setItems(filtered);

    // Reaplicar filtro ante cambios
    gradoSel.addListener((obs, a, b) -> refiltrar());
    seccionSel.addListener((obs, a, b) -> refiltrar());
    if (txtBuscar != null) {
      txtBuscar.textProperty().addListener((obs, a, b) -> refiltrar());
    }

    // Estado inicial desde FXML
    if (grpGrados.getSelectedToggle() == null && !grpGrados.getToggles().isEmpty()) {
      grpGrados.selectToggle(grpGrados.getToggles().get(0)); // 1°
    }
    if (grpSecciones.getSelectedToggle() == null && !grpSecciones.getToggles().isEmpty()) {
      grpSecciones.selectToggle(grpSecciones.getToggles().get(0)); // A
    }

    // Sincronizar propiedades con selección actual
    Toggle tgG = grpGrados.getSelectedToggle();
    if (tgG != null && tgG.getUserData() != null) gradoSel.set(tgG.getUserData().toString());
    Toggle tgS = grpSecciones.getSelectedToggle();
    if (tgS != null && tgS.getUserData() != null) seccionSel.set(tgS.getUserData().toString());

    // Filtro inicial
    refiltrar();
  }

  private Button buildEditButton() {
    SVGPath pencil = new SVGPath();
    pencil.setContent("M3,14 L10,7 13,10 6,17 3,17z M10,6 L12,4 15,7 13,9z");
    Button b = new Button();
    b.getStyleClass().add("icon-btn");
    b.setGraphic(new HBox(pencil));
    return b;
  }

  private Predicate<RowAlumno> buildPredicate() {
    final String q = (txtBuscar == null) ? "" : txtBuscar.getText();
    return row -> row.getGrado().equals(gradoSel.get())
        && row.getSeccion().equalsIgnoreCase(seccionSel.get())
        && matchesSearch(row, q);
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

  @FXML
  private void onChangeGrado() {
    Toggle sel = grpGrados.getSelectedToggle();
    if (sel != null) {
      ToggleButton tb = (ToggleButton) sel;   // en FXML son ToggleButton
      Object ud = tb.getUserData();
      String g = (ud != null) ? ud.toString() : tb.getText().replace("°", "");
      gradoSel.set(g);

      // Al cambiar de grado, reset a sección A
      if (!grpSecciones.getToggles().isEmpty()) {
        grpSecciones.selectToggle(grpSecciones.getToggles().get(0));
        seccionSel.set("A");
      }
      refiltrar();
    }
  }

  @FXML
  private void onChangeSeccion() {
    Toggle sel = grpSecciones.getSelectedToggle();
    if (sel != null) {
      ToggleButton tb = (ToggleButton) sel;
      Object ud = tb.getUserData();
      String sec = (ud != null) ? ud.toString() : tb.getText().replace("Sección", "").trim();
      seccionSel.set(sec);
      refiltrar();
    }
  }

  @FXML
  private void onDescargar() {
    try {
      File out = new File(System.getProperty("user.home"), "alumnos_seleccion.csv");
      try (BufferedWriter w = new BufferedWriter(new FileWriter(out))) {
        w.write("Apellidos,Nombres,Codigo\n");
        for (RowAlumno r : tblAlumnos.getItems()) {
          if (r.isSelected()) {
            w.write(String.format("%s,%s,%s%n",
                r.getApellidos(), r.getNombres(), r.getCodigo()));
          }
        }
      }
      System.out.println("CSV generado: " + out.getAbsolutePath());
      Dialogs.info(stage, "Descarga Completada", "El archivo se ha generado correctamente en:\n" + out.getAbsolutePath());
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
