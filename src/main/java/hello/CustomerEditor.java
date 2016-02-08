package hello;

import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.Component;
import com.vaadin.ui.TextField;
import org.vaadin.viritin.form.AbstractForm;
import org.vaadin.viritin.layouts.MFormLayout;

@SpringComponent
@UIScope
public class CustomerEditor extends AbstractForm<Customer> {

    /* Fields to edit properties in Customer entity */
    TextField firstName = new TextField("First name");
    TextField lastName = new TextField("Last name");

    public CustomerEditor() {
        setVisible(false);
    }

    @Override
    protected Component createContent() {
        return new MFormLayout(firstName, lastName, getToolbar());
    }

}
