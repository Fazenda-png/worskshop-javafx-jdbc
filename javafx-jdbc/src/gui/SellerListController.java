package gui;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import db.DbIntegrityException;
import gui.listeners.DataChangeListener;
import gui.util.Alerts;
import gui.util.utils;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.entities.Seller;
import model.services.DepartmentServices;
import model.services.SellerServices;

public class SellerListController implements Initializable, DataChangeListener {

	private SellerServices service;

	@FXML
	private TableView<Seller> tableViewSeller;
	@FXML
	private TableColumn<Seller, Integer> tableColumId;
	@FXML
	private TableColumn<Seller, String> tableColumEmail;
	@FXML
	private TableColumn<Seller, Date> tableColumBirthDate;
	@FXML
	private TableColumn<Seller, Double> tableColumSalary;
	@FXML
	private TableColumn<Seller, String> tableColumName;
	@FXML
	private TableColumn<Seller, Seller> tableColumEDIT;
	@FXML
	private TableColumn<Seller, Seller> tableColumREMOVE;
	@FXML
	private Button btnNew;

	private ObservableList<Seller> obslist;

	public void onBtnNewAction(ActionEvent event) {
		Stage parentStage = utils.currentStage(event);
		Seller obj = new Seller();
		createDialogForm(obj, "/gui/SellerForm.fxml", parentStage);
	}

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		initializeNodes();

	}

	public void setSellerService(SellerServices service) {
		this.service = service;
	}

	private void initializeNodes() {
		tableColumId.setCellValueFactory(new PropertyValueFactory<>("id"));
		tableColumName.setCellValueFactory(new PropertyValueFactory<>("name"));
		tableColumId.setCellValueFactory(new PropertyValueFactory<>("email"));
		tableColumName.setCellValueFactory(new PropertyValueFactory<>("birthDate"));
		utils.formatTableColumnDate(tableColumBirthDate, "dd/MM/yyyy");
		tableColumId.setCellValueFactory(new PropertyValueFactory<>("baseSalary"));
		utils.formatTableColumnDouble(tableColumSalary, 2);
	}

	public void updateTableView() {
		if (service == null) {
			throw new IllegalStateException("Services was null");
		}
		List<Seller> list = service.findAll();
		obslist = FXCollections.observableArrayList(list);
		tableViewSeller.setItems(obslist);
		initEditButtons();
		initRemoveButtons();
	}

	private void createDialogForm(Seller obj, String absoluteName, Stage parentStage) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource(absoluteName));
			Pane pane = loader.load();

			SellerFormController controller = loader.getController();
			controller.setSeller(obj);
			controller.setServices(new SellerServices(), new DepartmentServices());
			controller.loadAssociateObjects();
			controller.subscribeDataChangeListeners(this);
			controller.updateFormData();

			Stage dialogStage = new Stage();
			dialogStage.setTitle("Enter Seller data");
			dialogStage.setScene(new Scene(pane));
			dialogStage.setResizable(false);
			dialogStage.initOwner(parentStage);
			dialogStage.initModality(Modality.WINDOW_MODAL);
			dialogStage.showAndWait();

		} catch (IOException e) {
			Alerts.showAlert("IO Exception", "Error loading view", e.getMessage(), AlertType.ERROR);
		}
	}

	@Override
	public void onDataChange() {
		updateTableView();
	}

	private void initEditButtons() {
		tableColumEDIT.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
		tableColumEDIT.setCellFactory(param -> new TableCell<Seller, Seller>() {
			private final Button button = new Button("edit");

			@Override
			protected void updateItem(Seller obj, boolean empty) {
				super.updateItem(obj, empty);
				if (obj == null) {
					setGraphic(null);
					return;
				}
				setGraphic(button);
				button.setOnAction(event -> createDialogForm(obj, "/gui/SellerForm.fxml", utils.currentStage(event)));
			}
		});
	}

	private void initRemoveButtons() {
		tableColumREMOVE.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
		tableColumREMOVE.setCellFactory(param -> new TableCell<Seller, Seller>() {
			private final Button button = new Button("remove");

			@Override
			protected void updateItem(Seller obj, boolean empty) {
				super.updateItem(obj, empty);
				if (obj == null) {
					setGraphic(null);
					return;
				}
				setGraphic(button);
				button.setOnAction(event -> removeEntity(obj));
			}
		});
	}

	private void removeEntity(Seller obj) {
		Optional<ButtonType> result = Alerts.showConfirmation("Confirmation", "Are you sure to delete");

		if (result.get() == ButtonType.OK) {
			if (service == null) {
				throw new IllegalStateException("Services was null");
			}
			try {
				service.remove(obj);
				updateTableView();
			} catch (DbIntegrityException e) {
				Alerts.showAlert("Error removing", null, e.getMessage(), AlertType.ERROR);
			}

		}
	}
}
