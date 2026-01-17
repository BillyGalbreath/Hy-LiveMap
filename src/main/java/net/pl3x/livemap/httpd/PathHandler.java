package net.pl3x.livemap.httpd;

import io.undertow.UndertowLogger;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.resource.ResourceHandler;
import io.undertow.server.handlers.resource.ResourceManager;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;
import net.pl3x.livemap.util.FileUtil;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;

public class PathHandler extends io.undertow.server.handlers.PathHandler {
    private final ResourceHandler handler;

    public PathHandler(@NotNull ResourceManager manager, @NotNull Path webDir) {
        this.handler = new ResourceHandler(manager, new ErrorHandler(webDir));
    }

    @Override
    public void handleRequest(@NotNull HttpServerExchange exchange) throws Exception {
        String url = exchange.getRelativePath();
        if (url.contains("/tiles/")) {
            // do not cache anything in the tiles directory (includes json files)
            exchange.getResponseHeaders().put(Headers.CACHE_CONTROL, "max-age=0, must-revalidate, no-cache");
        }
        this.handler.handleRequest(exchange);
    }

    private static class ErrorHandler implements HttpHandler {
        private final Path webDir;

        public ErrorHandler(Path webDir) {
            this.webDir = webDir;
        }

        @Override
        public void handleRequest(@NotNull HttpServerExchange exchange) {
            String url = exchange.getRelativePath();
            if (url.contains("/tiles/") && url.endsWith(".png")) {
                // do not 404 on missing tiles (keeps client log clean)
                exchange.setStatusCode(StatusCodes.OK);
                return;
            }

            // set the 404 status
            exchange.setStatusCode(StatusCodes.NOT_FOUND);

            // check for a 404.html file
            Path file = this.webDir.resolve("404.html");
            if (Files.exists(file)) {
                // serve the 404.html file
                exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/html");
                exchange.getResponseSender().send(FileUtil.readString(file));
            }

            // standard undertow logging
            if (UndertowLogger.PREDICATE_LOGGER.isDebugEnabled()) {
                UndertowLogger.PREDICATE_LOGGER.debugf("Response code set to [%s] for %s.", StatusCodes.NOT_FOUND, exchange);
            }
        }
    }
}
