package fictioncraft.wintersteve25.ascensiontweaker.config.object;

import fictioncraft.wintersteve25.fclib.api.json.objects.SimpleConfigObject;
import fictioncraft.wintersteve25.fclib.api.json.objects.SimpleObjectMap;
import fictioncraft.wintersteve25.fclib.common.helper.MiscHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleAOAMap extends SimpleObjectMap{
    private final Map<String, List<SimpleAOAConfigObject>> configurations;

    public SimpleAOAMap(Map<String, List<SimpleAOAConfigObject>> configurations) {
        super(null);
        this.configurations = configurations;
    }

    @Override
    public Map<String, List<SimpleConfigObject>> getConfigs() {
        Map<String, List<SimpleConfigObject>> map = new HashMap<>();

        for (List<SimpleAOAConfigObject> list : configurations.values()) {
            List<SimpleConfigObject> list1 = new ArrayList<>();

            for (SimpleAOAConfigObject configObject : list) {
                list1.add(new SimpleConfigObject(configObject.getTarget()));
            }

            map.putIfAbsent(MiscHelper.getKeysWithValue(configurations, list).get(0), list1);
        }

        return map;
    }

    public Map<String, List<SimpleAOAConfigObject>> getConfig() {
        return configurations;
    }
}
