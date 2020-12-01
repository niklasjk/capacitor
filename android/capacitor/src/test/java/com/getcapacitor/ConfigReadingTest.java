package com.getcapacitor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import androidx.core.app.ActivityCompat;
import java.io.IOException;
import java.io.InputStream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

public class ConfigReadingTest {

    private static final String FLAT_TEST = "configs/flat.json";
    private static final String BAD_TEST = "configs/bad.json";
    private static final String HIERARCHY_TEST = "configs/hierarchy.json";
    private static final String NONJSON_TEST = "configs/nonjson.json";
    private static final String SERVER_TEST = "configs/server.json";

    Activity context = Mockito.mock(Activity.class);
    AssetManager assetManager = Mockito.mock(AssetManager.class);

    private InputStream getTestInputStream(String testPath) {
        return this.getClass().getClassLoader().getResourceAsStream(testPath);
    }

    @Before
    public void before() {
        when(context.getAssets()).thenReturn(assetManager);
    }

    @Test
    public void bad() {
        try {
            when(assetManager.open("capacitor.config.json")).thenReturn(getTestInputStream(BAD_TEST));

            CapConfig config = new CapConfig(context, false);
            assertEquals("not a real domain", config.getServerUrl());
            assertNull(config.getBackgroundColor());
            assertFalse(config.hideLogs());
        } catch (IOException e) {
            fail();
        }
    }

    @Test
    public void flat() {
        try {
            when(assetManager.open("capacitor.config.json")).thenReturn(getTestInputStream(FLAT_TEST));

            CapConfig config = new CapConfig(context, false);
            assertEquals("level 1 override", config.getOverriddenUserAgentString());
            assertEquals("level 1 append", config.getAppendedUserAgentString());
            assertEquals("#ffffff", config.getBackgroundColor());
            assertTrue(config.hideLogs());
            assertEquals(1, config.getPluginInt("SplashScreen", "launchShowDuration", 0));
        } catch (IOException e) {
            fail();
        }
    }

    @Test
    public void hierarchy() {
        try {
            when(assetManager.open("capacitor.config.json")).thenReturn(getTestInputStream(HIERARCHY_TEST));

            CapConfig config = new CapConfig(context, false);
            assertEquals("level 2 override", config.getOverriddenUserAgentString());
            assertEquals("level 2 append", config.getAppendedUserAgentString());
            assertEquals("#000000", config.getBackgroundColor());
            assertFalse(config.hideLogs());
        } catch (IOException e) {
            fail();
        }
    }

    @Test
    public void nonJSON() {
        try {
            final String errText = "Unable to parse capacitor.config.json. Make sure it's valid json";

            when(assetManager.open("capacitor.config.json")).thenReturn(getTestInputStream(NONJSON_TEST));

            try (MockedStatic<Logger> logger = mockStatic(Logger.class)) {
                CapConfig config = new CapConfig(context, false);
                logger.verify(times(1), () -> Logger.error(eq(errText), any()));
            }
        } catch (IOException e) {
            fail();
        }
    }

    @Test
    public void server() {
        try {
            when(assetManager.open("capacitor.config.json")).thenReturn(getTestInputStream(SERVER_TEST));

            CapConfig config = new CapConfig(context, false);
            assertEquals("myhost", config.getHostname());
            assertEquals("http://192.168.100.1:2057", config.getServerUrl());
            assertEquals("override", config.getAndroidScheme());
        } catch (IOException e) {
            fail();
        }
    }
}
