package gui;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import model.entities.Department;
import model.services.DeparmentServices;

public class DepartmentListController implements Initializable {

	private DeparmentServices service;

	@FXML
	private TableView<Department> tableViewDepartment;
	@FXML
	private TableColumn<Department, Integer> tableColumId;
	@FXML
	private TableColumn<Department, String> tableColumName;
	@FXML
	private Button btnNew;

	private ObservableList<Department> obslist;

	public void onBtnNewAction() {
		System.out.println("OLÁ");
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		initializeNodes();

	}

	public void setDepartmentService(DeparmentServices service) {
		this.service = service;
	}

	private void initializeNodes() {
		tableColumId.setCellValueFactory(new PropertyValueFactory<>("id"));
		tableColumName.setCellValueFactory(new PropertyValueFactory<>("name"));
	}

	public void updateTableView() {
		if (service == null) {
			throw new IllegalStateException("Services was null");
		}
		List<Department> list = service.findAll();
		obslist = FXCollections.observableArrayList(list);
		tableViewDepartment.setItems(obslist);
	}
}
