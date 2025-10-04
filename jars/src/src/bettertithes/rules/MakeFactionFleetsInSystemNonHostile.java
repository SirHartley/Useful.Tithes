package bettertithes.rules;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;

import java.util.List;
import java.util.Map;

public class MakeFactionFleetsInSystemNonHostile extends BaseCommandPlugin {

    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) return false;
        if (dialog.getInteractionTarget() == null) return false;

        final String factionId = params.get(0).getString(memoryMap);
        String reason = params.get(1).getString(memoryMap);
        float days = params.get(2).getFloat(memoryMap);

        for (CampaignFleetAPI curr : dialog.getInteractionTarget().getContainingLocation().getFleets()) {
            if (factionId.equals(curr.getFaction().getId())){
                MemoryAPI memory = curr.getMemoryWithoutUpdate();
                Misc.setFlagWithReason(memory, MemFlags.MEMORY_KEY_MAKE_NON_HOSTILE, reason, true, days);
            }
        }

        return true;
    }
}
