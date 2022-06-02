package fictioncraft.wintersteve25.ascensiontweaker.config.object;

import fictioncraft.wintersteve25.fclib.api.json.objects.providers.obj.SimpleObjProvider;

public class SimpleAOALevelProvider extends SimpleObjProvider {
    private final int level;

    public SimpleAOALevelProvider(String name, int level) {
        super(name, false, "AOALevel");
        this.level = level;
    }

    public int getLevel() {
        return level;
    }
}
