package bettertithes.rules;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;

import java.util.List;
import java.util.Map;

public class SetSystemMemoryKey extends BaseCommandPlugin {

    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) return false;
        if (dialog.getInteractionTarget() == null) return false;

        String key = params.get(0).getString(memoryMap);
        float days = params.get(1).getFloat(memoryMap);

        if (!key.startsWith("$")) key = "$" + key;

        dialog.getInteractionTarget().getContainingLocation().getMemoryWithoutUpdate().set(key, true, days);

        return true;
    }
}
