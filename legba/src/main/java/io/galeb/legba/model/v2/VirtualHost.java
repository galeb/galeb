package io.galeb.legba.model.v2;

public class VirtualHost {

    private String name;
    private String version;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public VirtualhostGroup getVirtualhostGroup() {
        return virtualhostGroup;
    }

    public void setVirtualhostGroup(VirtualhostGroup virtualhostGroup) {
        this.virtualhostGroup = virtualhostGroup;
    }

    private VirtualhostGroup virtualhostGroup;

}
