package hello;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.annotations.Theme;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Button;
import com.vaadin.ui.UI;
import org.vaadin.viritin.fields.MTable;
import org.vaadin.viritin.layouts.MVerticalLayout;

@SpringUI
@Theme("valo")
public class VaadinUI extends UI {

	private final CustomerRepository repo;

	private final CustomerEditor editor;

	private final MTable<Customer> grid;

	private final Button addNewBtn;

	@Autowired
	public VaadinUI(CustomerRepository repo, CustomerEditor editor) {
		this.repo = repo;
		this.editor = editor;
		this.grid = new MTable<>(Customer.class)
                .withProperties("id", "firstName", "lastName")
                .withHeight("300px");
		this.addNewBtn = new Button("New customer", FontAwesome.PLUS);
	}

	@Override
	protected void init(VaadinRequest request) {
		// Connect selected Customer to editor or hide if none is selected
        grid.addMValueChangeListener(e->{
            if(e.getValue() == null) {
                editor.setVisible(false);
            } else {
				editor.setEntity(e.getValue());
            }
        });

		// Instantiate and edit new Customer the new button is clicked
		addNewBtn.addClickListener(e -> editor.setEntity(new Customer("", "")));

		// Listen changes made by the editor, refresh data from backend
        editor.setSavedHandler(customer->{
            repo.save(customer);
            listCustomers();
            editor.setVisible(false);
        });
        
        editor.setResetHandler(customer->{
            editor.setVisible(false);
            listCustomers();
        });
        
        editor.setDeleteHandler(customer -> {
            repo.delete(customer);
            listCustomers();
        });

		// Initialize listing
		listCustomers();

        // build layout
		setContent(new MVerticalLayout(addNewBtn, grid, editor));
	}

	private void listCustomers() {
        grid.setBeans(repo.findAll());
	}

}
