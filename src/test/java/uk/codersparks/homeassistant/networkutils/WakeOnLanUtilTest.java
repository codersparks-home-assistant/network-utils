package uk.codersparks.homeassistant.networkutils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test class for {@link WakeOnLanUtil}
 */
@ExtendWith(MockitoExtension.class)
class WakeOnLanUtilTest {

    private String validMacAddress = "AB:12:32:dF:2A:3B";
    private String invalidMacAddressTooShort = "AB:12:32:dF:2A";
    private String invalidMacAddressTooLong = "AB:12:32:dF:2A:3B:A1";
    private String invalidMacAddressWrongChar = "AH:12:32:dF:2A:3B";

    @Mock
    private DatagramSocket datagramSocketMock;

    @Captor
    private ArgumentCaptor<DatagramPacket> datagramPacketCaptor;

    @Test
    public void test_validMacAddress() throws IOException {

        WakeOnLanUtil.Builder builder = WakeOnLanUtil.builder();

        WakeOnLanUtil wakeOnLanUtil = builder.macAddress(validMacAddress).build();

        wakeOnLanUtil.sendWakeOnLan(datagramSocketMock);

        verify(datagramSocketMock).send(datagramPacketCaptor.capture());
        verify(datagramSocketMock).close();
        verifyNoMoreInteractions(datagramSocketMock);

        DatagramPacket packet = datagramPacketCaptor.getValue();

        InetAddress address = packet.getAddress();
        assertEquals("255.255.255.255", address.getHostAddress());
        assertEquals(9, packet.getPort());
        byte[] data = packet.getData();

        for(int i = 0; i < 6; i++) {
            assertEquals((byte)0xFF, data[i]);
        }

        final String[] hex = validMacAddress.split("\\:");
        final byte[] expectedBytes = new byte[6];
        for(int i=0; i<6; i++) {
            expectedBytes[i] = (byte) Integer.parseInt(hex[i], 16);
        }

        for(int i=6; i < data.length; i+=expectedBytes.length) {
            for(int j=0; j < 6; j++) {
                assertEquals(expectedBytes[j], data[i+j]);
            }
        }

    }

    @Test
    public void test_validMacAddressCustomised() throws IOException {

        WakeOnLanUtil.Builder builder = WakeOnLanUtil.builder();

        String ipAddress = "192.168.0.2";
        int port = 99;

        WakeOnLanUtil wakeOnLanUtil = builder
                .macAddress(validMacAddress)
                .broadcastIpAddress(ipAddress)
                .port(port)
                .build();

        wakeOnLanUtil.sendWakeOnLan(datagramSocketMock);

        verify(datagramSocketMock).send(datagramPacketCaptor.capture());
        verify(datagramSocketMock).close();
        verifyNoMoreInteractions(datagramSocketMock);

        DatagramPacket packet = datagramPacketCaptor.getValue();

        InetAddress address = packet.getAddress();
        assertEquals(ipAddress, address.getHostAddress());
        assertEquals(port, packet.getPort());
        byte[] data = packet.getData();

        for(int i = 0; i < 6; i++) {
            assertEquals((byte)0xFF, data[i]);
        }

        final String[] hex = validMacAddress.split("\\:");
        final byte[] expectedBytes = new byte[6];
        for(int i=0; i<6; i++) {
            expectedBytes[i] = (byte) Integer.parseInt(hex[i], 16);
        }

        for(int i=6; i < data.length; i+=expectedBytes.length) {
            for(int j=0; j < 6; j++) {
                assertEquals(expectedBytes[j], data[i+j]);
            }
        }

    }

    @Test
    public void test_invalidMacNotSet() {

        try {
            WakeOnLanUtil.Builder builder = WakeOnLanUtil.builder();

            WakeOnLanUtil wakeOnLanUtil = builder.build();

            fail("Illegal State exception expected but not thrown");

        } catch (IllegalStateException e) {
            verifyNoInteractions(datagramSocketMock);
        }

    }

    @Test
    public void test_invalidMacTooLong() {

        try {
            WakeOnLanUtil.Builder builder = WakeOnLanUtil.builder();

            WakeOnLanUtil wakeOnLanUtil = builder.macAddress(invalidMacAddressTooLong).build();

            fail("Illegal Argument exception expected but not thrown");

        } catch (IllegalArgumentException e) {
            verifyNoInteractions(datagramSocketMock);
        }

    }

    @Test
    public void test_invalidMacTooShort() {

        try {
            WakeOnLanUtil.Builder builder = WakeOnLanUtil.builder();

            WakeOnLanUtil wakeOnLanUtil = builder.macAddress(invalidMacAddressTooShort).build();

            fail("Illegal Argument exception expected but not thrown");

        } catch (IllegalArgumentException e) {
            verifyNoInteractions(datagramSocketMock);
        }

    }

    @Test
    public void test_invalidMacIncorrectChar() {

        try {
            WakeOnLanUtil.Builder builder = WakeOnLanUtil.builder();

            WakeOnLanUtil wakeOnLanUtil = builder.macAddress(invalidMacAddressWrongChar).build();

            fail("Illegal Argument exception expected but not thrown");

        } catch (IllegalArgumentException e) {
            verifyNoInteractions(datagramSocketMock);
        }

    }
}