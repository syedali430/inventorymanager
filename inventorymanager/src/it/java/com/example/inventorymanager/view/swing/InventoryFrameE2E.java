package com.example.inventorymanager.view.swing;

import com.example.inventorymanager.model.Item;
import com.example.inventorymanager.repository.ItemRepository;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class InventoryFrameE2E extends AssertJSwingJUnitTestCase {

    private static MongodExecutable mongodExecutable;
    private static MongodProcess mongod;
    private static int port;

    @BeforeClass
    public static void startMongo() throws Exception {
        port = Network.getFreeServerPort();
        MongodConfig mongodConfig = MongodConfig.builder()
                .version(Version.Main.V4_4)
                .net(new Net(port, Network.localhostIsIPv6()))
                .build();
        MongodStarter starter = MongodStarter.getDefaultInstance();
        mongodExecutable = starter.prepare(mongodConfig);
        mongod = mongodExecutable.start();
    }

    @AfterClass
    public static void stopMongo() {
        if (mongod != null) {
            mongod.stop();
        }
        if (mongodExecutable != null) {
            mongodExecutable.stop();
        }
    }

    private FrameFixture window;
    private ItemRepository repo;

    @Override
    protected void onSetUp() {
        repo = new ItemRepository("localhost", port);
        InventoryFrame frame = GuiActionRunner.execute(() -> {
            InventoryFrame f = new InventoryFrame(repo);
            f.setVisible(true);
            return f;
        });
        window = new FrameFixture(robot(), frame);
    }

    @Test
    public void testAddFromUiPersistsInMongo() {
        window.textBox("nameField").setText("e2e-item");
        window.textBox("quantityField").setText("4");
        window.textBox("priceField").setText("11");
        window.textBox("descField").setText("desc");

        window.button("addButton").click();

        window.list("itemList").requireItemCount(1);
        assertThat(repo.findAll()).hasSize(1);
        Item saved = repo.findAll().get(0);
        assertThat(saved.getName()).isEqualTo("e2e-item");
    }
}


