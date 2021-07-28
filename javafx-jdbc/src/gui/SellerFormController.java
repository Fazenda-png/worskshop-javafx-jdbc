package gui;

import java.net.URL;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import db.DbException;
import gui.listeners.DataChangeListener;
import gui.util.Alerts;
import gui.util.Constraints;
import gui.util.utils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.util.Callback;
import model.entities.Department;
import model.entities.Seller;
import model.exceptions.ValidationExceptions;
import model.services.DepartmentServices;
import model.services.SellerServices;

public class SellerFormController implements Initializable {

	private Seller entity;

	private SellerServices service;

	private DepartmentServices departmentService;

	private List<DataChangeListener> dataChangeListeners = new ArrayList();

	@FXML
	private TextField textId;
	@FXML
	private TextField textName;
	@FXML
	private TextField textEmail;
	@FXML
	private DatePicker dpBirthDate;
	@FXML
	private TextField textBaseSalary;
	@FXML
	private Label errorTextName;
	@FXML
	private Label errorTextEmail;
	@FXML
	private Label errorTextBirthDate;
	@FXML
	private Label errorTextBaseSalary;
	@FXML
	private Button btnSave;
	@FXML
	private Button btnCancel;
	@FXML
	private ComboBox<Department> comboBoxDepartment;

	private ObservableList<Department> obsList;

	public void onBtnSave(javafx.event.ActionEvent event) {
		if (entity == null) {
			throw new IllegalStateException("Entity was null");
		}
		if (service == null) {
			throw new IllegalStateException("Service was null");
		}
		try {
			entity = getFormData();
			service.SaveOrUpdate(entity);
			notifyDataChageListeres();
			utils.currentStage(event).close();
		} catch (DbException e) {
			Alerts.showAlert("Error saving onject", null, e.getMessage(), AlertType.ERROR);
		} catch (ValidationExceptions e) {
			setError(e.getErrors());
		}
	}

	private void notifyDataChageListeres() {
		for (DataChangeListener listeners : dataChangeListeners) {
			listeners.onDataChange();
		}
	}

	private Seller getFormData() {
		Seller obj = new Seller();

		ValidationExceptions exception = new ValidationExceptions("Validation error");

		obj.setId(utils.tryParseToInt(textId.getText()));
		if (textName.getText() == null || textName.getText().trim().equals("")) {
			exception.addError("name", "Field can't be empty");
		}
		obj.setName(textName.getText());
		if (textEmail.getText() == null || textEmail.getText().trim().equals("")) {
			exception.addError("email", "Field can't be empty");
		}
		obj.setEmail(textEmail.getText());

		if (dpBirthDate.getValue() == null) {
			exception.addError("birthDate", "Field can't be empty");
		}
		Instant instant = Instant.from(dpBirthDate.getValue().atStartOfDay(ZoneId.systemDefault()));
		obj.setBirthDate(Date.from(instant));

		if (textBaseSalary.getText() == null || textBaseSalary.getText().trim().equals("")) {
			exception.addError("baseSalary", "Field can't be empty");
		}
		obj.setBaseSalary(utils.tryParseToDouble(textBaseSalary.getText()));

		obj.setDepartment(comboBoxDepartment.getValue());
		if (exception.getErrors().size() > 0) {
			throw exception;
		}
		return obj;
	}

	public void onBtnCancel(javafx.event.ActionEvent event) {
		utils.currentStage(event).close();
	}

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		initializeNode();
	}

	private void initializeNode() {
		Constraints.setTextFieldMaxLength(textName, 10);
		Constraints.setTextFieldDouble(textBaseSalary);
		initializeComboBoxDepartment();
	}

	public void setSeller(Seller entity) {
		this.entity = entity;
	}

	public void setServices(SellerServices service, DepartmentServices departmentService) {
		this.service = service;
		this.departmentService = departmentService;
	}

	public void subscribeDataChangeListeners(DataChangeListener listeners) {
		dataChangeListeners.add(listeners);
	}

	public void updateFormData() {
		if (entity == null) {
			throw new IllegalStateException("Entity was null");
		}
		textId.setText(String.valueOf(entity.getId()));
		textName.setText(entity.getName());
		textEmail.setText(entity.getEmail());
		textBaseSalary.setText(String.format("%.2f", entity.getBaseSalary()));
		if (entity.getBirthDate() != null) {
			dpBirthDate.setValue(LocalDate.ofInstant(entity.getBirthDate().toInstant(), ZoneId.systemDefault()));
		}
		if (entity.getDepartment() == null) {
			comboBoxDepartment.getSelectionModel().selectFirst();
		}
		comboBoxDepartment.setValue(entity.getDepartment());
	}

	private void setError(Map<String, String> errors) {
		Set<String> fields = errors.keySet();

		if (fields.contains("name")) {
			errorTextName.setText(errors.get("name"));
		} else {
			errorTextName.setText("");
		}
		if (fields.contains("email")) {
			errorTextEmail.setText(errors.get("email"));
		} else {
			errorTextEmail.setText("");
		}
		if (fields.contains("baseSalary")) {
			errorTextBaseSalary.setText(errors.get("baseSalary"));
		} else {
			errorTextBaseSalary.setText("");
		}
		if (fields.contains("birthDate")) {
			errorTextBirthDate.setText(errors.get("birthDate"));
		} else {
			errorTextBirthDate.setText("");
		}
	}

	public void loadAssociateObjects() {
		if (departmentService == null) {
			throw new IllegalStateException("DepartmentServices was null");
		}
		List<Department> list = departmentService.findAll();
		obsList = FXCollections.observableArrayList(list);
		comboBoxDepartment.setItems(obsList);
	}

	private void initializeComboBoxDepartment() {
		Callback<ListView<Department>, ListCell<Department>> factory = lv -> new ListCell<Department>() {
			@Override
			protected void updateItem(Department item, boolean empty) {
				super.updateItem(item, empty);
				setText(empty ? "" : item.getName());
			}
		};
		comboBoxDepartment.setCellFactory(factory);
		comboBoxDepartment.setButtonCell(factory.call(null));
	}
}
