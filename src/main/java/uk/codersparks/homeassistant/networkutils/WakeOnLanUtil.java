package uk.codersparks.homeassistant.networkutils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides functionality to use wake on lan to power on a network device
 *
 * Use {@link Builder} to create the util by calling {@link WakeOnLanUtil#builder()} and
 * setting mac address {@link Builder#macAddress(String)} then calling {@link Builder#build()}
 *
 * You can then send the WakeOnLan packet using {@link WakeOnLanUtil#sendWakeOnLan(DatagramSocket)}
 */
public class WakeOnLanUtil {

    private final String macAddress;
    private final String ipAddress;
    private final int port;

    /**
     * Private constructor for WakeOnLan - use {@link Builder} instead
     * @param macAddress The Mac Address to send WOL packet to
     * @param ipAddress The ip address to broadcast to
     * @param port The port to use
     */
    private WakeOnLanUtil(String macAddress, String ipAddress, int port) {
        this.macAddress = macAddress;
        this.ipAddress = ipAddress;
        this.port = port;
    }

    /**
     * Send the wake on lan packet to the configured details
     * @param socket The {@link DatagramSocket} to use to send the details
     * @throws IOException
     */
    public void sendWakeOnLan(DatagramSocket socket) throws IOException {

        final String[] hex = this.macAddress.split("(\\:)");

        // convert to base 16 bytes
        final byte[] macBytes = new byte[6];
        for(int i=0; i<6; i++) {
            macBytes[i] = (byte) Integer.parseInt(hex[i], 16);
        }

        final byte[] bytes = new byte[102];

        // fill first 6 bytes
        for(int i=0; i<6; i++) {
            bytes[i] = (byte) 0xff;
        }
        // fill remaining bytes with target MAC
        for(int i=6; i<bytes.length; i+=macBytes.length) {
            System.arraycopy(macBytes, 0, bytes, i, macBytes.length);
        }

        // create socket to IP
        final InetAddress address = InetAddress.getByName(ipAddress);
        final DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, port);
        socket.send(packet);
        socket.close();
    }

    /**
     * Create builder to create configured {@link WakeOnLanUtil}
     * @return Instance of {@link WakeOnLanUtil}
     */
    public static Builder builder() {
        return Builder.create();
    }

    public static class Builder {

        private String macAddress = null;
        private String broadcastIpAddress = "255.255.255.255";
        private int port = 9;
        private final String seperator = ":";

        private Builder() {}

        /**
         * Create the builder
         * @return Instance of {@link Builder}
         */
        public static Builder create() {
            return new Builder();
        }

        /**
         * Set the mac address - required
         * @param macAddress Mac address string (format xx:xx:xx:xx:xx)
         * @return the instance of the {@link Builder}
         */
        public Builder macAddress(String macAddress) {
            String mac = this.cleanMac(macAddress);
            this.macAddress = mac;
            return this;
        }

        /**
         * Set the port to send message to - Default value: 9
         * @param port Value to use for port
         * @return the instance of the {@link Builder}
         */
        public Builder port(int port) {
            this.port = port;
            return this;
        }

        /**
         * Set the broadcast IP Address - Default value: 255.255.255.255
         * @param ipAddress The ip address (format x.x.x.x)
         * @return
         */
        public Builder broadcastIpAddress(String ipAddress) {
            this.broadcastIpAddress = ipAddress;
            return this;
        }

        /**
         * Create the instance of {@link WakeOnLanUtil}
         * @return instance of configured {@link WakeOnLanUtil}
         */
        public WakeOnLanUtil build() {

            if(macAddress == null) {
                throw new IllegalStateException("Mac address has not been configured");
            }

            return new WakeOnLanUtil(this.macAddress, this.broadcastIpAddress, this.port);
        }

        /**
         * Ensures the mac is formatted correctly
         * @param macAddress The mac address to format
         * @return correctly formatted mac address
         */
        private String cleanMac(String macAddress) {
            final String[] hex = validateMac(macAddress);

            StringBuffer sb = new StringBuffer();
            boolean isMixedCase = false;

            // check for mixed case
            for(int i=0; i<6; i++) {
                sb.append(hex[i]);
            }
            String testMac = sb.toString();
            if((testMac.toLowerCase().equals(testMac) == false) && (testMac.toUpperCase().equals(testMac) == false)) {
                isMixedCase = true;
            }

            sb = new StringBuffer();
            for(int i=0; i<6; i++) {
                // convert mixed case to lower
                if(isMixedCase == true) {
                    sb.append(hex[i].toLowerCase());
                }else{
                    sb.append(hex[i]);
                }
                if(i < 5) {
                    sb.append(seperator);
                }
            }
            return sb.toString();
        }

        /**
         * Validate the mac address is of the correct format
         * @param macAddress Mac Address to validate
         * @return Validated mac address (as string array)
         * @throws IllegalArgumentException
         */
        private String[] validateMac(String macAddress) throws IllegalArgumentException
        {
            // error handle semi colons
            macAddress = macAddress.replace(";", ":");

            // regexp pattern match a valid MAC address
            final Pattern pat = Pattern.compile("^((([0-9a-fA-F]){2}[-:]){5}([0-9a-fA-F]){2})$");
            final Matcher m = pat.matcher(macAddress);

            if(m.find()) {
                String result = m.group();
                return result.split("(\\:|\\-)");
            }else{
                throw new IllegalArgumentException("Invalid MAC address");
            }
        }


    }
}
