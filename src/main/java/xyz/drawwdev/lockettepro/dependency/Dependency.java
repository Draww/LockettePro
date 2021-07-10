package xyz.drawwdev.lockettepro.dependency;

public interface Dependency {

    String getName();

    boolean isRequired();

    String getVersion();

    boolean load();

}
