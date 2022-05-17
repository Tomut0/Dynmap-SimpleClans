package net.sacredlabyrinth.phaed.dynmap.simpleclans;

import org.dynmap.markers.MarkerAPI;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;

@RunWith(MockitoJUnitRunner.class)
public class IconStorageTest {

    @Mock
    MarkerAPI markerAPI;

    @Test
    public void IsNotNull() {
        try {
            IconStorage iconStorage = new IconStorage("/images/clanhome", "clanhome.png", markerAPI);
            Assert.assertNotNull(iconStorage);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
