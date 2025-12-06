package com.arsw.ids_ia.dto.request;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class NetworkTrafficFeaturesTest {

    @Test
    void testConstructorAndAllGettersSetters() {
        NetworkTrafficFeatures features = new NetworkTrafficFeatures();
        
        // Set all 41 properties
        features.setDuration(100);
        features.setProtocolType("tcp");
        features.setService("http");
        features.setFlag("SF");
        features.setSrcBytes(5000);
        features.setDstBytes(3000);
        features.setLand(0);
        features.setWrongFragment(0);
        features.setUrgent(0);
        features.setHot(0);
        features.setNumFailedLogins(0);
        features.setLoggedIn(1);
        features.setNumCompromised(0);
        features.setRootShell(0);
        features.setSuAttempted(0);
        features.setNumRoot(0);
        features.setNumFileCreations(0);
        features.setNumShells(0);
        features.setNumAccessFiles(0);
        features.setNumOutboundCmds(0);
        features.setIsHostLogin(0);
        features.setIsGuestLogin(0);
        features.setCount(10);
        features.setSrvCount(5);
        features.setSerrorRate(0.0);
        features.setSrvSerrorRate(0.0);
        features.setRerrorRate(0.0);
        features.setSrvRerrorRate(0.0);
        features.setSameSrvRate(1.0);
        features.setDiffSrvRate(0.0);
        features.setSrvDiffHostRate(0.0);
        features.setDstHostCount(255);
        features.setDstHostSrvCount(255);
        features.setDstHostSameSrvRate(1.0);
        features.setDstHostDiffSrvRate(0.0);
        features.setDstHostSameSrcPortRate(0.0);
        features.setDstHostSrvDiffHostRate(0.0);
        features.setDstHostSerrorRate(0.0);
        features.setDstHostSrvSerrorRate(0.0);
        features.setDstHostRerrorRate(0.0);
        features.setDstHostSrvRerrorRate(0.0);
        
        // Verify all getters
        assertEquals(100, features.getDuration());
        assertEquals("tcp", features.getProtocolType());
        assertEquals("http", features.getService());
        assertEquals("SF", features.getFlag());
        assertEquals(5000, features.getSrcBytes());
        assertEquals(3000, features.getDstBytes());
        assertEquals(0, features.getLand());
        assertEquals(0, features.getWrongFragment());
        assertEquals(0, features.getUrgent());
        assertEquals(0, features.getHot());
        assertEquals(0, features.getNumFailedLogins());
        assertEquals(1, features.getLoggedIn());
        assertEquals(0, features.getNumCompromised());
        assertEquals(0, features.getRootShell());
        assertEquals(0, features.getSuAttempted());
        assertEquals(0, features.getNumRoot());
        assertEquals(0, features.getNumFileCreations());
        assertEquals(0, features.getNumShells());
        assertEquals(0, features.getNumAccessFiles());
        assertEquals(0, features.getNumOutboundCmds());
        assertEquals(0, features.getIsHostLogin());
        assertEquals(0, features.getIsGuestLogin());
        assertEquals(10, features.getCount());
        assertEquals(5, features.getSrvCount());
        assertEquals(0.0, features.getSerrorRate());
        assertEquals(0.0, features.getSrvSerrorRate());
        assertEquals(0.0, features.getRerrorRate());
        assertEquals(0.0, features.getSrvRerrorRate());
        assertEquals(1.0, features.getSameSrvRate());
        assertEquals(0.0, features.getDiffSrvRate());
        assertEquals(0.0, features.getSrvDiffHostRate());
        assertEquals(255, features.getDstHostCount());
        assertEquals(255, features.getDstHostSrvCount());
        assertEquals(1.0, features.getDstHostSameSrvRate());
        assertEquals(0.0, features.getDstHostDiffSrvRate());
        assertEquals(0.0, features.getDstHostSameSrcPortRate());
        assertEquals(0.0, features.getDstHostSrvDiffHostRate());
        assertEquals(0.0, features.getDstHostSerrorRate());
        assertEquals(0.0, features.getDstHostSrvSerrorRate());
        assertEquals(0.0, features.getDstHostRerrorRate());
        assertEquals(0.0, features.getDstHostSrvRerrorRate());
    }

    @Test
    void testDuration() {
        NetworkTrafficFeatures features = new NetworkTrafficFeatures();
        features.setDuration(500);
        assertEquals(500, features.getDuration());
    }

    @Test
    void testProtocolType() {
        NetworkTrafficFeatures features = new NetworkTrafficFeatures();
        features.setProtocolType("udp");
        assertEquals("udp", features.getProtocolType());
    }

    @Test
    void testService() {
        NetworkTrafficFeatures features = new NetworkTrafficFeatures();
        features.setService("ftp");
        assertEquals("ftp", features.getService());
    }

    @Test
    void testFlag() {
        NetworkTrafficFeatures features = new NetworkTrafficFeatures();
        features.setFlag("REJ");
        assertEquals("REJ", features.getFlag());
    }

    @Test
    void testSrcBytes() {
        NetworkTrafficFeatures features = new NetworkTrafficFeatures();
        features.setSrcBytes(1024);
        assertEquals(1024, features.getSrcBytes());
    }

    @Test
    void testDstBytes() {
        NetworkTrafficFeatures features = new NetworkTrafficFeatures();
        features.setDstBytes(2048);
        assertEquals(2048, features.getDstBytes());
    }

    @Test
    void testCount() {
        NetworkTrafficFeatures features = new NetworkTrafficFeatures();
        features.setCount(25);
        assertEquals(25, features.getCount());
    }

    @Test
    void testSerrorRate() {
        NetworkTrafficFeatures features = new NetworkTrafficFeatures();
        features.setSerrorRate(0.5);
        assertEquals(0.5, features.getSerrorRate());
    }

    @Test
    void testDstHostCount() {
        NetworkTrafficFeatures features = new NetworkTrafficFeatures();
        features.setDstHostCount(128);
        assertEquals(128, features.getDstHostCount());
    }

    @Test
    void testLoggedIn() {
        NetworkTrafficFeatures features = new NetworkTrafficFeatures();
        features.setLoggedIn(1);
        assertEquals(1, features.getLoggedIn());
    }
}
