package com.example.inventorymanager.view.swing;

import com.example.inventorymanager.model.Item;
import com.example.inventorymanager.repository.ItemRepository;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.DialogFixture;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
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
    public void testAddButtonInitiallyDisabled() {
        window.button("addButton").requireDisabled();
    }

    @Test
    public void testAddButtonEnablesWhenFieldsFilled() {
        window.textBox("nameField").setText("testItem");
        window.textBox("quantityField").setText("5");
        window.textBox("priceField").setText("10");
        window.button("addButton").requireEnabled();
    }

    @Test
    public void testAddButtonStaysDisabledWithOnlyWhitespace() {
        window.textBox("nameField").setText("   ");
        window.textBox("quantityField").setText("  ");
        window.textBox("priceField").setText("  ");
        window.button("addButton").requireDisabled();
    }

    @Test
    public void testAddButtonDisablesWhenFieldCleared() {
        window.textBox("nameField").setText("abc");
        window.textBox("quantityField").setText("1");
        window.textBox("priceField").setText("1");
        window.button("addButton").requireEnabled();
        window.textBox("priceField").setText("");
        window.button("addButton").requireDisabled();
    }

    @Test
    public void testAddItemDelegatesToRepositoryAndUpdatesList() {
        window.textBox("nameField").setText("testItem");
        window.textBox("quantityField").setText("3");
        window.textBox("priceField").setText("12");
        GuiActionRunner.execute(() -> window.button("addButton").target().doClick());
        window.list("itemList").requireItemCount(1);
        verify(repository, times(1)).save(any(Item.class));
    }

    @Test
    public void testAddItemSavesWithGeneratedId() {
        window.textBox("nameField").setText("widget");
        window.textBox("quantityField").setText("7");
        window.textBox("priceField").setText("15");
        GuiActionRunner.execute(() -> window.button("addButton").target().doClick());
        ArgumentCaptor<Item> captor = ArgumentCaptor.forClass(Item.class);
        verify(repository).save(captor.capture());
        Item saved = captor.getValue();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("widget");
    }

    @Test
    public void testAddRejectsNonNumericQuantityOrPrice() {
        window.textBox("nameField").setText("bad");
        window.textBox("quantityField").setText("abc");
        window.textBox("priceField").setText("xyz");
        window.button("addButton").click();
        DialogFixture dialog = window.dialog();
        dialog.requireVisible();
        dialog.button(JButtonMatcher.withText("OK")).click();
        verify(repository, never()).save(any(Item.class));
        window.list("itemList").requireItemCount(0);
    }

    @Test
    public void testAddButtonStaysDisabledWhenNameMissing() {
        window.textBox("quantityField").setText("2");
        window.textBox("priceField").setText("5");
        window.textBox("nameField").setText("");
        window.button("addButton").requireDisabled();
    }

    @Test
    public void testAddButtonStaysDisabledWhenQuantityMissing() {
        window.textBox("nameField").setText("item");
        window.textBox("priceField").setText("5");
        window.textBox("quantityField").setText("");
        window.button("addButton").requireDisabled();
    }

    @Test
    public void testAddButtonStaysDisabledWhenPriceMissing() {
        window.textBox("nameField").setText("item");
        window.textBox("quantityField").setText("2");
        window.textBox("priceField").setText("");
        window.button("addButton").requireDisabled();
    }

    @Test
    public void testAddClearsFieldsAndDisablesButton() {
        window.textBox("nameField").setText("item");
        window.textBox("quantityField").setText("2");
        window.textBox("priceField").setText("5");
        GuiActionRunner.execute(() -> window.button("addButton").target().doClick());
        window.textBox("nameField").requireText("");
        window.textBox("quantityField").requireText("");
        window.textBox("priceField").requireText("");
        window.button("addButton").requireDisabled();
    }

    @Test
    public void testAddThenAddAnotherKeepsListGrowing() {
        window.textBox("nameField").setText("first");
        window.textBox("quantityField").setText("1");
        window.textBox("priceField").setText("1");
        GuiActionRunner.execute(() -> window.button("addButton").target().doClick());
        window.textBox("nameField").setText("second");
        window.textBox("quantityField").setText("2");
        window.textBox("priceField").setText("2");
        GuiActionRunner.execute(() -> window.button("addButton").target().doClick());
        window.list("itemList").requireItemCount(2);
    }

    @Test
    public void testExistingItemsShownOnStartup() {
        Item existing = new Item("1", "preloaded", 1, 1.0, "desc");
        when(repository.findAll()).thenReturn(Collections.singletonList(existing));
        InventoryFrame frame = GuiActionRunner.execute(() -> {
            InventoryFrame f = new InventoryFrame(repository);
            f.setVisible(true);
            return f;
        });
        FrameFixture local = new FrameFixture(robot(), frame);
        local.list("itemList").requireItemCount(1);
        local.cleanUp();
    }

    @Test
    public void testExistingMultipleItemsShownOnStartup() {
        when(repository.findAll()).thenReturn(Arrays.asList(
                new Item("1", "one", 1, 1.0, "a"),
                new Item("2", "two", 2, 2.0, "b")));
        InventoryFrame frame = GuiActionRunner.execute(() -> {
            InventoryFrame f = new InventoryFrame(repository);
            f.setVisible(true);
            return f;
        });
        FrameFixture local = new FrameFixture(robot(), frame);
        local.list("itemList").requireItemCount(2);
        local.cleanUp();
    }

    @Test
    public void testListSelectionPopulatesFields() {
        Item existing = new Item("1", "pre", 3, 4.0, "d");
        when(repository.findAll()).thenReturn(Collections.singletonList(existing));
        InventoryFrame frame = GuiActionRunner.execute(() -> {
            InventoryFrame f = new InventoryFrame(repository);
            f.setVisible(true);
            return f;
        });
        FrameFixture local = new FrameFixture(robot(), frame);
        local.list("itemList").requireItemCount(1);
        local.list("itemList").selectItem(0);
        local.robot().waitForIdle();
        local.textBox("nameField").requireText("pre");
        local.textBox("quantityField").requireText("3");
        local.textBox("priceField").requireText("4.0");
        local.cleanUp();
    }

    @Test
    public void testSelectionEnablesDeleteAndUpdateButtons() {
        Item existing = new Item("1", "pre", 1, 1.0, "d");
        when(repository.findAll()).thenReturn(Collections.singletonList(existing));
        InventoryFrame frame = GuiActionRunner.execute(() -> {
            InventoryFrame f = new InventoryFrame(repository);
            f.setVisible(true);
            return f;
        });
        FrameFixture local = new FrameFixture(robot(), frame);
        local.list("itemList").selectItem(0);
        local.button("deleteButton").requireEnabled();
        local.button("updateButton").requireEnabled();
        local.cleanUp();
    }

    @Test
    public void testClearSelectionDisablesDeleteAndUpdateButtons() {
        Item existing = new Item("1", "pre", 1, 1.0, "d");
        when(repository.findAll()).thenReturn(Collections.singletonList(existing));
        InventoryFrame frame = GuiActionRunner.execute(() -> {
            InventoryFrame f = new InventoryFrame(repository);
            f.setVisible(true);
            return f;
        });
        FrameFixture local = new FrameFixture(robot(), frame);
        local.list("itemList").selectItem(0);
        local.list("itemList").clearSelection();
        local.robot().waitForIdle();
        assertThat(local.list("itemList").selection()).isEmpty();
        local.button("deleteButton").requireDisabled();
        local.button("updateButton").requireDisabled();
        local.cleanUp();
    }

    @Test
    public void testDeleteItemRemovesFromListAndCallsRepository() {
        Item existing = new Item("x1", "preloaded", 1, 1.0, "desc");
        when(repository.findAll()).thenReturn(Collections.singletonList(existing));
        InventoryFrame frame = GuiActionRunner.execute(() -> {
            InventoryFrame f = new InventoryFrame(repository);
            f.setVisible(true);
            return f;
        });
        FrameFixture local = new FrameFixture(robot(), frame);
        local.list("itemList").selectItem(0);
        local.button("deleteButton").click();
        local.dialog().button(JButtonMatcher.withText("Yes")).click();
        local.list("itemList").requireItemCount(0);
        verify(repository).delete("x1");
        local.cleanUp();
    }

    @Test
    public void testDeleteCancelledDoesNotCallRepository() {
        Item existing = new Item("x1", "pre", 1, 1.0, "desc");
        when(repository.findAll()).thenReturn(Collections.singletonList(existing));
        InventoryFrame frame = GuiActionRunner.execute(() -> {
            InventoryFrame f = new InventoryFrame(repository);
            f.setVisible(true);
            return f;
        });
        FrameFixture local = new FrameFixture(robot(), frame);
        local.list("itemList").selectItem(0);
        local.button("deleteButton").click();
        local.dialog().button(JButtonMatcher.withText("No")).click();
        verify(repository, never()).delete(anyString());
        local.list("itemList").requireItemCount(1);
        local.cleanUp();
    }

    @Test
    public void testDeleteWithoutSelectionShowsWarningDialog() {
        window.button("deleteButton").requireDisabled();
        verify(repository, never()).delete(anyString());
    }

    @Test
    public void testUpdateWithoutSelectionShowsWarningDialog() {
        window.button("updateButton").requireDisabled();
        verify(repository, never()).update(any(Item.class));
    }

    @Test
    public void testAddButtonDisabledWhenItemSelected() {
        Item existing = new Item("1", "pre", 1, 1.0, "d");
        when(repository.findAll()).thenReturn(Collections.singletonList(existing));
        InventoryFrame frame = GuiActionRunner.execute(() -> {
            InventoryFrame f = new InventoryFrame(repository);
            f.setVisible(true);
            return f;
        });
        FrameFixture local = new FrameFixture(robot(), frame);
        local.list("itemList").selectItem(0);
        local.button("addButton").requireDisabled();
        local.cleanUp();
    }

    @Test
    public void testDeleteClearsDetailFields() {
        Item existing = new Item("x1", "pre", 1, 1.0, "desc");
        when(repository.findAll()).thenReturn(Collections.singletonList(existing));
        InventoryFrame frame = GuiActionRunner.execute(() -> {
            InventoryFrame f = new InventoryFrame(repository);
            f.setVisible(true);
            return f;
        });
        FrameFixture local = new FrameFixture(robot(), frame);
        local.list("itemList").selectItem(0);
        local.button("deleteButton").click();
        local.dialog().button(JButtonMatcher.withText("Yes")).click();
        local.textBox("nameField").requireText("");
        local.textBox("quantityField").requireText("");
        local.textBox("priceField").requireText("");
        local.cleanUp();
    }

    @Test
    public void testListOrderMatchesRepositoryOrder() {
        Item a = new Item("1", "A", 1, 1.0, "a");
        Item b = new Item("2", "B", 2, 2.0, "b");
        when(repository.findAll()).thenReturn(Arrays.asList(a, b));
        InventoryFrame frame = GuiActionRunner.execute(() -> {
            InventoryFrame f = new InventoryFrame(repository);
            f.setVisible(true);
            return f;
        });
        FrameFixture local = new FrameFixture(robot(), frame);
        String[] contents = local.list("itemList").contents();
        assertThat(contents[0]).contains("A");
        assertThat(contents[1]).contains("B");
        local.cleanUp();
    }
}


