package de.effms.jade.agent.search;

import de.effms.jade.agent.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAException;import java.lang.Override;

public class PeriodicSearchBehavior extends TickerBehaviour implements SearchBehavior
{
    private final DFAgentDescription query;

    private final SearchCallback callback;

    public PeriodicSearchBehavior(Agent agent, DFAgentDescription query, SearchCallback callback, long period)
    {
        super(agent.asJadeAgent(), period);

        this.query = query;
        this.callback = callback;
    }

    @Override
    public void onTick()
    {
        try {
            DFAgentDescription[] result = DFService.search(this.getAgent(), this.query);
            if (0 < result.length) {
                this.callback.onSearchResult(result, this.query);
            }
        } catch (FIPAException ex) {
            ex.printStackTrace();
        }
    }
}
