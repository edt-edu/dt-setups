transition ${t.name}
    first ${t.first}
    <#if t.guardCondition?has_content>
    ${t.guardCondition}
    </#if>
    then ${t.then};
    