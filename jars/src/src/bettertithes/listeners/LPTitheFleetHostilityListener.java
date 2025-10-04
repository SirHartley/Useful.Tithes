package bettertithes.listeners;

import bettertithes.memory.MemKeys;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCampaignEventListener;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.JumpPointAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.util.DelayedActionScript;
import com.fs.starfarer.api.util.Misc;

public class LPTitheFleetHostilityListener extends BaseCampaignEventListener {

    public static void register(){
        Global.getSector().addTransientListener(new LPTitheFleetHostilityListener());
    }

    public LPTitheFleetHostilityListener() {
        super(false);
    }

    @Override
    public void reportFleetJumped(CampaignFleetAPI fleet, SectorEntityToken from, JumpPointAPI.JumpDestination to) {
        super.reportFleetJumped(fleet, from, to);
        if (!fleet.isInHyperspace() && Factions.LUDDIC_PATH.equals(fleet.getFaction().getId()))
            makeNonHostileIfTithePaid(fleet);
    }

    @Override
    public void reportFleetSpawned(CampaignFleetAPI fleet) {
        super.reportFleetSpawned(fleet);
        if (Factions.LUDDIC_PATH.equals(fleet.getFaction().getId()))

            //listener triggers before fleet has location, we delay until the fleet is actually spawned, then do our checks
            fleet.addScript(new DelayedActionScript(0.001f) {
                @Override
                public void doAction() {
                    makeNonHostileIfTithePaid(fleet);
                }
            });
    }

    public MemoryAPI getSystemMemory(CampaignFleetAPI fleet){
        MemoryAPI mem = null;
        if (fleet != null && fleet.getContainingLocation() != null) mem = fleet.getContainingLocation().getMemoryWithoutUpdate();

        return mem;
    }

    public void makeNonHostileIfTithePaid(CampaignFleetAPI fleet){
        MemoryAPI mem = getSystemMemory(fleet);

        if (mem != null && mem.contains(MemKeys.LP_TITHE_PAID)){
            Misc.setFlagWithReason(fleet.getMemoryWithoutUpdate(), MemFlags.MEMORY_KEY_MAKE_NON_HOSTILE, MemKeys.LP_TITHE_PAID, true, mem.getExpire(MemKeys.LP_TITHE_PAID));
            Global.getLogger(LPTitheFleetHostilityListener.class).info("Making fleet " + fleet.getName() + " non-hostile for " + Math.round(mem.getExpire(MemKeys.LP_TITHE_PAID)) + " days");
        }
    }
}
