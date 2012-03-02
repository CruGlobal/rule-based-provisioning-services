package org.ccci.idm.rules.services;

import java.util.ArrayList;
import java.util.List;

public class RuleFilter
{
    private List<String> changedFacts = new ArrayList<String>();
    private List<String> rulesets = new ArrayList<String>();

    public boolean serviceMatches(RuleBasedRoleProvisioningService svc)
    {
        if(rulesets!=null && rulesets.size()>0)
        {
            if(!rulesets.contains(svc.getName())) return false;
        }
        
        if(rulesets==null || rulesets.size()==0) return true;
        
        for(String requiredFact : svc.getRequiredFacts())
        {
            if(changedFacts.contains(requiredFact)) return true;
        }
        
        return false;
    }

    public List<String> getChangedFacts()
    {
        return changedFacts;
    }

    public void setChangedFacts(List<String> facts)
    {
        this.changedFacts = facts;
    }

    public List<String> getRulesets()
    {
        return rulesets;
    }

    public void setRulesets(List<String> rulesets)
    {
        this.rulesets = rulesets;
    }

}
