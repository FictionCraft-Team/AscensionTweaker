package fictioncraft.wintersteve25.ascensiontweaker.config.object;

import fictioncraft.wintersteve25.fclib.api.json.objects.SimpleConfigObject;
import fictioncraft.wintersteve25.fclib.api.json.objects.providers.SimpleObjProvider;

public class SimpleAOAConfigObject extends SimpleConfigObject {
    private final SimpleAOALevelProvider levelProvider;

    public SimpleAOAConfigObject(SimpleObjProvider target, SimpleAOALevelProvider levelProvider) {
        super(target);
        this.levelProvider = levelProvider;
    }

    public SimpleAOALevelProvider getLevelProvider() {
        return levelProvider;
    }
}
