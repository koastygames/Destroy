package petrolpark.mc.destroy.mixin.plugin;

import petrolpark.mc.library.mixin.plugin.PetrolparkMixinPlugin;

public class DestroyMixinPlugin extends PetrolparkMixinPlugin {
    
    @Override
    protected String getMixinPackage() {
        return "com.destroy.mixin";
    };

    @Override
    public void onLoad(String mixinPackage) {
        //NOOP
    };
};
