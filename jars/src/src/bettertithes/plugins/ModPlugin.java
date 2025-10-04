package bettertithes.plugins;

import bettertithes.listeners.LPTitheFleetHostilityListener;
import com.fs.starfarer.api.BaseModPlugin;

//upon paying tithes, make all pather fleets in the current system non-hostile for 30 days.
//tithes cost twice their original amount

public class ModPlugin extends BaseModPlugin {

    @Override
    public void onGameLoad(boolean newGame) {
        super.onGameLoad(newGame);

        LPTitheFleetHostilityListener.register();
    }
}
