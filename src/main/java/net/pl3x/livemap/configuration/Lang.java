package net.pl3x.livemap.configuration;

import net.pl3x.livemap.LiveMap;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public final class Lang extends AbstractConfig {
    @Key("httpd.started.success")
    public static String HTTPD_STARTED = "<green>Internal webserver running on <yellow><bind></yellow> port(s) <yellow><port></yellow>";
    @Key("httpd.started.error")
    public static String HTTPD_START_ERROR = "<red>Internal webserver could not start";
    @Key("httpd.stopped.success")
    public static String HTTPD_STOPPED = "<green>Internal webserver stopped";
    @Key("httpd.stopped.error")
    public static String HTTPD_STOP_ERROR = "<red>An error occurred with the internal webserver";
    @Key("httpd.disabled")
    public static String HTTPD_DISABLED = "<green>Internal webserver is disabled";

    private Lang(@NotNull Path file) {
        super(file);
    }

    public static void reload() {
        //FileUtil.extractDir("/lang/", this.file.getParent(), false);

        new Lang(LiveMap.instance().getDataDirectory().resolve("lang").resolve(Config.LANGUAGE_FILE));
    }
}
