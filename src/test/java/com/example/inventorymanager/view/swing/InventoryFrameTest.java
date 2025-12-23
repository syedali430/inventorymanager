package com.example.inventorymanager.view.swing;

import com.example.inventorymanager.model.Item;
import com.example.inventorymanager.repository.ItemRepository;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collections;

import static org.mockito.Mockito.*;

public class InventoryFrameTest extends AssertJSwingJUnitTestCase {

    private FrameFixture window;
    private ItemRepository repository;

    @Override
    protected void onSetUp() {
        repository = Mockito.mock(ItemRepository.class);
        when(repository.findAll()).thenReturn(Collections.emptyList());

        InventoryFrame frame = GuiActionRunner.execute(() -> {
            InventoryFrame f = new InventoryFrame(repository);
            f.setVisible(true);
            return f;
        });

        window = new FrameFixture(robot(), frame);
    }

    @Test
    public void should_disable_add_button_initially() {
        window.button("addButton").requireDisabled();
    }

    @Test
    public void should_enable_add_button_when_fields_filled() {
        window.textBox("nameField").setText("testItem");
        window.textBox("quantityField").setText("5");
        window.textBox("priceField").setText("10");

        window.button("addButton").requireEnabled();
    }

    @Test
    public void should_add_item_to_list_when_add_clicked() {
        window.textBox("nameField").setText("testItem");
        window.textBox("quantityField").setText("3");
        window.textBox("priceField").setText("12");

        GuiActionRunner.execute(() -> window.button("addButton").target().doClick());

        window.list("itemList").requireItemCount(1);
        verify(repository, times(1)).save(any(Item.class));
    }
}
