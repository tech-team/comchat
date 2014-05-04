package layers.phy.settings;

import java.util.HashMap;
import java.util.Map;

public abstract class PhysicalLayerSettings {
    protected Map<PhySettings, Object> settings = new HashMap<>();

    public void add(PhySettings setting, Object obj) {
        settings.put(setting, obj);
    }

}
