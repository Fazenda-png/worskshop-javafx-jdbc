package gui;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import db.DbException;
import gui.listeners.DataChangeListener;
import gui.util.Alerts;
import gui.util.Constraints;
import gui.util.utils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import model.entities.Department;
import model.exceptions.ValidationExceptions;
import model.services.DepartmentServices;

public class DepartmentFormController implements Initializable {

	private Department entity;

	private DepartmentServices service;

	private List<DataChangeListener> dataChangeListeners = new ArrayList();

	@FXML
	private TextField textId;
	@FXML
	private TextField textName;
	@FXML
	private Label errorTextName;
	@FXML
	private Button btnSave;
	@FXML
	private Button btnCancel;

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

	private Department getFormData() {
		Department obj = new Department();

		ValidationExceptions exception = new ValidationExceptions("Validation error");

		obj.setId(utils.TryParseToInt(textId.getText()));
		if (textName.getText() == null || textName.getText().trim().equals("")) {
			exception.addError("name", "Field can't be empty");
		}
		obj.setName(textName.getText());

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

	}

	private void initializeNode() {
		Constraints.setTextFieldMaxLength(textName, 10);
	}

	public void setDepartment(Department entity) {
		this.entity = entity;
	}

	public void setDepartmentService(DepartmentServices service) {
		this.service = service;
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
	}

	private void setError(Map<String, String> errors) {
		Set<String> fields = errors.keySet();

		if (fields.contains("name")) {
			errorTextName.setText(errors.get("name"));
		}
	}
}
