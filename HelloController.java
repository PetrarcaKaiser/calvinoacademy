package com.example.demo;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import org.apache.poi.xwpf.usermodel.*;

import java.io.*;
import java.sql.*;

public class HelloController {


    @FXML
    private TextField nameField;

    @FXML
    private TextField surnameField;

    @FXML
    private Button processButton;

    @FXML
    private Button createButton;

    @FXML
    private ChoiceBox<String> classChoiceBox;

    @FXML
    private ChoiceBox<String> yearChoiceBox;

    @FXML
    private ChoiceBox<String> studentChoiceBox;

    @FXML
    private void initialize() {
        classChoiceBox.getItems().addAll("5AII", "4AII", "3AII");

        String url = "jdbc:mysql://localhost:3306/calvinoacademy2";
        String user = "root";
        String password = "";

        try {
            Connection conn = DriverManager.getConnection(url, user, password);

            String queryYear = "SELECT sy.description AS anno FROM ca_school_years AS sy";
            PreparedStatement pstmtYear = conn.prepareStatement(queryYear);
            ResultSet rsYear = pstmtYear.executeQuery();

            while (rsYear.next()) {
                String year = rsYear.getString("anno");
                yearChoiceBox.getItems().add(year);
            }

            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void createDetails(ActionEvent event) {
        String name = nameField.getText();
        String surname = surnameField.getText();
        String classe = classChoiceBox.getValue();
        String anno = yearChoiceBox.getValue();

        String url = "jdbc:mysql://localhost:3306/calvinoacademy2";
        String user = "root";
        String password = "";

        try {
            Connection conn = DriverManager.getConnection(url, user, password);

            String queryStudente = "SELECT CONCAT(u.name, ' ', u.surname) AS Name, c.title as NomeCorso, " +
                    "HOUR(SEC_TO_TIME(SUM(TIMESTAMPDIFF(SECOND, a.started_at, a.ended_at)))) AS OreSvolte " +
                    "FROM ca_users u " +
                    "JOIN ca_presences p ON p.user_id = u.id " +
                    "JOIN ca_activities a ON p.activity_id = a.id " +
                    "JOIN ca_courses c ON a.course_id = c.id " +
                    "WHERE u.name = ? AND u.surname = ? " +
                    "GROUP BY c.title";

            PreparedStatement pstmtStudente = conn.prepareStatement(queryStudente);
            pstmtStudente.setString(1, name);
            pstmtStudente.setString(2, surname);
            ResultSet rsStudente = pstmtStudente.executeQuery();

            InputStream fis = getClass().getResourceAsStream("documento.docx");
            XWPFDocument document = new XWPFDocument(fis);
            fis.close();

            int corsoCounter = 0;

            String nomeStudente = null;
            while (rsStudente.next()) {
                nomeStudente = rsStudente.getString("Name");
                String nomeCorso = rsStudente.getString("NomeCorso");
                String oreStudente = rsStudente.getString("OreSvolte");

                PreparedStatement pstmtOreCorso = conn.prepareStatement("SELECT HOUR(SEC_TO_TIME(SUM(TIMESTAMPDIFF(SECOND, a.started_at, a.ended_at)))) AS OreTotali " +
                        "FROM ca_activities a JOIN ca_courses c ON a.course_id = c.id " +
                        "WHERE c.title = ?");
                pstmtOreCorso.setString(1, nomeCorso);
                ResultSet rsOreCorso = pstmtOreCorso.executeQuery();

                String oreTotali = "";
                if (rsOreCorso.next()) {
                    oreTotali = rsOreCorso.getString("OreTotali");
                }

                PreparedStatement pstmtDocente = conn.prepareStatement("SELECT CONCAT(u.name, ' ', u.surname) AS docente " +
                        "FROM ca_activities a " +
                        "JOIN ca_courses c ON a.course_id = c.id " +
                        "JOIN ca_users u ON c.user_id = u.id " +
                        "WHERE c.title = ? " +
                        "LIMIT 1");
                pstmtDocente.setString(1, nomeCorso);
                ResultSet rsDocente = pstmtDocente.executeQuery();

                String nomeDocente = "";
                if (rsDocente.next()) {
                    nomeDocente = rsDocente.getString("docente");
                }

                replaceTextInTable(document, "{nomeCorso" + corsoCounter + "}", nomeCorso);
                replaceTextInTable(document, "{ore" + corsoCounter + "}", oreStudente);
                replaceTextInTable(document, "{oreTot" + corsoCounter + "}", oreTotali);
                replaceTextInTable(document, "{docente" + corsoCounter + "}", nomeDocente);

                corsoCounter++;
            }

            PreparedStatement pstmtClasse = conn.prepareStatement("SELECT sc.name AS classe " +
                    "FROM ca_frequented_classes f " +
                    "JOIN ca_users u ON f.user_id = u.id " +
                    "JOIN ca_school_classes sc ON f.school_class_id = sc.id " +
                    "WHERE u.name = ? AND u.surname = ?");
            pstmtClasse.setString(1, name);
            pstmtClasse.setString(2, surname);
            ResultSet rsClasse = pstmtClasse.executeQuery();

            String nomeClasse = "";
            if (rsClasse.next()) {
                nomeClasse = rsClasse.getString("classe");
            }

            PreparedStatement pstmtAnno = conn.prepareStatement("SELECT sy.description AS anno " +
                    "FROM ca_frequented_classes f " +
                    "JOIN ca_users u ON f.user_id = u.id " +
                    "JOIN ca_school_classes sc ON f.school_class_id = sc.id " +
                    "JOIN ca_school_years sy ON sc.school_year_id = sy.id " +
                    "WHERE u.name = ? AND u.surname = ?");
            pstmtAnno.setString(1, name);
            pstmtAnno.setString(2, surname);
            ResultSet rsAnno = pstmtAnno.executeQuery();

            String annoClasse = "";
            if (rsAnno.next()) {
                annoClasse = rsAnno.getString("anno");
            }

            PreparedStatement pstmtTot = conn.prepareStatement("SELECT HOUR(SEC_TO_TIME(SUM(TIMESTAMPDIFF(SECOND, p.exited_at, p.joined_at)))) as tot " +
                    "FROM ca_presences as p " +
                    "JOIN ca_users as u ON u.id = p.user_id " +
                    "WHERE u.name = ? AND u.surname = ?");
            pstmtTot.setString(1, name);
            pstmtTot.setString(2, surname);
            ResultSet rsTot = pstmtTot.executeQuery();

            String oreTot = "";
            if (rsTot.next()) {
                oreTot = rsTot.getString("tot");
                System.out.println("Totale ore: " + oreTot);
            }

            PreparedStatement pstmtDate = conn.prepareStatement("SELECT DATE_FORMAT(NOW(), '%d-%m-%Y') AS date");
            ResultSet rsDate = pstmtDate.executeQuery();
            String dataCorrente = "";
            if (rsDate.next()) {
                dataCorrente = rsDate.getString("date");
                System.out.println("Data corrente: " + dataCorrente);
            }

            replaceText(document, "{name}", nomeStudente);
            replaceText(document, "{c}", classe);
            replaceText(document, "{anno}", anno);
            replaceText(document, "{t}", oreTot);
            replaceText(document, "{dataCorrente}", dataCorrente);

            FileOutputStream fos = new FileOutputStream("documento_modificato.docx");
            document.write(fos);
            fos.close();

            System.out.println("Documento modificato con successo!");

            conn.close();
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    private void clearChoiceBox(ChoiceBox<String> choiceBox) {
        choiceBox.getItems().clear();
    }

    @FXML
    private void processDetails(ActionEvent event) {

        String url = "jdbc:mysql://localhost:3306/calvinoacademy2";
        String user = "root";
        String password = "";

        try {
            clearChoiceBox(studentChoiceBox);
            Connection conn = DriverManager.getConnection(url, user, password);

            String queryStudente = "SELECT CONCAT(u.name, ' ', u.surname) AS name " +
                    "FROM ca_school_classes AS sc " +
                    "JOIN ca_school_years AS sy ON sy.id = sc.school_year_id " +
                    "JOIN ca_frequented_classes AS f ON f.school_class_id = sc.id " +
                    "JOIN ca_users AS u ON u.id = f.user_id " +
                    "JOIN ca_role_user AS ru ON ru.user_id = u.id " +
                    "JOIN ca_roles AS r ON r.id = ru.role_id " +
                    "WHERE sc.name = ? AND sy.description = ? AND ru.role_id = 1";
            PreparedStatement pstmtStudente = conn.prepareStatement(queryStudente);
            pstmtStudente.setString(1, classChoiceBox.getValue());
            pstmtStudente.setString(2, yearChoiceBox.getValue().replace("Anno Scolastico ", ""));
            ResultSet rsStudente = pstmtStudente.executeQuery();

            while (rsStudente.next()) {
                String studentName = rsStudente.getString("name");
                studentChoiceBox.getItems().add(studentName);
            }

            // Aggiungi un gestore degli eventi per la choice box studentChoiceBox
            studentChoiceBox.setOnAction(e -> {
                String selectedStudent = studentChoiceBox.getValue();
                if (selectedStudent != null) {
                    String[] parts = selectedStudent.split(" ");
                    if (parts.length >= 2) {
                        nameField.setText(parts[0]); // il primo pezzo è il nome
                        surnameField.setText(parts[1]); // il secondo pezzo è il cognome
                    }
                }
            });

            PreparedStatement pstmtClasse = conn.prepareStatement("SELECT sc.name AS classe FROM ca_school_classes AS sc " +
                    "JOIN ca_school_years AS sy ON sy.id = sc.school_year_id " +
                    "WHERE sy.description = ?");
            pstmtClasse.setString(1, yearChoiceBox.getValue().replace("Anno Scolastico ", ""));
            ResultSet rsClasse = pstmtClasse.executeQuery();

            while (rsClasse.next()) {
                String classe = rsClasse.getString("classe");
                classChoiceBox.getItems().add(classe);
            }

            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private static void replaceText(XWPFDocument doc, String findText, String replaceText) {
        for (XWPFParagraph p : doc.getParagraphs()) {
            for (XWPFRun r : p.getRuns()) {
                String text = r.getText(0);
                if (text != null && text.contains(findText)) {
                    text = text.replace(findText, replaceText);
                    r.setText(text, 0);
                }
            }
        }
    }

    private static void replaceTextInTable(XWPFDocument doc, String findText, String replaceText) {
        for (XWPFTable tbl : doc.getTables()) {
            for (XWPFTableRow row : tbl.getRows()) {
                for (XWPFTableCell cell : row.getTableCells()) {
                    for (XWPFParagraph p : cell.getParagraphs()) {
                        for (XWPFRun r : p.getRuns()) {
                            String text = r.getText(0);
                            if (text != null && text.contains(findText)) {
                                text = text.replace(findText, replaceText);
                                r.setText(text, 0);
                            }
                        }
                    }
                }
            }
        }
    }
}