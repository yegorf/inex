package sample.Controllers;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import sample.CurrentUser;
import sample.DB.DatabaseHandler;
import sample.Entries.Currency;
import sample.Entries.Income;
import sample.Entries.Money;
import sample.Entries.User;
import sample.LineChart;
import sample.RateParser;
import sample.usdConverter;


public class GeneralController {

    @FXML
    private Label usernameLabel;

    @FXML
    private TableColumn<Income, String> sumColumn;

    @FXML
    private TableView<Income> incomeTable;

    @FXML
    private TableColumn<Income, String> dateColumn;

    @FXML
    private TableColumn<Income, String> reasonColumn;

    @FXML
    private TableColumn<Income, String> currencyColumn;

    @FXML
    private Label eurLabel;

    @FXML
    private Label usdLabel;

    @FXML
    private Label rubLabel;

    @FXML
    private Label eurLabel1;

    @FXML
    private Label usdLabel1;

    @FXML
    private Label rubLabel1;

    @FXML
    private Label nowRub;

    @FXML
    private Label nowEur;

    @FXML
    private Label nowUah;

    @FXML
    private Label nowUsd;

    @FXML
    private Button signOutButton;

    @FXML
    private RadioButton usdRB;

    @FXML
    private RadioButton rubRB;

    @FXML
    private RadioButton uahRB;

    @FXML
    private RadioButton eurRB;

    @FXML
    private RadioButton incomeRB;

    @FXML
    private Button addButton;

    @FXML
    private TextField sumTextBox;

    @FXML
    private TextField reasonTextBox;

    @FXML
    private Label totalUsdLabel;

    @FXML
    private javafx.scene.chart.LineChart<?, ?> incomeChart;

    @FXML
    private CategoryAxis x;

    @FXML
    private NumberAxis y;



    @FXML
    void initialize() {

        XYChart.Series series = new XYChart.Series();
        series.getData().add(new XYChart.Data("1", 23));
        incomeChart.getData().addAll(series);

        usdConverter converter = new usdConverter();
        DatabaseHandler dbHandler = new DatabaseHandler();
        RateParser rateParser = new RateParser();
        ArrayList<Double> incomeList = showTable(dbHandler);

        showCurrentScore(incomeList);
        setRates(rateParser);

        double sum = convertMoney(converter,incomeList,rateParser);
        totalUsdLabel.setText(Double.toString(Math.rint(100.0*sum)/100.0)+" $");

        signOutButton.setOnAction(event -> {
            openNewScene("/sample/fxml_files/sample.fxml");
        });

        addButton.setOnAction(event -> {
            addIncome(dbHandler);
            showTable(dbHandler);
        });
    }

    public String getCurrentMontYear() {
        Date dateNow = new Date();
        SimpleDateFormat formatForDateNow =
                new SimpleDateFormat("dd-mm-yyyy");
        return formatForDateNow.format(dateNow);
    }

    public void addIncome(DatabaseHandler dbHandler) {
        Date dateNow = new Date();
        SimpleDateFormat formatForDateNow =
                new SimpleDateFormat("yyyy-MM-dd");


        Income income = new Income();

        income.setSum(Double.parseDouble(sumTextBox.getText()));
        income.setReason(reasonTextBox.getText());
        income.setDate(formatForDateNow.format(dateNow));

        if(incomeRB.isSelected()) {
            income.setPositive(true);
        } else {
            income.setPositive(false);
        }

        if(rubRB.isSelected()) {
            income.setCurrency(Currency.RUB);
        }
        else if(uahRB.isSelected()) {
            income.setCurrency(Currency.UAH);
        }
        else if(usdRB.isSelected()) {
            income.setCurrency(Currency.USD);
        }
        else if(eurRB.isSelected()) {
            income.setCurrency(Currency.EUR);
        }

        dbHandler.addIncome(income);
        clearAddParameters();
    }

    public double convertMoney(usdConverter converter,
                               ArrayList<Double> incomeList,
                               RateParser rateParser) {
        converter.convertUah(incomeList.get(1), Double.parseDouble(rateParser.getSellUsdRate()));
        converter.convertRub(incomeList.get(0), Double.parseDouble(rateParser.getSellRubRate()), Double.parseDouble(rateParser.getSellUsdRate()));
        converter.convertEur(incomeList.get(3), Double.parseDouble(rateParser.getSellUsdRate()), Double.parseDouble(rateParser.getSellEurRate()));

        double sum = converter.getValue() + incomeList.get(2);
        return sum;
    }

    public void setRates(RateParser rateParser) {
        usdLabel.setText(rateParser.getBuyUsdRate());
        eurLabel.setText(rateParser.getBuyEurRate());
        rubLabel.setText(rateParser.getBuyRubRate());
        usdLabel1.setText(rateParser.getSellUsdRate());
        eurLabel1.setText(rateParser.getSellEurRate());
        rubLabel1.setText(rateParser.getSellRubRate());
    }

    public void showCurrentScore(ArrayList<Double> incomeList) {
        nowRub.setText("RUB: " + incomeList.get(0).toString());
        nowUah.setText("UAH: " + incomeList.get(1).toString());
        nowUsd.setText("USD: " + incomeList.get(2).toString());
        nowEur.setText("EUR: " + incomeList.get(3).toString());
    }

    public ArrayList<Double> showTable(DatabaseHandler dbHandler) {
        Money money = dbHandler.getAllIncome();
        ArrayList<Double> incomeList = money.getIncome();

        currencyColumn.setCellValueFactory(new PropertyValueFactory<>("Currency"));
        sumColumn.setCellValueFactory(new PropertyValueFactory<>("Sum"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("Date"));
        reasonColumn.setCellValueFactory(new PropertyValueFactory<>("Reason"));
        incomeTable.setItems(money.getList());

        return incomeList;
    }

    public void clearAddParameters() {
        sumTextBox.clear();
        reasonTextBox.clear();
        rubRB.setSelected(true);
        incomeRB.setSelected(true);
    }

    public void openNewScene(String window) {
        signOutButton.getScene().getWindow().hide();

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource(window));

        try {
            loader.load();
        }catch(IOException e){e.printStackTrace();}

        Parent root = loader.getRoot();
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.show();
    }
}