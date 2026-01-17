package net.pl3x.livemap.configuration;

import net.pl3x.livemap.LiveMap;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public final class Config extends AbstractConfig {
    @Key("settings.language-file")
    @Comment("The language file to use from the lang folder.")
    public static String LANGUAGE_FILE = "en_us.json";

    @Key("settings.web-directory.path")
    @Comment("""
            The directory that houses the website and world tiles.
            Relative paths are from LiveMap's plugin directory,
            but absolute paths are supported, too.""")
    public static String WEB_DIR = "web";

    @Key("settings.internal-webserver.follow-symlinks")
    @Comment("""
        Allows the built-in web server to follow symlinks.
        It is generally advised against enabling this,
        for security reasons. But you do you, boo boo.""")
    public static boolean HTTPD_FOLLOW_SYMLINKS = false;
    @Key("settings.internal-webserver.case-sensitive")
    @Comment("""
        Whether the web server should be case sensitive when serving files.""")
    public static boolean HTTPD_CASE_SENSITIVE = true;

    @Key("settings.internal-webserver.http.enabled")
    @Comment("""
        Enable the built-in web server for regular http.""")
    public static boolean HTTPD_HTTP_ENABLED = true;
    @Key("settings.internal-webserver.http.bind")
    @Comment("""
        The interface the built-in web server should bind to for http requests.
        If you don't understand what this is leave it set to 0.0.0.0""")
    public static String HTTPD_HTTP_BIND = "0.0.0.0";
    @Key("settings.internal-webserver.http.port")
    @Comment("""
        The port the built-in web server listens to for http.
        Make sure the port is allocated if using a panel like Pterodactyl.""")
    public static int HTTPD_HTTP_PORT = 8080;


    @Key("settings.internal-webserver.https.enabled")
    @Comment("""
        Enable the built-in web server for secure https.""")
    public static boolean HTTPD_HTTPS_ENABLED = false;
    @Key("settings.internal-webserver.https.bind")
    @Comment("""
        The interface the built-in web server should bind to for https requests.
        If you don't understand what this is leave it set to 0.0.0.0""")
    public static String HTTPD_HTTPS_BIND = "0.0.0.0";
    @Key("settings.internal-webserver.https.port")
    @Comment("""
        The port the built-in web server listens to for https.
        Make sure the port is allocated if using a panel like Pterodactyl.""")
    public static int HTTPD_HTTPS_PORT = 8081;
    @Key("settings.internal-webserver.https.algorithm")
    @Comment("""
        https://docs.oracle.com/en/java/javase/21/docs/specs/security/standard-names.html#sslcontext-algorithms""")
    public static String HTTPD_HTTPS_ALGORITHM = "TLS";
    @Key("settings.internal-webserver.https.keystore.file")
    @Comment("""
        Path to your keystore file. Can be relative or absolute path.""")
    public static String HTTPD_HTTPS_KEYSTORE_FILE = "";
    @Key("settings.internal-webserver.https.keystore.type")
    @Comment("""
        https://docs.oracle.com/en/java/javase/21/docs/specs/security/standard-names.html#keystore-types""")
    public static String HTTPD_HTTPS_KEYSTORE_TYPE = "pkcs12";
    @Key("settings.internal-webserver.https.keystore.password")
    @Comment("""
        Password for your keystore""")
    public static String HTTPD_HTTPS_KEYSTORE_PASSWORD = "";

    private Config(@NotNull Path file) {
        super(file);
    }

    public static void reload() {
        new Config(LiveMap.instance().getDataDirectory().resolve("config.yml"));
    }
}
