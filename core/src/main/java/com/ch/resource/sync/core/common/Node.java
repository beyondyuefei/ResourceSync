package com.ch.resource.sync.core.common;

public class Node {
    private final String ip;
    private final int port;
    private volatile boolean isHealth;

    public Node(String ip, int port) {
        this.ip = ip;
        this.port = port;
        this.isHealth = true;
    }

    public static Node of(final String uniqueKey) {
        final String[] ipAndPort = uniqueKey.split(":");
        final String ip = ipAndPort[0];
        final int port = Integer.parseInt(ipAndPort[1]);
        return new Node(ip, port);
    }

    public String getNodeUniqueKey() {
        return ip + ":" + port;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public boolean isHealth() {
        return isHealth;
    }

    public void setHealth(boolean health) {
        isHealth = health;
    }

    @Override
    public String toString() {
        return "Node{" +
                "ip='" + ip + '\'' +
                ", port=" + port +
                ", isHealth=" + isHealth +
                '}';
    }
}
