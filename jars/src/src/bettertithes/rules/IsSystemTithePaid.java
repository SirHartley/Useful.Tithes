package bettertithes.rules;

import bettertithes.memory.MemKeys;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;

import java.util.List;
import java.util.Map;

public class IsSystemTithePaid extends BaseCommandPlugin {

    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) return false;
        if (dialog.getInteractionTarget() == null) return false;

        return dialog.getInteractionTarget().getContainingLocation().getMemoryWithoutUpdate().getBoolean(MemKeys.LP_TITHE_PAID);
    }
}
