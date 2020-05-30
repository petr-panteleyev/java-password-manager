/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.pwdmanager;

import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.panteleyev.fx.BaseDialog;
import org.panteleyev.pwdmanager.model.Card;
import java.util.List;
import static org.panteleyev.fx.GridFactory.newGridPane;
import static org.panteleyev.fx.LabelFactory.newLabel;
import static org.panteleyev.pwdmanager.PasswordManagerApplication.RB;

abstract class RecordDialog extends BaseDialog<Card> implements Styles {
    private final ValidationSupport validation = new ValidationSupport();

    private final TextField nameEdit = new TextField();
    private final ComboBox<RecordType> typeList = new ComboBox<>();
    private final ComboBox<Picture> pictureList = new ComboBox<>();
    private final Label typeLabel = newLabel(RB, "label.type");

    RecordDialog() {
        super(MainWindowController.CSS_PATH);

        nameEdit.setPrefColumnCount(25);

        getDialogPane().setContent(newGridPane(GRID_PANE,
            List.of(newLabel(RB, "label.Name"), nameEdit),
            List.of(typeLabel, typeList),
            List.of(newLabel(RB, "label.Icon"), pictureList)
        ));
    }

    TextField getNameEdit() {
        return nameEdit;
    }

    ComboBox<RecordType> getTypeList() {
        return typeList;
    }

    ComboBox<Picture> getPictureList() {
        return pictureList;
    }

    void setTypeLabelText(String text) {
        typeLabel.setText(text);
    }

    void initLists() {
        typeList.setItems(FXCollections.observableArrayList(RecordType.values()));
        typeList.setCellFactory(p -> new CardTypeListCell());
        typeList.setButtonCell(new CardTypeListCell());
        Picture.setupComboBox(pictureList);
    }

    void setupValidator() {
        validation.registerValidator(nameEdit, (Control c, String value) ->
            ValidationResult.fromErrorIf(c, null, nameEdit.getText().isEmpty()));
        validation.initInitialDecoration();
    }
}
