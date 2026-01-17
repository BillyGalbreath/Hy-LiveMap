package net.pl3x.livemap.configuration;

import net.pl3x.livemap.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.simpleyaml.configuration.ConfigurationSection;
import org.simpleyaml.configuration.comments.CommentType;
import org.simpleyaml.configuration.file.YamlFile;
import org.simpleyaml.exceptions.InvalidConfigurationException;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public abstract class AbstractConfig {
    protected final Path file;

    protected YamlFile yaml;

    /**
     * Create a base YAML configuration.
     */
    protected AbstractConfig(@NotNull Path file) {
        this.file = file;

        reload0();
    }

    /**
     * Gets the direct YAML configuration for this config.
     *
     * @return YAML configuration
     */
    @NotNull
    public YamlFile yaml() {
        return Objects.requireNonNullElseGet(this.yaml,
            () -> this.yaml = new YamlFile(this.file.toFile()));
    }

    /**
     * Reloads configuration from YAML file.
     */
    private void reload0() {
        // read YAML from file
        try {
            yaml().createOrLoadWithComments();
        } catch (InvalidConfigurationException e) {
            Logger.severe("Could not load " + this.file.getFileName() + ", please correct your syntax errors", e);
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // load data from yaml
        Arrays.stream(getClass().getDeclaredFields()).forEach(field -> {
            Key key = field.getDeclaredAnnotation(Key.class);
            if (key == null) {
                return;
            }
            try {
                Object value = getAndSetDefault(key.value(), field.get(this));
                field.set(this, value instanceof String str ? str.translateEscapes() : value);
                Comment comment = field.getDeclaredAnnotation(Comment.class);
                if (comment != null) {
                    setComment(key.value(), comment.value());
                }
            } catch (Throwable e) {
                Logger.warning("Failed to load " + key.value() + " from " + this.file.getFileName().toString(), e);
            }
        });

        try {
            yaml().save();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the requested Object by path.
     * <p>
     * If the Object does not exist, this will set and return the default value.
     *
     * @param path Path of the Object to get
     * @param def  The default value to return if the path is not found
     * @return Requested Object
     */
    @Nullable
    protected Object getAndSetDefault(@NotNull String path, @Nullable Object def) {
        if (yaml().get(path) == null) {
            set(path, def);
        }
        return get(path, def);
    }

    /**
     * Sets the specified path to the given value.
     * <p>
     * If value is null, the entry will be removed. Any existing entry will be
     * replaced, regardless of what the new value is.
     *
     * @param path  Path of the object to set
     * @param value value to set the path to
     */
    protected void set(@NotNull String path, @Nullable Object value) {
        yaml().set(path, value);
    }

    /**
     * Gets the requested Object by path.
     * <p>
     * If the Object does not exist but a default value has been specified, this will return the default value. If the Object does not exist and no default value was specified, this will return null.
     *
     * @param path Path of the Object to get
     * @param def  The default value to return if the path is not found
     * @return Requested Object
     */
    @Nullable
    protected Object get(@NotNull String path, @Nullable Object def) {
        Object val = get(path);
        return val == null ? def : val;
    }

    /**
     * Gets the requested Object by path.
     *
     * @param path Path of the Object to get
     * @return Requested Object
     */
    @Nullable
    protected Object get(@NotNull String path) {
        Object value = yaml().get(path);
        if (!(value instanceof ConfigurationSection section)) {
            return value;
        }
        Map<String, Object> map = new LinkedHashMap<>();
        for (String key : section.getKeys(false)) {
            String rawValue = section.getString(key);
            if (rawValue == null) {
                continue;
            }
            map.put(key, string2Object(rawValue));
        }
        return map;
    }

    /**
     * Converts a string representation of an object into an Object.
     *
     * @param rawValue Raw String
     * @return New Object
     */
    @NotNull
    protected Object string2Object(@NotNull String rawValue) {
        return rawValue;
    }

    /**
     * Sets the comment at the specified path.
     * <p>
     * If value is null, the comment will be removed. If the path does
     * not exist, no comment will be set. Any existing comment will be
     * replaced, regardless of what the new comment is.
     * <p>
     * Use \n for newline.
     *
     * @param path    Path of the comment to set
     * @param comment New comment to set at the path
     */
    protected void setComment(@NotNull String path, @Nullable String comment) {
        yaml().setComment(path, comment, CommentType.BLOCK);
    }

    /**
     * Key for a YAML element.
     */
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Key {
        /**
         * Get value of this key.
         *
         * @return Key value
         */
        String value();
    }

    /**
     * Comment of a YAML element.
     */
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Comment {
        /**
         * Get value of this comment.
         *
         * @return Comment value
         */
        String value();
    }
}
