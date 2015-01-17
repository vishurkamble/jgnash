/*
 * jGnash, a personal finance application
 * Copyright (C) 2001-2015 Craig Cavanaugh
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package jgnash.uifx.views.register;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

import jgnash.engine.Account;
import jgnash.engine.Transaction;
import jgnash.util.NotNull;

/**
 * Register pane controller
 *
 * @author Craig Cavanaugh
 */
public abstract class RegisterPaneController implements Initializable {

    @FXML
    protected Button newButton;

    @FXML
    protected Button duplicateButton;

    @FXML
    protected Button deleteButton;

    /**
     * The register table and controls should be loaded into this pane
     */
    @FXML
    protected StackPane register;

    protected ResourceBundle resources;

    /**
     * Active account for the pane
     */
    private final ObjectProperty<Account> accountProperty = new SimpleObjectProperty<>();

    /**
     * This will be bound to the register table selection
     */
    final ObjectProperty<Transaction> selectedTransactionProperty = new SimpleObjectProperty<>();

    protected final ObjectProperty<RegisterTableController> registerTableControllerProperty = new SimpleObjectProperty<>();

    ObjectProperty<Account> getAccountProperty() {
        return accountProperty;
    }

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        this.resources = resources;

        /**
         * The Buttons may be null depending on the form tha was loaded with this controller
         */

        // Buttons should not be enabled if a transaction is not selected
        if (deleteButton != null) {
            deleteButton.disableProperty().bind(selectedTransactionProperty.isNull());
        }

        if (duplicateButton != null) {
            duplicateButton.disableProperty().bind(selectedTransactionProperty.isNull());
        }

        // Clear the table selection
        if (newButton != null) {
            newButton.setOnAction(event -> registerTableControllerProperty.get().clearTableSelection());
        }

        // When changed, bind the selected transaction and account properties
        registerTableControllerProperty.addListener(new ChangeListener<RegisterTableController>() {
            @Override
            public void changed(final ObservableValue<? extends RegisterTableController> observable,
                                final RegisterTableController oldValue, final RegisterTableController newValue) {

                // Bind transaction selection to the register table controller
                selectedTransactionProperty.bind(newValue.getSelectedTransactionProperty());

                // Bind the register pane to this account property
                newValue.getAccountProperty().bind(getAccountProperty());
            }
        });

        // When changed, call for transaction modification if not null
        selectedTransactionProperty.addListener(new ChangeListener<Transaction>() {
            @Override
            public void changed(final ObservableValue<? extends Transaction> observable, final Transaction oldValue,
                                final Transaction newValue) {

                /* Push to the end of the application thread to allow other UI controls to update before
                * updating many transaction form controls */

                Platform.runLater(() -> {
                    if (newValue != null) {
                        modifyTransaction(newValue);
                    } else {
                        clearForm(); // selection was forcibly cleared, better clear the form
                    }
                });
            }
        });
    }

    @FXML
    private void handleDeleteAction() {
        registerTableControllerProperty.get().deleteTransactions();
    }

    /**
     * Default empty implementation to modify a transaction when selected
     *
     * @param transaction {@code Transaction} to be modified
     */
    protected void modifyTransaction(@NotNull final Transaction transaction) {

    }

    /**
     * Default empty implementation.
     */
    protected void clearForm() {

    }

    @FXML
    private void handleDuplicateAction() {
        clearForm();

        RegisterActions.duplicateTransaction(accountProperty.get(), registerTableControllerProperty.get().getSelectedTransactions());

        // Request focus as it may have been lost
        register.getScene().getWindow().requestFocus();
    }
}
