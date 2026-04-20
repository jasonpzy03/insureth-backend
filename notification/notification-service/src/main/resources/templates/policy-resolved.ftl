<#import "base-layout.ftl" as layout>
<#import "components.ftl" as ui>

<@layout.mainLayout title="Insureth - Policy Resolution Update" preheader="Flight Policy Outcome Notification">
    <#if hasPayout == "true">
        <@ui.sectionTitle text="Claim paid — payout sent to your wallet" />
    <#else>
        <@ui.sectionTitle text="Your policy has been resolved" />
    </#if>
    
    <@ui.paragraph text="Dear <strong>${username!'Valued Customer'}</strong>," />
    
    <#if status == "Cancelled">
        <@ui.paragraph text="We are writing to inform you that your insured flight <strong>${flightNumber}</strong> has been resolved as <strong>Cancelled</strong>. Based on the parametric terms of your policy, a payout of <strong>${payoutEth!'0'} ETH</strong> has been successfully triggered and sent to your wallet address." />
    <#elseif hasPayout == "true">
        <@ui.paragraph text="We are writing to inform you that your insured flight <strong>${flightNumber}</strong> has been resolved with a status of <strong>${status}</strong>. Based on the parametric terms of your policy, a payout of <strong>${payoutEth!'0'} ETH</strong> has been successfully triggered and sent to your wallet address." />
    <#else>
        <@ui.paragraph text="We are writing to inform you that your insured flight <strong>${flightNumber}</strong> has been resolved with a status of <strong>${status}</strong>. As this outcome is within the on-time thresholds of your coverage, no payout was triggered for this specific policy." />
    </#if>

    <@ui.summaryTable 
        title="Resolution Details" 
        fields=[
            {"label": "Policy ID", "value": (policyId!'#')},
            {"label": "Flight Number", "value": flightNumber},
            {"label": "Route", "value": (departureAirportName!origin!'-') + " (" + (origin!'-') + ") to " + (arrivalAirportName!destination!'-') + " (" + (destination!'-') + ")"},
            {"label": "Final Status", "value": status},
            {"label": "Delay Minutes", "value": delayMinutes!"0"},
            {"label": "Payout Amount", "value": (payoutEth!'0') + " ETH"},
            {"label": "Resolution Time", "value": resolutionTime!'-'},
            {"label": "Transaction Hash", "value": (transactionHash!'-'), "breakAll": true}
        ]
    />

    <@ui.actionButton label="View Policy History" link="${managePoliciesLink!'http://localhost:4200/policies'}" />

    <p style="font-size: 14px; line-height: 1.6; margin: 0; color: #555555;">
        Thank you for using Insureth for your travel protection. If you have any questions regarding this resolution, you can view the full transaction details on-chain via your dashboard.
    </p>
</@layout.mainLayout>
