package net.pl3x.livemap.httpd;

import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.server.handlers.resource.ResourceManager;
import net.pl3x.livemap.LiveMap;
import net.pl3x.livemap.Logger;
import net.pl3x.livemap.configuration.Config;
import net.pl3x.livemap.configuration.Lang;

import java.io.IOException;
import java.nio.file.Path;

public class HttpdServer {
    private Undertow server;

    public void start() {
        // stop any running servers
        stop();

        if (!Config.HTTPD_HTTP_ENABLED) {
            Logger.info(Lang.HTTPD_DISABLED);
            return;
        }

        Path webDir = Path.of(Config.WEB_DIR);
        if (!webDir.isAbsolute()) {
            webDir = LiveMap.instance().getDataDirectory().resolve(webDir);
        }

        //LogFilter.HIDE_UNDERTOW_LOGS = true;

        try (ResourceManager resourceManager = new FriendlyUrlPathResourceManager(webDir)) {

            Undertow.Builder builder = Undertow.builder();
            String ports = "";
            if (Config.HTTPD_HTTP_ENABLED) {
                ports += Config.HTTPD_HTTP_PORT;
                builder.addHttpListener(Config.HTTPD_HTTP_PORT, Config.HTTPD_HTTP_BIND);
            }
            if (Config.HTTPD_HTTPS_ENABLED) {
                ports += (!ports.isBlank() ? " and " : "") + Config.HTTPD_HTTPS_PORT;
                builder.addHttpsListener(Config.HTTPD_HTTPS_PORT, Config.HTTPD_HTTPS_BIND, SSLContextBuilder.build());
            }
            this.server = builder
                .setServerOption(UndertowOptions.ENABLE_HTTP2, true)
                .setHandler(new PathHandler(resourceManager, webDir))
                .build();
            this.server.start();

            Logger.info(Lang.HTTPD_STARTED
                .replace("<bind>", Config.HTTPD_HTTP_BIND)
                .replace("<port>", ports)
            );

        } catch (IOException e) {
            this.server = null;
            Logger.severe(Lang.HTTPD_START_ERROR, e);
        }

        //LogFilter.HIDE_UNDERTOW_LOGS = false;
    }

    public void stop() {
        if (this.server == null) {
            Logger.warning(Lang.HTTPD_STOP_ERROR);
            return;
        }

        //LogFilter.HIDE_UNDERTOW_LOGS = true;
        this.server.stop();
        //LogFilter.HIDE_UNDERTOW_LOGS = false;

        this.server = null;
        Logger.info(Lang.HTTPD_STOPPED);
    }
}
