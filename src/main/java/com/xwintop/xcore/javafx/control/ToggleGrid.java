package com.xwintop.xcore.javafx.control;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.event.EventListenerSupport;

/**
 * 一个可以通过鼠标拖拽来选择内容的 Grid
 */
public class ToggleGrid<T> extends FlowPane {

    private double cellWidth;

    private double cellHeight;

    private List<TogglePane> togglePanes = new ArrayList<>();

    private int selectionStart = -1;

    private int selectionEnd = -1;

    private boolean mouseDragging;

    private EventListenerSupport<Runnable> selectionUpdatedListeners = EventListenerSupport.create(Runnable.class);

    public ToggleGrid(double cellWidth, double cellHeight) {
        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;
        this.setHgap(1);
        this.setVgap(1);

        this.addEventFilter(MouseDragEvent.MOUSE_DRAG_RELEASED, event -> this.onSelectionEnd());
    }

    public void addSelectionUpdatedListener(Runnable runnable) {
        this.selectionUpdatedListeners.addListener(runnable);
    }

    public void select(T... values) {
        HashSet<T> valueSet = new HashSet<>(Arrays.asList(values));
        select(valueSet::contains);
    }

    public void select(Predicate<T> predicate) {
        this.togglePanes.forEach(
            togglePane -> {
                boolean selected = predicate.test(togglePane.value);
                togglePane.setSelected(selected);
            }
        );
        selectionUpdated();
    }

    public void clearSelection() {
        this.togglePanes.stream()
            .filter(TogglePane::isSelected)
            .forEach(togglePane -> togglePane.setSelected(false));
        selectionUpdated();
    }

    public List<String> getSelectedValues() {
        return this.togglePanes.stream()
            .filter(TogglePane::isSelected)
            .map(TogglePane::getText)
            .collect(Collectors.toList());
    }

    public void addCell(T t) {
        TogglePane togglePane = new TogglePane(cellWidth, cellHeight, t, this.togglePanes.size());
        this.getChildren().add(togglePane);
        this.togglePanes.add(togglePane);
    }

    private void onSelectionStart(TogglePane togglePane) {
        this.selectionStart = togglePane.index;
        this.selectionEnd = this.selectionStart;
        this.mouseDragging = true;
        updateSelectionByRange();
    }

    private void onSelectionDrag(TogglePane togglePane) {
        if (!this.mouseDragging) {
            return;
        }
        this.selectionEnd = togglePane.index;
        updateSelectionByRange();
    }

    private void onSelectionEnd() {
        this.mouseDragging = false;
    }

    private void updateSelectionByRange() {
        int start = Math.min(this.selectionStart, this.selectionEnd);
        int end = Math.max(this.selectionStart, this.selectionEnd);
        this.togglePanes.forEach(togglePane -> {
            if (togglePane.index >= start && togglePane.index <= end) {
                togglePane.setSelected(true);
            } else {
                togglePane.setSelected(false);
            }
        });
        selectionUpdated();
    }

    private void selectionUpdated() {
        this.selectionUpdatedListeners.fire().run();
    }

    public class TogglePane extends VBox {

        private final Label label = new Label();

        private int index;

        private T value;

        private BooleanProperty selected = new SimpleBooleanProperty();

        public TogglePane(double width, double height, T value, int index) {
            this.setAlignment(Pos.CENTER);
            this.setPrefSize(width, height);
            this.setMinSize(width, height);
            this.setMaxSize(width, height);
            setUnselected();

            this.getChildren().add(label);
            this.label.setText(String.valueOf(value));
            this.index = index;
            this.value = value;

            this.selected.addListener((observable, oldValue, selected) -> {
                if (selected) {
                    setSelected();
                } else {
                    setUnselected();
                }
            });

            this.addEventHandler(MouseDragEvent.DRAG_DETECTED, event -> this.startFullDrag());
            this.addEventHandler(MouseDragEvent.MOUSE_PRESSED, event -> onSelectionStart(this));
            this.addEventHandler(MouseDragEvent.MOUSE_DRAG_OVER, event -> onSelectionDrag(this));
        }

        public void setSelected(boolean selected) {
            this.selected.set(selected);
        }

        public boolean isSelected() {
            return selected.get();
        }

        public BooleanProperty selectedProperty() {
            return selected;
        }

        private void setUnselected() {
            this.setStyle("-fx-background-color: #DDDDDD");
            this.label.setStyle("-fx-text-fill: #000000");
        }

        private void setSelected() {
            this.setStyle("-fx-background-color: #444444;");
            this.label.setStyle("-fx-text-fill: #FFFFFF");
        }

        public String getText() {
            return this.label.getText();
        }
    }
}
