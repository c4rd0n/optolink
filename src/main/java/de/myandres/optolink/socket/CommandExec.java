package de.myandres.optolink.socket;

import de.myandres.optolink.config.Config;
import de.myandres.optolink.entity.Channel;
import de.myandres.optolink.entity.Telegram;
import de.myandres.optolink.entity.Thing;
import de.myandres.optolink.viessmann.ViessmannHandler;
import de.myandres.optolink.viessmann.ViessmannSemaphore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;

public class CommandExec implements Runnable {

    private static Logger log = LoggerFactory.getLogger(SocketHandler.class);

    private static final String DATA_OPEN = "<data>";
    private static final String DATA_CLOSE = "</data>";
    private static final String THING_OPEN = "  <thing id=\"";
    private static final String THING_CLOSE = "  </thing>";

    private String param;
    private PrintStream out;
    private Config config;
    private ViessmannHandler viessmannHandler;

    CommandExec(String param, PrintStream out, Config config, ViessmannHandler viessmannHandler) {
        this.param = param;
        this.out = out;
        this.config = config;
        this.viessmannHandler = viessmannHandler;
    }

    public void run() {
        String command;
        String param1;
        String param2;

        log.debug("Execute Thread for: '{}'", param);
        String[] inStr = param.trim().split(" +");
        command = inStr[0];
        if (inStr.length > 1)
            param1 = inStr[1];
        else
            param1 = "";
        if (inStr.length > 2)
            param2 = inStr[2];
        else
            param2 = "";
        exec(command, param1, param2);
        log.debug("Thread for: '{}' done", param);
    }

    private void exec(String command, String param1, String param2) {

        try {
            ViessmannSemaphore.getInstance().acquire();
        } catch (InterruptedException e) {
            log.error("Viessmann is busy");
            Thread.currentThread().interrupt();
        }
        if (log.isTraceEnabled()) {
            log.trace("Queue Command: |{}|", command);
            log.trace("      param1 : |{}|", param1);
            log.trace("      param2 : |{}|", param2);
        }

        switch (command.toLowerCase()) {

            case "list":
                list();
                break;
            case "get":
                if (param2.equals(""))
                    getThing(param1);
                else
                    getThing(param1, param2);
                break;
            case "set":
                set(param1, param2);
                break;
            default:
                log.error("Unknown Client Command: {}", command);

                log.trace("Queue Command: |{}| done", command);

        }
        ViessmannSemaphore.getInstance().release();

    }

    private void set(String id, String value) {
        // Format id = <thing>:<channel>

        String[] ids = id.trim().split(":");

        if (ids.length != 2) {
            log.error("Wrong format '{}' of id", id);
            return;
        }
        Telegram telegram = config.getThing(ids[0]).getChannel(ids[1]).getTelegram();
        if (telegram != null) {
            out.println(DATA_OPEN);
            out.println(THING_OPEN + ids[0] + "\">");
            printChannel(ids[1], viessmannHandler.setValue(telegram, value.toUpperCase()));
            out.println(THING_CLOSE);
            out.println(DATA_CLOSE);
        }

    }

    private void getThing(String id) {
        log.debug("Try to get Thing for ID: {}", id);
        Thing thing = config.getThing(id);
        if (thing != null) {
            out.println(DATA_OPEN);
            out.println(THING_OPEN + thing.getId() + "\">");
            for (Channel channel : thing.getChannelMap()) {
                if (!channel.getId().startsWith("*")) {
                    printChannel(channel.getId(), viessmannHandler.getValue(channel.getTelegram()));
                }
            }
            out.println(THING_CLOSE);
            out.println(DATA_CLOSE);
        }
    }

    private void getThing(String id, String channels) {
        Channel channel;
        log.debug("Try to get Thing for ID: {} channels: {}", id, channels);
        String[] channelList = channels.split(",");
        Thing thing = config.getThing(id);
        if (thing != null) {
            out.println(DATA_OPEN);
            out.println(THING_OPEN + thing.getId() + "\">");
            for (String channelName : channelList) {
                channel = thing.getChannel(channelName);
                if (channel != null) {
                    printChannel(channel.getId(), viessmannHandler.getValue(channel.getTelegram()));
                } else {
                    log.error("Channel : {}.{} not define! ", id, channelName);
                }
            }
            out.println(THING_CLOSE);
            out.println(DATA_CLOSE);
        }
    }

    private void list() {
        log.debug("List Things for ID");
        out.println("<define>");
        for (Thing thing : config.getThingList()) {

            if ((thing != null) && !thing.getId().startsWith("*")) {

                out.println(THING_OPEN + thing.getId() + "\" type=\"" + thing.getType() + "\">");
                for (Channel channel : thing.getChannelMap()) {
                    if (!channel.getId().startsWith("*")) {
                        printChannel(channel.getId());
                    }
                }
                out.println(THING_CLOSE);

            }
        }
        out.println("</define>");

    }

    private void printChannel(String id) {
        printChannel(id, null);
    }

    private void printChannel(String id, String value) {
        out.print("    <channel id=\"" + id + "\"");
        if (value != null) {
            out.print(" value=\"" + value + "\"");
        }
        out.println("/>");
    }
}
