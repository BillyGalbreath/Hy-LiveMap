package net.pl3x.livemap.httpd;

import com.hypixel.hytale.server.core.universe.Universe;
import io.undertow.server.handlers.resource.PathResourceManager;
import io.undertow.server.handlers.resource.Resource;
import net.pl3x.livemap.configuration.Config;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FriendlyUrlPathResourceManager extends PathResourceManager {
    public static final Pattern FRIENDLY_URLS = Pattern.compile("^/(.+?)(?:/(.+?)?/?(-?\\d+)?/?(-?\\d+)?/?(-?\\d+)?(?:/(.+)?)?)?$");

    public FriendlyUrlPathResourceManager(Path webDir) {
        super(
            webDir,

            // PathResourceManager#DEFAULT_TRANSFER_MIN_SIZE
            1024L,

            Config.HTTPD_CASE_SENSITIVE,
            Config.HTTPD_FOLLOW_SYMLINKS,

            // PathResourceManager#DEFAULT_CHANGE_LISTENERS_ALLOWED
            !Boolean.getBoolean("io.undertow.disable-file-system-watcher")
        );

        // weird that this isn't exposed anywhere except the builder...
        try {
            Field eTagFunction = PathResourceManager.class.getDeclaredField("eTagFunction");
            eTagFunction.setAccessible(true);
            eTagFunction.set(this, new LastModifiedETagFunction());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @Nullable
    public Resource getResource(@Nullable String input) {
        // this is a cheap way of handling friendly urls. the server has
        // zero care about what world/renderer/zoom/coords the client
        // wants so we basically just chop out those parts from the
        // requested input and serve the requested content left after
        // all that. the client will not see this alteration, so it will
        // do the actual parsing of the world/renderer/zoom/coords from
        // the url for us.

        if (input != null && !input.isEmpty()) {
            Matcher matcher = FRIENDLY_URLS.matcher(input);
            // check for friendly url format and that world requested exists
            if (matcher.find() && Universe.get().getWorld(matcher.group(1)) != null) {
                // serve the requested destination without world, renderer,
                // zoom, and coords to let the client figure out this mess.
                input = matcher.group(6);
            }
        }

        // let the real PathResourceManager do its thing with the altered input
        return super.getResource(input == null ? "/" : input);
    }
}
