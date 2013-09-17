package net.wuehrer.sms_sender.plugin;

public interface SenderPlugin{
    public void sendMessage(String message, String recipient, String sender);
}
