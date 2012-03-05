package org.ccci.idm.rules.test;

import org.ccci.idm.rules.services.RuleFilter;
import org.ccci.idm.rules.webservice.RuleProvSvc;

public class DemoRuleProvSvc
{
    public static void main(String[] args) throws Exception
    {
        RuleProvSvc service = new RuleProvSvc();
        try
        {
            RuleFilter ruleFilter =new RuleFilter();
            ruleFilter.getChangedFacts().add("EmployeeInfo");
            service.runRules("SOA", "password-goes-here", "nathan.kopp@ccci.org", ruleFilter);
            System.out.println("finished!");
        }
        finally
        {
        }
    }
}
