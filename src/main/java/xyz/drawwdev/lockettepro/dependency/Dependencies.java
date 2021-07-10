package xyz.drawwdev.lockettepro.dependency;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class Dependencies {

    private final List<Dependency> dependencies = new ArrayList<>();

    public boolean register(@NotNull final Dependency dependency) {
        if (!dependency.load()) return false;
        return dependencies.add(dependency);
    }

    public boolean isLoaded(@NotNull final String pluginName) {
        return dependencies.stream().anyMatch(hook -> hook.getName().equals(pluginName));
    }

    @SuppressWarnings("unchecked")
    public <T extends Dependency> T get(@NotNull final String name) {
        return (T) dependencies.stream().filter(hook -> hook.getName().equals(name)).findFirst().orElse(null);
    }

    @SuppressWarnings("unchecked")
    public <T extends Dependency> T get(@NotNull final Class<T> clazz) {
        return (T) dependencies.stream().filter(hook -> hook.getClass().equals(clazz)).findFirst().orElse(null);
    }

    public List<Dependency> getDependencies() {
        return dependencies;
    }

}
