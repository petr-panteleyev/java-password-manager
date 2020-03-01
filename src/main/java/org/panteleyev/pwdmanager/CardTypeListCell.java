package org.panteleyev.pwdmanager;

/*
 * Copyright (c) Petr Panteleyev. All rights reserved.
 * Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

import javafx.scene.control.ListCell;
import javafx.scene.image.ImageView;

final class CardTypeListCell extends ListCell<RecordType> {
    @Override
    public void updateItem(RecordType item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            setText(item.getName());
            setGraphic(new ImageView(item.getImage()));
        }
    }
}
